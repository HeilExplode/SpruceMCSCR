/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LargeFireball
extends Fireball {
    private int explosionPower = 1;

    public LargeFireball(EntityType<? extends LargeFireball> entityType, Level level) {
        super((EntityType<? extends Fireball>)entityType, level);
    }

    public LargeFireball(Level level, LivingEntity livingEntity, Vec3 vec3, int n) {
        super((EntityType<? extends Fireball>)EntityType.FIREBALL, livingEntity, vec3, level);
        this.explosionPower = n;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            boolean bl = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, bl, Level.ExplosionInteraction.MOB);
            this.discard();
        }
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
        DamageSource damageSource = this.damageSources().fireball(this, entity);
        ((Entity)object).hurt(damageSource, 6.0f);
        EnchantmentHelper.doPostAttackEffects(serverLevel, (Entity)object, damageSource);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("ExplosionPower", 99)) {
            this.explosionPower = compoundTag.getByte("ExplosionPower");
        }
    }
}

