/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public abstract class GenerationChunkHolder {
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private static final ChunkResult<ChunkAccess> NOT_DONE_YET = ChunkResult.error("Not done yet");
    public static final ChunkResult<ChunkAccess> UNLOADED_CHUNK = ChunkResult.error("Unloaded chunk");
    public static final CompletableFuture<ChunkResult<ChunkAccess>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
    protected final ChunkPos pos;
    @Nullable
    private volatile ChunkStatus highestAllowedStatus;
    private final AtomicReference<ChunkStatus> startedWork = new AtomicReference();
    private final AtomicReferenceArray<CompletableFuture<ChunkResult<ChunkAccess>>> futures = new AtomicReferenceArray(CHUNK_STATUSES.size());
    private final AtomicReference<ChunkGenerationTask> task = new AtomicReference();
    private final AtomicInteger generationRefCount = new AtomicInteger();

    public GenerationChunkHolder(ChunkPos chunkPos) {
        this.pos = chunkPos;
    }

    public CompletableFuture<ChunkResult<ChunkAccess>> scheduleChunkGenerationTask(ChunkStatus chunkStatus, ChunkMap chunkMap) {
        if (this.isStatusDisallowed(chunkStatus)) {
            return UNLOADED_CHUNK_FUTURE;
        }
        CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.getOrCreateFuture(chunkStatus);
        if (completableFuture.isDone()) {
            return completableFuture;
        }
        ChunkGenerationTask chunkGenerationTask = this.task.get();
        if (chunkGenerationTask == null || chunkStatus.isAfter(chunkGenerationTask.targetStatus)) {
            this.rescheduleChunkTask(chunkMap, chunkStatus);
        }
        return completableFuture;
    }

    CompletableFuture<ChunkResult<ChunkAccess>> applyStep(ChunkStep chunkStep, GeneratingChunkMap generatingChunkMap, StaticCache2D<GenerationChunkHolder> staticCache2D) {
        if (this.isStatusDisallowed(chunkStep.targetStatus())) {
            return UNLOADED_CHUNK_FUTURE;
        }
        if (this.acquireStatusBump(chunkStep.targetStatus())) {
            return generatingChunkMap.applyStep(this, chunkStep, staticCache2D).handle((chunkAccess, throwable) -> {
                if (throwable != null) {
                    CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception chunk generation/loading");
                    MinecraftServer.setFatalException(new ReportedException(crashReport));
                } else {
                    this.completeFuture(chunkStep.targetStatus(), (ChunkAccess)chunkAccess);
                }
                return ChunkResult.of(chunkAccess);
            });
        }
        return this.getOrCreateFuture(chunkStep.targetStatus());
    }

    protected void updateHighestAllowedStatus(ChunkMap chunkMap) {
        boolean bl;
        ChunkStatus chunkStatus;
        ChunkStatus chunkStatus2 = this.highestAllowedStatus;
        this.highestAllowedStatus = chunkStatus = ChunkLevel.generationStatus(this.getTicketLevel());
        boolean bl2 = bl = chunkStatus2 != null && (chunkStatus == null || chunkStatus.isBefore(chunkStatus2));
        if (bl) {
            this.failAndClearPendingFuturesBetween(chunkStatus, chunkStatus2);
            if (this.task.get() != null) {
                this.rescheduleChunkTask(chunkMap, this.findHighestStatusWithPendingFuture(chunkStatus));
            }
        }
    }

    public void replaceProtoChunk(ImposterProtoChunk imposterProtoChunk) {
        CompletableFuture<ChunkResult<ImposterProtoChunk>> completableFuture = CompletableFuture.completedFuture(ChunkResult.of(imposterProtoChunk));
        for (int i = 0; i < this.futures.length() - 1; ++i) {
            CompletableFuture<ChunkResult<ChunkAccess>> completableFuture2 = this.futures.get(i);
            Objects.requireNonNull(completableFuture2);
            ChunkAccess chunkAccess = completableFuture2.getNow(NOT_DONE_YET).orElse(null);
            if (chunkAccess instanceof ProtoChunk) {
                if (this.futures.compareAndSet(i, completableFuture2, completableFuture)) continue;
                throw new IllegalStateException("Future changed by other thread while trying to replace it");
            }
            throw new IllegalStateException("Trying to replace a ProtoChunk, but found " + String.valueOf(chunkAccess));
        }
    }

    void removeTask(ChunkGenerationTask chunkGenerationTask) {
        this.task.compareAndSet(chunkGenerationTask, null);
    }

    private void rescheduleChunkTask(ChunkMap chunkMap, @Nullable ChunkStatus chunkStatus) {
        ChunkGenerationTask chunkGenerationTask = chunkStatus != null ? chunkMap.scheduleGenerationTask(chunkStatus, this.getPos()) : null;
        ChunkGenerationTask chunkGenerationTask2 = this.task.getAndSet(chunkGenerationTask);
        if (chunkGenerationTask2 != null) {
            chunkGenerationTask2.markForCancellation();
        }
    }

    private CompletableFuture<ChunkResult<ChunkAccess>> getOrCreateFuture(ChunkStatus chunkStatus) {
        if (this.isStatusDisallowed(chunkStatus)) {
            return UNLOADED_CHUNK_FUTURE;
        }
        int n = chunkStatus.getIndex();
        CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.futures.get(n);
        while (completableFuture == null) {
            CompletableFuture<ChunkResult<ChunkAccess>> completableFuture2 = new CompletableFuture<ChunkResult<ChunkAccess>>();
            completableFuture = this.futures.compareAndExchange(n, null, completableFuture2);
            if (completableFuture != null) continue;
            if (this.isStatusDisallowed(chunkStatus)) {
                this.failAndClearPendingFuture(n, completableFuture2);
                return UNLOADED_CHUNK_FUTURE;
            }
            return completableFuture2;
        }
        return completableFuture;
    }

    private void failAndClearPendingFuturesBetween(@Nullable ChunkStatus chunkStatus, ChunkStatus chunkStatus2) {
        int n = chunkStatus == null ? 0 : chunkStatus.getIndex() + 1;
        int n2 = chunkStatus2.getIndex();
        for (int i = n; i <= n2; ++i) {
            CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.futures.get(i);
            if (completableFuture == null) continue;
            this.failAndClearPendingFuture(i, completableFuture);
        }
    }

    private void failAndClearPendingFuture(int n, CompletableFuture<ChunkResult<ChunkAccess>> completableFuture) {
        if (completableFuture.complete(UNLOADED_CHUNK) && !this.futures.compareAndSet(n, completableFuture, null)) {
            throw new IllegalStateException("Nothing else should replace the future here");
        }
    }

    private void completeFuture(ChunkStatus chunkStatus, ChunkAccess chunkAccess) {
        ChunkResult<ChunkAccess> chunkResult = ChunkResult.of(chunkAccess);
        int n = chunkStatus.getIndex();
        while (true) {
            CompletableFuture<ChunkResult<ChunkAccess>> completableFuture;
            if ((completableFuture = this.futures.get(n)) == null) {
                if (!this.futures.compareAndSet(n, null, CompletableFuture.completedFuture(chunkResult))) continue;
                return;
            }
            if (completableFuture.complete(chunkResult)) {
                return;
            }
            if (completableFuture.getNow(NOT_DONE_YET).isSuccess()) {
                throw new IllegalStateException("Trying to complete a future but found it to be completed successfully already");
            }
            Thread.yield();
        }
    }

    @Nullable
    private ChunkStatus findHighestStatusWithPendingFuture(@Nullable ChunkStatus chunkStatus) {
        if (chunkStatus == null) {
            return null;
        }
        ChunkStatus chunkStatus2 = chunkStatus;
        ChunkStatus chunkStatus3 = this.startedWork.get();
        while (chunkStatus3 == null || chunkStatus2.isAfter(chunkStatus3)) {
            if (this.futures.get(chunkStatus2.getIndex()) != null) {
                return chunkStatus2;
            }
            if (chunkStatus2 == ChunkStatus.EMPTY) break;
            chunkStatus2 = chunkStatus2.getParent();
        }
        return null;
    }

    private boolean acquireStatusBump(ChunkStatus chunkStatus) {
        ChunkStatus chunkStatus2 = chunkStatus == ChunkStatus.EMPTY ? null : chunkStatus.getParent();
        ChunkStatus chunkStatus3 = this.startedWork.compareAndExchange(chunkStatus2, chunkStatus);
        if (chunkStatus3 == chunkStatus2) {
            return true;
        }
        if (chunkStatus3 == null || chunkStatus.isAfter(chunkStatus3)) {
            throw new IllegalStateException("Unexpected last startedWork status: " + String.valueOf(chunkStatus3) + " while trying to start: " + String.valueOf(chunkStatus));
        }
        return false;
    }

    private boolean isStatusDisallowed(ChunkStatus chunkStatus) {
        ChunkStatus chunkStatus2 = this.highestAllowedStatus;
        return chunkStatus2 == null || chunkStatus.isAfter(chunkStatus2);
    }

    public void increaseGenerationRefCount() {
        this.generationRefCount.incrementAndGet();
    }

    public void decreaseGenerationRefCount() {
        int n = this.generationRefCount.decrementAndGet();
        if (n < 0) {
            throw new IllegalStateException("More releases than claims. Count: " + n);
        }
    }

    public int getGenerationRefCount() {
        return this.generationRefCount.get();
    }

    @Nullable
    public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus chunkStatus) {
        CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.futures.get(chunkStatus.getIndex());
        return completableFuture == null ? null : (ChunkAccess)completableFuture.getNow(NOT_DONE_YET).orElse(null);
    }

    @Nullable
    public ChunkAccess getChunkIfPresent(ChunkStatus chunkStatus) {
        if (this.isStatusDisallowed(chunkStatus)) {
            return null;
        }
        return this.getChunkIfPresentUnchecked(chunkStatus);
    }

    @Nullable
    public ChunkAccess getLatestChunk() {
        ChunkStatus chunkStatus = this.startedWork.get();
        if (chunkStatus == null) {
            return null;
        }
        ChunkAccess chunkAccess = this.getChunkIfPresentUnchecked(chunkStatus);
        if (chunkAccess != null) {
            return chunkAccess;
        }
        return this.getChunkIfPresentUnchecked(chunkStatus.getParent());
    }

    @Nullable
    public ChunkStatus getPersistedStatus() {
        CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.futures.get(ChunkStatus.EMPTY.getIndex());
        ChunkAccess chunkAccess = completableFuture == null ? null : (ChunkAccess)completableFuture.getNow(NOT_DONE_YET).orElse(null);
        return chunkAccess == null ? null : chunkAccess.getPersistedStatus();
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public FullChunkStatus getFullStatus() {
        return ChunkLevel.fullStatus(this.getTicketLevel());
    }

    public abstract int getTicketLevel();

    public abstract int getQueueLevel();

    @VisibleForDebug
    public List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> getAllFutures() {
        ArrayList<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> arrayList = new ArrayList<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>>();
        for (int i = 0; i < CHUNK_STATUSES.size(); ++i) {
            arrayList.add((Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>)Pair.of((Object)CHUNK_STATUSES.get(i), this.futures.get(i)));
        }
        return arrayList;
    }

    @Nullable
    @VisibleForDebug
    public ChunkStatus getLatestStatus() {
        for (int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus chunkStatus = CHUNK_STATUSES.get(i);
            ChunkAccess chunkAccess = this.getChunkIfPresentUnchecked(chunkStatus);
            if (chunkAccess == null) continue;
            return chunkStatus;
        }
        return null;
    }
}

