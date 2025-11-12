/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Reference2FloatMap
 *  it.unimi.dsi.fastutil.objects.Reference2FloatMaps
 *  it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.worldupdate;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMaps;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RecreatingChunkStorage;
import net.minecraft.world.level.chunk.storage.RecreatingSimpleRegionStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class WorldUpgrader {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private static final String NEW_DIRECTORY_PREFIX = "new_";
    static final MutableComponent STATUS_UPGRADING_POI = Component.translatable("optimizeWorld.stage.upgrading.poi");
    static final MutableComponent STATUS_FINISHED_POI = Component.translatable("optimizeWorld.stage.finished.poi");
    static final MutableComponent STATUS_UPGRADING_ENTITIES = Component.translatable("optimizeWorld.stage.upgrading.entities");
    static final MutableComponent STATUS_FINISHED_ENTITIES = Component.translatable("optimizeWorld.stage.finished.entities");
    static final MutableComponent STATUS_UPGRADING_CHUNKS = Component.translatable("optimizeWorld.stage.upgrading.chunks");
    static final MutableComponent STATUS_FINISHED_CHUNKS = Component.translatable("optimizeWorld.stage.finished.chunks");
    final Registry<LevelStem> dimensions;
    final Set<ResourceKey<Level>> levels;
    final boolean eraseCache;
    final boolean recreateRegionFiles;
    final LevelStorageSource.LevelStorageAccess levelStorage;
    private final Thread thread;
    final DataFixer dataFixer;
    volatile boolean running = true;
    private volatile boolean finished;
    volatile float progress;
    volatile int totalChunks;
    volatile int totalFiles;
    volatile int converted;
    volatile int skipped;
    final Reference2FloatMap<ResourceKey<Level>> progressMap = Reference2FloatMaps.synchronize((Reference2FloatMap)new Reference2FloatOpenHashMap());
    volatile Component status = Component.translatable("optimizeWorld.stage.counting");
    static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    final DimensionDataStorage overworldDataStorage;

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, RegistryAccess registryAccess, boolean bl, boolean bl2) {
        this.dimensions = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
        this.levels = this.dimensions.registryKeySet().stream().map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet());
        this.eraseCache = bl;
        this.dataFixer = dataFixer;
        this.levelStorage = levelStorageAccess;
        this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(), dataFixer, registryAccess);
        this.recreateRegionFiles = bl2;
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Error upgrading world", throwable);
            this.status = Component.translatable("optimizeWorld.stage.failed");
            this.finished = true;
        });
        this.thread.start();
    }

    public void cancel() {
        this.running = false;
        try {
            this.thread.join();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void work() {
        long l = Util.getMillis();
        LOGGER.info("Upgrading entities");
        new EntityUpgrader(this).upgrade();
        LOGGER.info("Upgrading POIs");
        new PoiUpgrader(this).upgrade();
        LOGGER.info("Upgrading blocks");
        new ChunkUpgrader().upgrade();
        this.overworldDataStorage.save();
        l = Util.getMillis() - l;
        LOGGER.info("World optimizaton finished after {} seconds", (Object)(l / 1000L));
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    public float dimensionProgress(ResourceKey<Level> resourceKey) {
        return this.progressMap.getFloat(resourceKey);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public int getConverted() {
        return this.converted;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public Component getStatus() {
        return this.status;
    }

    static Path resolveRecreateDirectory(Path path) {
        return path.resolveSibling(NEW_DIRECTORY_PREFIX + path.getFileName().toString());
    }

    class EntityUpgrader
    extends SimpleRegionStorageUpgrader {
        EntityUpgrader(WorldUpgrader worldUpgrader) {
            super(DataFixTypes.ENTITY_CHUNK, "entities", STATUS_UPGRADING_ENTITIES, STATUS_FINISHED_ENTITIES);
        }

        @Override
        protected CompoundTag upgradeTag(SimpleRegionStorage simpleRegionStorage, CompoundTag compoundTag) {
            return simpleRegionStorage.upgradeChunkTag(compoundTag, -1);
        }
    }

    class PoiUpgrader
    extends SimpleRegionStorageUpgrader {
        PoiUpgrader(WorldUpgrader worldUpgrader) {
            super(DataFixTypes.POI_CHUNK, "poi", STATUS_UPGRADING_POI, STATUS_FINISHED_POI);
        }

        @Override
        protected CompoundTag upgradeTag(SimpleRegionStorage simpleRegionStorage, CompoundTag compoundTag) {
            return simpleRegionStorage.upgradeChunkTag(compoundTag, 1945);
        }
    }

    class ChunkUpgrader
    extends AbstractUpgrader<ChunkStorage> {
        ChunkUpgrader() {
            super(DataFixTypes.CHUNK, "chunk", "region", STATUS_UPGRADING_CHUNKS, STATUS_FINISHED_CHUNKS);
        }

        @Override
        protected boolean tryProcessOnePosition(ChunkStorage chunkStorage, ChunkPos chunkPos, ResourceKey<Level> resourceKey) {
            CompoundTag compoundTag = chunkStorage.read(chunkPos).join().orElse(null);
            if (compoundTag != null) {
                boolean bl;
                int n = ChunkStorage.getVersion(compoundTag);
                ChunkGenerator chunkGenerator = WorldUpgrader.this.dimensions.getOrThrow(Registries.levelToLevelStem(resourceKey)).generator();
                CompoundTag compoundTag2 = chunkStorage.upgradeChunkTag(resourceKey, () -> WorldUpgrader.this.overworldDataStorage, compoundTag, chunkGenerator.getTypeNameForDataFixer());
                ChunkPos chunkPos2 = new ChunkPos(compoundTag2.getInt("xPos"), compoundTag2.getInt("zPos"));
                if (!chunkPos2.equals(chunkPos)) {
                    LOGGER.warn("Chunk {} has invalid position {}", (Object)chunkPos, (Object)chunkPos2);
                }
                boolean bl2 = bl = n < SharedConstants.getCurrentVersion().getDataVersion().getVersion();
                if (WorldUpgrader.this.eraseCache) {
                    bl = bl || compoundTag2.contains("Heightmaps");
                    compoundTag2.remove("Heightmaps");
                    bl = bl || compoundTag2.contains("isLightOn");
                    compoundTag2.remove("isLightOn");
                    ListTag listTag = compoundTag2.getList("sections", 10);
                    for (int i = 0; i < listTag.size(); ++i) {
                        CompoundTag compoundTag3 = listTag.getCompound(i);
                        bl = bl || compoundTag3.contains("BlockLight");
                        compoundTag3.remove("BlockLight");
                        bl = bl || compoundTag3.contains("SkyLight");
                        compoundTag3.remove("SkyLight");
                    }
                }
                if (bl || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }
                    this.previousWriteFuture = chunkStorage.write(chunkPos, compoundTag2);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected ChunkStorage createStorage(RegionStorageInfo regionStorageInfo, Path path) {
            return WorldUpgrader.this.recreateRegionFiles ? new RecreatingChunkStorage(regionStorageInfo.withTypeSuffix("source"), path, regionStorageInfo.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory(path), WorldUpgrader.this.dataFixer, true) : new ChunkStorage(regionStorageInfo, path, WorldUpgrader.this.dataFixer, true);
        }

        @Override
        protected /* synthetic */ AutoCloseable createStorage(RegionStorageInfo regionStorageInfo, Path path) {
            return this.createStorage(regionStorageInfo, path);
        }
    }

    abstract class SimpleRegionStorageUpgrader
    extends AbstractUpgrader<SimpleRegionStorage> {
        SimpleRegionStorageUpgrader(DataFixTypes dataFixTypes, String string, MutableComponent mutableComponent, MutableComponent mutableComponent2) {
            super(dataFixTypes, string, string, mutableComponent, mutableComponent2);
        }

        @Override
        protected SimpleRegionStorage createStorage(RegionStorageInfo regionStorageInfo, Path path) {
            return WorldUpgrader.this.recreateRegionFiles ? new RecreatingSimpleRegionStorage(regionStorageInfo.withTypeSuffix("source"), path, regionStorageInfo.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory(path), WorldUpgrader.this.dataFixer, true, this.dataFixType) : new SimpleRegionStorage(regionStorageInfo, path, WorldUpgrader.this.dataFixer, true, this.dataFixType);
        }

        @Override
        protected boolean tryProcessOnePosition(SimpleRegionStorage simpleRegionStorage, ChunkPos chunkPos, ResourceKey<Level> resourceKey) {
            CompoundTag compoundTag = simpleRegionStorage.read(chunkPos).join().orElse(null);
            if (compoundTag != null) {
                boolean bl;
                int n = ChunkStorage.getVersion(compoundTag);
                CompoundTag compoundTag2 = this.upgradeTag(simpleRegionStorage, compoundTag);
                boolean bl2 = bl = n < SharedConstants.getCurrentVersion().getDataVersion().getVersion();
                if (bl || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }
                    this.previousWriteFuture = simpleRegionStorage.write(chunkPos, compoundTag2);
                    return true;
                }
            }
            return false;
        }

        protected abstract CompoundTag upgradeTag(SimpleRegionStorage var1, CompoundTag var2);

        @Override
        protected /* synthetic */ AutoCloseable createStorage(RegionStorageInfo regionStorageInfo, Path path) {
            return this.createStorage(regionStorageInfo, path);
        }
    }

    abstract class AbstractUpgrader<T extends AutoCloseable> {
        private final MutableComponent upgradingStatus;
        private final MutableComponent finishedStatus;
        private final String type;
        private final String folderName;
        @Nullable
        protected CompletableFuture<Void> previousWriteFuture;
        protected final DataFixTypes dataFixType;

        AbstractUpgrader(DataFixTypes dataFixTypes, String string, String string2, MutableComponent mutableComponent, MutableComponent mutableComponent2) {
            this.dataFixType = dataFixTypes;
            this.type = string;
            this.folderName = string2;
            this.upgradingStatus = mutableComponent;
            this.finishedStatus = mutableComponent2;
        }

        public void upgrade() {
            WorldUpgrader.this.totalFiles = 0;
            WorldUpgrader.this.totalChunks = 0;
            WorldUpgrader.this.converted = 0;
            WorldUpgrader.this.skipped = 0;
            List<DimensionToUpgrade<T>> list = this.getDimensionsToUpgrade();
            if (WorldUpgrader.this.totalChunks == 0) {
                return;
            }
            float f = WorldUpgrader.this.totalFiles;
            WorldUpgrader.this.status = this.upgradingStatus;
            while (WorldUpgrader.this.running) {
                boolean bl = false;
                float f2 = 0.0f;
                for (DimensionToUpgrade<T> dimensionToUpgrade : list) {
                    ResourceKey<Level> resourceKey = dimensionToUpgrade.dimensionKey;
                    ListIterator<FileToUpgrade> listIterator = dimensionToUpgrade.files;
                    AutoCloseable autoCloseable = (AutoCloseable)dimensionToUpgrade.storage;
                    if (listIterator.hasNext()) {
                        FileToUpgrade fileToUpgrade = listIterator.next();
                        boolean bl2 = true;
                        for (ChunkPos chunkPos : fileToUpgrade.chunksToUpgrade) {
                            bl2 = bl2 && this.processOnePosition(resourceKey, autoCloseable, chunkPos);
                            bl = true;
                        }
                        if (WorldUpgrader.this.recreateRegionFiles) {
                            if (bl2) {
                                this.onFileFinished(fileToUpgrade.file);
                            } else {
                                LOGGER.error("Failed to convert region file {}", (Object)fileToUpgrade.file.getPath());
                            }
                        }
                    }
                    float f3 = (float)listIterator.nextIndex() / f;
                    WorldUpgrader.this.progressMap.put(resourceKey, f3);
                    f2 += f3;
                }
                WorldUpgrader.this.progress = f2;
                if (bl) continue;
                break;
            }
            WorldUpgrader.this.status = this.finishedStatus;
            for (DimensionToUpgrade<T> dimensionToUpgrade : list) {
                try {
                    ((AutoCloseable)dimensionToUpgrade.storage).close();
                }
                catch (Exception exception) {
                    LOGGER.error("Error upgrading chunk", (Throwable)exception);
                }
            }
        }

        private List<DimensionToUpgrade<T>> getDimensionsToUpgrade() {
            ArrayList arrayList = Lists.newArrayList();
            for (ResourceKey<Level> resourceKey : WorldUpgrader.this.levels) {
                RegionStorageInfo regionStorageInfo = new RegionStorageInfo(WorldUpgrader.this.levelStorage.getLevelId(), resourceKey, this.type);
                Path path = WorldUpgrader.this.levelStorage.getDimensionPath(resourceKey).resolve(this.folderName);
                T t = this.createStorage(regionStorageInfo, path);
                ListIterator<FileToUpgrade> listIterator = this.getFilesToProcess(regionStorageInfo, path);
                arrayList.add(new DimensionToUpgrade<T>(resourceKey, t, listIterator));
            }
            return arrayList;
        }

        protected abstract T createStorage(RegionStorageInfo var1, Path var2);

        private ListIterator<FileToUpgrade> getFilesToProcess(RegionStorageInfo regionStorageInfo, Path path) {
            List<FileToUpgrade> list = AbstractUpgrader.getAllChunkPositions(regionStorageInfo, path);
            WorldUpgrader.this.totalFiles += list.size();
            WorldUpgrader.this.totalChunks += list.stream().mapToInt(fileToUpgrade -> fileToUpgrade.chunksToUpgrade.size()).sum();
            return list.listIterator();
        }

        private static List<FileToUpgrade> getAllChunkPositions(RegionStorageInfo regionStorageInfo, Path path) {
            File[] fileArray = path.toFile().listFiles((file, string) -> string.endsWith(".mca"));
            if (fileArray == null) {
                return List.of();
            }
            ArrayList arrayList = Lists.newArrayList();
            for (File file2 : fileArray) {
                Matcher matcher = REGEX.matcher(file2.getName());
                if (!matcher.matches()) continue;
                int n = Integer.parseInt(matcher.group(1)) << 5;
                int n2 = Integer.parseInt(matcher.group(2)) << 5;
                ArrayList arrayList2 = Lists.newArrayList();
                try (RegionFile regionFile = new RegionFile(regionStorageInfo, file2.toPath(), path, true);){
                    for (int i = 0; i < 32; ++i) {
                        for (int j = 0; j < 32; ++j) {
                            ChunkPos chunkPos = new ChunkPos(i + n, j + n2);
                            if (!regionFile.doesChunkExist(chunkPos)) continue;
                            arrayList2.add(chunkPos);
                        }
                    }
                    if (arrayList2.isEmpty()) continue;
                    arrayList.add(new FileToUpgrade(regionFile, arrayList2));
                }
                catch (Throwable throwable) {
                    LOGGER.error("Failed to read chunks from region file {}", (Object)file2.toPath(), (Object)throwable);
                }
            }
            return arrayList;
        }

        private boolean processOnePosition(ResourceKey<Level> resourceKey, T t, ChunkPos chunkPos) {
            boolean bl = false;
            try {
                bl = this.tryProcessOnePosition(t, chunkPos, resourceKey);
            }
            catch (CompletionException | ReportedException runtimeException) {
                Throwable throwable = runtimeException.getCause();
                if (throwable instanceof IOException) {
                    LOGGER.error("Error upgrading chunk {}", (Object)chunkPos, (Object)throwable);
                }
                throw runtimeException;
            }
            if (bl) {
                ++WorldUpgrader.this.converted;
            } else {
                ++WorldUpgrader.this.skipped;
            }
            return bl;
        }

        protected abstract boolean tryProcessOnePosition(T var1, ChunkPos var2, ResourceKey<Level> var3);

        private void onFileFinished(RegionFile regionFile) {
            if (!WorldUpgrader.this.recreateRegionFiles) {
                return;
            }
            if (this.previousWriteFuture != null) {
                this.previousWriteFuture.join();
            }
            Path path = regionFile.getPath();
            Path path2 = path.getParent();
            Path path3 = WorldUpgrader.resolveRecreateDirectory(path2).resolve(path.getFileName().toString());
            try {
                if (path3.toFile().exists()) {
                    Files.delete(path);
                    Files.move(path3, path, new CopyOption[0]);
                } else {
                    LOGGER.error("Failed to replace an old region file. New file {} does not exist.", (Object)path3);
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to replace an old region file", (Throwable)iOException);
            }
        }
    }

    static final class FileToUpgrade
    extends Record {
        final RegionFile file;
        final List<ChunkPos> chunksToUpgrade;

        FileToUpgrade(RegionFile regionFile, List<ChunkPos> list) {
            this.file = regionFile;
            this.chunksToUpgrade = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this, object);
        }

        public RegionFile file() {
            return this.file;
        }

        public List<ChunkPos> chunksToUpgrade() {
            return this.chunksToUpgrade;
        }
    }

    static final class DimensionToUpgrade<T>
    extends Record {
        final ResourceKey<Level> dimensionKey;
        final T storage;
        final ListIterator<FileToUpgrade> files;

        DimensionToUpgrade(ResourceKey<Level> resourceKey, T t, ListIterator<FileToUpgrade> listIterator) {
            this.dimensionKey = resourceKey;
            this.storage = t;
            this.files = listIterator;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this, object);
        }

        public ResourceKey<Level> dimensionKey() {
            return this.dimensionKey;
        }

        public T storage() {
            return this.storage;
        }

        public ListIterator<FileToUpgrade> files() {
            return this.files;
        }
    }
}

