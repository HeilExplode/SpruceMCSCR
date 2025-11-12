/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level.progress;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.slf4j.Logger;

public class LoggerChunkProgressListener
implements ChunkProgressListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int maxCount;
    private int count;
    private long startTime;
    private long nextTickTime = Long.MAX_VALUE;

    private LoggerChunkProgressListener(int n) {
        this.maxCount = n;
    }

    public static LoggerChunkProgressListener createFromGameruleRadius(int n) {
        return n > 0 ? LoggerChunkProgressListener.create(n + 1) : LoggerChunkProgressListener.createCompleted();
    }

    public static LoggerChunkProgressListener create(int n) {
        int n2 = ChunkProgressListener.calculateDiameter(n);
        return new LoggerChunkProgressListener(n2 * n2);
    }

    public static LoggerChunkProgressListener createCompleted() {
        return new LoggerChunkProgressListener(0);
    }

    @Override
    public void updateSpawnPos(ChunkPos chunkPos) {
        this.startTime = this.nextTickTime = Util.getMillis();
    }

    @Override
    public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
        if (chunkStatus == ChunkStatus.FULL) {
            ++this.count;
        }
        int n = this.getProgress();
        if (Util.getMillis() > this.nextTickTime) {
            this.nextTickTime += 500L;
            LOGGER.info(Component.translatable("menu.preparingSpawn", Mth.clamp(n, 0, 100)).getString());
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        LOGGER.info("Time elapsed: {} ms", (Object)(Util.getMillis() - this.startTime));
        this.nextTickTime = Long.MAX_VALUE;
    }

    public int getProgress() {
        if (this.maxCount == 0) {
            return 100;
        }
        return Mth.floor((float)this.count * 100.0f / (float)this.maxCount);
    }
}

