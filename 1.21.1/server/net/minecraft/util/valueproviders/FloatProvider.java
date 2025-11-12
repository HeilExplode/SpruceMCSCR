/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.SampledFloat;

public abstract class FloatProvider
implements SampledFloat {
    private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either((Codec)Codec.FLOAT, (Codec)BuiltInRegistries.FLOAT_PROVIDER_TYPE.byNameCodec().dispatch(FloatProvider::getType, FloatProviderType::codec));
    public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(either -> (FloatProvider)either.map(ConstantFloat::of, floatProvider -> floatProvider), floatProvider -> floatProvider.getType() == FloatProviderType.CONSTANT ? Either.left((Object)Float.valueOf(((ConstantFloat)floatProvider).getValue())) : Either.right((Object)floatProvider));

    public static Codec<FloatProvider> codec(float f, float f2) {
        return CODEC.validate(floatProvider -> {
            if (floatProvider.getMinValue() < f) {
                return DataResult.error(() -> "Value provider too low: " + f + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]");
            }
            if (floatProvider.getMaxValue() > f2) {
                return DataResult.error(() -> "Value provider too high: " + f2 + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]");
            }
            return DataResult.success((Object)floatProvider);
        });
    }

    public abstract float getMinValue();

    public abstract float getMaxValue();

    public abstract FloatProviderType<?> getType();
}

