/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class WeightedListInt
extends IntProvider {
    public static final MapCodec<WeightedListInt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SimpleWeightedRandomList.wrappedCodec(IntProvider.CODEC).fieldOf("distribution").forGetter(weightedListInt -> weightedListInt.distribution)).apply((Applicative)instance, WeightedListInt::new));
    private final SimpleWeightedRandomList<IntProvider> distribution;
    private final int minValue;
    private final int maxValue;

    public WeightedListInt(SimpleWeightedRandomList<IntProvider> simpleWeightedRandomList) {
        this.distribution = simpleWeightedRandomList;
        List list = simpleWeightedRandomList.unwrap();
        int n = Integer.MAX_VALUE;
        int n2 = Integer.MIN_VALUE;
        for (WeightedEntry.Wrapper wrapper : list) {
            int n3 = ((IntProvider)wrapper.data()).getMinValue();
            int n4 = ((IntProvider)wrapper.data()).getMaxValue();
            n = Math.min(n, n3);
            n2 = Math.max(n2, n4);
        }
        this.minValue = n;
        this.maxValue = n2;
    }

    @Override
    public int sample(RandomSource randomSource) {
        return this.distribution.getRandomValue(randomSource).orElseThrow(IllegalStateException::new).sample(randomSource);
    }

    @Override
    public int getMinValue() {
        return this.minValue;
    }

    @Override
    public int getMaxValue() {
        return this.maxValue;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.WEIGHTED_LIST;
    }
}

