/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleArrayList
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 *  it.unimi.dsi.fastutil.doubles.DoubleLists
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import net.minecraft.world.phys.shapes.IndexMerger;

public class IndirectMerger
implements IndexMerger {
    private static final DoubleList EMPTY = DoubleLists.unmodifiable((DoubleList)DoubleArrayList.wrap((double[])new double[]{0.0}));
    private final double[] result;
    private final int[] firstIndices;
    private final int[] secondIndices;
    private final int resultLength;

    public IndirectMerger(DoubleList doubleList, DoubleList doubleList2, boolean bl, boolean bl2) {
        double d = Double.NaN;
        int n = doubleList.size();
        int n2 = doubleList2.size();
        int n3 = n + n2;
        this.result = new double[n3];
        this.firstIndices = new int[n3];
        this.secondIndices = new int[n3];
        boolean bl3 = !bl;
        boolean bl4 = !bl2;
        int n4 = 0;
        int n5 = 0;
        int n6 = 0;
        while (true) {
            double d2;
            boolean bl5;
            boolean bl6;
            boolean bl7 = n5 >= n;
            boolean bl8 = bl6 = n6 >= n2;
            if (bl7 && bl6) break;
            boolean bl9 = bl5 = !bl7 && (bl6 || doubleList.getDouble(n5) < doubleList2.getDouble(n6) + 1.0E-7);
            if (bl5) {
                ++n5;
                if (bl3 && (n6 == 0 || bl6)) {
                    continue;
                }
            } else {
                ++n6;
                if (bl4 && (n5 == 0 || bl7)) continue;
            }
            int n7 = n5 - 1;
            int n8 = n6 - 1;
            double d3 = d2 = bl5 ? doubleList.getDouble(n7) : doubleList2.getDouble(n8);
            if (!(d >= d2 - 1.0E-7)) {
                this.firstIndices[n4] = n7;
                this.secondIndices[n4] = n8;
                this.result[n4] = d2;
                ++n4;
                d = d2;
                continue;
            }
            this.firstIndices[n4 - 1] = n7;
            this.secondIndices[n4 - 1] = n8;
        }
        this.resultLength = Math.max(1, n4);
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int n = this.resultLength - 1;
        for (int i = 0; i < n; ++i) {
            if (indexConsumer.merge(this.firstIndices[i], this.secondIndices[i], i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.resultLength;
    }

    @Override
    public DoubleList getList() {
        return this.resultLength <= 1 ? EMPTY : DoubleArrayList.wrap((double[])this.result, (int)this.resultLength);
    }
}

