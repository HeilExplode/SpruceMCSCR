/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends Entry<K>> {
    private final Map<K, V> contents = new HashMap();

    public DependencySorter<K, V> addEntry(K k, V v) {
        this.contents.put(k, v);
        return this;
    }

    private void visitDependenciesAndElement(Multimap<K, K> multimap, Set<K> set, K k, BiConsumer<K, V> biConsumer) {
        if (!set.add(k)) {
            return;
        }
        multimap.get(k).forEach(object -> this.visitDependenciesAndElement(multimap, set, object, biConsumer));
        Entry entry = (Entry)this.contents.get(k);
        if (entry != null) {
            biConsumer.accept(k, entry);
        }
    }

    private static <K> boolean isCyclic(Multimap<K, K> multimap, K k, K k2) {
        Collection collection = multimap.get(k2);
        if (collection.contains(k)) {
            return true;
        }
        return collection.stream().anyMatch(object2 -> DependencySorter.isCyclic(multimap, k, object2));
    }

    private static <K> void addDependencyIfNotCyclic(Multimap<K, K> multimap, K k, K k2) {
        if (!DependencySorter.isCyclic(multimap, k, k2)) {
            multimap.put(k, k2);
        }
    }

    public void orderByDependencies(BiConsumer<K, V> biConsumer) {
        HashMultimap hashMultimap = HashMultimap.create();
        this.contents.forEach((arg_0, arg_1) -> DependencySorter.lambda$orderByDependencies$3((Multimap)hashMultimap, arg_0, arg_1));
        this.contents.forEach((arg_0, arg_1) -> DependencySorter.lambda$orderByDependencies$5((Multimap)hashMultimap, arg_0, arg_1));
        HashSet hashSet = new HashSet();
        this.contents.keySet().forEach(arg_0 -> this.lambda$orderByDependencies$6((Multimap)hashMultimap, hashSet, biConsumer, arg_0));
    }

    private /* synthetic */ void lambda$orderByDependencies$6(Multimap multimap, Set set, BiConsumer biConsumer, Object object) {
        this.visitDependenciesAndElement(multimap, set, object, biConsumer);
    }

    private static /* synthetic */ void lambda$orderByDependencies$5(Multimap multimap, Object object, Entry entry) {
        entry.visitOptionalDependencies(object2 -> DependencySorter.addDependencyIfNotCyclic(multimap, object, object2));
    }

    private static /* synthetic */ void lambda$orderByDependencies$3(Multimap multimap, Object object, Entry entry) {
        entry.visitRequiredDependencies(object2 -> DependencySorter.addDependencyIfNotCyclic(multimap, object, object2));
    }

    public static interface Entry<K> {
        public void visitRequiredDependencies(Consumer<K> var1);

        public void visitOptionalDependencies(Consumer<K> var1);
    }
}

