/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkGenerationTask {
    private final GeneratingChunkMap chunkMap;
    private final ChunkPos pos;
    @Nullable
    private ChunkStatus scheduledStatus = null;
    public final ChunkStatus targetStatus;
    private volatile boolean markedForCancellation;
    private final List<CompletableFuture<ChunkResult<ChunkAccess>>> scheduledLayer = new ArrayList<CompletableFuture<ChunkResult<ChunkAccess>>>();
    private final StaticCache2D<GenerationChunkHolder> cache;
    private boolean needsGeneration;

    private ChunkGenerationTask(GeneratingChunkMap generatingChunkMap, ChunkStatus chunkStatus, ChunkPos chunkPos, StaticCache2D<GenerationChunkHolder> staticCache2D) {
        this.chunkMap = generatingChunkMap;
        this.targetStatus = chunkStatus;
        this.pos = chunkPos;
        this.cache = staticCache2D;
    }

    public static ChunkGenerationTask create(GeneratingChunkMap generatingChunkMap, ChunkStatus chunkStatus, ChunkPos chunkPos) {
        int n3 = ChunkPyramid.GENERATION_PYRAMID.getStepTo(chunkStatus).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
        StaticCache2D<GenerationChunkHolder> staticCache2D = StaticCache2D.create(chunkPos.x, chunkPos.z, n3, (n, n2) -> generatingChunkMap.acquireGeneration(ChunkPos.asLong(n, n2)));
        return new ChunkGenerationTask(generatingChunkMap, chunkStatus, chunkPos, staticCache2D);
    }

    @Nullable
    public CompletableFuture<?> runUntilWait() {
        CompletableFuture<?> completableFuture;
        while ((completableFuture = this.waitForScheduledLayer()) == null) {
            if (this.markedForCancellation || this.scheduledStatus == this.targetStatus) {
                this.releaseClaim();
                return null;
            }
            this.scheduleNextLayer();
        }
        return completableFuture;
    }

    private void scheduleNextLayer() {
        ChunkStatus chunkStatus;
        if (this.scheduledStatus == null) {
            chunkStatus = ChunkStatus.EMPTY;
        } else if (!this.needsGeneration && this.scheduledStatus == ChunkStatus.EMPTY && !this.canLoadWithoutGeneration()) {
            this.needsGeneration = true;
            chunkStatus = ChunkStatus.EMPTY;
        } else {
            chunkStatus = ChunkStatus.getStatusList().get(this.scheduledStatus.getIndex() + 1);
        }
        this.scheduleLayer(chunkStatus, this.needsGeneration);
        this.scheduledStatus = chunkStatus;
    }

    public void markForCancellation() {
        this.markedForCancellation = true;
    }

    private void releaseClaim() {
        GenerationChunkHolder generationChunkHolder = this.cache.get(this.pos.x, this.pos.z);
        generationChunkHolder.removeTask(this);
        this.cache.forEach(this.chunkMap::releaseGeneration);
    }

    private boolean canLoadWithoutGeneration() {
        if (this.targetStatus == ChunkStatus.EMPTY) {
            return true;
        }
        ChunkStatus chunkStatus = this.cache.get(this.pos.x, this.pos.z).getPersistedStatus();
        if (chunkStatus == null || chunkStatus.isBefore(this.targetStatus)) {
            return false;
        }
        ChunkDependencies chunkDependencies = ChunkPyramid.LOADING_PYRAMID.getStepTo(this.targetStatus).accumulatedDependencies();
        int n = chunkDependencies.getRadius();
        for (int i = this.pos.x - n; i <= this.pos.x + n; ++i) {
            for (int j = this.pos.z - n; j <= this.pos.z + n; ++j) {
                int n2 = this.pos.getChessboardDistance(i, j);
                ChunkStatus chunkStatus2 = chunkDependencies.get(n2);
                ChunkStatus chunkStatus3 = this.cache.get(i, j).getPersistedStatus();
                if (chunkStatus3 != null && !chunkStatus3.isBefore(chunkStatus2)) continue;
                return false;
            }
        }
        return true;
    }

    public GenerationChunkHolder getCenter() {
        return this.cache.get(this.pos.x, this.pos.z);
    }

    private void scheduleLayer(ChunkStatus chunkStatus, boolean bl) {
        int n = this.getRadiusForLayer(chunkStatus, bl);
        for (int i = this.pos.x - n; i <= this.pos.x + n; ++i) {
            for (int j = this.pos.z - n; j <= this.pos.z + n; ++j) {
                GenerationChunkHolder generationChunkHolder = this.cache.get(i, j);
                if (!this.markedForCancellation && this.scheduleChunkInLayer(chunkStatus, bl, generationChunkHolder)) continue;
                return;
            }
        }
    }

    private int getRadiusForLayer(ChunkStatus chunkStatus, boolean bl) {
        ChunkPyramid chunkPyramid = bl ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
        return chunkPyramid.getStepTo(this.targetStatus).getAccumulatedRadiusOf(chunkStatus);
    }

    private boolean scheduleChunkInLayer(ChunkStatus chunkStatus, boolean bl, GenerationChunkHolder generationChunkHolder) {
        ChunkPyramid chunkPyramid;
        ChunkStatus chunkStatus2 = generationChunkHolder.getPersistedStatus();
        boolean bl2 = chunkStatus2 != null && chunkStatus.isAfter(chunkStatus2);
        ChunkPyramid chunkPyramid2 = chunkPyramid = bl2 ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
        if (bl2 && !bl) {
            throw new IllegalStateException("Can't load chunk, but didn't expect to need to generate");
        }
        CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = generationChunkHolder.applyStep(chunkPyramid.getStepTo(chunkStatus), this.chunkMap, this.cache);
        ChunkResult chunkResult = completableFuture.getNow(null);
        if (chunkResult == null) {
            this.scheduledLayer.add(completableFuture);
            return true;
        }
        if (chunkResult.isSuccess()) {
            return true;
        }
        this.markForCancellation();
        return false;
    }

    @Nullable
    private CompletableFuture<?> waitForScheduledLayer() {
        while (!this.scheduledLayer.isEmpty()) {
            CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.scheduledLayer.getLast();
            ChunkResult chunkResult = completableFuture.getNow(null);
            if (chunkResult == null) {
                return completableFuture;
            }
            this.scheduledLayer.removeLast();
            if (chunkResult.isSuccess()) continue;
            this.markForCancellation();
        }
        return null;
    }
}

