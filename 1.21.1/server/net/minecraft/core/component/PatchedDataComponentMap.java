/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  javax.annotation.Nullable
 */
package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;

public final class PatchedDataComponentMap
implements DataComponentMap {
    private final DataComponentMap prototype;
    private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
    private boolean copyOnWrite;

    public PatchedDataComponentMap(DataComponentMap dataComponentMap) {
        this(dataComponentMap, Reference2ObjectMaps.emptyMap(), true);
    }

    private PatchedDataComponentMap(DataComponentMap dataComponentMap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap, boolean bl) {
        this.prototype = dataComponentMap;
        this.patch = reference2ObjectMap;
        this.copyOnWrite = bl;
    }

    public static PatchedDataComponentMap fromPatch(DataComponentMap dataComponentMap, DataComponentPatch dataComponentPatch) {
        if (PatchedDataComponentMap.isPatchSanitized(dataComponentMap, dataComponentPatch.map)) {
            return new PatchedDataComponentMap(dataComponentMap, dataComponentPatch.map, true);
        }
        PatchedDataComponentMap patchedDataComponentMap = new PatchedDataComponentMap(dataComponentMap);
        patchedDataComponentMap.applyPatch(dataComponentPatch);
        return patchedDataComponentMap;
    }

    private static boolean isPatchSanitized(DataComponentMap dataComponentMap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap) {
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(reference2ObjectMap)) {
            Object t = dataComponentMap.get((DataComponentType)entry.getKey());
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent() && optional.get().equals(t)) {
                return false;
            }
            if (!optional.isEmpty() || t != null) continue;
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> dataComponentType) {
        Optional optional = (Optional)this.patch.get(dataComponentType);
        if (optional != null) {
            return optional.orElse(null);
        }
        return this.prototype.get(dataComponentType);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> dataComponentType, @Nullable T t) {
        this.ensureMapOwnership();
        T t2 = this.prototype.get(dataComponentType);
        Optional optional = Objects.equals(t, t2) ? (Optional)this.patch.remove(dataComponentType) : (Optional)this.patch.put(dataComponentType, Optional.ofNullable(t));
        if (optional != null) {
            return optional.orElse(t2);
        }
        return t2;
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> dataComponentType) {
        this.ensureMapOwnership();
        T t = this.prototype.get(dataComponentType);
        Optional optional = t != null ? (Optional)this.patch.put(dataComponentType, Optional.empty()) : (Optional)this.patch.remove(dataComponentType);
        if (optional != null) {
            return optional.orElse(null);
        }
        return t;
    }

    public void applyPatch(DataComponentPatch dataComponentPatch) {
        this.ensureMapOwnership();
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(dataComponentPatch.map)) {
            this.applyPatch((DataComponentType)entry.getKey(), (Optional)entry.getValue());
        }
    }

    private void applyPatch(DataComponentType<?> dataComponentType, Optional<?> optional) {
        Object obj = this.prototype.get(dataComponentType);
        if (optional.isPresent()) {
            if (optional.get().equals(obj)) {
                this.patch.remove(dataComponentType);
            } else {
                this.patch.put(dataComponentType, optional);
            }
        } else if (obj != null) {
            this.patch.put(dataComponentType, Optional.empty());
        } else {
            this.patch.remove(dataComponentType);
        }
    }

    public void restorePatch(DataComponentPatch dataComponentPatch) {
        this.ensureMapOwnership();
        this.patch.clear();
        this.patch.putAll(dataComponentPatch.map);
    }

    public void setAll(DataComponentMap dataComponentMap) {
        for (TypedDataComponent<?> typedDataComponent : dataComponentMap) {
            typedDataComponent.applyTo(this);
        }
    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.patch = new Reference2ObjectArrayMap(this.patch);
            this.copyOnWrite = false;
        }
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        }
        ReferenceArraySet referenceArraySet = new ReferenceArraySet(this.prototype.keySet());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                referenceArraySet.add((DataComponentType)entry.getKey());
                continue;
            }
            referenceArraySet.remove(entry.getKey());
        }
        return referenceArraySet;
    }

    @Override
    public Iterator<TypedDataComponent<?>> iterator() {
        if (this.patch.isEmpty()) {
            return this.prototype.iterator();
        }
        ArrayList<Object> arrayList = new ArrayList<Object>(this.patch.size() + this.prototype.size());
        for (Object object : Reference2ObjectMaps.fastIterable(this.patch)) {
            if (!((Optional)object.getValue()).isPresent()) continue;
            arrayList.add(TypedDataComponent.createUnchecked((DataComponentType)object.getKey(), ((Optional)object.getValue()).get()));
        }
        for (Object object : this.prototype) {
            if (this.patch.containsKey(((TypedDataComponent)object).type())) continue;
            arrayList.add(object);
        }
        return arrayList.iterator();
    }

    @Override
    public int size() {
        int n = this.prototype.size();
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            boolean bl;
            boolean bl2 = ((Optional)entry.getValue()).isPresent();
            if (bl2 == (bl = this.prototype.has((DataComponentType)entry.getKey()))) continue;
            n += bl2 ? 1 : -1;
        }
        return n;
    }

    public DataComponentPatch asPatch() {
        if (this.patch.isEmpty()) {
            return DataComponentPatch.EMPTY;
        }
        this.copyOnWrite = true;
        return new DataComponentPatch(this.patch);
    }

    public PatchedDataComponentMap copy() {
        this.copyOnWrite = true;
        return new PatchedDataComponentMap(this.prototype, this.patch, true);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PatchedDataComponentMap)) return false;
        PatchedDataComponentMap patchedDataComponentMap = (PatchedDataComponentMap)object;
        if (!this.prototype.equals(patchedDataComponentMap.prototype)) return false;
        if (!this.patch.equals(patchedDataComponentMap.patch)) return false;
        return true;
    }

    public int hashCode() {
        return this.prototype.hashCode() + this.patch.hashCode() * 31;
    }

    public String toString() {
        return "{" + this.stream().map(TypedDataComponent::toString).collect(Collectors.joining(", ")) + "}";
    }
}

