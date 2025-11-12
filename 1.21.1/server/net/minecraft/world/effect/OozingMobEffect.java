/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.world.effect;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

class OozingMobEffect
extends MobEffect {
    private static final int RADIUS_TO_CHECK_SLIMES = 2;
    public static final int SLIME_SIZE = 2;
    private final ToIntFunction<RandomSource> spawnedCount;

    protected OozingMobEffect(MobEffectCategory mobEffectCategory, int n, ToIntFunction<RandomSource> toIntFunction) {
        super(mobEffectCategory, n, ParticleTypes.ITEM_SLIME);
        this.spawnedCount = toIntFunction;
    }

    @VisibleForTesting
    protected static int numberOfSlimesToSpawn(int n, NearbySlimes nearbySlimes, int n2) {
        if (n < 1) {
            return n2;
        }
        return Mth.clamp(0, n - nearbySlimes.count(n), n2);
    }

    @Override
    public void onMobRemoved(LivingEntity livingEntity, int n, Entity.RemovalReason removalReason) {
        if (removalReason != Entity.RemovalReason.KILLED) {
            return;
        }
        int n2 = this.spawnedCount.applyAsInt(livingEntity.getRandom());
        Level level = livingEntity.level();
        int n3 = level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
        int n4 = OozingMobEffect.numberOfSlimesToSpawn(n3, NearbySlimes.closeTo(livingEntity), n2);
        for (int i = 0; i < n4; ++i) {
            this.spawnSlimeOffspring(livingEntity.level(), livingEntity.getX(), livingEntity.getY() + 0.5, livingEntity.getZ());
        }
    }

    private void spawnSlimeOffspring(Level level, double d, double d2, double d3) {
        Slime slime = EntityType.SLIME.create(level);
        if (slime == null) {
            return;
        }
        slime.setSize(2, true);
        slime.moveTo(d, d2, d3, level.getRandom().nextFloat() * 360.0f, 0.0f);
        level.addFreshEntity(slime);
    }

    @FunctionalInterface
    protected static interface NearbySlimes {
        public int count(int var1);

        public static NearbySlimes closeTo(LivingEntity livingEntity) {
            return n -> {
                ArrayList arrayList = new ArrayList();
                livingEntity.level().getEntities(EntityType.SLIME, livingEntity.getBoundingBox().inflate(2.0), slime -> slime != livingEntity, arrayList, n);
                return arrayList.size();
            };
        }
    }
}

