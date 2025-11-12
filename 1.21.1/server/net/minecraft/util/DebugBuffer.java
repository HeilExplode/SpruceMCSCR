/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class DebugBuffer<T> {
    private final AtomicReferenceArray<T> data;
    private final AtomicInteger index;

    public DebugBuffer(int n) {
        this.data = new AtomicReferenceArray(n);
        this.index = new AtomicInteger(0);
    }

    public void push(T t) {
        int n;
        int n2;
        int n3 = this.data.length();
        while (!this.index.compareAndSet(n2 = this.index.get(), n = (n2 + 1) % n3)) {
        }
        this.data.set(n, t);
    }

    public List<T> dump() {
        int n = this.index.get();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (int i = 0; i < this.data.length(); ++i) {
            int n2 = Math.floorMod(n - i, this.data.length());
            T t = this.data.get(n2);
            if (t == null) continue;
            builder.add(t);
        }
        return builder.build();
    }
}

