/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage
implements AutoCloseable {
    public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
    private final IOWorker worker;
    protected final DataFixer fixerUpper;
    @Nullable
    private volatile LegacyStructureDataHandler legacyStructureHandler;

    public ChunkStorage(RegionStorageInfo regionStorageInfo, Path path, DataFixer dataFixer, boolean bl) {
        this.fixerUpper = dataFixer;
        this.worker = new IOWorker(regionStorageInfo, path, bl);
    }

    public boolean isOldChunkAround(ChunkPos chunkPos, int n) {
        return this.worker.isOldChunkAround(chunkPos, n);
    }

    public CompoundTag upgradeChunkTag(ResourceKey<Level> resourceKey, Supplier<DimensionDataStorage> supplier, CompoundTag compoundTag, Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> optional) {
        int n = ChunkStorage.getVersion(compoundTag);
        if (n == SharedConstants.getCurrentVersion().getDataVersion().getVersion()) {
            return compoundTag;
        }
        try {
            if (n < 1493 && (compoundTag = DataFixTypes.CHUNK.update(this.fixerUpper, compoundTag, n, 1493)).getCompound("Level").getBoolean("hasLegacyStructureData")) {
                LegacyStructureDataHandler legacyStructureDataHandler = this.getLegacyStructureHandler(resourceKey, supplier);
                compoundTag = legacyStructureDataHandler.updateFromLegacy(compoundTag);
            }
            ChunkStorage.injectDatafixingContext(compoundTag, resourceKey, optional);
            compoundTag = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, compoundTag, Math.max(1493, n));
            ChunkStorage.removeDatafixingContext(compoundTag);
            NbtUtils.addCurrentDataVersion(compoundTag);
            return compoundTag;
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Updated chunk");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Updated chunk details");
            crashReportCategory.setDetail("Data version", n);
            throw new ReportedException(crashReport);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourceKey, Supplier<DimensionDataStorage> supplier) {
        LegacyStructureDataHandler legacyStructureDataHandler = this.legacyStructureHandler;
        if (legacyStructureDataHandler == null) {
            ChunkStorage chunkStorage = this;
            synchronized (chunkStorage) {
                legacyStructureDataHandler = this.legacyStructureHandler;
                if (legacyStructureDataHandler == null) {
                    this.legacyStructureHandler = legacyStructureDataHandler = LegacyStructureDataHandler.getLegacyStructureHandler(resourceKey, supplier.get());
                }
            }
        }
        return legacyStructureDataHandler;
    }

    public static void injectDatafixingContext(CompoundTag compoundTag, ResourceKey<Level> resourceKey2, Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> optional) {
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.putString("dimension", resourceKey2.location().toString());
        optional.ifPresent(resourceKey -> compoundTag2.putString("generator", resourceKey.location().toString()));
        compoundTag.put("__context", compoundTag2);
    }

    private static void removeDatafixingContext(CompoundTag compoundTag) {
        compoundTag.remove("__context");
    }

    public static int getVersion(CompoundTag compoundTag) {
        return NbtUtils.getDataVersion(compoundTag, -1);
    }

    public CompletableFuture<Optional<CompoundTag>> read(ChunkPos chunkPos) {
        return this.worker.loadAsync(chunkPos);
    }

    public CompletableFuture<Void> write(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.handleLegacyStructureIndex(chunkPos);
        return this.worker.store(chunkPos, compoundTag);
    }

    protected void handleLegacyStructureIndex(ChunkPos chunkPos) {
        if (this.legacyStructureHandler != null) {
            this.legacyStructureHandler.removeIndex(chunkPos.toLong());
        }
    }

    public void flushWorker() {
        this.worker.synchronize(true).join();
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }

    public ChunkScanAccess chunkScanner() {
        return this.worker;
    }

    protected RegionStorageInfo storageInfo() {
        return this.worker.storageInfo();
    }
}

