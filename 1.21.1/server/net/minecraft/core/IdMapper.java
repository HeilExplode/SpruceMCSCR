/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;

public class IdMapper<T>
implements IdMap<T> {
    private int nextId;
    private final Reference2IntMap<T> tToId;
    private final List<T> idToT;

    public IdMapper() {
        this(512);
    }

    public IdMapper(int n) {
        this.idToT = Lists.newArrayListWithExpectedSize((int)n);
        this.tToId = new Reference2IntOpenHashMap(n);
        this.tToId.defaultReturnValue(-1);
    }

    public void addMapping(T t, int n) {
        this.tToId.put(t, n);
        while (this.idToT.size() <= n) {
            this.idToT.add(null);
        }
        this.idToT.set(n, t);
        if (this.nextId <= n) {
            this.nextId = n + 1;
        }
    }

    public void add(T t) {
        this.addMapping(t, this.nextId);
    }

    @Override
    public int getId(T t) {
        return this.tToId.getInt(t);
    }

    @Override
    @Nullable
    public final T byId(int n) {
        if (n >= 0 && n < this.idToT.size()) {
            return this.idToT.get(n);
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.idToT.iterator(), Objects::nonNull);
    }

    public boolean contains(int n) {
        return this.byId(n) != null;
    }

    @Override
    public int size() {
        return this.tToId.size();
    }
}

