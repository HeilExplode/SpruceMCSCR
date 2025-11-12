/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.io.Files
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.FileNameDateFormatter;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraft.world.level.validation.PathAllowList;
import org.slf4j.Logger;

public class LevelStorageSource {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();
    private static final String TAG_DATA = "Data";
    private static final PathMatcher NO_SYMLINKS_ALLOWED = path -> false;
    public static final String ALLOWED_SYMLINKS_CONFIG_NAME = "allowed_symlinks.txt";
    private static final int UNCOMPRESSED_NBT_QUOTA = 0x6400000;
    private static final int DISK_SPACE_WARNING_THRESHOLD = 0x4000000;
    private final Path baseDir;
    private final Path backupDir;
    final DataFixer fixerUpper;
    private final DirectoryValidator worldDirValidator;

    public LevelStorageSource(Path path, Path path2, DirectoryValidator directoryValidator, DataFixer dataFixer) {
        this.fixerUpper = dataFixer;
        try {
            FileUtil.createDirectoriesSafe(path);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
        this.baseDir = path;
        this.backupDir = path2;
        this.worldDirValidator = directoryValidator;
    }

    public static DirectoryValidator parseValidator(Path path) {
        if (Files.exists(path, new LinkOption[0])) {
            DirectoryValidator directoryValidator;
            block9: {
                BufferedReader bufferedReader = Files.newBufferedReader(path);
                try {
                    directoryValidator = new DirectoryValidator(PathAllowList.readPlain(bufferedReader));
                    if (bufferedReader == null) break block9;
                }
                catch (Throwable throwable) {
                    try {
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (Exception exception) {
                        LOGGER.error("Failed to parse {}, disallowing all symbolic links", (Object)ALLOWED_SYMLINKS_CONFIG_NAME, (Object)exception);
                    }
                }
                bufferedReader.close();
            }
            return directoryValidator;
        }
        return new DirectoryValidator(NO_SYMLINKS_ALLOWED);
    }

    public static LevelStorageSource createDefault(Path path) {
        DirectoryValidator directoryValidator = LevelStorageSource.parseValidator(path.resolve(ALLOWED_SYMLINKS_CONFIG_NAME));
        return new LevelStorageSource(path, path.resolve("../backups"), directoryValidator, DataFixers.getDataFixer());
    }

    public static WorldDataConfiguration readDataConfig(Dynamic<?> dynamic) {
        return WorldDataConfiguration.CODEC.parse(dynamic).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElse(WorldDataConfiguration.DEFAULT);
    }

    public static WorldLoader.PackConfig getPackConfig(Dynamic<?> dynamic, PackRepository packRepository, boolean bl) {
        return new WorldLoader.PackConfig(packRepository, LevelStorageSource.readDataConfig(dynamic), bl, false);
    }

    public static LevelDataAndDimensions getLevelDataAndDimensions(Dynamic<?> dynamic, WorldDataConfiguration worldDataConfiguration, Registry<LevelStem> registry, RegistryAccess.Frozen frozen) {
        Dynamic<?> dynamic2 = RegistryOps.injectRegistryContext(dynamic, frozen);
        Dynamic dynamic3 = dynamic2.get("WorldGenSettings").orElseEmptyMap();
        WorldGenSettings worldGenSettings = (WorldGenSettings)WorldGenSettings.CODEC.parse(dynamic3).getOrThrow();
        LevelSettings levelSettings = LevelSettings.parse(dynamic2, worldDataConfiguration);
        WorldDimensions.Complete complete = worldGenSettings.dimensions().bake(registry);
        Lifecycle lifecycle = complete.lifecycle().add(frozen.allRegistriesLifecycle());
        PrimaryLevelData primaryLevelData = PrimaryLevelData.parse(dynamic2, levelSettings, complete.specialWorldProperty(), worldGenSettings.options(), lifecycle);
        return new LevelDataAndDimensions(primaryLevelData, complete);
    }

    public String getName() {
        return "Anvil";
    }

    public LevelCandidates findLevelCandidates() throws LevelStorageException {
        LevelCandidates levelCandidates;
        block9: {
            if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
                throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
            }
            Stream<Path> stream = Files.list(this.baseDir);
            try {
                List<LevelDirectory> list = stream.filter(path -> Files.isDirectory(path, new LinkOption[0])).map(LevelDirectory::new).filter(levelDirectory -> Files.isRegularFile(levelDirectory.dataFile(), new LinkOption[0]) || Files.isRegularFile(levelDirectory.oldDataFile(), new LinkOption[0])).toList();
                levelCandidates = new LevelCandidates(list);
                if (stream == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
                }
            }
            stream.close();
        }
        return levelCandidates;
    }

    public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelCandidates levelCandidates) {
        ArrayList<CompletableFuture<LevelSummary>> arrayList = new ArrayList<CompletableFuture<LevelSummary>>(levelCandidates.levels.size());
        for (LevelDirectory levelDirectory : levelCandidates.levels) {
            arrayList.add(CompletableFuture.supplyAsync(() -> {
                boolean bl;
                try {
                    bl = DirectoryLock.isLocked(levelDirectory.path());
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to read {} lock", (Object)levelDirectory.path(), (Object)exception);
                    return null;
                }
                try {
                    return this.readLevelSummary(levelDirectory, bl);
                }
                catch (OutOfMemoryError outOfMemoryError) {
                    MemoryReserve.release();
                    System.gc();
                    String string = "Ran out of memory trying to read summary of world folder \"" + levelDirectory.directoryName() + "\"";
                    LOGGER.error(LogUtils.FATAL_MARKER, string);
                    OutOfMemoryError outOfMemoryError2 = new OutOfMemoryError("Ran out of memory reading level data");
                    outOfMemoryError2.initCause(outOfMemoryError);
                    CrashReport crashReport = CrashReport.forThrowable(outOfMemoryError2, string);
                    CrashReportCategory crashReportCategory = crashReport.addCategory("World details");
                    crashReportCategory.setDetail("Folder Name", levelDirectory.directoryName());
                    try {
                        long l = Files.size(levelDirectory.dataFile());
                        crashReportCategory.setDetail("level.dat size", l);
                    }
                    catch (IOException iOException) {
                        crashReportCategory.setDetailError("level.dat size", iOException);
                    }
                    throw new ReportedException(crashReport);
                }
            }, Util.backgroundExecutor()));
        }
        return Util.sequenceFailFastAndCancel(arrayList).thenApply(list -> list.stream().filter(Objects::nonNull).sorted().toList());
    }

    private int getStorageVersion() {
        return 19133;
    }

    static CompoundTag readLevelDataTagRaw(Path path) throws IOException {
        return NbtIo.readCompressed(path, NbtAccounter.create(0x6400000L));
    }

    static Dynamic<?> readLevelDataTagFixed(Path path, DataFixer dataFixer) throws IOException {
        CompoundTag compoundTag = LevelStorageSource.readLevelDataTagRaw(path);
        CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
        int n = NbtUtils.getDataVersion(compoundTag2, -1);
        Dynamic dynamic2 = DataFixTypes.LEVEL.updateToCurrentVersion(dataFixer, new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag2), n);
        dynamic2 = dynamic2.update("Player", dynamic -> DataFixTypes.PLAYER.updateToCurrentVersion(dataFixer, dynamic, n));
        dynamic2 = dynamic2.update("WorldGenSettings", dynamic -> DataFixTypes.WORLD_GEN_SETTINGS.updateToCurrentVersion(dataFixer, dynamic, n));
        return dynamic2;
    }

    private LevelSummary readLevelSummary(LevelDirectory levelDirectory, boolean bl) {
        Path path = levelDirectory.dataFile();
        if (Files.exists(path, new LinkOption[0])) {
            try {
                Object object;
                if (Files.isSymbolicLink(path) && !(object = this.worldDirValidator.validateSymlink(path)).isEmpty()) {
                    LOGGER.warn("{}", (Object)ContentValidationException.getMessage(path, (List<ForbiddenSymlinkInfo>)object));
                    return new LevelSummary.SymlinkLevelSummary(levelDirectory.directoryName(), levelDirectory.iconFile());
                }
                object = LevelStorageSource.readLightweightData(path);
                if (object instanceof CompoundTag) {
                    CompoundTag compoundTag = (CompoundTag)object;
                    CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
                    int n = NbtUtils.getDataVersion(compoundTag2, -1);
                    Dynamic dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(this.fixerUpper, new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag2), n);
                    return this.makeLevelSummary(dynamic, levelDirectory, bl);
                }
                LOGGER.warn("Invalid root tag in {}", (Object)path);
            }
            catch (Exception exception) {
                LOGGER.error("Exception reading {}", (Object)path, (Object)exception);
            }
        }
        return new LevelSummary.CorruptedLevelSummary(levelDirectory.directoryName(), levelDirectory.iconFile(), LevelStorageSource.getFileModificationTime(levelDirectory));
    }

    private static long getFileModificationTime(LevelDirectory levelDirectory) {
        Instant instant = LevelStorageSource.getFileModificationTime(levelDirectory.dataFile());
        if (instant == null) {
            instant = LevelStorageSource.getFileModificationTime(levelDirectory.oldDataFile());
        }
        return instant == null ? -1L : instant.toEpochMilli();
    }

    @Nullable
    static Instant getFileModificationTime(Path path) {
        try {
            return Files.getLastModifiedTime(path, new LinkOption[0]).toInstant();
        }
        catch (IOException iOException) {
            return null;
        }
    }

    LevelSummary makeLevelSummary(Dynamic<?> dynamic, LevelDirectory levelDirectory, boolean bl) {
        LevelVersion levelVersion = LevelVersion.parse(dynamic);
        int n = levelVersion.levelDataVersion();
        if (n == 19132 || n == 19133) {
            boolean bl2 = n != this.getStorageVersion();
            Path path = levelDirectory.iconFile();
            WorldDataConfiguration worldDataConfiguration = LevelStorageSource.readDataConfig(dynamic);
            LevelSettings levelSettings = LevelSettings.parse(dynamic, worldDataConfiguration);
            FeatureFlagSet featureFlagSet = LevelStorageSource.parseFeatureFlagsFromSummary(dynamic);
            boolean bl3 = FeatureFlags.isExperimental(featureFlagSet);
            return new LevelSummary(levelSettings, levelVersion, levelDirectory.directoryName(), bl2, bl, bl3, path);
        }
        throw new NbtFormatException("Unknown data version: " + Integer.toHexString(n));
    }

    private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<?> dynamic2) {
        Set<ResourceLocation> set = dynamic2.get("enabled_features").asStream().flatMap(dynamic -> dynamic.asString().result().map(ResourceLocation::tryParse).stream()).collect(Collectors.toSet());
        return FeatureFlags.REGISTRY.fromNames(set, resourceLocation -> {});
    }

    @Nullable
    private static Tag readLightweightData(Path path) throws IOException {
        SkipFields skipFields = new SkipFields(new FieldSelector(TAG_DATA, CompoundTag.TYPE, "Player"), new FieldSelector(TAG_DATA, CompoundTag.TYPE, "WorldGenSettings"));
        NbtIo.parseCompressed(path, (StreamTagVisitor)skipFields, NbtAccounter.create(0x6400000L));
        return skipFields.getResult();
    }

    public boolean isNewLevelIdAcceptable(String string) {
        try {
            Path path = this.getLevelPath(string);
            Files.createDirectory(path, new FileAttribute[0]);
            Files.deleteIfExists(path);
            return true;
        }
        catch (IOException iOException) {
            return false;
        }
    }

    public boolean levelExists(String string) {
        try {
            return Files.isDirectory(this.getLevelPath(string), new LinkOption[0]);
        }
        catch (InvalidPathException invalidPathException) {
            return false;
        }
    }

    public Path getLevelPath(String string) {
        return this.baseDir.resolve(string);
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageAccess validateAndCreateAccess(String string) throws IOException, ContentValidationException {
        Path path = this.getLevelPath(string);
        List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateDirectory(path, true);
        if (!list.isEmpty()) {
            throw new ContentValidationException(path, list);
        }
        return new LevelStorageAccess(string, path);
    }

    public LevelStorageAccess createAccess(String string) throws IOException {
        Path path = this.getLevelPath(string);
        return new LevelStorageAccess(string, path);
    }

    public DirectoryValidator getWorldDirValidator() {
        return this.worldDirValidator;
    }

    public static final class LevelCandidates
    extends Record
    implements Iterable<LevelDirectory> {
        final List<LevelDirectory> levels;

        public LevelCandidates(List<LevelDirectory> list) {
            this.levels = list;
        }

        public boolean isEmpty() {
            return this.levels.isEmpty();
        }

        @Override
        public Iterator<LevelDirectory> iterator() {
            return this.levels.iterator();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LevelCandidates.class, "levels", "levels"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LevelCandidates.class, "levels", "levels"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LevelCandidates.class, "levels", "levels"}, this, object);
        }

        public List<LevelDirectory> levels() {
            return this.levels;
        }
    }

    public static final class LevelDirectory
    extends Record {
        final Path path;

        public LevelDirectory(Path path) {
            this.path = path;
        }

        public String directoryName() {
            return this.path.getFileName().toString();
        }

        public Path dataFile() {
            return this.resourcePath(LevelResource.LEVEL_DATA_FILE);
        }

        public Path oldDataFile() {
            return this.resourcePath(LevelResource.OLD_LEVEL_DATA_FILE);
        }

        public Path corruptedDataFile(LocalDateTime localDateTime) {
            return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_corrupted_" + localDateTime.format(FORMATTER));
        }

        public Path rawDataFile(LocalDateTime localDateTime) {
            return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_raw_" + localDateTime.format(FORMATTER));
        }

        public Path iconFile() {
            return this.resourcePath(LevelResource.ICON_FILE);
        }

        public Path lockFile() {
            return this.resourcePath(LevelResource.LOCK_FILE);
        }

        public Path resourcePath(LevelResource levelResource) {
            return this.path.resolve(levelResource.getId());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LevelDirectory.class, "path", "path"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LevelDirectory.class, "path", "path"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LevelDirectory.class, "path", "path"}, this, object);
        }

        public Path path() {
            return this.path;
        }
    }

    public class LevelStorageAccess
    implements AutoCloseable {
        final DirectoryLock lock;
        final LevelDirectory levelDirectory;
        private final String levelId;
        private final Map<LevelResource, Path> resources = Maps.newHashMap();

        LevelStorageAccess(String string, Path path) throws IOException {
            this.levelId = string;
            this.levelDirectory = new LevelDirectory(path);
            this.lock = DirectoryLock.create(path);
        }

        public long estimateDiskSpace() {
            try {
                return Files.getFileStore(this.levelDirectory.path).getUsableSpace();
            }
            catch (Exception exception) {
                return Long.MAX_VALUE;
            }
        }

        public boolean checkForLowDiskSpace() {
            return this.estimateDiskSpace() < 0x4000000L;
        }

        public void safeClose() {
            try {
                this.close();
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to unlock access to level {}", (Object)this.getLevelId(), (Object)iOException);
            }
        }

        public LevelStorageSource parent() {
            return LevelStorageSource.this;
        }

        public LevelDirectory getLevelDirectory() {
            return this.levelDirectory;
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(LevelResource levelResource) {
            return this.resources.computeIfAbsent(levelResource, this.levelDirectory::resourcePath);
        }

        public Path getDimensionPath(ResourceKey<Level> resourceKey) {
            return DimensionType.getStorageFolder(resourceKey, this.levelDirectory.path());
        }

        private void checkLock() {
            if (!this.lock.isValid()) {
                throw new IllegalStateException("Lock is no longer valid");
            }
        }

        public PlayerDataStorage createPlayerStorage() {
            this.checkLock();
            return new PlayerDataStorage(this, LevelStorageSource.this.fixerUpper);
        }

        public LevelSummary getSummary(Dynamic<?> dynamic) {
            this.checkLock();
            return LevelStorageSource.this.makeLevelSummary(dynamic, this.levelDirectory, false);
        }

        public Dynamic<?> getDataTag() throws IOException {
            return this.getDataTag(false);
        }

        public Dynamic<?> getDataTagFallback() throws IOException {
            return this.getDataTag(true);
        }

        private Dynamic<?> getDataTag(boolean bl) throws IOException {
            this.checkLock();
            return LevelStorageSource.readLevelDataTagFixed(bl ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile(), LevelStorageSource.this.fixerUpper);
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData) {
            this.saveDataTag(registryAccess, worldData, null);
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData, @Nullable CompoundTag compoundTag) {
            CompoundTag compoundTag2 = worldData.createTag(registryAccess, compoundTag);
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.put(LevelStorageSource.TAG_DATA, compoundTag2);
            this.saveLevelData(compoundTag3);
        }

        private void saveLevelData(CompoundTag compoundTag) {
            Path path = this.levelDirectory.path();
            try {
                Path path2 = Files.createTempFile(path, "level", ".dat", new FileAttribute[0]);
                NbtIo.writeCompressed(compoundTag, path2);
                Path path3 = this.levelDirectory.oldDataFile();
                Path path4 = this.levelDirectory.dataFile();
                Util.safeReplaceFile(path4, path2, path3);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to save level {}", (Object)path, (Object)exception);
            }
        }

        public Optional<Path> getIconFile() {
            if (!this.lock.isValid()) {
                return Optional.empty();
            }
            return Optional.of(this.levelDirectory.iconFile());
        }

        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path path = this.levelDirectory.lockFile();
            LOGGER.info("Deleting level {}", (Object)this.levelId);
            for (int i = 1; i <= 5; ++i) {
                LOGGER.info("Attempt {}...", (Object)i);
                try {
                    Files.walkFileTree(this.levelDirectory.path(), (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                        @Override
                        public FileVisitResult visitFile(Path path2, BasicFileAttributes basicFileAttributes) throws IOException {
                            if (!path2.equals(path)) {
                                LOGGER.debug("Deleting {}", (Object)path2);
                                Files.delete(path2);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path path2, @Nullable IOException iOException) throws IOException {
                            if (iOException != null) {
                                throw iOException;
                            }
                            if (path2.equals(LevelStorageAccess.this.levelDirectory.path())) {
                                LevelStorageAccess.this.lock.close();
                                Files.deleteIfExists(path);
                            }
                            Files.delete(path2);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public /* synthetic */ FileVisitResult postVisitDirectory(Object object, @Nullable IOException iOException) throws IOException {
                            return this.postVisitDirectory((Path)object, iOException);
                        }

                        @Override
                        public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                            return this.visitFile((Path)object, basicFileAttributes);
                        }
                    });
                    break;
                }
                catch (IOException iOException) {
                    if (i < 5) {
                        LOGGER.warn("Failed to delete {}", (Object)this.levelDirectory.path(), (Object)iOException);
                        try {
                            Thread.sleep(500L);
                        }
                        catch (InterruptedException interruptedException) {}
                        continue;
                    }
                    throw iOException;
                }
            }
        }

        public void renameLevel(String string) throws IOException {
            this.modifyLevelDataWithoutDatafix(compoundTag -> compoundTag.putString("LevelName", string.trim()));
        }

        public void renameAndDropPlayer(String string) throws IOException {
            this.modifyLevelDataWithoutDatafix(compoundTag -> {
                compoundTag.putString("LevelName", string.trim());
                compoundTag.remove("Player");
            });
        }

        private void modifyLevelDataWithoutDatafix(Consumer<CompoundTag> consumer) throws IOException {
            this.checkLock();
            CompoundTag compoundTag = LevelStorageSource.readLevelDataTagRaw(this.levelDirectory.dataFile());
            consumer.accept(compoundTag.getCompound(LevelStorageSource.TAG_DATA));
            this.saveLevelData(compoundTag);
        }

        public long makeWorldBackup() throws IOException {
            this.checkLock();
            String string = LocalDateTime.now().format(FORMATTER) + "_" + this.levelId;
            Path path = LevelStorageSource.this.getBackupPath();
            try {
                FileUtil.createDirectoriesSafe(path);
            }
            catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Path path2 = path.resolve(FileUtil.findAvailableName(path, string, ".zip"));
            try (final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path2, new OpenOption[0])));){
                final Path path3 = Paths.get(this.levelId, new String[0]);
                Files.walkFileTree(this.levelDirectory.path(), (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        if (path.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        }
                        String string = path3.resolve(LevelStorageAccess.this.levelDirectory.path().relativize(path)).toString().replace('\\', '/');
                        ZipEntry zipEntry = new ZipEntry(string);
                        zipOutputStream.putNextEntry(zipEntry);
                        com.google.common.io.Files.asByteSource((File)path.toFile()).copyTo((OutputStream)zipOutputStream);
                        zipOutputStream.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                        return this.visitFile((Path)object, basicFileAttributes);
                    }
                });
            }
            return Files.size(path2);
        }

        public boolean hasWorldData() {
            return Files.exists(this.levelDirectory.dataFile(), new LinkOption[0]) || Files.exists(this.levelDirectory.oldDataFile(), new LinkOption[0]);
        }

        @Override
        public void close() throws IOException {
            this.lock.close();
        }

        public boolean restoreLevelDataFromOld() {
            return Util.safeReplaceOrMoveFile(this.levelDirectory.dataFile(), this.levelDirectory.oldDataFile(), this.levelDirectory.corruptedDataFile(LocalDateTime.now()), true);
        }

        @Nullable
        public Instant getFileModificationTime(boolean bl) {
            return LevelStorageSource.getFileModificationTime(bl ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile());
        }
    }
}

