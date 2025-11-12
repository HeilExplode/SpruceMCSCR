/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level.progress;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class StoringChunkProgressListener
implements ChunkProgressListener {
    private final LoggerChunkProgressListener delegate;
    private final Long2ObjectOpenHashMap<ChunkStatus> statuses = new Long2ObjectOpenHashMap();
    private ChunkPos spawnPos = new ChunkPos(0, 0);
    private final int fullDiameter;
    private final int radius;
    private final int diameter;
    private boolean started;

    private StoringChunkProgressListener(LoggerChunkProgressListener loggerChunkProgressListener, int n, int n2, int n3) {
        this.delegate = loggerChunkProgressListener;
        this.fullDiameter = n;
        this.radius = n2;
        this.diameter = n3;
    }

    public static StoringChunkProgressListener createFromGameruleRadius(int n) {
        return n > 0 ? StoringChunkProgressListener.create(n + 1) : StoringChunkProgressListener.createCompleted();
    }

    public static StoringChunkProgressListener create(int n) {
        LoggerChunkProgressListener loggerChunkProgressListener = LoggerChunkProgressListener.create(n);
        int n2 = ChunkProgressListener.calculateDiameter(n);
        int n3 = n + ChunkLevel.RADIUS_AROUND_FULL_CHUNK;
        int n4 = ChunkProgressListener.calculateDiameter(n3);
        return new StoringChunkProgressListener(loggerChunkProgressListener, n2, n3, n4);
    }

    public static StoringChunkProgressListener createCompleted() {
        return new StoringChunkProgressListener(LoggerChunkProgressListener.createCompleted(), 0, 0, 0);
    }

    @Override
    public void updateSpawnPos(ChunkPos chunkPos) {
        if (!this.started) {
            return;
        }
        this.delegate.updateSpawnPos(chunkPos);
        this.spawnPos = chunkPos;
    }

    @Override
    public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
        if (!this.started) {
            return;
        }
        this.delegate.onStatusChange(chunkPos, chunkStatus);
        if (chunkStatus == null) {
            this.statuses.remove(chunkPos.toLong());
        } else {
            this.statuses.put(chunkPos.toLong(), (Object)chunkStatus);
        }
    }

    @Override
    public void start() {
        this.started = true;
        this.statuses.clear();
        this.delegate.start();
    }

    @Override
    public void stop() {
        this.started = false;
        this.delegate.stop();
    }

    public int getFullDiameter() {
        return this.fullDiameter;
    }

    public int getDiameter() {
        return this.diameter;
    }

    public int getProgress() {
        return this.delegate.getProgress();
    }

    @Nullable
    public ChunkStatus getStatus(int n, int n2) {
        return (ChunkStatus)this.statuses.get(ChunkPos.asLong(n + this.spawnPos.x - this.radius, n2 + this.spawnPos.z - this.radius));
    }
}

