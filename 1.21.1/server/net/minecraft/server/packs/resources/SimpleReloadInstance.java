/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ProfiledReloadInstance;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.InactiveProfiler;

public class SimpleReloadInstance<S>
implements ReloadInstance {
    private static final int PREPARATION_PROGRESS_WEIGHT = 2;
    private static final int EXTRA_RELOAD_PROGRESS_WEIGHT = 2;
    private static final int LISTENER_PROGRESS_WEIGHT = 1;
    protected final CompletableFuture<Unit> allPreparations = new CompletableFuture();
    protected CompletableFuture<List<S>> allDone;
    final Set<PreparableReloadListener> preparingListeners;
    private final int listenerCount;
    private int startedReloads;
    private int finishedReloads;
    private final AtomicInteger startedTaskCounter = new AtomicInteger();
    private final AtomicInteger doneTaskCounter = new AtomicInteger();

    public static SimpleReloadInstance<Void> of(ResourceManager resourceManager2, List<PreparableReloadListener> list, Executor executor, Executor executor4, CompletableFuture<Unit> completableFuture) {
        return new SimpleReloadInstance<Void>(executor, executor4, resourceManager2, list, (preparationBarrier, resourceManager, preparableReloadListener, executor2, executor3) -> preparableReloadListener.reload(preparationBarrier, resourceManager, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, executor, executor3), completableFuture);
    }

    protected SimpleReloadInstance(Executor executor, final Executor executor2, ResourceManager resourceManager, List<PreparableReloadListener> list, StateFactory<S> stateFactory, CompletableFuture<Unit> completableFuture) {
        this.listenerCount = list.size();
        this.startedTaskCounter.incrementAndGet();
        completableFuture.thenRun(this.doneTaskCounter::incrementAndGet);
        ArrayList arrayList = Lists.newArrayList();
        CompletableFuture<Unit> completableFuture2 = completableFuture;
        this.preparingListeners = Sets.newHashSet(list);
        for (final PreparableReloadListener preparableReloadListener : list) {
            final CompletableFuture<Unit> completableFuture3 = completableFuture2;
            CompletableFuture<S> completableFuture4 = stateFactory.create(new PreparableReloadListener.PreparationBarrier(){

                @Override
                public <T> CompletableFuture<T> wait(T t) {
                    executor2.execute(() -> {
                        SimpleReloadInstance.this.preparingListeners.remove(preparableReloadListener);
                        if (SimpleReloadInstance.this.preparingListeners.isEmpty()) {
                            SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
                        }
                    });
                    return SimpleReloadInstance.this.allPreparations.thenCombine((CompletionStage)completableFuture3, (unit, object2) -> t);
                }
            }, resourceManager, preparableReloadListener, runnable -> {
                this.startedTaskCounter.incrementAndGet();
                executor.execute(() -> {
                    runnable.run();
                    this.doneTaskCounter.incrementAndGet();
                });
            }, runnable -> {
                ++this.startedReloads;
                executor2.execute(() -> {
                    runnable.run();
                    ++this.finishedReloads;
                });
            });
            arrayList.add(completableFuture4);
            completableFuture2 = completableFuture4;
        }
        this.allDone = Util.sequenceFailFast(arrayList);
    }

    @Override
    public CompletableFuture<?> done() {
        return this.allDone;
    }

    @Override
    public float getActualProgress() {
        int n = this.listenerCount - this.preparingListeners.size();
        float f = this.doneTaskCounter.get() * 2 + this.finishedReloads * 2 + n * 1;
        float f2 = this.startedTaskCounter.get() * 2 + this.startedReloads * 2 + this.listenerCount * 1;
        return f / f2;
    }

    public static ReloadInstance create(ResourceManager resourceManager, List<PreparableReloadListener> list, Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, boolean bl) {
        if (bl) {
            return new ProfiledReloadInstance(resourceManager, list, executor, executor2, completableFuture);
        }
        return SimpleReloadInstance.of(resourceManager, list, executor, executor2, completableFuture);
    }

    protected static interface StateFactory<S> {
        public CompletableFuture<S> create(PreparableReloadListener.PreparationBarrier var1, ResourceManager var2, PreparableReloadListener var3, Executor var4, Executor var5);
    }
}

