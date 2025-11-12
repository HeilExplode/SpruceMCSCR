/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class MagmaCube
extends Slime {
    public MagmaCube(EntityType<? extends MagmaCube> entityType, Level level) {
        super((EntityType<? extends Slime>)entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.2f);
    }

    public static boolean checkMagmaCubeSpawnRules(EntityType<MagmaCube> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override
    public void setSize(int n, boolean bl) {
        super.setSize(n, bl);
        this.getAttribute(Attributes.ARMOR).setBaseValue(n * 3);
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return ParticleTypes.FLAME;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected int getJumpDelay() {
        return super.getJumpDelay() * 4;
    }

    @Override
    protected void decreaseSquish() {
        this.targetSquish *= 0.9f;
    }

    @Override
    public void jumpFromGround() {
        Vec3 vec3 = this.getDeltaMovement();
        float f = (float)this.getSize() * 0.1f;
        this.setDeltaMovement(vec3.x, this.getJumpPower() + f, vec3.z);
        this.hasImpulse = true;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> tagKey) {
        if (tagKey == FluidTags.LAVA) {
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.x, 0.22f + (float)this.getSize() * 0.05f, vec3.z);
            this.hasImpulse = true;
        } else {
            super.jumpInLiquid(tagKey);
        }
    }

    @Override
    protected boolean isDealsDamage() {
        return this.isEffectiveAi();
    }

    @Override
    protected float getAttackDamage() {
        return super.getAttackDamage() + 2.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.isTiny()) {
            return SoundEvents.MAGMA_CUBE_HURT_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isTiny()) {
            return SoundEvents.MAGMA_CUBE_DEATH_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound() {
        if (this.isTiny()) {
            return SoundEvents.MAGMA_CUBE_SQUISH_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_SQUISH;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return SoundEvents.MAGMA_CUBE_JUMP;
    }
}

