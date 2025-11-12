/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  javax.annotation.Nullable
 *  org.apache.commons.io.FileUtils
 */
package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.apache.commons.io.FileUtils;

public class RecreatingSimpleRegionStorage
extends SimpleRegionStorage {
    private final IOWorker writeWorker;
    private final Path writeFolder;

    public RecreatingSimpleRegionStorage(RegionStorageInfo regionStorageInfo, Path path, RegionStorageInfo regionStorageInfo2, Path path2, DataFixer dataFixer, boolean bl, DataFixTypes dataFixTypes) {
        super(regionStorageInfo, path, dataFixer, bl, dataFixTypes);
        this.writeFolder = path2;
        this.writeWorker = new IOWorker(regionStorageInfo2, path2, bl);
    }

    @Override
    public CompletableFuture<Void> write(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
        return this.writeWorker.store(chunkPos, compoundTag);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.writeWorker.close();
        if (this.writeFolder.toFile().exists()) {
            FileUtils.deleteDirectory((File)this.writeFolder.toFile());
        }
    }
}

