/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

class RaidOmenMobEffect
extends MobEffect {
    protected RaidOmenMobEffect(MobEffectCategory mobEffectCategory, int n, ParticleOptions particleOptions) {
        super(mobEffectCategory, n, particleOptions);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int n, int n2) {
        return n == 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int n) {
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            if (!livingEntity.isSpectator()) {
                ServerLevel serverLevel = serverPlayer.serverLevel();
                BlockPos blockPos = serverPlayer.getRaidOmenPosition();
                if (blockPos != null) {
                    serverLevel.getRaids().createOrExtendRaid(serverPlayer, blockPos);
                    serverPlayer.clearRaidOmenPosition();
                    return false;
                }
            }
        }
        return true;
    }
}

