/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class Phantom
extends FlyingMob
implements Enemy {
    public static final float FLAP_DEGREES_PER_TICK = 7.448451f;
    public static final int TICKS_PER_FLAP = Mth.ceil(24.166098f);
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Phantom.class, EntityDataSerializers.INT);
    Vec3 moveTargetPoint = Vec3.ZERO;
    BlockPos anchorPoint = BlockPos.ZERO;
    AttackPhase attackPhase = AttackPhase.CIRCLE;

    public Phantom(EntityType<? extends Phantom> entityType, Level level) {
        super((EntityType<? extends FlyingMob>)entityType, level);
        this.xpReward = 5;
        this.moveControl = new PhantomMoveControl(this);
        this.lookControl = new PhantomLookControl(this, this);
    }

    @Override
    public boolean isFlapping() {
        return (this.getUniqueFlapTickOffset() + this.tickCount) % TICKS_PER_FLAP == 0;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new PhantomBodyRotationControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PhantomAttackStrategyGoal());
        this.goalSelector.addGoal(2, new PhantomSweepAttackGoal());
        this.goalSelector.addGoal(3, new PhantomCircleAroundAnchorGoal());
        this.targetSelector.addGoal(1, new PhantomAttackPlayerTargetGoal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SIZE, 0);
    }

    public void setPhantomSize(int n) {
        this.entityData.set(ID_SIZE, Mth.clamp(n, 0, 64));
    }

    private void updatePhantomSizeInfo() {
        this.refreshDimensions();
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6 + this.getPhantomSize());
    }

    public int getPhantomSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ID_SIZE.equals(entityDataAccessor)) {
            this.updatePhantomSizeInfo();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public int getUniqueFlapTickOffset() {
        return this.getId() * 3;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            float f = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount) * 7.448451f * ((float)Math.PI / 180) + (float)Math.PI);
            float f2 = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount + 1) * 7.448451f * ((float)Math.PI / 180) + (float)Math.PI);
            if (f > 0.0f && f2 <= 0.0f) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, this.getSoundSource(), 0.95f + this.random.nextFloat() * 0.05f, 0.95f + this.random.nextFloat() * 0.05f, false);
            }
            float f3 = this.getBbWidth() * 1.48f;
            float f4 = Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * f3;
            float f5 = Mth.sin(this.getYRot() * ((float)Math.PI / 180)) * f3;
            float f6 = (0.3f + f * 0.45f) * this.getBbHeight() * 2.5f;
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() + (double)f4, this.getY() + (double)f6, this.getZ() + (double)f5, 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() - (double)f4, this.getY() + (double)f6, this.getZ() - (double)f5, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void aiStep() {
        if (this.isAlive() && this.isSunBurnTick()) {
            this.igniteForSeconds(8.0f);
        }
        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.anchorPoint = this.blockPosition().above(5);
        this.setPhantomSize(0);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("AX")) {
            this.anchorPoint = new BlockPos(compoundTag.getInt("AX"), compoundTag.getInt("AY"), compoundTag.getInt("AZ"));
        }
        this.setPhantomSize(compoundTag.getInt("Size"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("AX", this.anchorPoint.getX());
        compoundTag.putInt("AY", this.anchorPoint.getY());
        compoundTag.putInt("AZ", this.anchorPoint.getZ());
        compoundTag.putInt("Size", this.getPhantomSize());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return true;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        int n = this.getPhantomSize();
        EntityDimensions entityDimensions = super.getDefaultDimensions(pose);
        return entityDimensions.scale(1.0f + 0.15f * (float)n);
    }

    static enum AttackPhase {
        CIRCLE,
        SWOOP;

    }

    class PhantomMoveControl
    extends MoveControl {
        private float speed;

        public PhantomMoveControl(Mob mob) {
            super(mob);
            this.speed = 0.1f;
        }

        @Override
        public void tick() {
            if (Phantom.this.horizontalCollision) {
                Phantom.this.setYRot(Phantom.this.getYRot() + 180.0f);
                this.speed = 0.1f;
            }
            double d = Phantom.this.moveTargetPoint.x - Phantom.this.getX();
            double d2 = Phantom.this.moveTargetPoint.y - Phantom.this.getY();
            double d3 = Phantom.this.moveTargetPoint.z - Phantom.this.getZ();
            double d4 = Math.sqrt(d * d + d3 * d3);
            if (Math.abs(d4) > (double)1.0E-5f) {
                double d5 = 1.0 - Math.abs(d2 * (double)0.7f) / d4;
                d4 = Math.sqrt((d *= d5) * d + (d3 *= d5) * d3);
                double d6 = Math.sqrt(d * d + d3 * d3 + d2 * d2);
                float f = Phantom.this.getYRot();
                float f2 = (float)Mth.atan2(d3, d);
                float f3 = Mth.wrapDegrees(Phantom.this.getYRot() + 90.0f);
                float f4 = Mth.wrapDegrees(f2 * 57.295776f);
                Phantom.this.setYRot(Mth.approachDegrees(f3, f4, 4.0f) - 90.0f);
                Phantom.this.yBodyRot = Phantom.this.getYRot();
                this.speed = Mth.degreesDifferenceAbs(f, Phantom.this.getYRot()) < 3.0f ? Mth.approach(this.speed, 1.8f, 0.005f * (1.8f / this.speed)) : Mth.approach(this.speed, 0.2f, 0.025f);
                float f5 = (float)(-(Mth.atan2(-d2, d4) * 57.2957763671875));
                Phantom.this.setXRot(f5);
                float f6 = Phantom.this.getYRot() + 90.0f;
                double d7 = (double)(this.speed * Mth.cos(f6 * ((float)Math.PI / 180))) * Math.abs(d / d6);
                double d8 = (double)(this.speed * Mth.sin(f6 * ((float)Math.PI / 180))) * Math.abs(d3 / d6);
                double d9 = (double)(this.speed * Mth.sin(f5 * ((float)Math.PI / 180))) * Math.abs(d2 / d6);
                Vec3 vec3 = Phantom.this.getDeltaMovement();
                Phantom.this.setDeltaMovement(vec3.add(new Vec3(d7, d9, d8).subtract(vec3).scale(0.2)));
            }
        }
    }

    class PhantomLookControl
    extends LookControl {
        public PhantomLookControl(Phantom phantom, Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }

    class PhantomBodyRotationControl
    extends BodyRotationControl {
        public PhantomBodyRotationControl(Mob mob) {
            super(mob);
        }

        @Override
        public void clientTick() {
            Phantom.this.yHeadRot = Phantom.this.yBodyRot;
            Phantom.this.yBodyRot = Phantom.this.getYRot();
        }
    }

    class PhantomAttackStrategyGoal
    extends Goal {
        private int nextSweepTick;

        PhantomAttackStrategyGoal() {
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity != null) {
                return Phantom.this.canAttack(livingEntity, TargetingConditions.DEFAULT);
            }
            return false;
        }

        @Override
        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            Phantom.this.attackPhase = AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        @Override
        public void stop() {
            Phantom.this.anchorPoint = Phantom.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, Phantom.this.anchorPoint).above(10 + Phantom.this.random.nextInt(20));
        }

        @Override
        public void tick() {
            if (Phantom.this.attackPhase == AttackPhase.CIRCLE) {
                --this.nextSweepTick;
                if (this.nextSweepTick <= 0) {
                    Phantom.this.attackPhase = AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((8 + Phantom.this.random.nextInt(4)) * 20);
                    Phantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0f, 0.95f + Phantom.this.random.nextFloat() * 0.1f);
                }
            }
        }

        private void setAnchorAboveTarget() {
            Phantom.this.anchorPoint = Phantom.this.getTarget().blockPosition().above(20 + Phantom.this.random.nextInt(20));
            if (Phantom.this.anchorPoint.getY() < Phantom.this.level().getSeaLevel()) {
                Phantom.this.anchorPoint = new BlockPos(Phantom.this.anchorPoint.getX(), Phantom.this.level().getSeaLevel() + 1, Phantom.this.anchorPoint.getZ());
            }
        }
    }

    class PhantomSweepAttackGoal
    extends PhantomMoveTargetGoal {
        private static final int CAT_SEARCH_TICK_DELAY = 20;
        private boolean isScaredOfCat;
        private int catSearchTick;

        PhantomSweepAttackGoal() {
        }

        @Override
        public boolean canUse() {
            return Phantom.this.getTarget() != null && Phantom.this.attackPhase == AttackPhase.SWOOP;
        }

        @Override
        public boolean canContinueToUse() {
            Object object;
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity == null) {
                return false;
            }
            if (!livingEntity.isAlive()) {
                return false;
            }
            if (livingEntity instanceof Player) {
                object = (Player)livingEntity;
                if (livingEntity.isSpectator() || ((Player)object).isCreative()) {
                    return false;
                }
            }
            if (!this.canUse()) {
                return false;
            }
            if (Phantom.this.tickCount > this.catSearchTick) {
                this.catSearchTick = Phantom.this.tickCount + 20;
                object = Phantom.this.level().getEntitiesOfClass(Cat.class, Phantom.this.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);
                Iterator iterator = object.iterator();
                while (iterator.hasNext()) {
                    Cat cat = (Cat)iterator.next();
                    cat.hiss();
                }
                this.isScaredOfCat = !object.isEmpty();
            }
            return !this.isScaredOfCat;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
            Phantom.this.setTarget(null);
            Phantom.this.attackPhase = AttackPhase.CIRCLE;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            Phantom.this.moveTargetPoint = new Vec3(livingEntity.getX(), livingEntity.getY(0.5), livingEntity.getZ());
            if (Phantom.this.getBoundingBox().inflate(0.2f).intersects(livingEntity.getBoundingBox())) {
                Phantom.this.doHurtTarget(livingEntity);
                Phantom.this.attackPhase = AttackPhase.CIRCLE;
                if (!Phantom.this.isSilent()) {
                    Phantom.this.level().levelEvent(1039, Phantom.this.blockPosition(), 0);
                }
            } else if (Phantom.this.horizontalCollision || Phantom.this.hurtTime > 0) {
                Phantom.this.attackPhase = AttackPhase.CIRCLE;
            }
        }
    }

    class PhantomCircleAroundAnchorGoal
    extends PhantomMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        PhantomCircleAroundAnchorGoal() {
        }

        @Override
        public boolean canUse() {
            return Phantom.this.getTarget() == null || Phantom.this.attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void start() {
            this.distance = 5.0f + Phantom.this.random.nextFloat() * 10.0f;
            this.height = -4.0f + Phantom.this.random.nextFloat() * 9.0f;
            this.clockwise = Phantom.this.random.nextBoolean() ? 1.0f : -1.0f;
            this.selectNext();
        }

        @Override
        public void tick() {
            if (Phantom.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0f + Phantom.this.random.nextFloat() * 9.0f;
            }
            if (Phantom.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                this.distance += 1.0f;
                if (this.distance > 15.0f) {
                    this.distance = 5.0f;
                    this.clockwise = -this.clockwise;
                }
            }
            if (Phantom.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = Phantom.this.random.nextFloat() * 2.0f * (float)Math.PI;
                this.selectNext();
            }
            if (this.touchingTarget()) {
                this.selectNext();
            }
            if (Phantom.this.moveTargetPoint.y < Phantom.this.getY() && !Phantom.this.level().isEmptyBlock(Phantom.this.blockPosition().below(1))) {
                this.height = Math.max(1.0f, this.height);
                this.selectNext();
            }
            if (Phantom.this.moveTargetPoint.y > Phantom.this.getY() && !Phantom.this.level().isEmptyBlock(Phantom.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0f, this.height);
                this.selectNext();
            }
        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(Phantom.this.anchorPoint)) {
                Phantom.this.anchorPoint = Phantom.this.blockPosition();
            }
            this.angle += this.clockwise * 15.0f * ((float)Math.PI / 180);
            Phantom.this.moveTargetPoint = Vec3.atLowerCornerOf(Phantom.this.anchorPoint).add(this.distance * Mth.cos(this.angle), -4.0f + this.height, this.distance * Mth.sin(this.angle));
        }
    }

    class PhantomAttackPlayerTargetGoal
    extends Goal {
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);
        private int nextScanTick = PhantomAttackPlayerTargetGoal.reducedTickDelay(20);

        PhantomAttackPlayerTargetGoal() {
        }

        @Override
        public boolean canUse() {
            if (this.nextScanTick > 0) {
                --this.nextScanTick;
                return false;
            }
            this.nextScanTick = PhantomAttackPlayerTargetGoal.reducedTickDelay(60);
            List<Player> list = Phantom.this.level().getNearbyPlayers(this.attackTargeting, Phantom.this, Phantom.this.getBoundingBox().inflate(16.0, 64.0, 16.0));
            if (!list.isEmpty()) {
                list.sort(Comparator.comparing(Entity::getY).reversed());
                for (Player player : list) {
                    if (!Phantom.this.canAttack(player, TargetingConditions.DEFAULT)) continue;
                    Phantom.this.setTarget(player);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity != null) {
                return Phantom.this.canAttack(livingEntity, TargetingConditions.DEFAULT);
            }
            return false;
        }
    }

    abstract class PhantomMoveTargetGoal
    extends Goal {
        public PhantomMoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return Phantom.this.moveTargetPoint.distanceToSqr(Phantom.this.getX(), Phantom.this.getY(), Phantom.this.getZ()) < 4.0;
        }
    }
}

