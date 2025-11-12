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

public class UniformInt
extends IntProvider {
    public static final MapCodec<UniformInt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("min_inclusive").forGetter(uniformInt -> uniformInt.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter(uniformInt -> uniformInt.maxInclusive)).apply((Applicative)instance, UniformInt::new)).validate(uniformInt -> {
        if (uniformInt.maxInclusive < uniformInt.minInclusive) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + uniformInt.minInclusive + ", max_inclusive: " + uniformInt.maxInclusive);
        }
        return DataResult.success((Object)uniformInt);
    });
    private final int minInclusive;
    private final int maxInclusive;

    private UniformInt(int n, int n2) {
        this.minInclusive = n;
        this.maxInclusive = n2;
    }

    public static UniformInt of(int n, int n2) {
        return new UniformInt(n, n2);
    }

    @Override
    public int sample(RandomSource randomSource) {
        return Mth.randomBetweenInclusive(randomSource, this.minInclusive, this.maxInclusive);
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
        return IntProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

