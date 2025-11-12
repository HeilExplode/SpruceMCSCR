/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicate
 *  com.google.common.base.Predicates
 *  com.google.common.collect.Iterators
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.util.Mth;

public class CrudeIncrementalIntIdentityHashBiMap<K>
implements IdMap<K> {
    private static final int NOT_FOUND = -1;
    private static final Object EMPTY_SLOT = null;
    private static final float LOADFACTOR = 0.8f;
    private K[] keys;
    private int[] values;
    private K[] byId;
    private int nextId;
    private int size;

    private CrudeIncrementalIntIdentityHashBiMap(int n) {
        this.keys = new Object[n];
        this.values = new int[n];
        this.byId = new Object[n];
    }

    private CrudeIncrementalIntIdentityHashBiMap(K[] KArray, int[] nArray, K[] KArray2, int n, int n2) {
        this.keys = KArray;
        this.values = nArray;
        this.byId = KArray2;
        this.nextId = n;
        this.size = n2;
    }

    public static <A> CrudeIncrementalIntIdentityHashBiMap<A> create(int n) {
        return new CrudeIncrementalIntIdentityHashBiMap((int)((float)n / 0.8f));
    }

    @Override
    public int getId(@Nullable K k) {
        return this.getValue(this.indexOf(k, this.hash(k)));
    }

    @Override
    @Nullable
    public K byId(int n) {
        if (n < 0 || n >= this.byId.length) {
            return null;
        }
        return this.byId[n];
    }

    private int getValue(int n) {
        if (n == -1) {
            return -1;
        }
        return this.values[n];
    }

    public boolean contains(K k) {
        return this.getId(k) != -1;
    }

    public boolean contains(int n) {
        return this.byId(n) != null;
    }

    public int add(K k) {
        int n = this.nextId();
        this.addMapping(k, n);
        return n;
    }

    private int nextId() {
        while (this.nextId < this.byId.length && this.byId[this.nextId] != null) {
            ++this.nextId;
        }
        return this.nextId;
    }

    private void grow(int n) {
        K[] KArray = this.keys;
        int[] nArray = this.values;
        CrudeIncrementalIntIdentityHashBiMap<K> crudeIncrementalIntIdentityHashBiMap = new CrudeIncrementalIntIdentityHashBiMap<K>(n);
        for (int i = 0; i < KArray.length; ++i) {
            if (KArray[i] == null) continue;
            crudeIncrementalIntIdentityHashBiMap.addMapping(KArray[i], nArray[i]);
        }
        this.keys = crudeIncrementalIntIdentityHashBiMap.keys;
        this.values = crudeIncrementalIntIdentityHashBiMap.values;
        this.byId = crudeIncrementalIntIdentityHashBiMap.byId;
        this.nextId = crudeIncrementalIntIdentityHashBiMap.nextId;
        this.size = crudeIncrementalIntIdentityHashBiMap.size;
    }

    public void addMapping(K k, int n) {
        int n2;
        int n3 = Math.max(n, this.size + 1);
        if ((float)n3 >= (float)this.keys.length * 0.8f) {
            for (n2 = this.keys.length << 1; n2 < n; n2 <<= 1) {
            }
            this.grow(n2);
        }
        n2 = this.findEmpty(this.hash(k));
        this.keys[n2] = k;
        this.values[n2] = n;
        this.byId[n] = k;
        ++this.size;
        if (n == this.nextId) {
            ++this.nextId;
        }
    }

    private int hash(@Nullable K k) {
        return (Mth.murmurHash3Mixer(System.identityHashCode(k)) & Integer.MAX_VALUE) % this.keys.length;
    }

    private int indexOf(@Nullable K k, int n) {
        int n2;
        for (n2 = n; n2 < this.keys.length; ++n2) {
            if (this.keys[n2] == k) {
                return n2;
            }
            if (this.keys[n2] != EMPTY_SLOT) continue;
            return -1;
        }
        for (n2 = 0; n2 < n; ++n2) {
            if (this.keys[n2] == k) {
                return n2;
            }
            if (this.keys[n2] != EMPTY_SLOT) continue;
            return -1;
        }
        return -1;
    }

    private int findEmpty(int n) {
        int n2;
        for (n2 = n; n2 < this.keys.length; ++n2) {
            if (this.keys[n2] != EMPTY_SLOT) continue;
            return n2;
        }
        for (n2 = 0; n2 < n; ++n2) {
            if (this.keys[n2] != EMPTY_SLOT) continue;
            return n2;
        }
        throw new RuntimeException("Overflowed :(");
    }

    @Override
    public Iterator<K> iterator() {
        return Iterators.filter((Iterator)Iterators.forArray((Object[])this.byId), (Predicate)Predicates.notNull());
    }

    public void clear() {
        Arrays.fill(this.keys, null);
        Arrays.fill(this.byId, null);
        this.nextId = 0;
        this.size = 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    public CrudeIncrementalIntIdentityHashBiMap<K> copy() {
        return new CrudeIncrementalIntIdentityHashBiMap<Object>((Object[])this.keys.clone(), (int[])this.values.clone(), (Object[])this.byId.clone(), this.nextId, this.size);
    }
}

