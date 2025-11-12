/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.AbstractDoubleList
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.IndexMerger;

public class NonOverlappingMerger
extends AbstractDoubleList
implements IndexMerger {
    private final DoubleList lower;
    private final DoubleList upper;
    private final boolean swap;

    protected NonOverlappingMerger(DoubleList doubleList, DoubleList doubleList2, boolean bl) {
        this.lower = doubleList;
        this.upper = doubleList2;
        this.swap = bl;
    }

    @Override
    public int size() {
        return this.lower.size() + this.upper.size();
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        if (this.swap) {
            return this.forNonSwappedIndexes((n, n2, n3) -> indexConsumer.merge(n2, n, n3));
        }
        return this.forNonSwappedIndexes(indexConsumer);
    }

    private boolean forNonSwappedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int n;
        int n2 = this.lower.size();
        for (n = 0; n < n2; ++n) {
            if (indexConsumer.merge(n, -1, n)) continue;
            return false;
        }
        n = this.upper.size() - 1;
        for (int i = 0; i < n; ++i) {
            if (indexConsumer.merge(n2 - 1, i, n2 + i)) continue;
            return false;
        }
        return true;
    }

    public double getDouble(int n) {
        if (n < this.lower.size()) {
            return this.lower.getDouble(n);
        }
        return this.upper.getDouble(n - this.lower.size());
    }

    @Override
    public DoubleList getList() {
        return this;
    }
}

