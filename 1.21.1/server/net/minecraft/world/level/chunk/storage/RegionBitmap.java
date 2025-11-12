/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntSet
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.BitSet;

public class RegionBitmap {
    private final BitSet used = new BitSet();

    public void force(int n, int n2) {
        this.used.set(n, n + n2);
    }

    public void free(int n, int n2) {
        this.used.clear(n, n + n2);
    }

    public int allocate(int n) {
        int n2 = 0;
        while (true) {
            int n3;
            int n4;
            if ((n4 = this.used.nextSetBit(n3 = this.used.nextClearBit(n2))) == -1 || n4 - n3 >= n) {
                this.force(n3, n);
                return n3;
            }
            n2 = n4;
        }
    }

    @VisibleForTesting
    public IntSet getUsed() {
        return (IntSet)this.used.stream().collect(IntArraySet::new, IntCollection::add, IntCollection::addAll);
    }
}

