/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.debugchart;

import net.minecraft.util.debugchart.SampleLogger;

public abstract class AbstractSampleLogger
implements SampleLogger {
    protected final long[] defaults;
    protected final long[] sample;

    protected AbstractSampleLogger(int n, long[] lArray) {
        if (lArray.length != n) {
            throw new IllegalArgumentException("defaults have incorrect length of " + lArray.length);
        }
        this.sample = new long[n];
        this.defaults = lArray;
    }

    @Override
    public void logFullSample(long[] lArray) {
        System.arraycopy(lArray, 0, this.sample, 0, lArray.length);
        this.useSample();
        this.resetSample();
    }

    @Override
    public void logSample(long l) {
        this.sample[0] = l;
        this.useSample();
        this.resetSample();
    }

    @Override
    public void logPartialSample(long l, int n) {
        if (n < 1 || n >= this.sample.length) {
            throw new IndexOutOfBoundsException(n + " out of bounds for dimensions " + this.sample.length);
        }
        this.sample[n] = l;
    }

    protected abstract void useSample();

    protected void resetSample() {
        System.arraycopy(this.defaults, 0, this.sample, 0, this.defaults.length);
    }
}

