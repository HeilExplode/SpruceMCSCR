/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Projectile
extends Entity
implements TraceableEntity {
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private boolean leftOwner;
    private boolean hasBeenShot;
    @Nullable
    private Entity lastDeflectedBy;

    Projectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }
    }

    @Override
    @Nullable
    public Entity getOwner() {
        Level level;
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        }
        if (this.ownerUUID != null && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    public Entity getEffectSource() {
        return (Entity)MoreObjects.firstNonNull((Object)this.getOwner(), (Object)this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
        if (this.leftOwner) {
            compoundTag.putBoolean("LeftOwner", true);
        }
        compoundTag.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
            this.cachedOwner = null;
        }
        this.leftOwner = compoundTag.getBoolean("LeftOwner");
        this.hasBeenShot = compoundTag.getBoolean("HasBeenShot");
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            this.cachedOwner = projectile.cachedOwner;
        }
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }
        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            for (Entity entity3 : this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), entity -> !entity.isSpectator() && entity.isPickable())) {
                if (entity3.getRootVehicle() != entity2.getRootVehicle()) continue;
                return false;
            }
        }
        return true;
    }

    public Vec3 getMovementToShoot(double d, double d2, double d3, float f, float f2) {
        return new Vec3(d, d2, d3).normalize().add(this.random.triangle(0.0, 0.0172275 * (double)f2), this.random.triangle(0.0, 0.0172275 * (double)f2), this.random.triangle(0.0, 0.0172275 * (double)f2)).scale(f);
    }

    public void shoot(double d, double d2, double d3, float f, float f2) {
        Vec3 vec3 = this.getMovementToShoot(d, d2, d3, f, f2);
        this.setDeltaMovement(vec3);
        this.hasImpulse = true;
        double d4 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2(vec3.y, d4) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity entity, float f, float f2, float f3, float f4, float f5) {
        float f6 = -Mth.sin(f2 * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        float f7 = -Mth.sin((f + f3) * ((float)Math.PI / 180));
        float f8 = Mth.cos(f2 * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        this.shoot(f6, f7, f8, f4, f5);
        Vec3 vec3 = entity.getKnownMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.onGround() ? 0.0 : vec3.y, vec3.z));
    }

    protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult hitResult) {
        EntityHitResult entityHitResult;
        Entity entity;
        ProjectileDeflection projectileDeflection;
        if (hitResult.getType() == HitResult.Type.ENTITY && (projectileDeflection = (entity = (entityHitResult = (EntityHitResult)hitResult).getEntity()).deflection(this)) != ProjectileDeflection.NONE) {
            if (entity != this.lastDeflectedBy && this.deflect(projectileDeflection, entity, this.getOwner(), false)) {
                this.lastDeflectedBy = entity;
            }
            return projectileDeflection;
        }
        this.onHit(hitResult);
        return ProjectileDeflection.NONE;
    }

    public boolean deflect(ProjectileDeflection projectileDeflection, @Nullable Entity entity, @Nullable Entity entity2, boolean bl) {
        if (!this.level().isClientSide) {
            projectileDeflection.deflect(this, entity, this.random);
            this.setOwner(entity2);
            this.onDeflection(entity, bl);
        }
        return true;
    }

    protected void onDeflection(@Nullable Entity entity, boolean bl) {
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = entityHitResult.getEntity();
            if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile) {
                Projectile projectile = (Projectile)entity;
                projectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.getOwner(), true);
            }
            this.onHitEntity(entityHitResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            this.onHitBlock(blockHitResult);
            BlockPos blockPos = blockHitResult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockState blockState = this.level().getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.level(), blockState, blockHitResult, this);
    }

    @Override
    public void lerpMotion(double d, double d2, double d3) {
        this.setDeltaMovement(d, d2, d3);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            double d4 = Math.sqrt(d * d + d3 * d3);
            this.setXRot((float)(Mth.atan2(d2, d4) * 57.2957763671875));
            this.setYRot((float)(Mth.atan2(d, d3) * 57.2957763671875));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    protected boolean canHitEntity(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        }
        Entity entity2 = this.getOwner();
        return entity2 == null || this.leftOwner || !entity2.isPassengerOfSameVehicle(entity);
    }

    protected void updateRotation() {
        Vec3 vec3 = this.getDeltaMovement();
        double d = vec3.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d) * 57.2957763671875)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875)));
    }

    protected static float lerpRotation(float f, float f2) {
        while (f2 - f < -180.0f) {
            f -= 360.0f;
        }
        while (f2 - f >= 180.0f) {
            f += 360.0f;
        }
        return Mth.lerp(0.2f, f, f2);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket((Entity)this, serverEntity, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        Entity entity = this.level().getEntity(clientboundAddEntityPacket.getData());
        if (entity != null) {
            this.setOwner(entity);
        }
    }

    @Override
    public boolean mayInteract(Level level, BlockPos blockPos) {
        Entity entity = this.getOwner();
        if (entity instanceof Player) {
            return entity.mayInteract(level, blockPos);
        }
        return entity == null || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public boolean mayBreak(Level level) {
        return this.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && level.getGameRules().getBoolean(GameRules.RULE_PROJECTILESCANBREAKBLOCKS);
    }

    @Override
    public boolean isPickable() {
        return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getPickRadius() {
        return this.isPickable() ? 1.0f : 0.0f;
    }

    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
        double d = this.getDeltaMovement().x;
        double d2 = this.getDeltaMovement().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)d2);
    }
}

