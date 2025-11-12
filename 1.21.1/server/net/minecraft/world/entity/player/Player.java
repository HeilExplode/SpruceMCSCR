/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class Player
extends LivingEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final HumanoidArm DEFAULT_MAIN_HAND = HumanoidArm.RIGHT;
    public static final int DEFAULT_MODEL_CUSTOMIZATION = 0;
    public static final int MAX_HEALTH = 20;
    public static final int SLEEP_DURATION = 100;
    public static final int WAKE_UP_DURATION = 10;
    public static final int ENDER_SLOT_OFFSET = 200;
    public static final int HELD_ITEM_SLOT = 499;
    public static final int CRAFTING_SLOT_OFFSET = 500;
    public static final float DEFAULT_BLOCK_INTERACTION_RANGE = 4.5f;
    public static final float DEFAULT_ENTITY_INTERACTION_RANGE = 3.0f;
    public static final float CROUCH_BB_HEIGHT = 1.5f;
    public static final float SWIMMING_BB_WIDTH = 0.6f;
    public static final float SWIMMING_BB_HEIGHT = 0.6f;
    public static final float DEFAULT_EYE_HEIGHT = 1.62f;
    private static final int CURRENT_IMPULSE_CONTEXT_RESET_GRACE_TIME_TICKS = 40;
    public static final Vec3 DEFAULT_VEHICLE_ATTACHMENT = new Vec3(0.0, 0.6, 0.0);
    public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6f, 1.8f).withEyeHeight(1.62f).withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT));
    private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.builder().put((Object)Pose.STANDING, (Object)STANDING_DIMENSIONS).put((Object)Pose.SLEEPING, (Object)SLEEPING_DIMENSIONS).put((Object)Pose.FALL_FLYING, (Object)EntityDimensions.scalable(0.6f, 0.6f).withEyeHeight(0.4f)).put((Object)Pose.SWIMMING, (Object)EntityDimensions.scalable(0.6f, 0.6f).withEyeHeight(0.4f)).put((Object)Pose.SPIN_ATTACK, (Object)EntityDimensions.scalable(0.6f, 0.6f).withEyeHeight(0.4f)).put((Object)Pose.CROUCHING, (Object)EntityDimensions.scalable(0.6f, 1.5f).withEyeHeight(1.27f).withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT))).put((Object)Pose.DYING, (Object)EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(1.62f)).build();
    private static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_SCORE_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
    protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
    private long timeEntitySatOnShoulder;
    final Inventory inventory = new Inventory(this);
    protected PlayerEnderChestContainer enderChestInventory = new PlayerEnderChestContainer();
    public final InventoryMenu inventoryMenu;
    public AbstractContainerMenu containerMenu;
    protected FoodData foodData = new FoodData();
    protected int jumpTriggerTime;
    public float oBob;
    public float bob;
    public int takeXpDelay;
    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;
    private int sleepCounter;
    protected boolean wasUnderwater;
    private final Abilities abilities = new Abilities();
    public int experienceLevel;
    public int totalExperience;
    public float experienceProgress;
    protected int enchantmentSeed;
    protected final float defaultFlySpeed = 0.02f;
    private int lastLevelUpTime;
    private final GameProfile gameProfile;
    private boolean reducedDebugInfo;
    private ItemStack lastItemInMainHand = ItemStack.EMPTY;
    private final ItemCooldowns cooldowns = this.createItemCooldowns();
    private Optional<GlobalPos> lastDeathLocation = Optional.empty();
    @Nullable
    public FishingHook fishing;
    protected float hurtDir;
    @Nullable
    public Vec3 currentImpulseImpactPos;
    @Nullable
    public Entity currentExplosionCause;
    private boolean ignoreFallDamageFromCurrentImpulse;
    private int currentImpulseContextResetGraceTime;

    public Player(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super((EntityType<? extends LivingEntity>)EntityType.PLAYER, level);
        this.setUUID(gameProfile.getId());
        this.gameProfile = gameProfile;
        this.inventoryMenu = new InventoryMenu(this.inventory, !level.isClientSide, this);
        this.containerMenu = this.inventoryMenu;
        this.moveTo((double)blockPos.getX() + 0.5, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5, f, 0.0f);
        this.rotOffs = 180.0f;
    }

    public boolean blockActionRestricted(Level level, BlockPos blockPos, GameType gameType) {
        if (!gameType.isBlockPlacingRestricted()) {
            return false;
        }
        if (gameType == GameType.SPECTATOR) {
            return true;
        }
        if (this.mayBuild()) {
            return false;
        }
        ItemStack itemStack = this.getMainHandItem();
        return itemStack.isEmpty() || !itemStack.canBreakBlockInAdventureMode(new BlockInWorld(level, blockPos, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.MOVEMENT_SPEED, 0.1f).add(Attributes.ATTACK_SPEED).add(Attributes.LUCK).add(Attributes.BLOCK_INTERACTION_RANGE, 4.5).add(Attributes.ENTITY_INTERACTION_RANGE, 3.0).add(Attributes.BLOCK_BREAK_SPEED).add(Attributes.SUBMERGED_MINING_SPEED).add(Attributes.SNEAKING_SPEED).add(Attributes.MINING_EFFICIENCY).add(Attributes.SWEEPING_DAMAGE_RATIO);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYER_ABSORPTION_ID, Float.valueOf(0.0f));
        builder.define(DATA_SCORE_ID, 0);
        builder.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
        builder.define(DATA_PLAYER_MAIN_HAND, (byte)DEFAULT_MAIN_HAND.getId());
        builder.define(DATA_SHOULDER_LEFT, new CompoundTag());
        builder.define(DATA_SHOULDER_RIGHT, new CompoundTag());
    }

    @Override
    public void tick() {
        this.noPhysics = this.isSpectator();
        if (this.isSpectator()) {
            this.setOnGround(false);
        }
        if (this.takeXpDelay > 0) {
            --this.takeXpDelay;
        }
        if (this.isSleeping()) {
            ++this.sleepCounter;
            if (this.sleepCounter > 100) {
                this.sleepCounter = 100;
            }
            if (!this.level().isClientSide && this.level().isDay()) {
                this.stopSleepInBed(false, true);
            }
        } else if (this.sleepCounter > 0) {
            ++this.sleepCounter;
            if (this.sleepCounter >= 110) {
                this.sleepCounter = 0;
            }
        }
        this.updateIsUnderwater();
        super.tick();
        if (!this.level().isClientSide && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }
        this.moveCloak();
        if (!this.level().isClientSide) {
            this.foodData.tick(this);
            this.awardStat(Stats.PLAY_TIME);
            this.awardStat(Stats.TOTAL_WORLD_TIME);
            if (this.isAlive()) {
                this.awardStat(Stats.TIME_SINCE_DEATH);
            }
            if (this.isDiscrete()) {
                this.awardStat(Stats.CROUCH_TIME);
            }
            if (!this.isSleeping()) {
                this.awardStat(Stats.TIME_SINCE_REST);
            }
        }
        int n = 29999999;
        double d = Mth.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
        double d2 = Mth.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
        if (d != this.getX() || d2 != this.getZ()) {
            this.setPos(d, this.getY(), d2);
        }
        ++this.attackStrengthTicker;
        ItemStack itemStack = this.getMainHandItem();
        if (!ItemStack.matches(this.lastItemInMainHand, itemStack)) {
            if (!ItemStack.isSameItem(this.lastItemInMainHand, itemStack)) {
                this.resetAttackStrengthTicker();
            }
            this.lastItemInMainHand = itemStack.copy();
        }
        this.turtleHelmetTick();
        this.cooldowns.tick();
        this.updatePlayerPose();
        if (this.currentImpulseContextResetGraceTime > 0) {
            --this.currentImpulseContextResetGraceTime;
        }
    }

    @Override
    protected float getMaxHeadRotationRelativeToBody() {
        if (this.isBlocking()) {
            return 15.0f;
        }
        return super.getMaxHeadRotationRelativeToBody();
    }

    public boolean isSecondaryUseActive() {
        return this.isShiftKeyDown();
    }

    protected boolean wantsToStopRiding() {
        return this.isShiftKeyDown();
    }

    protected boolean isStayingOnGroundSurface() {
        return this.isShiftKeyDown();
    }

    protected boolean updateIsUnderwater() {
        this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
        return this.wasUnderwater;
    }

    private void turtleHelmetTick() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.is(Items.TURTLE_HELMET) && !this.isEyeInFluid(FluidTags.WATER)) {
            this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, true));
        }
    }

    protected ItemCooldowns createItemCooldowns() {
        return new ItemCooldowns();
    }

    private void moveCloak() {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double d = this.getX() - this.xCloak;
        double d2 = this.getY() - this.yCloak;
        double d3 = this.getZ() - this.zCloak;
        double d4 = 10.0;
        if (d > 10.0) {
            this.xCloakO = this.xCloak = this.getX();
        }
        if (d3 > 10.0) {
            this.zCloakO = this.zCloak = this.getZ();
        }
        if (d2 > 10.0) {
            this.yCloakO = this.yCloak = this.getY();
        }
        if (d < -10.0) {
            this.xCloakO = this.xCloak = this.getX();
        }
        if (d3 < -10.0) {
            this.zCloakO = this.zCloak = this.getZ();
        }
        if (d2 < -10.0) {
            this.yCloakO = this.yCloak = this.getY();
        }
        this.xCloak += d * 0.25;
        this.zCloak += d3 * 0.25;
        this.yCloak += d2 * 0.25;
    }

    protected void updatePlayerPose() {
        if (!this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.SWIMMING)) {
            return;
        }
        Pose pose = this.isFallFlying() ? Pose.FALL_FLYING : (this.isSleeping() ? Pose.SLEEPING : (this.isSwimming() ? Pose.SWIMMING : (this.isAutoSpinAttack() ? Pose.SPIN_ATTACK : (this.isShiftKeyDown() && !this.abilities.flying ? Pose.CROUCHING : Pose.STANDING))));
        Pose pose2 = this.isSpectator() || this.isPassenger() || this.canPlayerFitWithinBlocksAndEntitiesWhen(pose) ? pose : (this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.CROUCHING) ? Pose.CROUCHING : Pose.SWIMMING);
        this.setPose(pose2);
    }

    protected boolean canPlayerFitWithinBlocksAndEntitiesWhen(Pose pose) {
        return this.level().noCollision(this, this.getDimensions(pose).makeBoundingBox(this.position()).deflate(1.0E-7));
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.PLAYER_SWIM;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.PLAYER_SPLASH;
    }

    @Override
    protected SoundEvent getSwimHighSpeedSplashSound() {
        return SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
    }

    @Override
    public int getDimensionChangingDelay() {
        return 10;
    }

    @Override
    public void playSound(SoundEvent soundEvent, float f, float f2) {
        this.level().playSound(this, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, f2);
    }

    public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    protected int getFireImmuneTicks() {
        return 20;
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 9) {
            this.completeUsingItem();
        } else if (by == 23) {
            this.reducedDebugInfo = false;
        } else if (by == 22) {
            this.reducedDebugInfo = true;
        } else {
            super.handleEntityEvent(by);
        }
    }

    protected void closeContainer() {
        this.containerMenu = this.inventoryMenu;
    }

    protected void doCloseContainer() {
    }

    @Override
    public void rideTick() {
        if (!this.level().isClientSide && this.wantsToStopRiding() && this.isPassenger()) {
            this.stopRiding();
            this.setShiftKeyDown(false);
            return;
        }
        super.rideTick();
        this.oBob = this.bob;
        this.bob = 0.0f;
    }

    @Override
    protected void serverAiStep() {
        super.serverAiStep();
        this.updateSwingTime();
        this.yHeadRot = this.getYRot();
    }

    @Override
    public void aiStep() {
        if (this.jumpTriggerTime > 0) {
            --this.jumpTriggerTime;
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            if (this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
                this.heal(1.0f);
            }
            if (this.foodData.getSaturationLevel() < 20.0f && this.tickCount % 20 == 0) {
                this.foodData.setSaturation(this.foodData.getSaturationLevel() + 1.0f);
            }
            if (this.foodData.needsFood() && this.tickCount % 10 == 0) {
                this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
            }
        }
        this.inventory.tick();
        this.oBob = this.bob;
        super.aiStep();
        this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        float f = !this.onGround() || this.isDeadOrDying() || this.isSwimming() ? 0.0f : Math.min(0.1f, (float)this.getDeltaMovement().horizontalDistance());
        this.bob += (f - this.bob) * 0.4f;
        if (this.getHealth() > 0.0f && !this.isSpectator()) {
            AABB aABB = this.isPassenger() && !this.getVehicle().isRemoved() ? this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0) : this.getBoundingBox().inflate(1.0, 0.5, 1.0);
            List<Entity> list = this.level().getEntities(this, aABB);
            ArrayList arrayList = Lists.newArrayList();
            for (Entity entity : list) {
                if (entity.getType() == EntityType.EXPERIENCE_ORB) {
                    arrayList.add(entity);
                    continue;
                }
                if (entity.isRemoved()) continue;
                this.touch(entity);
            }
            if (!arrayList.isEmpty()) {
                this.touch((Entity)Util.getRandom(arrayList, this.random));
            }
        }
        this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
        this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
        if (!this.level().isClientSide && (this.fallDistance > 0.5f || this.isInWater()) || this.abilities.flying || this.isSleeping() || this.isInPowderSnow) {
            this.removeEntitiesOnShoulder();
        }
    }

    private void playShoulderEntityAmbientSound(@Nullable CompoundTag compoundTag) {
        if (!(compoundTag == null || compoundTag.contains("Silent") && compoundTag.getBoolean("Silent") || this.level().random.nextInt(200) != 0)) {
            String string = compoundTag.getString("id");
            EntityType.byString(string).filter(entityType -> entityType == EntityType.PARROT).ifPresent(entityType -> {
                if (!Parrot.imitateNearbyMobs(this.level(), this)) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), Parrot.getAmbient(this.level(), this.level().random), this.getSoundSource(), 1.0f, Parrot.getPitch(this.level().random));
                }
            });
        }
    }

    private void touch(Entity entity) {
        entity.playerTouch(this);
    }

    public int getScore() {
        return this.entityData.get(DATA_SCORE_ID);
    }

    public void setScore(int n) {
        this.entityData.set(DATA_SCORE_ID, n);
    }

    public void increaseScore(int n) {
        int n2 = this.getScore();
        this.entityData.set(DATA_SCORE_ID, n2 + n);
    }

    public void startAutoSpinAttack(int n, float f, ItemStack itemStack) {
        this.autoSpinAttackTicks = n;
        this.autoSpinAttackDmg = f;
        this.autoSpinAttackItemStack = itemStack;
        if (!this.level().isClientSide) {
            this.removeEntitiesOnShoulder();
            this.setLivingEntityFlag(4, true);
        }
    }

    @Override
    @Nonnull
    public ItemStack getWeaponItem() {
        if (this.isAutoSpinAttack() && this.autoSpinAttackItemStack != null) {
            return this.autoSpinAttackItemStack;
        }
        return super.getWeaponItem();
    }

    @Override
    public void die(DamageSource damageSource) {
        Level level;
        super.die(damageSource);
        this.reapplyPosition();
        if (!this.isSpectator() && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.dropAllDeathLoot(serverLevel, damageSource);
        }
        if (damageSource != null) {
            this.setDeltaMovement(-Mth.cos((this.getHurtDir() + this.getYRot()) * ((float)Math.PI / 180)) * 0.1f, 0.1f, -Mth.sin((this.getHurtDir() + this.getYRot()) * ((float)Math.PI / 180)) * 0.1f);
        } else {
            this.setDeltaMovement(0.0, 0.1, 0.0);
        }
        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setSharedFlagOnFire(false);
        this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            this.destroyVanishingCursedItems();
            this.inventory.dropAll();
        }
    }

    protected void destroyVanishingCursedItems() {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (itemStack.isEmpty() || !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) continue;
            this.inventory.removeItemNoUpdate(i);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return damageSource.type().effects().sound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Nullable
    public ItemEntity drop(ItemStack itemStack, boolean bl) {
        return this.drop(itemStack, false, bl);
    }

    @Nullable
    public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
        if (itemStack.isEmpty()) {
            return null;
        }
        if (this.level().isClientSide) {
            this.swing(InteractionHand.MAIN_HAND);
        }
        double d = this.getEyeY() - (double)0.3f;
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), d, this.getZ(), itemStack);
        itemEntity.setPickUpDelay(40);
        if (bl2) {
            itemEntity.setThrower(this);
        }
        if (bl) {
            float f = this.random.nextFloat() * 0.5f;
            float f2 = this.random.nextFloat() * ((float)Math.PI * 2);
            itemEntity.setDeltaMovement(-Mth.sin(f2) * f, 0.2f, Mth.cos(f2) * f);
        } else {
            float f = 0.3f;
            float f3 = Mth.sin(this.getXRot() * ((float)Math.PI / 180));
            float f4 = Mth.cos(this.getXRot() * ((float)Math.PI / 180));
            float f5 = Mth.sin(this.getYRot() * ((float)Math.PI / 180));
            float f6 = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
            float f7 = this.random.nextFloat() * ((float)Math.PI * 2);
            float f8 = 0.02f * this.random.nextFloat();
            itemEntity.setDeltaMovement((double)(-f5 * f4 * 0.3f) + Math.cos(f7) * (double)f8, -f3 * 0.3f + 0.1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, (double)(f6 * f4 * 0.3f) + Math.sin(f7) * (double)f8);
        }
        return itemEntity;
    }

    public float getDestroySpeed(BlockState blockState) {
        float f = this.inventory.getDestroySpeed(blockState);
        if (f > 1.0f) {
            f += (float)this.getAttributeValue(Attributes.MINING_EFFICIENCY);
        }
        if (MobEffectUtil.hasDigSpeed(this)) {
            f *= 1.0f + (float)(MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2f;
        }
        if (this.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            f *= (switch (this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        f *= (float)this.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (this.isEyeInFluid(FluidTags.WATER)) {
            f *= (float)this.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }
        if (!this.onGround()) {
            f /= 5.0f;
        }
        return f;
    }

    public boolean hasCorrectToolForDrops(BlockState blockState) {
        return !blockState.requiresCorrectToolForDrops() || this.inventory.getSelected().isCorrectToolForDrops(blockState);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setUUID(this.gameProfile.getId());
        ListTag listTag = compoundTag.getList("Inventory", 10);
        this.inventory.load(listTag);
        this.inventory.selected = compoundTag.getInt("SelectedItemSlot");
        this.sleepCounter = compoundTag.getShort("SleepTimer");
        this.experienceProgress = compoundTag.getFloat("XpP");
        this.experienceLevel = compoundTag.getInt("XpLevel");
        this.totalExperience = compoundTag.getInt("XpTotal");
        this.enchantmentSeed = compoundTag.getInt("XpSeed");
        if (this.enchantmentSeed == 0) {
            this.enchantmentSeed = this.random.nextInt();
        }
        this.setScore(compoundTag.getInt("Score"));
        this.foodData.readAdditionalSaveData(compoundTag);
        this.abilities.loadSaveData(compoundTag);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.abilities.getWalkingSpeed());
        if (compoundTag.contains("EnderItems", 9)) {
            this.enderChestInventory.fromTag(compoundTag.getList("EnderItems", 10), this.registryAccess());
        }
        if (compoundTag.contains("ShoulderEntityLeft", 10)) {
            this.setShoulderEntityLeft(compoundTag.getCompound("ShoulderEntityLeft"));
        }
        if (compoundTag.contains("ShoulderEntityRight", 10)) {
            this.setShoulderEntityRight(compoundTag.getCompound("ShoulderEntityRight"));
        }
        if (compoundTag.contains("LastDeathLocation", 10)) {
            this.setLastDeathLocation(GlobalPos.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get("LastDeathLocation")).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)));
        }
        if (compoundTag.contains("current_explosion_impact_pos", 9)) {
            Vec3.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get("current_explosion_impact_pos")).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(vec3 -> {
                this.currentImpulseImpactPos = vec3;
            });
        }
        this.ignoreFallDamageFromCurrentImpulse = compoundTag.getBoolean("ignore_fall_damage_from_current_explosion");
        this.currentImpulseContextResetGraceTime = compoundTag.getInt("current_impulse_context_reset_grace_time");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        NbtUtils.addCurrentDataVersion(compoundTag);
        compoundTag.put("Inventory", this.inventory.save(new ListTag()));
        compoundTag.putInt("SelectedItemSlot", this.inventory.selected);
        compoundTag.putShort("SleepTimer", (short)this.sleepCounter);
        compoundTag.putFloat("XpP", this.experienceProgress);
        compoundTag.putInt("XpLevel", this.experienceLevel);
        compoundTag.putInt("XpTotal", this.totalExperience);
        compoundTag.putInt("XpSeed", this.enchantmentSeed);
        compoundTag.putInt("Score", this.getScore());
        this.foodData.addAdditionalSaveData(compoundTag);
        this.abilities.addSaveData(compoundTag);
        compoundTag.put("EnderItems", this.enderChestInventory.createTag(this.registryAccess()));
        if (!this.getShoulderEntityLeft().isEmpty()) {
            compoundTag.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
        }
        if (!this.getShoulderEntityRight().isEmpty()) {
            compoundTag.put("ShoulderEntityRight", this.getShoulderEntityRight());
        }
        this.getLastDeathLocation().flatMap(globalPos -> GlobalPos.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, globalPos).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0))).ifPresent(tag -> compoundTag.put("LastDeathLocation", (Tag)tag));
        if (this.currentImpulseImpactPos != null) {
            compoundTag.put("current_explosion_impact_pos", (Tag)Vec3.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.currentImpulseImpactPos).getOrThrow());
        }
        compoundTag.putBoolean("ignore_fall_damage_from_current_explosion", this.ignoreFallDamageFromCurrentImpulse);
        compoundTag.putInt("current_impulse_context_reset_grace_time", this.currentImpulseContextResetGraceTime);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (super.isInvulnerableTo(damageSource)) {
            return true;
        }
        if (damageSource.is(DamageTypeTags.IS_DROWNING)) {
            return !this.level().getGameRules().getBoolean(GameRules.RULE_DROWNING_DAMAGE);
        }
        if (damageSource.is(DamageTypeTags.IS_FALL)) {
            return !this.level().getGameRules().getBoolean(GameRules.RULE_FALL_DAMAGE);
        }
        if (damageSource.is(DamageTypeTags.IS_FIRE)) {
            return !this.level().getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE);
        }
        if (damageSource.is(DamageTypeTags.IS_FREEZING)) {
            return !this.level().getGameRules().getBoolean(GameRules.RULE_FREEZE_DAMAGE);
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (this.abilities.invulnerable && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        this.noActionTime = 0;
        if (this.isDeadOrDying()) {
            return false;
        }
        if (!this.level().isClientSide) {
            this.removeEntitiesOnShoulder();
        }
        if (damageSource.scalesWithDifficulty()) {
            if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
                f = 0.0f;
            }
            if (this.level().getDifficulty() == Difficulty.EASY) {
                f = Math.min(f / 2.0f + 1.0f, f);
            }
            if (this.level().getDifficulty() == Difficulty.HARD) {
                f = f * 3.0f / 2.0f;
            }
        }
        if (f == 0.0f) {
            return false;
        }
        return super.hurt(damageSource, f);
    }

    @Override
    protected void blockUsingShield(LivingEntity livingEntity) {
        super.blockUsingShield(livingEntity);
        if (livingEntity.canDisableShield()) {
            this.disableShield();
        }
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return !this.getAbilities().invulnerable && super.canBeSeenAsEnemy();
    }

    public boolean canHarmPlayer(Player player) {
        PlayerTeam playerTeam = this.getTeam();
        PlayerTeam playerTeam2 = player.getTeam();
        if (playerTeam == null) {
            return true;
        }
        if (!playerTeam.isAlliedTo(playerTeam2)) {
            return true;
        }
        return ((Team)playerTeam).isAllowFriendlyFire();
    }

    @Override
    protected void hurtArmor(DamageSource damageSource, float f) {
        this.doHurtEquipment(damageSource, f, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    }

    @Override
    protected void hurtHelmet(DamageSource damageSource, float f) {
        this.doHurtEquipment(damageSource, f, EquipmentSlot.HEAD);
    }

    @Override
    protected void hurtCurrentlyUsedShield(float f) {
        if (!this.useItem.is(Items.SHIELD)) {
            return;
        }
        if (!this.level().isClientSide) {
            this.awardStat(Stats.ITEM_USED.get(this.useItem.getItem()));
        }
        if (f >= 3.0f) {
            int n = 1 + Mth.floor(f);
            InteractionHand interactionHand = this.getUsedItemHand();
            this.useItem.hurtAndBreak(n, this, Player.getSlotForHand(interactionHand));
            if (this.useItem.isEmpty()) {
                if (interactionHand == InteractionHand.MAIN_HAND) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                this.useItem = ItemStack.EMPTY;
                this.playSound(SoundEvents.SHIELD_BREAK, 0.8f, 0.8f + this.level().random.nextFloat() * 0.4f);
            }
        }
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return;
        }
        f = this.getDamageAfterArmorAbsorb(damageSource, f);
        float f2 = f = this.getDamageAfterMagicAbsorb(damageSource, f);
        f = Math.max(f - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (f2 - f));
        float f3 = f2 - f;
        if (f3 > 0.0f && f3 < 3.4028235E37f) {
            this.awardStat(Stats.DAMAGE_ABSORBED, Math.round(f3 * 10.0f));
        }
        if (f == 0.0f) {
            return;
        }
        this.causeFoodExhaustion(damageSource.getFoodExhaustion());
        this.getCombatTracker().recordDamage(damageSource, f);
        this.setHealth(this.getHealth() - f);
        if (f < 3.4028235E37f) {
            this.awardStat(Stats.DAMAGE_TAKEN, Math.round(f * 10.0f));
        }
        this.gameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public boolean isTextFilteringEnabled() {
        return false;
    }

    public void openTextEdit(SignBlockEntity signBlockEntity, boolean bl) {
    }

    public void openMinecartCommandBlock(BaseCommandBlock baseCommandBlock) {
    }

    public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
    }

    public void openStructureBlock(StructureBlockEntity structureBlockEntity) {
    }

    public void openJigsawBlock(JigsawBlockEntity jigsawBlockEntity) {
    }

    public void openHorseInventory(AbstractHorse abstractHorse, Container container) {
    }

    public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
        return OptionalInt.empty();
    }

    public void sendMerchantOffers(int n, MerchantOffers merchantOffers, int n2, int n3, boolean bl, boolean bl2) {
    }

    public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
    }

    public InteractionResult interactOn(Entity entity, InteractionHand interactionHand) {
        if (this.isSpectator()) {
            if (entity instanceof MenuProvider) {
                this.openMenu((MenuProvider)((Object)entity));
            }
            return InteractionResult.PASS;
        }
        ItemStack itemStack = this.getItemInHand(interactionHand);
        ItemStack itemStack2 = itemStack.copy();
        InteractionResult interactionResult = entity.interact(this, interactionHand);
        if (interactionResult.consumesAction()) {
            if (this.abilities.instabuild && itemStack == this.getItemInHand(interactionHand) && itemStack.getCount() < itemStack2.getCount()) {
                itemStack.setCount(itemStack2.getCount());
            }
            return interactionResult;
        }
        if (!itemStack.isEmpty() && entity instanceof LivingEntity) {
            InteractionResult interactionResult2;
            if (this.abilities.instabuild) {
                itemStack = itemStack2;
            }
            if ((interactionResult2 = itemStack.interactLivingEntity(this, (LivingEntity)entity, interactionHand)).consumesAction()) {
                this.level().gameEvent(GameEvent.ENTITY_INTERACT, entity.position(), GameEvent.Context.of(this));
                if (itemStack.isEmpty() && !this.abilities.instabuild) {
                    this.setItemInHand(interactionHand, ItemStack.EMPTY);
                }
                return interactionResult2;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void removeVehicle() {
        super.removeVehicle();
        this.boardingCooldown = 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isSleeping();
    }

    @Override
    public boolean isAffectedByFluids() {
        return !this.abilities.flying;
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType moverType) {
        double d;
        float f = this.maxUpStep();
        if (this.abilities.flying || vec3.y > 0.0 || moverType != MoverType.SELF && moverType != MoverType.PLAYER || !this.isStayingOnGroundSurface() || !this.isAboveGround(f)) {
            return vec3;
        }
        double d2 = vec3.z;
        double d3 = 0.05;
        double d4 = Math.signum(d) * 0.05;
        double d5 = Math.signum(d2) * 0.05;
        for (d = vec3.x; d != 0.0 && this.canFallAtLeast(d, 0.0, f); d -= d4) {
            if (!(Math.abs(d) <= 0.05)) continue;
            d = 0.0;
            break;
        }
        while (d2 != 0.0 && this.canFallAtLeast(0.0, d2, f)) {
            if (Math.abs(d2) <= 0.05) {
                d2 = 0.0;
                break;
            }
            d2 -= d5;
        }
        while (d != 0.0 && d2 != 0.0 && this.canFallAtLeast(d, d2, f)) {
            d = Math.abs(d) <= 0.05 ? 0.0 : (d -= d4);
            if (Math.abs(d2) <= 0.05) {
                d2 = 0.0;
                continue;
            }
            d2 -= d5;
        }
        return new Vec3(d, vec3.y, d2);
    }

    private boolean isAboveGround(float f) {
        return this.onGround() || this.fallDistance < f && !this.canFallAtLeast(0.0, 0.0, f - this.fallDistance);
    }

    private boolean canFallAtLeast(double d, double d2, float f) {
        AABB aABB = this.getBoundingBox();
        return this.level().noCollision(this, new AABB(aABB.minX + d, aABB.minY - (double)f - (double)1.0E-5f, aABB.minZ + d2, aABB.maxX + d, aABB.minY, aABB.maxZ + d2));
    }

    public void attack(Entity entity) {
        Projectile projectile;
        if (!entity.isAttackable()) {
            return;
        }
        if (entity.skipAttackInteraction(this)) {
            return;
        }
        float f = this.isAutoSpinAttack() ? this.autoSpinAttackDmg : (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack itemStack = this.getWeaponItem();
        DamageSource damageSource = this.damageSources().playerAttack(this);
        float f2 = this.getEnchantedDamage(entity, f, damageSource) - f;
        float f3 = this.getAttackStrengthScale(0.5f);
        f *= 0.2f + f3 * f3 * 0.8f;
        f2 *= f3;
        this.resetAttackStrengthTicker();
        if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile && (projectile = (Projectile)entity).deflect(ProjectileDeflection.AIM_DEFLECT, this, this, true)) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource());
            return;
        }
        if (f > 0.0f || f2 > 0.0f) {
            Object object;
            ItemStack itemStack2;
            boolean bl;
            boolean bl2;
            boolean bl3;
            boolean bl4 = bl3 = f3 > 0.9f;
            if (this.isSprinting() && bl3) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0f, 1.0f);
                bl2 = true;
            } else {
                bl2 = false;
            }
            f += itemStack.getItem().getAttackDamageBonus(entity, f, damageSource);
            boolean bl5 = bl = bl3 && this.fallDistance > 0.0f && !this.onGround() && !this.onClimbable() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && entity instanceof LivingEntity && !this.isSprinting();
            if (bl) {
                f *= 1.5f;
            }
            float f4 = f + f2;
            boolean bl6 = false;
            double d = this.walkDist - this.walkDistO;
            if (bl3 && !bl && !bl2 && this.onGround() && d < (double)this.getSpeed() && (itemStack2 = this.getItemInHand(InteractionHand.MAIN_HAND)).getItem() instanceof SwordItem) {
                bl6 = true;
            }
            float f5 = 0.0f;
            if (entity instanceof LivingEntity) {
                object = (LivingEntity)entity;
                f5 = ((LivingEntity)object).getHealth();
            }
            object = entity.getDeltaMovement();
            boolean bl7 = entity.hurt(damageSource, f4);
            if (bl7) {
                Entity entity2;
                float f6 = this.getKnockback(entity, damageSource) + (bl2 ? 1.0f : 0.0f);
                if (f6 > 0.0f) {
                    if (entity instanceof LivingEntity) {
                        entity2 = (LivingEntity)entity;
                        entity2.knockback(f6 * 0.5f, Mth.sin(this.getYRot() * ((float)Math.PI / 180)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180)));
                    } else {
                        entity.push(-Mth.sin(this.getYRot() * ((float)Math.PI / 180)) * f6 * 0.5f, 0.1, Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * f6 * 0.5f);
                    }
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                    this.setSprinting(false);
                }
                if (bl6) {
                    float f7 = 1.0f + (float)this.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * f;
                    List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.0, 0.25, 1.0));
                    for (LivingEntity object22 : list) {
                        if (object22 == this || object22 == entity || this.isAlliedTo(object22) || object22 instanceof ArmorStand && ((ArmorStand)object22).isMarker() || !(this.distanceToSqr(object22) < 9.0)) continue;
                        float f8 = this.getEnchantedDamage(object22, f7, damageSource) * f3;
                        object22.knockback(0.4f, Mth.sin(this.getYRot() * ((float)Math.PI / 180)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180)));
                        object22.hurt(damageSource, f8);
                        Level level = this.level();
                        if (!(level instanceof ServerLevel)) continue;
                        ServerLevel serverLevel = (ServerLevel)level;
                        EnchantmentHelper.doPostAttackEffects(serverLevel, object22, damageSource);
                    }
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0f, 1.0f);
                    this.sweepAttack();
                }
                if (entity instanceof ServerPlayer && entity.hurtMarked) {
                    ((ServerPlayer)entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
                    entity.hurtMarked = false;
                    entity.setDeltaMovement((Vec3)object);
                }
                if (bl) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0f, 1.0f);
                    this.crit(entity);
                }
                if (!bl && !bl6) {
                    if (bl3) {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0f, 1.0f);
                    } else {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0f, 1.0f);
                    }
                }
                if (f2 > 0.0f) {
                    this.magicCrit(entity);
                }
                this.setLastHurtMob(entity);
                entity2 = entity;
                if (entity instanceof EnderDragonPart) {
                    entity2 = ((EnderDragonPart)entity).parentMob;
                }
                boolean bl8 = false;
                Level n = this.level();
                if (n instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)n;
                    if (entity2 instanceof LivingEntity) {
                        Entity entity3 = entity2;
                        bl8 = itemStack.hurtEnemy((LivingEntity)entity3, this);
                    }
                    EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
                }
                if (!this.level().isClientSide && !itemStack.isEmpty() && entity2 instanceof LivingEntity) {
                    if (bl8) {
                        itemStack.postHurtEnemy((LivingEntity)entity2, this);
                    }
                    if (itemStack.isEmpty()) {
                        if (itemStack == this.getMainHandItem()) {
                            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        } else {
                            this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                        }
                    }
                }
                if (entity instanceof LivingEntity) {
                    float f9 = f5 - ((LivingEntity)entity).getHealth();
                    this.awardStat(Stats.DAMAGE_DEALT, Math.round(f9 * 10.0f));
                    if (this.level() instanceof ServerLevel && f9 > 2.0f) {
                        int n2 = (int)((double)f9 * 0.5);
                        ((ServerLevel)this.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5), entity.getZ(), n2, 0.1, 0.0, 0.1, 0.2);
                    }
                }
                this.causeFoodExhaustion(0.1f);
            } else {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0f, 1.0f);
            }
        }
    }

    protected float getEnchantedDamage(Entity entity, float f, DamageSource damageSource) {
        return f;
    }

    @Override
    protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
        this.attack(livingEntity);
    }

    public void disableShield() {
        this.getCooldowns().addCooldown(Items.SHIELD, 100);
        this.stopUsingItem();
        this.level().broadcastEntityEvent(this, (byte)30);
    }

    public void crit(Entity entity) {
    }

    public void magicCrit(Entity entity) {
    }

    public void sweepAttack() {
        double d = -Mth.sin(this.getYRot() * ((float)Math.PI / 180));
        double d2 = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getY(0.5), this.getZ() + d2, 0, d, 0.0, d2, 0.0);
        }
    }

    public void respawn() {
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        super.remove(removalReason);
        this.inventoryMenu.removed(this);
        if (this.containerMenu != null && this.hasContainerOpen()) {
            this.doCloseContainer();
        }
    }

    public boolean isLocalPlayer() {
        return false;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Abilities getAbilities() {
        return this.abilities;
    }

    @Override
    public boolean hasInfiniteMaterials() {
        return this.abilities.instabuild;
    }

    public void updateTutorialInventoryAction(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction) {
    }

    public boolean hasContainerOpen() {
        return this.containerMenu != this.inventoryMenu;
    }

    public Either<BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
        this.startSleeping(blockPos);
        this.sleepCounter = 0;
        return Either.right((Object)((Object)Unit.INSTANCE));
    }

    public void stopSleepInBed(boolean bl, boolean bl2) {
        super.stopSleeping();
        if (this.level() instanceof ServerLevel && bl2) {
            ((ServerLevel)this.level()).updateSleepingPlayerList();
        }
        this.sleepCounter = bl ? 0 : 100;
    }

    @Override
    public void stopSleeping() {
        this.stopSleepInBed(true, true);
    }

    public boolean isSleepingLongEnough() {
        return this.isSleeping() && this.sleepCounter >= 100;
    }

    public int getSleepTimer() {
        return this.sleepCounter;
    }

    public void displayClientMessage(Component component, boolean bl) {
    }

    public void awardStat(ResourceLocation resourceLocation) {
        this.awardStat(Stats.CUSTOM.get(resourceLocation));
    }

    public void awardStat(ResourceLocation resourceLocation, int n) {
        this.awardStat(Stats.CUSTOM.get(resourceLocation), n);
    }

    public void awardStat(Stat<?> stat) {
        this.awardStat(stat, 1);
    }

    public void awardStat(Stat<?> stat, int n) {
    }

    public void resetStat(Stat<?> stat) {
    }

    public int awardRecipes(Collection<RecipeHolder<?>> collection) {
        return 0;
    }

    public void triggerRecipeCrafted(RecipeHolder<?> recipeHolder, List<ItemStack> list) {
    }

    public void awardRecipesByKey(List<ResourceLocation> list) {
    }

    public int resetRecipes(Collection<RecipeHolder<?>> collection) {
        return 0;
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        this.awardStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.causeFoodExhaustion(0.2f);
        } else {
            this.causeFoodExhaustion(0.05f);
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        double d;
        if (this.isSwimming() && !this.isPassenger()) {
            double d2;
            d = this.getLookAngle().y;
            double d3 = d2 = d < -0.2 ? 0.085 : 0.06;
            if (d <= 0.0 || this.jumping || !this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).getFluidState().isEmpty()) {
                Vec3 vec32 = this.getDeltaMovement();
                this.setDeltaMovement(vec32.add(0.0, (d - vec32.y) * d2, 0.0));
            }
        }
        if (this.abilities.flying && !this.isPassenger()) {
            d = this.getDeltaMovement().y;
            super.travel(vec3);
            Vec3 vec33 = this.getDeltaMovement();
            this.setDeltaMovement(vec33.x, d * 0.6, vec33.z);
            this.resetFallDistance();
            this.setSharedFlag(7, false);
        } else {
            super.travel(vec3);
        }
    }

    @Override
    public void updateSwimming() {
        if (this.abilities.flying) {
            this.setSwimming(false);
        } else {
            super.updateSwimming();
        }
    }

    protected boolean freeAt(BlockPos blockPos) {
        return !this.level().getBlockState(blockPos).isSuffocating(this.level(), blockPos);
    }

    @Override
    public float getSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public boolean causeFallDamage(float f, float f2, DamageSource damageSource) {
        boolean bl;
        if (this.abilities.mayfly) {
            return false;
        }
        if (f >= 2.0f) {
            this.awardStat(Stats.FALL_ONE_CM, (int)Math.round((double)f * 100.0));
        }
        if (this.ignoreFallDamageFromCurrentImpulse && this.currentImpulseImpactPos != null) {
            double d = this.currentImpulseImpactPos.y;
            this.tryResetCurrentImpulseContext();
            if (d < this.getY()) {
                return false;
            }
            float f3 = Math.min(f, (float)(d - this.getY()));
            bl = super.causeFallDamage(f3, f2, damageSource);
        } else {
            bl = super.causeFallDamage(f, f2, damageSource);
        }
        if (bl) {
            this.resetCurrentImpulseContext();
        }
        return bl;
    }

    public boolean tryToStartFallFlying() {
        ItemStack itemStack;
        if (!this.onGround() && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(MobEffects.LEVITATION) && (itemStack = this.getItemBySlot(EquipmentSlot.CHEST)).is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemStack)) {
            this.startFallFlying();
            return true;
        }
        return false;
    }

    public void startFallFlying() {
        this.setSharedFlag(7, true);
    }

    public void stopFallFlying() {
        this.setSharedFlag(7, true);
        this.setSharedFlag(7, false);
    }

    @Override
    protected void doWaterSplashEffect() {
        if (!this.isSpectator()) {
            super.doWaterSplashEffect();
        }
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (this.isInWater()) {
            this.waterSwimSound();
            this.playMuffledStepSound(blockState);
        } else {
            BlockPos blockPos2 = this.getPrimaryStepSoundBlockPos(blockPos);
            if (!blockPos.equals(blockPos2)) {
                BlockState blockState2 = this.level().getBlockState(blockPos2);
                if (blockState2.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
                    this.playCombinationStepSounds(blockState2, blockState);
                } else {
                    super.playStepSound(blockPos2, blockState2);
                }
            } else {
                super.playStepSound(blockPos, blockState);
            }
        }
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.PLAYER_SMALL_FALL, SoundEvents.PLAYER_BIG_FALL);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.awardStat(Stats.ENTITY_KILLED.get(livingEntity.getType()));
        return true;
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
        if (!this.abilities.flying) {
            super.makeStuckInBlock(blockState, vec3);
        }
        this.tryResetCurrentImpulseContext();
    }

    public void giveExperiencePoints(int n) {
        this.increaseScore(n);
        this.experienceProgress += (float)n / (float)this.getXpNeededForNextLevel();
        this.totalExperience = Mth.clamp(this.totalExperience + n, 0, Integer.MAX_VALUE);
        while (this.experienceProgress < 0.0f) {
            float f = this.experienceProgress * (float)this.getXpNeededForNextLevel();
            if (this.experienceLevel > 0) {
                this.giveExperienceLevels(-1);
                this.experienceProgress = 1.0f + f / (float)this.getXpNeededForNextLevel();
                continue;
            }
            this.giveExperienceLevels(-1);
            this.experienceProgress = 0.0f;
        }
        while (this.experienceProgress >= 1.0f) {
            this.experienceProgress = (this.experienceProgress - 1.0f) * (float)this.getXpNeededForNextLevel();
            this.giveExperienceLevels(1);
            this.experienceProgress /= (float)this.getXpNeededForNextLevel();
        }
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed;
    }

    public void onEnchantmentPerformed(ItemStack itemStack, int n) {
        this.experienceLevel -= n;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        this.enchantmentSeed = this.random.nextInt();
    }

    public void giveExperienceLevels(int n) {
        this.experienceLevel += n;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        if (n > 0 && this.experienceLevel % 5 == 0 && (float)this.lastLevelUpTime < (float)this.tickCount - 100.0f) {
            float f = this.experienceLevel > 30 ? 1.0f : (float)this.experienceLevel / 30.0f;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), f * 0.75f, 1.0f);
            this.lastLevelUpTime = this.tickCount;
        }
    }

    public int getXpNeededForNextLevel() {
        if (this.experienceLevel >= 30) {
            return 112 + (this.experienceLevel - 30) * 9;
        }
        if (this.experienceLevel >= 15) {
            return 37 + (this.experienceLevel - 15) * 5;
        }
        return 7 + this.experienceLevel * 2;
    }

    public void causeFoodExhaustion(float f) {
        if (this.abilities.invulnerable) {
            return;
        }
        if (!this.level().isClientSide) {
            this.foodData.addExhaustion(f);
        }
    }

    public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
        return Optional.empty();
    }

    public FoodData getFoodData() {
        return this.foodData;
    }

    public boolean canEat(boolean bl) {
        return this.abilities.invulnerable || bl || this.foodData.needsFood();
    }

    public boolean isHurt() {
        return this.getHealth() > 0.0f && this.getHealth() < this.getMaxHealth();
    }

    public boolean mayBuild() {
        return this.abilities.mayBuild;
    }

    public boolean mayUseItemAt(BlockPos blockPos, Direction direction, ItemStack itemStack) {
        if (this.abilities.mayBuild) {
            return true;
        }
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        BlockInWorld blockInWorld = new BlockInWorld(this.level(), blockPos2, false);
        return itemStack.canPlaceOnBlockInAdventureMode(blockInWorld);
    }

    @Override
    protected int getBaseExperienceReward() {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || this.isSpectator()) {
            return 0;
        }
        int n = this.experienceLevel * 7;
        if (n > 100) {
            return 100;
        }
        return n;
    }

    @Override
    protected boolean isAlwaysExperienceDropper() {
        return true;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return !this.abilities.flying && (!this.onGround() || !this.isDiscrete()) ? Entity.MovementEmission.ALL : Entity.MovementEmission.NONE;
    }

    public void onUpdateAbilities() {
    }

    @Override
    public Component getName() {
        return Component.literal(this.gameProfile.getName());
    }

    public PlayerEnderChestContainer getEnderChestInventory() {
        return this.enderChestInventory;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            return this.inventory.getSelected();
        }
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            return this.inventory.offhand.get(0);
        }
        if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            return this.inventory.armor.get(equipmentSlot.getIndex());
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean doesEmitEquipEvent(EquipmentSlot equipmentSlot) {
        return equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.verifyEquippedItem(itemStack);
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            this.onEquipItem(equipmentSlot, this.inventory.items.set(this.inventory.selected, itemStack), itemStack);
        } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
            this.onEquipItem(equipmentSlot, this.inventory.offhand.set(0, itemStack), itemStack);
        } else if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            this.onEquipItem(equipmentSlot, this.inventory.armor.set(equipmentSlot.getIndex(), itemStack), itemStack);
        }
    }

    public boolean addItem(ItemStack itemStack) {
        return this.inventory.add(itemStack);
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return Lists.newArrayList((Object[])new ItemStack[]{this.getMainHandItem(), this.getOffhandItem()});
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.inventory.armor;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return equipmentSlot != EquipmentSlot.BODY;
    }

    public boolean setEntityOnShoulder(CompoundTag compoundTag) {
        if (this.isPassenger() || !this.onGround() || this.isInWater() || this.isInPowderSnow) {
            return false;
        }
        if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(compoundTag);
            this.timeEntitySatOnShoulder = this.level().getGameTime();
            return true;
        }
        if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(compoundTag);
            this.timeEntitySatOnShoulder = this.level().getGameTime();
            return true;
        }
        return false;
    }

    protected void removeEntitiesOnShoulder() {
        if (this.timeEntitySatOnShoulder + 20L < this.level().getGameTime()) {
            this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new CompoundTag());
            this.respawnEntityOnShoulder(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new CompoundTag());
        }
    }

    private void respawnEntityOnShoulder(CompoundTag compoundTag) {
        if (!this.level().isClientSide && !compoundTag.isEmpty()) {
            EntityType.create(compoundTag, this.level()).ifPresent(entity -> {
                if (entity instanceof TamableAnimal) {
                    ((TamableAnimal)entity).setOwnerUUID(this.uuid);
                }
                entity.setPos(this.getX(), this.getY() + (double)0.7f, this.getZ());
                ((ServerLevel)this.level()).addWithUUID((Entity)entity);
            });
        }
    }

    @Override
    public abstract boolean isSpectator();

    @Override
    public boolean canBeHitByProjectile() {
        return !this.isSpectator() && super.canBeHitByProjectile();
    }

    @Override
    public boolean isSwimming() {
        return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
    }

    public abstract boolean isCreative();

    @Override
    public boolean isPushedByFluid() {
        return !this.abilities.flying;
    }

    public Scoreboard getScoreboard() {
        return this.level().getScoreboard();
    }

    @Override
    public Component getDisplayName() {
        MutableComponent mutableComponent = PlayerTeam.formatNameForTeam(this.getTeam(), this.getName());
        return this.decorateDisplayNameComponent(mutableComponent);
    }

    private MutableComponent decorateDisplayNameComponent(MutableComponent mutableComponent) {
        String string = this.getGameProfile().getName();
        return mutableComponent.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + string + " ")).withHoverEvent(this.createHoverEvent()).withInsertion(string));
    }

    @Override
    public String getScoreboardName() {
        return this.getGameProfile().getName();
    }

    @Override
    protected void internalSetAbsorptionAmount(float f) {
        this.getEntityData().set(DATA_PLAYER_ABSORPTION_ID, Float.valueOf(f));
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getEntityData().get(DATA_PLAYER_ABSORPTION_ID).floatValue();
    }

    public boolean isModelPartShown(PlayerModelPart playerModelPart) {
        return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & playerModelPart.getMask()) == playerModelPart.getMask();
    }

    @Override
    public SlotAccess getSlot(int n) {
        if (n == 499) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return Player.this.containerMenu.getCarried();
                }

                @Override
                public boolean set(ItemStack itemStack) {
                    Player.this.containerMenu.setCarried(itemStack);
                    return true;
                }
            };
        }
        final int n2 = n - 500;
        if (n2 >= 0 && n2 < 4) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return Player.this.inventoryMenu.getCraftSlots().getItem(n2);
                }

                @Override
                public boolean set(ItemStack itemStack) {
                    Player.this.inventoryMenu.getCraftSlots().setItem(n2, itemStack);
                    Player.this.inventoryMenu.slotsChanged(Player.this.inventory);
                    return true;
                }
            };
        }
        if (n >= 0 && n < this.inventory.items.size()) {
            return SlotAccess.forContainer(this.inventory, n);
        }
        int n3 = n - 200;
        if (n3 >= 0 && n3 < this.enderChestInventory.getContainerSize()) {
            return SlotAccess.forContainer(this.enderChestInventory, n3);
        }
        return super.getSlot(n);
    }

    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public void setReducedDebugInfo(boolean bl) {
        this.reducedDebugInfo = bl;
    }

    @Override
    public void setRemainingFireTicks(int n) {
        super.setRemainingFireTicks(this.abilities.invulnerable ? Math.min(n, 1) : n);
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.entityData.get(DATA_PLAYER_MAIN_HAND) == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public void setMainArm(HumanoidArm humanoidArm) {
        this.entityData.set(DATA_PLAYER_MAIN_HAND, (byte)(humanoidArm != HumanoidArm.LEFT ? 1 : 0));
    }

    public CompoundTag getShoulderEntityLeft() {
        return this.entityData.get(DATA_SHOULDER_LEFT);
    }

    protected void setShoulderEntityLeft(CompoundTag compoundTag) {
        this.entityData.set(DATA_SHOULDER_LEFT, compoundTag);
    }

    public CompoundTag getShoulderEntityRight() {
        return this.entityData.get(DATA_SHOULDER_RIGHT);
    }

    protected void setShoulderEntityRight(CompoundTag compoundTag) {
        this.entityData.set(DATA_SHOULDER_RIGHT, compoundTag);
    }

    public float getCurrentItemAttackStrengthDelay() {
        return (float)(1.0 / this.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0);
    }

    public float getAttackStrengthScale(float f) {
        return Mth.clamp(((float)this.attackStrengthTicker + f) / this.getCurrentItemAttackStrengthDelay(), 0.0f, 1.0f);
    }

    public void resetAttackStrengthTicker() {
        this.attackStrengthTicker = 0;
    }

    public ItemCooldowns getCooldowns() {
        return this.cooldowns;
    }

    @Override
    protected float getBlockSpeedFactor() {
        return this.abilities.flying || this.isFallFlying() ? 1.0f : super.getBlockSpeedFactor();
    }

    public float getLuck() {
        return (float)this.getAttributeValue(Attributes.LUCK);
    }

    public boolean canUseGameMasterBlocks() {
        return this.abilities.instabuild && this.getPermissionLevel() >= 2;
    }

    @Override
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
        return this.getItemBySlot(equipmentSlot).isEmpty();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return POSES.getOrDefault((Object)pose, STANDING_DIMENSIONS);
    }

    @Override
    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of((Object)((Object)Pose.STANDING), (Object)((Object)Pose.CROUCHING), (Object)((Object)Pose.SWIMMING));
    }

    @Override
    public ItemStack getProjectile(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ProjectileWeaponItem)) {
            return ItemStack.EMPTY;
        }
        Predicate<ItemStack> predicate = ((ProjectileWeaponItem)itemStack.getItem()).getSupportedHeldProjectiles();
        ItemStack itemStack2 = ProjectileWeaponItem.getHeldProjectile(this, predicate);
        if (!itemStack2.isEmpty()) {
            return itemStack2;
        }
        predicate = ((ProjectileWeaponItem)itemStack.getItem()).getAllSupportedProjectiles();
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack3 = this.inventory.getItem(i);
            if (!predicate.test(itemStack3)) continue;
            return itemStack3;
        }
        return this.abilities.instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack eat(Level level, ItemStack itemStack, FoodProperties foodProperties) {
        this.getFoodData().eat(foodProperties);
        this.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
        if (this instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)this, itemStack);
        }
        ItemStack itemStack2 = super.eat(level, itemStack, foodProperties);
        Optional<ItemStack> optional = foodProperties.usingConvertsTo();
        if (optional.isPresent() && !this.hasInfiniteMaterials()) {
            if (itemStack2.isEmpty()) {
                return optional.get().copy();
            }
            if (!this.level().isClientSide()) {
                this.getInventory().add(optional.get().copy());
            }
        }
        return itemStack2;
    }

    @Override
    public Vec3 getRopeHoldPosition(float f) {
        double d = 0.22 * (this.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0);
        float f2 = Mth.lerp(f * 0.5f, this.getXRot(), this.xRotO) * ((float)Math.PI / 180);
        float f3 = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * ((float)Math.PI / 180);
        if (this.isFallFlying() || this.isAutoSpinAttack()) {
            float f4;
            Vec3 vec3 = this.getViewVector(f);
            Vec3 vec32 = this.getDeltaMovement();
            double d2 = vec32.horizontalDistanceSqr();
            double d3 = vec3.horizontalDistanceSqr();
            if (d2 > 0.0 && d3 > 0.0) {
                double d4 = (vec32.x * vec3.x + vec32.z * vec3.z) / Math.sqrt(d2 * d3);
                double d5 = vec32.x * vec3.z - vec32.z * vec3.x;
                f4 = (float)(Math.signum(d5) * Math.acos(d4));
            } else {
                f4 = 0.0f;
            }
            return this.getPosition(f).add(new Vec3(d, -0.11, 0.85).zRot(-f4).xRot(-f2).yRot(-f3));
        }
        if (this.isVisuallySwimming()) {
            return this.getPosition(f).add(new Vec3(d, 0.2, -0.15).xRot(-f2).yRot(-f3));
        }
        double d6 = this.getBoundingBox().getYsize() - 1.0;
        double d7 = this.isCrouching() ? -0.2 : 0.07;
        return this.getPosition(f).add(new Vec3(d, d6, d7).yRot(-f3));
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    public boolean isScoping() {
        return this.isUsingItem() && this.getUseItem().is(Items.SPYGLASS);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    public Optional<GlobalPos> getLastDeathLocation() {
        return this.lastDeathLocation;
    }

    public void setLastDeathLocation(Optional<GlobalPos> optional) {
        this.lastDeathLocation = optional;
    }

    @Override
    public float getHurtDir() {
        return this.hurtDir;
    }

    @Override
    public void animateHurt(float f) {
        super.animateHurt(f);
        this.hurtDir = f;
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected float getFlyingSpeed() {
        if (this.abilities.flying && !this.isPassenger()) {
            return this.isSprinting() ? this.abilities.getFlyingSpeed() * 2.0f : this.abilities.getFlyingSpeed();
        }
        return this.isSprinting() ? 0.025999999f : 0.02f;
    }

    public double blockInteractionRange() {
        return this.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
    }

    public double entityInteractionRange() {
        return this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
    }

    public boolean canInteractWithEntity(Entity entity, double d) {
        if (entity.isRemoved()) {
            return false;
        }
        return this.canInteractWithEntity(entity.getBoundingBox(), d);
    }

    public boolean canInteractWithEntity(AABB aABB, double d) {
        double d2 = this.entityInteractionRange() + d;
        return aABB.distanceToSqr(this.getEyePosition()) < d2 * d2;
    }

    public boolean canInteractWithBlock(BlockPos blockPos, double d) {
        double d2 = this.blockInteractionRange() + d;
        return new AABB(blockPos).distanceToSqr(this.getEyePosition()) < d2 * d2;
    }

    public void setIgnoreFallDamageFromCurrentImpulse(boolean bl) {
        this.ignoreFallDamageFromCurrentImpulse = bl;
        this.currentImpulseContextResetGraceTime = bl ? 40 : 0;
    }

    public boolean isIgnoringFallDamageFromCurrentImpulse() {
        return this.ignoreFallDamageFromCurrentImpulse;
    }

    public void tryResetCurrentImpulseContext() {
        if (this.currentImpulseContextResetGraceTime == 0) {
            this.resetCurrentImpulseContext();
        }
    }

    public void resetCurrentImpulseContext() {
        this.currentImpulseContextResetGraceTime = 0;
        this.currentExplosionCause = null;
        this.currentImpulseImpactPos = null;
        this.ignoreFallDamageFromCurrentImpulse = false;
    }

    public static enum BedSleepingProblem {
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW(Component.translatable("block.minecraft.bed.no_sleep")),
        TOO_FAR_AWAY(Component.translatable("block.minecraft.bed.too_far_away")),
        OBSTRUCTED(Component.translatable("block.minecraft.bed.obstructed")),
        OTHER_PROBLEM,
        NOT_SAFE(Component.translatable("block.minecraft.bed.not_safe"));

        @Nullable
        private final Component message;

        private BedSleepingProblem() {
            this.message = null;
        }

        private BedSleepingProblem(Component component) {
            this.message = component;
        }

        @Nullable
        public Component getMessage() {
            return this.message;
        }
    }
}

