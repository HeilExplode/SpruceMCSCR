/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

public class ShufflingList<U>
implements Iterable<U> {
    protected final List<WeightedEntry<U>> entries;
    private final RandomSource random = RandomSource.create();

    public ShufflingList() {
        this.entries = Lists.newArrayList();
    }

    private ShufflingList(List<WeightedEntry<U>> list) {
        this.entries = Lists.newArrayList(list);
    }

    public static <U> Codec<ShufflingList<U>> codec(Codec<U> codec) {
        return WeightedEntry.codec(codec).listOf().xmap(ShufflingList::new, shufflingList -> shufflingList.entries);
    }

    public ShufflingList<U> add(U u, int n) {
        this.entries.add(new WeightedEntry<U>(u, n));
        return this;
    }

    public ShufflingList<U> shuffle() {
        this.entries.forEach(weightedEntry -> weightedEntry.setRandom(this.random.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(WeightedEntry::getRandWeight));
        return this;
    }

    public Stream<U> stream() {
        return this.entries.stream().map(WeightedEntry::getData);
    }

    @Override
    public Iterator<U> iterator() {
        return Iterators.transform(this.entries.iterator(), WeightedEntry::getData);
    }

    public String toString() {
        return "ShufflingList[" + String.valueOf(this.entries) + "]";
    }

    public static class WeightedEntry<T> {
        final T data;
        final int weight;
        private double randWeight;

        WeightedEntry(T t, int n) {
            this.weight = n;
            this.data = t;
        }

        private double getRandWeight() {
            return this.randWeight;
        }

        void setRandom(float f) {
            this.randWeight = -Math.pow(f, 1.0f / (float)this.weight);
        }

        public T getData() {
            return this.data;
        }

        public int getWeight() {
            return this.weight;
        }

        public String toString() {
            return this.weight + ":" + String.valueOf(this.data);
        }

        public static <E> Codec<WeightedEntry<E>> codec(final Codec<E> codec) {
            return new Codec<WeightedEntry<E>>(){

                public <T> DataResult<Pair<WeightedEntry<E>, T>> decode(DynamicOps<T> dynamicOps, T t) {
                    Dynamic dynamic = new Dynamic(dynamicOps, t);
                    return dynamic.get("data").flatMap(arg_0 -> ((Codec)codec).parse(arg_0)).map(object -> new WeightedEntry<Object>(object, dynamic.get("weight").asInt(1))).map(weightedEntry -> Pair.of((Object)weightedEntry, (Object)dynamicOps.empty()));
                }

                public <T> DataResult<T> encode(WeightedEntry<E> weightedEntry, DynamicOps<T> dynamicOps, T t) {
                    return dynamicOps.mapBuilder().add("weight", dynamicOps.createInt(weightedEntry.weight)).add("data", codec.encodeStart(dynamicOps, weightedEntry.data)).build(t);
                }

                public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                    return this.encode((WeightedEntry)object, dynamicOps, object2);
                }
            };
        }
    }
}

