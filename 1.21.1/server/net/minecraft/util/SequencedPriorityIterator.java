/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.Queues
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Deque;
import javax.annotation.Nullable;

public final class SequencedPriorityIterator<T>
extends AbstractIterator<T> {
    private static final int MIN_PRIO = Integer.MIN_VALUE;
    @Nullable
    private Deque<T> highestPrioQueue = null;
    private int highestPrio = Integer.MIN_VALUE;
    private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap();

    public void add(T t, int n2) {
        if (n2 == this.highestPrio && this.highestPrioQueue != null) {
            this.highestPrioQueue.addLast(t);
            return;
        }
        Deque deque = (Deque)this.queuesByPriority.computeIfAbsent(n2, n -> Queues.newArrayDeque());
        deque.addLast(t);
        if (n2 >= this.highestPrio) {
            this.highestPrioQueue = deque;
            this.highestPrio = n2;
        }
    }

    @Nullable
    protected T computeNext() {
        if (this.highestPrioQueue == null) {
            return (T)this.endOfData();
        }
        T t = this.highestPrioQueue.removeFirst();
        if (t == null) {
            return (T)this.endOfData();
        }
        if (this.highestPrioQueue.isEmpty()) {
            this.switchCacheToNextHighestPrioQueue();
        }
        return t;
    }

    private void switchCacheToNextHighestPrioQueue() {
        int n = Integer.MIN_VALUE;
        Deque deque = null;
        for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(this.queuesByPriority)) {
            Deque deque2 = (Deque)entry.getValue();
            int n2 = entry.getIntKey();
            if (n2 <= n || deque2.isEmpty()) continue;
            n = n2;
            deque = deque2;
            if (n2 != this.highestPrio - 1) continue;
            break;
        }
        this.highestPrio = n;
        this.highestPrioQueue = deque;
    }
}

