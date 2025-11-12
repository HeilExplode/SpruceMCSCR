/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record AddValue(LevelBasedValue value) implements EnchantmentValueEffect
{
    public static final MapCodec<AddValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LevelBasedValue.CODEC.fieldOf("value").forGetter(AddValue::value)).apply((Applicative)instance, AddValue::new));

    @Override
    public float process(int n, RandomSource randomSource, float f) {
        return f + this.value.calculate(n);
    }

    public MapCodec<AddValue> codec() {
        return CODEC;
    }
}

