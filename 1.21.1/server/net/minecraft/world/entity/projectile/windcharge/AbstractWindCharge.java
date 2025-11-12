/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractWindCharge
extends AbstractHurtingProjectile
implements ItemSupplier {
    public static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(true, false, Optional.empty(), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()));
    public static final double JUMP_SCALE = 0.25;

    public AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
        super((EntityType<? extends AbstractHurtingProjectile>)entityType, level);
        this.accelerationPower = 0.0;
    }

    public AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level, Entity entity, double d, double d2, double d3) {
        super(entityType, d, d2, d3, level);
        this.setOwner(entity);
        this.accelerationPower = 0.0;
    }

    AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, double d, double d2, double d3, Vec3 vec3, Level level) {
        super(entityType, d, d2, d3, vec3, level);
        this.accelerationPower = 0.0;
    }

    @Override
    protected AABB makeBoundingBox() {
        float f = this.getType().getDimensions().width() / 2.0f;
        float f2 = this.getType().getDimensions().height();
        float f3 = 0.15f;
        return new AABB(this.position().x - (double)f, this.position().y - (double)0.15f, this.position().z - (double)f, this.position().x + (double)f, this.position().y - (double)0.15f + (double)f2, this.position().z + (double)f);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        if (entity instanceof AbstractWindCharge) {
            return false;
        }
        return super.canCollideWith(entity);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof AbstractWindCharge) {
            return false;
        }
        if (entity.getType() == EntityType.END_CRYSTAL) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        LivingEntity livingEntity;
        Entity entity;
        super.onHitEntity(entityHitResult);
        if (this.level().isClientSide) {
            return;
        }
        Object object = this.getOwner();
        if (object instanceof LivingEntity) {
            entity = (LivingEntity)object;
            livingEntity = entity;
        } else {
            livingEntity = null;
        }
        LivingEntity livingEntity2 = livingEntity;
        entity = entityHitResult.getEntity();
        if (livingEntity2 != null) {
            livingEntity2.setLastHurtMob(entity);
        }
        if (entity.hurt((DamageSource)(object = this.damageSources().windCharge(this, livingEntity2)), 1.0f) && entity instanceof LivingEntity) {
            LivingEntity livingEntity3 = (LivingEntity)entity;
            EnchantmentHelper.doPostAttackEffects((ServerLevel)this.level(), livingEntity3, (DamageSource)object);
        }
        this.explode(this.position());
    }

    @Override
    public void push(double d, double d2, double d3) {
    }

    protected abstract void explode(Vec3 var1);

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (!this.level().isClientSide) {
            Vec3i vec3i = blockHitResult.getDirection().getNormal();
            Vec3 vec3 = Vec3.atLowerCornerOf(vec3i).multiply(0.25, 0.25, 0.25);
            Vec3 vec32 = blockHitResult.getLocation().add(vec3);
            this.explode(vec32);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getInertia() {
        return 1.0f;
    }

    @Override
    protected float getLiquidInertia() {
        return this.getInertia();
    }

    @Override
    @Nullable
    protected ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.getBlockY() > this.level().getMaxBuildHeight() + 30) {
            this.explode(this.position());
            this.discard();
        } else {
            super.tick();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        return false;
    }
}

