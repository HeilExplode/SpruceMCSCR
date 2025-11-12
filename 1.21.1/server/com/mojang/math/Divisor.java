/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.ints.IntIterator
 */
package com.mojang.math;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.NoSuchElementException;

public class Divisor
implements IntIterator {
    private final int denominator;
    private final int quotient;
    private final int mod;
    private int returnedParts;
    private int remainder;

    public Divisor(int n, int n2) {
        this.denominator = n2;
        if (n2 > 0) {
            this.quotient = n / n2;
            this.mod = n % n2;
        } else {
            this.quotient = 0;
            this.mod = 0;
        }
    }

    public boolean hasNext() {
        return this.returnedParts < this.denominator;
    }

    public int nextInt() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        int n = this.quotient;
        this.remainder += this.mod;
        if (this.remainder >= this.denominator) {
            this.remainder -= this.denominator;
            ++n;
        }
        ++this.returnedParts;
        return n;
    }

    @VisibleForTesting
    public static Iterable<Integer> asIterable(int n, int n2) {
        return () -> new Divisor(n, n2);
    }
}

