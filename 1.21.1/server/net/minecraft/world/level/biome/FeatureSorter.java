/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public class FeatureSorter {
    public static <T> List<StepFeatureData> buildFeaturesPerStep(List<T> list, Function<T, List<HolderSet<PlacedFeature>>> function, boolean bl) {
        ArrayList<T> arrayList;
        Object object2;
        Object object3;
        ArrayList arrayList2;
        Object object42;
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        MutableInt mutableInt = new MutableInt(0);
        record FeatureData(int featureIndex, int step, PlacedFeature feature) {
        }
        Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        TreeMap<FeatureData, Set> treeMap = new TreeMap<FeatureData, Set>(comparator);
        int n = 0;
        for (Object object42 : list) {
            int n2;
            arrayList2 = Lists.newArrayList();
            object3 = function.apply(object42);
            n = Math.max(n, object3.size());
            for (n2 = 0; n2 < object3.size(); ++n2) {
                for (Object object5 : (HolderSet)object3.get(n2)) {
                    object2 = (PlacedFeature)object5.value();
                    arrayList2.add(new FeatureData(object2IntOpenHashMap.computeIfAbsent(object2, object -> mutableInt.getAndIncrement()), n2, (PlacedFeature)object2));
                }
            }
            for (n2 = 0; n2 < arrayList2.size(); ++n2) {
                arrayList = treeMap.computeIfAbsent((FeatureData)arrayList2.get(n2), featureData -> new TreeSet(comparator));
                if (n2 >= arrayList2.size() - 1) continue;
                arrayList.add((FeatureData)((FeatureData)arrayList2.get(n2 + 1)));
            }
        }
        TreeSet<FeatureData> treeSet = new TreeSet<FeatureData>(comparator);
        object42 = new TreeSet<FeatureData>(comparator);
        arrayList2 = Lists.newArrayList();
        for (FeatureData featureData2 : treeMap.keySet()) {
            if (!object42.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }
            if (treeSet.contains(featureData2) || !Graph.depthFirstSearch(treeMap, treeSet, object42, arrayList2::add, featureData2)) continue;
            if (bl) {
                int n3;
                arrayList = new ArrayList<T>(list);
                do {
                    n3 = arrayList.size();
                    object2 = arrayList.listIterator();
                    while (object2.hasNext()) {
                        Object e = object2.next();
                        object2.remove();
                        try {
                            FeatureSorter.buildFeaturesPerStep(arrayList, function, false);
                        }
                        catch (IllegalStateException illegalStateException) {
                            continue;
                        }
                        object2.add(e);
                    }
                } while (n3 != arrayList.size());
                throw new IllegalStateException("Feature order cycle found, involved sources: " + String.valueOf(arrayList));
            }
            throw new IllegalStateException("Feature order cycle found");
        }
        Collections.reverse(arrayList2);
        object3 = ImmutableList.builder();
        int n4 = 0;
        while (n4 < n) {
            Object object5;
            int n5 = n4++;
            object5 = arrayList2.stream().filter(featureData -> featureData.step() == n5).map(FeatureData::feature).collect(Collectors.toList());
            object3.add((Object)new StepFeatureData((List<PlacedFeature>)object5));
        }
        return object3.build();
    }

    public record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
        StepFeatureData(List<PlacedFeature> list) {
            this(list, Util.createIndexIdentityLookup(list));
        }
    }
}

