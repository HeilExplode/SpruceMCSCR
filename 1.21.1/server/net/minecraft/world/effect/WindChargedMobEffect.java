/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;

class WindChargedMobEffect
extends MobEffect {
    protected WindChargedMobEffect(MobEffectCategory mobEffectCategory, int n) {
        super(mobEffectCategory, n, ParticleTypes.SMALL_GUST);
    }

    @Override
    public void onMobRemoved(LivingEntity livingEntity, int n, Entity.RemovalReason removalReason) {
        Level level;
        if (removalReason == Entity.RemovalReason.KILLED && (level = livingEntity.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            double d = livingEntity.getX();
            double d2 = livingEntity.getY() + (double)(livingEntity.getBbHeight() / 2.0f);
            double d3 = livingEntity.getZ();
            float f = 3.0f + livingEntity.getRandom().nextFloat() * 2.0f;
            serverLevel.explode(livingEntity, null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR, d, d2, d3, f, false, Level.ExplosionInteraction.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.BREEZE_WIND_CHARGE_BURST);
        }
    }
}

