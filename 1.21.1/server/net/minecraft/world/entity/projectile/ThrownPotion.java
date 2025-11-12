/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownPotion
extends ThrowableItemProjectile
implements ItemSupplier {
    public static final double SPLASH_RANGE = 4.0;
    private static final double SPLASH_RANGE_SQ = 16.0;
    public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = livingEntity -> livingEntity.isSensitiveToWater() || livingEntity.isOnFire();

    public ThrownPotion(EntityType<? extends ThrownPotion> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownPotion(Level level, LivingEntity livingEntity) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.POTION, livingEntity, level);
    }

    public ThrownPotion(Level level, double d, double d2, double d3) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.POTION, d, d2, d3, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level().isClientSide) {
            return;
        }
        ItemStack itemStack = this.getItem();
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockPos blockPos2 = blockPos.relative(direction);
        PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (potionContents.is(Potions.WATER)) {
            this.dowseFire(blockPos2);
            this.dowseFire(blockPos2.relative(direction.getOpposite()));
            for (Direction direction2 : Direction.Plane.HORIZONTAL) {
                this.dowseFire(blockPos2.relative(direction2));
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level().isClientSide) {
            return;
        }
        ItemStack itemStack = this.getItem();
        PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (potionContents.is(Potions.WATER)) {
            this.applyWater();
        } else if (potionContents.hasEffects()) {
            if (this.isLingering()) {
                this.makeAreaOfEffectCloud(potionContents);
            } else {
                this.applySplash(potionContents.getAllEffects(), hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)hitResult).getEntity() : null);
            }
        }
        int n = potionContents.potion().isPresent() && potionContents.potion().get().value().hasInstantEffects() ? 2007 : 2002;
        this.level().levelEvent(n, this.blockPosition(), potionContents.getColor());
        this.discard();
    }

    private void applyWater() {
        AABB aABB = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, aABB, WATER_SENSITIVE_OR_ON_FIRE);
        for (LivingEntity object2 : list) {
            double axolotl = this.distanceToSqr(object2);
            if (!(axolotl < 16.0)) continue;
            if (object2.isSensitiveToWater()) {
                object2.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 1.0f);
            }
            if (!object2.isOnFire() || !object2.isAlive()) continue;
            object2.extinguishFire();
        }
        List<Axolotl> list2 = this.level().getEntitiesOfClass(Axolotl.class, aABB);
        Iterator iterator = list2.iterator();
        while (iterator.hasNext()) {
            Axolotl axolotl = (Axolotl)iterator.next();
            axolotl.rehydrate();
        }
    }

    private void applySplash(Iterable<MobEffectInstance> iterable, @Nullable Entity entity) {
        AABB aABB = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, aABB);
        if (!list.isEmpty()) {
            Entity entity2 = this.getEffectSource();
            for (LivingEntity livingEntity : list) {
                double d;
                if (!livingEntity.isAffectedByPotions() || !((d = this.distanceToSqr(livingEntity)) < 16.0)) continue;
                double d2 = livingEntity == entity ? 1.0 : 1.0 - Math.sqrt(d) / 4.0;
                for (MobEffectInstance mobEffectInstance : iterable) {
                    Holder<MobEffect> holder = mobEffectInstance.getEffect();
                    if (holder.value().isInstantenous()) {
                        holder.value().applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance.getAmplifier(), d2);
                        continue;
                    }
                    int n2 = mobEffectInstance.mapDuration(n -> (int)(d2 * (double)n + 0.5));
                    MobEffectInstance mobEffectInstance2 = new MobEffectInstance(holder, n2, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible());
                    if (mobEffectInstance2.endsWithin(20)) continue;
                    livingEntity.addEffect(mobEffectInstance2, entity2);
                }
            }
        }
    }

    private void makeAreaOfEffectCloud(PotionContents potionContents) {
        AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            areaEffectCloud.setOwner(livingEntity);
        }
        areaEffectCloud.setRadius(3.0f);
        areaEffectCloud.setRadiusOnUse(-0.5f);
        areaEffectCloud.setWaitTime(10);
        areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float)areaEffectCloud.getDuration());
        areaEffectCloud.setPotionContents(potionContents);
        this.level().addFreshEntity(areaEffectCloud);
    }

    private boolean isLingering() {
        return this.getItem().is(Items.LINGERING_POTION);
    }

    private void dowseFire(BlockPos blockPos) {
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.is(BlockTags.FIRE)) {
            this.level().destroyBlock(blockPos, false, this);
        } else if (AbstractCandleBlock.isLit(blockState)) {
            AbstractCandleBlock.extinguish(null, blockState, this.level(), blockPos);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            this.level().levelEvent(null, 1009, blockPos, 0);
            CampfireBlock.dowse(this.getOwner(), this.level(), blockPos, blockState);
            this.level().setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(CampfireBlock.LIT, false));
        }
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
        double d = livingEntity.position().x - this.position().x;
        double d2 = livingEntity.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)d2);
    }
}

