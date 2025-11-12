/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public record StatePropertiesPredicate(List<PropertyMatcher> properties) {
    private static final Codec<List<PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap((Codec)Codec.STRING, ValueMatcher.CODEC).xmap(map -> map.entrySet().stream().map(entry -> new PropertyMatcher((String)entry.getKey(), (ValueMatcher)entry.getValue())).toList(), list -> list.stream().collect(Collectors.toMap(PropertyMatcher::name, PropertyMatcher::valueMatcher)));
    public static final Codec<StatePropertiesPredicate> CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
    public static final StreamCodec<ByteBuf, StatePropertiesPredicate> STREAM_CODEC = PropertyMatcher.STREAM_CODEC.apply(ByteBufCodecs.list()).map(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);

    public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> stateDefinition, S s) {
        for (PropertyMatcher propertyMatcher : this.properties) {
            if (propertyMatcher.match(stateDefinition, s)) continue;
            return false;
        }
        return true;
    }

    public boolean matches(BlockState blockState) {
        return this.matches(blockState.getBlock().getStateDefinition(), blockState);
    }

    public boolean matches(FluidState fluidState) {
        return this.matches(fluidState.getType().getStateDefinition(), fluidState);
    }

    public Optional<String> checkState(StateDefinition<?, ?> stateDefinition) {
        for (PropertyMatcher propertyMatcher : this.properties) {
            Optional<String> optional = propertyMatcher.checkState(stateDefinition);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    record PropertyMatcher(String name, ValueMatcher valueMatcher) {
        public static final StreamCodec<ByteBuf, PropertyMatcher> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PropertyMatcher::name, ValueMatcher.STREAM_CODEC, PropertyMatcher::valueMatcher, PropertyMatcher::new);

        public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> stateDefinition, S s) {
            Property<?> property = stateDefinition.getProperty(this.name);
            return property != null && this.valueMatcher.match(s, property);
        }

        public Optional<String> checkState(StateDefinition<?, ?> stateDefinition) {
            Property<?> property = stateDefinition.getProperty(this.name);
            return property != null ? Optional.empty() : Optional.of(this.name);
        }
    }

    static interface ValueMatcher {
        public static final Codec<ValueMatcher> CODEC = Codec.either(ExactMatcher.CODEC, RangedMatcher.CODEC).xmap(Either::unwrap, valueMatcher -> {
            if (valueMatcher instanceof ExactMatcher) {
                ExactMatcher exactMatcher = (ExactMatcher)valueMatcher;
                return Either.left((Object)exactMatcher);
            }
            if (valueMatcher instanceof RangedMatcher) {
                RangedMatcher rangedMatcher = (RangedMatcher)valueMatcher;
                return Either.right((Object)rangedMatcher);
            }
            throw new UnsupportedOperationException();
        });
        public static final StreamCodec<ByteBuf, ValueMatcher> STREAM_CODEC = ByteBufCodecs.either(ExactMatcher.STREAM_CODEC, RangedMatcher.STREAM_CODEC).map(Either::unwrap, valueMatcher -> {
            if (valueMatcher instanceof ExactMatcher) {
                ExactMatcher exactMatcher = (ExactMatcher)valueMatcher;
                return Either.left((Object)exactMatcher);
            }
            if (valueMatcher instanceof RangedMatcher) {
                RangedMatcher rangedMatcher = (RangedMatcher)valueMatcher;
                return Either.right((Object)rangedMatcher);
            }
            throw new UnsupportedOperationException();
        });

        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> var1, Property<T> var2);
    }

    public static class Builder {
        private final ImmutableList.Builder<PropertyMatcher> matchers = ImmutableList.builder();

        private Builder() {
        }

        public static Builder properties() {
            return new Builder();
        }

        public Builder hasProperty(Property<?> property, String string) {
            this.matchers.add((Object)new PropertyMatcher(property.getName(), new ExactMatcher(string)));
            return this;
        }

        public Builder hasProperty(Property<Integer> property, int n) {
            return this.hasProperty((Property)property, (Comparable<T> & StringRepresentable)Integer.toString(n));
        }

        public Builder hasProperty(Property<Boolean> property, boolean bl) {
            return this.hasProperty((Property)property, (Comparable<T> & StringRepresentable)Boolean.toString(bl));
        }

        public <T extends Comparable<T> & StringRepresentable> Builder hasProperty(Property<T> property, T t) {
            return this.hasProperty(property, (T)((StringRepresentable)t).getSerializedName());
        }

        public Optional<StatePropertiesPredicate> build() {
            return Optional.of(new StatePropertiesPredicate((List<PropertyMatcher>)this.matchers.build()));
        }
    }

    record RangedMatcher(Optional<String> minValue, Optional<String> maxValue) implements ValueMatcher
    {
        public static final Codec<RangedMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.optionalFieldOf("min").forGetter(RangedMatcher::minValue), (App)Codec.STRING.optionalFieldOf("max").forGetter(RangedMatcher::maxValue)).apply((Applicative)instance, RangedMatcher::new));
        public static final StreamCodec<ByteBuf, RangedMatcher> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), RangedMatcher::minValue, ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), RangedMatcher::maxValue, RangedMatcher::new);

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property) {
            Optional<T> optional;
            Comparable comparable = stateHolder.getValue(property);
            if (this.minValue.isPresent() && ((optional = property.getValue(this.minValue.get())).isEmpty() || comparable.compareTo((Comparable)((Comparable)optional.get())) < 0)) {
                return false;
            }
            return !this.maxValue.isPresent() || !(optional = property.getValue(this.maxValue.get())).isEmpty() && comparable.compareTo((Comparable)((Comparable)optional.get())) <= 0;
        }
    }

    record ExactMatcher(String value) implements ValueMatcher
    {
        public static final Codec<ExactMatcher> CODEC = Codec.STRING.xmap(ExactMatcher::new, ExactMatcher::value);
        public static final StreamCodec<ByteBuf, ExactMatcher> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ExactMatcher::new, ExactMatcher::value);

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property) {
            Comparable comparable = stateHolder.getValue(property);
            Optional<T> optional = property.getValue(this.value);
            return optional.isPresent() && comparable.compareTo((Comparable)((Comparable)optional.get())) == 0;
        }
    }
}

