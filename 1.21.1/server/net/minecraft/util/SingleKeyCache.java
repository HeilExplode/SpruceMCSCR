/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;

public class SingleKeyCache<K, V> {
    private final Function<K, V> computeValue;
    @Nullable
    private K cacheKey = null;
    @Nullable
    private V cachedValue;

    public SingleKeyCache(Function<K, V> function) {
        this.computeValue = function;
    }

    public V getValue(K k) {
        if (this.cachedValue == null || !Objects.equals(this.cacheKey, k)) {
            this.cachedValue = this.computeValue.apply(k);
            this.cacheKey = k;
        }
        return this.cachedValue;
    }
}

