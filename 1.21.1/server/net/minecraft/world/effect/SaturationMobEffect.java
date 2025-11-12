/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class SaturationMobEffect
extends InstantenousMobEffect {
    protected SaturationMobEffect(MobEffectCategory mobEffectCategory, int n) {
        super(mobEffectCategory, n);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int n) {
        if (!livingEntity.level().isClientSide && livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            player.getFoodData().eat(n + 1, 1.0f);
        }
        return true;
    }
}

