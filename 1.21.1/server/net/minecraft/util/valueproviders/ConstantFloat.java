/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class ConstantFloat
extends FloatProvider {
    public static final ConstantFloat ZERO = new ConstantFloat(0.0f);
    public static final MapCodec<ConstantFloat> CODEC = Codec.FLOAT.fieldOf("value").xmap(ConstantFloat::of, ConstantFloat::getValue);
    private final float value;

    public static ConstantFloat of(float f) {
        if (f == 0.0f) {
            return ZERO;
        }
        return new ConstantFloat(f);
    }

    private ConstantFloat(float f) {
        this.value = f;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public float sample(RandomSource randomSource) {
        return this.value;
    }

    @Override
    public float getMinValue() {
        return this.value;
    }

    @Override
    public float getMaxValue() {
        return this.value;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CONSTANT;
    }

    public String toString() {
        return Float.toString(this.value);
    }
}

