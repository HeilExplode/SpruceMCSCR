/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.serialization.Codec
 */
package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;

public class SimpleWeightedRandomList<E>
extends WeightedRandomList<WeightedEntry.Wrapper<E>> {
    public static <E> Codec<SimpleWeightedRandomList<E>> wrappedCodecAllowingEmpty(Codec<E> codec) {
        return WeightedEntry.Wrapper.codec(codec).listOf().xmap(SimpleWeightedRandomList::new, WeightedRandomList::unwrap);
    }

    public static <E> Codec<SimpleWeightedRandomList<E>> wrappedCodec(Codec<E> codec) {
        return ExtraCodecs.nonEmptyList(WeightedEntry.Wrapper.codec(codec).listOf()).xmap(SimpleWeightedRandomList::new, WeightedRandomList::unwrap);
    }

    SimpleWeightedRandomList(List<? extends WeightedEntry.Wrapper<E>> list) {
        super(list);
    }

    public static <E> Builder<E> builder() {
        return new Builder();
    }

    public static <E> SimpleWeightedRandomList<E> empty() {
        return new SimpleWeightedRandomList<E>(List.of());
    }

    public static <E> SimpleWeightedRandomList<E> single(E e) {
        return new SimpleWeightedRandomList<E>(List.of(WeightedEntry.wrap(e, 1)));
    }

    public Optional<E> getRandomValue(RandomSource randomSource) {
        return this.getRandom(randomSource).map(WeightedEntry.Wrapper::data);
    }

    public static class Builder<E> {
        private final ImmutableList.Builder<WeightedEntry.Wrapper<E>> result = ImmutableList.builder();

        public Builder<E> add(E e) {
            return this.add(e, 1);
        }

        public Builder<E> add(E e, int n) {
            this.result.add(WeightedEntry.wrap(e, n));
            return this;
        }

        public SimpleWeightedRandomList<E> build() {
            return new SimpleWeightedRandomList(this.result.build());
        }
    }
}

