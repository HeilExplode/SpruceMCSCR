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
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class ClampedNormalFloat
extends FloatProvider {
    public static final MapCodec<ClampedNormalFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("mean").forGetter(clampedNormalFloat -> Float.valueOf(clampedNormalFloat.mean)), (App)Codec.FLOAT.fieldOf("deviation").forGetter(clampedNormalFloat -> Float.valueOf(clampedNormalFloat.deviation)), (App)Codec.FLOAT.fieldOf("min").forGetter(clampedNormalFloat -> Float.valueOf(clampedNormalFloat.min)), (App)Codec.FLOAT.fieldOf("max").forGetter(clampedNormalFloat -> Float.valueOf(clampedNormalFloat.max))).apply((Applicative)instance, ClampedNormalFloat::new)).validate(clampedNormalFloat -> {
        if (clampedNormalFloat.max < clampedNormalFloat.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + clampedNormalFloat.min + ", " + clampedNormalFloat.max + "]");
        }
        return DataResult.success((Object)clampedNormalFloat);
    });
    private final float mean;
    private final float deviation;
    private final float min;
    private final float max;

    public static ClampedNormalFloat of(float f, float f2, float f3, float f4) {
        return new ClampedNormalFloat(f, f2, f3, f4);
    }

    private ClampedNormalFloat(float f, float f2, float f3, float f4) {
        this.mean = f;
        this.deviation = f2;
        this.min = f3;
        this.max = f4;
    }

    @Override
    public float sample(RandomSource randomSource) {
        return ClampedNormalFloat.sample(randomSource, this.mean, this.deviation, this.min, this.max);
    }

    public static float sample(RandomSource randomSource, float f, float f2, float f3, float f4) {
        return Mth.clamp(Mth.normal(randomSource, f, f2), f3, f4);
    }

    @Override
    public float getMinValue() {
        return this.min;
    }

    @Override
    public float getMaxValue() {
        return this.max;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
    }
}

