/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Spider
extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);
    private static final float SPIDER_SPECIAL_EFFECT_CHANCE = 0.1f;

    public Spider(EntityType<? extends Spider> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Armadillo>(this, Armadillo.class, 6.0f, 1.0, 1.2, livingEntity -> !((Armadillo)livingEntity).isScared()));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(4, new SpiderAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new SpiderTargetGoal<Player>(this, Player.class));
        this.targetSelector.addGoal(3, new SpiderTargetGoal<IronGolem>(this, IronGolem.class));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.setClimbing(this.horizontalCollision);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.3f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
        if (!blockState.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(blockState, vec3);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.is(MobEffects.POISON)) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean bl) {
        byte by = this.entityData.get(DATA_FLAGS_ID);
        by = bl ? (byte)(by | 1) : (byte)(by & 0xFFFFFFFE);
        this.entityData.set(DATA_FLAGS_ID, by);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        Object object;
        spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        RandomSource randomSource = serverLevelAccessor.getRandom();
        if (randomSource.nextInt(100) == 0 && (object = EntityType.SKELETON.create(this.level())) != null) {
            ((Entity)object).moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
            ((AbstractSkeleton)object).finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, null);
            ((Entity)object).startRiding(this);
        }
        if (spawnGroupData == null) {
            spawnGroupData = new SpiderEffectsGroupData();
            if (serverLevelAccessor.getDifficulty() == Difficulty.HARD && randomSource.nextFloat() < 0.1f * difficultyInstance.getSpecialMultiplier()) {
                ((SpiderEffectsGroupData)spawnGroupData).setRandomEffect(randomSource);
            }
        }
        if (spawnGroupData instanceof SpiderEffectsGroupData) {
            object = (SpiderEffectsGroupData)spawnGroupData;
            Holder<MobEffect> holder = ((SpiderEffectsGroupData)object).effect;
            if (holder != null) {
                this.addEffect(new MobEffectInstance(holder, -1));
            }
        }
        return spawnGroupData;
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity entity) {
        if (entity.getBbWidth() <= this.getBbWidth()) {
            return new Vec3(0.0, 0.3125 * (double)this.getScale(), 0.0);
        }
        return super.getVehicleAttachmentPoint(entity);
    }

    static class SpiderAttackGoal
    extends MeleeAttackGoal {
        public SpiderAttackGoal(Spider spider) {
            super(spider, 1.0, true);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        @Override
        public boolean canContinueToUse() {
            float f = this.mob.getLightLevelDependentMagicValue();
            if (f >= 0.5f && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget(null);
                return false;
            }
            return super.canContinueToUse();
        }
    }

    static class SpiderTargetGoal<T extends LivingEntity>
    extends NearestAttackableTargetGoal<T> {
        public SpiderTargetGoal(Spider spider, Class<T> clazz) {
            super((Mob)spider, clazz, true);
        }

        @Override
        public boolean canUse() {
            float f = this.mob.getLightLevelDependentMagicValue();
            if (f >= 0.5f) {
                return false;
            }
            return super.canUse();
        }
    }

    public static class SpiderEffectsGroupData
    implements SpawnGroupData {
        @Nullable
        public Holder<MobEffect> effect;

        public void setRandomEffect(RandomSource randomSource) {
            int n = randomSource.nextInt(5);
            if (n <= 1) {
                this.effect = MobEffects.MOVEMENT_SPEED;
            } else if (n <= 2) {
                this.effect = MobEffects.DAMAGE_BOOST;
            } else if (n <= 3) {
                this.effect = MobEffects.REGENERATION;
            } else if (n <= 4) {
                this.effect = MobEffects.INVISIBILITY;
            }
        }
    }
}

