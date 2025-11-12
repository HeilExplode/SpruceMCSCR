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

public class UniformFloat
extends FloatProvider {
    public static final MapCodec<UniformFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("min_inclusive").forGetter(uniformFloat -> Float.valueOf(uniformFloat.minInclusive)), (App)Codec.FLOAT.fieldOf("max_exclusive").forGetter(uniformFloat -> Float.valueOf(uniformFloat.maxExclusive))).apply((Applicative)instance, UniformFloat::new)).validate(uniformFloat -> {
        if (uniformFloat.maxExclusive <= uniformFloat.minInclusive) {
            return DataResult.error(() -> "Max must be larger than min, min_inclusive: " + uniformFloat.minInclusive + ", max_exclusive: " + uniformFloat.maxExclusive);
        }
        return DataResult.success((Object)uniformFloat);
    });
    private final float minInclusive;
    private final float maxExclusive;

    private UniformFloat(float f, float f2) {
        this.minInclusive = f;
        this.maxExclusive = f2;
    }

    public static UniformFloat of(float f, float f2) {
        if (f2 <= f) {
            throw new IllegalArgumentException("Max must exceed min");
        }
        return new UniformFloat(f, f2);
    }

    @Override
    public float sample(RandomSource randomSource) {
        return Mth.randomBetween(randomSource, this.minInclusive, this.maxExclusive);
    }

    @Override
    public float getMinValue() {
        return this.minInclusive;
    }

    @Override
    public float getMaxValue() {
        return this.maxExclusive;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
    }
}

