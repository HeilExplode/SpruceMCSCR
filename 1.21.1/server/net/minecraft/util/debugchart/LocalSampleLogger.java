/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.debugchart;

import net.minecraft.util.debugchart.AbstractSampleLogger;
import net.minecraft.util.debugchart.SampleStorage;

public class LocalSampleLogger
extends AbstractSampleLogger
implements SampleStorage {
    public static final int CAPACITY = 240;
    private final long[][] samples;
    private int start;
    private int size;

    public LocalSampleLogger(int n) {
        this(n, new long[n]);
    }

    public LocalSampleLogger(int n, long[] lArray) {
        super(n, lArray);
        this.samples = new long[240][n];
    }

    @Override
    protected void useSample() {
        int n = this.wrapIndex(this.start + this.size);
        System.arraycopy(this.sample, 0, this.samples[n], 0, this.sample.length);
        if (this.size < 240) {
            ++this.size;
        } else {
            this.start = this.wrapIndex(this.start + 1);
        }
    }

    @Override
    public int capacity() {
        return this.samples.length;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public long get(int n) {
        return this.get(n, 0);
    }

    @Override
    public long get(int n, int n2) {
        if (n < 0 || n >= this.size) {
            throw new IndexOutOfBoundsException(n + " out of bounds for length " + this.size);
        }
        long[] lArray = this.samples[this.wrapIndex(this.start + n)];
        if (n2 < 0 || n2 >= lArray.length) {
            throw new IndexOutOfBoundsException(n2 + " out of bounds for dimensions " + lArray.length);
        }
        return lArray[n2];
    }

    private int wrapIndex(int n) {
        return n % 240;
    }

    @Override
    public void reset() {
        this.start = 0;
        this.size = 0;
    }
}

