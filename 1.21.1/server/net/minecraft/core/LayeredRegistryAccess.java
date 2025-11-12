/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

public class LayeredRegistryAccess<T> {
    private final List<T> keys;
    private final List<RegistryAccess.Frozen> values;
    private final RegistryAccess.Frozen composite;

    public LayeredRegistryAccess(List<T> list) {
        this(list, Util.make(() -> {
            Object[] objectArray = new RegistryAccess.Frozen[list.size()];
            Arrays.fill(objectArray, RegistryAccess.EMPTY);
            return Arrays.asList(objectArray);
        }));
    }

    private LayeredRegistryAccess(List<T> list, List<RegistryAccess.Frozen> list2) {
        this.keys = List.copyOf(list);
        this.values = List.copyOf(list2);
        this.composite = new RegistryAccess.ImmutableRegistryAccess(LayeredRegistryAccess.collectRegistries(list2.stream())).freeze();
    }

    private int getLayerIndexOrThrow(T t) {
        int n = this.keys.indexOf(t);
        if (n == -1) {
            throw new IllegalStateException("Can't find " + String.valueOf(t) + " inside " + String.valueOf(this.keys));
        }
        return n;
    }

    public RegistryAccess.Frozen getLayer(T t) {
        int n = this.getLayerIndexOrThrow(t);
        return this.values.get(n);
    }

    public RegistryAccess.Frozen getAccessForLoading(T t) {
        int n = this.getLayerIndexOrThrow(t);
        return this.getCompositeAccessForLayers(0, n);
    }

    public RegistryAccess.Frozen getAccessFrom(T t) {
        int n = this.getLayerIndexOrThrow(t);
        return this.getCompositeAccessForLayers(n, this.values.size());
    }

    private RegistryAccess.Frozen getCompositeAccessForLayers(int n, int n2) {
        return new RegistryAccess.ImmutableRegistryAccess(LayeredRegistryAccess.collectRegistries(this.values.subList(n, n2).stream())).freeze();
    }

    public LayeredRegistryAccess<T> replaceFrom(T t, RegistryAccess.Frozen ... frozenArray) {
        return this.replaceFrom(t, Arrays.asList(frozenArray));
    }

    public LayeredRegistryAccess<T> replaceFrom(T t, List<RegistryAccess.Frozen> list) {
        int n = this.getLayerIndexOrThrow(t);
        if (list.size() > this.values.size() - n) {
            throw new IllegalStateException("Too many values to replace");
        }
        ArrayList<RegistryAccess.Frozen> arrayList = new ArrayList<RegistryAccess.Frozen>();
        for (int i = 0; i < n; ++i) {
            arrayList.add(this.values.get(i));
        }
        arrayList.addAll(list);
        while (arrayList.size() < this.values.size()) {
            arrayList.add(RegistryAccess.EMPTY);
        }
        return new LayeredRegistryAccess<T>(this.keys, arrayList);
    }

    public RegistryAccess.Frozen compositeAccess() {
        return this.composite;
    }

    private static Map<ResourceKey<? extends Registry<?>>, Registry<?>> collectRegistries(Stream<? extends RegistryAccess> stream) {
        HashMap hashMap = new HashMap();
        stream.forEach(registryAccess -> registryAccess.registries().forEach(registryEntry -> {
            if (hashMap.put(registryEntry.key(), registryEntry.value()) != null) {
                throw new IllegalStateException("Duplicated registry " + String.valueOf(registryEntry.key()));
            }
        }));
        return hashMap;
    }
}

