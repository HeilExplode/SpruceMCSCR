/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Wolf
extends TamableAnimal
implements NeutralMob,
VariantHolder<Holder<WolfVariant>> {
    private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Holder<WolfVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.WOLF_VARIANT);
    public static final Predicate<LivingEntity> PREY_SELECTOR = livingEntity -> {
        EntityType<?> entityType = livingEntity.getType();
        return entityType == EntityType.SHEEP || entityType == EntityType.RABBIT || entityType == EntityType.FOX;
    };
    private static final float START_HEALTH = 8.0f;
    private static final float TAME_HEALTH = 40.0f;
    private static final float ARMOR_REPAIR_UNIT = 0.125f;
    private float interestedAngle;
    private float interestedAngleO;
    private boolean isWet;
    private boolean isShaking;
    private float shakeAnim;
    private float shakeAnimO;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @Nullable
    private UUID persistentAngerTarget;

    public Wolf(EntityType<? extends Wolf> entityType, Level level) {
        super((EntityType<? extends TamableAnimal>)entityType, level);
        this.setTame(false, false);
        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0f);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new WolfAvoidEntityGoal<Llama>(this, Llama.class, 24.0f, 1.5, 1.5));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new BegGoal(this, 8.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<Animal>(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<Turtle>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<AbstractSkeleton>((Mob)this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<Wolf>(this, true));
    }

    public ResourceLocation getTexture() {
        WolfVariant wolfVariant = (WolfVariant)this.getVariant().value();
        if (this.isTame()) {
            return wolfVariant.tameTexture();
        }
        if (this.isAngry()) {
            return wolfVariant.angryTexture();
        }
        return wolfVariant.wildTexture();
    }

    @Override
    public Holder<WolfVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    public void setVariant(Holder<WolfVariant> holder) {
        this.entityData.set(DATA_VARIANT_ID, holder);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        RegistryAccess registryAccess = this.registryAccess();
        Registry<WolfVariant> registry = registryAccess.registryOrThrow(Registries.WOLF_VARIANT);
        builder.define(DATA_VARIANT_ID, (Holder)registry.getHolder(WolfVariants.DEFAULT).or(registry::getAny).orElseThrow());
        builder.define(DATA_INTERESTED_ID, false);
        builder.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15f, 1.0f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putByte("CollarColor", (byte)this.getCollarColor().getId());
        this.getVariant().unwrapKey().ifPresent(resourceKey -> compoundTag.putString("variant", resourceKey.location().toString()));
        this.addPersistentAngerSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString("variant"))).map(resourceLocation -> ResourceKey.create(Registries.WOLF_VARIANT, resourceLocation)).flatMap(resourceKey -> this.registryAccess().registryOrThrow(Registries.WOLF_VARIANT).getHolder((ResourceKey<WolfVariant>)resourceKey)).ifPresent(this::setVariant);
        if (compoundTag.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
        }
        this.readPersistentAngerSaveData((Level)this.level(), compoundTag);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        Holder<WolfVariant> holder;
        Holder<Biome> holder2 = serverLevelAccessor.getBiome(this.blockPosition());
        if (spawnGroupData instanceof WolfPackData) {
            WolfPackData wolfPackData = (WolfPackData)spawnGroupData;
            holder = wolfPackData.type;
        } else {
            holder = WolfVariants.getSpawnVariant(this.registryAccess(), holder2);
            spawnGroupData = new WolfPackData(holder);
        }
        this.setVariant(holder);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAngry()) {
            return SoundEvents.WOLF_GROWL;
        }
        if (this.random.nextInt(3) == 0) {
            if (this.isTame() && this.getHealth() < 20.0f) {
                return SoundEvents.WOLF_WHINE;
            }
            return SoundEvents.WOLF_PANT;
        }
        return SoundEvents.WOLF_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.canArmorAbsorb(damageSource)) {
            return SoundEvents.WOLF_ARMOR_DAMAGE;
        }
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!((Level)this.level()).isClientSide && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
            ((Level)this.level()).broadcastEntityEvent(this, (byte)8);
        }
        if (!((Level)this.level()).isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        this.interestedAngleO = this.interestedAngle;
        this.interestedAngle = this.isInterested() ? (this.interestedAngle += (1.0f - this.interestedAngle) * 0.4f) : (this.interestedAngle += (0.0f - this.interestedAngle) * 0.4f);
        if (this.isInWaterRainOrBubble()) {
            this.isWet = true;
            if (this.isShaking && !((Level)this.level()).isClientSide) {
                ((Level)this.level()).broadcastEntityEvent(this, (byte)56);
                this.cancelShake();
            }
        } else if ((this.isWet || this.isShaking) && this.isShaking) {
            if (this.shakeAnim == 0.0f) {
                this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
            this.shakeAnimO = this.shakeAnim;
            this.shakeAnim += 0.05f;
            if (this.shakeAnimO >= 2.0f) {
                this.isWet = false;
                this.isShaking = false;
                this.shakeAnimO = 0.0f;
                this.shakeAnim = 0.0f;
            }
            if (this.shakeAnim > 0.4f) {
                float f = (float)this.getY();
                int n = (int)(Mth.sin((this.shakeAnim - 0.4f) * (float)Math.PI) * 7.0f);
                Vec3 vec3 = this.getDeltaMovement();
                for (int i = 0; i < n; ++i) {
                    float f2 = (this.random.nextFloat() * 2.0f - 1.0f) * this.getBbWidth() * 0.5f;
                    float f3 = (this.random.nextFloat() * 2.0f - 1.0f) * this.getBbWidth() * 0.5f;
                    ((Level)this.level()).addParticle(ParticleTypes.SPLASH, this.getX() + (double)f2, f + 0.8f, this.getZ() + (double)f3, vec3.x, vec3.y, vec3.z);
                }
            }
        }
    }

    private void cancelShake() {
        this.isShaking = false;
        this.shakeAnim = 0.0f;
        this.shakeAnimO = 0.0f;
    }

    @Override
    public void die(DamageSource damageSource) {
        this.isWet = false;
        this.isShaking = false;
        this.shakeAnimO = 0.0f;
        this.shakeAnim = 0.0f;
        super.die(damageSource);
    }

    public boolean isWet() {
        return this.isWet;
    }

    public float getWetShade(float f) {
        return Math.min(0.75f + Mth.lerp(f, this.shakeAnimO, this.shakeAnim) / 2.0f * 0.25f, 1.0f);
    }

    public float getBodyRollAngle(float f, float f2) {
        float f3 = (Mth.lerp(f, this.shakeAnimO, this.shakeAnim) + f2) / 1.8f;
        if (f3 < 0.0f) {
            f3 = 0.0f;
        } else if (f3 > 1.0f) {
            f3 = 1.0f;
        }
        return Mth.sin(f3 * (float)Math.PI) * Mth.sin(f3 * (float)Math.PI * 11.0f) * 0.15f * (float)Math.PI;
    }

    public float getHeadRollAngle(float f) {
        return Mth.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.15f * (float)Math.PI;
    }

    @Override
    public int getMaxHeadXRot() {
        if (this.isInSittingPose()) {
            return 20;
        }
        return super.getMaxHeadXRot();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!((Level)this.level()).isClientSide) {
            this.setOrderedToSit(false);
        }
        return super.hurt(damageSource, f);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return true;
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float f) {
        if (!this.canArmorAbsorb(damageSource)) {
            super.actuallyHurt(damageSource, f);
            return;
        }
        ItemStack itemStack = this.getBodyArmorItem();
        int n = itemStack.getDamageValue();
        int n2 = itemStack.getMaxDamage();
        itemStack.hurtAndBreak(Mth.ceil(f), this, EquipmentSlot.BODY);
        if (Crackiness.WOLF_ARMOR.byDamage(n, n2) != Crackiness.WOLF_ARMOR.byDamage(this.getBodyArmorItem())) {
            this.playSound(SoundEvents.WOLF_ARMOR_CRACK);
            EntityGetter entityGetter = this.level();
            if (entityGetter instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)entityGetter;
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultInstance()), this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.2, 0.1, 0.2, 0.1);
            }
        }
    }

    private boolean canArmorAbsorb(DamageSource damageSource) {
        return this.hasArmor() && !damageSource.is(DamageTypeTags.BYPASSES_WOLF_ARMOR);
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0f);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0);
        }
    }

    @Override
    protected void hurtArmor(DamageSource damageSource, float f) {
        this.doHurtEquipment(damageSource, f, EquipmentSlot.BODY);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (!(!((Level)this.level()).isClientSide || this.isBaby() && this.isFood(itemStack))) {
            boolean bl = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.BONE) && !this.isTame() && !this.isAngry();
            return bl ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                itemStack.consume(1, player);
                FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
                float f = foodProperties != null ? (float)foodProperties.nutrition() : 1.0f;
                this.heal(2.0f * f);
                return InteractionResult.sidedSuccess(((Level)this.level()).isClientSide());
            }
            if (item instanceof DyeItem) {
                DyeItem dyeItem = (DyeItem)item;
                if (this.isOwnedBy(player)) {
                    DyeColor dyeColor = dyeItem.getDyeColor();
                    if (dyeColor == this.getCollarColor()) return super.mobInteract(player, interactionHand);
                    this.setCollarColor(dyeColor);
                    itemStack.consume(1, player);
                    return InteractionResult.SUCCESS;
                }
            }
            if (itemStack.is(Items.WOLF_ARMOR) && this.isOwnedBy(player) && this.getBodyArmorItem().isEmpty() && !this.isBaby()) {
                this.setBodyArmorItem(itemStack.copyWithCount(1));
                itemStack.consume(1, player);
                return InteractionResult.SUCCESS;
            }
            if (itemStack.is(Items.SHEARS) && this.isOwnedBy(player) && this.hasArmor() && (!EnchantmentHelper.has(this.getBodyArmorItem(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative())) {
                itemStack.hurtAndBreak(1, player, Wolf.getSlotForHand(interactionHand));
                this.playSound(SoundEvents.ARMOR_UNEQUIP_WOLF);
                ItemStack itemStack2 = this.getBodyArmorItem();
                this.setBodyArmorItem(ItemStack.EMPTY);
                this.spawnAtLocation(itemStack2);
                return InteractionResult.SUCCESS;
            }
            if (ArmorMaterials.ARMADILLO.value().repairIngredient().get().test(itemStack) && this.isInSittingPose() && this.hasArmor() && this.isOwnedBy(player) && this.getBodyArmorItem().isDamaged()) {
                itemStack.shrink(1);
                this.playSound(SoundEvents.WOLF_ARMOR_REPAIR);
                ItemStack itemStack3 = this.getBodyArmorItem();
                int n = (int)((float)itemStack3.getMaxDamage() * 0.125f);
                itemStack3.setDamageValue(Math.max(0, itemStack3.getDamageValue() - n));
                return InteractionResult.SUCCESS;
            }
            InteractionResult interactionResult = super.mobInteract(player, interactionHand);
            if (interactionResult.consumesAction() || !this.isOwnedBy(player)) return interactionResult;
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        if (!itemStack.is(Items.BONE) || this.isAngry()) return super.mobInteract(player, interactionHand);
        itemStack.consume(1, player);
        this.tryToTame(player);
        return InteractionResult.SUCCESS;
    }

    private void tryToTame(Player player) {
        if (this.random.nextInt(3) == 0) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            ((Level)this.level()).broadcastEntityEvent(this, (byte)7);
        } else {
            ((Level)this.level()).broadcastEntityEvent(this, (byte)6);
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 8) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
        } else if (by == 56) {
            this.cancelShake();
        } else {
            super.handleEntityEvent(by);
        }
    }

    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804f;
        }
        if (this.isTame()) {
            float f = this.getMaxHealth();
            float f2 = (f - this.getHealth()) / f;
            return (0.55f - f2 * 0.4f) * (float)Math.PI;
        }
        return 0.62831855f;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.WOLF_FOOD);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int n) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, n);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID uUID) {
        this.persistentAngerTarget = uUID;
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    public boolean hasArmor() {
        return this.getBodyArmorItem().is(Items.WOLF_ARMOR);
    }

    private void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
    }

    @Override
    @Nullable
    public Wolf getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Wolf wolf = EntityType.WOLF.create(serverLevel);
        if (wolf != null && ageableMob instanceof Wolf) {
            Wolf wolf2 = (Wolf)ageableMob;
            if (this.random.nextBoolean()) {
                wolf.setVariant((Holder<WolfVariant>)this.getVariant());
            } else {
                wolf.setVariant((Holder<WolfVariant>)wolf2.getVariant());
            }
            if (this.isTame()) {
                wolf.setOwnerUUID(this.getOwnerUUID());
                wolf.setTame(true, true);
                if (this.random.nextBoolean()) {
                    wolf.setCollarColor(this.getCollarColor());
                } else {
                    wolf.setCollarColor(wolf2.getCollarColor());
                }
            }
        }
        return wolf;
    }

    public void setIsInterested(boolean bl) {
        this.entityData.set(DATA_INTERESTED_ID, bl);
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        if (!this.isTame()) {
            return false;
        }
        if (!(animal instanceof Wolf)) {
            return false;
        }
        Wolf wolf = (Wolf)animal;
        if (!wolf.isTame()) {
            return false;
        }
        if (wolf.isInSittingPose()) {
            return false;
        }
        return this.isInLove() && wolf.isInLove();
    }

    public boolean isInterested() {
        return this.entityData.get(DATA_INTERESTED_ID);
    }

    @Override
    public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
        LivingEntity livingEntity3;
        if (livingEntity instanceof Creeper || livingEntity instanceof Ghast || livingEntity instanceof ArmorStand) {
            return false;
        }
        if (livingEntity instanceof Wolf) {
            Wolf wolf = (Wolf)livingEntity;
            return !wolf.isTame() || wolf.getOwner() != livingEntity2;
        }
        if (livingEntity instanceof Player) {
            Player player;
            livingEntity3 = (Player)livingEntity;
            if (livingEntity2 instanceof Player && !(player = (Player)livingEntity2).canHarmPlayer((Player)livingEntity3)) {
                return false;
            }
        }
        if (livingEntity instanceof AbstractHorse && ((AbstractHorse)(livingEntity3 = (AbstractHorse)livingEntity)).isTamed()) {
            return false;
        }
        return !(livingEntity instanceof TamableAnimal) || !((TamableAnimal)(livingEntity3 = (TamableAnimal)livingEntity)).isTame();
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isAngry();
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    public static boolean checkWolfSpawnRules(EntityType<Wolf> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.WOLVES_SPAWNABLE_ON) && Wolf.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    @Override
    public /* synthetic */ void setVariant(Object object) {
        this.setVariant((Holder)object);
    }

    class WolfAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Wolf wolf;

        public WolfAvoidEntityGoal(Wolf wolf2, Class<T> clazz, float f, double d, double d2) {
            super(wolf2, clazz, f, d, d2);
            this.wolf = wolf2;
        }

        @Override
        public boolean canUse() {
            if (super.canUse() && this.toAvoid instanceof Llama) {
                return !this.wolf.isTame() && this.avoidLlama((Llama)this.toAvoid);
            }
            return false;
        }

        private boolean avoidLlama(Llama llama) {
            return llama.getStrength() >= Wolf.this.random.nextInt(5);
        }

        @Override
        public void start() {
            Wolf.this.setTarget(null);
            super.start();
        }

        @Override
        public void tick() {
            Wolf.this.setTarget(null);
            super.tick();
        }
    }

    public static class WolfPackData
    extends AgeableMob.AgeableMobGroupData {
        public final Holder<WolfVariant> type;

        public WolfPackData(Holder<WolfVariant> holder) {
            super(false);
            this.type = holder;
        }
    }
}

