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

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect
{
    public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomial::chance)).apply((Applicative)instance, RemoveBinomial::new));

    @Override
    public float process(int n, RandomSource randomSource, float f) {
        float f2 = this.chance.calculate(n);
        int n2 = 0;
        int n3 = 0;
        while ((float)n3 < f) {
            if (randomSource.nextFloat() < f2) {
                ++n2;
            }
            ++n3;
        }
        return f - (float)n2;
    }

    public MapCodec<RemoveBinomial> codec() {
        return CODEC;
    }
}

