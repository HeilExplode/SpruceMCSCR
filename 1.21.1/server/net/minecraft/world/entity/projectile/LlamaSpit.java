/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit
extends Projectile {
    public LlamaSpit(EntityType<? extends LlamaSpit> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    public LlamaSpit(Level level, Llama llama) {
        this((EntityType<? extends LlamaSpit>)EntityType.LLAMA_SPIT, level);
        this.setOwner(llama);
        this.setPos(llama.getX() - (double)(llama.getBbWidth() + 1.0f) * 0.5 * (double)Mth.sin(llama.yBodyRot * ((float)Math.PI / 180)), llama.getEyeY() - (double)0.1f, llama.getZ() + (double)(llama.getBbWidth() + 1.0f) * 0.5 * (double)Mth.cos(llama.yBodyRot * ((float)Math.PI / 180)));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        this.hitTargetOrDeflectSelf(hitResult);
        double d = this.getX() + vec3.x;
        double d2 = this.getY() + vec3.y;
        double d3 = this.getZ() + vec3.z;
        this.updateRotation();
        float f = 0.99f;
        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
            this.discard();
            return;
        }
        if (this.isInWaterOrBubble()) {
            this.discard();
            return;
        }
        this.setDeltaMovement(vec3.scale(0.99f));
        this.applyGravity();
        this.setPos(d, d2, d3);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            Level level;
            DamageSource damageSource;
            LivingEntity livingEntity = (LivingEntity)entity;
            entity = entityHitResult.getEntity();
            if (entity.hurt(damageSource = this.damageSources().spit(this, livingEntity), 1.0f) && (level = this.level()) instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        double d = clientboundAddEntityPacket.getXa();
        double d2 = clientboundAddEntityPacket.getYa();
        double d3 = clientboundAddEntityPacket.getZa();
        for (int i = 0; i < 7; ++i) {
            double d4 = 0.4 + 0.1 * (double)i;
            this.level().addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), d * d4, d2, d3 * d4);
        }
        this.setDeltaMovement(d, d2, d3);
    }
}

