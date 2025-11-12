/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal.horse;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Llama
extends AbstractChestedHorse
implements VariantHolder<Variant>,
RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.LLAMA.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.LLAMA.getHeight() - 0.8125f, -0.3f)).scale(0.5f);
    boolean didSpit;
    @Nullable
    private Llama caravanHead;
    @Nullable
    private Llama caravanTail;

    public Llama(EntityType<? extends Llama> entityType, Level level) {
        super((EntityType<? extends AbstractChestedHorse>)entityType, level);
    }

    public boolean isTraderLlama() {
        return false;
    }

    private void setStrength(int n) {
        this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, n)));
    }

    private void setRandomStrength(RandomSource randomSource) {
        int n = randomSource.nextFloat() < 0.04f ? 5 : 3;
        this.setStrength(1 + randomSource.nextInt(n));
    }

    public int getStrength() {
        return this.entityData.get(DATA_STRENGTH_ID);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", this.getVariant().id);
        compoundTag.putInt("Strength", this.getStrength());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.setStrength(compoundTag.getInt("Strength"));
        super.readAdditionalSaveData(compoundTag);
        this.setVariant(Variant.byId(compoundTag.getInt("Variant")));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1f));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0f));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25, itemStack -> itemStack.is(ItemTags.LLAMA_TEMPT_ITEMS), false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new LlamaHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new LlamaAttackWolfGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Llama.createBaseChestedHorseAttributes().add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STRENGTH_ID, 0);
        builder.define(DATA_VARIANT_ID, 0);
    }

    @Override
    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    @Override
    public void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant.id);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.LLAMA_FOOD);
    }

    @Override
    protected boolean handleEating(Player player, ItemStack itemStack) {
        SoundEvent soundEvent;
        int n = 0;
        int n2 = 0;
        float f = 0.0f;
        boolean bl = false;
        if (itemStack.is(Items.WHEAT)) {
            n = 10;
            n2 = 3;
            f = 2.0f;
        } else if (itemStack.is(Blocks.HAY_BLOCK.asItem())) {
            n = 90;
            n2 = 6;
            f = 10.0f;
            if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
                bl = true;
                this.setInLove(player);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && f > 0.0f) {
            this.heal(f);
            bl = true;
        }
        if (this.isBaby() && n > 0) {
            ((Level)this.level()).addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!((Level)this.level()).isClientSide) {
                this.ageUp(n);
            }
            bl = true;
        }
        if (n2 > 0 && (bl || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
            bl = true;
            if (!((Level)this.level()).isClientSide) {
                this.modifyTemper(n2);
            }
        }
        if (bl && !this.isSilent() && (soundEvent = this.getEatingSound()) != null) {
            ((Level)this.level()).playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(), this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return bl;
    }

    @Override
    public boolean isImmobile() {
        return this.isDeadOrDying() || this.isEating();
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        Variant variant;
        RandomSource randomSource = serverLevelAccessor.getRandom();
        this.setRandomStrength(randomSource);
        if (spawnGroupData instanceof LlamaGroupData) {
            variant = ((LlamaGroupData)spawnGroupData).variant;
        } else {
            variant = Util.getRandom(Variant.values(), randomSource);
            spawnGroupData = new LlamaGroupData(variant);
        }
        this.setVariant(variant);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.LLAMA_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.LLAMA_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? this.getStrength() : 0;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return true;
    }

    @Override
    public boolean isBodyArmorItem(ItemStack itemStack) {
        return itemStack.is(ItemTags.WOOL_CARPETS);
    }

    @Override
    public boolean isSaddleable() {
        return false;
    }

    @Nullable
    private static DyeColor getDyeColor(ItemStack itemStack) {
        Block block = Block.byItem(itemStack.getItem());
        if (block instanceof WoolCarpetBlock) {
            return ((WoolCarpetBlock)block).getColor();
        }
        return null;
    }

    @Nullable
    public DyeColor getSwag() {
        return Llama.getDyeColor(this.getItemBySlot(EquipmentSlot.BODY));
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canMate(Animal animal) {
        return animal != this && animal instanceof Llama && this.canParent() && ((Llama)animal).canParent();
    }

    @Override
    @Nullable
    public Llama getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Llama llama = this.makeNewLlama();
        if (llama != null) {
            this.setOffspringAttributes(ageableMob, llama);
            Llama llama2 = (Llama)ageableMob;
            int n = this.random.nextInt(Math.max(this.getStrength(), llama2.getStrength())) + 1;
            if (this.random.nextFloat() < 0.03f) {
                ++n;
            }
            llama.setStrength(n);
            llama.setVariant(this.random.nextBoolean() ? this.getVariant() : llama2.getVariant());
        }
        return llama;
    }

    @Nullable
    protected Llama makeNewLlama() {
        return EntityType.LLAMA.create((Level)this.level());
    }

    private void spit(LivingEntity livingEntity) {
        LlamaSpit llamaSpit = new LlamaSpit((Level)this.level(), this);
        double d = livingEntity.getX() - this.getX();
        double d2 = livingEntity.getY(0.3333333333333333) - llamaSpit.getY();
        double d3 = livingEntity.getZ() - this.getZ();
        double d4 = Math.sqrt(d * d + d3 * d3) * (double)0.2f;
        llamaSpit.shoot(d, d2 + d4, d3, 1.5f, 10.0f);
        if (!this.isSilent()) {
            ((Level)this.level()).playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        this.level().addFreshEntity(llamaSpit);
        this.didSpit = true;
    }

    void setDidSpit(boolean bl) {
        this.didSpit = bl;
    }

    @Override
    public boolean causeFallDamage(float f, float f2, DamageSource damageSource) {
        int n = this.calculateFallDamage(f, f2);
        if (n <= 0) {
            return false;
        }
        if (f >= 6.0f) {
            this.hurt(damageSource, n);
            if (this.isVehicle()) {
                for (Entity entity : this.getIndirectPassengers()) {
                    entity.hurt(damageSource, n);
                }
            }
        }
        this.playBlockFallSound();
        return true;
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }
        this.caravanHead = null;
    }

    public void joinCaravan(Llama llama) {
        this.caravanHead = llama;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public Llama getCaravanHead() {
        return this.caravanHead;
    }

    @Override
    protected double followLeashSpeed() {
        return 2.0;
    }

    @Override
    protected void followMommy() {
        if (!this.inCaravan() && this.isBaby()) {
            super.followMommy();
        }
    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        this.spit(livingEntity);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75 * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        return Llama.getDefaultPassengerAttachmentPoint(this, entity, entityDimensions.attachments());
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

    public static enum Variant implements StringRepresentable
    {
        CREAMY(0, "creamy"),
        WHITE(1, "white"),
        BROWN(2, "brown"),
        GRAY(3, "gray");

        public static final Codec<Variant> CODEC;
        private static final IntFunction<Variant> BY_ID;
        final int id;
        private final String name;

        private Variant(int n2, String string2) {
            this.id = n2;
            this.name = string2;
        }

        public int getId() {
            return this.id;
        }

        public static Variant byId(int n) {
            return BY_ID.apply(n);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Variant::values);
            BY_ID = ByIdMap.continuous(Variant::getId, Variant.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        }
    }

    static class LlamaHurtByTargetGoal
    extends HurtByTargetGoal {
        public LlamaHurtByTargetGoal(Llama llama) {
            super(llama, new Class[0]);
        }

        @Override
        public boolean canContinueToUse() {
            Mob mob = this.mob;
            if (mob instanceof Llama) {
                Llama llama = (Llama)mob;
                if (llama.didSpit) {
                    llama.setDidSpit(false);
                    return false;
                }
            }
            return super.canContinueToUse();
        }
    }

    static class LlamaAttackWolfGoal
    extends NearestAttackableTargetGoal<Wolf> {
        public LlamaAttackWolfGoal(Llama llama) {
            super(llama, Wolf.class, 16, false, true, livingEntity -> !((Wolf)livingEntity).isTame());
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.25;
        }
    }

    static class LlamaGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        LlamaGroupData(Variant variant) {
            super(true);
            this.variant = variant;
        }
    }
}

