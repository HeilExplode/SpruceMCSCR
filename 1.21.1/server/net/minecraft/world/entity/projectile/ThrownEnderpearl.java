/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEnderpearl
extends ThrowableItemProjectile {
    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownEnderpearl(Level level, LivingEntity livingEntity) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.ENDER_PEARL, livingEntity, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        entityHitResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        ServerLevel serverLevel;
        Object object;
        block12: {
            block11: {
                super.onHit(hitResult);
                for (int i = 0; i < 32; ++i) {
                    this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
                }
                object = this.level();
                if (!(object instanceof ServerLevel)) break block11;
                serverLevel = (ServerLevel)object;
                if (!this.isRemoved()) break block12;
            }
            return;
        }
        object = this.getOwner();
        if (object == null || !ThrownEnderpearl.isAllowedToTeleportOwner((Entity)object, serverLevel)) {
            this.discard();
            return;
        }
        if (((Entity)object).isPassenger()) {
            ((Entity)object).unRide();
        }
        if (object instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)object;
            if (serverPlayer.connection.isAcceptingMessages()) {
                Endermite endermite;
                if (this.random.nextFloat() < 0.05f && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && (endermite = EntityType.ENDERMITE.create(serverLevel)) != null) {
                    endermite.moveTo(((Entity)object).getX(), ((Entity)object).getY(), ((Entity)object).getZ(), ((Entity)object).getYRot(), ((Entity)object).getXRot());
                    serverLevel.addFreshEntity(endermite);
                }
                ((Entity)object).changeDimension(new DimensionTransition(serverLevel, this.position(), ((Entity)object).getDeltaMovement(), ((Entity)object).getYRot(), ((Entity)object).getXRot(), DimensionTransition.DO_NOTHING));
                ((Entity)object).resetFallDistance();
                serverPlayer.resetCurrentImpulseContext();
                ((Entity)object).hurt(this.damageSources().fall(), 5.0f);
                this.playSound(serverLevel, this.position());
            }
        } else {
            ((Entity)object).changeDimension(new DimensionTransition(serverLevel, this.position(), ((Entity)object).getDeltaMovement(), ((Entity)object).getYRot(), ((Entity)object).getXRot(), DimensionTransition.DO_NOTHING));
            ((Entity)object).resetFallDistance();
            this.playSound(serverLevel, this.position());
        }
        this.discard();
    }

    private static boolean isAllowedToTeleportOwner(Entity entity, Level level) {
        if (entity.level().dimension() == level.dimension()) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                return livingEntity.isAlive() && !livingEntity.isSleeping();
            }
            return entity.isAlive();
        }
        return entity.canUsePortal(true);
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayer && !entity.isAlive() && this.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else {
            super.tick();
        }
    }

    private void playSound(Level level, Vec3 vec3) {
        level.playSound(null, vec3.x, vec3.y, vec3.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
    }

    @Override
    public boolean canChangeDimensions(Level level, Level level2) {
        Entity entity;
        if (level.dimension() == Level.END && (entity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            return super.canChangeDimensions(level, level2) && serverPlayer.seenCredits;
        }
        return super.canChangeDimensions(level, level2);
    }

    @Override
    protected void onInsideBlock(BlockState blockState) {
        Entity entity;
        super.onInsideBlock(blockState);
        if (blockState.is(Blocks.END_GATEWAY) && (entity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.onInsideBlock(blockState);
        }
    }
}

