/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WitherSkull
extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WitherSkull.class, EntityDataSerializers.BOOLEAN);

    public WitherSkull(EntityType<? extends WitherSkull> entityType, Level level) {
        super((EntityType<? extends AbstractHurtingProjectile>)entityType, level);
    }

    public WitherSkull(Level level, LivingEntity livingEntity, Vec3 vec3) {
        super(EntityType.WITHER_SKULL, livingEntity, vec3, level);
    }

    @Override
    protected float getInertia() {
        return this.isDangerous() ? 0.73f : super.getInertia();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f) {
        if (this.isDangerous() && WitherBoss.canDestroy(blockState)) {
            return Math.min(0.8f, f);
        }
        return f;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        boolean bl;
        LivingEntity livingEntity;
        super.onHitEntity(entityHitResult);
        Object object = this.level();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = entityHitResult.getEntity();
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            DamageSource damageSource = this.damageSources().witherSkull(this, livingEntity);
            bl = ((Entity)object).hurt(damageSource, 8.0f);
            if (bl) {
                if (((Entity)object).isAlive()) {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, (Entity)object, damageSource);
                } else {
                    livingEntity.heal(5.0f);
                }
            }
        } else {
            bl = ((Entity)object).hurt(this.damageSources().magic(), 5.0f);
        }
        if (bl && object instanceof LivingEntity) {
            livingEntity = (LivingEntity)object;
            int n = 0;
            if (this.level().getDifficulty() == Difficulty.NORMAL) {
                n = 10;
            } else if (this.level().getDifficulty() == Difficulty.HARD) {
                n = 40;
            }
            if (n > 0) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * n, 1), this.getEffectSource());
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), 1.0f, false, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    public void setDangerous(boolean bl) {
        this.entityData.set(DATA_DANGEROUS, bl);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("dangerous", this.isDangerous());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setDangerous(compoundTag.getBoolean("dangerous"));
    }
}

