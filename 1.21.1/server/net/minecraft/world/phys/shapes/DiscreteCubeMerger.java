/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.IntMath
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.world.phys.shapes;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.CubePointRange;
import net.minecraft.world.phys.shapes.IndexMerger;
import net.minecraft.world.phys.shapes.Shapes;

public final class DiscreteCubeMerger
implements IndexMerger {
    private final CubePointRange result;
    private final int firstDiv;
    private final int secondDiv;

    DiscreteCubeMerger(int n, int n2) {
        this.result = new CubePointRange((int)Shapes.lcm(n, n2));
        int n3 = IntMath.gcd((int)n, (int)n2);
        this.firstDiv = n / n3;
        this.secondDiv = n2 / n3;
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int n = this.result.size() - 1;
        for (int i = 0; i < n; ++i) {
            if (indexConsumer.merge(i / this.secondDiv, i / this.firstDiv, i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.result.size();
    }

    @Override
    public DoubleList getList() {
        return this.result;
    }
}

