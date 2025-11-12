/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class BiasedToBottomInt
extends IntProvider {
    public static final MapCodec<BiasedToBottomInt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("min_inclusive").forGetter(biasedToBottomInt -> biasedToBottomInt.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter(biasedToBottomInt -> biasedToBottomInt.maxInclusive)).apply((Applicative)instance, BiasedToBottomInt::new)).validate(biasedToBottomInt -> {
        if (biasedToBottomInt.maxInclusive < biasedToBottomInt.minInclusive) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + biasedToBottomInt.minInclusive + ", max_inclusive: " + biasedToBottomInt.maxInclusive);
        }
        return DataResult.success((Object)biasedToBottomInt);
    });
    private final int minInclusive;
    private final int maxInclusive;

    private BiasedToBottomInt(int n, int n2) {
        this.minInclusive = n;
        this.maxInclusive = n2;
    }

    public static BiasedToBottomInt of(int n, int n2) {
        return new BiasedToBottomInt(n, n2);
    }

    @Override
    public int sample(RandomSource randomSource) {
        return this.minInclusive + randomSource.nextInt(randomSource.nextInt(this.maxInclusive - this.minInclusive + 1) + 1);
    }

    @Override
    public int getMinValue() {
        return this.minInclusive;
    }

    @Override
    public int getMaxValue() {
        return this.maxInclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.BIASED_TO_BOTTOM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

