/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 *  it.unimi.dsi.fastutil.doubles.DoubleListIterator
 *  it.unimi.dsi.fastutil.floats.FloatArraySet
 *  it.unimi.dsi.fastutil.floats.FloatArrays
 *  it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2DoubleMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class Entity
implements SyncedDataHolder,
Nameable,
EntityAccess,
CommandSource,
ScoreHolder {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ID_TAG = "id";
    public static final String PASSENGERS_TAG = "Passengers";
    private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
    public static final int CONTENTS_SLOT_INDEX = 0;
    public static final int BOARDING_COOLDOWN = 60;
    public static final int TOTAL_AIR_SUPPLY = 300;
    public static final int MAX_ENTITY_TAG_COUNT = 1024;
    public static final float DELTA_AFFECTED_BY_BLOCKS_BELOW_0_2 = 0.2f;
    public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_0_5 = 0.500001;
    public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_1_0 = 0.999999;
    public static final int BASE_TICKS_REQUIRED_TO_FREEZE = 140;
    public static final int FREEZE_HURT_FREQUENCY = 40;
    public static final int BASE_SAFE_FALL_DISTANCE = 3;
    private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static final double WATER_FLOW_SCALE = 0.014;
    private static final double LAVA_FAST_FLOW_SCALE = 0.007;
    private static final double LAVA_SLOW_FLOW_SCALE = 0.0023333333333333335;
    public static final String UUID_TAG = "UUID";
    private static double viewScale = 1.0;
    private final EntityType<?> type;
    private int id = ENTITY_COUNTER.incrementAndGet();
    public boolean blocksBuilding;
    private ImmutableList<Entity> passengers = ImmutableList.of();
    protected int boardingCooldown;
    @Nullable
    private Entity vehicle;
    private Level level;
    public double xo;
    public double yo;
    public double zo;
    private Vec3 position;
    private BlockPos blockPosition;
    private ChunkPos chunkPosition;
    private Vec3 deltaMovement = Vec3.ZERO;
    private float yRot;
    private float xRot;
    public float yRotO;
    public float xRotO;
    private AABB bb = INITIAL_AABB;
    private boolean onGround;
    public boolean horizontalCollision;
    public boolean verticalCollision;
    public boolean verticalCollisionBelow;
    public boolean minorHorizontalCollision;
    public boolean hurtMarked;
    protected Vec3 stuckSpeedMultiplier = Vec3.ZERO;
    @Nullable
    private RemovalReason removalReason;
    public static final float DEFAULT_BB_WIDTH = 0.6f;
    public static final float DEFAULT_BB_HEIGHT = 1.8f;
    public float walkDistO;
    public float walkDist;
    public float moveDist;
    public float flyDist;
    public float fallDistance;
    private float nextStep = 1.0f;
    public double xOld;
    public double yOld;
    public double zOld;
    public boolean noPhysics;
    protected final RandomSource random = RandomSource.create();
    public int tickCount;
    private int remainingFireTicks = -this.getFireImmuneTicks();
    protected boolean wasTouchingWater;
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight = new Object2DoubleArrayMap(2);
    protected boolean wasEyeInWater;
    private final Set<TagKey<Fluid>> fluidOnEyes = new HashSet<TagKey<Fluid>>();
    public int invulnerableTime;
    protected boolean firstTick = true;
    protected final SynchedEntityData entityData;
    protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BYTE);
    protected static final int FLAG_ONFIRE = 0;
    private static final int FLAG_SHIFT_KEY_DOWN = 1;
    private static final int FLAG_SPRINTING = 3;
    private static final int FLAG_SWIMMING = 4;
    private static final int FLAG_INVISIBLE = 5;
    protected static final int FLAG_GLOWING = 6;
    protected static final int FLAG_FALL_FLYING = 7;
    private static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT);
    private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SILENT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Pose> DATA_POSE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.POSE);
    private static final EntityDataAccessor<Integer> DATA_TICKS_FROZEN = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
    private EntityInLevelCallback levelCallback = EntityInLevelCallback.NULL;
    private final VecDeltaCodec packetPositionCodec = new VecDeltaCodec();
    public boolean noCulling;
    public boolean hasImpulse;
    @Nullable
    public PortalProcessor portalProcess;
    private int portalCooldown;
    private boolean invulnerable;
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    protected String stringUUID = this.uuid.toString();
    private boolean hasGlowingTag;
    private final Set<String> tags = Sets.newHashSet();
    private final double[] pistonDeltas = new double[]{0.0, 0.0, 0.0};
    private long pistonDeltasGameTime;
    private EntityDimensions dimensions;
    private float eyeHeight;
    public boolean isInPowderSnow;
    public boolean wasInPowderSnow;
    public boolean wasOnFire;
    public Optional<BlockPos> mainSupportingBlockPos = Optional.empty();
    private boolean onGroundNoBlocks = false;
    private float crystalSoundIntensity;
    private int lastCrystalSoundPlayTick;
    private boolean hasVisualFire;
    @Nullable
    private BlockState inBlockState = null;

    public Entity(EntityType<?> entityType, Level level) {
        this.type = entityType;
        this.level = level;
        this.dimensions = entityType.getDimensions();
        this.position = Vec3.ZERO;
        this.blockPosition = BlockPos.ZERO;
        this.chunkPosition = ChunkPos.ZERO;
        SynchedEntityData.Builder builder = new SynchedEntityData.Builder(this);
        builder.define(DATA_SHARED_FLAGS_ID, (byte)0);
        builder.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
        builder.define(DATA_CUSTOM_NAME_VISIBLE, false);
        builder.define(DATA_CUSTOM_NAME, Optional.empty());
        builder.define(DATA_SILENT, false);
        builder.define(DATA_NO_GRAVITY, false);
        builder.define(DATA_POSE, Pose.STANDING);
        builder.define(DATA_TICKS_FROZEN, 0);
        this.defineSynchedData(builder);
        this.entityData = builder.build();
        this.setPos(0.0, 0.0, 0.0);
        this.eyeHeight = this.dimensions.eyeHeight();
    }

    public boolean isColliding(BlockPos blockPos, BlockState blockState) {
        VoxelShape voxelShape = blockState.getCollisionShape(this.level(), blockPos, CollisionContext.of(this));
        VoxelShape voxelShape2 = voxelShape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return Shapes.joinIsNotEmpty(voxelShape2, Shapes.create(this.getBoundingBox()), BooleanOp.AND);
    }

    public int getTeamColor() {
        PlayerTeam playerTeam = this.getTeam();
        if (playerTeam != null && ((Team)playerTeam).getColor().getColor() != null) {
            return ((Team)playerTeam).getColor().getColor();
        }
        return 0xFFFFFF;
    }

    public boolean isSpectator() {
        return false;
    }

    public final void unRide() {
        if (this.isVehicle()) {
            this.ejectPassengers();
        }
        if (this.isPassenger()) {
            this.stopRiding();
        }
    }

    public void syncPacketPositionCodec(double d, double d2, double d3) {
        this.packetPositionCodec.setBase(new Vec3(d, d2, d3));
    }

    public VecDeltaCodec getPositionCodec() {
        return this.packetPositionCodec;
    }

    public EntityType<?> getType() {
        return this.type;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public void setId(int n) {
        this.id = n;
    }

    public Set<String> getTags() {
        return this.tags;
    }

    public boolean addTag(String string) {
        if (this.tags.size() >= 1024) {
            return false;
        }
        return this.tags.add(string);
    }

    public boolean removeTag(String string) {
        return this.tags.remove(string);
    }

    public void kill() {
        this.remove(RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    public final void discard() {
        this.remove(RemovalReason.DISCARDED);
    }

    protected abstract void defineSynchedData(SynchedEntityData.Builder var1);

    public SynchedEntityData getEntityData() {
        return this.entityData;
    }

    public boolean equals(Object object) {
        if (object instanceof Entity) {
            return ((Entity)object).id == this.id;
        }
        return false;
    }

    public int hashCode() {
        return this.id;
    }

    public void remove(RemovalReason removalReason) {
        this.setRemoved(removalReason);
    }

    public void onClientRemoval() {
    }

    public void setPose(Pose pose) {
        this.entityData.set(DATA_POSE, pose);
    }

    public Pose getPose() {
        return this.entityData.get(DATA_POSE);
    }

    public boolean hasPose(Pose pose) {
        return this.getPose() == pose;
    }

    public boolean closerThan(Entity entity, double d) {
        return this.position().closerThan(entity.position(), d);
    }

    public boolean closerThan(Entity entity, double d, double d2) {
        double d3 = entity.getX() - this.getX();
        double d4 = entity.getY() - this.getY();
        double d5 = entity.getZ() - this.getZ();
        return Mth.lengthSquared(d3, d5) < Mth.square(d) && Mth.square(d4) < Mth.square(d2);
    }

    protected void setRot(float f, float f2) {
        this.setYRot(f % 360.0f);
        this.setXRot(f2 % 360.0f);
    }

    public final void setPos(Vec3 vec3) {
        this.setPos(vec3.x(), vec3.y(), vec3.z());
    }

    public void setPos(double d, double d2, double d3) {
        this.setPosRaw(d, d2, d3);
        this.setBoundingBox(this.makeBoundingBox());
    }

    protected AABB makeBoundingBox() {
        return this.dimensions.makeBoundingBox(this.position);
    }

    protected void reapplyPosition() {
        this.setPos(this.position.x, this.position.y, this.position.z);
    }

    public void turn(double d, double d2) {
        float f = (float)d2 * 0.15f;
        float f2 = (float)d * 0.15f;
        this.setXRot(this.getXRot() + f);
        this.setYRot(this.getYRot() + f2);
        this.setXRot(Mth.clamp(this.getXRot(), -90.0f, 90.0f));
        this.xRotO += f;
        this.yRotO += f2;
        this.xRotO = Mth.clamp(this.xRotO, -90.0f, 90.0f);
        if (this.vehicle != null) {
            this.vehicle.onPassengerTurned(this);
        }
    }

    public void tick() {
        this.baseTick();
    }

    public void baseTick() {
        this.level().getProfiler().push("entityBaseTick");
        this.inBlockState = null;
        if (this.isPassenger() && this.getVehicle().isRemoved()) {
            this.stopRiding();
        }
        if (this.boardingCooldown > 0) {
            --this.boardingCooldown;
        }
        this.walkDistO = this.walkDist;
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.handlePortal();
        if (this.canSpawnSprintParticle()) {
            this.spawnSprintParticle();
        }
        this.wasInPowderSnow = this.isInPowderSnow;
        this.isInPowderSnow = false;
        this.updateInWaterStateAndDoFluidPushing();
        this.updateFluidOnEyes();
        this.updateSwimming();
        if (this.level().isClientSide) {
            this.clearFire();
        } else if (this.remainingFireTicks > 0) {
            if (this.fireImmune()) {
                this.setRemainingFireTicks(this.remainingFireTicks - 4);
                if (this.remainingFireTicks < 0) {
                    this.clearFire();
                }
            } else {
                if (this.remainingFireTicks % 20 == 0 && !this.isInLava()) {
                    this.hurt(this.damageSources().onFire(), 1.0f);
                }
                this.setRemainingFireTicks(this.remainingFireTicks - 1);
            }
            if (this.getTicksFrozen() > 0) {
                this.setTicksFrozen(0);
                this.level().levelEvent(null, 1009, this.blockPosition, 1);
            }
        }
        if (this.isInLava()) {
            this.lavaHurt();
            this.fallDistance *= 0.5f;
        }
        this.checkBelowWorld();
        if (!this.level().isClientSide) {
            this.setSharedFlagOnFire(this.remainingFireTicks > 0);
        }
        this.firstTick = false;
        if (!this.level().isClientSide && this instanceof Leashable) {
            Leashable.tickLeash((Entity)((Object)((Leashable)((Object)this))));
        }
        this.level().getProfiler().pop();
    }

    public void setSharedFlagOnFire(boolean bl) {
        this.setSharedFlag(0, bl || this.hasVisualFire);
    }

    public void checkBelowWorld() {
        if (this.getY() < (double)(this.level().getMinBuildHeight() - 64)) {
            this.onBelowWorld();
        }
    }

    public void setPortalCooldown() {
        this.portalCooldown = this.getDimensionChangingDelay();
    }

    public void setPortalCooldown(int n) {
        this.portalCooldown = n;
    }

    public int getPortalCooldown() {
        return this.portalCooldown;
    }

    public boolean isOnPortalCooldown() {
        return this.portalCooldown > 0;
    }

    protected void processPortalCooldown() {
        if (this.isOnPortalCooldown()) {
            --this.portalCooldown;
        }
    }

    public void lavaHurt() {
        if (this.fireImmune()) {
            return;
        }
        this.igniteForSeconds(15.0f);
        if (this.hurt(this.damageSources().lava(), 4.0f)) {
            this.playSound(SoundEvents.GENERIC_BURN, 0.4f, 2.0f + this.random.nextFloat() * 0.4f);
        }
    }

    public final void igniteForSeconds(float f) {
        this.igniteForTicks(Mth.floor(f * 20.0f));
    }

    public void igniteForTicks(int n) {
        if (this.remainingFireTicks < n) {
            this.setRemainingFireTicks(n);
        }
    }

    public void setRemainingFireTicks(int n) {
        this.remainingFireTicks = n;
    }

    public int getRemainingFireTicks() {
        return this.remainingFireTicks;
    }

    public void clearFire() {
        this.setRemainingFireTicks(0);
    }

    protected void onBelowWorld() {
        this.discard();
    }

    public boolean isFree(double d, double d2, double d3) {
        return this.isFree(this.getBoundingBox().move(d, d2, d3));
    }

    private boolean isFree(AABB aABB) {
        return this.level().noCollision(this, aABB) && !this.level().containsAnyLiquid(aABB);
    }

    public void setOnGround(boolean bl) {
        this.onGround = bl;
        this.checkSupportingBlock(bl, null);
    }

    public void setOnGroundWithMovement(boolean bl, Vec3 vec3) {
        this.onGround = bl;
        this.checkSupportingBlock(bl, vec3);
    }

    public boolean isSupportedBy(BlockPos blockPos) {
        return this.mainSupportingBlockPos.isPresent() && this.mainSupportingBlockPos.get().equals(blockPos);
    }

    protected void checkSupportingBlock(boolean bl, @Nullable Vec3 vec3) {
        if (bl) {
            AABB aABB = this.getBoundingBox();
            AABB aABB2 = new AABB(aABB.minX, aABB.minY - 1.0E-6, aABB.minZ, aABB.maxX, aABB.minY, aABB.maxZ);
            Optional<BlockPos> optional = this.level.findSupportingBlock(this, aABB2);
            if (optional.isPresent() || this.onGroundNoBlocks) {
                this.mainSupportingBlockPos = optional;
            } else if (vec3 != null) {
                AABB aABB3 = aABB2.move(-vec3.x, 0.0, -vec3.z);
                optional = this.level.findSupportingBlock(this, aABB3);
                this.mainSupportingBlockPos = optional;
            }
            this.onGroundNoBlocks = optional.isEmpty();
        } else {
            this.onGroundNoBlocks = false;
            if (this.mainSupportingBlockPos.isPresent()) {
                this.mainSupportingBlockPos = Optional.empty();
            }
        }
    }

    public boolean onGround() {
        return this.onGround;
    }

    public void move(MoverType moverType, Vec3 vec3) {
        MovementEmission movementEmission;
        Object object;
        Vec3 vec32;
        double d;
        if (this.noPhysics) {
            this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
            return;
        }
        this.wasOnFire = this.isOnFire();
        if (moverType == MoverType.PISTON && (vec3 = this.limitPistonMovement(vec3)).equals(Vec3.ZERO)) {
            return;
        }
        this.level().getProfiler().push("move");
        if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
            vec3 = vec3.multiply(this.stuckSpeedMultiplier);
            this.stuckSpeedMultiplier = Vec3.ZERO;
            this.setDeltaMovement(Vec3.ZERO);
        }
        if ((d = (vec32 = this.collide(vec3 = this.maybeBackOffFromEdge(vec3, moverType))).lengthSqr()) > 1.0E-7) {
            BlockHitResult blockHitResult;
            if (this.fallDistance != 0.0f && d >= 1.0 && (blockHitResult = this.level().clip(new ClipContext(this.position(), this.position().add(vec32), ClipContext.Block.FALLDAMAGE_RESETTING, ClipContext.Fluid.WATER, this))).getType() != HitResult.Type.MISS) {
                this.resetFallDistance();
            }
            this.setPos(this.getX() + vec32.x, this.getY() + vec32.y, this.getZ() + vec32.z);
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("rest");
        boolean bl = !Mth.equal(vec3.x, vec32.x);
        boolean bl2 = !Mth.equal(vec3.z, vec32.z);
        this.horizontalCollision = bl || bl2;
        this.verticalCollision = vec3.y != vec32.y;
        this.verticalCollisionBelow = this.verticalCollision && vec3.y < 0.0;
        this.minorHorizontalCollision = this.horizontalCollision ? this.isHorizontalCollisionMinor(vec32) : false;
        this.setOnGroundWithMovement(this.verticalCollisionBelow, vec32);
        BlockPos blockPos = this.getOnPosLegacy();
        BlockState blockState2 = this.level().getBlockState(blockPos);
        this.checkFallDamage(vec32.y, this.onGround(), blockState2, blockPos);
        if (this.isRemoved()) {
            this.level().getProfiler().pop();
            return;
        }
        if (this.horizontalCollision) {
            object = this.getDeltaMovement();
            this.setDeltaMovement(bl ? 0.0 : ((Vec3)object).x, ((Vec3)object).y, bl2 ? 0.0 : ((Vec3)object).z);
        }
        object = blockState2.getBlock();
        if (vec3.y != vec32.y) {
            ((Block)object).updateEntityAfterFallOn(this.level(), this);
        }
        if (this.onGround()) {
            ((Block)object).stepOn(this.level(), blockPos, blockState2, this);
        }
        if ((movementEmission = this.getMovementEmission()).emitsAnything() && !this.isPassenger()) {
            double d2 = vec32.x;
            double d3 = vec32.y;
            double d4 = vec32.z;
            this.flyDist += (float)(vec32.length() * 0.6);
            BlockPos blockPos2 = this.getOnPos();
            BlockState blockState3 = this.level().getBlockState(blockPos2);
            boolean bl3 = this.isStateClimbable(blockState3);
            if (!bl3) {
                d3 = 0.0;
            }
            this.walkDist += (float)vec32.horizontalDistance() * 0.6f;
            this.moveDist += (float)Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4) * 0.6f;
            if (this.moveDist > this.nextStep && !blockState3.isAir()) {
                boolean bl4 = blockPos2.equals(blockPos);
                boolean bl5 = this.vibrationAndSoundEffectsFromBlock(blockPos, blockState2, movementEmission.emitsSounds(), bl4, vec3);
                if (!bl4) {
                    bl5 |= this.vibrationAndSoundEffectsFromBlock(blockPos2, blockState3, false, movementEmission.emitsEvents(), vec3);
                }
                if (bl5) {
                    this.nextStep = this.nextStep();
                } else if (this.isInWater()) {
                    this.nextStep = this.nextStep();
                    if (movementEmission.emitsSounds()) {
                        this.waterSwimSound();
                    }
                    if (movementEmission.emitsEvents()) {
                        this.gameEvent(GameEvent.SWIM);
                    }
                }
            } else if (blockState3.isAir()) {
                this.processFlappingMovement();
            }
        }
        this.tryCheckInsideBlocks();
        float f = this.getBlockSpeedFactor();
        this.setDeltaMovement(this.getDeltaMovement().multiply(f, 1.0, f));
        if (this.level().getBlockStatesIfLoaded(this.getBoundingBox().deflate(1.0E-6)).noneMatch(blockState -> blockState.is(BlockTags.FIRE) || blockState.is(Blocks.LAVA))) {
            if (this.remainingFireTicks <= 0) {
                this.setRemainingFireTicks(-this.getFireImmuneTicks());
            }
            if (this.wasOnFire && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
                this.playEntityOnFireExtinguishedSound();
            }
        }
        if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
            this.setRemainingFireTicks(-this.getFireImmuneTicks());
        }
        this.level().getProfiler().pop();
    }

    private boolean isStateClimbable(BlockState blockState) {
        return blockState.is(BlockTags.CLIMBABLE) || blockState.is(Blocks.POWDER_SNOW);
    }

    private boolean vibrationAndSoundEffectsFromBlock(BlockPos blockPos, BlockState blockState, boolean bl, boolean bl2, Vec3 vec3) {
        if (blockState.isAir()) {
            return false;
        }
        boolean bl3 = this.isStateClimbable(blockState);
        if ((this.onGround() || bl3 || this.isCrouching() && vec3.y == 0.0 || this.isOnRails()) && !this.isSwimming()) {
            if (bl) {
                this.walkingStepSound(blockPos, blockState);
            }
            if (bl2) {
                this.level().gameEvent(GameEvent.STEP, this.position(), GameEvent.Context.of(this, blockState));
            }
            return true;
        }
        return false;
    }

    protected boolean isHorizontalCollisionMinor(Vec3 vec3) {
        return false;
    }

    protected void tryCheckInsideBlocks() {
        try {
            this.checkInsideBlocks();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Checking entity block collision");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being checked for collision");
            this.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    protected void playEntityOnFireExtinguishedSound() {
        this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7f, 1.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
    }

    public void extinguishFire() {
        if (!this.level().isClientSide && this.wasOnFire) {
            this.playEntityOnFireExtinguishedSound();
        }
        this.clearFire();
    }

    protected void processFlappingMovement() {
        if (this.isFlapping()) {
            this.onFlap();
            if (this.getMovementEmission().emitsEvents()) {
                this.gameEvent(GameEvent.FLAP);
            }
        }
    }

    @Deprecated
    public BlockPos getOnPosLegacy() {
        return this.getOnPos(0.2f);
    }

    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.500001f);
    }

    public BlockPos getOnPos() {
        return this.getOnPos(1.0E-5f);
    }

    protected BlockPos getOnPos(float f) {
        if (this.mainSupportingBlockPos.isPresent()) {
            BlockPos blockPos = this.mainSupportingBlockPos.get();
            if (f > 1.0E-5f) {
                BlockState blockState = this.level().getBlockState(blockPos);
                if ((double)f <= 0.5 && blockState.is(BlockTags.FENCES) || blockState.is(BlockTags.WALLS) || blockState.getBlock() instanceof FenceGateBlock) {
                    return blockPos;
                }
                return blockPos.atY(Mth.floor(this.position.y - (double)f));
            }
            return blockPos;
        }
        int n = Mth.floor(this.position.x);
        int n2 = Mth.floor(this.position.y - (double)f);
        int n3 = Mth.floor(this.position.z);
        return new BlockPos(n, n2, n3);
    }

    protected float getBlockJumpFactor() {
        float f = this.level().getBlockState(this.blockPosition()).getBlock().getJumpFactor();
        float f2 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getJumpFactor();
        return (double)f == 1.0 ? f2 : f;
    }

    protected float getBlockSpeedFactor() {
        BlockState blockState = this.level().getBlockState(this.blockPosition());
        float f = blockState.getBlock().getSpeedFactor();
        if (blockState.is(Blocks.WATER) || blockState.is(Blocks.BUBBLE_COLUMN)) {
            return f;
        }
        return (double)f == 1.0 ? this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getSpeedFactor() : f;
    }

    protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType moverType) {
        return vec3;
    }

    protected Vec3 limitPistonMovement(Vec3 vec3) {
        if (vec3.lengthSqr() <= 1.0E-7) {
            return vec3;
        }
        long l = this.level().getGameTime();
        if (l != this.pistonDeltasGameTime) {
            Arrays.fill(this.pistonDeltas, 0.0);
            this.pistonDeltasGameTime = l;
        }
        if (vec3.x != 0.0) {
            double d = this.applyPistonMovementRestriction(Direction.Axis.X, vec3.x);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3.ZERO : new Vec3(d, 0.0, 0.0);
        }
        if (vec3.y != 0.0) {
            double d = this.applyPistonMovementRestriction(Direction.Axis.Y, vec3.y);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3.ZERO : new Vec3(0.0, d, 0.0);
        }
        if (vec3.z != 0.0) {
            double d = this.applyPistonMovementRestriction(Direction.Axis.Z, vec3.z);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3.ZERO : new Vec3(0.0, 0.0, d);
        }
        return Vec3.ZERO;
    }

    private double applyPistonMovementRestriction(Direction.Axis axis, double d) {
        int n = axis.ordinal();
        double d2 = Mth.clamp(d + this.pistonDeltas[n], -0.51, 0.51);
        d = d2 - this.pistonDeltas[n];
        this.pistonDeltas[n] = d2;
        return d;
    }

    private Vec3 collide(Vec3 vec3) {
        boolean bl;
        AABB aABB = this.getBoundingBox();
        List<VoxelShape> list = this.level().getEntityCollisions(this, aABB.expandTowards(vec3));
        Vec3 vec32 = vec3.lengthSqr() == 0.0 ? vec3 : Entity.collideBoundingBox(this, vec3, aABB, this.level(), list);
        boolean bl2 = vec3.x != vec32.x;
        boolean bl3 = vec3.y != vec32.y;
        boolean bl4 = vec3.z != vec32.z;
        boolean bl5 = bl = bl3 && vec3.y < 0.0;
        if (this.maxUpStep() > 0.0f && (bl || this.onGround()) && (bl2 || bl4)) {
            float[] fArray;
            AABB aABB2 = bl ? aABB.move(0.0, vec32.y, 0.0) : aABB;
            AABB aABB3 = aABB2.expandTowards(vec3.x, this.maxUpStep(), vec3.z);
            if (!bl) {
                aABB3 = aABB3.expandTowards(0.0, -1.0E-5f, 0.0);
            }
            List<VoxelShape> list2 = Entity.collectColliders(this, this.level, list, aABB3);
            float f = (float)vec32.y;
            for (float f2 : fArray = Entity.collectCandidateStepUpHeights(aABB2, list2, this.maxUpStep(), f)) {
                Vec3 vec33 = Entity.collideWithShapes(new Vec3(vec3.x, f2, vec3.z), aABB2, list2);
                if (!(vec33.horizontalDistanceSqr() > vec32.horizontalDistanceSqr())) continue;
                double d = aABB.minY - aABB2.minY;
                return vec33.add(0.0, -d, 0.0);
            }
        }
        return vec32;
    }

    private static float[] collectCandidateStepUpHeights(AABB aABB, List<VoxelShape> list, float f, float f2) {
        FloatArraySet floatArraySet = new FloatArraySet(4);
        block0: for (VoxelShape voxelShape : list) {
            DoubleList doubleList = voxelShape.getCoords(Direction.Axis.Y);
            DoubleListIterator doubleListIterator = doubleList.iterator();
            while (doubleListIterator.hasNext()) {
                double d = (Double)doubleListIterator.next();
                float f3 = (float)(d - aABB.minY);
                if (f3 < 0.0f || f3 == f2) continue;
                if (f3 > f) continue block0;
                floatArraySet.add(f3);
            }
        }
        Object object = floatArraySet.toFloatArray();
        FloatArrays.unstableSort((float[])object);
        return object;
    }

    public static Vec3 collideBoundingBox(@Nullable Entity entity, Vec3 vec3, AABB aABB, Level level, List<VoxelShape> list) {
        List<VoxelShape> list2 = Entity.collectColliders(entity, level, list, aABB.expandTowards(vec3));
        return Entity.collideWithShapes(vec3, aABB, list2);
    }

    private static List<VoxelShape> collectColliders(@Nullable Entity entity, Level level, List<VoxelShape> list, AABB aABB) {
        boolean bl;
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)(list.size() + 1));
        if (!list.isEmpty()) {
            builder.addAll(list);
        }
        WorldBorder worldBorder = level.getWorldBorder();
        boolean bl2 = bl = entity != null && worldBorder.isInsideCloseToBorder(entity, aABB);
        if (bl) {
            builder.add((Object)worldBorder.getCollisionShape());
        }
        builder.addAll(level.getBlockCollisions(entity, aABB));
        return builder.build();
    }

    private static Vec3 collideWithShapes(Vec3 vec3, AABB aABB, List<VoxelShape> list) {
        boolean bl;
        if (list.isEmpty()) {
            return vec3;
        }
        double d = vec3.x;
        double d2 = vec3.y;
        double d3 = vec3.z;
        if (d2 != 0.0 && (d2 = Shapes.collide(Direction.Axis.Y, aABB, list, d2)) != 0.0) {
            aABB = aABB.move(0.0, d2, 0.0);
        }
        boolean bl2 = bl = Math.abs(d) < Math.abs(d3);
        if (bl && d3 != 0.0 && (d3 = Shapes.collide(Direction.Axis.Z, aABB, list, d3)) != 0.0) {
            aABB = aABB.move(0.0, 0.0, d3);
        }
        if (d != 0.0) {
            d = Shapes.collide(Direction.Axis.X, aABB, list, d);
            if (!bl && d != 0.0) {
                aABB = aABB.move(d, 0.0, 0.0);
            }
        }
        if (!bl && d3 != 0.0) {
            d3 = Shapes.collide(Direction.Axis.Z, aABB, list, d3);
        }
        return new Vec3(d, d2, d3);
    }

    protected float nextStep() {
        return (int)this.moveDist + 1;
    }

    protected SoundEvent getSwimSound() {
        return SoundEvents.GENERIC_SWIM;
    }

    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.GENERIC_SPLASH;
    }

    protected SoundEvent getSwimHighSpeedSplashSound() {
        return SoundEvents.GENERIC_SPLASH;
    }

    protected void checkInsideBlocks() {
        AABB aABB = this.getBoundingBox();
        BlockPos blockPos = BlockPos.containing(aABB.minX + 1.0E-7, aABB.minY + 1.0E-7, aABB.minZ + 1.0E-7);
        BlockPos blockPos2 = BlockPos.containing(aABB.maxX - 1.0E-7, aABB.maxY - 1.0E-7, aABB.maxZ - 1.0E-7);
        if (this.level().hasChunksAt(blockPos, blockPos2)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int i = blockPos.getX(); i <= blockPos2.getX(); ++i) {
                for (int j = blockPos.getY(); j <= blockPos2.getY(); ++j) {
                    for (int k = blockPos.getZ(); k <= blockPos2.getZ(); ++k) {
                        if (!this.isAlive()) {
                            return;
                        }
                        mutableBlockPos.set(i, j, k);
                        BlockState blockState = this.level().getBlockState(mutableBlockPos);
                        try {
                            blockState.entityInside(this.level(), mutableBlockPos, this);
                            this.onInsideBlock(blockState);
                            continue;
                        }
                        catch (Throwable throwable) {
                            CrashReport crashReport = CrashReport.forThrowable(throwable, "Colliding entity with block");
                            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being collided with");
                            CrashReportCategory.populateBlockDetails(crashReportCategory, this.level(), mutableBlockPos, blockState);
                            throw new ReportedException(crashReport);
                        }
                    }
                }
            }
        }
    }

    protected void onInsideBlock(BlockState blockState) {
    }

    public BlockPos adjustSpawnLocation(ServerLevel serverLevel, BlockPos blockPos) {
        BlockPos blockPos2 = serverLevel.getSharedSpawnPos();
        Vec3 vec3 = blockPos2.getCenter();
        int n = serverLevel.getChunkAt(blockPos2).getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos2.getX(), blockPos2.getZ()) + 1;
        return BlockPos.containing(vec3.x, n, vec3.z);
    }

    public void gameEvent(Holder<GameEvent> holder, @Nullable Entity entity) {
        this.level().gameEvent(entity, holder, this.position);
    }

    public void gameEvent(Holder<GameEvent> holder) {
        this.gameEvent(holder, this);
    }

    private void walkingStepSound(BlockPos blockPos, BlockState blockState) {
        this.playStepSound(blockPos, blockState);
        if (this.shouldPlayAmethystStepSound(blockState)) {
            this.playAmethystStepSound();
        }
    }

    protected void waterSwimSound() {
        Entity entity = Objects.requireNonNullElse(this.getControllingPassenger(), this);
        float f = entity == this ? 0.35f : 0.4f;
        Vec3 vec3 = entity.getDeltaMovement();
        float f2 = Math.min(1.0f, (float)Math.sqrt(vec3.x * vec3.x * (double)0.2f + vec3.y * vec3.y + vec3.z * vec3.z * (double)0.2f) * f);
        this.playSwimSound(f2);
    }

    protected BlockPos getPrimaryStepSoundBlockPos(BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState = this.level().getBlockState(blockPos2);
        if (blockState.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS) || blockState.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
            return blockPos2;
        }
        return blockPos;
    }

    protected void playCombinationStepSounds(BlockState blockState, BlockState blockState2) {
        SoundType soundType = blockState.getSoundType();
        this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15f, soundType.getPitch());
        this.playMuffledStepSound(blockState2);
    }

    protected void playMuffledStepSound(BlockState blockState) {
        SoundType soundType = blockState.getSoundType();
        this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.05f, soundType.getPitch() * 0.8f);
    }

    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        SoundType soundType = blockState.getSoundType();
        this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15f, soundType.getPitch());
    }

    private boolean shouldPlayAmethystStepSound(BlockState blockState) {
        return blockState.is(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.tickCount >= this.lastCrystalSoundPlayTick + 20;
    }

    private void playAmethystStepSound() {
        this.crystalSoundIntensity *= (float)Math.pow(0.997, this.tickCount - this.lastCrystalSoundPlayTick);
        this.crystalSoundIntensity = Math.min(1.0f, this.crystalSoundIntensity + 0.07f);
        float f = 0.5f + this.crystalSoundIntensity * this.random.nextFloat() * 1.2f;
        float f2 = 0.1f + this.crystalSoundIntensity * 1.2f;
        this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, f2, f);
        this.lastCrystalSoundPlayTick = this.tickCount;
    }

    protected void playSwimSound(float f) {
        this.playSound(this.getSwimSound(), f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
    }

    protected void onFlap() {
    }

    protected boolean isFlapping() {
        return false;
    }

    public void playSound(SoundEvent soundEvent, float f, float f2) {
        if (!this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, f2);
        }
    }

    public void playSound(SoundEvent soundEvent) {
        if (!this.isSilent()) {
            this.playSound(soundEvent, 1.0f, 1.0f);
        }
    }

    public boolean isSilent() {
        return this.entityData.get(DATA_SILENT);
    }

    public void setSilent(boolean bl) {
        this.entityData.set(DATA_SILENT, bl);
    }

    public boolean isNoGravity() {
        return this.entityData.get(DATA_NO_GRAVITY);
    }

    public void setNoGravity(boolean bl) {
        this.entityData.set(DATA_NO_GRAVITY, bl);
    }

    protected double getDefaultGravity() {
        return 0.0;
    }

    public final double getGravity() {
        return this.isNoGravity() ? 0.0 : this.getDefaultGravity();
    }

    protected void applyGravity() {
        double d = this.getGravity();
        if (d != 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d, 0.0));
        }
    }

    protected MovementEmission getMovementEmission() {
        return MovementEmission.ALL;
    }

    public boolean dampensVibrations() {
        return false;
    }

    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos2) {
        if (bl) {
            if (this.fallDistance > 0.0f) {
                blockState.getBlock().fallOn(this.level(), blockState, blockPos2, this, this.fallDistance);
                this.level().gameEvent(GameEvent.HIT_GROUND, this.position, GameEvent.Context.of(this, this.mainSupportingBlockPos.map(blockPos -> this.level().getBlockState((BlockPos)blockPos)).orElse(blockState)));
            }
            this.resetFallDistance();
        } else if (d < 0.0) {
            this.fallDistance -= (float)d;
        }
    }

    public boolean fireImmune() {
        return this.getType().fireImmune();
    }

    public boolean causeFallDamage(float f, float f2, DamageSource damageSource) {
        if (this.type.is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return false;
        }
        if (this.isVehicle()) {
            for (Entity entity : this.getPassengers()) {
                entity.causeFallDamage(f, f2, damageSource);
            }
        }
        return false;
    }

    public boolean isInWater() {
        return this.wasTouchingWater;
    }

    private boolean isInRain() {
        BlockPos blockPos = this.blockPosition();
        return this.level().isRainingAt(blockPos) || this.level().isRainingAt(BlockPos.containing(blockPos.getX(), this.getBoundingBox().maxY, blockPos.getZ()));
    }

    private boolean isInBubbleColumn() {
        return this.getInBlockState().is(Blocks.BUBBLE_COLUMN);
    }

    public boolean isInWaterOrRain() {
        return this.isInWater() || this.isInRain();
    }

    public boolean isInWaterRainOrBubble() {
        return this.isInWater() || this.isInRain() || this.isInBubbleColumn();
    }

    public boolean isInWaterOrBubble() {
        return this.isInWater() || this.isInBubbleColumn();
    }

    public boolean isInLiquid() {
        return this.isInWaterOrBubble() || this.isInLava();
    }

    public boolean isUnderWater() {
        return this.wasEyeInWater && this.isInWater();
    }

    public void updateSwimming() {
        if (this.isSwimming()) {
            this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
        } else {
            this.setSwimming(this.isSprinting() && this.isUnderWater() && !this.isPassenger() && this.level().getFluidState(this.blockPosition).is(FluidTags.WATER));
        }
    }

    protected boolean updateInWaterStateAndDoFluidPushing() {
        this.fluidHeight.clear();
        this.updateInWaterStateAndDoWaterCurrentPushing();
        double d = this.level().dimensionType().ultraWarm() ? 0.007 : 0.0023333333333333335;
        boolean bl = this.updateFluidHeightAndDoFluidPushing(FluidTags.LAVA, d);
        return this.isInWater() || bl;
    }

    void updateInWaterStateAndDoWaterCurrentPushing() {
        Boat boat;
        Entity entity = this.getVehicle();
        if (entity instanceof Boat && !(boat = (Boat)entity).isUnderWater()) {
            this.wasTouchingWater = false;
        } else if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014)) {
            if (!this.wasTouchingWater && !this.firstTick) {
                this.doWaterSplashEffect();
            }
            this.resetFallDistance();
            this.wasTouchingWater = true;
            this.clearFire();
        } else {
            this.wasTouchingWater = false;
        }
    }

    private void updateFluidOnEyes() {
        Object object;
        this.wasEyeInWater = this.isEyeInFluid(FluidTags.WATER);
        this.fluidOnEyes.clear();
        double d = this.getEyeY();
        Entity entity = this.getVehicle();
        if (entity instanceof Boat && !((Boat)(object = (Boat)entity)).isUnderWater() && ((Entity)object).getBoundingBox().maxY >= d && ((Entity)object).getBoundingBox().minY <= d) {
            return;
        }
        object = BlockPos.containing(this.getX(), d, this.getZ());
        FluidState fluidState = this.level().getFluidState((BlockPos)object);
        double d2 = (float)((Vec3i)object).getY() + fluidState.getHeight(this.level(), (BlockPos)object);
        if (d2 > d) {
            fluidState.getTags().forEach(this.fluidOnEyes::add);
        }
    }

    protected void doWaterSplashEffect() {
        double d;
        double d2;
        Entity entity = Objects.requireNonNullElse(this.getControllingPassenger(), this);
        float f = entity == this ? 0.2f : 0.9f;
        Vec3 vec3 = entity.getDeltaMovement();
        float f2 = Math.min(1.0f, (float)Math.sqrt(vec3.x * vec3.x * (double)0.2f + vec3.y * vec3.y + vec3.z * vec3.z * (double)0.2f) * f);
        if (f2 < 0.25f) {
            this.playSound(this.getSwimSplashSound(), f2, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        } else {
            this.playSound(this.getSwimHighSpeedSplashSound(), f2, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        }
        float f3 = Mth.floor(this.getY());
        int n = 0;
        while ((float)n < 1.0f + this.dimensions.width() * 20.0f) {
            d2 = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d2, f3 + 1.0f, this.getZ() + d, vec3.x, vec3.y - this.random.nextDouble() * (double)0.2f, vec3.z);
            ++n;
        }
        n = 0;
        while ((float)n < 1.0f + this.dimensions.width() * 20.0f) {
            d2 = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            this.level().addParticle(ParticleTypes.SPLASH, this.getX() + d2, f3 + 1.0f, this.getZ() + d, vec3.x, vec3.y, vec3.z);
            ++n;
        }
        this.gameEvent(GameEvent.SPLASH);
    }

    @Deprecated
    protected BlockState getBlockStateOnLegacy() {
        return this.level().getBlockState(this.getOnPosLegacy());
    }

    public BlockState getBlockStateOn() {
        return this.level().getBlockState(this.getOnPos());
    }

    public boolean canSpawnSprintParticle() {
        return this.isSprinting() && !this.isInWater() && !this.isSpectator() && !this.isCrouching() && !this.isInLava() && this.isAlive();
    }

    protected void spawnSprintParticle() {
        BlockPos blockPos = this.getOnPosLegacy();
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            Vec3 vec3 = this.getDeltaMovement();
            BlockPos blockPos2 = this.blockPosition();
            double d = this.getX() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
            double d2 = this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
            if (blockPos2.getX() != blockPos.getX()) {
                d = Mth.clamp(d, (double)blockPos.getX(), (double)blockPos.getX() + 1.0);
            }
            if (blockPos2.getZ() != blockPos.getZ()) {
                d2 = Mth.clamp(d2, (double)blockPos.getZ(), (double)blockPos.getZ() + 1.0);
            }
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d, this.getY() + 0.1, d2, vec3.x * -4.0, 1.5, vec3.z * -4.0);
        }
    }

    public boolean isEyeInFluid(TagKey<Fluid> tagKey) {
        return this.fluidOnEyes.contains(tagKey);
    }

    public boolean isInLava() {
        return !this.firstTick && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
    }

    public void moveRelative(float f, Vec3 vec3) {
        Vec3 vec32 = Entity.getInputVector(vec3, f, this.getYRot());
        this.setDeltaMovement(this.getDeltaMovement().add(vec32));
    }

    private static Vec3 getInputVector(Vec3 vec3, float f, float f2) {
        double d = vec3.lengthSqr();
        if (d < 1.0E-7) {
            return Vec3.ZERO;
        }
        Vec3 vec32 = (d > 1.0 ? vec3.normalize() : vec3).scale(f);
        float f3 = Mth.sin(f2 * ((float)Math.PI / 180));
        float f4 = Mth.cos(f2 * ((float)Math.PI / 180));
        return new Vec3(vec32.x * (double)f4 - vec32.z * (double)f3, vec32.y, vec32.z * (double)f4 + vec32.x * (double)f3);
    }

    @Deprecated
    public float getLightLevelDependentMagicValue() {
        if (this.level().hasChunkAt(this.getBlockX(), this.getBlockZ())) {
            return this.level().getLightLevelDependentMagicValue(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ()));
        }
        return 0.0f;
    }

    public void absMoveTo(double d, double d2, double d3, float f, float f2) {
        this.absMoveTo(d, d2, d3);
        this.absRotateTo(f, f2);
    }

    public void absRotateTo(float f, float f2) {
        this.setYRot(f % 360.0f);
        this.setXRot(Mth.clamp(f2, -90.0f, 90.0f) % 360.0f);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void absMoveTo(double d, double d2, double d3) {
        double d4 = Mth.clamp(d, -3.0E7, 3.0E7);
        double d5 = Mth.clamp(d3, -3.0E7, 3.0E7);
        this.xo = d4;
        this.yo = d2;
        this.zo = d5;
        this.setPos(d4, d2, d5);
    }

    public void moveTo(Vec3 vec3) {
        this.moveTo(vec3.x, vec3.y, vec3.z);
    }

    public void moveTo(double d, double d2, double d3) {
        this.moveTo(d, d2, d3, this.getYRot(), this.getXRot());
    }

    public void moveTo(BlockPos blockPos, float f, float f2) {
        this.moveTo(blockPos.getBottomCenter(), f, f2);
    }

    public void moveTo(Vec3 vec3, float f, float f2) {
        this.moveTo(vec3.x, vec3.y, vec3.z, f, f2);
    }

    public void moveTo(double d, double d2, double d3, float f, float f2) {
        this.setPosRaw(d, d2, d3);
        this.setYRot(f);
        this.setXRot(f2);
        this.setOldPosAndRot();
        this.reapplyPosition();
    }

    public final void setOldPosAndRot() {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
        this.xOld = d;
        this.yOld = d2;
        this.zOld = d3;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public float distanceTo(Entity entity) {
        float f = (float)(this.getX() - entity.getX());
        float f2 = (float)(this.getY() - entity.getY());
        float f3 = (float)(this.getZ() - entity.getZ());
        return Mth.sqrt(f * f + f2 * f2 + f3 * f3);
    }

    public double distanceToSqr(double d, double d2, double d3) {
        double d4 = this.getX() - d;
        double d5 = this.getY() - d2;
        double d6 = this.getZ() - d3;
        return d4 * d4 + d5 * d5 + d6 * d6;
    }

    public double distanceToSqr(Entity entity) {
        return this.distanceToSqr(entity.position());
    }

    public double distanceToSqr(Vec3 vec3) {
        double d = this.getX() - vec3.x;
        double d2 = this.getY() - vec3.y;
        double d3 = this.getZ() - vec3.z;
        return d * d + d2 * d2 + d3 * d3;
    }

    public void playerTouch(Player player) {
    }

    public void push(Entity entity) {
        double d;
        if (this.isPassengerOfSameVehicle(entity)) {
            return;
        }
        if (entity.noPhysics || this.noPhysics) {
            return;
        }
        double d2 = entity.getX() - this.getX();
        double d3 = Mth.absMax(d2, d = entity.getZ() - this.getZ());
        if (d3 >= (double)0.01f) {
            d3 = Math.sqrt(d3);
            d2 /= d3;
            d /= d3;
            double d4 = 1.0 / d3;
            if (d4 > 1.0) {
                d4 = 1.0;
            }
            d2 *= d4;
            d *= d4;
            d2 *= (double)0.05f;
            d *= (double)0.05f;
            if (!this.isVehicle() && this.isPushable()) {
                this.push(-d2, 0.0, -d);
            }
            if (!entity.isVehicle() && entity.isPushable()) {
                entity.push(d2, 0.0, d);
            }
        }
    }

    public void push(Vec3 vec3) {
        this.push(vec3.x, vec3.y, vec3.z);
    }

    public void push(double d, double d2, double d3) {
        this.setDeltaMovement(this.getDeltaMovement().add(d, d2, d3));
        this.hasImpulse = true;
    }

    protected void markHurt() {
        this.hurtMarked = true;
    }

    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        this.markHurt();
        return false;
    }

    public final Vec3 getViewVector(float f) {
        return this.calculateViewVector(this.getViewXRot(f), this.getViewYRot(f));
    }

    public Direction getNearestViewDirection() {
        return Direction.getNearest(this.getViewVector(1.0f));
    }

    public float getViewXRot(float f) {
        if (f == 1.0f) {
            return this.getXRot();
        }
        return Mth.lerp(f, this.xRotO, this.getXRot());
    }

    public float getViewYRot(float f) {
        if (f == 1.0f) {
            return this.getYRot();
        }
        return Mth.lerp(f, this.yRotO, this.getYRot());
    }

    public final Vec3 calculateViewVector(float f, float f2) {
        float f3 = f * ((float)Math.PI / 180);
        float f4 = -f2 * ((float)Math.PI / 180);
        float f5 = Mth.cos(f4);
        float f6 = Mth.sin(f4);
        float f7 = Mth.cos(f3);
        float f8 = Mth.sin(f3);
        return new Vec3(f6 * f7, -f8, f5 * f7);
    }

    public final Vec3 getUpVector(float f) {
        return this.calculateUpVector(this.getViewXRot(f), this.getViewYRot(f));
    }

    protected final Vec3 calculateUpVector(float f, float f2) {
        return this.calculateViewVector(f - 90.0f, f2);
    }

    public final Vec3 getEyePosition() {
        return new Vec3(this.getX(), this.getEyeY(), this.getZ());
    }

    public final Vec3 getEyePosition(float f) {
        double d = Mth.lerp((double)f, this.xo, this.getX());
        double d2 = Mth.lerp((double)f, this.yo, this.getY()) + (double)this.getEyeHeight();
        double d3 = Mth.lerp((double)f, this.zo, this.getZ());
        return new Vec3(d, d2, d3);
    }

    public Vec3 getLightProbePosition(float f) {
        return this.getEyePosition(f);
    }

    public final Vec3 getPosition(float f) {
        double d = Mth.lerp((double)f, this.xo, this.getX());
        double d2 = Mth.lerp((double)f, this.yo, this.getY());
        double d3 = Mth.lerp((double)f, this.zo, this.getZ());
        return new Vec3(d, d2, d3);
    }

    public HitResult pick(double d, float f, boolean bl) {
        Vec3 vec3 = this.getEyePosition(f);
        Vec3 vec32 = this.getViewVector(f);
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        return this.level().clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this));
    }

    public boolean canBeHitByProjectile() {
        return this.isAlive() && this.isPickable();
    }

    public boolean isPickable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public void awardKillScore(Entity entity, int n, DamageSource damageSource) {
        if (entity instanceof ServerPlayer) {
            CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((ServerPlayer)entity, this, damageSource);
        }
    }

    public boolean shouldRender(double d, double d2, double d3) {
        double d4 = this.getX() - d;
        double d5 = this.getY() - d2;
        double d6 = this.getZ() - d3;
        double d7 = d4 * d4 + d5 * d5 + d6 * d6;
        return this.shouldRenderAtSqrDistance(d7);
    }

    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = this.getBoundingBox().getSize();
        if (Double.isNaN(d2)) {
            d2 = 1.0;
        }
        return d < (d2 *= 64.0 * viewScale) * d2;
    }

    public boolean saveAsPassenger(CompoundTag compoundTag) {
        if (this.removalReason != null && !this.removalReason.shouldSave()) {
            return false;
        }
        String string = this.getEncodeId();
        if (string == null) {
            return false;
        }
        compoundTag.putString(ID_TAG, string);
        this.saveWithoutId(compoundTag);
        return true;
    }

    public boolean save(CompoundTag compoundTag) {
        if (this.isPassenger()) {
            return false;
        }
        return this.saveAsPassenger(compoundTag);
    }

    public CompoundTag saveWithoutId(CompoundTag compoundTag) {
        try {
            ListTag listTag;
            int n;
            if (this.vehicle != null) {
                compoundTag.put("Pos", this.newDoubleList(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
            } else {
                compoundTag.put("Pos", this.newDoubleList(this.getX(), this.getY(), this.getZ()));
            }
            Vec3 vec3 = this.getDeltaMovement();
            compoundTag.put("Motion", this.newDoubleList(vec3.x, vec3.y, vec3.z));
            compoundTag.put("Rotation", this.newFloatList(this.getYRot(), this.getXRot()));
            compoundTag.putFloat("FallDistance", this.fallDistance);
            compoundTag.putShort("Fire", (short)this.remainingFireTicks);
            compoundTag.putShort("Air", (short)this.getAirSupply());
            compoundTag.putBoolean("OnGround", this.onGround());
            compoundTag.putBoolean("Invulnerable", this.invulnerable);
            compoundTag.putInt("PortalCooldown", this.portalCooldown);
            compoundTag.putUUID(UUID_TAG, this.getUUID());
            Component component = this.getCustomName();
            if (component != null) {
                compoundTag.putString("CustomName", Component.Serializer.toJson(component, this.registryAccess()));
            }
            if (this.isCustomNameVisible()) {
                compoundTag.putBoolean("CustomNameVisible", this.isCustomNameVisible());
            }
            if (this.isSilent()) {
                compoundTag.putBoolean("Silent", this.isSilent());
            }
            if (this.isNoGravity()) {
                compoundTag.putBoolean("NoGravity", this.isNoGravity());
            }
            if (this.hasGlowingTag) {
                compoundTag.putBoolean("Glowing", true);
            }
            if ((n = this.getTicksFrozen()) > 0) {
                compoundTag.putInt("TicksFrozen", this.getTicksFrozen());
            }
            if (this.hasVisualFire) {
                compoundTag.putBoolean("HasVisualFire", this.hasVisualFire);
            }
            if (!this.tags.isEmpty()) {
                listTag = new ListTag();
                for (String object : this.tags) {
                    listTag.add(StringTag.valueOf(object));
                }
                compoundTag.put("Tags", listTag);
            }
            this.addAdditionalSaveData(compoundTag);
            if (this.isVehicle()) {
                listTag = new ListTag();
                for (Entity entity : this.getPassengers()) {
                    CompoundTag compoundTag2;
                    if (!entity.saveAsPassenger(compoundTag2 = new CompoundTag())) continue;
                    listTag.add(compoundTag2);
                }
                if (!listTag.isEmpty()) {
                    compoundTag.put(PASSENGERS_TAG, listTag);
                }
            }
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Saving entity NBT");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being saved");
            this.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
        return compoundTag;
    }

    public void load(CompoundTag compoundTag) {
        try {
            Object object;
            ListTag listTag = compoundTag.getList("Pos", 6);
            ListTag listTag2 = compoundTag.getList("Motion", 6);
            ListTag listTag3 = compoundTag.getList("Rotation", 5);
            double d = listTag2.getDouble(0);
            double d2 = listTag2.getDouble(1);
            double d3 = listTag2.getDouble(2);
            this.setDeltaMovement(Math.abs(d) > 10.0 ? 0.0 : d, Math.abs(d2) > 10.0 ? 0.0 : d2, Math.abs(d3) > 10.0 ? 0.0 : d3);
            double d4 = 3.0000512E7;
            this.setPosRaw(Mth.clamp(listTag.getDouble(0), -3.0000512E7, 3.0000512E7), Mth.clamp(listTag.getDouble(1), -2.0E7, 2.0E7), Mth.clamp(listTag.getDouble(2), -3.0000512E7, 3.0000512E7));
            this.setYRot(listTag3.getFloat(0));
            this.setXRot(listTag3.getFloat(1));
            this.setOldPosAndRot();
            this.setYHeadRot(this.getYRot());
            this.setYBodyRot(this.getYRot());
            this.fallDistance = compoundTag.getFloat("FallDistance");
            this.remainingFireTicks = compoundTag.getShort("Fire");
            if (compoundTag.contains("Air")) {
                this.setAirSupply(compoundTag.getShort("Air"));
            }
            this.onGround = compoundTag.getBoolean("OnGround");
            this.invulnerable = compoundTag.getBoolean("Invulnerable");
            this.portalCooldown = compoundTag.getInt("PortalCooldown");
            if (compoundTag.hasUUID(UUID_TAG)) {
                this.uuid = compoundTag.getUUID(UUID_TAG);
                this.stringUUID = this.uuid.toString();
            }
            if (!(Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ()))) {
                throw new IllegalStateException("Entity has invalid position");
            }
            if (!Double.isFinite(this.getYRot()) || !Double.isFinite(this.getXRot())) {
                throw new IllegalStateException("Entity has invalid rotation");
            }
            this.reapplyPosition();
            this.setRot(this.getYRot(), this.getXRot());
            if (compoundTag.contains("CustomName", 8)) {
                object = compoundTag.getString("CustomName");
                try {
                    this.setCustomName(Component.Serializer.fromJson((String)object, (HolderLookup.Provider)this.registryAccess()));
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to parse entity custom name {}", object, (Object)exception);
                }
            }
            this.setCustomNameVisible(compoundTag.getBoolean("CustomNameVisible"));
            this.setSilent(compoundTag.getBoolean("Silent"));
            this.setNoGravity(compoundTag.getBoolean("NoGravity"));
            this.setGlowingTag(compoundTag.getBoolean("Glowing"));
            this.setTicksFrozen(compoundTag.getInt("TicksFrozen"));
            this.hasVisualFire = compoundTag.getBoolean("HasVisualFire");
            if (compoundTag.contains("Tags", 9)) {
                this.tags.clear();
                object = compoundTag.getList("Tags", 8);
                int n = Math.min(((ListTag)object).size(), 1024);
                for (int i = 0; i < n; ++i) {
                    this.tags.add(((ListTag)object).getString(i));
                }
            }
            this.readAdditionalSaveData(compoundTag);
            if (this.repositionEntityAfterLoad()) {
                this.reapplyPosition();
            }
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Loading entity NBT");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being loaded");
            this.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    protected boolean repositionEntityAfterLoad() {
        return true;
    }

    @Nullable
    protected final String getEncodeId() {
        EntityType<?> entityType = this.getType();
        ResourceLocation resourceLocation = EntityType.getKey(entityType);
        return !entityType.canSerialize() || resourceLocation == null ? null : resourceLocation.toString();
    }

    protected abstract void readAdditionalSaveData(CompoundTag var1);

    protected abstract void addAdditionalSaveData(CompoundTag var1);

    protected ListTag newDoubleList(double ... dArray) {
        ListTag listTag = new ListTag();
        for (double d : dArray) {
            listTag.add(DoubleTag.valueOf(d));
        }
        return listTag;
    }

    protected ListTag newFloatList(float ... fArray) {
        ListTag listTag = new ListTag();
        for (float f : fArray) {
            listTag.add(FloatTag.valueOf(f));
        }
        return listTag;
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemLike itemLike) {
        return this.spawnAtLocation(itemLike, 0);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemLike itemLike, int n) {
        return this.spawnAtLocation(new ItemStack(itemLike), (float)n);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemStack itemStack) {
        return this.spawnAtLocation(itemStack, 0.0f);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemStack itemStack, float f) {
        if (itemStack.isEmpty()) {
            return null;
        }
        if (this.level().isClientSide) {
            return null;
        }
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + (double)f, this.getZ(), itemStack);
        itemEntity.setDefaultPickUpDelay();
        this.level().addFreshEntity(itemEntity);
        return itemEntity;
    }

    public boolean isAlive() {
        return !this.isRemoved();
    }

    public boolean isInWall() {
        if (this.noPhysics) {
            return false;
        }
        float f = this.dimensions.width() * 0.8f;
        AABB aABB = AABB.ofSize(this.getEyePosition(), f, 1.0E-6, f);
        return BlockPos.betweenClosedStream(aABB).anyMatch(blockPos -> {
            BlockState blockState = this.level().getBlockState((BlockPos)blockPos);
            return !blockState.isAir() && blockState.isSuffocating(this.level(), (BlockPos)blockPos) && Shapes.joinIsNotEmpty(blockState.getCollisionShape(this.level(), (BlockPos)blockPos).move(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Shapes.create(aABB), BooleanOp.AND);
        });
    }

    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        Object object;
        if (this.isAlive() && (object = this) instanceof Leashable) {
            Leashable leashable = (Leashable)object;
            if (leashable.getLeashHolder() == player) {
                if (!this.level().isClientSide()) {
                    leashable.dropLeash(true, !player.hasInfiniteMaterials());
                    this.gameEvent(GameEvent.ENTITY_INTERACT, player);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            object = player.getItemInHand(interactionHand);
            if (((ItemStack)object).is(Items.LEAD) && leashable.canHaveALeashAttachedToIt()) {
                if (!this.level().isClientSide()) {
                    leashable.setLeashedTo(player, true);
                }
                ((ItemStack)object).shrink(1);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public boolean canCollideWith(Entity entity) {
        return entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity);
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public void rideTick() {
        this.setDeltaMovement(Vec3.ZERO);
        this.tick();
        if (!this.isPassenger()) {
            return;
        }
        this.getVehicle().positionRider(this);
    }

    public final void positionRider(Entity entity) {
        if (!this.hasPassenger(entity)) {
            return;
        }
        this.positionRider(entity, Entity::setPos);
    }

    protected void positionRider(Entity entity, MoveFunction moveFunction) {
        Vec3 vec3 = this.getPassengerRidingPosition(entity);
        Vec3 vec32 = entity.getVehicleAttachmentPoint(this);
        moveFunction.accept(entity, vec3.x - vec32.x, vec3.y - vec32.y, vec3.z - vec32.z);
    }

    public void onPassengerTurned(Entity entity) {
    }

    public Vec3 getVehicleAttachmentPoint(Entity entity) {
        return this.getAttachments().get(EntityAttachment.VEHICLE, 0, this.yRot);
    }

    public Vec3 getPassengerRidingPosition(Entity entity) {
        return this.position().add(this.getPassengerAttachmentPoint(entity, this.dimensions, 1.0f));
    }

    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        return Entity.getDefaultPassengerAttachmentPoint(this, entity, entityDimensions.attachments());
    }

    protected static Vec3 getDefaultPassengerAttachmentPoint(Entity entity, Entity entity2, EntityAttachments entityAttachments) {
        int n = entity.getPassengers().indexOf(entity2);
        return entityAttachments.getClamped(EntityAttachment.PASSENGER, n, entity.yRot);
    }

    public boolean startRiding(Entity entity) {
        return this.startRiding(entity, false);
    }

    public boolean showVehicleHealth() {
        return this instanceof LivingEntity;
    }

    public boolean startRiding(Entity entity2, boolean bl) {
        if (entity2 == this.vehicle) {
            return false;
        }
        if (!entity2.couldAcceptPassenger()) {
            return false;
        }
        Entity entity3 = entity2;
        while (entity3.vehicle != null) {
            if (entity3.vehicle == this) {
                return false;
            }
            entity3 = entity3.vehicle;
        }
        if (!(bl || this.canRide(entity2) && entity2.canAddPassenger(this))) {
            return false;
        }
        if (this.isPassenger()) {
            this.stopRiding();
        }
        this.setPose(Pose.STANDING);
        this.vehicle = entity2;
        this.vehicle.addPassenger(this);
        entity2.getIndirectPassengersStream().filter(entity -> entity instanceof ServerPlayer).forEach(entity -> CriteriaTriggers.START_RIDING_TRIGGER.trigger((ServerPlayer)entity));
        return true;
    }

    protected boolean canRide(Entity entity) {
        return !this.isShiftKeyDown() && this.boardingCooldown <= 0;
    }

    public void ejectPassengers() {
        for (int i = this.passengers.size() - 1; i >= 0; --i) {
            ((Entity)this.passengers.get(i)).stopRiding();
        }
    }

    public void removeVehicle() {
        if (this.vehicle != null) {
            Entity entity = this.vehicle;
            this.vehicle = null;
            entity.removePassenger(this);
        }
    }

    public void stopRiding() {
        this.removeVehicle();
    }

    protected void addPassenger(Entity entity) {
        if (entity.getVehicle() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        }
        if (this.passengers.isEmpty()) {
            this.passengers = ImmutableList.of((Object)entity);
        } else {
            ArrayList arrayList = Lists.newArrayList(this.passengers);
            if (!this.level().isClientSide && entity instanceof Player && !(this.getFirstPassenger() instanceof Player)) {
                arrayList.add(0, entity);
            } else {
                arrayList.add(entity);
            }
            this.passengers = ImmutableList.copyOf((Collection)arrayList);
        }
        this.gameEvent(GameEvent.ENTITY_MOUNT, entity);
    }

    protected void removePassenger(Entity entity) {
        if (entity.getVehicle() == this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        }
        this.passengers = this.passengers.size() == 1 && this.passengers.get(0) == entity ? ImmutableList.of() : (ImmutableList)this.passengers.stream().filter(entity2 -> entity2 != entity).collect(ImmutableList.toImmutableList());
        entity.boardingCooldown = 60;
        this.gameEvent(GameEvent.ENTITY_DISMOUNT, entity);
    }

    protected boolean canAddPassenger(Entity entity) {
        return this.passengers.isEmpty();
    }

    protected boolean couldAcceptPassenger() {
        return true;
    }

    public void lerpTo(double d, double d2, double d3, float f, float f2, int n) {
        this.setPos(d, d2, d3);
        this.setRot(f, f2);
    }

    public double lerpTargetX() {
        return this.getX();
    }

    public double lerpTargetY() {
        return this.getY();
    }

    public double lerpTargetZ() {
        return this.getZ();
    }

    public float lerpTargetXRot() {
        return this.getXRot();
    }

    public float lerpTargetYRot() {
        return this.getYRot();
    }

    public void lerpHeadTo(float f, int n) {
        this.setYHeadRot(f);
    }

    public float getPickRadius() {
        return 0.0f;
    }

    public Vec3 getLookAngle() {
        return this.calculateViewVector(this.getXRot(), this.getYRot());
    }

    public Vec3 getHandHoldingItemAngle(Item item) {
        Entity entity = this;
        if (entity instanceof Player) {
            Player player = (Player)entity;
            boolean bl = player.getOffhandItem().is(item) && !player.getMainHandItem().is(item);
            HumanoidArm humanoidArm = bl ? player.getMainArm().getOpposite() : player.getMainArm();
            return this.calculateViewVector(0.0f, this.getYRot() + (float)(humanoidArm == HumanoidArm.RIGHT ? 80 : -80)).scale(0.5);
        }
        return Vec3.ZERO;
    }

    public Vec2 getRotationVector() {
        return new Vec2(this.getXRot(), this.getYRot());
    }

    public Vec3 getForward() {
        return Vec3.directionFromRotation(this.getRotationVector());
    }

    public void setAsInsidePortal(Portal portal, BlockPos blockPos) {
        if (this.isOnPortalCooldown()) {
            this.setPortalCooldown();
            return;
        }
        if (this.portalProcess == null || !this.portalProcess.isSamePortal(portal)) {
            this.portalProcess = new PortalProcessor(portal, blockPos.immutable());
        } else {
            this.portalProcess.updateEntryPosition(blockPos.immutable());
            this.portalProcess.setAsInsidePortalThisTick(true);
        }
    }

    protected void handlePortal() {
        Object object = this.level();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        this.processPortalCooldown();
        if (this.portalProcess == null) {
            return;
        }
        if (this.portalProcess.processPortalTeleportation(serverLevel, this, this.canUsePortal(false))) {
            serverLevel.getProfiler().push("portal");
            this.setPortalCooldown();
            object = this.portalProcess.getPortalDestination(serverLevel, this);
            if (object != null) {
                ServerLevel serverLevel2 = ((DimensionTransition)object).newLevel();
                if (serverLevel.getServer().isLevelEnabled(serverLevel2) && (serverLevel2.dimension() == serverLevel.dimension() || this.canChangeDimensions(serverLevel, serverLevel2))) {
                    this.changeDimension((DimensionTransition)object);
                }
            }
            serverLevel.getProfiler().pop();
        } else if (this.portalProcess.hasExpired()) {
            this.portalProcess = null;
        }
    }

    public int getDimensionChangingDelay() {
        Entity entity = this.getFirstPassenger();
        return entity instanceof ServerPlayer ? entity.getDimensionChangingDelay() : 300;
    }

    public void lerpMotion(double d, double d2, double d3) {
        this.setDeltaMovement(d, d2, d3);
    }

    public void handleDamageEvent(DamageSource damageSource) {
    }

    public void handleEntityEvent(byte by) {
        switch (by) {
            case 53: {
                HoneyBlock.showSlideParticles(this);
            }
        }
    }

    public void animateHurt(float f) {
    }

    public boolean isOnFire() {
        boolean bl = this.level() != null && this.level().isClientSide;
        return !this.fireImmune() && (this.remainingFireTicks > 0 || bl && this.getSharedFlag(0));
    }

    public boolean isPassenger() {
        return this.getVehicle() != null;
    }

    public boolean isVehicle() {
        return !this.passengers.isEmpty();
    }

    public boolean dismountsUnderwater() {
        return this.getType().is(EntityTypeTags.DISMOUNTS_UNDERWATER);
    }

    public boolean canControlVehicle() {
        return !this.getType().is(EntityTypeTags.NON_CONTROLLING_RIDER);
    }

    public void setShiftKeyDown(boolean bl) {
        this.setSharedFlag(1, bl);
    }

    public boolean isShiftKeyDown() {
        return this.getSharedFlag(1);
    }

    public boolean isSteppingCarefully() {
        return this.isShiftKeyDown();
    }

    public boolean isSuppressingBounce() {
        return this.isShiftKeyDown();
    }

    public boolean isDiscrete() {
        return this.isShiftKeyDown();
    }

    public boolean isDescending() {
        return this.isShiftKeyDown();
    }

    public boolean isCrouching() {
        return this.hasPose(Pose.CROUCHING);
    }

    public boolean isSprinting() {
        return this.getSharedFlag(3);
    }

    public void setSprinting(boolean bl) {
        this.setSharedFlag(3, bl);
    }

    public boolean isSwimming() {
        return this.getSharedFlag(4);
    }

    public boolean isVisuallySwimming() {
        return this.hasPose(Pose.SWIMMING);
    }

    public boolean isVisuallyCrawling() {
        return this.isVisuallySwimming() && !this.isInWater();
    }

    public void setSwimming(boolean bl) {
        this.setSharedFlag(4, bl);
    }

    public final boolean hasGlowingTag() {
        return this.hasGlowingTag;
    }

    public final void setGlowingTag(boolean bl) {
        this.hasGlowingTag = bl;
        this.setSharedFlag(6, this.isCurrentlyGlowing());
    }

    public boolean isCurrentlyGlowing() {
        if (this.level().isClientSide()) {
            return this.getSharedFlag(6);
        }
        return this.hasGlowingTag;
    }

    public boolean isInvisible() {
        return this.getSharedFlag(5);
    }

    public boolean isInvisibleTo(Player player) {
        if (player.isSpectator()) {
            return false;
        }
        PlayerTeam playerTeam = this.getTeam();
        if (playerTeam != null && player != null && player.getTeam() == playerTeam && ((Team)playerTeam).canSeeFriendlyInvisibles()) {
            return false;
        }
        return this.isInvisible();
    }

    public boolean isOnRails() {
        return false;
    }

    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biConsumer) {
    }

    @Nullable
    public PlayerTeam getTeam() {
        return this.level().getScoreboard().getPlayersTeam(this.getScoreboardName());
    }

    public boolean isAlliedTo(Entity entity) {
        return this.isAlliedTo(entity.getTeam());
    }

    public boolean isAlliedTo(Team team) {
        if (this.getTeam() != null) {
            return this.getTeam().isAlliedTo(team);
        }
        return false;
    }

    public void setInvisible(boolean bl) {
        this.setSharedFlag(5, bl);
    }

    protected boolean getSharedFlag(int n) {
        return (this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << n) != 0;
    }

    protected void setSharedFlag(int n, boolean bl) {
        byte by = this.entityData.get(DATA_SHARED_FLAGS_ID);
        if (bl) {
            this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(by | 1 << n));
        } else {
            this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(by & ~(1 << n)));
        }
    }

    public int getMaxAirSupply() {
        return 300;
    }

    public int getAirSupply() {
        return this.entityData.get(DATA_AIR_SUPPLY_ID);
    }

    public void setAirSupply(int n) {
        this.entityData.set(DATA_AIR_SUPPLY_ID, n);
    }

    public int getTicksFrozen() {
        return this.entityData.get(DATA_TICKS_FROZEN);
    }

    public void setTicksFrozen(int n) {
        this.entityData.set(DATA_TICKS_FROZEN, n);
    }

    public float getPercentFrozen() {
        int n = this.getTicksRequiredToFreeze();
        return (float)Math.min(this.getTicksFrozen(), n) / (float)n;
    }

    public boolean isFullyFrozen() {
        return this.getTicksFrozen() >= this.getTicksRequiredToFreeze();
    }

    public int getTicksRequiredToFreeze() {
        return 140;
    }

    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        this.setRemainingFireTicks(this.remainingFireTicks + 1);
        if (this.remainingFireTicks == 0) {
            this.igniteForSeconds(8.0f);
        }
        this.hurt(this.damageSources().lightningBolt(), 5.0f);
    }

    public void onAboveBubbleCol(boolean bl) {
        Vec3 vec3 = this.getDeltaMovement();
        double d = bl ? Math.max(-0.9, vec3.y - 0.03) : Math.min(1.8, vec3.y + 0.1);
        this.setDeltaMovement(vec3.x, d, vec3.z);
    }

    public void onInsideBubbleColumn(boolean bl) {
        Vec3 vec3 = this.getDeltaMovement();
        double d = bl ? Math.max(-0.3, vec3.y - 0.03) : Math.min(0.7, vec3.y + 0.06);
        this.setDeltaMovement(vec3.x, d, vec3.z);
        this.resetFallDistance();
    }

    public boolean killedEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
        return true;
    }

    public void checkSlowFallDistance() {
        if (this.getDeltaMovement().y() > -0.5 && this.fallDistance > 1.0f) {
            this.fallDistance = 1.0f;
        }
    }

    public void resetFallDistance() {
        this.fallDistance = 0.0f;
    }

    protected void moveTowardsClosestSpace(double d, double d2, double d3) {
        BlockPos blockPos = BlockPos.containing(d, d2, d3);
        Vec3 vec3 = new Vec3(d - (double)blockPos.getX(), d2 - (double)blockPos.getY(), d3 - (double)blockPos.getZ());
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Direction direction = Direction.UP;
        double d4 = Double.MAX_VALUE;
        for (Direction direction2 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
            double d5;
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction2);
            if (this.level().getBlockState(mutableBlockPos).isCollisionShapeFullBlock(this.level(), mutableBlockPos)) continue;
            double d6 = vec3.get(direction2.getAxis());
            double d7 = d5 = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - d6 : d6;
            if (!(d5 < d4)) continue;
            d4 = d5;
            direction = direction2;
        }
        float f = this.random.nextFloat() * 0.2f + 0.1f;
        float f2 = direction.getAxisDirection().getStep();
        Vec3 vec32 = this.getDeltaMovement().scale(0.75);
        if (direction.getAxis() == Direction.Axis.X) {
            this.setDeltaMovement(f2 * f, vec32.y, vec32.z);
        } else if (direction.getAxis() == Direction.Axis.Y) {
            this.setDeltaMovement(vec32.x, f2 * f, vec32.z);
        } else if (direction.getAxis() == Direction.Axis.Z) {
            this.setDeltaMovement(vec32.x, vec32.y, f2 * f);
        }
    }

    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
        this.resetFallDistance();
        this.stuckSpeedMultiplier = vec3;
    }

    private static Component removeAction(Component component) {
        MutableComponent mutableComponent = component.plainCopy().setStyle(component.getStyle().withClickEvent(null));
        for (Component component2 : component.getSiblings()) {
            mutableComponent.append(Entity.removeAction(component2));
        }
        return mutableComponent;
    }

    @Override
    public Component getName() {
        Component component = this.getCustomName();
        if (component != null) {
            return Entity.removeAction(component);
        }
        return this.getTypeName();
    }

    protected Component getTypeName() {
        return this.type.getDescription();
    }

    public boolean is(Entity entity) {
        return this == entity;
    }

    public float getYHeadRot() {
        return 0.0f;
    }

    public void setYHeadRot(float f) {
    }

    public void setYBodyRot(float f) {
    }

    public boolean isAttackable() {
        return true;
    }

    public boolean skipAttackInteraction(Entity entity) {
        return false;
    }

    public String toString() {
        String string;
        String string2 = string = this.level() == null ? "~NULL~" : this.level().toString();
        if (this.removalReason != null) {
            return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, removed=%s]", new Object[]{this.getClass().getSimpleName(), this.getName().getString(), this.id, string, this.getX(), this.getY(), this.getZ(), this.removalReason});
        }
        return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName().getString(), this.id, string, this.getX(), this.getY(), this.getZ());
    }

    public boolean isInvulnerableTo(DamageSource damageSource) {
        return this.isRemoved() || this.invulnerable && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.isCreativePlayer() || damageSource.is(DamageTypeTags.IS_FIRE) && this.fireImmune() || damageSource.is(DamageTypeTags.IS_FALL) && this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE);
    }

    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public void setInvulnerable(boolean bl) {
        this.invulnerable = bl;
    }

    public void copyPosition(Entity entity) {
        this.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
    }

    public void restoreFrom(Entity entity) {
        CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
        compoundTag.remove("Dimension");
        this.load(compoundTag);
        this.portalCooldown = entity.portalCooldown;
        this.portalProcess = entity.portalProcess;
    }

    @Nullable
    public Entity changeDimension(DimensionTransition dimensionTransition) {
        ServerLevel serverLevel;
        Level level;
        block9: {
            block8: {
                level = this.level();
                if (!(level instanceof ServerLevel)) break block8;
                serverLevel = (ServerLevel)level;
                if (!this.isRemoved()) break block9;
            }
            return null;
        }
        level = dimensionTransition.newLevel();
        List<Entity> list = this.getPassengers();
        this.unRide();
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        Object object = list.iterator();
        while (object.hasNext()) {
            Entity entity = object.next();
            Entity entity2 = entity.changeDimension(dimensionTransition);
            if (entity2 == null) continue;
            arrayList.add(entity2);
        }
        serverLevel.getProfiler().push("changeDimension");
        Object object2 = object = level.dimension() == serverLevel.dimension() ? this : this.getType().create(level);
        if (object != null) {
            if (this != object) {
                ((Entity)object).restoreFrom(this);
                this.removeAfterChangingDimensions();
            }
            ((Entity)object).moveTo(dimensionTransition.pos().x, dimensionTransition.pos().y, dimensionTransition.pos().z, dimensionTransition.yRot(), ((Entity)object).getXRot());
            ((Entity)object).setDeltaMovement(dimensionTransition.speed());
            if (this != object) {
                ((ServerLevel)level).addDuringTeleport((Entity)object);
            }
            for (Entity entity2 : arrayList) {
                entity2.startRiding((Entity)object, true);
            }
            serverLevel.resetEmptyTime();
            ((ServerLevel)level).resetEmptyTime();
            dimensionTransition.postDimensionTransition().onTransition((Entity)object);
        }
        serverLevel.getProfiler().pop();
        return object;
    }

    public void placePortalTicket(BlockPos blockPos) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos), 3, blockPos);
        }
    }

    protected void removeAfterChangingDimensions() {
        this.setRemoved(RemovalReason.CHANGED_DIMENSION);
        Entity entity = this;
        if (entity instanceof Leashable) {
            Leashable leashable = (Leashable)((Object)entity);
            leashable.dropLeash(true, false);
        }
    }

    public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return PortalShape.getRelativePosition(foundRectangle, axis, this.position(), this.getDimensions(this.getPose()));
    }

    public boolean canUsePortal(boolean bl) {
        return (bl || !this.isPassenger()) && this.isAlive();
    }

    public boolean canChangeDimensions(Level level, Level level2) {
        return true;
    }

    public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f) {
        return f;
    }

    public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
        return true;
    }

    public int getMaxFallDistance() {
        return 3;
    }

    public boolean isIgnoringBlockTriggers() {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Entity Type", () -> String.valueOf(EntityType.getKey(this.getType())) + " (" + this.getClass().getCanonicalName() + ")");
        crashReportCategory.setDetail("Entity ID", this.id);
        crashReportCategory.setDetail("Entity Name", () -> this.getName().getString());
        crashReportCategory.setDetail("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
        crashReportCategory.setDetail("Entity's Block location", CrashReportCategory.formatLocation((LevelHeightAccessor)this.level(), Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())));
        Vec3 vec3 = this.getDeltaMovement();
        crashReportCategory.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", vec3.x, vec3.y, vec3.z));
        crashReportCategory.setDetail("Entity's Passengers", () -> this.getPassengers().toString());
        crashReportCategory.setDetail("Entity's Vehicle", () -> String.valueOf(this.getVehicle()));
    }

    public boolean displayFireAnimation() {
        return this.isOnFire() && !this.isSpectator();
    }

    public void setUUID(UUID uUID) {
        this.uuid = uUID;
        this.stringUUID = this.uuid.toString();
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    public String getStringUUID() {
        return this.stringUUID;
    }

    @Override
    public String getScoreboardName() {
        return this.stringUUID;
    }

    public boolean isPushedByFluid() {
        return true;
    }

    public static double getViewScale() {
        return viewScale;
    }

    public static void setViewScale(double d) {
        viewScale = d;
    }

    @Override
    public Component getDisplayName() {
        return PlayerTeam.formatNameForTeam(this.getTeam(), this.getName()).withStyle(style -> style.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID()));
    }

    public void setCustomName(@Nullable Component component) {
        this.entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(component));
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.entityData.get(DATA_CUSTOM_NAME).orElse(null);
    }

    @Override
    public boolean hasCustomName() {
        return this.entityData.get(DATA_CUSTOM_NAME).isPresent();
    }

    public void setCustomNameVisible(boolean bl) {
        this.entityData.set(DATA_CUSTOM_NAME_VISIBLE, bl);
    }

    public boolean isCustomNameVisible() {
        return this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
    }

    public boolean teleportTo(ServerLevel serverLevel, double d, double d2, double d3, Set<RelativeMovement> set, float f, float f2) {
        float f3 = Mth.clamp(f2, -90.0f, 90.0f);
        if (serverLevel == this.level()) {
            this.moveTo(d, d2, d3, f, f3);
            this.teleportPassengers();
            this.setYHeadRot(f);
        } else {
            this.unRide();
            Object obj = this.getType().create(serverLevel);
            if (obj != null) {
                ((Entity)obj).restoreFrom(this);
                ((Entity)obj).moveTo(d, d2, d3, f, f3);
                ((Entity)obj).setYHeadRot(f);
                this.setRemoved(RemovalReason.CHANGED_DIMENSION);
                serverLevel.addDuringTeleport((Entity)obj);
            } else {
                return false;
            }
        }
        return true;
    }

    public void dismountTo(double d, double d2, double d3) {
        this.teleportTo(d, d2, d3);
    }

    public void teleportTo(double d, double d2, double d3) {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        this.moveTo(d, d2, d3, this.getYRot(), this.getXRot());
        this.teleportPassengers();
    }

    private void teleportPassengers() {
        this.getSelfAndPassengers().forEach(entity -> {
            for (Entity entity2 : entity.passengers) {
                entity.positionRider(entity2, Entity::moveTo);
            }
        });
    }

    public void teleportRelative(double d, double d2, double d3) {
        this.teleportTo(this.getX() + d, this.getY() + d2, this.getZ() + d3);
    }

    public boolean shouldShowName() {
        return this.isCustomNameVisible();
    }

    @Override
    public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> list) {
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_POSE.equals(entityDataAccessor)) {
            this.refreshDimensions();
        }
    }

    @Deprecated
    protected void fixupDimensions() {
        EntityDimensions entityDimensions;
        Pose pose = this.getPose();
        this.dimensions = entityDimensions = this.getDimensions(pose);
        this.eyeHeight = entityDimensions.eyeHeight();
    }

    public void refreshDimensions() {
        boolean bl;
        EntityDimensions entityDimensions;
        EntityDimensions entityDimensions2 = this.dimensions;
        Pose pose = this.getPose();
        this.dimensions = entityDimensions = this.getDimensions(pose);
        this.eyeHeight = entityDimensions.eyeHeight();
        this.reapplyPosition();
        boolean bl2 = bl = (double)entityDimensions.width() <= 4.0 && (double)entityDimensions.height() <= 4.0;
        if (!(this.level.isClientSide || this.firstTick || this.noPhysics || !bl || !(entityDimensions.width() > entityDimensions2.width()) && !(entityDimensions.height() > entityDimensions2.height()) || this instanceof Player)) {
            this.fudgePositionAfterSizeChange(entityDimensions2);
        }
    }

    public boolean fudgePositionAfterSizeChange(EntityDimensions entityDimensions) {
        VoxelShape voxelShape;
        Optional<Vec3> optional;
        double d;
        double d2;
        EntityDimensions entityDimensions2 = this.getDimensions(this.getPose());
        Vec3 vec3 = this.position().add(0.0, (double)entityDimensions.height() / 2.0, 0.0);
        VoxelShape voxelShape2 = Shapes.create(AABB.ofSize(vec3, d2 = (double)Math.max(0.0f, entityDimensions2.width() - entityDimensions.width()) + 1.0E-6, d = (double)Math.max(0.0f, entityDimensions2.height() - entityDimensions.height()) + 1.0E-6, d2));
        Optional<Vec3> optional2 = this.level.findFreePosition(this, voxelShape2, vec3, entityDimensions2.width(), entityDimensions2.height(), entityDimensions2.width());
        if (optional2.isPresent()) {
            this.setPos(optional2.get().add(0.0, (double)(-entityDimensions2.height()) / 2.0, 0.0));
            return true;
        }
        if (entityDimensions2.width() > entityDimensions.width() && entityDimensions2.height() > entityDimensions.height() && (optional = this.level.findFreePosition(this, voxelShape = Shapes.create(AABB.ofSize(vec3, d2, 1.0E-6, d2)), vec3, entityDimensions2.width(), entityDimensions.height(), entityDimensions2.width())).isPresent()) {
            this.setPos(optional.get().add(0.0, (double)(-entityDimensions.height()) / 2.0 + 1.0E-6, 0.0));
            return true;
        }
        return false;
    }

    public Direction getDirection() {
        return Direction.fromYRot(this.getYRot());
    }

    public Direction getMotionDirection() {
        return this.getDirection();
    }

    protected HoverEvent createHoverEvent() {
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(this.getType(), this.getUUID(), this.getName()));
    }

    public boolean broadcastToPlayer(ServerPlayer serverPlayer) {
        return true;
    }

    @Override
    public final AABB getBoundingBox() {
        return this.bb;
    }

    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    public final void setBoundingBox(AABB aABB) {
        this.bb = aABB;
    }

    public final float getEyeHeight(Pose pose) {
        return this.getDimensions(pose).eyeHeight();
    }

    public final float getEyeHeight() {
        return this.eyeHeight;
    }

    public Vec3 getLeashOffset(float f) {
        return this.getLeashOffset();
    }

    protected Vec3 getLeashOffset() {
        return new Vec3(0.0, this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    public SlotAccess getSlot(int n) {
        return SlotAccess.NULL;
    }

    @Override
    public void sendSystemMessage(Component component) {
    }

    public Level getCommandSenderWorld() {
        return this.level();
    }

    @Nullable
    public MinecraftServer getServer() {
        return this.level().getServer();
    }

    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    public boolean ignoreExplosion(Explosion explosion) {
        return false;
    }

    public void startSeenByPlayer(ServerPlayer serverPlayer) {
    }

    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
    }

    public float rotate(Rotation rotation) {
        float f = Mth.wrapDegrees(this.getYRot());
        switch (rotation) {
            case CLOCKWISE_180: {
                return f + 180.0f;
            }
            case COUNTERCLOCKWISE_90: {
                return f + 270.0f;
            }
            case CLOCKWISE_90: {
                return f + 90.0f;
            }
        }
        return f;
    }

    public float mirror(Mirror mirror) {
        float f = Mth.wrapDegrees(this.getYRot());
        switch (mirror) {
            case FRONT_BACK: {
                return -f;
            }
            case LEFT_RIGHT: {
                return 180.0f - f;
            }
        }
        return f;
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public ProjectileDeflection deflection(Projectile projectile) {
        return this.getType().is(EntityTypeTags.DEFLECTS_PROJECTILES) ? ProjectileDeflection.REVERSE : ProjectileDeflection.NONE;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        return null;
    }

    public final boolean hasControllingPassenger() {
        return this.getControllingPassenger() != null;
    }

    public final List<Entity> getPassengers() {
        return this.passengers;
    }

    @Nullable
    public Entity getFirstPassenger() {
        return this.passengers.isEmpty() ? null : (Entity)this.passengers.get(0);
    }

    public boolean hasPassenger(Entity entity) {
        return this.passengers.contains((Object)entity);
    }

    public boolean hasPassenger(Predicate<Entity> predicate) {
        for (Entity entity : this.passengers) {
            if (!predicate.test(entity)) continue;
            return true;
        }
        return false;
    }

    private Stream<Entity> getIndirectPassengersStream() {
        return this.passengers.stream().flatMap(Entity::getSelfAndPassengers);
    }

    public Stream<Entity> getSelfAndPassengers() {
        return Stream.concat(Stream.of(this), this.getIndirectPassengersStream());
    }

    public Stream<Entity> getPassengersAndSelf() {
        return Stream.concat(this.passengers.stream().flatMap(Entity::getPassengersAndSelf), Stream.of(this));
    }

    public Iterable<Entity> getIndirectPassengers() {
        return () -> this.getIndirectPassengersStream().iterator();
    }

    public int countPlayerPassengers() {
        return (int)this.getIndirectPassengersStream().filter(entity -> entity instanceof Player).count();
    }

    public boolean hasExactlyOnePlayerPassenger() {
        return this.countPlayerPassengers() == 1;
    }

    public Entity getRootVehicle() {
        Entity entity = this;
        while (entity.isPassenger()) {
            entity = entity.getVehicle();
        }
        return entity;
    }

    public boolean isPassengerOfSameVehicle(Entity entity) {
        return this.getRootVehicle() == entity.getRootVehicle();
    }

    public boolean hasIndirectPassenger(Entity entity) {
        if (!entity.isPassenger()) {
            return false;
        }
        Entity entity2 = entity.getVehicle();
        if (entity2 == this) {
            return true;
        }
        return this.hasIndirectPassenger(entity2);
    }

    public boolean isControlledByLocalInstance() {
        LivingEntity livingEntity = this.getControllingPassenger();
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            return player.isLocalPlayer();
        }
        return this.isEffectiveAi();
    }

    public boolean isEffectiveAi() {
        return !this.level().isClientSide;
    }

    protected static Vec3 getCollisionHorizontalEscapeVector(double d, double d2, float f) {
        double d3 = (d + d2 + (double)1.0E-5f) / 2.0;
        float f2 = -Mth.sin(f * ((float)Math.PI / 180));
        float f3 = Mth.cos(f * ((float)Math.PI / 180));
        float f4 = Math.max(Math.abs(f2), Math.abs(f3));
        return new Vec3((double)f2 * d3 / (double)f4, 0.0, (double)f3 * d3 / (double)f4);
    }

    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Nullable
    public Entity getVehicle() {
        return this.vehicle;
    }

    @Nullable
    public Entity getControlledVehicle() {
        return this.vehicle != null && this.vehicle.getControllingPassenger() == this ? this.vehicle : null;
    }

    public PushReaction getPistonPushReaction() {
        return PushReaction.NORMAL;
    }

    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    protected int getFireImmuneTicks() {
        return 1;
    }

    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(this, this.position(), this.getRotationVector(), this.level() instanceof ServerLevel ? (ServerLevel)this.level() : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.level().getServer(), this);
    }

    protected int getPermissionLevel() {
        return 0;
    }

    public boolean hasPermissions(int n) {
        return this.getPermissionLevel() >= n;
    }

    @Override
    public boolean acceptsSuccess() {
        return this.level().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
        Vec3 vec32 = anchor.apply(this);
        double d = vec3.x - vec32.x;
        double d2 = vec3.y - vec32.y;
        double d3 = vec3.z - vec32.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        this.setXRot(Mth.wrapDegrees((float)(-(Mth.atan2(d2, d4) * 57.2957763671875))));
        this.setYRot(Mth.wrapDegrees((float)(Mth.atan2(d3, d) * 57.2957763671875) - 90.0f));
        this.setYHeadRot(this.getYRot());
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public float getPreciseBodyRotation(float f) {
        return Mth.lerp(f, this.yRotO, this.yRot);
    }

    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagKey, double d) {
        if (this.touchingUnloadedChunk()) {
            return false;
        }
        AABB aABB = this.getBoundingBox().deflate(0.001);
        int n = Mth.floor(aABB.minX);
        int n2 = Mth.ceil(aABB.maxX);
        int n3 = Mth.floor(aABB.minY);
        int n4 = Mth.ceil(aABB.maxY);
        int n5 = Mth.floor(aABB.minZ);
        int n6 = Mth.ceil(aABB.maxZ);
        double d2 = 0.0;
        boolean bl = this.isPushedByFluid();
        boolean bl2 = false;
        Vec3 vec3 = Vec3.ZERO;
        int n7 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    double d3;
                    mutableBlockPos.set(i, j, k);
                    FluidState fluidState = this.level().getFluidState(mutableBlockPos);
                    if (!fluidState.is(tagKey) || !((d3 = (double)((float)j + fluidState.getHeight(this.level(), mutableBlockPos))) >= aABB.minY)) continue;
                    bl2 = true;
                    d2 = Math.max(d3 - aABB.minY, d2);
                    if (!bl) continue;
                    Vec3 vec32 = fluidState.getFlow(this.level(), mutableBlockPos);
                    if (d2 < 0.4) {
                        vec32 = vec32.scale(d2);
                    }
                    vec3 = vec3.add(vec32);
                    ++n7;
                }
            }
        }
        if (vec3.length() > 0.0) {
            if (n7 > 0) {
                vec3 = vec3.scale(1.0 / (double)n7);
            }
            if (!(this instanceof Player)) {
                vec3 = vec3.normalize();
            }
            Vec3 vec33 = this.getDeltaMovement();
            vec3 = vec3.scale(d);
            double d4 = 0.003;
            if (Math.abs(vec33.x) < 0.003 && Math.abs(vec33.z) < 0.003 && vec3.length() < 0.0045000000000000005) {
                vec3 = vec3.normalize().scale(0.0045000000000000005);
            }
            this.setDeltaMovement(this.getDeltaMovement().add(vec3));
        }
        this.fluidHeight.put(tagKey, d2);
        return bl2;
    }

    public boolean touchingUnloadedChunk() {
        AABB aABB = this.getBoundingBox().inflate(1.0);
        int n = Mth.floor(aABB.minX);
        int n2 = Mth.ceil(aABB.maxX);
        int n3 = Mth.floor(aABB.minZ);
        int n4 = Mth.ceil(aABB.maxZ);
        return !this.level().hasChunksAt(n, n3, n2, n4);
    }

    public double getFluidHeight(TagKey<Fluid> tagKey) {
        return this.fluidHeight.getDouble(tagKey);
    }

    public double getFluidJumpThreshold() {
        return (double)this.getEyeHeight() < 0.4 ? 0.0 : 0.4;
    }

    public final float getBbWidth() {
        return this.dimensions.width();
    }

    public final float getBbHeight() {
        return this.dimensions.height();
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    public EntityDimensions getDimensions(Pose pose) {
        return this.type.getDimensions();
    }

    public final EntityAttachments getAttachments() {
        return this.dimensions.attachments();
    }

    public Vec3 position() {
        return this.position;
    }

    public Vec3 trackingPosition() {
        return this.position();
    }

    @Override
    public BlockPos blockPosition() {
        return this.blockPosition;
    }

    public BlockState getInBlockState() {
        if (this.inBlockState == null) {
            this.inBlockState = this.level().getBlockState(this.blockPosition());
        }
        return this.inBlockState;
    }

    public ChunkPos chunkPosition() {
        return this.chunkPosition;
    }

    public Vec3 getDeltaMovement() {
        return this.deltaMovement;
    }

    public void setDeltaMovement(Vec3 vec3) {
        this.deltaMovement = vec3;
    }

    public void addDeltaMovement(Vec3 vec3) {
        this.setDeltaMovement(this.getDeltaMovement().add(vec3));
    }

    public void setDeltaMovement(double d, double d2, double d3) {
        this.setDeltaMovement(new Vec3(d, d2, d3));
    }

    public final int getBlockX() {
        return this.blockPosition.getX();
    }

    public final double getX() {
        return this.position.x;
    }

    public double getX(double d) {
        return this.position.x + (double)this.getBbWidth() * d;
    }

    public double getRandomX(double d) {
        return this.getX((2.0 * this.random.nextDouble() - 1.0) * d);
    }

    public final int getBlockY() {
        return this.blockPosition.getY();
    }

    public final double getY() {
        return this.position.y;
    }

    public double getY(double d) {
        return this.position.y + (double)this.getBbHeight() * d;
    }

    public double getRandomY() {
        return this.getY(this.random.nextDouble());
    }

    public double getEyeY() {
        return this.position.y + (double)this.eyeHeight;
    }

    public final int getBlockZ() {
        return this.blockPosition.getZ();
    }

    public final double getZ() {
        return this.position.z;
    }

    public double getZ(double d) {
        return this.position.z + (double)this.getBbWidth() * d;
    }

    public double getRandomZ(double d) {
        return this.getZ((2.0 * this.random.nextDouble() - 1.0) * d);
    }

    public final void setPosRaw(double d, double d2, double d3) {
        if (this.position.x != d || this.position.y != d2 || this.position.z != d3) {
            this.position = new Vec3(d, d2, d3);
            int n = Mth.floor(d);
            int n2 = Mth.floor(d2);
            int n3 = Mth.floor(d3);
            if (n != this.blockPosition.getX() || n2 != this.blockPosition.getY() || n3 != this.blockPosition.getZ()) {
                this.blockPosition = new BlockPos(n, n2, n3);
                this.inBlockState = null;
                if (SectionPos.blockToSectionCoord(n) != this.chunkPosition.x || SectionPos.blockToSectionCoord(n3) != this.chunkPosition.z) {
                    this.chunkPosition = new ChunkPos(this.blockPosition);
                }
            }
            this.levelCallback.onMove();
        }
    }

    public void checkDespawn() {
    }

    public Vec3 getRopeHoldPosition(float f) {
        return this.getPosition(f).add(0.0, (double)this.eyeHeight * 0.7, 0.0);
    }

    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        int n = clientboundAddEntityPacket.getId();
        double d = clientboundAddEntityPacket.getX();
        double d2 = clientboundAddEntityPacket.getY();
        double d3 = clientboundAddEntityPacket.getZ();
        this.syncPacketPositionCodec(d, d2, d3);
        this.moveTo(d, d2, d3);
        this.setXRot(clientboundAddEntityPacket.getXRot());
        this.setYRot(clientboundAddEntityPacket.getYRot());
        this.setId(n);
        this.setUUID(clientboundAddEntityPacket.getUUID());
    }

    @Nullable
    public ItemStack getPickResult() {
        return null;
    }

    public void setIsInPowderSnow(boolean bl) {
        this.isInPowderSnow = bl;
    }

    public boolean canFreeze() {
        return !this.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
    }

    public boolean isFreezing() {
        return (this.isInPowderSnow || this.wasInPowderSnow) && this.canFreeze();
    }

    public float getYRot() {
        return this.yRot;
    }

    public float getVisualRotationYInDegrees() {
        return this.getYRot();
    }

    public void setYRot(float f) {
        if (!Float.isFinite(f)) {
            Util.logAndPauseIfInIde("Invalid entity rotation: " + f + ", discarding.");
            return;
        }
        this.yRot = f;
    }

    public float getXRot() {
        return this.xRot;
    }

    public void setXRot(float f) {
        if (!Float.isFinite(f)) {
            Util.logAndPauseIfInIde("Invalid entity rotation: " + f + ", discarding.");
            return;
        }
        this.xRot = f;
    }

    public boolean canSprint() {
        return false;
    }

    public float maxUpStep() {
        return 0.0f;
    }

    public void onExplosionHit(@Nullable Entity entity) {
    }

    public final boolean isRemoved() {
        return this.removalReason != null;
    }

    @Nullable
    public RemovalReason getRemovalReason() {
        return this.removalReason;
    }

    @Override
    public final void setRemoved(RemovalReason removalReason) {
        if (this.removalReason == null) {
            this.removalReason = removalReason;
        }
        if (this.removalReason.shouldDestroy()) {
            this.stopRiding();
        }
        this.getPassengers().forEach(Entity::stopRiding);
        this.levelCallback.onRemove(removalReason);
    }

    protected void unsetRemoved() {
        this.removalReason = null;
    }

    @Override
    public void setLevelCallback(EntityInLevelCallback entityInLevelCallback) {
        this.levelCallback = entityInLevelCallback;
    }

    @Override
    public boolean shouldBeSaved() {
        if (this.removalReason != null && !this.removalReason.shouldSave()) {
            return false;
        }
        if (this.isPassenger()) {
            return false;
        }
        return !this.isVehicle() || !this.hasExactlyOnePlayerPassenger();
    }

    @Override
    public boolean isAlwaysTicking() {
        return false;
    }

    public boolean mayInteract(Level level, BlockPos blockPos) {
        return true;
    }

    public Level level() {
        return this.level;
    }

    protected void setLevel(Level level) {
        this.level = level;
    }

    public DamageSources damageSources() {
        return this.level().damageSources();
    }

    public RegistryAccess registryAccess() {
        return this.level().registryAccess();
    }

    protected void lerpPositionAndRotationStep(int n, double d, double d2, double d3, double d4, double d5) {
        double d6 = 1.0 / (double)n;
        double d7 = Mth.lerp(d6, this.getX(), d);
        double d8 = Mth.lerp(d6, this.getY(), d2);
        double d9 = Mth.lerp(d6, this.getZ(), d3);
        float f = (float)Mth.rotLerp(d6, (double)this.getYRot(), d4);
        float f2 = (float)Mth.lerp(d6, (double)this.getXRot(), d5);
        this.setPos(d7, d8, d9);
        this.setRot(f, f2);
    }

    public RandomSource getRandom() {
        return this.random;
    }

    public Vec3 getKnownMovement() {
        LivingEntity livingEntity = this.getControllingPassenger();
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            if (this.isAlive()) {
                return player.getKnownMovement();
            }
        }
        return this.getDeltaMovement();
    }

    @Nullable
    public ItemStack getWeaponItem() {
        return null;
    }

    public static enum RemovalReason {
        KILLED(true, false),
        DISCARDED(true, false),
        UNLOADED_TO_CHUNK(false, true),
        UNLOADED_WITH_PLAYER(false, false),
        CHANGED_DIMENSION(false, false);

        private final boolean destroy;
        private final boolean save;

        private RemovalReason(boolean bl, boolean bl2) {
            this.destroy = bl;
            this.save = bl2;
        }

        public boolean shouldDestroy() {
            return this.destroy;
        }

        public boolean shouldSave() {
            return this.save;
        }
    }

    public static enum MovementEmission {
        NONE(false, false),
        SOUNDS(true, false),
        EVENTS(false, true),
        ALL(true, true);

        final boolean sounds;
        final boolean events;

        private MovementEmission(boolean bl, boolean bl2) {
            this.sounds = bl;
            this.events = bl2;
        }

        public boolean emitsAnything() {
            return this.events || this.sounds;
        }

        public boolean emitsEvents() {
            return this.events;
        }

        public boolean emitsSounds() {
            return this.sounds;
        }
    }

    @FunctionalInterface
    public static interface MoveFunction {
        public void accept(Entity var1, double var2, double var4, double var6);
    }
}

