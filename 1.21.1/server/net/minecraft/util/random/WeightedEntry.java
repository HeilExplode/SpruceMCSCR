/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.random;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;

public interface WeightedEntry {
    public Weight getWeight();

    public static <T> Wrapper<T> wrap(T t, int n) {
        return new Wrapper<T>(t, Weight.of(n));
    }

    public record Wrapper<T>(T data, Weight weight) implements WeightedEntry
    {
        @Override
        public Weight getWeight() {
            return this.weight;
        }

        public static <E> Codec<Wrapper<E>> codec(Codec<E> codec) {
            return RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("data").forGetter(Wrapper::data), (App)Weight.CODEC.fieldOf("weight").forGetter(Wrapper::weight)).apply((Applicative)instance, Wrapper::new));
        }
    }

    public static class IntrusiveBase
    implements WeightedEntry {
        private final Weight weight;

        public IntrusiveBase(int n) {
            this.weight = Weight.of(n);
        }

        public IntrusiveBase(Weight weight) {
            this.weight = weight;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }
}

