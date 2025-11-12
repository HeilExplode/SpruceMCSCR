/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;

public class WeightedRandom {
    private WeightedRandom() {
    }

    public static int getTotalWeight(List<? extends WeightedEntry> list) {
        long l = 0L;
        for (WeightedEntry weightedEntry : list) {
            l += (long)weightedEntry.getWeight().asInt();
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)l;
    }

    public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomSource, List<T> list, int n) {
        if (n < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if (n == 0) {
            return Optional.empty();
        }
        int n2 = randomSource.nextInt(n);
        return WeightedRandom.getWeightedItem(list, n2);
    }

    public static <T extends WeightedEntry> Optional<T> getWeightedItem(List<T> list, int n) {
        for (WeightedEntry weightedEntry : list) {
            if ((n -= weightedEntry.getWeight().asInt()) >= 0) continue;
            return Optional.of(weightedEntry);
        }
        return Optional.empty();
    }

    public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomSource, List<T> list) {
        return WeightedRandom.getRandomItem(randomSource, list, WeightedRandom.getTotalWeight(list));
    }
}

