/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Guardian
extends Monster {
    protected static final int ATTACK_TIME = 80;
    private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.INT);
    private float clientSideTailAnimation;
    private float clientSideTailAnimationO;
    private float clientSideTailAnimationSpeed;
    private float clientSideSpikesAnimation;
    private float clientSideSpikesAnimationO;
    @Nullable
    private LivingEntity clientSideCachedAttackTarget;
    private int clientSideAttackTime;
    private boolean clientSideTouchedGround;
    @Nullable
    protected RandomStrollGoal randomStrollGoal;

    public Guardian(EntityType<? extends Guardian> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
        this.xpReward = 10;
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.moveControl = new GuardianMoveControl(this);
        this.clientSideTailAnimationO = this.clientSideTailAnimation = this.random.nextFloat();
    }

    @Override
    protected void registerGoals() {
        MoveTowardsRestrictionGoal moveTowardsRestrictionGoal = new MoveTowardsRestrictionGoal(this, 1.0);
        this.randomStrollGoal = new RandomStrollGoal(this, 1.0, 80);
        this.goalSelector.addGoal(4, new GuardianAttackGoal(this));
        this.goalSelector.addGoal(5, moveTowardsRestrictionGoal);
        this.goalSelector.addGoal(7, this.randomStrollGoal);
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Guardian.class, 12.0f, 0.01f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        moveTowardsRestrictionGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, 10, true, false, new GuardianAttackSelector(this)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_MOVING, false);
        builder.define(DATA_ID_ATTACK_TARGET, 0);
    }

    public boolean isMoving() {
        return this.entityData.get(DATA_ID_MOVING);
    }

    void setMoving(boolean bl) {
        this.entityData.set(DATA_ID_MOVING, bl);
    }

    public int getAttackDuration() {
        return 80;
    }

    void setActiveAttackTarget(int n) {
        this.entityData.set(DATA_ID_ATTACK_TARGET, n);
    }

    public boolean hasActiveAttackTarget() {
        return this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
    }

    @Nullable
    public LivingEntity getActiveAttackTarget() {
        if (!this.hasActiveAttackTarget()) {
            return null;
        }
        if (this.level().isClientSide) {
            if (this.clientSideCachedAttackTarget != null) {
                return this.clientSideCachedAttackTarget;
            }
            Entity entity = this.level().getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET));
            if (entity instanceof LivingEntity) {
                this.clientSideCachedAttackTarget = (LivingEntity)entity;
                return this.clientSideCachedAttackTarget;
            }
            return null;
        }
        return this.getTarget();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_ID_ATTACK_TARGET.equals(entityDataAccessor)) {
            this.clientSideAttackTime = 0;
            this.clientSideCachedAttackTarget = null;
        }
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_AMBIENT : SoundEvents.GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_HURT : SoundEvents.GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_DEATH : SoundEvents.GUARDIAN_DEATH_LAND;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getFluidState(blockPos).is(FluidTags.WATER)) {
            return 10.0f + levelReader.getPathfindingCostFromLightLevels(blockPos);
        }
        return super.getWalkTargetValue(blockPos, levelReader);
    }

    @Override
    public void aiStep() {
        if (this.isAlive()) {
            if (this.level().isClientSide) {
                Object object;
                this.clientSideTailAnimationO = this.clientSideTailAnimation;
                if (!this.isInWater()) {
                    this.clientSideTailAnimationSpeed = 2.0f;
                    object = this.getDeltaMovement();
                    if (((Vec3)object).y > 0.0 && this.clientSideTouchedGround && !this.isSilent()) {
                        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundSource(), 1.0f, 1.0f, false);
                    }
                    this.clientSideTouchedGround = ((Vec3)object).y < 0.0 && this.level().loadedAndEntityCanStandOn(this.blockPosition().below(), this);
                } else {
                    this.clientSideTailAnimationSpeed = this.isMoving() ? (this.clientSideTailAnimationSpeed < 0.5f ? 4.0f : (this.clientSideTailAnimationSpeed += (0.5f - this.clientSideTailAnimationSpeed) * 0.1f)) : (this.clientSideTailAnimationSpeed += (0.125f - this.clientSideTailAnimationSpeed) * 0.2f);
                }
                this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
                this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
                this.clientSideSpikesAnimation = !this.isInWaterOrBubble() ? this.random.nextFloat() : (this.isMoving() ? (this.clientSideSpikesAnimation += (0.0f - this.clientSideSpikesAnimation) * 0.25f) : (this.clientSideSpikesAnimation += (1.0f - this.clientSideSpikesAnimation) * 0.06f));
                if (this.isMoving() && this.isInWater()) {
                    object = this.getViewVector(0.0f);
                    for (int i = 0; i < 2; ++i) {
                        this.level().addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.5) - ((Vec3)object).x * 1.5, this.getRandomY() - ((Vec3)object).y * 1.5, this.getRandomZ(0.5) - ((Vec3)object).z * 1.5, 0.0, 0.0, 0.0);
                    }
                }
                if (this.hasActiveAttackTarget()) {
                    if (this.clientSideAttackTime < this.getAttackDuration()) {
                        ++this.clientSideAttackTime;
                    }
                    if ((object = this.getActiveAttackTarget()) != null) {
                        this.getLookControl().setLookAt((Entity)object, 90.0f, 90.0f);
                        this.getLookControl().tick();
                        double d = this.getAttackAnimationScale(0.0f);
                        double d2 = ((Entity)object).getX() - this.getX();
                        double d3 = ((Entity)object).getY(0.5) - this.getEyeY();
                        double d4 = ((Entity)object).getZ() - this.getZ();
                        double d5 = Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
                        d2 /= d5;
                        d3 /= d5;
                        d4 /= d5;
                        double d6 = this.random.nextDouble();
                        while (d6 < d5) {
                            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d2 * (d6 += 1.8 - d + this.random.nextDouble() * (1.7 - d)), this.getEyeY() + d3 * d6, this.getZ() + d4 * d6, 0.0, 0.0, 0.0);
                        }
                    }
                }
            }
            if (this.isInWaterOrBubble()) {
                this.setAirSupply(300);
            } else if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextFloat() * 2.0f - 1.0f) * 0.4f, 0.5, (this.random.nextFloat() * 2.0f - 1.0f) * 0.4f));
                this.setYRot(this.random.nextFloat() * 360.0f);
                this.setOnGround(false);
                this.hasImpulse = true;
            }
            if (this.hasActiveAttackTarget()) {
                this.setYRot(this.yHeadRot);
            }
        }
        super.aiStep();
    }

    protected SoundEvent getFlopSound() {
        return SoundEvents.GUARDIAN_FLOP;
    }

    public float getTailAnimation(float f) {
        return Mth.lerp(f, this.clientSideTailAnimationO, this.clientSideTailAnimation);
    }

    public float getSpikesAnimation(float f) {
        return Mth.lerp(f, this.clientSideSpikesAnimationO, this.clientSideSpikesAnimation);
    }

    public float getAttackAnimationScale(float f) {
        return ((float)this.clientSideAttackTime + f) / (float)this.getAttackDuration();
    }

    public float getClientSideAttackTime() {
        return this.clientSideAttackTime;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    public static boolean checkGuardianSpawnRules(EntityType<? extends Guardian> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return !(randomSource.nextInt(20) != 0 && levelAccessor.canSeeSkyFromBelowWater(blockPos) || levelAccessor.getDifficulty() == Difficulty.PEACEFUL || !MobSpawnType.isSpawner(mobSpawnType) && !levelAccessor.getFluidState(blockPos).is(FluidTags.WATER) || !levelAccessor.getFluidState(blockPos.below()).is(FluidTags.WATER));
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        Entity entity;
        if (this.level().isClientSide) {
            return false;
        }
        if (!this.isMoving() && !damageSource.is(DamageTypeTags.AVOIDS_GUARDIAN_THORNS) && !damageSource.is(DamageTypes.THORNS) && (entity = damageSource.getDirectEntity()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.hurt(this.damageSources().thorns(this), 2.0f);
        }
        if (this.randomStrollGoal != null) {
            this.randomStrollGoal.trigger();
        }
        return super.hurt(damageSource, f);
    }

    @Override
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isControlledByLocalInstance() && this.isInWater()) {
            this.moveRelative(0.1f, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (!this.isMoving() && this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(vec3);
        }
    }

    static class GuardianMoveControl
    extends MoveControl {
        private final Guardian guardian;

        public GuardianMoveControl(Guardian guardian) {
            super(guardian);
            this.guardian = guardian;
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO || this.guardian.getNavigation().isDone()) {
                this.guardian.setSpeed(0.0f);
                this.guardian.setMoving(false);
                return;
            }
            Vec3 vec3 = new Vec3(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
            double d = vec3.length();
            double d2 = vec3.x / d;
            double d3 = vec3.y / d;
            double d4 = vec3.z / d;
            float f = (float)(Mth.atan2(vec3.z, vec3.x) * 57.2957763671875) - 90.0f;
            this.guardian.setYRot(this.rotlerp(this.guardian.getYRot(), f, 90.0f));
            this.guardian.yBodyRot = this.guardian.getYRot();
            float f2 = (float)(this.speedModifier * this.guardian.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f3 = Mth.lerp(0.125f, this.guardian.getSpeed(), f2);
            this.guardian.setSpeed(f3);
            double d5 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.5) * 0.05;
            double d6 = Math.cos(this.guardian.getYRot() * ((float)Math.PI / 180));
            double d7 = Math.sin(this.guardian.getYRot() * ((float)Math.PI / 180));
            double d8 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.75) * 0.05;
            this.guardian.setDeltaMovement(this.guardian.getDeltaMovement().add(d5 * d6, d8 * (d7 + d6) * 0.25 + (double)f3 * d3 * 0.1, d5 * d7));
            LookControl lookControl = this.guardian.getLookControl();
            double d9 = this.guardian.getX() + d2 * 2.0;
            double d10 = this.guardian.getEyeY() + d3 / d;
            double d11 = this.guardian.getZ() + d4 * 2.0;
            double d12 = lookControl.getWantedX();
            double d13 = lookControl.getWantedY();
            double d14 = lookControl.getWantedZ();
            if (!lookControl.isLookingAtTarget()) {
                d12 = d9;
                d13 = d10;
                d14 = d11;
            }
            this.guardian.getLookControl().setLookAt(Mth.lerp(0.125, d12, d9), Mth.lerp(0.125, d13, d10), Mth.lerp(0.125, d14, d11), 10.0f, 40.0f);
            this.guardian.setMoving(true);
        }
    }

    static class GuardianAttackGoal
    extends Goal {
        private final Guardian guardian;
        private int attackTime;
        private final boolean elder;

        public GuardianAttackGoal(Guardian guardian) {
            this.guardian = guardian;
            this.elder = guardian instanceof ElderGuardian;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.guardian.getTarget();
            return livingEntity != null && livingEntity.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && (this.elder || this.guardian.getTarget() != null && this.guardian.distanceToSqr(this.guardian.getTarget()) > 9.0);
        }

        @Override
        public void start() {
            this.attackTime = -10;
            this.guardian.getNavigation().stop();
            LivingEntity livingEntity = this.guardian.getTarget();
            if (livingEntity != null) {
                this.guardian.getLookControl().setLookAt(livingEntity, 90.0f, 90.0f);
            }
            this.guardian.hasImpulse = true;
        }

        @Override
        public void stop() {
            this.guardian.setActiveAttackTarget(0);
            this.guardian.setTarget(null);
            this.guardian.randomStrollGoal.trigger();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.guardian.getTarget();
            if (livingEntity == null) {
                return;
            }
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(livingEntity, 90.0f, 90.0f);
            if (!this.guardian.hasLineOfSight(livingEntity)) {
                this.guardian.setTarget(null);
                return;
            }
            ++this.attackTime;
            if (this.attackTime == 0) {
                this.guardian.setActiveAttackTarget(livingEntity.getId());
                if (!this.guardian.isSilent()) {
                    this.guardian.level().broadcastEntityEvent(this.guardian, (byte)21);
                }
            } else if (this.attackTime >= this.guardian.getAttackDuration()) {
                float f = 1.0f;
                if (this.guardian.level().getDifficulty() == Difficulty.HARD) {
                    f += 2.0f;
                }
                if (this.elder) {
                    f += 2.0f;
                }
                livingEntity.hurt(this.guardian.damageSources().indirectMagic(this.guardian, this.guardian), f);
                this.guardian.doHurtTarget(livingEntity);
                this.guardian.setTarget(null);
            }
            super.tick();
        }
    }

    static class GuardianAttackSelector
    implements Predicate<LivingEntity> {
        private final Guardian guardian;

        public GuardianAttackSelector(Guardian guardian) {
            this.guardian = guardian;
        }

        @Override
        public boolean test(@Nullable LivingEntity livingEntity) {
            return (livingEntity instanceof Player || livingEntity instanceof Squid || livingEntity instanceof Axolotl) && livingEntity.distanceToSqr(this.guardian) > 9.0;
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((LivingEntity)object);
        }
    }
}

