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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class ClampedInt
extends IntProvider {
    public static final MapCodec<ClampedInt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)IntProvider.CODEC.fieldOf("source").forGetter(clampedInt -> clampedInt.source), (App)Codec.INT.fieldOf("min_inclusive").forGetter(clampedInt -> clampedInt.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter(clampedInt -> clampedInt.maxInclusive)).apply((Applicative)instance, ClampedInt::new)).validate(clampedInt -> {
        if (clampedInt.maxInclusive < clampedInt.minInclusive) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + clampedInt.minInclusive + ", max_inclusive: " + clampedInt.maxInclusive);
        }
        return DataResult.success((Object)clampedInt);
    });
    private final IntProvider source;
    private final int minInclusive;
    private final int maxInclusive;

    public static ClampedInt of(IntProvider intProvider, int n, int n2) {
        return new ClampedInt(intProvider, n, n2);
    }

    public ClampedInt(IntProvider intProvider, int n, int n2) {
        this.source = intProvider;
        this.minInclusive = n;
        this.maxInclusive = n2;
    }

    @Override
    public int sample(RandomSource randomSource) {
        return Mth.clamp(this.source.sample(randomSource), this.minInclusive, this.maxInclusive);
    }

    @Override
    public int getMinValue() {
        return Math.max(this.minInclusive, this.source.getMinValue());
    }

    @Override
    public int getMaxValue() {
        return Math.min(this.maxInclusive, this.source.getMaxValue());
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CLAMPED;
    }
}

