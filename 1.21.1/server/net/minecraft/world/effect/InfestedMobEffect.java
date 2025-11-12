/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 */
package net.minecraft.world.effect;

import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

class InfestedMobEffect
extends MobEffect {
    private final float chanceToSpawn;
    private final ToIntFunction<RandomSource> spawnedCount;

    protected InfestedMobEffect(MobEffectCategory mobEffectCategory, int n, float f, ToIntFunction<RandomSource> toIntFunction) {
        super(mobEffectCategory, n, ParticleTypes.INFESTED);
        this.chanceToSpawn = f;
        this.spawnedCount = toIntFunction;
    }

    @Override
    public void onMobHurt(LivingEntity livingEntity, int n, DamageSource damageSource, float f) {
        if (livingEntity.getRandom().nextFloat() <= this.chanceToSpawn) {
            int n2 = this.spawnedCount.applyAsInt(livingEntity.getRandom());
            for (int i = 0; i < n2; ++i) {
                this.spawnSilverfish(livingEntity.level(), livingEntity, livingEntity.getX(), livingEntity.getY() + (double)livingEntity.getBbHeight() / 2.0, livingEntity.getZ());
            }
        }
    }

    private void spawnSilverfish(Level level, LivingEntity livingEntity, double d, double d2, double d3) {
        Silverfish silverfish = EntityType.SILVERFISH.create(level);
        if (silverfish == null) {
            return;
        }
        RandomSource randomSource = livingEntity.getRandom();
        float f = 1.5707964f;
        float f2 = Mth.randomBetween(randomSource, -1.5707964f, 1.5707964f);
        Vector3f vector3f = livingEntity.getLookAngle().toVector3f().mul(0.3f).mul(1.0f, 1.5f, 1.0f).rotateY(f2);
        silverfish.moveTo(d, d2, d3, level.getRandom().nextFloat() * 360.0f, 0.0f);
        silverfish.setDeltaMovement(new Vec3(vector3f));
        level.addFreshEntity(silverfish);
        silverfish.playSound(SoundEvents.SILVERFISH_HURT);
    }
}

