/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.effect;

import javax.annotation.Nullable;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

class HealOrHarmMobEffect
extends InstantenousMobEffect {
    private final boolean isHarm;

    public HealOrHarmMobEffect(MobEffectCategory mobEffectCategory, int n, boolean bl) {
        super(mobEffectCategory, n);
        this.isHarm = bl;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int n) {
        if (this.isHarm == livingEntity.isInvertedHealAndHarm()) {
            livingEntity.heal(Math.max(4 << n, 0));
        } else {
            livingEntity.hurt(livingEntity.damageSources().magic(), 6 << n);
        }
        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int n, double d) {
        if (this.isHarm == livingEntity.isInvertedHealAndHarm()) {
            int n2 = (int)(d * (double)(4 << n) + 0.5);
            livingEntity.heal(n2);
        } else {
            int n3 = (int)(d * (double)(6 << n) + 0.5);
            if (entity == null) {
                livingEntity.hurt(livingEntity.damageSources().magic(), n3);
            } else {
                livingEntity.hurt(livingEntity.damageSources().indirectMagic(entity, entity2), n3);
            }
        }
    }
}

