/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;

public class WeightedRandomList<E extends WeightedEntry> {
    private final int totalWeight;
    private final ImmutableList<E> items;

    WeightedRandomList(List<? extends E> list) {
        this.items = ImmutableList.copyOf(list);
        this.totalWeight = WeightedRandom.getTotalWeight(list);
    }

    public static <E extends WeightedEntry> WeightedRandomList<E> create() {
        return new WeightedRandomList<E>(ImmutableList.of());
    }

    @SafeVarargs
    public static <E extends WeightedEntry> WeightedRandomList<E> create(E ... EArray) {
        return new WeightedRandomList<E>(ImmutableList.copyOf((Object[])EArray));
    }

    public static <E extends WeightedEntry> WeightedRandomList<E> create(List<E> list) {
        return new WeightedRandomList<E>(list);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public Optional<E> getRandom(RandomSource randomSource) {
        if (this.totalWeight == 0) {
            return Optional.empty();
        }
        int n = randomSource.nextInt(this.totalWeight);
        return WeightedRandom.getWeightedItem(this.items, n);
    }

    public List<E> unwrap() {
        return this.items;
    }

    public static <E extends WeightedEntry> Codec<WeightedRandomList<E>> codec(Codec<E> codec) {
        return codec.listOf().xmap(WeightedRandomList::create, WeightedRandomList::unwrap);
    }

    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        WeightedRandomList weightedRandomList = (WeightedRandomList)object;
        return this.totalWeight == weightedRandomList.totalWeight && Objects.equals(this.items, weightedRandomList.items);
    }

    public int hashCode() {
        return Objects.hash(this.totalWeight, this.items);
    }
}

