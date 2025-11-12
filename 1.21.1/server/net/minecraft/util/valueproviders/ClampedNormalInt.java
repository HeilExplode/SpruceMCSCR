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

public class ClampedNormalInt
extends IntProvider {
    public static final MapCodec<ClampedNormalInt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("mean").forGetter(clampedNormalInt -> Float.valueOf(clampedNormalInt.mean)), (App)Codec.FLOAT.fieldOf("deviation").forGetter(clampedNormalInt -> Float.valueOf(clampedNormalInt.deviation)), (App)Codec.INT.fieldOf("min_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.maxInclusive)).apply((Applicative)instance, ClampedNormalInt::new)).validate(clampedNormalInt -> {
        if (clampedNormalInt.maxInclusive < clampedNormalInt.minInclusive) {
            return DataResult.error(() -> "Max must be larger than min: [" + clampedNormalInt.minInclusive + ", " + clampedNormalInt.maxInclusive + "]");
        }
        return DataResult.success((Object)clampedNormalInt);
    });
    private final float mean;
    private final float deviation;
    private final int minInclusive;
    private final int maxInclusive;

    public static ClampedNormalInt of(float f, float f2, int n, int n2) {
        return new ClampedNormalInt(f, f2, n, n2);
    }

    private ClampedNormalInt(float f, float f2, int n, int n2) {
        this.mean = f;
        this.deviation = f2;
        this.minInclusive = n;
        this.maxInclusive = n2;
    }

    @Override
    public int sample(RandomSource randomSource) {
        return ClampedNormalInt.sample(randomSource, this.mean, this.deviation, this.minInclusive, this.maxInclusive);
    }

    public static int sample(RandomSource randomSource, float f, float f2, float f3, float f4) {
        return (int)Mth.clamp(Mth.normal(randomSource, f, f2), f3, f4);
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
        return IntProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

