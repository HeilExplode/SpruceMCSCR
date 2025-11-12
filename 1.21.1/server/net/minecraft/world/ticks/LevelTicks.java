/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2LongMap
 *  it.unimi.dsi.fastutil.longs.Long2LongMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2LongMaps
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet
 */
package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickAccess;

public class LevelTicks<T>
implements LevelTickAccess<T> {
    private static final Comparator<LevelChunkTicks<?>> CONTAINER_DRAIN_ORDER = (levelChunkTicks, levelChunkTicks2) -> ScheduledTick.INTRA_TICK_DRAIN_ORDER.compare(levelChunkTicks.peek(), levelChunkTicks2.peek());
    private final LongPredicate tickCheck;
    private final Supplier<ProfilerFiller> profiler;
    private final Long2ObjectMap<LevelChunkTicks<T>> allContainers = new Long2ObjectOpenHashMap();
    private final Long2LongMap nextTickForContainer = (Long2LongMap)Util.make(new Long2LongOpenHashMap(), long2LongOpenHashMap -> long2LongOpenHashMap.defaultReturnValue(Long.MAX_VALUE));
    private final Queue<LevelChunkTicks<T>> containersToTick = new PriorityQueue(CONTAINER_DRAIN_ORDER);
    private final Queue<ScheduledTick<T>> toRunThisTick = new ArrayDeque<ScheduledTick<T>>();
    private final List<ScheduledTick<T>> alreadyRunThisTick = new ArrayList<ScheduledTick<T>>();
    private final Set<ScheduledTick<?>> toRunThisTickSet = new ObjectOpenCustomHashSet(ScheduledTick.UNIQUE_TICK_HASH);
    private final BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> chunkScheduleUpdater = (levelChunkTicks, scheduledTick) -> {
        if (scheduledTick.equals(levelChunkTicks.peek())) {
            this.updateContainerScheduling((ScheduledTick<T>)scheduledTick);
        }
    };

    public LevelTicks(LongPredicate longPredicate, Supplier<ProfilerFiller> supplier) {
        this.tickCheck = longPredicate;
        this.profiler = supplier;
    }

    public void addContainer(ChunkPos chunkPos, LevelChunkTicks<T> levelChunkTicks) {
        long l = chunkPos.toLong();
        this.allContainers.put(l, levelChunkTicks);
        ScheduledTick<T> scheduledTick = levelChunkTicks.peek();
        if (scheduledTick != null) {
            this.nextTickForContainer.put(l, scheduledTick.triggerTick());
        }
        levelChunkTicks.setOnTickAdded(this.chunkScheduleUpdater);
    }

    public void removeContainer(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        LevelChunkTicks levelChunkTicks = (LevelChunkTicks)this.allContainers.remove(l);
        this.nextTickForContainer.remove(l);
        if (levelChunkTicks != null) {
            levelChunkTicks.setOnTickAdded(null);
        }
    }

    @Override
    public void schedule(ScheduledTick<T> scheduledTick) {
        long l = ChunkPos.asLong(scheduledTick.pos());
        LevelChunkTicks levelChunkTicks = (LevelChunkTicks)this.allContainers.get(l);
        if (levelChunkTicks == null) {
            Util.pauseInIde(new IllegalStateException("Trying to schedule tick in not loaded position " + String.valueOf(scheduledTick.pos())));
            return;
        }
        levelChunkTicks.schedule(scheduledTick);
    }

    public void tick(long l, int n, BiConsumer<BlockPos, T> biConsumer) {
        ProfilerFiller profilerFiller = this.profiler.get();
        profilerFiller.push("collect");
        this.collectTicks(l, n, profilerFiller);
        profilerFiller.popPush("run");
        profilerFiller.incrementCounter("ticksToRun", this.toRunThisTick.size());
        this.runCollectedTicks(biConsumer);
        profilerFiller.popPush("cleanup");
        this.cleanupAfterTick();
        profilerFiller.pop();
    }

    private void collectTicks(long l, int n, ProfilerFiller profilerFiller) {
        this.sortContainersToTick(l);
        profilerFiller.incrementCounter("containersToTick", this.containersToTick.size());
        this.drainContainers(l, n);
        this.rescheduleLeftoverContainers();
    }

    private void sortContainersToTick(long l) {
        ObjectIterator objectIterator = Long2LongMaps.fastIterator((Long2LongMap)this.nextTickForContainer);
        while (objectIterator.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry)objectIterator.next();
            long l2 = entry.getLongKey();
            long l3 = entry.getLongValue();
            if (l3 > l) continue;
            LevelChunkTicks levelChunkTicks = (LevelChunkTicks)this.allContainers.get(l2);
            if (levelChunkTicks == null) {
                objectIterator.remove();
                continue;
            }
            ScheduledTick scheduledTick = levelChunkTicks.peek();
            if (scheduledTick == null) {
                objectIterator.remove();
                continue;
            }
            if (scheduledTick.triggerTick() > l) {
                entry.setValue(scheduledTick.triggerTick());
                continue;
            }
            if (!this.tickCheck.test(l2)) continue;
            objectIterator.remove();
            this.containersToTick.add(levelChunkTicks);
        }
    }

    private void drainContainers(long l, int n) {
        LevelChunkTicks<T> levelChunkTicks;
        while (this.canScheduleMoreTicks(n) && (levelChunkTicks = this.containersToTick.poll()) != null) {
            ScheduledTick<T> scheduledTick = levelChunkTicks.poll();
            this.scheduleForThisTick(scheduledTick);
            this.drainFromCurrentContainer(this.containersToTick, levelChunkTicks, l, n);
            ScheduledTick<T> scheduledTick2 = levelChunkTicks.peek();
            if (scheduledTick2 == null) continue;
            if (scheduledTick2.triggerTick() <= l && this.canScheduleMoreTicks(n)) {
                this.containersToTick.add(levelChunkTicks);
                continue;
            }
            this.updateContainerScheduling(scheduledTick2);
        }
    }

    private void rescheduleLeftoverContainers() {
        for (LevelChunkTicks levelChunkTicks : this.containersToTick) {
            this.updateContainerScheduling(levelChunkTicks.peek());
        }
    }

    private void updateContainerScheduling(ScheduledTick<T> scheduledTick) {
        this.nextTickForContainer.put(ChunkPos.asLong(scheduledTick.pos()), scheduledTick.triggerTick());
    }

    private void drainFromCurrentContainer(Queue<LevelChunkTicks<T>> queue, LevelChunkTicks<T> levelChunkTicks, long l, int n) {
        ScheduledTick<T> scheduledTick;
        ScheduledTick<T> scheduledTick2;
        if (!this.canScheduleMoreTicks(n)) {
            return;
        }
        LevelChunkTicks<T> levelChunkTicks2 = queue.peek();
        ScheduledTick<T> scheduledTick3 = scheduledTick2 = levelChunkTicks2 != null ? levelChunkTicks2.peek() : null;
        while (this.canScheduleMoreTicks(n) && (scheduledTick = levelChunkTicks.peek()) != null && scheduledTick.triggerTick() <= l && (scheduledTick2 == null || ScheduledTick.INTRA_TICK_DRAIN_ORDER.compare(scheduledTick, scheduledTick2) <= 0)) {
            levelChunkTicks.poll();
            this.scheduleForThisTick(scheduledTick);
        }
    }

    private void scheduleForThisTick(ScheduledTick<T> scheduledTick) {
        this.toRunThisTick.add(scheduledTick);
    }

    private boolean canScheduleMoreTicks(int n) {
        return this.toRunThisTick.size() < n;
    }

    private void runCollectedTicks(BiConsumer<BlockPos, T> biConsumer) {
        while (!this.toRunThisTick.isEmpty()) {
            ScheduledTick<T> scheduledTick = this.toRunThisTick.poll();
            if (!this.toRunThisTickSet.isEmpty()) {
                this.toRunThisTickSet.remove(scheduledTick);
            }
            this.alreadyRunThisTick.add(scheduledTick);
            biConsumer.accept(scheduledTick.pos(), (BlockPos)scheduledTick.type());
        }
    }

    private void cleanupAfterTick() {
        this.toRunThisTick.clear();
        this.containersToTick.clear();
        this.alreadyRunThisTick.clear();
        this.toRunThisTickSet.clear();
    }

    @Override
    public boolean hasScheduledTick(BlockPos blockPos, T t) {
        LevelChunkTicks levelChunkTicks = (LevelChunkTicks)this.allContainers.get(ChunkPos.asLong(blockPos));
        return levelChunkTicks != null && levelChunkTicks.hasScheduledTick(blockPos, t);
    }

    @Override
    public boolean willTickThisTick(BlockPos blockPos, T t) {
        this.calculateTickSetIfNeeded();
        return this.toRunThisTickSet.contains(ScheduledTick.probe(t, blockPos));
    }

    private void calculateTickSetIfNeeded() {
        if (this.toRunThisTickSet.isEmpty() && !this.toRunThisTick.isEmpty()) {
            this.toRunThisTickSet.addAll(this.toRunThisTick);
        }
    }

    private void forContainersInArea(BoundingBox boundingBox, PosAndContainerConsumer<T> posAndContainerConsumer) {
        int n = SectionPos.posToSectionCoord(boundingBox.minX());
        int n2 = SectionPos.posToSectionCoord(boundingBox.minZ());
        int n3 = SectionPos.posToSectionCoord(boundingBox.maxX());
        int n4 = SectionPos.posToSectionCoord(boundingBox.maxZ());
        for (int i = n; i <= n3; ++i) {
            for (int j = n2; j <= n4; ++j) {
                long l = ChunkPos.asLong(i, j);
                LevelChunkTicks levelChunkTicks = (LevelChunkTicks)this.allContainers.get(l);
                if (levelChunkTicks == null) continue;
                posAndContainerConsumer.accept(l, levelChunkTicks);
            }
        }
    }

    public void clearArea(BoundingBox boundingBox) {
        Predicate<ScheduledTick> predicate = scheduledTick -> boundingBox.isInside(scheduledTick.pos());
        this.forContainersInArea(boundingBox, (l, levelChunkTicks) -> {
            ScheduledTick scheduledTick = levelChunkTicks.peek();
            levelChunkTicks.removeIf(predicate);
            ScheduledTick scheduledTick2 = levelChunkTicks.peek();
            if (scheduledTick2 != scheduledTick) {
                if (scheduledTick2 != null) {
                    this.updateContainerScheduling(scheduledTick2);
                } else {
                    this.nextTickForContainer.remove(l);
                }
            }
        });
        this.alreadyRunThisTick.removeIf(predicate);
        this.toRunThisTick.removeIf(predicate);
    }

    public void copyArea(BoundingBox boundingBox, Vec3i vec3i) {
        this.copyAreaFrom(this, boundingBox, vec3i);
    }

    public void copyAreaFrom(LevelTicks<T> levelTicks, BoundingBox boundingBox, Vec3i vec3i) {
        ArrayList arrayList = new ArrayList();
        Predicate<ScheduledTick> predicate = scheduledTick -> boundingBox.isInside(scheduledTick.pos());
        levelTicks.alreadyRunThisTick.stream().filter(predicate).forEach(arrayList::add);
        levelTicks.toRunThisTick.stream().filter(predicate).forEach(arrayList::add);
        levelTicks.forContainersInArea(boundingBox, (l, levelChunkTicks) -> levelChunkTicks.getAll().filter(predicate).forEach(arrayList::add));
        LongSummaryStatistics longSummaryStatistics = arrayList.stream().mapToLong(ScheduledTick::subTickOrder).summaryStatistics();
        long l2 = longSummaryStatistics.getMin();
        long l3 = longSummaryStatistics.getMax();
        arrayList.forEach(scheduledTick -> this.schedule(new ScheduledTick(scheduledTick.type(), scheduledTick.pos().offset(vec3i), scheduledTick.triggerTick(), scheduledTick.priority(), scheduledTick.subTickOrder() - l2 + l3 + 1L)));
    }

    @Override
    public int count() {
        return this.allContainers.values().stream().mapToInt(TickAccess::count).sum();
    }

    @FunctionalInterface
    static interface PosAndContainerConsumer<T> {
        public void accept(long var1, LevelChunkTicks<T> var3);
    }
}

