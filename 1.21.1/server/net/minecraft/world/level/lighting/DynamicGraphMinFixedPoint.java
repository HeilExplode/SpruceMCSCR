/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.longs.LongList
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.function.LongPredicate;
import net.minecraft.util.Mth;
import net.minecraft.world.level.lighting.LeveledPriorityQueue;

public abstract class DynamicGraphMinFixedPoint {
    public static final long SOURCE = Long.MAX_VALUE;
    private static final int NO_COMPUTED_LEVEL = 255;
    protected final int levelCount;
    private final LeveledPriorityQueue priorityQueue;
    private final Long2ByteMap computedLevels;
    private volatile boolean hasWork;

    protected DynamicGraphMinFixedPoint(int n, int n2, final int n3) {
        if (n >= 254) {
            throw new IllegalArgumentException("Level count must be < 254.");
        }
        this.levelCount = n;
        this.priorityQueue = new LeveledPriorityQueue(n, n2);
        this.computedLevels = new Long2ByteOpenHashMap(n3, 0.5f){

            protected void rehash(int n) {
                if (n > n3) {
                    super.rehash(n);
                }
            }
        };
        this.computedLevels.defaultReturnValue((byte)-1);
    }

    protected void removeFromQueue(long l) {
        int n = this.computedLevels.remove(l) & 0xFF;
        if (n == 255) {
            return;
        }
        int n2 = this.getLevel(l);
        int n3 = this.calculatePriority(n2, n);
        this.priorityQueue.dequeue(l, n3, this.levelCount);
        this.hasWork = !this.priorityQueue.isEmpty();
    }

    public void removeIf(LongPredicate longPredicate) {
        LongArrayList longArrayList = new LongArrayList();
        this.computedLevels.keySet().forEach(arg_0 -> DynamicGraphMinFixedPoint.lambda$removeIf$0(longPredicate, (LongList)longArrayList, arg_0));
        longArrayList.forEach(this::removeFromQueue);
    }

    private int calculatePriority(int n, int n2) {
        return Math.min(Math.min(n, n2), this.levelCount - 1);
    }

    protected void checkNode(long l) {
        this.checkEdge(l, l, this.levelCount - 1, false);
    }

    protected void checkEdge(long l, long l2, int n, boolean bl) {
        this.checkEdge(l, l2, n, this.getLevel(l2), this.computedLevels.get(l2) & 0xFF, bl);
        this.hasWork = !this.priorityQueue.isEmpty();
    }

    private void checkEdge(long l, long l2, int n, int n2, int n3, boolean bl) {
        boolean bl2;
        if (this.isSource(l2)) {
            return;
        }
        n = Mth.clamp(n, 0, this.levelCount - 1);
        n2 = Mth.clamp(n2, 0, this.levelCount - 1);
        boolean bl3 = bl2 = n3 == 255;
        if (bl2) {
            n3 = n2;
        }
        int n4 = bl ? Math.min(n3, n) : Mth.clamp(this.getComputedLevel(l2, l, n), 0, this.levelCount - 1);
        int n5 = this.calculatePriority(n2, n3);
        if (n2 != n4) {
            int n6 = this.calculatePriority(n2, n4);
            if (n5 != n6 && !bl2) {
                this.priorityQueue.dequeue(l2, n5, n6);
            }
            this.priorityQueue.enqueue(l2, n6);
            this.computedLevels.put(l2, (byte)n4);
        } else if (!bl2) {
            this.priorityQueue.dequeue(l2, n5, this.levelCount);
            this.computedLevels.remove(l2);
        }
    }

    protected final void checkNeighbor(long l, long l2, int n, boolean bl) {
        int n2 = this.computedLevels.get(l2) & 0xFF;
        int n3 = Mth.clamp(this.computeLevelFromNeighbor(l, l2, n), 0, this.levelCount - 1);
        if (bl) {
            this.checkEdge(l, l2, n3, this.getLevel(l2), n2, bl);
        } else {
            boolean bl2 = n2 == 255;
            int n4 = bl2 ? Mth.clamp(this.getLevel(l2), 0, this.levelCount - 1) : n2;
            if (n3 == n4) {
                this.checkEdge(l, l2, this.levelCount - 1, bl2 ? n4 : this.getLevel(l2), n2, bl);
            }
        }
    }

    protected final boolean hasWork() {
        return this.hasWork;
    }

    protected final int runUpdates(int n) {
        if (this.priorityQueue.isEmpty()) {
            return n;
        }
        while (!this.priorityQueue.isEmpty() && n > 0) {
            --n;
            long l = this.priorityQueue.removeFirstLong();
            int n2 = Mth.clamp(this.getLevel(l), 0, this.levelCount - 1);
            int n3 = this.computedLevels.remove(l) & 0xFF;
            if (n3 < n2) {
                this.setLevel(l, n3);
                this.checkNeighborsAfterUpdate(l, n3, true);
                continue;
            }
            if (n3 <= n2) continue;
            this.setLevel(l, this.levelCount - 1);
            if (n3 != this.levelCount - 1) {
                this.priorityQueue.enqueue(l, this.calculatePriority(this.levelCount - 1, n3));
                this.computedLevels.put(l, (byte)n3);
            }
            this.checkNeighborsAfterUpdate(l, n2, false);
        }
        this.hasWork = !this.priorityQueue.isEmpty();
        return n;
    }

    public int getQueueSize() {
        return this.computedLevels.size();
    }

    protected boolean isSource(long l) {
        return l == Long.MAX_VALUE;
    }

    protected abstract int getComputedLevel(long var1, long var3, int var5);

    protected abstract void checkNeighborsAfterUpdate(long var1, int var3, boolean var4);

    protected abstract int getLevel(long var1);

    protected abstract void setLevel(long var1, int var3);

    protected abstract int computeLevelFromNeighbor(long var1, long var3, int var5);

    private static /* synthetic */ void lambda$removeIf$0(LongPredicate longPredicate, LongList longList, long l) {
        if (longPredicate.test(l)) {
            longList.add(l);
        }
    }
}

