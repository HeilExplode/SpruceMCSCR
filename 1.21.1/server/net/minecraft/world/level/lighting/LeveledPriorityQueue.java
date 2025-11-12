/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public class LeveledPriorityQueue {
    private final int levelCount;
    private final LongLinkedOpenHashSet[] queues;
    private int firstQueuedLevel;

    public LeveledPriorityQueue(int n, final int n2) {
        this.levelCount = n;
        this.queues = new LongLinkedOpenHashSet[n];
        for (int i = 0; i < n; ++i) {
            this.queues[i] = new LongLinkedOpenHashSet(n2, 0.5f){

                protected void rehash(int n) {
                    if (n > n2) {
                        super.rehash(n);
                    }
                }
            };
        }
        this.firstQueuedLevel = n;
    }

    public long removeFirstLong() {
        LongLinkedOpenHashSet longLinkedOpenHashSet = this.queues[this.firstQueuedLevel];
        long l = longLinkedOpenHashSet.removeFirstLong();
        if (longLinkedOpenHashSet.isEmpty()) {
            this.checkFirstQueuedLevel(this.levelCount);
        }
        return l;
    }

    public boolean isEmpty() {
        return this.firstQueuedLevel >= this.levelCount;
    }

    public void dequeue(long l, int n, int n2) {
        LongLinkedOpenHashSet longLinkedOpenHashSet = this.queues[n];
        longLinkedOpenHashSet.remove(l);
        if (longLinkedOpenHashSet.isEmpty() && this.firstQueuedLevel == n) {
            this.checkFirstQueuedLevel(n2);
        }
    }

    public void enqueue(long l, int n) {
        this.queues[n].add(l);
        if (this.firstQueuedLevel > n) {
            this.firstQueuedLevel = n;
        }
    }

    private void checkFirstQueuedLevel(int n) {
        int n2 = this.firstQueuedLevel;
        this.firstQueuedLevel = n;
        for (int i = n2 + 1; i < n; ++i) {
            if (this.queues[i].isEmpty()) continue;
            this.firstQueuedLevel = i;
            break;
        }
    }
}

