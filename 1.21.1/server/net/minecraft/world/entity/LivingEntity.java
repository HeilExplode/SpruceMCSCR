/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Objects
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;

public abstract class LivingEntity
extends Entity
implements Attackable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_ACTIVE_EFFECTS = "active_effects";
    private static final ResourceLocation SPEED_MODIFIER_POWDER_SNOW_ID = ResourceLocation.withDefaultNamespace("powder_snow");
    private static final ResourceLocation SPRINTING_MODIFIER_ID = ResourceLocation.withDefaultNamespace("sprinting");
    private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(SPRINTING_MODIFIER_ID, 0.3f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final int HAND_SLOTS = 2;
    public static final int ARMOR_SLOTS = 4;
    public static final int EQUIPMENT_SLOT_OFFSET = 98;
    public static final int ARMOR_SLOT_OFFSET = 100;
    public static final int BODY_ARMOR_OFFSET = 105;
    public static final int SWING_DURATION = 6;
    public static final int PLAYER_HURT_EXPERIENCE_TIME = 100;
    private static final int DAMAGE_SOURCE_TIMEOUT = 40;
    public static final double MIN_MOVEMENT_DISTANCE = 0.003;
    public static final double DEFAULT_BASE_GRAVITY = 0.08;
    public static final int DEATH_DURATION = 20;
    private static final int TICKS_PER_ELYTRA_FREE_FALL_EVENT = 10;
    private static final int FREE_FALL_EVENTS_PER_ELYTRA_BREAK = 2;
    public static final int USE_ITEM_INTERVAL = 4;
    public static final float BASE_JUMP_POWER = 0.42f;
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 128.0;
    protected static final int LIVING_ENTITY_FLAG_IS_USING = 1;
    protected static final int LIVING_ENTITY_FLAG_OFF_HAND = 2;
    protected static final int LIVING_ENTITY_FLAG_SPIN_ATTACK = 4;
    protected static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.PARTICLES);
    private static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final int PARTICLE_FREQUENCY_WHEN_INVISIBLE = 15;
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(0.2f);
    public static final float EXTRA_RENDER_CULLING_SIZE_WITH_BIG_HAT = 0.5f;
    public static final float DEFAULT_BABY_SCALE = 0.5f;
    private static final float ITEM_USE_EFFECT_START_FRACTION = 0.21875f;
    public static final String ATTRIBUTES_FIELD = "attributes";
    private final AttributeMap attributes;
    private final CombatTracker combatTracker = new CombatTracker(this);
    private final Map<Holder<MobEffect>, MobEffectInstance> activeEffects = Maps.newHashMap();
    private final NonNullList<ItemStack> lastHandItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> lastArmorItemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
    private ItemStack lastBodyItemStack = ItemStack.EMPTY;
    public boolean swinging;
    private boolean discardFriction = false;
    public InteractionHand swingingArm;
    public int swingTime;
    public int removeArrowTime;
    public int removeStingerTime;
    public int hurtTime;
    public int hurtDuration;
    public int deathTime;
    public float oAttackAnim;
    public float attackAnim;
    protected int attackStrengthTicker;
    public final WalkAnimationState walkAnimation = new WalkAnimationState();
    public final int invulnerableDuration = 20;
    public final float timeOffs;
    public final float rotA;
    public float yBodyRot;
    public float yBodyRotO;
    public float yHeadRot;
    public float yHeadRotO;
    @Nullable
    protected Player lastHurtByPlayer;
    protected int lastHurtByPlayerTime;
    protected boolean dead;
    protected int noActionTime;
    protected float oRun;
    protected float run;
    protected float animStep;
    protected float animStepO;
    protected float rotOffs;
    protected int deathScore;
    protected float lastHurt;
    protected boolean jumping;
    public float xxa;
    public float yya;
    public float zza;
    protected int lerpSteps;
    protected double lerpX;
    protected double lerpY;
    protected double lerpZ;
    protected double lerpYRot;
    protected double lerpXRot;
    protected double lerpYHeadRot;
    protected int lerpHeadSteps;
    private boolean effectsDirty = true;
    @Nullable
    private LivingEntity lastHurtByMob;
    private int lastHurtByMobTimestamp;
    @Nullable
    private LivingEntity lastHurtMob;
    private int lastHurtMobTimestamp;
    private float speed;
    private int noJumpDelay;
    private float absorptionAmount;
    protected ItemStack useItem = ItemStack.EMPTY;
    protected int useItemRemaining;
    protected int fallFlyTicks;
    private BlockPos lastPos;
    private Optional<BlockPos> lastClimbablePos = Optional.empty();
    @Nullable
    private DamageSource lastDamageSource;
    private long lastDamageStamp;
    protected int autoSpinAttackTicks;
    protected float autoSpinAttackDmg;
    @Nullable
    protected ItemStack autoSpinAttackItemStack;
    private float swimAmount;
    private float swimAmountO;
    protected Brain<?> brain;
    private boolean skipDropExperience;
    private final Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>> activeLocationDependentEnchantments = new Reference2ObjectArrayMap();
    protected float appliedScale = 1.0f;

    protected LivingEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.attributes = new AttributeMap(DefaultAttributes.getSupplier(entityType));
        this.setHealth(this.getMaxHealth());
        this.blocksBuilding = true;
        this.rotA = (float)((Math.random() + 1.0) * (double)0.01f);
        this.reapplyPosition();
        this.timeOffs = (float)Math.random() * 12398.0f;
        this.setYRot((float)(Math.random() * 6.2831854820251465));
        this.yHeadRot = this.getYRot();
        NbtOps nbtOps = NbtOps.INSTANCE;
        this.brain = this.makeBrain(new Dynamic((DynamicOps)nbtOps, (Object)((Tag)nbtOps.createMap((Map)ImmutableMap.of((Object)nbtOps.createString("memories"), (Object)((Tag)nbtOps.emptyMap()))))));
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(ImmutableList.of(), ImmutableList.of());
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return this.brainProvider().makeBrain(dynamic);
    }

    @Override
    public void kill() {
        this.hurt(this.damageSources().genericKill(), Float.MAX_VALUE);
    }

    public boolean canAttackType(EntityType<?> entityType) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
        builder.define(DATA_EFFECT_PARTICLES, List.of());
        builder.define(DATA_EFFECT_AMBIENCE_ID, false);
        builder.define(DATA_ARROW_COUNT_ID, 0);
        builder.define(DATA_STINGER_COUNT_ID, 0);
        builder.define(DATA_HEALTH_ID, Float.valueOf(1.0f));
        builder.define(SLEEPING_POS_ID, Optional.empty());
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return AttributeSupplier.builder().add(Attributes.MAX_HEALTH).add(Attributes.KNOCKBACK_RESISTANCE).add(Attributes.MOVEMENT_SPEED).add(Attributes.ARMOR).add(Attributes.ARMOR_TOUGHNESS).add(Attributes.MAX_ABSORPTION).add(Attributes.STEP_HEIGHT).add(Attributes.SCALE).add(Attributes.GRAVITY).add(Attributes.SAFE_FALL_DISTANCE).add(Attributes.FALL_DAMAGE_MULTIPLIER).add(Attributes.JUMP_STRENGTH).add(Attributes.OXYGEN_BONUS).add(Attributes.BURNING_TIME).add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE).add(Attributes.WATER_MOVEMENT_EFFICIENCY).add(Attributes.MOVEMENT_EFFICIENCY).add(Attributes.ATTACK_KNOCKBACK);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        Level level;
        if (!this.isInWater()) {
            this.updateInWaterStateAndDoWaterCurrentPushing();
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (bl && this.fallDistance > 0.0f) {
                this.onChangedBlock(serverLevel, blockPos);
                double d2 = this.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
                if ((double)this.fallDistance > d2 && !blockState.isAir()) {
                    double d3 = this.getX();
                    double d4 = this.getY();
                    double d5 = this.getZ();
                    BlockPos blockPos2 = this.blockPosition();
                    if (blockPos.getX() != blockPos2.getX() || blockPos.getZ() != blockPos2.getZ()) {
                        double d6 = d3 - (double)blockPos.getX() - 0.5;
                        double d7 = d5 - (double)blockPos.getZ() - 0.5;
                        double d8 = Math.max(Math.abs(d6), Math.abs(d7));
                        d3 = (double)blockPos.getX() + 0.5 + d6 / d8 * 0.5;
                        d5 = (double)blockPos.getZ() + 0.5 + d7 / d8 * 0.5;
                    }
                    float f = Mth.ceil((double)this.fallDistance - d2);
                    double d9 = Math.min((double)(0.2f + f / 15.0f), 2.5);
                    int n = (int)(150.0 * d9);
                    ((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d3, d4, d5, n, 0.0, 0.0, 0.0, 0.15f);
                }
            }
        }
        super.checkFallDamage(d, bl, blockState, blockPos);
        if (bl) {
            this.lastClimbablePos = Optional.empty();
        }
    }

    public final boolean canBreatheUnderwater() {
        return this.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER);
    }

    public float getSwimAmount(float f) {
        return Mth.lerp(f, this.swimAmountO, this.swimAmount);
    }

    public boolean hasLandedInLiquid() {
        return this.getDeltaMovement().y() < (double)1.0E-5f && this.isInLiquid();
    }

    @Override
    public void baseTick() {
        Level level;
        this.oAttackAnim = this.attackAnim;
        if (this.firstTick) {
            this.getSleepingPos().ifPresent(this::setPosToBed);
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            EnchantmentHelper.tickEffects(serverLevel, this);
        }
        super.baseTick();
        this.level().getProfiler().push("livingEntityBaseTick");
        if (this.fireImmune() || this.level().isClientSide) {
            this.clearFire();
        }
        if (this.isAlive()) {
            Object object;
            boolean bl = this instanceof Player;
            if (!this.level().isClientSide) {
                double d;
                double d2;
                if (this.isInWall()) {
                    this.hurt(this.damageSources().inWall(), 1.0f);
                } else if (bl && !this.level().getWorldBorder().isWithinBounds(this.getBoundingBox()) && (d2 = this.level().getWorldBorder().getDistanceToBorder(this) + this.level().getWorldBorder().getDamageSafeZone()) < 0.0 && (d = this.level().getWorldBorder().getDamagePerBlock()) > 0.0) {
                    this.hurt(this.damageSources().outOfBorder(), Math.max(1, Mth.floor(-d2 * d)));
                }
            }
            if (this.isEyeInFluid(FluidTags.WATER) && !this.level().getBlockState(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
                boolean bl2;
                boolean bl3 = bl2 = !this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && (!bl || !((Player)this).getAbilities().invulnerable);
                if (bl2) {
                    this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
                    if (this.getAirSupply() == -20) {
                        this.setAirSupply(0);
                        object = this.getDeltaMovement();
                        for (int i = 0; i < 8; ++i) {
                            double d = this.random.nextDouble() - this.random.nextDouble();
                            double d3 = this.random.nextDouble() - this.random.nextDouble();
                            double d4 = this.random.nextDouble() - this.random.nextDouble();
                            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d, this.getY() + d3, this.getZ() + d4, ((Vec3)object).x, ((Vec3)object).y, ((Vec3)object).z);
                        }
                        this.hurt(this.damageSources().drown(), 2.0f);
                    }
                }
                if (!this.level().isClientSide && this.isPassenger() && this.getVehicle() != null && this.getVehicle().dismountsUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            }
            object = this.level();
            if (object instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)object;
                object = this.blockPosition();
                if (!Objects.equal((Object)this.lastPos, (Object)object)) {
                    this.lastPos = object;
                    this.onChangedBlock(serverLevel, (BlockPos)object);
                }
            }
        }
        if (this.isAlive() && (this.isInWaterRainOrBubble() || this.isInPowderSnow)) {
            this.extinguishFire();
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.invulnerableTime > 0 && !(this instanceof ServerPlayer)) {
            --this.invulnerableTime;
        }
        if (this.isDeadOrDying() && this.level().shouldTickDeath(this)) {
            this.tickDeath();
        }
        if (this.lastHurtByPlayerTime > 0) {
            --this.lastHurtByPlayerTime;
        } else {
            this.lastHurtByPlayer = null;
        }
        if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
            this.lastHurtMob = null;
        }
        if (this.lastHurtByMob != null) {
            if (!this.lastHurtByMob.isAlive()) {
                this.setLastHurtByMob(null);
            } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                this.setLastHurtByMob(null);
            }
        }
        this.tickEffects();
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.level().getProfiler().pop();
    }

    @Override
    protected float getBlockSpeedFactor() {
        return Mth.lerp((float)this.getAttributeValue(Attributes.MOVEMENT_EFFICIENCY), super.getBlockSpeedFactor(), 1.0f);
    }

    protected void removeFrost() {
        AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeInstance == null) {
            return;
        }
        if (attributeInstance.getModifier(SPEED_MODIFIER_POWDER_SNOW_ID) != null) {
            attributeInstance.removeModifier(SPEED_MODIFIER_POWDER_SNOW_ID);
        }
    }

    protected void tryAddFrost() {
        int n;
        if (!this.getBlockStateOnLegacy().isAir() && (n = this.getTicksFrozen()) > 0) {
            AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance == null) {
                return;
            }
            float f = -0.05f * this.getPercentFrozen();
            attributeInstance.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_POWDER_SNOW_ID, f, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    protected void onChangedBlock(ServerLevel serverLevel, BlockPos blockPos) {
        EnchantmentHelper.runLocationChangedEffects(serverLevel, this);
    }

    public boolean isBaby() {
        return false;
    }

    public float getAgeScale() {
        return this.isBaby() ? 0.5f : 1.0f;
    }

    public float getScale() {
        AttributeMap attributeMap = this.getAttributes();
        if (attributeMap == null) {
            return 1.0f;
        }
        return this.sanitizeScale((float)attributeMap.getValue(Attributes.SCALE));
    }

    protected float sanitizeScale(float f) {
        return f;
    }

    protected boolean isAffectedByFluids() {
        return true;
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean shouldDropExperience() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot() {
        return !this.isBaby();
    }

    protected int decreaseAirSupply(int n) {
        AttributeInstance attributeInstance = this.getAttribute(Attributes.OXYGEN_BONUS);
        double d = attributeInstance != null ? attributeInstance.getValue() : 0.0;
        if (d > 0.0 && this.random.nextDouble() >= 1.0 / (d + 1.0)) {
            return n;
        }
        return n - 1;
    }

    protected int increaseAirSupply(int n) {
        return Math.min(n + 4, this.getMaxAirSupply());
    }

    public final int getExperienceReward(ServerLevel serverLevel, @Nullable Entity entity) {
        return EnchantmentHelper.processMobExperience(serverLevel, entity, this, this.getBaseExperienceReward());
    }

    protected int getBaseExperienceReward() {
        return 0;
    }

    protected boolean isAlwaysExperienceDropper() {
        return false;
    }

    @Nullable
    public LivingEntity getLastHurtByMob() {
        return this.lastHurtByMob;
    }

    @Override
    public LivingEntity getLastAttacker() {
        return this.getLastHurtByMob();
    }

    public int getLastHurtByMobTimestamp() {
        return this.lastHurtByMobTimestamp;
    }

    public void setLastHurtByPlayer(@Nullable Player player) {
        this.lastHurtByPlayer = player;
        this.lastHurtByPlayerTime = this.tickCount;
    }

    public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
        this.lastHurtByMob = livingEntity;
        this.lastHurtByMobTimestamp = this.tickCount;
    }

    @Nullable
    public LivingEntity getLastHurtMob() {
        return this.lastHurtMob;
    }

    public int getLastHurtMobTimestamp() {
        return this.lastHurtMobTimestamp;
    }

    public void setLastHurtMob(Entity entity) {
        this.lastHurtMob = entity instanceof LivingEntity ? (LivingEntity)entity : null;
        this.lastHurtMobTimestamp = this.tickCount;
    }

    public int getNoActionTime() {
        return this.noActionTime;
    }

    public void setNoActionTime(int n) {
        this.noActionTime = n;
    }

    public boolean shouldDiscardFriction() {
        return this.discardFriction;
    }

    public void setDiscardFriction(boolean bl) {
        this.discardFriction = bl;
    }

    protected boolean doesEmitEquipEvent(EquipmentSlot equipmentSlot) {
        return true;
    }

    public void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        boolean bl;
        boolean bl2 = bl = itemStack2.isEmpty() && itemStack.isEmpty();
        if (bl || ItemStack.isSameItemSameComponents(itemStack, itemStack2) || this.firstTick) {
            return;
        }
        Equipable equipable = Equipable.get(itemStack2);
        if (!this.level().isClientSide() && !this.isSpectator()) {
            if (!this.isSilent() && equipable != null && equipable.getEquipmentSlot() == equipmentSlot) {
                this.level().playSeededSound(null, this.getX(), this.getY(), this.getZ(), equipable.getEquipSound(), this.getSoundSource(), 1.0f, 1.0f, this.random.nextLong());
            }
            if (this.doesEmitEquipEvent(equipmentSlot)) {
                this.gameEvent(equipable != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (removalReason == Entity.RemovalReason.KILLED || removalReason == Entity.RemovalReason.DISCARDED) {
            this.triggerOnDeathMobEffects(removalReason);
        }
        super.remove(removalReason);
        this.brain.clearMemories();
    }

    protected void triggerOnDeathMobEffects(Entity.RemovalReason removalReason) {
        for (MobEffectInstance mobEffectInstance : this.getActiveEffects()) {
            mobEffectInstance.onMobRemoved(this, removalReason);
        }
        this.activeEffects.clear();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        DataResult<Tag> dataResult;
        compoundTag.putFloat("Health", this.getHealth());
        compoundTag.putShort("HurtTime", (short)this.hurtTime);
        compoundTag.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
        compoundTag.putShort("DeathTime", (short)this.deathTime);
        compoundTag.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        compoundTag.put(ATTRIBUTES_FIELD, this.getAttributes().save());
        if (!this.activeEffects.isEmpty()) {
            dataResult = new DataResult<Tag>();
            for (MobEffectInstance mobEffectInstance : this.activeEffects.values()) {
                dataResult.add(mobEffectInstance.save());
            }
            compoundTag.put(TAG_ACTIVE_EFFECTS, (Tag)dataResult);
        }
        compoundTag.putBoolean("FallFlying", this.isFallFlying());
        this.getSleepingPos().ifPresent(blockPos -> {
            compoundTag.putInt("SleepingX", blockPos.getX());
            compoundTag.putInt("SleepingY", blockPos.getY());
            compoundTag.putInt("SleepingZ", blockPos.getZ());
        });
        dataResult = this.brain.serializeStart(NbtOps.INSTANCE);
        dataResult.resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("Brain", (Tag)tag));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        Object object;
        Object object2;
        this.internalSetAbsorptionAmount(compoundTag.getFloat("AbsorptionAmount"));
        if (compoundTag.contains(ATTRIBUTES_FIELD, 9) && this.level() != null && !this.level().isClientSide) {
            this.getAttributes().load(compoundTag.getList(ATTRIBUTES_FIELD, 10));
        }
        if (compoundTag.contains(TAG_ACTIVE_EFFECTS, 9)) {
            object2 = compoundTag.getList(TAG_ACTIVE_EFFECTS, 10);
            for (int i = 0; i < ((ListTag)object2).size(); ++i) {
                object = ((ListTag)object2).getCompound(i);
                MobEffectInstance mobEffectInstance = MobEffectInstance.load((CompoundTag)object);
                if (mobEffectInstance == null) continue;
                this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
            }
        }
        if (compoundTag.contains("Health", 99)) {
            this.setHealth(compoundTag.getFloat("Health"));
        }
        this.hurtTime = compoundTag.getShort("HurtTime");
        this.deathTime = compoundTag.getShort("DeathTime");
        this.lastHurtByMobTimestamp = compoundTag.getInt("HurtByTimestamp");
        if (compoundTag.contains("Team", 8)) {
            boolean bl;
            object2 = compoundTag.getString("Team");
            Scoreboard scoreboard = this.level().getScoreboard();
            object = scoreboard.getPlayerTeam((String)object2);
            boolean bl2 = bl = object != null && scoreboard.addPlayerToTeam(this.getStringUUID(), (PlayerTeam)object);
            if (!bl) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", object2);
            }
        }
        if (compoundTag.getBoolean("FallFlying")) {
            this.setSharedFlag(7, true);
        }
        if (compoundTag.contains("SleepingX", 99) && compoundTag.contains("SleepingY", 99) && compoundTag.contains("SleepingZ", 99)) {
            object2 = new BlockPos(compoundTag.getInt("SleepingX"), compoundTag.getInt("SleepingY"), compoundTag.getInt("SleepingZ"));
            this.setSleepingPos((BlockPos)object2);
            this.entityData.set(DATA_POSE, Pose.SLEEPING);
            if (!this.firstTick) {
                this.setPosToBed((BlockPos)object2);
            }
        }
        if (compoundTag.contains("Brain", 10)) {
            this.brain = this.makeBrain(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get("Brain")));
        }
    }

    protected void tickEffects() {
        Holder<MobEffect> holder;
        Iterator<Holder<MobEffect>> iterator = this.activeEffects.keySet().iterator();
        try {
            while (iterator.hasNext()) {
                holder = iterator.next();
                MobEffectInstance mobEffectInstance = this.activeEffects.get(holder);
                if (!mobEffectInstance.tick(this, () -> this.onEffectUpdated(mobEffectInstance, true, null))) {
                    if (this.level().isClientSide) continue;
                    iterator.remove();
                    this.onEffectRemoved(mobEffectInstance);
                    continue;
                }
                if (mobEffectInstance.getDuration() % 600 != 0) continue;
                this.onEffectUpdated(mobEffectInstance, false, null);
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        if (this.effectsDirty) {
            if (!this.level().isClientSide) {
                this.updateInvisibilityStatus();
                this.updateGlowingStatus();
            }
            this.effectsDirty = false;
        }
        if (!(holder = this.entityData.get(DATA_EFFECT_PARTICLES)).isEmpty()) {
            int n;
            boolean bl = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
            int n2 = this.isInvisible() ? 15 : 4;
            int n3 = n = bl ? 5 : 1;
            if (this.random.nextInt(n2 * n) == 0) {
                this.level().addParticle((ParticleOptions)Util.getRandom(holder, this.random), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 1.0, 1.0, 1.0);
            }
        }
    }

    protected void updateInvisibilityStatus() {
        if (this.activeEffects.isEmpty()) {
            this.removeEffectParticles();
            this.setInvisible(false);
            return;
        }
        this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
        this.updateSynchronizedMobEffectParticles();
    }

    private void updateSynchronizedMobEffectParticles() {
        List<ParticleOptions> list = this.activeEffects.values().stream().filter(MobEffectInstance::isVisible).map(MobEffectInstance::getParticleOptions).toList();
        this.entityData.set(DATA_EFFECT_PARTICLES, list);
        this.entityData.set(DATA_EFFECT_AMBIENCE_ID, LivingEntity.areAllEffectsAmbient(this.activeEffects.values()));
    }

    private void updateGlowingStatus() {
        boolean bl = this.isCurrentlyGlowing();
        if (this.getSharedFlag(6) != bl) {
            this.setSharedFlag(6, bl);
        }
    }

    public double getVisibilityPercent(@Nullable Entity entity) {
        double d = 1.0;
        if (this.isDiscrete()) {
            d *= 0.8;
        }
        if (this.isInvisible()) {
            float f = this.getArmorCoverPercentage();
            if (f < 0.1f) {
                f = 0.1f;
            }
            d *= 0.7 * (double)f;
        }
        if (entity != null) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
            EntityType<?> entityType = entity.getType();
            if (entityType == EntityType.SKELETON && itemStack.is(Items.SKELETON_SKULL) || entityType == EntityType.ZOMBIE && itemStack.is(Items.ZOMBIE_HEAD) || entityType == EntityType.PIGLIN && itemStack.is(Items.PIGLIN_HEAD) || entityType == EntityType.PIGLIN_BRUTE && itemStack.is(Items.PIGLIN_HEAD) || entityType == EntityType.CREEPER && itemStack.is(Items.CREEPER_HEAD)) {
                d *= 0.5;
            }
        }
        return d;
    }

    public boolean canAttack(LivingEntity livingEntity) {
        if (livingEntity instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return livingEntity.canBeSeenAsEnemy();
    }

    public boolean canAttack(LivingEntity livingEntity, TargetingConditions targetingConditions) {
        return targetingConditions.test(this, livingEntity);
    }

    public boolean canBeSeenAsEnemy() {
        return !this.isInvulnerable() && this.canBeSeenByAnyone();
    }

    public boolean canBeSeenByAnyone() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean areAllEffectsAmbient(Collection<MobEffectInstance> collection) {
        for (MobEffectInstance mobEffectInstance : collection) {
            if (!mobEffectInstance.isVisible() || mobEffectInstance.isAmbient()) continue;
            return false;
        }
        return true;
    }

    protected void removeEffectParticles() {
        this.entityData.set(DATA_EFFECT_PARTICLES, List.of());
    }

    public boolean removeAllEffects() {
        if (this.level().isClientSide) {
            return false;
        }
        Iterator<MobEffectInstance> iterator = this.activeEffects.values().iterator();
        boolean bl = false;
        while (iterator.hasNext()) {
            this.onEffectRemoved(iterator.next());
            iterator.remove();
            bl = true;
        }
        return bl;
    }

    public Collection<MobEffectInstance> getActiveEffects() {
        return this.activeEffects.values();
    }

    public Map<Holder<MobEffect>, MobEffectInstance> getActiveEffectsMap() {
        return this.activeEffects;
    }

    public boolean hasEffect(Holder<MobEffect> holder) {
        return this.activeEffects.containsKey(holder);
    }

    @Nullable
    public MobEffectInstance getEffect(Holder<MobEffect> holder) {
        return this.activeEffects.get(holder);
    }

    public final boolean addEffect(MobEffectInstance mobEffectInstance) {
        return this.addEffect(mobEffectInstance, null);
    }

    public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (!this.canBeAffected(mobEffectInstance)) {
            return false;
        }
        MobEffectInstance mobEffectInstance2 = this.activeEffects.get(mobEffectInstance.getEffect());
        boolean bl = false;
        if (mobEffectInstance2 == null) {
            this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
            this.onEffectAdded(mobEffectInstance, entity);
            bl = true;
            mobEffectInstance.onEffectAdded(this);
        } else if (mobEffectInstance2.update(mobEffectInstance)) {
            this.onEffectUpdated(mobEffectInstance2, true, entity);
            bl = true;
        }
        mobEffectInstance.onEffectStarted(this);
        return bl;
    }

    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (this.getType().is(EntityTypeTags.IMMUNE_TO_INFESTED)) {
            return !mobEffectInstance.is(MobEffects.INFESTED);
        }
        if (this.getType().is(EntityTypeTags.IMMUNE_TO_OOZING)) {
            return !mobEffectInstance.is(MobEffects.OOZING);
        }
        if (this.getType().is(EntityTypeTags.IGNORES_POISON_AND_REGEN)) {
            return !mobEffectInstance.is(MobEffects.REGENERATION) && !mobEffectInstance.is(MobEffects.POISON);
        }
        return true;
    }

    public void forceAddEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (!this.canBeAffected(mobEffectInstance)) {
            return;
        }
        MobEffectInstance mobEffectInstance2 = this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
        if (mobEffectInstance2 == null) {
            this.onEffectAdded(mobEffectInstance, entity);
        } else {
            mobEffectInstance.copyBlendState(mobEffectInstance2);
            this.onEffectUpdated(mobEffectInstance, true, entity);
        }
    }

    public boolean isInvertedHealAndHarm() {
        return this.getType().is(EntityTypeTags.INVERTED_HEALING_AND_HARM);
    }

    @Nullable
    public MobEffectInstance removeEffectNoUpdate(Holder<MobEffect> holder) {
        return this.activeEffects.remove(holder);
    }

    public boolean removeEffect(Holder<MobEffect> holder) {
        MobEffectInstance mobEffectInstance = this.removeEffectNoUpdate(holder);
        if (mobEffectInstance != null) {
            this.onEffectRemoved(mobEffectInstance);
            return true;
        }
        return false;
    }

    protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        this.effectsDirty = true;
        if (!this.level().isClientSide) {
            mobEffectInstance.getEffect().value().addAttributeModifiers(this.getAttributes(), mobEffectInstance.getAmplifier());
            this.sendEffectToPassengers(mobEffectInstance);
        }
    }

    public void sendEffectToPassengers(MobEffectInstance mobEffectInstance) {
        for (Entity entity : this.getPassengers()) {
            if (!(entity instanceof ServerPlayer)) continue;
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance, false));
        }
    }

    protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl, @Nullable Entity entity) {
        this.effectsDirty = true;
        if (bl && !this.level().isClientSide) {
            MobEffect mobEffect = mobEffectInstance.getEffect().value();
            mobEffect.removeAttributeModifiers(this.getAttributes());
            mobEffect.addAttributeModifiers(this.getAttributes(), mobEffectInstance.getAmplifier());
            this.refreshDirtyAttributes();
        }
        if (!this.level().isClientSide) {
            this.sendEffectToPassengers(mobEffectInstance);
        }
    }

    protected void onEffectRemoved(MobEffectInstance mobEffectInstance) {
        this.effectsDirty = true;
        if (!this.level().isClientSide) {
            mobEffectInstance.getEffect().value().removeAttributeModifiers(this.getAttributes());
            this.refreshDirtyAttributes();
            for (Entity entity : this.getPassengers()) {
                if (!(entity instanceof ServerPlayer)) continue;
                ServerPlayer serverPlayer = (ServerPlayer)entity;
                serverPlayer.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobEffectInstance.getEffect()));
            }
        }
    }

    private void refreshDirtyAttributes() {
        Set<AttributeInstance> set = this.getAttributes().getAttributesToUpdate();
        for (AttributeInstance attributeInstance : set) {
            this.onAttributeUpdated(attributeInstance.getAttribute());
        }
        set.clear();
    }

    private void onAttributeUpdated(Holder<Attribute> holder) {
        if (holder.is(Attributes.MAX_HEALTH)) {
            float f = this.getMaxHealth();
            if (this.getHealth() > f) {
                this.setHealth(f);
            }
        } else if (holder.is(Attributes.MAX_ABSORPTION)) {
            float f = this.getMaxAbsorption();
            if (this.getAbsorptionAmount() > f) {
                this.setAbsorptionAmount(f);
            }
        }
    }

    public void heal(float f) {
        float f2 = this.getHealth();
        if (f2 > 0.0f) {
            this.setHealth(f2 + f);
        }
    }

    public float getHealth() {
        return this.entityData.get(DATA_HEALTH_ID).floatValue();
    }

    public void setHealth(float f) {
        this.entityData.set(DATA_HEALTH_ID, Float.valueOf(Mth.clamp(f, 0.0f, this.getMaxHealth())));
    }

    public boolean isDeadOrDying() {
        return this.getHealth() <= 0.0f;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        boolean bl;
        Entity entity;
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (this.level().isClientSide) {
            return false;
        }
        if (this.isDeadOrDying()) {
            return false;
        }
        if (damageSource.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (this.isSleeping() && !this.level().isClientSide) {
            this.stopSleeping();
        }
        this.noActionTime = 0;
        float f2 = f;
        boolean bl2 = false;
        float f3 = 0.0f;
        if (f > 0.0f && this.isDamageSourceBlocked(damageSource)) {
            Entity entity2;
            this.hurtCurrentlyUsedShield(f);
            f3 = f;
            f = 0.0f;
            if (!damageSource.is(DamageTypeTags.IS_PROJECTILE) && (entity2 = damageSource.getDirectEntity()) instanceof LivingEntity) {
                entity = (LivingEntity)entity2;
                this.blockUsingShield((LivingEntity)entity);
            }
            bl2 = true;
        }
        if (damageSource.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            f *= 5.0f;
        }
        if (damageSource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.hurtHelmet(damageSource, f);
            f *= 0.75f;
        }
        this.walkAnimation.setSpeed(1.5f);
        boolean bl3 = true;
        if ((float)this.invulnerableTime > 10.0f && !damageSource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (f <= this.lastHurt) {
                return false;
            }
            this.actuallyHurt(damageSource, f - this.lastHurt);
            this.lastHurt = f;
            bl3 = false;
        } else {
            this.lastHurt = f;
            this.invulnerableTime = 20;
            this.actuallyHurt(damageSource, f);
            this.hurtTime = this.hurtDuration = 10;
        }
        entity = damageSource.getEntity();
        if (entity != null) {
            Object object;
            Entity entity3;
            if (entity instanceof LivingEntity) {
                entity3 = entity;
                if (!(damageSource.is(DamageTypeTags.NO_ANGER) || damageSource.is(DamageTypes.WIND_CHARGE) && this.getType().is(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE))) {
                    this.setLastHurtByMob((LivingEntity)entity3);
                }
            }
            if (entity instanceof Player) {
                entity3 = (Player)entity;
                this.lastHurtByPlayerTime = 100;
                this.lastHurtByPlayer = entity3;
            } else if (entity instanceof Wolf && ((TamableAnimal)(object = (Wolf)entity)).isTame()) {
                Player object2;
                this.lastHurtByPlayerTime = 100;
                LivingEntity livingEntity = object.getOwner();
                this.lastHurtByPlayer = livingEntity instanceof Player ? (object2 = (Player)livingEntity) : null;
            }
        }
        if (bl3) {
            if (bl2) {
                this.level().broadcastEntityEvent(this, (byte)29);
            } else {
                this.level().broadcastDamageEvent(this, damageSource);
            }
            if (!(damageSource.is(DamageTypeTags.NO_IMPACT) || bl2 && !(f > 0.0f))) {
                this.markHurt();
            }
            if (!damageSource.is(DamageTypeTags.NO_KNOCKBACK)) {
                double d = 0.0;
                double d2 = 0.0;
                Entity entity2 = damageSource.getDirectEntity();
                if (entity2 instanceof Projectile) {
                    Projectile projectile = (Projectile)entity2;
                    entity2 = projectile.calculateHorizontalHurtKnockbackDirection(this, damageSource);
                    d = -entity2.leftDouble();
                    d2 = -entity2.rightDouble();
                } else if (damageSource.getSourcePosition() != null) {
                    d = damageSource.getSourcePosition().x() - this.getX();
                    d2 = damageSource.getSourcePosition().z() - this.getZ();
                }
                this.knockback(0.4f, d, d2);
                if (!bl2) {
                    this.indicateDamage(d, d2);
                }
            }
        }
        if (this.isDeadOrDying()) {
            if (!this.checkTotemDeathProtection(damageSource)) {
                if (bl3) {
                    this.makeSound(this.getDeathSound());
                }
                this.die(damageSource);
            }
        } else if (bl3) {
            this.playHurtSound(damageSource);
        }
        boolean bl4 = bl = !bl2 || f > 0.0f;
        if (bl) {
            this.lastDamageSource = damageSource;
            this.lastDamageStamp = this.level().getGameTime();
            for (MobEffectInstance mobEffectInstance : this.getActiveEffects()) {
                mobEffectInstance.onMobHurt(this, damageSource, f);
            }
        }
        if (this instanceof ServerPlayer) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)this, damageSource, f2, f, bl2);
            if (f3 > 0.0f && f3 < 3.4028235E37f) {
                ((ServerPlayer)this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f3 * 10.0f));
            }
        }
        if (entity instanceof ServerPlayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity, this, damageSource, f2, f, bl2);
        }
        return bl;
    }

    protected void blockUsingShield(LivingEntity livingEntity) {
        livingEntity.blockedByShield(this);
    }

    protected void blockedByShield(LivingEntity livingEntity) {
        livingEntity.knockback(0.5, livingEntity.getX() - this.getX(), livingEntity.getZ() - this.getZ());
    }

    private boolean checkTotemDeathProtection(DamageSource damageSource) {
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        ItemStack itemStack = null;
        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemStack2 = this.getItemInHand(interactionHand);
            if (!itemStack2.is(Items.TOTEM_OF_UNDYING)) continue;
            itemStack = itemStack2.copy();
            itemStack2.shrink(1);
            break;
        }
        if (itemStack != null) {
            LivingEntity livingEntity = this;
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                serverPlayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, itemStack);
                this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
            this.setHealth(1.0f);
            this.removeAllEffects();
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            this.level().broadcastEntityEvent(this, (byte)35);
        }
        return itemStack != null;
    }

    @Nullable
    public DamageSource getLastDamageSource() {
        if (this.level().getGameTime() - this.lastDamageStamp > 40L) {
            this.lastDamageSource = null;
        }
        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource damageSource) {
        this.makeSound(this.getHurtSound(damageSource));
    }

    public void makeSound(@Nullable SoundEvent soundEvent) {
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    public boolean isDamageSourceBlocked(DamageSource damageSource) {
        Object object;
        Entity entity = damageSource.getDirectEntity();
        boolean bl = false;
        if (entity instanceof AbstractArrow && ((AbstractArrow)(object = (AbstractArrow)entity)).getPierceLevel() > 0) {
            bl = true;
        }
        if (!damageSource.is(DamageTypeTags.BYPASSES_SHIELD) && this.isBlocking() && !bl && (object = damageSource.getSourcePosition()) != null) {
            Vec3 vec3 = this.calculateViewVector(0.0f, this.getYHeadRot());
            Vec3 vec32 = ((Vec3)object).vectorTo(this.position());
            vec32 = new Vec3(vec32.x, 0.0, vec32.z).normalize();
            return vec32.dot(vec3) < 0.0;
        }
        return false;
    }

    private void breakItem(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), itemStack.getBreakingSound(), this.getSoundSource(), 0.8f, 0.8f + this.level().random.nextFloat() * 0.4f, false);
            }
            this.spawnItemParticles(itemStack, 5);
        }
    }

    public void die(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity entity = damageSource.getEntity();
        LivingEntity livingEntity = this.getKillCredit();
        if (this.deathScore >= 0 && livingEntity != null) {
            livingEntity.awardKillScore(this, this.deathScore, damageSource);
        }
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        if (!this.level().isClientSide && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getCombatTracker().getDeathMessage().getString());
        }
        this.dead = true;
        this.getCombatTracker().recheckStatus();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (entity == null || entity.killedEntity(serverLevel, this)) {
                this.gameEvent(GameEvent.ENTITY_DIE);
                this.dropAllDeathLoot(serverLevel, damageSource);
                this.createWitherRose(livingEntity);
            }
            this.level().broadcastEntityEvent(this, (byte)3);
        }
        this.setPose(Pose.DYING);
    }

    protected void createWitherRose(@Nullable LivingEntity livingEntity) {
        if (this.level().isClientSide) {
            return;
        }
        boolean bl = false;
        if (livingEntity instanceof WitherBoss) {
            Object object;
            if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                object = this.blockPosition();
                BlockState blockState = Blocks.WITHER_ROSE.defaultBlockState();
                if (this.level().getBlockState((BlockPos)object).isAir() && blockState.canSurvive(this.level(), (BlockPos)object)) {
                    this.level().setBlock((BlockPos)object, blockState, 3);
                    bl = true;
                }
            }
            if (!bl) {
                object = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                this.level().addFreshEntity((Entity)object);
            }
        }
    }

    protected void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource) {
        boolean bl;
        boolean bl2 = bl = this.lastHurtByPlayerTime > 0;
        if (this.shouldDropLoot() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.dropFromLootTable(damageSource, bl);
            this.dropCustomDeathLoot(serverLevel, damageSource, bl);
        }
        this.dropEquipment();
        this.dropExperience(damageSource.getEntity());
    }

    protected void dropEquipment() {
    }

    protected void dropExperience(@Nullable Entity entity) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (!this.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
                ExperienceOrb.award(serverLevel, this.position(), this.getExperienceReward(serverLevel, entity));
            }
        }
    }

    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
    }

    public ResourceKey<LootTable> getLootTable() {
        return this.getType().getDefaultLootTable();
    }

    public long getLootTableSeed() {
        return 0L;
    }

    protected float getKnockback(Entity entity, DamageSource damageSource) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return EnchantmentHelper.modifyKnockback(serverLevel, this.getWeaponItem(), entity, damageSource, f);
        }
        return f;
    }

    protected void dropFromLootTable(DamageSource damageSource, boolean bl) {
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        LootTable lootTable = this.level().getServer().reloadableRegistries().getLootTable(resourceKey);
        LootParams.Builder builder = new LootParams.Builder((ServerLevel)this.level()).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damageSource).withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());
        if (bl && this.lastHurtByPlayer != null) {
            builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
        }
        LootParams lootParams = builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(lootParams, this.getLootTableSeed(), this::spawnAtLocation);
    }

    public void knockback(double d, double d2, double d3) {
        if ((d *= 1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        this.hasImpulse = true;
        Vec3 vec3 = this.getDeltaMovement();
        while (d2 * d2 + d3 * d3 < (double)1.0E-5f) {
            d2 = (Math.random() - Math.random()) * 0.01;
            d3 = (Math.random() - Math.random()) * 0.01;
        }
        Vec3 vec32 = new Vec3(d2, 0.0, d3).normalize().scale(d);
        this.setDeltaMovement(vec3.x / 2.0 - vec32.x, this.onGround() ? Math.min(0.4, vec3.y / 2.0 + d) : vec3.y, vec3.z / 2.0 - vec32.z);
    }

    public void indicateDamage(double d, double d2) {
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GENERIC_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    private SoundEvent getFallDamageSound(int n) {
        return n > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void skipDropExperience() {
        this.skipDropExperience = true;
    }

    public boolean wasExperienceConsumed() {
        return this.skipDropExperience;
    }

    public float getHurtDir() {
        return 0.0f;
    }

    protected AABB getHitbox() {
        AABB aABB = this.getBoundingBox();
        Entity entity = this.getVehicle();
        if (entity != null) {
            Vec3 vec3 = entity.getPassengerRidingPosition(this);
            return aABB.setMinY(Math.max(vec3.y, aABB.minY));
        }
        return aABB;
    }

    public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> activeLocationDependentEnchantments() {
        return this.activeLocationDependentEnchantments;
    }

    public Fallsounds getFallSounds() {
        return new Fallsounds(SoundEvents.GENERIC_SMALL_FALL, SoundEvents.GENERIC_BIG_FALL);
    }

    protected SoundEvent getDrinkingSound(ItemStack itemStack) {
        return itemStack.getDrinkingSound();
    }

    public SoundEvent getEatingSound(ItemStack itemStack) {
        return itemStack.getEatingSound();
    }

    public Optional<BlockPos> getLastClimbablePos() {
        return this.lastClimbablePos;
    }

    public boolean onClimbable() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos blockPos = this.blockPosition();
        BlockState blockState = this.getInBlockState();
        if (blockState.is(BlockTags.CLIMBABLE)) {
            this.lastClimbablePos = Optional.of(blockPos);
            return true;
        }
        if (blockState.getBlock() instanceof TrapDoorBlock && this.trapdoorUsableAsLadder(blockPos, blockState)) {
            this.lastClimbablePos = Optional.of(blockPos);
            return true;
        }
        return false;
    }

    private boolean trapdoorUsableAsLadder(BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(TrapDoorBlock.OPEN).booleanValue()) {
            BlockState blockState2 = this.level().getBlockState(blockPos.below());
            return blockState2.is(Blocks.LADDER) && blockState2.getValue(LadderBlock.FACING) == blockState.getValue(TrapDoorBlock.FACING);
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0f;
    }

    @Override
    public int getMaxFallDistance() {
        return this.getComfortableFallDistance(0.0f);
    }

    protected final int getComfortableFallDistance(float f) {
        return Mth.floor(f + 3.0f);
    }

    @Override
    public boolean causeFallDamage(float f, float f2, DamageSource damageSource) {
        boolean bl = super.causeFallDamage(f, f2, damageSource);
        int n = this.calculateFallDamage(f, f2);
        if (n > 0) {
            this.playSound(this.getFallDamageSound(n), 1.0f, 1.0f);
            this.playBlockFallSound();
            this.hurt(damageSource, n);
            return true;
        }
        return bl;
    }

    protected int calculateFallDamage(float f, float f2) {
        if (this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        float f3 = (float)this.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
        float f4 = f - f3;
        return Mth.ceil((double)(f4 * f2) * this.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER));
    }

    protected void playBlockFallSound() {
        if (this.isSilent()) {
            return;
        }
        int n = Mth.floor(this.getX());
        int n2 = Mth.floor(this.getY() - (double)0.2f);
        int n3 = Mth.floor(this.getZ());
        BlockState blockState = this.level().getBlockState(new BlockPos(n, n2, n3));
        if (!blockState.isAir()) {
            SoundType soundType = blockState.getSoundType();
            this.playSound(soundType.getFallSound(), soundType.getVolume() * 0.5f, soundType.getPitch() * 0.75f);
        }
    }

    @Override
    public void animateHurt(float f) {
        this.hurtTime = this.hurtDuration = 10;
    }

    public int getArmorValue() {
        return Mth.floor(this.getAttributeValue(Attributes.ARMOR));
    }

    protected void hurtArmor(DamageSource damageSource, float f) {
    }

    protected void hurtHelmet(DamageSource damageSource, float f) {
    }

    protected void hurtCurrentlyUsedShield(float f) {
    }

    protected void doHurtEquipment(DamageSource damageSource, float f, EquipmentSlot ... equipmentSlotArray) {
        if (f <= 0.0f) {
            return;
        }
        int n = (int)Math.max(1.0f, f / 4.0f);
        for (EquipmentSlot equipmentSlot : equipmentSlotArray) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (!(itemStack.getItem() instanceof ArmorItem) || !itemStack.canBeHurtBy(damageSource)) continue;
            itemStack.hurtAndBreak(n, this, equipmentSlot);
        }
    }

    protected float getDamageAfterArmorAbsorb(DamageSource damageSource, float f) {
        if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
            this.hurtArmor(damageSource, f);
            f = CombatRules.getDamageAfterAbsorb(this, f, damageSource, this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }
        return f;
    }

    protected float getDamageAfterMagicAbsorb(DamageSource damageSource, float f) {
        float f2;
        int n;
        int n2;
        float f3;
        float f4;
        float f5;
        if (damageSource.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return f;
        }
        if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !damageSource.is(DamageTypeTags.BYPASSES_RESISTANCE) && (f5 = (f4 = f) - (f = Math.max((f3 = f * (float)(n2 = 25 - (n = (this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5))) / 25.0f, 0.0f))) > 0.0f && f5 < 3.4028235E37f) {
            if (this instanceof ServerPlayer) {
                ((ServerPlayer)this).awardStat(Stats.DAMAGE_RESISTED, Math.round(f5 * 10.0f));
            } else if (damageSource.getEntity() instanceof ServerPlayer) {
                ((ServerPlayer)damageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f5 * 10.0f));
            }
        }
        if (f <= 0.0f) {
            return 0.0f;
        }
        if (damageSource.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return f;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            f2 = EnchantmentHelper.getDamageProtection(serverLevel, this, damageSource);
        } else {
            f2 = 0.0f;
        }
        if (f2 > 0.0f) {
            f = CombatRules.getDamageAfterMagicAbsorb(f, f2);
        }
        return f;
    }

    protected void actuallyHurt(DamageSource damageSource, float f) {
        Entity entity;
        if (this.isInvulnerableTo(damageSource)) {
            return;
        }
        f = this.getDamageAfterArmorAbsorb(damageSource, f);
        float f2 = f = this.getDamageAfterMagicAbsorb(damageSource, f);
        f = Math.max(f - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (f2 - f));
        float f3 = f2 - f;
        if (f3 > 0.0f && f3 < 3.4028235E37f && (entity = damageSource.getEntity()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f3 * 10.0f));
        }
        if (f == 0.0f) {
            return;
        }
        this.getCombatTracker().recordDamage(damageSource, f);
        this.setHealth(this.getHealth() - f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - f);
        this.gameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    @Nullable
    public LivingEntity getKillCredit() {
        if (this.lastHurtByPlayer != null) {
            return this.lastHurtByPlayer;
        }
        if (this.lastHurtByMob != null) {
            return this.lastHurtByMob;
        }
        return null;
    }

    public final float getMaxHealth() {
        return (float)this.getAttributeValue(Attributes.MAX_HEALTH);
    }

    public final float getMaxAbsorption() {
        return (float)this.getAttributeValue(Attributes.MAX_ABSORPTION);
    }

    public final int getArrowCount() {
        return this.entityData.get(DATA_ARROW_COUNT_ID);
    }

    public final void setArrowCount(int n) {
        this.entityData.set(DATA_ARROW_COUNT_ID, n);
    }

    public final int getStingerCount() {
        return this.entityData.get(DATA_STINGER_COUNT_ID);
    }

    public final void setStingerCount(int n) {
        this.entityData.set(DATA_STINGER_COUNT_ID, n);
    }

    private int getCurrentSwingDuration() {
        if (MobEffectUtil.hasDigSpeed(this)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
        }
        if (this.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            return 6 + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2;
        }
        return 6;
    }

    public void swing(InteractionHand interactionHand) {
        this.swing(interactionHand, false);
    }

    public void swing(InteractionHand interactionHand, boolean bl) {
        if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
            this.swingTime = -1;
            this.swinging = true;
            this.swingingArm = interactionHand;
            if (this.level() instanceof ServerLevel) {
                ClientboundAnimatePacket clientboundAnimatePacket = new ClientboundAnimatePacket(this, interactionHand == InteractionHand.MAIN_HAND ? 0 : 3);
                ServerChunkCache serverChunkCache = ((ServerLevel)this.level()).getChunkSource();
                if (bl) {
                    serverChunkCache.broadcastAndSend(this, clientboundAnimatePacket);
                } else {
                    serverChunkCache.broadcast(this, clientboundAnimatePacket);
                }
            }
        }
    }

    @Override
    public void handleDamageEvent(DamageSource damageSource) {
        this.walkAnimation.setSpeed(1.5f);
        this.invulnerableTime = 20;
        this.hurtTime = this.hurtDuration = 10;
        SoundEvent soundEvent = this.getHurtSound(damageSource);
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
        this.hurt(this.damageSources().generic(), 0.0f);
        this.lastDamageSource = damageSource;
        this.lastDamageStamp = this.level().getGameTime();
    }

    @Override
    public void handleEntityEvent(byte by) {
        switch (by) {
            case 3: {
                SoundEvent soundEvent = this.getDeathSound();
                if (soundEvent != null) {
                    this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                }
                if (this instanceof Player) break;
                this.setHealth(0.0f);
                this.die(this.damageSources().generic());
                break;
            }
            case 30: {
                this.playSound(SoundEvents.SHIELD_BREAK, 0.8f, 0.8f + this.level().random.nextFloat() * 0.4f);
                break;
            }
            case 29: {
                this.playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 0.8f + this.level().random.nextFloat() * 0.4f);
                break;
            }
            case 46: {
                int n = 128;
                for (int i = 0; i < 128; ++i) {
                    double d = (double)i / 127.0;
                    float f = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float f2 = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float f3 = (this.random.nextFloat() - 0.5f) * 0.2f;
                    double d2 = Mth.lerp(d, this.xo, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    double d3 = Mth.lerp(d, this.yo, this.getY()) + this.random.nextDouble() * (double)this.getBbHeight();
                    double d4 = Mth.lerp(d, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    this.level().addParticle(ParticleTypes.PORTAL, d2, d3, d4, f, f2, f3);
                }
                break;
            }
            case 47: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
                break;
            }
            case 48: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.OFFHAND));
                break;
            }
            case 49: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.HEAD));
                break;
            }
            case 50: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.CHEST));
                break;
            }
            case 51: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.LEGS));
                break;
            }
            case 52: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.FEET));
                break;
            }
            case 65: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.BODY));
                break;
            }
            case 54: {
                HoneyBlock.showJumpParticles(this);
                break;
            }
            case 55: {
                this.swapHandItems();
                break;
            }
            case 60: {
                this.makePoofParticles();
                break;
            }
            default: {
                super.handleEntityEvent(by);
            }
        }
    }

    private void makePoofParticles() {
        for (int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            double d3 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.POOF, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), d, d2, d3);
        }
    }

    private void swapHandItems() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
        this.setItemSlot(EquipmentSlot.OFFHAND, this.getItemBySlot(EquipmentSlot.MAINHAND));
        this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
    }

    @Override
    protected void onBelowWorld() {
        this.hurt(this.damageSources().fellOutOfWorld(), 4.0f);
    }

    protected void updateSwingTime() {
        int n = this.getCurrentSwingDuration();
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= n) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }
        this.attackAnim = (float)this.swingTime / (float)n;
    }

    @Nullable
    public AttributeInstance getAttribute(Holder<Attribute> holder) {
        return this.getAttributes().getInstance(holder);
    }

    public double getAttributeValue(Holder<Attribute> holder) {
        return this.getAttributes().getValue(holder);
    }

    public double getAttributeBaseValue(Holder<Attribute> holder) {
        return this.getAttributes().getBaseValue(holder);
    }

    public AttributeMap getAttributes() {
        return this.attributes;
    }

    public ItemStack getMainHandItem() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffhandItem() {
        return this.getItemBySlot(EquipmentSlot.OFFHAND);
    }

    @Override
    @Nonnull
    public ItemStack getWeaponItem() {
        return this.getMainHandItem();
    }

    public boolean isHolding(Item item) {
        return this.isHolding((ItemStack itemStack) -> itemStack.is(item));
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandItem()) || predicate.test(this.getOffhandItem());
    }

    public ItemStack getItemInHand(InteractionHand interactionHand) {
        if (interactionHand == InteractionHand.MAIN_HAND) {
            return this.getItemBySlot(EquipmentSlot.MAINHAND);
        }
        if (interactionHand == InteractionHand.OFF_HAND) {
            return this.getItemBySlot(EquipmentSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)interactionHand));
    }

    public void setItemInHand(InteractionHand interactionHand, ItemStack itemStack) {
        if (interactionHand == InteractionHand.MAIN_HAND) {
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        } else if (interactionHand == InteractionHand.OFF_HAND) {
            this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
        } else {
            throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)interactionHand));
        }
    }

    public boolean hasItemInSlot(EquipmentSlot equipmentSlot) {
        return !this.getItemBySlot(equipmentSlot).isEmpty();
    }

    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return false;
    }

    public abstract Iterable<ItemStack> getArmorSlots();

    public abstract ItemStack getItemBySlot(EquipmentSlot var1);

    public abstract void setItemSlot(EquipmentSlot var1, ItemStack var2);

    public Iterable<ItemStack> getHandSlots() {
        return List.of();
    }

    public Iterable<ItemStack> getArmorAndBodyArmorSlots() {
        return this.getArmorSlots();
    }

    public Iterable<ItemStack> getAllSlots() {
        return Iterables.concat(this.getHandSlots(), this.getArmorAndBodyArmorSlots());
    }

    protected void verifyEquippedItem(ItemStack itemStack) {
        itemStack.getItem().verifyComponentsAfterLoad(itemStack);
    }

    public float getArmorCoverPercentage() {
        Iterable<ItemStack> iterable = this.getArmorSlots();
        int n = 0;
        int n2 = 0;
        for (ItemStack itemStack : iterable) {
            if (!itemStack.isEmpty()) {
                ++n2;
            }
            ++n;
        }
        return n > 0 ? (float)n2 / (float)n : 0.0f;
    }

    @Override
    public void setSprinting(boolean bl) {
        super.setSprinting(bl);
        AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        attributeInstance.removeModifier(SPEED_MODIFIER_SPRINTING.id());
        if (bl) {
            attributeInstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
        }
    }

    protected float getSoundVolume() {
        return 1.0f;
    }

    public float getVoicePitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isSleeping()) {
            super.push(entity);
        }
    }

    private void dismountVehicle(Entity entity) {
        Vec3 vec3;
        if (this.isRemoved()) {
            vec3 = this.position();
        } else if (entity.isRemoved() || this.level().getBlockState(entity.blockPosition()).is(BlockTags.PORTALS)) {
            double d = Math.max(this.getY(), entity.getY());
            vec3 = new Vec3(this.getX(), d, this.getZ());
        } else {
            vec3 = entity.getDismountLocationForPassenger(this);
        }
        this.dismountTo(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public boolean shouldShowName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpPower() {
        return this.getJumpPower(1.0f);
    }

    protected float getJumpPower(float f) {
        return (float)this.getAttributeValue(Attributes.JUMP_STRENGTH) * f * this.getBlockJumpFactor() + this.getJumpBoostPower();
    }

    public float getJumpBoostPower() {
        return this.hasEffect(MobEffects.JUMP) ? 0.1f * ((float)this.getEffect(MobEffects.JUMP).getAmplifier() + 1.0f) : 0.0f;
    }

    @VisibleForTesting
    public void jumpFromGround() {
        float f = this.getJumpPower();
        if (f <= 1.0E-5f) {
            return;
        }
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, f, vec3.z);
        if (this.isSprinting()) {
            float f2 = this.getYRot() * ((float)Math.PI / 180);
            this.addDeltaMovement(new Vec3((double)(-Mth.sin(f2)) * 0.2, 0.0, (double)Mth.cos(f2) * 0.2));
        }
        this.hasImpulse = true;
    }

    protected void goDownInWater() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04f, 0.0));
    }

    protected void jumpInLiquid(TagKey<Fluid> tagKey) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04f, 0.0));
    }

    protected float getWaterSlowDown() {
        return 0.8f;
    }

    public boolean canStandOnFluid(FluidState fluidState) {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return this.getAttributeValue(Attributes.GRAVITY);
    }

    public void travel(Vec3 vec3) {
        if (this.isControlledByLocalInstance()) {
            boolean bl;
            double d = this.getGravity();
            boolean bl2 = bl = this.getDeltaMovement().y <= 0.0;
            if (bl && this.hasEffect(MobEffects.SLOW_FALLING)) {
                d = Math.min(d, 0.01);
            }
            FluidState fluidState = this.level().getFluidState(this.blockPosition());
            if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
                double d2 = this.getY();
                float f = this.isSprinting() ? 0.9f : this.getWaterSlowDown();
                float f2 = 0.02f;
                float f3 = (float)this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
                if (!this.onGround()) {
                    f3 *= 0.5f;
                }
                if (f3 > 0.0f) {
                    f += (0.54600006f - f) * f3;
                    f2 += (this.getSpeed() - f2) * f3;
                }
                if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    f = 0.96f;
                }
                this.moveRelative(f2, vec3);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 vec32 = this.getDeltaMovement();
                if (this.horizontalCollision && this.onClimbable()) {
                    vec32 = new Vec3(vec32.x, 0.2, vec32.z);
                }
                this.setDeltaMovement(vec32.multiply(f, 0.8f, f));
                Vec3 vec33 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
                this.setDeltaMovement(vec33);
                if (this.horizontalCollision && this.isFree(vec33.x, vec33.y + (double)0.6f - this.getY() + d2, vec33.z)) {
                    this.setDeltaMovement(vec33.x, 0.3f, vec33.z);
                }
            } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
                Vec3 vec34;
                double d3 = this.getY();
                this.moveRelative(0.02f, vec3);
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8f, 0.5));
                    vec34 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
                    this.setDeltaMovement(vec34);
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
                }
                if (d != 0.0) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d / 4.0, 0.0));
                }
                vec34 = this.getDeltaMovement();
                if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + (double)0.6f - this.getY() + d3, vec34.z)) {
                    this.setDeltaMovement(vec34.x, 0.3f, vec34.z);
                }
            } else if (this.isFallFlying()) {
                double d4;
                float f;
                double d5;
                this.checkSlowFallDistance();
                Vec3 vec35 = this.getDeltaMovement();
                Vec3 vec36 = this.getLookAngle();
                float f4 = this.getXRot() * ((float)Math.PI / 180);
                double d6 = Math.sqrt(vec36.x * vec36.x + vec36.z * vec36.z);
                double d7 = vec35.horizontalDistance();
                double d8 = vec36.length();
                double d9 = Math.cos(f4);
                d9 = d9 * d9 * Math.min(1.0, d8 / 0.4);
                vec35 = this.getDeltaMovement().add(0.0, d * (-1.0 + d9 * 0.75), 0.0);
                if (vec35.y < 0.0 && d6 > 0.0) {
                    d5 = vec35.y * -0.1 * d9;
                    vec35 = vec35.add(vec36.x * d5 / d6, d5, vec36.z * d5 / d6);
                }
                if (f4 < 0.0f && d6 > 0.0) {
                    d5 = d7 * (double)(-Mth.sin(f4)) * 0.04;
                    vec35 = vec35.add(-vec36.x * d5 / d6, d5 * 3.2, -vec36.z * d5 / d6);
                }
                if (d6 > 0.0) {
                    vec35 = vec35.add((vec36.x / d6 * d7 - vec35.x) * 0.1, 0.0, (vec36.z / d6 * d7 - vec35.z) * 0.1);
                }
                this.setDeltaMovement(vec35.multiply(0.99f, 0.98f, 0.99f));
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.horizontalCollision && !this.level().isClientSide && (f = (float)((d4 = d7 - (d5 = this.getDeltaMovement().horizontalDistance())) * 10.0 - 3.0)) > 0.0f) {
                    this.playSound(this.getFallDamageSound((int)f), 1.0f, 1.0f);
                    this.hurt(this.damageSources().flyIntoWall(), f);
                }
                if (this.onGround() && !this.level().isClientSide) {
                    this.setSharedFlag(7, false);
                }
            } else {
                BlockPos blockPos = this.getBlockPosBelowThatAffectsMyMovement();
                float f = this.level().getBlockState(blockPos).getBlock().getFriction();
                float f5 = this.onGround() ? f * 0.91f : 0.91f;
                Vec3 vec37 = this.handleRelativeFrictionAndCalculateMovement(vec3, f);
                double d10 = vec37.y;
                d10 = this.hasEffect(MobEffects.LEVITATION) ? (d10 += (0.05 * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec37.y) * 0.2) : (!this.level().isClientSide || this.level().hasChunkAt(blockPos) ? (d10 -= d) : (this.getY() > (double)this.level().getMinBuildHeight() ? -0.1 : 0.0));
                if (this.shouldDiscardFriction()) {
                    this.setDeltaMovement(vec37.x, d10, vec37.z);
                } else {
                    this.setDeltaMovement(vec37.x * (double)f5, this instanceof FlyingAnimal ? d10 * (double)f5 : d10 * (double)0.98f, vec37.z * (double)f5);
                }
            }
        }
        this.calculateEntityAnimation(this instanceof FlyingAnimal);
    }

    private void travelRidden(Player player, Vec3 vec3) {
        Vec3 vec32 = this.getRiddenInput(player, vec3);
        this.tickRidden(player, vec32);
        if (this.isControlledByLocalInstance()) {
            this.setSpeed(this.getRiddenSpeed(player));
            this.travel(vec32);
        } else {
            this.calculateEntityAnimation(false);
            this.setDeltaMovement(Vec3.ZERO);
            this.tryCheckInsideBlocks();
        }
    }

    protected void tickRidden(Player player, Vec3 vec3) {
    }

    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        return vec3;
    }

    protected float getRiddenSpeed(Player player) {
        return this.getSpeed();
    }

    public void calculateEntityAnimation(boolean bl) {
        float f = (float)Mth.length(this.getX() - this.xo, bl ? this.getY() - this.yo : 0.0, this.getZ() - this.zo);
        this.updateWalkAnimation(f);
    }

    protected void updateWalkAnimation(float f) {
        float f2 = Math.min(f * 4.0f, 1.0f);
        this.walkAnimation.update(f2, 0.4f);
    }

    public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 vec3, float f) {
        this.moveRelative(this.getFrictionInfluencedSpeed(f), vec3);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 vec32 = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.getInBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
            vec32 = new Vec3(vec32.x, 0.2, vec32.z);
        }
        return vec32;
    }

    public Vec3 getFluidFallingAdjustedMovement(double d, boolean bl, Vec3 vec3) {
        if (d != 0.0 && !this.isSprinting()) {
            double d2 = bl && Math.abs(vec3.y - 0.005) >= 0.003 && Math.abs(vec3.y - d / 16.0) < 0.003 ? -0.003 : vec3.y - d / 16.0;
            return new Vec3(vec3.x, d2, vec3.z);
        }
        return vec3;
    }

    private Vec3 handleOnClimbable(Vec3 vec3) {
        if (this.onClimbable()) {
            this.resetFallDistance();
            float f = 0.15f;
            double d = Mth.clamp(vec3.x, (double)-0.15f, (double)0.15f);
            double d2 = Mth.clamp(vec3.z, (double)-0.15f, (double)0.15f);
            double d3 = Math.max(vec3.y, (double)-0.15f);
            if (d3 < 0.0 && !this.getInBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
                d3 = 0.0;
            }
            vec3 = new Vec3(d, d3, d2);
        }
        return vec3;
    }

    private float getFrictionInfluencedSpeed(float f) {
        if (this.onGround()) {
            return this.getSpeed() * (0.21600002f / (f * f * f));
        }
        return this.getFlyingSpeed();
    }

    protected float getFlyingSpeed() {
        return this.getControllingPassenger() instanceof Player ? this.getSpeed() * 0.1f : 0.02f;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float f) {
        this.speed = f;
    }

    public boolean doHurtTarget(Entity entity) {
        this.setLastHurtMob(entity);
        return false;
    }

    @Override
    public void tick() {
        float f;
        super.tick();
        this.updatingUsingItem();
        this.updateSwimAmount();
        if (!this.level().isClientSide) {
            int n;
            int n2 = this.getArrowCount();
            if (n2 > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - n2);
                }
                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount(n2 - 1);
                }
            }
            if ((n = this.getStingerCount()) > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - n);
                }
                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount(n - 1);
                }
            }
            this.detectEquipmentUpdates();
            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }
            if (this.isSleeping() && !this.checkBedExists()) {
                this.stopSleeping();
            }
        }
        if (!this.isRemoved()) {
            this.aiStep();
        }
        double d = this.getX() - this.xo;
        double d2 = this.getZ() - this.zo;
        float f2 = (float)(d * d + d2 * d2);
        float f3 = this.yBodyRot;
        float f4 = 0.0f;
        this.oRun = this.run;
        float f5 = 0.0f;
        if (f2 > 0.0025000002f) {
            f5 = 1.0f;
            f4 = (float)Math.sqrt(f2) * 3.0f;
            f = (float)Mth.atan2(d2, d) * 57.295776f - 90.0f;
            float f6 = Mth.abs(Mth.wrapDegrees(this.getYRot()) - f);
            f3 = 95.0f < f6 && f6 < 265.0f ? f - 180.0f : f;
        }
        if (this.attackAnim > 0.0f) {
            f3 = this.getYRot();
        }
        if (!this.onGround()) {
            f5 = 0.0f;
        }
        this.run += (f5 - this.run) * 0.3f;
        this.level().getProfiler().push("headTurn");
        f4 = this.tickHeadTurn(f3, f4);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("rangeChecks");
        while (this.getYRot() - this.yRotO < -180.0f) {
            this.yRotO -= 360.0f;
        }
        while (this.getYRot() - this.yRotO >= 180.0f) {
            this.yRotO += 360.0f;
        }
        while (this.yBodyRot - this.yBodyRotO < -180.0f) {
            this.yBodyRotO -= 360.0f;
        }
        while (this.yBodyRot - this.yBodyRotO >= 180.0f) {
            this.yBodyRotO += 360.0f;
        }
        while (this.getXRot() - this.xRotO < -180.0f) {
            this.xRotO -= 360.0f;
        }
        while (this.getXRot() - this.xRotO >= 180.0f) {
            this.xRotO += 360.0f;
        }
        while (this.yHeadRot - this.yHeadRotO < -180.0f) {
            this.yHeadRotO -= 360.0f;
        }
        while (this.yHeadRot - this.yHeadRotO >= 180.0f) {
            this.yHeadRotO += 360.0f;
        }
        this.level().getProfiler().pop();
        this.animStep += f4;
        this.fallFlyTicks = this.isFallFlying() ? ++this.fallFlyTicks : 0;
        if (this.isSleeping()) {
            this.setXRot(0.0f);
        }
        this.refreshDirtyAttributes();
        f = this.getScale();
        if (f != this.appliedScale) {
            this.appliedScale = f;
            this.refreshDimensions();
        }
    }

    private void detectEquipmentUpdates() {
        Map<EquipmentSlot, ItemStack> map = this.collectEquipmentChanges();
        if (map != null) {
            this.handleHandSwap(map);
            if (!map.isEmpty()) {
                this.handleEquipmentChanges(map);
            }
        }
    }

    @Nullable
    private Map<EquipmentSlot, ItemStack> collectEquipmentChanges() {
        Map map = null;
        for (EquipmentSlot object : EquipmentSlot.values()) {
            ItemStack itemStack = switch (object.getType()) {
                default -> throw new MatchException(null, null);
                case EquipmentSlot.Type.HAND -> this.getLastHandItem(object);
                case EquipmentSlot.Type.HUMANOID_ARMOR -> this.getLastArmorItem(object);
                case EquipmentSlot.Type.ANIMAL_ARMOR -> this.lastBodyItemStack;
            };
            ItemStack itemStack2 = this.getItemBySlot(object);
            if (!this.equipmentHasChanged(itemStack, itemStack2)) continue;
            if (map == null) {
                map = Maps.newEnumMap(EquipmentSlot.class);
            }
            map.put(object, itemStack2);
            AttributeMap attributeMap = this.getAttributes();
            if (itemStack.isEmpty()) continue;
            itemStack.forEachModifier(object, (holder, attributeModifier) -> {
                AttributeInstance attributeInstance = attributeMap.getInstance((Holder<Attribute>)holder);
                if (attributeInstance != null) {
                    attributeInstance.removeModifier((AttributeModifier)attributeModifier);
                }
                EnchantmentHelper.stopLocationBasedEffects(itemStack, this, object);
            });
        }
        if (map != null) {
            for (Map.Entry entry : map.entrySet()) {
                EquipmentSlot equipmentSlot = (EquipmentSlot)entry.getKey();
                ItemStack itemStack = (ItemStack)entry.getValue();
                if (itemStack.isEmpty()) continue;
                itemStack.forEachModifier(equipmentSlot, (holder, attributeModifier) -> {
                    Level level;
                    AttributeInstance attributeInstance = this.attributes.getInstance((Holder<Attribute>)holder);
                    if (attributeInstance != null) {
                        attributeInstance.removeModifier(attributeModifier.id());
                        attributeInstance.addTransientModifier((AttributeModifier)attributeModifier);
                    }
                    if ((level = this.level()) instanceof ServerLevel) {
                        ServerLevel serverLevel = (ServerLevel)level;
                        EnchantmentHelper.runLocationChangedEffects(serverLevel, itemStack, this, equipmentSlot);
                    }
                });
            }
        }
        return map;
    }

    public boolean equipmentHasChanged(ItemStack itemStack, ItemStack itemStack2) {
        return !ItemStack.matches(itemStack2, itemStack);
    }

    private void handleHandSwap(Map<EquipmentSlot, ItemStack> map) {
        ItemStack itemStack = map.get(EquipmentSlot.MAINHAND);
        ItemStack itemStack2 = map.get(EquipmentSlot.OFFHAND);
        if (itemStack != null && itemStack2 != null && ItemStack.matches(itemStack, this.getLastHandItem(EquipmentSlot.OFFHAND)) && ItemStack.matches(itemStack2, this.getLastHandItem(EquipmentSlot.MAINHAND))) {
            ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundEntityEventPacket(this, 55));
            map.remove(EquipmentSlot.MAINHAND);
            map.remove(EquipmentSlot.OFFHAND);
            this.setLastHandItem(EquipmentSlot.MAINHAND, itemStack.copy());
            this.setLastHandItem(EquipmentSlot.OFFHAND, itemStack2.copy());
        }
    }

    private void handleEquipmentChanges(Map<EquipmentSlot, ItemStack> map) {
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)map.size());
        map.forEach((equipmentSlot, itemStack) -> {
            ItemStack itemStack2 = itemStack.copy();
            arrayList.add(Pair.of((Object)equipmentSlot, (Object)itemStack2));
            switch (equipmentSlot.getType()) {
                case HAND: {
                    this.setLastHandItem((EquipmentSlot)equipmentSlot, itemStack2);
                    break;
                }
                case HUMANOID_ARMOR: {
                    this.setLastArmorItem((EquipmentSlot)equipmentSlot, itemStack2);
                    break;
                }
                case ANIMAL_ARMOR: {
                    this.lastBodyItemStack = itemStack2;
                }
            }
        });
        ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEquipmentPacket(this.getId(), arrayList));
    }

    private ItemStack getLastArmorItem(EquipmentSlot equipmentSlot) {
        return this.lastArmorItemStacks.get(equipmentSlot.getIndex());
    }

    private void setLastArmorItem(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.lastArmorItemStacks.set(equipmentSlot.getIndex(), itemStack);
    }

    private ItemStack getLastHandItem(EquipmentSlot equipmentSlot) {
        return this.lastHandItemStacks.get(equipmentSlot.getIndex());
    }

    private void setLastHandItem(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.lastHandItemStacks.set(equipmentSlot.getIndex(), itemStack);
    }

    protected float tickHeadTurn(float f, float f2) {
        boolean bl;
        float f3 = Mth.wrapDegrees(f - this.yBodyRot);
        this.yBodyRot += f3 * 0.3f;
        float f4 = Mth.wrapDegrees(this.getYRot() - this.yBodyRot);
        float f5 = this.getMaxHeadRotationRelativeToBody();
        if (Math.abs(f4) > f5) {
            this.yBodyRot += f4 - (float)Mth.sign(f4) * f5;
        }
        boolean bl2 = bl = f4 < -90.0f || f4 >= 90.0f;
        if (bl) {
            f2 *= -1.0f;
        }
        return f2;
    }

    protected float getMaxHeadRotationRelativeToBody() {
        return 50.0f;
    }

    /*
     * Unable to fully structure code
     */
    public void aiStep() {
        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            --this.lerpSteps;
        } else if (!this.isEffectiveAi()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            --this.lerpHeadSteps;
        }
        var1_1 = this.getDeltaMovement();
        var2_2 = var1_1.x;
        var4_3 = var1_1.y;
        var6_4 = var1_1.z;
        if (Math.abs(var1_1.x) < 0.003) {
            var2_2 = 0.0;
        }
        if (Math.abs(var1_1.y) < 0.003) {
            var4_3 = 0.0;
        }
        if (Math.abs(var1_1.z) < 0.003) {
            var6_4 = 0.0;
        }
        this.setDeltaMovement(var2_2, var4_3, var6_4);
        this.level().getProfiler().push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0f;
            this.zza = 0.0f;
        } else if (this.isEffectiveAi()) {
            this.level().getProfiler().push("newAi");
            this.serverAiStep();
            this.level().getProfiler().pop();
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("jump");
        if (this.jumping && this.isAffectedByFluids()) {
            var8_5 = this.isInLava() != false ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER);
            var10_7 = this.isInWater() != false && var8_5 > 0.0;
            var11_10 = this.getFluidJumpThreshold();
            if (var10_7 && (!this.onGround() || var8_5 > var11_10)) {
                this.jumpInLiquid(FluidTags.WATER);
            } else if (this.isInLava() && (!this.onGround() || var8_5 > var11_10)) {
                this.jumpInLiquid(FluidTags.LAVA);
            } else if ((this.onGround() || var10_7 && var8_5 <= var11_10) && this.noJumpDelay == 0) {
                this.jumpFromGround();
                this.noJumpDelay = 10;
            }
        } else {
            this.noJumpDelay = 0;
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("travel");
        this.xxa *= 0.98f;
        this.zza *= 0.98f;
        this.updateFallFlying();
        var8_6 = this.getBoundingBox();
        var9_12 = new Vec3(this.xxa, this.yya, this.zza);
        if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
            this.resetFallDistance();
        }
        if (!((var11_11 = this.getControllingPassenger()) instanceof Player)) ** GOTO lbl-1000
        var10_8 = (Player)var11_11;
        if (this.isAlive()) {
            this.travelRidden(var10_8, var9_12);
        } else lbl-1000:
        // 2 sources

        {
            this.travel(var9_12);
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("freezing");
        if (!this.level().isClientSide && !this.isDeadOrDying()) {
            var10_9 = this.getTicksFrozen();
            if (this.isInPowderSnow && this.canFreeze()) {
                this.setTicksFrozen(Math.min(this.getTicksRequiredToFreeze(), var10_9 + 1));
            } else {
                this.setTicksFrozen(Math.max(0, var10_9 - 2));
            }
        }
        this.removeFrost();
        this.tryAddFrost();
        if (!this.level().isClientSide && this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
            this.hurt(this.damageSources().freeze(), 1.0f);
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            this.checkAutoSpinAttack(var8_6, this.getBoundingBox());
        }
        this.pushEntities();
        this.level().getProfiler().pop();
        if (!this.level().isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
            this.hurt(this.damageSources().drown(), 1.0f);
        }
    }

    public boolean isSensitiveToWater() {
        return false;
    }

    private void updateFallFlying() {
        boolean bl = this.getSharedFlag(7);
        if (bl && !this.onGround() && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.CHEST);
            if (itemStack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemStack)) {
                bl = true;
                int n = this.fallFlyTicks + 1;
                if (!this.level().isClientSide && n % 10 == 0) {
                    int n2 = n / 10;
                    if (n2 % 2 == 0) {
                        itemStack.hurtAndBreak(1, this, EquipmentSlot.CHEST);
                    }
                    this.gameEvent(GameEvent.ELYTRA_GLIDE);
                }
            } else {
                bl = false;
            }
        } else {
            bl = false;
        }
        if (!this.level().isClientSide) {
            this.setSharedFlag(7, bl);
        }
    }

    protected void serverAiStep() {
    }

    protected void pushEntities() {
        if (this.level().isClientSide()) {
            this.level().getEntities(EntityTypeTest.forClass(Player.class), this.getBoundingBox(), EntitySelector.pushableBy(this)).forEach(this::doPush);
            return;
        }
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
        if (!list.isEmpty()) {
            int n = this.level().getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (n > 0 && list.size() > n - 1 && this.random.nextInt(4) == 0) {
                int n2 = 0;
                for (Entity entity : list) {
                    if (entity.isPassenger()) continue;
                    ++n2;
                }
                if (n2 > n - 1) {
                    this.hurt(this.damageSources().cramming(), 6.0f);
                }
            }
            for (Entity entity : list) {
                this.doPush(entity);
            }
        }
    }

    protected void checkAutoSpinAttack(AABB aABB, AABB aABB2) {
        AABB aABB3 = aABB.minmax(aABB2);
        List<Entity> list = this.level().getEntities(this, aABB3);
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (!(entity instanceof LivingEntity)) continue;
                this.doAutoAttackOnTouch((LivingEntity)entity);
                this.autoSpinAttackTicks = 0;
                this.setDeltaMovement(this.getDeltaMovement().scale(-0.2));
                break;
            }
        } else if (this.horizontalCollision) {
            this.autoSpinAttackTicks = 0;
        }
        if (!this.level().isClientSide && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
            this.autoSpinAttackDmg = 0.0f;
            this.autoSpinAttackItemStack = null;
        }
    }

    protected void doPush(Entity entity) {
        entity.push(this);
    }

    protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
    }

    public boolean isAutoSpinAttack() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity entity = this.getVehicle();
        super.stopRiding();
        if (entity != null && entity != this.getVehicle() && !this.level().isClientSide) {
            this.dismountVehicle(entity);
        }
    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.oRun = this.run;
        this.run = 0.0f;
        this.resetFallDistance();
    }

    @Override
    public void lerpTo(double d, double d2, double d3, float f, float f2, int n) {
        this.lerpX = d;
        this.lerpY = d2;
        this.lerpZ = d3;
        this.lerpYRot = f;
        this.lerpXRot = f2;
        this.lerpSteps = n;
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
    public void lerpHeadTo(float f, int n) {
        this.lerpYHeadRot = f;
        this.lerpHeadSteps = n;
    }

    public void setJumping(boolean bl) {
        this.jumping = bl;
    }

    public void onItemPickup(ItemEntity itemEntity) {
        Entity entity = itemEntity.getOwner();
        if (entity instanceof ServerPlayer) {
            CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayer)entity, itemEntity.getItem(), this);
        }
    }

    public void take(Entity entity, int n) {
        if (!entity.isRemoved() && !this.level().isClientSide && (entity instanceof ItemEntity || entity instanceof AbstractArrow || entity instanceof ExperienceOrb)) {
            ((ServerLevel)this.level()).getChunkSource().broadcast(entity, new ClientboundTakeItemEntityPacket(entity.getId(), this.getId(), n));
        }
    }

    public boolean hasLineOfSight(Entity entity) {
        if (entity.level() != this.level()) {
            return false;
        }
        Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        if (vec32.distanceTo(vec3) > 128.0) {
            return false;
        }
        return this.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getViewYRot(float f) {
        if (f == 1.0f) {
            return this.yHeadRot;
        }
        return Mth.lerp(f, this.yHeadRotO, this.yHeadRot);
    }

    public float getAttackAnim(float f) {
        float f2 = this.attackAnim - this.oAttackAnim;
        if (f2 < 0.0f) {
            f2 += 1.0f;
        }
        return this.oAttackAnim + f2 * f;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.onClimbable();
    }

    @Override
    public float getYHeadRot() {
        return this.yHeadRot;
    }

    @Override
    public void setYHeadRot(float f) {
        this.yHeadRot = f;
    }

    @Override
    public void setYBodyRot(float f) {
        this.yBodyRot = f;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
    }

    public static Vec3 resetForwardDirectionOfRelativePortalPosition(Vec3 vec3) {
        return new Vec3(vec3.x, vec3.y, 0.0);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public final void setAbsorptionAmount(float f) {
        this.internalSetAbsorptionAmount(Mth.clamp(f, 0.0f, this.getMaxAbsorption()));
    }

    protected void internalSetAbsorptionAmount(float f) {
        this.absorptionAmount = f;
    }

    public void onEnterCombat() {
    }

    public void onLeaveCombat() {
    }

    protected void updateEffectVisibility() {
        this.effectsDirty = true;
    }

    public abstract HumanoidArm getMainArm();

    public boolean isUsingItem() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
    }

    public InteractionHand getUsedItemHand() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private void updatingUsingItem() {
        if (this.isUsingItem()) {
            if (ItemStack.isSameItem(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                this.updateUsingItem(this.useItem);
            } else {
                this.stopUsingItem();
            }
        }
    }

    protected void updateUsingItem(ItemStack itemStack) {
        itemStack.onUseTick(this.level(), this, this.getUseItemRemainingTicks());
        if (this.shouldTriggerItemUseEffects()) {
            this.triggerItemUseEffects(itemStack, 5);
        }
        if (--this.useItemRemaining == 0 && !this.level().isClientSide && !itemStack.useOnRelease()) {
            this.completeUsingItem();
        }
    }

    private boolean shouldTriggerItemUseEffects() {
        int n;
        int n2 = this.useItem.getUseDuration(this) - this.getUseItemRemainingTicks();
        boolean bl = n2 > (n = (int)((float)this.useItem.getUseDuration(this) * 0.21875f));
        return bl && this.getUseItemRemainingTicks() % 4 == 0;
    }

    private void updateSwimAmount() {
        this.swimAmountO = this.swimAmount;
        this.swimAmount = this.isVisuallySwimming() ? Math.min(1.0f, this.swimAmount + 0.09f) : Math.max(0.0f, this.swimAmount - 0.09f);
    }

    protected void setLivingEntityFlag(int n, boolean bl) {
        int n2 = this.entityData.get(DATA_LIVING_ENTITY_FLAGS).byteValue();
        n2 = bl ? (n2 |= n) : (n2 &= ~n);
        this.entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte)n2);
    }

    public void startUsingItem(InteractionHand interactionHand) {
        ItemStack itemStack = this.getItemInHand(interactionHand);
        if (itemStack.isEmpty() || this.isUsingItem()) {
            return;
        }
        this.useItem = itemStack;
        this.useItemRemaining = itemStack.getUseDuration(this);
        if (!this.level().isClientSide) {
            this.setLivingEntityFlag(1, true);
            this.setLivingEntityFlag(2, interactionHand == InteractionHand.OFF_HAND);
            this.gameEvent(GameEvent.ITEM_INTERACT_START);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (SLEEPING_POS_ID.equals(entityDataAccessor)) {
            if (this.level().isClientSide) {
                this.getSleepingPos().ifPresent(this::setPosToBed);
            }
        } else if (DATA_LIVING_ENTITY_FLAGS.equals(entityDataAccessor) && this.level().isClientSide) {
            if (this.isUsingItem() && this.useItem.isEmpty()) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                if (!this.useItem.isEmpty()) {
                    this.useItemRemaining = this.useItem.getUseDuration(this);
                }
            } else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
                this.useItem = ItemStack.EMPTY;
                this.useItemRemaining = 0;
            }
        }
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
        super.lookAt(anchor, vec3);
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot = this.yHeadRot;
    }

    @Override
    public float getPreciseBodyRotation(float f) {
        return Mth.lerp(f, this.yBodyRotO, this.yBodyRot);
    }

    protected void triggerItemUseEffects(ItemStack itemStack, int n) {
        if (itemStack.isEmpty() || !this.isUsingItem()) {
            return;
        }
        if (itemStack.getUseAnimation() == UseAnim.DRINK) {
            this.playSound(this.getDrinkingSound(itemStack), 0.5f, this.level().random.nextFloat() * 0.1f + 0.9f);
        }
        if (itemStack.getUseAnimation() == UseAnim.EAT) {
            this.spawnItemParticles(itemStack, n);
            this.playSound(this.getEatingSound(itemStack), 0.5f + 0.5f * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    private void spawnItemParticles(ItemStack itemStack, int n) {
        for (int i = 0; i < n; ++i) {
            Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-this.getXRot() * ((float)Math.PI / 180));
            vec3 = vec3.yRot(-this.getYRot() * ((float)Math.PI / 180));
            double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3 vec32 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
            vec32 = vec32.xRot(-this.getXRot() * ((float)Math.PI / 180));
            vec32 = vec32.yRot(-this.getYRot() * ((float)Math.PI / 180));
            vec32 = vec32.add(this.getX(), this.getEyeY(), this.getZ());
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack), vec32.x, vec32.y, vec32.z, vec3.x, vec3.y + 0.05, vec3.z);
        }
    }

    protected void completeUsingItem() {
        if (this.level().isClientSide && !this.isUsingItem()) {
            return;
        }
        InteractionHand interactionHand = this.getUsedItemHand();
        if (!this.useItem.equals(this.getItemInHand(interactionHand))) {
            this.releaseUsingItem();
            return;
        }
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            this.triggerItemUseEffects(this.useItem, 16);
            ItemStack itemStack = this.useItem.finishUsingItem(this.level(), this);
            if (itemStack != this.useItem) {
                this.setItemInHand(interactionHand, itemStack);
            }
            this.stopUsingItem();
        }
    }

    public ItemStack getUseItem() {
        return this.useItem;
    }

    public int getUseItemRemainingTicks() {
        return this.useItemRemaining;
    }

    public int getTicksUsingItem() {
        if (this.isUsingItem()) {
            return this.useItem.getUseDuration(this) - this.getUseItemRemainingTicks();
        }
        return 0;
    }

    public void releaseUsingItem() {
        if (!this.useItem.isEmpty()) {
            this.useItem.releaseUsing(this.level(), this, this.getUseItemRemainingTicks());
            if (this.useItem.useOnRelease()) {
                this.updatingUsingItem();
            }
        }
        this.stopUsingItem();
    }

    public void stopUsingItem() {
        if (!this.level().isClientSide) {
            boolean bl = this.isUsingItem();
            this.setLivingEntityFlag(1, false);
            if (bl) {
                this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        this.useItem = ItemStack.EMPTY;
        this.useItemRemaining = 0;
    }

    public boolean isBlocking() {
        if (!this.isUsingItem() || this.useItem.isEmpty()) {
            return false;
        }
        Item item = this.useItem.getItem();
        if (item.getUseAnimation(this.useItem) != UseAnim.BLOCK) {
            return false;
        }
        return item.getUseDuration(this.useItem, this) - this.useItemRemaining >= 5;
    }

    public boolean isSuppressingSlidingDownLadder() {
        return this.isShiftKeyDown();
    }

    public boolean isFallFlying() {
        return this.getSharedFlag(7);
    }

    @Override
    public boolean isVisuallySwimming() {
        return super.isVisuallySwimming() || !this.isFallFlying() && this.hasPose(Pose.FALL_FLYING);
    }

    public int getFallFlyingTicks() {
        return this.fallFlyTicks;
    }

    public boolean randomTeleport(double d, double d2, double d3, boolean bl) {
        Object object;
        double d4 = this.getX();
        double d5 = this.getY();
        double d6 = this.getZ();
        double d7 = d2;
        boolean bl2 = false;
        Object object2 = BlockPos.containing(d, d7, d3);
        Level level = this.level();
        if (level.hasChunkAt((BlockPos)object2)) {
            boolean bl3 = false;
            while (!bl3 && ((Vec3i)object2).getY() > level.getMinBuildHeight()) {
                object = ((BlockPos)object2).below();
                BlockState blockState = level.getBlockState((BlockPos)object);
                if (blockState.blocksMotion()) {
                    bl3 = true;
                    continue;
                }
                d7 -= 1.0;
                object2 = object;
            }
            if (bl3) {
                this.teleportTo(d, d7, d3);
                if (level.noCollision(this) && !level.containsAnyLiquid(this.getBoundingBox())) {
                    bl2 = true;
                }
            }
        }
        if (!bl2) {
            this.teleportTo(d4, d5, d6);
            return false;
        }
        if (bl) {
            level.broadcastEntityEvent(this, (byte)46);
        }
        if ((object = this) instanceof PathfinderMob) {
            PathfinderMob pathfinderMob = (PathfinderMob)object;
            pathfinderMob.getNavigation().stop();
        }
        return true;
    }

    public boolean isAffectedByPotions() {
        return !this.isDeadOrDying();
    }

    public boolean attackable() {
        return true;
    }

    public void setRecordPlayingNearby(BlockPos blockPos, boolean bl) {
    }

    public boolean canTakeItem(ItemStack itemStack) {
        return false;
    }

    @Override
    public final EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.SLEEPING ? SLEEPING_DIMENSIONS : this.getDefaultDimensions(pose).scale(this.getScale());
    }

    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return this.getType().getDimensions().scale(this.getAgeScale());
    }

    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of((Object)((Object)Pose.STANDING));
    }

    public AABB getLocalBoundsForPose(Pose pose) {
        EntityDimensions entityDimensions = this.getDimensions(pose);
        return new AABB(-entityDimensions.width() / 2.0f, 0.0, -entityDimensions.width() / 2.0f, entityDimensions.width() / 2.0f, entityDimensions.height(), entityDimensions.width() / 2.0f);
    }

    protected boolean wouldNotSuffocateAtTargetPose(Pose pose) {
        AABB aABB = this.getDimensions(pose).makeBoundingBox(this.position());
        return this.level().noBlockCollision(this, aABB);
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        return super.canUsePortal(bl) && !this.isSleeping();
    }

    public Optional<BlockPos> getSleepingPos() {
        return this.entityData.get(SLEEPING_POS_ID);
    }

    public void setSleepingPos(BlockPos blockPos) {
        this.entityData.set(SLEEPING_POS_ID, Optional.of(blockPos));
    }

    public void clearSleepingPos() {
        this.entityData.set(SLEEPING_POS_ID, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPos().isPresent();
    }

    public void startSleeping(BlockPos blockPos) {
        BlockState blockState;
        if (this.isPassenger()) {
            this.stopRiding();
        }
        if ((blockState = this.level().getBlockState(blockPos)).getBlock() instanceof BedBlock) {
            this.level().setBlock(blockPos, (BlockState)blockState.setValue(BedBlock.OCCUPIED, true), 3);
        }
        this.setPose(Pose.SLEEPING);
        this.setPosToBed(blockPos);
        this.setSleepingPos(blockPos);
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
    }

    private void setPosToBed(BlockPos blockPos) {
        this.setPos((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.6875, (double)blockPos.getZ() + 0.5);
    }

    private boolean checkBedExists() {
        return this.getSleepingPos().map(blockPos -> this.level().getBlockState((BlockPos)blockPos).getBlock() instanceof BedBlock).orElse(false);
    }

    public void stopSleeping() {
        this.getSleepingPos().filter(this.level()::hasChunkAt).ifPresent(blockPos -> {
            BlockState blockState = this.level().getBlockState((BlockPos)blockPos);
            if (blockState.getBlock() instanceof BedBlock) {
                Direction direction = blockState.getValue(BedBlock.FACING);
                this.level().setBlock((BlockPos)blockPos, (BlockState)blockState.setValue(BedBlock.OCCUPIED, false), 3);
                Vec3 vec3 = BedBlock.findStandUpPosition(this.getType(), this.level(), blockPos, direction, this.getYRot()).orElseGet(() -> {
                    BlockPos blockPos2 = blockPos.above();
                    return new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.1, (double)blockPos2.getZ() + 0.5);
                });
                Vec3 vec32 = Vec3.atBottomCenterOf(blockPos).subtract(vec3).normalize();
                float f = (float)Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * 57.2957763671875 - 90.0);
                this.setPos(vec3.x, vec3.y, vec3.z);
                this.setYRot(f);
                this.setXRot(0.0f);
            }
        });
        Vec3 vec3 = this.position();
        this.setPose(Pose.STANDING);
        this.setPos(vec3.x, vec3.y, vec3.z);
        this.clearSleepingPos();
    }

    @Nullable
    public Direction getBedOrientation() {
        BlockPos blockPos = this.getSleepingPos().orElse(null);
        return blockPos != null ? BedBlock.getBedOrientation(this.level(), blockPos) : null;
    }

    @Override
    public boolean isInWall() {
        return !this.isSleeping() && super.isInWall();
    }

    public ItemStack getProjectile(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    public final ItemStack eat(Level level, ItemStack itemStack) {
        FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
        if (foodProperties != null) {
            return this.eat(level, itemStack, foodProperties);
        }
        return itemStack;
    }

    public ItemStack eat(Level level, ItemStack itemStack, FoodProperties foodProperties) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(itemStack), SoundSource.NEUTRAL, 1.0f, 1.0f + (level.random.nextFloat() - level.random.nextFloat()) * 0.4f);
        this.addEatEffect(foodProperties);
        itemStack.consume(1, this);
        this.gameEvent(GameEvent.EAT);
        return itemStack;
    }

    private void addEatEffect(FoodProperties foodProperties) {
        if (this.level().isClientSide()) {
            return;
        }
        List<FoodProperties.PossibleEffect> list = foodProperties.effects();
        for (FoodProperties.PossibleEffect possibleEffect : list) {
            if (!(this.random.nextFloat() < possibleEffect.probability())) continue;
            this.addEffect(possibleEffect.effect());
        }
    }

    private static byte entityEventForEquipmentBreak(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> 47;
            case EquipmentSlot.OFFHAND -> 48;
            case EquipmentSlot.HEAD -> 49;
            case EquipmentSlot.CHEST -> 50;
            case EquipmentSlot.FEET -> 52;
            case EquipmentSlot.LEGS -> 51;
            case EquipmentSlot.BODY -> 65;
        };
    }

    public void onEquippedItemBroken(Item item, EquipmentSlot equipmentSlot) {
        this.level().broadcastEntityEvent(this, LivingEntity.entityEventForEquipmentBreak(equipmentSlot));
    }

    public static EquipmentSlot getSlotForHand(InteractionHand interactionHand) {
        return interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        if (this.getItemBySlot(EquipmentSlot.HEAD).is(Items.DRAGON_HEAD)) {
            float f = 0.5f;
            return this.getBoundingBox().inflate(0.5, 0.5, 0.5);
        }
        return super.getBoundingBoxForCulling();
    }

    public EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlot;
        Equipable equipable = Equipable.get(itemStack);
        if (equipable != null && this.canUseSlot(equipmentSlot = equipable.getEquipmentSlot())) {
            return equipmentSlot;
        }
        return EquipmentSlot.MAINHAND;
    }

    private static SlotAccess createEquipmentSlotAccess(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HEAD || equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND) {
            return SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot);
        }
        return SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot, itemStack -> itemStack.isEmpty() || livingEntity.getEquipmentSlotForItem((ItemStack)itemStack) == equipmentSlot);
    }

    @Nullable
    private static EquipmentSlot getEquipmentSlot(int n) {
        if (n == 100 + EquipmentSlot.HEAD.getIndex()) {
            return EquipmentSlot.HEAD;
        }
        if (n == 100 + EquipmentSlot.CHEST.getIndex()) {
            return EquipmentSlot.CHEST;
        }
        if (n == 100 + EquipmentSlot.LEGS.getIndex()) {
            return EquipmentSlot.LEGS;
        }
        if (n == 100 + EquipmentSlot.FEET.getIndex()) {
            return EquipmentSlot.FEET;
        }
        if (n == 98) {
            return EquipmentSlot.MAINHAND;
        }
        if (n == 99) {
            return EquipmentSlot.OFFHAND;
        }
        if (n == 105) {
            return EquipmentSlot.BODY;
        }
        return null;
    }

    @Override
    public SlotAccess getSlot(int n) {
        EquipmentSlot equipmentSlot = LivingEntity.getEquipmentSlot(n);
        if (equipmentSlot != null) {
            return LivingEntity.createEquipmentSlotAccess(this, equipmentSlot);
        }
        return super.getSlot(n);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        }
        boolean bl = !this.getItemBySlot(EquipmentSlot.HEAD).is(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EquipmentSlot.CHEST).is(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EquipmentSlot.LEGS).is(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EquipmentSlot.FEET).is(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EquipmentSlot.BODY).is(ItemTags.FREEZE_IMMUNE_WEARABLES);
        return bl && super.canFreeze();
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return !this.level().isClientSide() && this.hasEffect(MobEffects.GLOWING) || super.isCurrentlyGlowing();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.yBodyRot;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        double d = clientboundAddEntityPacket.getX();
        double d2 = clientboundAddEntityPacket.getY();
        double d3 = clientboundAddEntityPacket.getZ();
        float f = clientboundAddEntityPacket.getYRot();
        float f2 = clientboundAddEntityPacket.getXRot();
        this.syncPacketPositionCodec(d, d2, d3);
        this.yBodyRot = clientboundAddEntityPacket.getYHeadRot();
        this.yHeadRot = clientboundAddEntityPacket.getYHeadRot();
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.setId(clientboundAddEntityPacket.getId());
        this.setUUID(clientboundAddEntityPacket.getUUID());
        this.absMoveTo(d, d2, d3, f, f2);
        this.setDeltaMovement(clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
    }

    public boolean canDisableShield() {
        return this.getWeaponItem().getItem() instanceof AxeItem;
    }

    @Override
    public float maxUpStep() {
        float f = (float)this.getAttributeValue(Attributes.STEP_HEIGHT);
        return this.getControllingPassenger() instanceof Player ? Math.max(f, 1.0f) : f;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return this.position().add(this.getPassengerAttachmentPoint(entity, this.getDimensions(this.getPose()), this.getScale() * this.getAgeScale()));
    }

    protected void lerpHeadRotationStep(int n, double d) {
        this.yHeadRot = (float)Mth.rotLerp(1.0 / (double)n, (double)this.yHeadRot, d);
    }

    @Override
    public void igniteForTicks(int n) {
        super.igniteForTicks(Mth.ceil((double)n * this.getAttributeValue(Attributes.BURNING_TIME)));
    }

    public boolean hasInfiniteMaterials() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        ServerLevel serverLevel;
        Level level;
        return super.isInvulnerableTo(damageSource) || (level = this.level()) instanceof ServerLevel && EnchantmentHelper.isImmuneToDamage(serverLevel = (ServerLevel)level, this, damageSource);
    }

    public record Fallsounds(SoundEvent small, SoundEvent big) {
    }
}

