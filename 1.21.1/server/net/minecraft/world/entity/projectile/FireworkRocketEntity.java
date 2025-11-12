/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketEntity
extends Projectile
implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int lifetime;
    @Nullable
    private LivingEntity attachedToEntity;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    public FireworkRocketEntity(Level level, double d, double d2, double d3, ItemStack itemStack) {
        super((EntityType<? extends Projectile>)EntityType.FIREWORK_ROCKET, level);
        this.life = 0;
        this.setPos(d, d2, d3);
        this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemStack.copy());
        int n = 1;
        Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
        if (fireworks != null) {
            n += fireworks.flightDuration();
        }
        this.setDeltaMovement(this.random.triangle(0.0, 0.002297), 0.05, this.random.triangle(0.0, 0.002297));
        this.lifetime = 10 * n + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public FireworkRocketEntity(Level level, @Nullable Entity entity, double d, double d2, double d3, ItemStack itemStack) {
        this(level, d, d2, d3, itemStack);
        this.setOwner(entity);
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, LivingEntity livingEntity) {
        this(level, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), itemStack);
        this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(livingEntity.getId()));
        this.attachedToEntity = livingEntity;
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, double d, double d2, double d3, boolean bl) {
        this(level, d, d2, d3, itemStack);
        this.entityData.set(DATA_SHOT_AT_ANGLE, bl);
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, Entity entity, double d, double d2, double d3, boolean bl) {
        this(level, itemStack, d, d2, d3, bl);
        this.setOwner(entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_FIREWORKS_ITEM, FireworkRocketEntity.getDefaultItem());
        builder.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
        builder.define(DATA_SHOT_AT_ANGLE, false);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 4096.0 && !this.isAttachedToEntity();
    }

    @Override
    public boolean shouldRender(double d, double d2, double d3) {
        return super.shouldRender(d, d2, d3) && !this.isAttachedToEntity();
    }

    @Override
    public void tick() {
        Object object;
        super.tick();
        if (this.isAttachedToEntity()) {
            if (this.attachedToEntity == null) {
                this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent(n -> {
                    Entity entity = this.level().getEntity(n);
                    if (entity instanceof LivingEntity) {
                        this.attachedToEntity = (LivingEntity)entity;
                    }
                });
            }
            if (this.attachedToEntity != null) {
                if (this.attachedToEntity.isFallFlying()) {
                    Vec3 vec3 = this.attachedToEntity.getLookAngle();
                    double d = 1.5;
                    double d2 = 0.1;
                    Vec3 vec32 = this.attachedToEntity.getDeltaMovement();
                    this.attachedToEntity.setDeltaMovement(vec32.add(vec3.x * 0.1 + (vec3.x * 1.5 - vec32.x) * 0.5, vec3.y * 0.1 + (vec3.y * 1.5 - vec32.y) * 0.5, vec3.z * 0.1 + (vec3.z * 1.5 - vec32.z) * 0.5));
                    object = this.attachedToEntity.getHandHoldingItemAngle(Items.FIREWORK_ROCKET);
                } else {
                    object = Vec3.ZERO;
                }
                this.setPos(this.attachedToEntity.getX() + ((Vec3)object).x, this.attachedToEntity.getY() + ((Vec3)object).y, this.attachedToEntity.getZ() + ((Vec3)object).z);
                this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
            }
        } else {
            if (!this.isShotAtAngle()) {
                double d = this.horizontalCollision ? 1.0 : 1.15;
                this.setDeltaMovement(this.getDeltaMovement().multiply(d, 1.0, d).add(0.0, 0.04, 0.0));
            }
            object = this.getDeltaMovement();
            this.move(MoverType.SELF, (Vec3)object);
            this.setDeltaMovement((Vec3)object);
        }
        object = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (!this.noPhysics) {
            this.hitTargetOrDeflectSelf((HitResult)object);
            this.hasImpulse = true;
        }
        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
        }
        ++this.life;
        if (this.level().isClientSide && this.life % 2 < 2) {
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getDeltaMovement().y * 0.5, this.random.nextGaussian() * 0.05);
        }
        if (!this.level().isClientSide && this.life > this.lifetime) {
            this.explode();
        }
    }

    private void explode() {
        this.level().broadcastEntityEvent(this, (byte)17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.dealExplosionDamage();
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level().isClientSide) {
            return;
        }
        this.explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
        this.level().getBlockState(blockPos).entityInside(this.level(), blockPos, this);
        if (!this.level().isClientSide() && this.hasExplosion()) {
            this.explode();
        }
        super.onHitBlock(blockHitResult);
    }

    private boolean hasExplosion() {
        return !this.getExplosions().isEmpty();
    }

    private void dealExplosionDamage() {
        float f = 0.0f;
        List<FireworkExplosion> list = this.getExplosions();
        if (!list.isEmpty()) {
            f = 5.0f + (float)(list.size() * 2);
        }
        if (f > 0.0f) {
            if (this.attachedToEntity != null) {
                this.attachedToEntity.hurt(this.damageSources().fireworks(this, this.getOwner()), 5.0f + (float)(list.size() * 2));
            }
            double d = 5.0;
            Vec3 vec3 = this.position();
            List<LivingEntity> list2 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0));
            for (LivingEntity livingEntity : list2) {
                if (livingEntity == this.attachedToEntity || this.distanceToSqr(livingEntity) > 25.0) continue;
                boolean bl = false;
                for (int i = 0; i < 2; ++i) {
                    Vec3 vec32 = new Vec3(livingEntity.getX(), livingEntity.getY(0.5 * (double)i), livingEntity.getZ());
                    BlockHitResult blockHitResult = this.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (((HitResult)blockHitResult).getType() != HitResult.Type.MISS) continue;
                    bl = true;
                    break;
                }
                if (!bl) continue;
                float f2 = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(livingEntity)) / 5.0);
                livingEntity.hurt(this.damageSources().fireworks(this, this.getOwner()), f2);
            }
        }
    }

    private boolean isAttachedToEntity() {
        return this.entityData.get(DATA_ATTACHED_TO_TARGET).isPresent();
    }

    public boolean isShotAtAngle() {
        return this.entityData.get(DATA_SHOT_AT_ANGLE);
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 17 && this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            this.level().createFireworks(this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y, vec3.z, this.getExplosions());
        }
        super.handleEntityEvent(by);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Life", this.life);
        compoundTag.putInt("LifeTime", this.lifetime);
        compoundTag.put("FireworksItem", this.getItem().save(this.registryAccess()));
        compoundTag.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.life = compoundTag.getInt("Life");
        this.lifetime = compoundTag.getInt("LifeTime");
        if (compoundTag.contains("FireworksItem", 10)) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, ItemStack.parse(this.registryAccess(), compoundTag.getCompound("FireworksItem")).orElseGet(FireworkRocketEntity::getDefaultItem));
        } else {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, FireworkRocketEntity.getDefaultItem());
        }
        if (compoundTag.contains("ShotAtAngle")) {
            this.entityData.set(DATA_SHOT_AT_ANGLE, compoundTag.getBoolean("ShotAtAngle"));
        }
    }

    private List<FireworkExplosion> getExplosions() {
        ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
        return fireworks != null ? fireworks.explosions() : List.of();
    }

    @Override
    public ItemStack getItem() {
        return this.entityData.get(DATA_ID_FIREWORKS_ITEM);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    private static ItemStack getDefaultItem() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
        double d = livingEntity.position().x - this.position().x;
        double d2 = livingEntity.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)d2);
    }
}

