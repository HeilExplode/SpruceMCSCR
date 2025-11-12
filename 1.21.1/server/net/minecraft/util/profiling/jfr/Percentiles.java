/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.Quantiles
 *  com.google.common.math.Quantiles$ScaleAndIndexes
 *  it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap
 *  it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap
 *  it.unimi.dsi.fastutil.ints.Int2DoubleSortedMaps
 */
package net.minecraft.util.profiling.jfr;

import com.google.common.math.Quantiles;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMaps;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.Util;

public class Percentiles {
    public static final Quantiles.ScaleAndIndexes DEFAULT_INDEXES = Quantiles.scale((int)100).indexes(new int[]{50, 75, 90, 99});

    private Percentiles() {
    }

    public static Map<Integer, Double> evaluate(long[] lArray) {
        return lArray.length == 0 ? Map.of() : Percentiles.sorted(DEFAULT_INDEXES.compute(lArray));
    }

    public static Map<Integer, Double> evaluate(double[] dArray) {
        return dArray.length == 0 ? Map.of() : Percentiles.sorted(DEFAULT_INDEXES.compute(dArray));
    }

    private static Map<Integer, Double> sorted(Map<Integer, Double> map) {
        Int2DoubleSortedMap int2DoubleSortedMap = (Int2DoubleSortedMap)Util.make(new Int2DoubleRBTreeMap(Comparator.reverseOrder()), int2DoubleRBTreeMap -> int2DoubleRBTreeMap.putAll(map));
        return Int2DoubleSortedMaps.unmodifiable((Int2DoubleSortedMap)int2DoubleSortedMap);
    }
}

