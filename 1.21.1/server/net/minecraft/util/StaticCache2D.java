/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.Locale;
import java.util.function.Consumer;

public class StaticCache2D<T> {
    private final int minX;
    private final int minZ;
    private final int sizeX;
    private final int sizeZ;
    private final Object[] cache;

    public static <T> StaticCache2D<T> create(int n, int n2, int n3, Initializer<T> initializer) {
        int n4 = n - n3;
        int n5 = n2 - n3;
        int n6 = 2 * n3 + 1;
        return new StaticCache2D<T>(n4, n5, n6, n6, initializer);
    }

    private StaticCache2D(int n, int n2, int n3, int n4, Initializer<T> initializer) {
        this.minX = n;
        this.minZ = n2;
        this.sizeX = n3;
        this.sizeZ = n4;
        this.cache = new Object[this.sizeX * this.sizeZ];
        for (int i = n; i < n + n3; ++i) {
            for (int j = n2; j < n2 + n4; ++j) {
                this.cache[this.getIndex((int)i, (int)j)] = initializer.get(i, j);
            }
        }
    }

    public void forEach(Consumer<T> consumer) {
        for (Object object : this.cache) {
            consumer.accept(object);
        }
    }

    public T get(int n, int n2) {
        if (!this.contains(n, n2)) {
            throw new IllegalArgumentException("Requested out of range value (" + n + "," + n2 + ") from " + String.valueOf(this));
        }
        return (T)this.cache[this.getIndex(n, n2)];
    }

    public boolean contains(int n, int n2) {
        int n3 = n - this.minX;
        int n4 = n2 - this.minZ;
        return n3 >= 0 && n3 < this.sizeX && n4 >= 0 && n4 < this.sizeZ;
    }

    public String toString() {
        return String.format(Locale.ROOT, "StaticCache2D[%d, %d, %d, %d]", this.minX, this.minZ, this.minX + this.sizeX, this.minZ + this.sizeZ);
    }

    private int getIndex(int n, int n2) {
        int n3 = n - this.minX;
        int n4 = n2 - this.minZ;
        return n3 * this.sizeZ + n4;
    }

    @FunctionalInterface
    public static interface Initializer<T> {
        public T get(int var1, int var2);
    }
}

