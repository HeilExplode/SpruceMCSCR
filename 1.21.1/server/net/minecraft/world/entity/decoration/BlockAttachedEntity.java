/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int checkInterval;
    protected BlockPos pos;

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level) {
        super(entityType, level);
    }

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level, BlockPos blockPos) {
        this(entityType, level);
        this.pos = blockPos;
    }

    protected abstract void recalculateBoundingBox();

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            this.checkBelowWorld();
            if (this.checkInterval++ == 100) {
                this.checkInterval = 0;
                if (!this.isRemoved() && !this.survives()) {
                    this.discard();
                    this.dropItem(null);
                }
            }
        }
    }

    public abstract boolean survives();

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (!this.level().mayInteract(player, this.pos)) {
                return true;
            }
            return this.hurt(this.damageSources().playerAttack(player), 0.0f);
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!this.isRemoved() && !this.level().isClientSide) {
            this.kill();
            this.markHurt();
            this.dropItem(damageSource.getEntity());
        }
        return true;
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        if (!this.level().isClientSide && !this.isRemoved() && vec3.lengthSqr() > 0.0) {
            this.kill();
            this.dropItem(null);
        }
    }

    @Override
    public void push(double d, double d2, double d3) {
        if (!this.level().isClientSide && !this.isRemoved() && d * d + d2 * d2 + d3 * d3 > 0.0) {
            this.kill();
            this.dropItem(null);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        BlockPos blockPos = this.getPos();
        compoundTag.putInt("TileX", blockPos.getX());
        compoundTag.putInt("TileY", blockPos.getY());
        compoundTag.putInt("TileZ", blockPos.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        BlockPos blockPos = new BlockPos(compoundTag.getInt("TileX"), compoundTag.getInt("TileY"), compoundTag.getInt("TileZ"));
        if (!blockPos.closerThan(this.blockPosition(), 16.0)) {
            LOGGER.error("Block-attached entity at invalid position: {}", (Object)blockPos);
            return;
        }
        this.pos = blockPos;
    }

    public abstract void dropItem(@Nullable Entity var1);

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void setPos(double d, double d2, double d3) {
        this.pos = BlockPos.containing(d, d2, d3);
        this.recalculateBoundingBox();
        this.hasImpulse = true;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
    }

    @Override
    public void refreshDimensions() {
    }
}

