/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.IndexMerger;

public class IdenticalMerger
implements IndexMerger {
    private final DoubleList coords;

    public IdenticalMerger(DoubleList doubleList) {
        this.coords = doubleList;
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int n = this.coords.size() - 1;
        for (int i = 0; i < n; ++i) {
            if (indexConsumer.merge(i, i, i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.coords.size();
    }

    @Override
    public DoubleList getList() {
        return this.coords;
    }
}

