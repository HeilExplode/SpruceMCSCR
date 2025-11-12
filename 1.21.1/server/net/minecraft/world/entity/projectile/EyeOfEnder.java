/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EyeOfEnder
extends Entity
implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeOfEnder.class, EntityDataSerializers.ITEM_STACK);
    private double tx;
    private double ty;
    private double tz;
    private int life;
    private boolean surviveAfterDeath;

    public EyeOfEnder(EntityType<? extends EyeOfEnder> entityType, Level level) {
        super(entityType, level);
    }

    public EyeOfEnder(Level level, double d, double d2, double d3) {
        this((EntityType<? extends EyeOfEnder>)EntityType.EYE_OF_ENDER, level);
        this.setPos(d, d2, d3);
    }

    public void setItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            this.getEntityData().set(DATA_ITEM_STACK, this.getDefaultItem());
        } else {
            this.getEntityData().set(DATA_ITEM_STACK, itemStack.copyWithCount(1));
        }
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM_STACK, this.getDefaultItem());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d2)) {
            d2 = 4.0;
        }
        return d < (d2 *= 64.0) * d2;
    }

    public void signalTo(BlockPos blockPos) {
        double d;
        double d2 = blockPos.getX();
        int n = blockPos.getY();
        double d3 = blockPos.getZ();
        double d4 = d2 - this.getX();
        double d5 = Math.sqrt(d4 * d4 + (d = d3 - this.getZ()) * d);
        if (d5 > 12.0) {
            this.tx = this.getX() + d4 / d5 * 12.0;
            this.tz = this.getZ() + d / d5 * 12.0;
            this.ty = this.getY() + 8.0;
        } else {
            this.tx = d2;
            this.ty = n;
            this.tz = d3;
        }
        this.life = 0;
        this.surviveAfterDeath = this.random.nextInt(5) > 0;
    }

    @Override
    public void lerpMotion(double d, double d2, double d3) {
        this.setDeltaMovement(d, d2, d3);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            double d4 = Math.sqrt(d * d + d3 * d3);
            this.setYRot((float)(Mth.atan2(d, d3) * 57.2957763671875));
            this.setXRot((float)(Mth.atan2(d2, d4) * 57.2957763671875));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vec3 = this.getDeltaMovement();
        double d = this.getX() + vec3.x;
        double d2 = this.getY() + vec3.y;
        double d3 = this.getZ() + vec3.z;
        double d4 = vec3.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d4) * 57.2957763671875)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875)));
        if (!this.level().isClientSide) {
            double d5 = this.tx - d;
            double d6 = this.tz - d3;
            float f = (float)Math.sqrt(d5 * d5 + d6 * d6);
            float f2 = (float)Mth.atan2(d6, d5);
            double d7 = Mth.lerp(0.0025, d4, (double)f);
            double d8 = vec3.y;
            if (f < 1.0f) {
                d7 *= 0.8;
                d8 *= 0.8;
            }
            int n = this.getY() < this.ty ? 1 : -1;
            vec3 = new Vec3(Math.cos(f2) * d7, d8 + ((double)n - d8) * (double)0.015f, Math.sin(f2) * d7);
            this.setDeltaMovement(vec3);
        }
        float f = 0.25f;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.BUBBLE, d - vec3.x * 0.25, d2 - vec3.y * 0.25, d3 - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
            }
        } else {
            this.level().addParticle(ParticleTypes.PORTAL, d - vec3.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3, d2 - vec3.y * 0.25 - 0.5, d3 - vec3.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3, vec3.x, vec3.y, vec3.z);
        }
        if (!this.level().isClientSide) {
            this.setPos(d, d2, d3);
            ++this.life;
            if (this.life > 80 && !this.level().isClientSide) {
                this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0f, 1.0f);
                this.discard();
                if (this.surviveAfterDeath) {
                    this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
                } else {
                    this.level().levelEvent(2003, this.blockPosition(), 0);
                }
            }
        } else {
            this.setPosRaw(d, d2, d3);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.put("Item", this.getItem().save(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("Item", 10)) {
            this.setItem(ItemStack.parse(this.registryAccess(), compoundTag.getCompound("Item")).orElse(this.getDefaultItem()));
        } else {
            this.setItem(this.getDefaultItem());
        }
    }

    private ItemStack getDefaultItem() {
        return new ItemStack(Items.ENDER_EYE);
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}

