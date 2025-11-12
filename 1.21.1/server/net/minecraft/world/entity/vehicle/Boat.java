/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.vehicle;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Boat
extends VehicleEntity
implements Leashable,
VariantHolder<Type> {
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    public static final int PADDLE_LEFT = 0;
    public static final int PADDLE_RIGHT = 1;
    private static final int TIME_TO_EJECT = 60;
    private static final float PADDLE_SPEED = 0.3926991f;
    public static final double PADDLE_SOUND_TIME = 0.7853981852531433;
    public static final int BUBBLE_TIME = 60;
    private final float[] paddlePositions = new float[2];
    private float invFriction;
    private float outOfControlTicks;
    private float deltaRotation;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private double waterLevel;
    private float landFriction;
    private Status status;
    private Status oldStatus;
    private double lastYd;
    private boolean isAboveBubbleColumn;
    private boolean bubbleColumnDirectionIsDown;
    private float bubbleMultiplier;
    private float bubbleAngle;
    private float bubbleAngleO;
    @Nullable
    private Leashable.LeashData leashData;

    public Boat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public Boat(Level level, double d, double d2, double d3) {
        this((EntityType<? extends Boat>)EntityType.BOAT, level);
        this.setPos(d, d2, d3);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_TYPE, Type.OAK.ordinal());
        builder.define(DATA_ID_PADDLE_LEFT, false);
        builder.define(DATA_ID_PADDLE_RIGHT, false);
        builder.define(DATA_ID_BUBBLE_TIME, 0);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return Boat.canVehicleCollide(this, entity);
    }

    public static boolean canVehicleCollide(Entity entity, Entity entity2) {
        return (entity2.canBeCollidedWith() || entity2.isPushable()) && !entity.isPassengerOfSameVehicle(entity2);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        float f2 = this.getSinglePassengerXOffset();
        if (this.getPassengers().size() > 1) {
            int n = this.getPassengers().indexOf(entity);
            f2 = n == 0 ? 0.2f : -0.6f;
            if (entity instanceof Animal) {
                f2 += 0.2f;
            }
        }
        return new Vec3(0.0, this.getVariant() == Type.BAMBOO ? (double)(entityDimensions.height() * 0.8888889f) : (double)(entityDimensions.height() / 3.0f), f2).yRot(-this.getYRot() * ((float)Math.PI / 180));
    }

    @Override
    public void onAboveBubbleCol(boolean bl) {
        if (!this.level().isClientSide) {
            this.isAboveBubbleColumn = true;
            this.bubbleColumnDirectionIsDown = bl;
            if (this.getBubbleTime() == 0) {
                this.setBubbleTime(60);
            }
        }
        this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7, this.getZ() + (double)this.random.nextFloat(), 0.0, 0.0, 0.0);
        if (this.random.nextInt(20) == 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0f, 0.8f + 0.4f * this.random.nextFloat(), false);
            this.gameEvent(GameEvent.SPLASH, this.getControllingPassenger());
        }
    }

    @Override
    public void push(Entity entity) {
        if (entity instanceof Boat) {
            if (entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
                super.push(entity);
            }
        } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
            super.push(entity);
        }
    }

    @Override
    public Item getDropItem() {
        return switch (this.getVariant().ordinal()) {
            case 1 -> Items.SPRUCE_BOAT;
            case 2 -> Items.BIRCH_BOAT;
            case 3 -> Items.JUNGLE_BOAT;
            case 4 -> Items.ACACIA_BOAT;
            case 5 -> Items.CHERRY_BOAT;
            case 6 -> Items.DARK_OAK_BOAT;
            case 7 -> Items.MANGROVE_BOAT;
            case 8 -> Items.BAMBOO_RAFT;
            default -> Items.OAK_BOAT;
        };
    }

    @Override
    public void animateHurt(float f) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() * 11.0f);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void lerpTo(double d, double d2, double d3, float f, float f2, int n) {
        this.lerpX = d;
        this.lerpY = d2;
        this.lerpZ = d3;
        this.lerpYRot = f;
        this.lerpXRot = f2;
        this.lerpSteps = 10;
    }

    @Override
    public double lerpTargetX() {
        return this.lerpSteps > 0 ? this.lerpX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.lerpSteps > 0 ? this.lerpY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.lerpSteps > 0 ? (float)this.lerpXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.lerpSteps > 0 ? (float)this.lerpYRot : this.getYRot();
    }

    @Override
    public Direction getMotionDirection() {
        return this.getDirection().getClockWise();
    }

    @Override
    public void tick() {
        this.oldStatus = this.status;
        this.status = this.getStatus();
        this.outOfControlTicks = this.status == Status.UNDER_WATER || this.status == Status.UNDER_FLOWING_WATER ? (this.outOfControlTicks += 1.0f) : 0.0f;
        if (!this.level().isClientSide && this.outOfControlTicks >= 60.0f) {
            this.ejectPassengers();
        }
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0f) {
            this.setDamage(this.getDamage() - 1.0f);
        }
        super.tick();
        this.tickLerp();
        if (this.isControlledByLocalInstance()) {
            if (!(this.getFirstPassenger() instanceof Player)) {
                this.setPaddleState(false, false);
            }
            this.floatBoat();
            if (this.level().isClientSide) {
                this.controlBoat();
                this.level().sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.tickBubbleColumn();
        for (int i = 0; i <= 1; ++i) {
            if (this.getPaddleState(i)) {
                SoundEvent soundEvent;
                if (!this.isSilent() && (double)(this.paddlePositions[i] % ((float)Math.PI * 2)) <= 0.7853981852531433 && (double)((this.paddlePositions[i] + 0.3926991f) % ((float)Math.PI * 2)) >= 0.7853981852531433 && (soundEvent = this.getPaddleSound()) != null) {
                    Vec3 vec3 = this.getViewVector(1.0f);
                    double d = i == 1 ? -vec3.z : vec3.z;
                    double d2 = i == 1 ? vec3.x : -vec3.x;
                    this.level().playSound(null, this.getX() + d, this.getY(), this.getZ() + d2, soundEvent, this.getSoundSource(), 1.0f, 0.8f + 0.4f * this.random.nextFloat());
                }
                int n = i;
                this.paddlePositions[n] = this.paddlePositions[n] + 0.3926991f;
                continue;
            }
            this.paddlePositions[i] = 0.0f;
        }
        this.checkInsideBlocks();
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.2f, -0.01f, 0.2f), EntitySelector.pushableBy(this));
        if (!list.isEmpty()) {
            boolean bl = !this.level().isClientSide && !(this.getControllingPassenger() instanceof Player);
            for (Entity entity : list) {
                if (entity.hasPassenger(this)) continue;
                if (bl && this.getPassengers().size() < this.getMaxPassengers() && !entity.isPassenger() && this.hasEnoughSpaceFor(entity) && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player)) {
                    entity.startRiding(this);
                    continue;
                }
                this.push(entity);
            }
        }
    }

    private void tickBubbleColumn() {
        if (this.level().isClientSide) {
            int n = this.getBubbleTime();
            this.bubbleMultiplier = n > 0 ? (this.bubbleMultiplier += 0.05f) : (this.bubbleMultiplier -= 0.1f);
            this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0f, 1.0f);
            this.bubbleAngleO = this.bubbleAngle;
            this.bubbleAngle = 10.0f * (float)Math.sin(0.5f * (float)this.level().getGameTime()) * this.bubbleMultiplier;
        } else {
            int n;
            if (!this.isAboveBubbleColumn) {
                this.setBubbleTime(0);
            }
            if ((n = this.getBubbleTime()) > 0) {
                this.setBubbleTime(--n);
                int n2 = 60 - n - 1;
                if (n2 > 0 && n == 0) {
                    this.setBubbleTime(0);
                    Vec3 vec3 = this.getDeltaMovement();
                    if (this.bubbleColumnDirectionIsDown) {
                        this.setDeltaMovement(vec3.add(0.0, -0.7, 0.0));
                        this.ejectPassengers();
                    } else {
                        this.setDeltaMovement(vec3.x, this.hasPassenger((Entity entity) -> entity instanceof Player) ? 2.7 : 0.6, vec3.z);
                    }
                }
                this.isAboveBubbleColumn = false;
            }
        }
    }

    @Nullable
    protected SoundEvent getPaddleSound() {
        switch (this.getStatus().ordinal()) {
            case 0: 
            case 1: 
            case 2: {
                return SoundEvents.BOAT_PADDLE_WATER;
            }
            case 3: {
                return SoundEvents.BOAT_PADDLE_LAND;
            }
        }
        return null;
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        if (this.lerpSteps <= 0) {
            return;
        }
        this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
        --this.lerpSteps;
    }

    public void setPaddleState(boolean bl, boolean bl2) {
        this.entityData.set(DATA_ID_PADDLE_LEFT, bl);
        this.entityData.set(DATA_ID_PADDLE_RIGHT, bl2);
    }

    public float getRowingTime(int n, float f) {
        if (this.getPaddleState(n)) {
            return Mth.clampedLerp(this.paddlePositions[n] - 0.3926991f, this.paddlePositions[n], f);
        }
        return 0.0f;
    }

    @Override
    @Nullable
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.88f * this.getEyeHeight(), this.getBbWidth() * 0.64f);
    }

    @Override
    public void elasticRangeLeashBehaviour(Entity entity, float f) {
        Vec3 vec3 = entity.position().subtract(this.position()).normalize().scale((double)f - 6.0);
        Vec3 vec32 = this.getDeltaMovement();
        boolean bl = vec32.dot(vec3) > 0.0;
        this.setDeltaMovement(vec32.add(vec3.scale(bl ? (double)0.15f : (double)0.2f)));
    }

    private Status getStatus() {
        Status status = this.isUnderwater();
        if (status != null) {
            this.waterLevel = this.getBoundingBox().maxY;
            return status;
        }
        if (this.checkInWater()) {
            return Status.IN_WATER;
        }
        float f = this.getGroundFriction();
        if (f > 0.0f) {
            this.landFriction = f;
            return Status.ON_LAND;
        }
        return Status.IN_AIR;
    }

    public float getWaterLevelAbove() {
        AABB aABB = this.getBoundingBox();
        int n = Mth.floor(aABB.minX);
        int n2 = Mth.ceil(aABB.maxX);
        int n3 = Mth.floor(aABB.maxY);
        int n4 = Mth.ceil(aABB.maxY - this.lastYd);
        int n5 = Mth.floor(aABB.minZ);
        int n6 = Mth.ceil(aABB.maxZ);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block0: for (int i = n3; i < n4; ++i) {
            float f = 0.0f;
            for (int j = n; j < n2; ++j) {
                for (int k = n5; k < n6; ++k) {
                    mutableBlockPos.set(j, i, k);
                    FluidState fluidState = this.level().getFluidState(mutableBlockPos);
                    if (fluidState.is(FluidTags.WATER)) {
                        f = Math.max(f, fluidState.getHeight(this.level(), mutableBlockPos));
                    }
                    if (f >= 1.0f) continue block0;
                }
            }
            if (!(f < 1.0f)) continue;
            return (float)mutableBlockPos.getY() + f;
        }
        return n4 + 1;
    }

    public float getGroundFriction() {
        AABB aABB = this.getBoundingBox();
        AABB aABB2 = new AABB(aABB.minX, aABB.minY - 0.001, aABB.minZ, aABB.maxX, aABB.minY, aABB.maxZ);
        int n = Mth.floor(aABB2.minX) - 1;
        int n2 = Mth.ceil(aABB2.maxX) + 1;
        int n3 = Mth.floor(aABB2.minY) - 1;
        int n4 = Mth.ceil(aABB2.maxY) + 1;
        int n5 = Mth.floor(aABB2.minZ) - 1;
        int n6 = Mth.ceil(aABB2.maxZ) + 1;
        VoxelShape voxelShape = Shapes.create(aABB2);
        float f = 0.0f;
        int n7 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n2; ++i) {
            for (int j = n5; j < n6; ++j) {
                int n8 = (i == n || i == n2 - 1 ? 1 : 0) + (j == n5 || j == n6 - 1 ? 1 : 0);
                if (n8 == 2) continue;
                for (int k = n3; k < n4; ++k) {
                    if (n8 > 0 && (k == n3 || k == n4 - 1)) continue;
                    mutableBlockPos.set(i, k, j);
                    BlockState blockState = this.level().getBlockState(mutableBlockPos);
                    if (blockState.getBlock() instanceof WaterlilyBlock || !Shapes.joinIsNotEmpty(blockState.getCollisionShape(this.level(), mutableBlockPos).move(i, k, j), voxelShape, BooleanOp.AND)) continue;
                    f += blockState.getBlock().getFriction();
                    ++n7;
                }
            }
        }
        return f / (float)n7;
    }

    private boolean checkInWater() {
        AABB aABB = this.getBoundingBox();
        int n = Mth.floor(aABB.minX);
        int n2 = Mth.ceil(aABB.maxX);
        int n3 = Mth.floor(aABB.minY);
        int n4 = Mth.ceil(aABB.minY + 0.001);
        int n5 = Mth.floor(aABB.minZ);
        int n6 = Mth.ceil(aABB.maxZ);
        boolean bl = false;
        this.waterLevel = -1.7976931348623157E308;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    mutableBlockPos.set(i, j, k);
                    FluidState fluidState = this.level().getFluidState(mutableBlockPos);
                    if (!fluidState.is(FluidTags.WATER)) continue;
                    float f = (float)j + fluidState.getHeight(this.level(), mutableBlockPos);
                    this.waterLevel = Math.max((double)f, this.waterLevel);
                    bl |= aABB.minY < (double)f;
                }
            }
        }
        return bl;
    }

    @Nullable
    private Status isUnderwater() {
        AABB aABB = this.getBoundingBox();
        double d = aABB.maxY + 0.001;
        int n = Mth.floor(aABB.minX);
        int n2 = Mth.ceil(aABB.maxX);
        int n3 = Mth.floor(aABB.maxY);
        int n4 = Mth.ceil(d);
        int n5 = Mth.floor(aABB.minZ);
        int n6 = Mth.ceil(aABB.maxZ);
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    mutableBlockPos.set(i, j, k);
                    FluidState fluidState = this.level().getFluidState(mutableBlockPos);
                    if (!fluidState.is(FluidTags.WATER) || !(d < (double)((float)mutableBlockPos.getY() + fluidState.getHeight(this.level(), mutableBlockPos)))) continue;
                    if (fluidState.isSource()) {
                        bl = true;
                        continue;
                    }
                    return Status.UNDER_FLOWING_WATER;
                }
            }
        }
        return bl ? Status.UNDER_WATER : null;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    private void floatBoat() {
        double d = -this.getGravity();
        double d2 = 0.0;
        this.invFriction = 0.05f;
        if (this.oldStatus == Status.IN_AIR && this.status != Status.IN_AIR && this.status != Status.ON_LAND) {
            this.waterLevel = this.getY(1.0);
            double d3 = (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101;
            if (this.level().noCollision(this, this.getBoundingBox().move(0.0, d3 - this.getY(), 0.0))) {
                this.setPos(this.getX(), d3, this.getZ());
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                this.lastYd = 0.0;
            }
            this.status = Status.IN_WATER;
        } else {
            if (this.status == Status.IN_WATER) {
                d2 = (this.waterLevel - this.getY()) / (double)this.getBbHeight();
                this.invFriction = 0.9f;
            } else if (this.status == Status.UNDER_FLOWING_WATER) {
                d = -7.0E-4;
                this.invFriction = 0.9f;
            } else if (this.status == Status.UNDER_WATER) {
                d2 = 0.01f;
                this.invFriction = 0.45f;
            } else if (this.status == Status.IN_AIR) {
                this.invFriction = 0.9f;
            } else if (this.status == Status.ON_LAND) {
                this.invFriction = this.landFriction;
                if (this.getControllingPassenger() instanceof Player) {
                    this.landFriction /= 2.0f;
                }
            }
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.x * (double)this.invFriction, vec3.y + d, vec3.z * (double)this.invFriction);
            this.deltaRotation *= this.invFriction;
            if (d2 > 0.0) {
                Vec3 vec32 = this.getDeltaMovement();
                this.setDeltaMovement(vec32.x, (vec32.y + d2 * (this.getDefaultGravity() / 0.65)) * 0.75, vec32.z);
            }
        }
    }

    private void controlBoat() {
        if (!this.isVehicle()) {
            return;
        }
        float f = 0.0f;
        if (this.inputLeft) {
            this.deltaRotation -= 1.0f;
        }
        if (this.inputRight) {
            this.deltaRotation += 1.0f;
        }
        if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
            f += 0.005f;
        }
        this.setYRot(this.getYRot() + this.deltaRotation);
        if (this.inputUp) {
            f += 0.04f;
        }
        if (this.inputDown) {
            f -= 0.005f;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(Mth.sin(-this.getYRot() * ((float)Math.PI / 180)) * f, 0.0, Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * f));
        this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
    }

    protected float getSinglePassengerXOffset() {
        return 0.0f;
    }

    public boolean hasEnoughSpaceFor(Entity entity) {
        return entity.getBbWidth() < this.getBbWidth();
    }

    @Override
    protected void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
        super.positionRider(entity, moveFunction);
        if (entity.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            return;
        }
        entity.setYRot(entity.getYRot() + this.deltaRotation);
        entity.setYHeadRot(entity.getYHeadRot() + this.deltaRotation);
        this.clampRotation(entity);
        if (entity instanceof Animal && this.getPassengers().size() == this.getMaxPassengers()) {
            int n = entity.getId() % 2 == 0 ? 90 : 270;
            entity.setYBodyRot(((Animal)entity).yBodyRot + (float)n);
            entity.setYHeadRot(entity.getYHeadRot() + (float)n);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3 vec3 = Boat.getCollisionHorizontalEscapeVector(this.getBbWidth() * Mth.SQRT_OF_TWO, livingEntity.getBbWidth(), livingEntity.getYRot());
        double d = this.getX() + vec3.x;
        double d2 = this.getZ() + vec3.z;
        BlockPos blockPos = BlockPos.containing(d, this.getBoundingBox().maxY, d2);
        BlockPos blockPos2 = blockPos.below();
        if (!this.level().isWaterAt(blockPos2)) {
            double d3;
            ArrayList arrayList = Lists.newArrayList();
            double d4 = this.level().getBlockFloorHeight(blockPos);
            if (DismountHelper.isBlockFloorValid(d4)) {
                arrayList.add(new Vec3(d, (double)blockPos.getY() + d4, d2));
            }
            if (DismountHelper.isBlockFloorValid(d3 = this.level().getBlockFloorHeight(blockPos2))) {
                arrayList.add(new Vec3(d, (double)blockPos2.getY() + d3, d2));
            }
            for (Pose pose : livingEntity.getDismountPoses()) {
                for (Vec3 vec32 : arrayList) {
                    if (!DismountHelper.canDismountTo(this.level(), vec32, livingEntity, pose)) continue;
                    livingEntity.setPose(pose);
                    return vec32;
                }
            }
        }
        return super.getDismountLocationForPassenger(livingEntity);
    }

    protected void clampRotation(Entity entity) {
        entity.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(entity.getYRot() - this.getYRot());
        float f2 = Mth.clamp(f, -105.0f, 105.0f);
        entity.yRotO += f2 - f;
        entity.setYRot(entity.getYRot() + f2 - f);
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        this.clampRotation(entity);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        this.writeLeashData(compoundTag, this.leashData);
        compoundTag.putString("Type", this.getVariant().getSerializedName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.leashData = this.readLeashData(compoundTag);
        if (compoundTag.contains("Type", 8)) {
            this.setVariant(Type.byName(compoundTag.getString("Type")));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult = super.interact(player, interactionHand);
        if (interactionResult != InteractionResult.PASS) {
            return interactionResult;
        }
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (this.outOfControlTicks < 60.0f) {
            if (!this.level().isClientSide) {
                return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide && removalReason.shouldDestroy() && this.isLeashed()) {
            this.dropLeash(true, true);
        }
        super.remove(removalReason);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        this.lastYd = this.getDeltaMovement().y;
        if (this.isPassenger()) {
            return;
        }
        if (bl) {
            if (this.fallDistance > 3.0f) {
                if (this.status != Status.ON_LAND) {
                    this.resetFallDistance();
                    return;
                }
                this.causeFallDamage(this.fallDistance, 1.0f, this.damageSources().fall());
                if (!this.level().isClientSide && !this.isRemoved()) {
                    this.kill();
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        int n;
                        for (n = 0; n < 3; ++n) {
                            this.spawnAtLocation(this.getVariant().getPlanks());
                        }
                        for (n = 0; n < 2; ++n) {
                            this.spawnAtLocation(Items.STICK);
                        }
                    }
                }
            }
            this.resetFallDistance();
        } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && d < 0.0) {
            this.fallDistance -= (float)d;
        }
    }

    public boolean getPaddleState(int n) {
        return this.entityData.get(n == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) != false && this.getControllingPassenger() != null;
    }

    private void setBubbleTime(int n) {
        this.entityData.set(DATA_ID_BUBBLE_TIME, n);
    }

    private int getBubbleTime() {
        return this.entityData.get(DATA_ID_BUBBLE_TIME);
    }

    public float getBubbleAngle(float f) {
        return Mth.lerp(f, this.bubbleAngleO, this.bubbleAngle);
    }

    @Override
    public void setVariant(Type type) {
        this.entityData.set(DATA_ID_TYPE, type.ordinal());
    }

    @Override
    public Type getVariant() {
        return Type.byId(this.entityData.get(DATA_ID_TYPE));
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < this.getMaxPassengers() && !this.isEyeInFluid(FluidTags.WATER);
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        LivingEntity livingEntity;
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : super.getControllingPassenger();
    }

    public void setInput(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        this.inputLeft = bl;
        this.inputRight = bl2;
        this.inputUp = bl3;
        this.inputDown = bl4;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(this.getDropItem().getDescriptionId());
    }

    @Override
    public boolean isUnderWater() {
        return this.status == Status.UNDER_WATER || this.status == Status.UNDER_FLOWING_WATER;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDropItem());
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static enum Type implements StringRepresentable
    {
        OAK(Blocks.OAK_PLANKS, "oak"),
        SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
        BIRCH(Blocks.BIRCH_PLANKS, "birch"),
        JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
        ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
        CHERRY(Blocks.CHERRY_PLANKS, "cherry"),
        DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak"),
        MANGROVE(Blocks.MANGROVE_PLANKS, "mangrove"),
        BAMBOO(Blocks.BAMBOO_PLANKS, "bamboo");

        private final String name;
        private final Block planks;
        public static final StringRepresentable.EnumCodec<Type> CODEC;
        private static final IntFunction<Type> BY_ID;

        private Type(Block block, String string2) {
            this.name = string2;
            this.planks = block;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        public Block getPlanks() {
            return this.planks;
        }

        public String toString() {
            return this.name;
        }

        public static Type byId(int n) {
            return BY_ID.apply(n);
        }

        public static Type byName(String string) {
            return CODEC.byName(string, OAK);
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values);
            BY_ID = ByIdMap.continuous(Enum::ordinal, Type.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        }
    }

    public static enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;

    }
}

