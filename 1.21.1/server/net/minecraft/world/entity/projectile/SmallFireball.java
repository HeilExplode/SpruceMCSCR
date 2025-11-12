/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SmallFireball
extends Fireball {
    public SmallFireball(EntityType<? extends SmallFireball> entityType, Level level) {
        super((EntityType<? extends Fireball>)entityType, level);
    }

    public SmallFireball(Level level, LivingEntity livingEntity, Vec3 vec3) {
        super((EntityType<? extends Fireball>)EntityType.SMALL_FIREBALL, livingEntity, vec3, level);
    }

    public SmallFireball(Level level, double d, double d2, double d3, Vec3 vec3) {
        super((EntityType<? extends Fireball>)EntityType.SMALL_FIREBALL, d, d2, d3, vec3, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Object object = this.level();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = entityHitResult.getEntity();
        Entity entity = this.getOwner();
        int n = ((Entity)object).getRemainingFireTicks();
        ((Entity)object).igniteForSeconds(5.0f);
        DamageSource damageSource = this.damageSources().fireball(this, entity);
        if (!((Entity)object).hurt(damageSource, 5.0f)) {
            ((Entity)object).setRemainingFireTicks(n);
        } else {
            EnchantmentHelper.doPostAttackEffects(serverLevel, (Entity)object, damageSource);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level().isClientSide) {
            return;
        }
        Entity entity = this.getOwner();
        if (!(entity instanceof Mob) || this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
            if (this.level().isEmptyBlock(blockPos)) {
                this.level().setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level(), blockPos));
            }
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
    public boolean hurt(DamageSource damageSource, float f) {
        return false;
    }
}

