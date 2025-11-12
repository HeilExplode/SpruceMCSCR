/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.StateHolder;

public abstract class Property<T extends Comparable<T>> {
    private final Class<T> clazz;
    private final String name;
    @Nullable
    private Integer hashCode;
    private final Codec<T> codec = Codec.STRING.comapFlatMap(string -> this.getValue((String)string).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unable to read property: " + String.valueOf(this) + " with value: " + string)), this::getName);
    private final Codec<Value<T>> valueCodec = this.codec.xmap(this::value, Value::value);

    protected Property(String string2, Class<T> clazz) {
        this.clazz = clazz;
        this.name = string2;
    }

    public Value<T> value(T t) {
        return new Value<T>(this, t);
    }

    public Value<T> value(StateHolder<?, ?> stateHolder) {
        return new Value(this, stateHolder.getValue(this));
    }

    public Stream<Value<T>> getAllValues() {
        return this.getPossibleValues().stream().map(this::value);
    }

    public Codec<T> codec() {
        return this.codec;
    }

    public Codec<Value<T>> valueCodec() {
        return this.valueCodec;
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getValueClass() {
        return this.clazz;
    }

    public abstract Collection<T> getPossibleValues();

    public abstract String getName(T var1);

    public abstract Optional<T> getValue(String var1);

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("name", (Object)this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Property) {
            Property property = (Property)object;
            return this.clazz.equals(property.clazz) && this.name.equals(property.name);
        }
        return false;
    }

    public final int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = this.generateHashCode();
        }
        return this.hashCode;
    }

    public int generateHashCode() {
        return 31 * this.clazz.hashCode() + this.name.hashCode();
    }

    public <U, S extends StateHolder<?, S>> DataResult<S> parseValue(DynamicOps<U> dynamicOps, S s, U u) {
        DataResult dataResult = this.codec.parse(dynamicOps, u);
        return dataResult.map(comparable -> (StateHolder)s.setValue(this, comparable)).setPartial(s);
    }

    public record Value<T extends Comparable<T>>(Property<T> property, T value) {
        public Value {
            if (!property.getPossibleValues().contains(t)) {
                throw new IllegalArgumentException("Value " + String.valueOf(t) + " does not belong to property " + String.valueOf(property));
            }
        }

        @Override
        public String toString() {
            return this.property.getName() + "=" + this.property.getName(this.value);
        }
    }
}

