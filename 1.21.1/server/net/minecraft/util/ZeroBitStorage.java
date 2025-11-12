/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.util;

import java.util.Arrays;
import java.util.function.IntConsumer;
import net.minecraft.util.BitStorage;
import org.apache.commons.lang3.Validate;

public class ZeroBitStorage
implements BitStorage {
    public static final long[] RAW = new long[0];
    private final int size;

    public ZeroBitStorage(int n) {
        this.size = n;
    }

    @Override
    public int getAndSet(int n, int n2) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)n);
        Validate.inclusiveBetween((long)0L, (long)0L, (long)n2);
        return 0;
    }

    @Override
    public void set(int n, int n2) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)n);
        Validate.inclusiveBetween((long)0L, (long)0L, (long)n2);
    }

    @Override
    public int get(int n) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)n);
        return 0;
    }

    @Override
    public long[] getRaw() {
        return RAW;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getBits() {
        return 0;
    }

    @Override
    public void getAll(IntConsumer intConsumer) {
        for (int i = 0; i < this.size; ++i) {
            intConsumer.accept(0);
        }
    }

    @Override
    public void unpack(int[] nArray) {
        Arrays.fill(nArray, 0, this.size, 0);
    }

    @Override
    public BitStorage copy() {
        return this;
    }
}

