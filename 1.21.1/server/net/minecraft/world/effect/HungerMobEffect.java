/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class HungerMobEffect
extends MobEffect {
    protected HungerMobEffect(MobEffectCategory mobEffectCategory, int n) {
        super(mobEffectCategory, n);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int n) {
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            player.causeFoodExhaustion(0.005f * (float)(n + 1));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int n, int n2) {
        return true;
    }
}

