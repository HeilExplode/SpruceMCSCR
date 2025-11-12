/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 */
package net.minecraft.core.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class DataComponentPredicate
implements Predicate<DataComponentMap> {
    public static final Codec<DataComponentPredicate> CODEC = DataComponentType.VALUE_MAP_CODEC.xmap(map -> new DataComponentPredicate(map.entrySet().stream().map(TypedDataComponent::fromEntryUnchecked).collect(Collectors.toList())), dataComponentPredicate -> dataComponentPredicate.expectedComponents.stream().filter(typedDataComponent -> !typedDataComponent.type().isTransient()).collect(Collectors.toMap(TypedDataComponent::type, TypedDataComponent::value)));
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate> STREAM_CODEC = TypedDataComponent.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DataComponentPredicate::new, dataComponentPredicate -> dataComponentPredicate.expectedComponents);
    public static final DataComponentPredicate EMPTY = new DataComponentPredicate(List.of());
    private final List<TypedDataComponent<?>> expectedComponents;

    DataComponentPredicate(List<TypedDataComponent<?>> list) {
        this.expectedComponents = list;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DataComponentPredicate allOf(DataComponentMap dataComponentMap) {
        return new DataComponentPredicate((List<TypedDataComponent<?>>)ImmutableList.copyOf((Iterable)dataComponentMap));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (!(object instanceof DataComponentPredicate)) return false;
        DataComponentPredicate dataComponentPredicate = (DataComponentPredicate)object;
        if (!this.expectedComponents.equals(dataComponentPredicate.expectedComponents)) return false;
        return true;
    }

    public int hashCode() {
        return this.expectedComponents.hashCode();
    }

    public String toString() {
        return this.expectedComponents.toString();
    }

    @Override
    public boolean test(DataComponentMap dataComponentMap) {
        for (TypedDataComponent<?> typedDataComponent : this.expectedComponents) {
            Object obj = dataComponentMap.get(typedDataComponent.type());
            if (Objects.equals(typedDataComponent.value(), obj)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean test(DataComponentHolder dataComponentHolder) {
        return this.test(dataComponentHolder.getComponents());
    }

    public boolean alwaysMatches() {
        return this.expectedComponents.isEmpty();
    }

    public DataComponentPatch asPatch() {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        for (TypedDataComponent<?> typedDataComponent : this.expectedComponents) {
            builder.set(typedDataComponent);
        }
        return builder.build();
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((DataComponentMap)object);
    }

    public static class Builder {
        private final List<TypedDataComponent<?>> expectedComponents = new ArrayList();

        Builder() {
        }

        public <T> Builder expect(DataComponentType<? super T> dataComponentType, T t) {
            for (TypedDataComponent<?> typedDataComponent : this.expectedComponents) {
                if (typedDataComponent.type() != dataComponentType) continue;
                throw new IllegalArgumentException("Predicate already has component of type: '" + String.valueOf(dataComponentType) + "'");
            }
            this.expectedComponents.add(new TypedDataComponent<T>(dataComponentType, t));
            return this;
        }

        public DataComponentPredicate build() {
            return new DataComponentPredicate(List.copyOf(this.expectedComponents));
        }
    }
}

