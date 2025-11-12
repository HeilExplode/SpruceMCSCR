/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.tags.CatVariantTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

public class Cat
extends TamableAnimal
implements VariantHolder<Holder<CatVariant>> {
    public static final double TEMPT_SPEED_MOD = 0.6;
    public static final double WALK_SPEED_MOD = 0.8;
    public static final double SPRINT_SPEED_MOD = 1.33;
    private static final EntityDataAccessor<Holder<CatVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.CAT_VARIANT);
    private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RELAX_STATE_ONE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
    private static final ResourceKey<CatVariant> DEFAULT_VARIANT = CatVariant.BLACK;
    @Nullable
    private CatAvoidEntityGoal<Player> avoidPlayersGoal;
    @Nullable
    private TemptGoal temptGoal;
    private float lieDownAmount;
    private float lieDownAmountO;
    private float lieDownAmountTail;
    private float lieDownAmountOTail;
    private float relaxStateOneAmount;
    private float relaxStateOneAmountO;

    public Cat(EntityType<? extends Cat> entityType, Level level) {
        super((EntityType<? extends TamableAnimal>)entityType, level);
        this.reassessTameGoals();
    }

    public ResourceLocation getTextureId() {
        return ((CatVariant)this.getVariant().value()).texture();
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new CatTemptGoal(this, 0.6, itemStack -> itemStack.is(ItemTags.CAT_FOOD), true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new CatRelaxOnOwnerGoal(this));
        this.goalSelector.addGoal(4, this.temptGoal);
        this.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1, 8));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0f, 5.0f));
        this.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3f));
        this.goalSelector.addGoal(9, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(10, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 0.8, 1.0000001E-5f));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<Rabbit>(this, Rabbit.class, false, null));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<Turtle>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    public Holder<CatVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    public void setVariant(Holder<CatVariant> holder) {
        this.entityData.set(DATA_VARIANT_ID, holder);
    }

    public void setLying(boolean bl) {
        this.entityData.set(IS_LYING, bl);
    }

    public boolean isLying() {
        return this.entityData.get(IS_LYING);
    }

    void setRelaxStateOne(boolean bl) {
        this.entityData.set(RELAX_STATE_ONE, bl);
    }

    boolean isRelaxStateOne() {
        return this.entityData.get(RELAX_STATE_ONE);
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    private void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, BuiltInRegistries.CAT_VARIANT.getHolderOrThrow(DEFAULT_VARIANT));
        builder.define(IS_LYING, false);
        builder.define(RELAX_STATE_ONE, false);
        builder.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putString("variant", this.getVariant().unwrapKey().orElse(DEFAULT_VARIANT).location().toString());
        compoundTag.putByte("CollarColor", (byte)this.getCollarColor().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString("variant"))).map(resourceLocation -> ResourceKey.create(Registries.CAT_VARIANT, resourceLocation)).flatMap(BuiltInRegistries.CAT_VARIANT::getHolder).ifPresent(this::setVariant);
        if (compoundTag.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
        }
    }

    @Override
    public void customServerAiStep() {
        if (this.getMoveControl().hasWanted()) {
            double d = this.getMoveControl().getSpeedModifier();
            if (d == 0.6) {
                this.setPose(Pose.CROUCHING);
                this.setSprinting(false);
            } else if (d == 1.33) {
                this.setPose(Pose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(Pose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(Pose.STANDING);
            this.setSprinting(false);
        }
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isTame()) {
            if (this.isInLove()) {
                return SoundEvents.CAT_PURR;
            }
            if (this.random.nextInt(4) == 0) {
                return SoundEvents.CAT_PURREOW;
            }
            return SoundEvents.CAT_AMBIENT;
        }
        return SoundEvents.CAT_STRAY_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    public void hiss() {
        this.makeSound(SoundEvents.CAT_HISS);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void usePlayerItem(Player player, InteractionHand interactionHand, ItemStack itemStack) {
        if (this.isFood(itemStack)) {
            this.playSound(SoundEvents.CAT_EAT, 1.0f, 1.0f);
        }
        super.usePlayerItem(player, interactionHand, itemStack);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.temptGoal != null && this.temptGoal.isRunning() && !this.isTame() && this.tickCount % 100 == 0) {
            this.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0f, 1.0f);
        }
        this.handleLieDown();
    }

    private void handleLieDown() {
        if ((this.isLying() || this.isRelaxStateOne()) && this.tickCount % 5 == 0) {
            this.playSound(SoundEvents.CAT_PURR, 0.6f + 0.4f * (this.random.nextFloat() - this.random.nextFloat()), 1.0f);
        }
        this.updateLieDownAmount();
        this.updateRelaxStateOneAmount();
    }

    private void updateLieDownAmount() {
        this.lieDownAmountO = this.lieDownAmount;
        this.lieDownAmountOTail = this.lieDownAmountTail;
        if (this.isLying()) {
            this.lieDownAmount = Math.min(1.0f, this.lieDownAmount + 0.15f);
            this.lieDownAmountTail = Math.min(1.0f, this.lieDownAmountTail + 0.08f);
        } else {
            this.lieDownAmount = Math.max(0.0f, this.lieDownAmount - 0.22f);
            this.lieDownAmountTail = Math.max(0.0f, this.lieDownAmountTail - 0.13f);
        }
    }

    private void updateRelaxStateOneAmount() {
        this.relaxStateOneAmountO = this.relaxStateOneAmount;
        this.relaxStateOneAmount = this.isRelaxStateOne() ? Math.min(1.0f, this.relaxStateOneAmount + 0.1f) : Math.max(0.0f, this.relaxStateOneAmount - 0.13f);
    }

    public float getLieDownAmount(float f) {
        return Mth.lerp(f, this.lieDownAmountO, this.lieDownAmount);
    }

    public float getLieDownAmountTail(float f) {
        return Mth.lerp(f, this.lieDownAmountOTail, this.lieDownAmountTail);
    }

    public float getRelaxStateOneAmount(float f) {
        return Mth.lerp(f, this.relaxStateOneAmountO, this.relaxStateOneAmount);
    }

    @Override
    @Nullable
    public Cat getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Cat cat = EntityType.CAT.create(serverLevel);
        if (cat != null && ageableMob instanceof Cat) {
            Cat cat2 = (Cat)ageableMob;
            if (this.random.nextBoolean()) {
                cat.setVariant((Holder<CatVariant>)this.getVariant());
            } else {
                cat.setVariant((Holder<CatVariant>)cat2.getVariant());
            }
            if (this.isTame()) {
                cat.setOwnerUUID(this.getOwnerUUID());
                cat.setTame(true, true);
                if (this.random.nextBoolean()) {
                    cat.setCollarColor(this.getCollarColor());
                } else {
                    cat.setCollarColor(cat2.getCollarColor());
                }
            }
        }
        return cat;
    }

    @Override
    public boolean canMate(Animal animal) {
        if (!this.isTame()) {
            return false;
        }
        if (!(animal instanceof Cat)) {
            return false;
        }
        Cat cat = (Cat)animal;
        return cat.isTame() && super.canMate(animal);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        boolean bl = serverLevelAccessor.getMoonBrightness() > 0.9f;
        TagKey<CatVariant> tagKey = bl ? CatVariantTags.FULL_MOON_SPAWNS : CatVariantTags.DEFAULT_SPAWNS;
        BuiltInRegistries.CAT_VARIANT.getRandomElementOf(tagKey, serverLevelAccessor.getRandom()).ifPresent(this::setVariant);
        ServerLevel serverLevel = serverLevelAccessor.getLevel();
        if (serverLevel.structureManager().getStructureWithPieceAt(this.blockPosition(), StructureTags.CATS_SPAWN_AS_BLACK).isValid()) {
            this.setVariant((Holder<CatVariant>)BuiltInRegistries.CAT_VARIANT.getHolderOrThrow(CatVariant.ALL_BLACK));
            this.setPersistenceRequired();
        }
        return spawnGroupData;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                Object object;
                if (item instanceof DyeItem) {
                    object = (DyeItem)item;
                    DyeColor dyeColor = ((DyeItem)object).getDyeColor();
                    if (dyeColor != this.getCollarColor()) {
                        if (!((Level)this.level()).isClientSide()) {
                            this.setCollarColor(dyeColor);
                            itemStack.consume(1, player);
                            this.setPersistenceRequired();
                        }
                        return InteractionResult.sidedSuccess(((Level)this.level()).isClientSide());
                    }
                } else if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                    if (!((Level)this.level()).isClientSide()) {
                        this.usePlayerItem(player, interactionHand, itemStack);
                        FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
                        this.heal(foodProperties != null ? (float)foodProperties.nutrition() : 1.0f);
                    }
                    return InteractionResult.sidedSuccess(((Level)this.level()).isClientSide());
                }
                if (!((InteractionResult)((Object)(object = super.mobInteract(player, interactionHand)))).consumesAction()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    return InteractionResult.sidedSuccess(((Level)this.level()).isClientSide());
                }
                return object;
            }
        } else if (this.isFood(itemStack)) {
            if (!((Level)this.level()).isClientSide()) {
                this.usePlayerItem(player, interactionHand, itemStack);
                this.tryToTame(player);
                this.setPersistenceRequired();
            }
            return InteractionResult.sidedSuccess(((Level)this.level()).isClientSide());
        }
        if ((interactionResult = super.mobInteract(player, interactionHand)).consumesAction()) {
            this.setPersistenceRequired();
        }
        return interactionResult;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.CAT_FOOD);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.isTame() && this.tickCount > 2400;
    }

    @Override
    public void setTame(boolean bl, boolean bl2) {
        super.setTame(bl, bl2);
        this.reassessTameGoals();
    }

    protected void reassessTameGoals() {
        if (this.avoidPlayersGoal == null) {
            this.avoidPlayersGoal = new CatAvoidEntityGoal<Player>(this, Player.class, 16.0f, 0.8, 1.33);
        }
        this.goalSelector.removeGoal(this.avoidPlayersGoal);
        if (!this.isTame()) {
            this.goalSelector.addGoal(4, this.avoidPlayersGoal);
        }
    }

    private void tryToTame(Player player) {
        if (this.random.nextInt(3) == 0) {
            this.tame(player);
            this.setOrderedToSit(true);
            ((Level)this.level()).broadcastEntityEvent(this, (byte)7);
        } else {
            ((Level)this.level()).broadcastEntityEvent(this, (byte)6);
        }
    }

    @Override
    public boolean isSteppingCarefully() {
        return this.isCrouching() || super.isSteppingCarefully();
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

    static class CatTemptGoal
    extends TemptGoal {
        @Nullable
        private Player selectedPlayer;
        private final Cat cat;

        public CatTemptGoal(Cat cat, double d, Predicate<ItemStack> predicate, boolean bl) {
            super(cat, d, predicate, bl);
            this.cat = cat;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.selectedPlayer == null && this.mob.getRandom().nextInt(this.adjustedTickDelay(600)) == 0) {
                this.selectedPlayer = this.player;
            } else if (this.mob.getRandom().nextInt(this.adjustedTickDelay(500)) == 0) {
                this.selectedPlayer = null;
            }
        }

        @Override
        protected boolean canScare() {
            if (this.selectedPlayer != null && this.selectedPlayer.equals(this.player)) {
                return false;
            }
            return super.canScare();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.cat.isTame();
        }
    }

    static class CatRelaxOnOwnerGoal
    extends Goal {
        private final Cat cat;
        @Nullable
        private Player ownerPlayer;
        @Nullable
        private BlockPos goalPos;
        private int onBedTicks;

        public CatRelaxOnOwnerGoal(Cat cat) {
            this.cat = cat;
        }

        @Override
        public boolean canUse() {
            if (!this.cat.isTame()) {
                return false;
            }
            if (this.cat.isOrderedToSit()) {
                return false;
            }
            LivingEntity livingEntity = this.cat.getOwner();
            if (livingEntity instanceof Player) {
                this.ownerPlayer = (Player)livingEntity;
                if (!livingEntity.isSleeping()) {
                    return false;
                }
                if (this.cat.distanceToSqr(this.ownerPlayer) > 100.0) {
                    return false;
                }
                BlockPos blockPos = this.ownerPlayer.blockPosition();
                BlockState blockState = ((Level)this.cat.level()).getBlockState(blockPos);
                if (blockState.is(BlockTags.BEDS)) {
                    this.goalPos = blockState.getOptionalValue(BedBlock.FACING).map(direction -> blockPos.relative(direction.getOpposite())).orElseGet(() -> new BlockPos(blockPos));
                    return !this.spaceIsOccupied();
                }
            }
            return false;
        }

        private boolean spaceIsOccupied() {
            List<Cat> list = this.cat.level().getEntitiesOfClass(Cat.class, new AABB(this.goalPos).inflate(2.0));
            for (Cat cat : list) {
                if (cat == this.cat || !cat.isLying() && !cat.isRelaxStateOne()) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.cat.isTame() && !this.cat.isOrderedToSit() && this.ownerPlayer != null && this.ownerPlayer.isSleeping() && this.goalPos != null && !this.spaceIsOccupied();
        }

        @Override
        public void start() {
            if (this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.1f);
            }
        }

        @Override
        public void stop() {
            this.cat.setLying(false);
            float f = this.cat.level().getTimeOfDay(1.0f);
            if (this.ownerPlayer.getSleepTimer() >= 100 && (double)f > 0.77 && (double)f < 0.8 && (double)((Level)this.cat.level()).getRandom().nextFloat() < 0.7) {
                this.giveMorningGift();
            }
            this.onBedTicks = 0;
            this.cat.setRelaxStateOne(false);
            this.cat.getNavigation().stop();
        }

        private void giveMorningGift() {
            RandomSource randomSource = this.cat.getRandom();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            mutableBlockPos.set(this.cat.isLeashed() ? this.cat.getLeashHolder().blockPosition() : this.cat.blockPosition());
            this.cat.randomTeleport(mutableBlockPos.getX() + randomSource.nextInt(11) - 5, mutableBlockPos.getY() + randomSource.nextInt(5) - 2, mutableBlockPos.getZ() + randomSource.nextInt(11) - 5, false);
            mutableBlockPos.set(this.cat.blockPosition());
            LootTable lootTable = ((Level)this.cat.level()).getServer().reloadableRegistries().getLootTable(BuiltInLootTables.CAT_MORNING_GIFT);
            LootParams lootParams = new LootParams.Builder((ServerLevel)this.cat.level()).withParameter(LootContextParams.ORIGIN, this.cat.position()).withParameter(LootContextParams.THIS_ENTITY, this.cat).create(LootContextParamSets.GIFT);
            ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams);
            for (ItemStack itemStack : objectArrayList) {
                this.cat.level().addFreshEntity(new ItemEntity((Level)this.cat.level(), (double)mutableBlockPos.getX() - (double)Mth.sin(this.cat.yBodyRot * ((float)Math.PI / 180)), mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + (double)Mth.cos(this.cat.yBodyRot * ((float)Math.PI / 180)), itemStack));
            }
        }

        @Override
        public void tick() {
            if (this.ownerPlayer != null && this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.1f);
                if (this.cat.distanceToSqr(this.ownerPlayer) < 2.5) {
                    ++this.onBedTicks;
                    if (this.onBedTicks > this.adjustedTickDelay(16)) {
                        this.cat.setLying(true);
                        this.cat.setRelaxStateOne(false);
                    } else {
                        this.cat.lookAt(this.ownerPlayer, 45.0f, 45.0f);
                        this.cat.setRelaxStateOne(true);
                    }
                } else {
                    this.cat.setLying(false);
                }
            }
        }
    }

    static class CatAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Cat cat;

        public CatAvoidEntityGoal(Cat cat, Class<T> clazz, float f, double d, double d2) {
            super(cat, clazz, f, d, d2, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.cat = cat;
        }

        @Override
        public boolean canUse() {
            return !this.cat.isTame() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.cat.isTame() && super.canContinueToUse();
        }
    }
}

