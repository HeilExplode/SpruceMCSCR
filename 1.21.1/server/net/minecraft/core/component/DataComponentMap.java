/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Sets
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.core.component;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;

public interface DataComponentMap
extends Iterable<TypedDataComponent<?>> {
    public static final DataComponentMap EMPTY = new DataComponentMap(){

        @Override
        @Nullable
        public <T> T get(DataComponentType<? extends T> dataComponentType) {
            return null;
        }

        @Override
        public Set<DataComponentType<?>> keySet() {
            return Set.of();
        }

        @Override
        public Iterator<TypedDataComponent<?>> iterator() {
            return Collections.emptyIterator();
        }
    };
    public static final Codec<DataComponentMap> CODEC = DataComponentMap.makeCodecFromMap(DataComponentType.VALUE_MAP_CODEC);

    public static Codec<DataComponentMap> makeCodec(Codec<DataComponentType<?>> codec) {
        return DataComponentMap.makeCodecFromMap(Codec.dispatchedMap(codec, DataComponentType::codecOrThrow));
    }

    public static Codec<DataComponentMap> makeCodecFromMap(Codec<Map<DataComponentType<?>, Object>> codec) {
        return codec.flatComapMap(Builder::buildFromMapTrusted, dataComponentMap -> {
            int n = dataComponentMap.size();
            if (n == 0) {
                return DataResult.success((Object)Reference2ObjectMaps.emptyMap());
            }
            Reference2ObjectArrayMap reference2ObjectArrayMap = new Reference2ObjectArrayMap(n);
            for (TypedDataComponent<?> typedDataComponent : dataComponentMap) {
                if (typedDataComponent.type().isTransient()) continue;
                reference2ObjectArrayMap.put(typedDataComponent.type(), typedDataComponent.value());
            }
            return DataResult.success((Object)reference2ObjectArrayMap);
        });
    }

    public static DataComponentMap composite(final DataComponentMap dataComponentMap, final DataComponentMap dataComponentMap2) {
        return new DataComponentMap(){

            @Override
            @Nullable
            public <T> T get(DataComponentType<? extends T> dataComponentType) {
                T t = dataComponentMap2.get(dataComponentType);
                if (t != null) {
                    return t;
                }
                return dataComponentMap.get(dataComponentType);
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return Sets.union(dataComponentMap.keySet(), dataComponentMap2.keySet());
            }
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    @Nullable
    public <T> T get(DataComponentType<? extends T> var1);

    public Set<DataComponentType<?>> keySet();

    default public boolean has(DataComponentType<?> dataComponentType) {
        return this.get(dataComponentType) != null;
    }

    default public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T t) {
        T t2 = this.get(dataComponentType);
        return t2 != null ? t2 : t;
    }

    @Nullable
    default public <T> TypedDataComponent<T> getTyped(DataComponentType<T> dataComponentType) {
        T t = this.get(dataComponentType);
        return t != null ? new TypedDataComponent<T>(dataComponentType, t) : null;
    }

    @Override
    default public Iterator<TypedDataComponent<?>> iterator() {
        return Iterators.transform(this.keySet().iterator(), dataComponentType -> Objects.requireNonNull(this.getTyped((DataComponentType)dataComponentType)));
    }

    default public Stream<TypedDataComponent<?>> stream() {
        return StreamSupport.stream(Spliterators.spliterator(this.iterator(), (long)this.size(), 1345), false);
    }

    default public int size() {
        return this.keySet().size();
    }

    default public boolean isEmpty() {
        return this.size() == 0;
    }

    default public DataComponentMap filter(final Predicate<DataComponentType<?>> predicate) {
        return new DataComponentMap(){

            @Override
            @Nullable
            public <T> T get(DataComponentType<? extends T> dataComponentType) {
                return predicate.test(dataComponentType) ? (T)DataComponentMap.this.get(dataComponentType) : null;
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return Sets.filter(DataComponentMap.this.keySet(), predicate::test);
            }
        };
    }

    public static class Builder {
        private final Reference2ObjectMap<DataComponentType<?>, Object> map = new Reference2ObjectArrayMap();

        Builder() {
        }

        public <T> Builder set(DataComponentType<T> dataComponentType, @Nullable T t) {
            this.setUnchecked(dataComponentType, t);
            return this;
        }

        <T> void setUnchecked(DataComponentType<T> dataComponentType, @Nullable Object object) {
            if (object != null) {
                this.map.put(dataComponentType, object);
            } else {
                this.map.remove(dataComponentType);
            }
        }

        public Builder addAll(DataComponentMap dataComponentMap) {
            for (TypedDataComponent<?> typedDataComponent : dataComponentMap) {
                this.map.put(typedDataComponent.type(), typedDataComponent.value());
            }
            return this;
        }

        public DataComponentMap build() {
            return Builder.buildFromMapTrusted(this.map);
        }

        private static DataComponentMap buildFromMapTrusted(Map<DataComponentType<?>, Object> map) {
            if (map.isEmpty()) {
                return EMPTY;
            }
            if (map.size() < 8) {
                return new SimpleMap((Reference2ObjectMap<DataComponentType<?>, Object>)new Reference2ObjectArrayMap(map));
            }
            return new SimpleMap((Reference2ObjectMap<DataComponentType<?>, Object>)new Reference2ObjectOpenHashMap(map));
        }

        record SimpleMap(Reference2ObjectMap<DataComponentType<?>, Object> map) implements DataComponentMap
        {
            @Override
            @Nullable
            public <T> T get(DataComponentType<? extends T> dataComponentType) {
                return (T)this.map.get(dataComponentType);
            }

            @Override
            public boolean has(DataComponentType<?> dataComponentType) {
                return this.map.containsKey(dataComponentType);
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return this.map.keySet();
            }

            @Override
            public Iterator<TypedDataComponent<?>> iterator() {
                return Iterators.transform((Iterator)Reference2ObjectMaps.fastIterator(this.map), TypedDataComponent::fromEntryUnchecked);
            }

            @Override
            public int size() {
                return this.map.size();
            }

            @Override
            public String toString() {
                return this.map.toString();
            }
        }
    }
}

