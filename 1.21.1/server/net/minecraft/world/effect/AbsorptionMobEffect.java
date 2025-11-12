/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

class AbsorptionMobEffect
extends MobEffect {
    protected AbsorptionMobEffect(MobEffectCategory mobEffectCategory, int n) {
        super(mobEffectCategory, n);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int n) {
        return livingEntity.getAbsorptionAmount() > 0.0f || livingEntity.level().isClientSide;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int n, int n2) {
        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity livingEntity, int n) {
        super.onEffectStarted(livingEntity, n);
        livingEntity.setAbsorptionAmount(Math.max(livingEntity.getAbsorptionAmount(), (float)(4 * (1 + n))));
    }
}

