/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ArrayTable
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.Table
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class StateHolder<O, S> {
    public static final String NAME_TAG = "Name";
    public static final String PROPERTIES_TAG = "Properties";
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Map.Entry<Property<?>, Comparable<?>>, String>(){

        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            }
            Property<?> property = entry.getKey();
            return property.getName() + "=" + this.getName(property, entry.getValue());
        }

        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
            return property.getName(comparable);
        }

        @Override
        public /* synthetic */ Object apply(@Nullable Object object) {
            return this.apply((Map.Entry)object);
        }
    };
    protected final O owner;
    private final Reference2ObjectArrayMap<Property<?>, Comparable<?>> values;
    private Table<Property<?>, Comparable<?>, S> neighbours;
    protected final MapCodec<S> propertiesCodec;

    protected StateHolder(O o, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<S> mapCodec) {
        this.owner = o;
        this.values = reference2ObjectArrayMap;
        this.propertiesCodec = mapCodec;
    }

    public <T extends Comparable<T>> S cycle(Property<T> property) {
        return this.setValue(property, (Comparable)StateHolder.findNextInCollection(property.getPossibleValues(), this.getValue(property)));
    }

    protected static <T> T findNextInCollection(Collection<T> collection, T t) {
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().equals(t)) continue;
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return collection.iterator().next();
        }
        return iterator.next();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.owner);
        if (!this.getValues().isEmpty()) {
            stringBuilder.append('[');
            stringBuilder.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.values.keySet());
    }

    public <T extends Comparable<T>> boolean hasProperty(Property<T> property) {
        return this.values.containsKey(property);
    }

    public <T extends Comparable<T>> T getValue(Property<T> property) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot get property " + String.valueOf(property) + " as it does not exist in " + String.valueOf(this.owner));
        }
        return (T)((Comparable)property.getValueClass().cast(comparable));
    }

    public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> property) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            return Optional.empty();
        }
        return Optional.of((Comparable)property.getValueClass().cast(comparable));
    }

    public <T extends Comparable<T>, V extends T> S setValue(Property<T> property, V v) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot set property " + String.valueOf(property) + " as it does not exist in " + String.valueOf(this.owner));
        }
        if (comparable.equals(v)) {
            return (S)this;
        }
        Object object = this.neighbours.get(property, v);
        if (object == null) {
            throw new IllegalArgumentException("Cannot set property " + String.valueOf(property) + " to " + String.valueOf(v) + " on " + String.valueOf(this.owner) + ", it is not an allowed value");
        }
        return (S)object;
    }

    public <T extends Comparable<T>, V extends T> S trySetValue(Property<T> property, V v) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null || comparable.equals(v)) {
            return (S)this;
        }
        Object object = this.neighbours.get(property, v);
        if (object == null) {
            throw new IllegalArgumentException("Cannot set property " + String.valueOf(property) + " to " + String.valueOf(v) + " on " + String.valueOf(this.owner) + ", it is not an allowed value");
        }
        return (S)object;
    }

    public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> map) {
        if (this.neighbours != null) {
            throw new IllegalStateException();
        }
        HashBasedTable hashBasedTable = HashBasedTable.create();
        for (Map.Entry entry : this.values.entrySet()) {
            Property property = (Property)entry.getKey();
            for (Comparable comparable : property.getPossibleValues()) {
                if (comparable.equals(entry.getValue())) continue;
                hashBasedTable.put((Object)property, (Object)comparable, map.get(this.makeNeighbourValues(property, comparable)));
            }
        }
        this.neighbours = hashBasedTable.isEmpty() ? hashBasedTable : ArrayTable.create((Table)hashBasedTable);
    }

    private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> property, Comparable<?> comparable) {
        Reference2ObjectArrayMap reference2ObjectArrayMap = new Reference2ObjectArrayMap(this.values);
        reference2ObjectArrayMap.put(property, comparable);
        return reference2ObjectArrayMap;
    }

    public Map<Property<?>, Comparable<?>> getValues() {
        return this.values;
    }

    protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> codec, Function<O, S> function) {
        return codec.dispatch(NAME_TAG, stateHolder -> stateHolder.owner, object -> {
            StateHolder stateHolder = (StateHolder)function.apply(object);
            if (stateHolder.getValues().isEmpty()) {
                return MapCodec.unit((Object)stateHolder);
            }
            return stateHolder.propertiesCodec.codec().lenientOptionalFieldOf(PROPERTIES_TAG).xmap(optional -> optional.orElse(stateHolder), Optional::of);
        });
    }
}

