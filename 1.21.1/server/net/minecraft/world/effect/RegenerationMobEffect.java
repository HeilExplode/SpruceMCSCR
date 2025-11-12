/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

class RegenerationMobEffect
extends MobEffect {
    protected RegenerationMobEffect(MobEffectCategory mobEffectCategory, int n) {
        super(mobEffectCategory, n);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int n) {
        if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
            livingEntity.heal(1.0f);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int n, int n2) {
        int n3 = 50 >> n2;
        if (n3 > 0) {
            return n % n3 == 0;
        }
        return true;
    }
}

