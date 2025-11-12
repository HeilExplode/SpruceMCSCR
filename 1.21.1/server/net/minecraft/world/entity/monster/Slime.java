/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;

public class Slime
extends Mob
implements Enemy {
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 127;
    public static final int MAX_NATURAL_SIZE = 4;
    public float targetSquish;
    public float squish;
    public float oSquish;
    private boolean wasOnGround;

    public Slime(EntityType<? extends Slime> entityType, Level level) {
        super((EntityType<? extends Mob>)entityType, level);
        this.fixupDimensions();
        this.moveControl = new SlimeMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new SlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new SlimeKeepOnJumpingGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, livingEntity -> Math.abs(livingEntity.getY() - this.getY()) <= 4.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SIZE, 1);
    }

    @VisibleForTesting
    public void setSize(int n, boolean bl) {
        int n2 = Mth.clamp(n, 1, 127);
        this.entityData.set(ID_SIZE, n2);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(n2 * n2);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2f + 0.1f * (float)n2);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(n2);
        if (bl) {
            this.setHealth(this.getMaxHealth());
        }
        this.xpReward = n2;
    }

    public int getSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Size", this.getSize() - 1);
        compoundTag.putBoolean("wasOnGround", this.wasOnGround);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.setSize(compoundTag.getInt("Size") + 1, false);
        super.readAdditionalSaveData(compoundTag);
        this.wasOnGround = compoundTag.getBoolean("wasOnGround");
    }

    public boolean isTiny() {
        return this.getSize() <= 1;
    }

    protected ParticleOptions getParticleType() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return this.getSize() > 0;
    }

    @Override
    public void tick() {
        this.squish += (this.targetSquish - this.squish) * 0.5f;
        this.oSquish = this.squish;
        super.tick();
        if (this.onGround() && !this.wasOnGround) {
            float f = this.getDimensions(this.getPose()).width() * 2.0f;
            float f2 = f / 2.0f;
            int n = 0;
            while ((float)n < f * 16.0f) {
                float f3 = this.random.nextFloat() * ((float)Math.PI * 2);
                float f4 = this.random.nextFloat() * 0.5f + 0.5f;
                float f5 = Mth.sin(f3) * f2 * f4;
                float f6 = Mth.cos(f3) * f2 * f4;
                this.level().addParticle(this.getParticleType(), this.getX() + (double)f5, this.getY(), this.getZ() + (double)f6, 0.0, 0.0, 0.0);
                ++n;
            }
            this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) / 0.8f);
            this.targetSquish = -0.5f;
        } else if (!this.onGround() && this.wasOnGround) {
            this.targetSquish = 1.0f;
        }
        this.wasOnGround = this.onGround();
        this.decreaseSquish();
    }

    protected void decreaseSquish() {
        this.targetSquish *= 0.6f;
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        super.refreshDimensions();
        this.setPos(d, d2, d3);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ID_SIZE.equals(entityDataAccessor)) {
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.yBodyRot = this.yHeadRot;
            if (this.isInWater() && this.random.nextInt(20) == 0) {
                this.doWaterSplashEffect();
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public EntityType<? extends Slime> getType() {
        return super.getType();
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        int n = this.getSize();
        if (!this.level().isClientSide && n > 1 && this.isDeadOrDying()) {
            Component component = this.getCustomName();
            boolean bl = this.isNoAi();
            float f = this.getDimensions(this.getPose()).width();
            float f2 = f / 2.0f;
            int n2 = n / 2;
            int n3 = 2 + this.random.nextInt(3);
            for (int i = 0; i < n3; ++i) {
                float f3 = ((float)(i % 2) - 0.5f) * f2;
                float f4 = ((float)(i / 2) - 0.5f) * f2;
                Slime slime = this.getType().create(this.level());
                if (slime == null) continue;
                if (this.isPersistenceRequired()) {
                    slime.setPersistenceRequired();
                }
                slime.setCustomName(component);
                slime.setNoAi(bl);
                slime.setInvulnerable(this.isInvulnerable());
                slime.setSize(n2, true);
                slime.moveTo(this.getX() + (double)f3, this.getY() + 0.5, this.getZ() + (double)f4, this.random.nextFloat() * 360.0f, 0.0f);
                this.level().addFreshEntity(slime);
            }
        }
        super.remove(removalReason);
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        if (entity instanceof IronGolem && this.isDealsDamage()) {
            this.dealDamage((LivingEntity)entity);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (this.isDealsDamage()) {
            this.dealDamage(player);
        }
    }

    protected void dealDamage(LivingEntity livingEntity) {
        DamageSource damageSource;
        if (this.isAlive() && this.isWithinMeleeAttackRange(livingEntity) && this.hasLineOfSight(livingEntity) && livingEntity.hurt(damageSource = this.damageSources().mobAttack(this), this.getAttackDamage())) {
            this.playSound(SoundEvents.SLIME_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            Level level = this.level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
            }
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        return new Vec3(0.0, (double)entityDimensions.height() - 0.015625 * (double)this.getSize() * (double)f, 0.0);
    }

    protected boolean isDealsDamage() {
        return !this.isTiny() && this.isEffectiveAi();
    }

    protected float getAttackDamage() {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.isTiny()) {
            return SoundEvents.SLIME_HURT_SMALL;
        }
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isTiny()) {
            return SoundEvents.SLIME_DEATH_SMALL;
        }
        return SoundEvents.SLIME_DEATH;
    }

    protected SoundEvent getSquishSound() {
        if (this.isTiny()) {
            return SoundEvents.SLIME_SQUISH_SMALL;
        }
        return SoundEvents.SLIME_SQUISH;
    }

    public static boolean checkSlimeSpawnRules(EntityType<Slime> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        if (MobSpawnType.isSpawner(mobSpawnType)) {
            return Slime.checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
        }
        if (levelAccessor.getDifficulty() != Difficulty.PEACEFUL) {
            boolean bl;
            if (mobSpawnType == MobSpawnType.SPAWNER) {
                return Slime.checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
            }
            if (levelAccessor.getBiome(blockPos).is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS) && blockPos.getY() > 50 && blockPos.getY() < 70 && randomSource.nextFloat() < 0.5f && randomSource.nextFloat() < levelAccessor.getMoonBrightness() && levelAccessor.getMaxLocalRawBrightness(blockPos) <= randomSource.nextInt(8)) {
                return Slime.checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
            }
            if (!(levelAccessor instanceof WorldGenLevel)) {
                return false;
            }
            ChunkPos chunkPos = new ChunkPos(blockPos);
            boolean bl2 = bl = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, ((WorldGenLevel)levelAccessor).getSeed(), 987234911L).nextInt(10) == 0;
            if (randomSource.nextInt(10) == 0 && bl && blockPos.getY() < 40) {
                return Slime.checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
            }
        }
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f * (float)this.getSize();
    }

    @Override
    public int getMaxHeadXRot() {
        return 0;
    }

    protected boolean doPlayJumpSound() {
        return this.getSize() > 0;
    }

    @Override
    public void jumpFromGround() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, this.getJumpPower(), vec3.z);
        this.hasImpulse = true;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        int n = randomSource.nextInt(3);
        if (n < 2 && randomSource.nextFloat() < 0.5f * difficultyInstance.getSpecialMultiplier()) {
            ++n;
        }
        int n2 = 1 << n;
        this.setSize(n2, true);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    float getSoundPitch() {
        float f = this.isTiny() ? 1.4f : 0.8f;
        return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * f;
    }

    protected SoundEvent getJumpSound() {
        return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(this.getSize());
    }

    static class SlimeMoveControl
    extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final Slime slime;
        private boolean isAggressive;

        public SlimeMoveControl(Slime slime) {
            super(slime);
            this.slime = slime;
            this.yRot = 180.0f * slime.getYRot() / (float)Math.PI;
        }

        public void setDirection(float f, boolean bl) {
            this.yRot = f;
            this.isAggressive = bl;
        }

        public void setWantedMovement(double d) {
            this.speedModifier = d;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0f));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0f);
                return;
            }
            this.operation = MoveControl.Operation.WAIT;
            if (this.mob.onGround()) {
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.jumpDelay-- <= 0) {
                    this.jumpDelay = this.slime.getJumpDelay();
                    if (this.isAggressive) {
                        this.jumpDelay /= 3;
                    }
                    this.slime.getJumpControl().jump();
                    if (this.slime.doPlayJumpSound()) {
                        this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
                    }
                } else {
                    this.slime.xxa = 0.0f;
                    this.slime.zza = 0.0f;
                    this.mob.setSpeed(0.0f);
                }
            } else {
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }
        }
    }

    static class SlimeFloatGoal
    extends Goal {
        private final Slime slime;

        public SlimeFloatGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            slime.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            if (this.slime.getRandom().nextFloat() < 0.8f) {
                this.slime.getJumpControl().jump();
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setWantedMovement(1.2);
            }
        }
    }

    static class SlimeAttackGoal
    extends Goal {
        private final Slime slime;
        private int growTiredTimer;

        public SlimeAttackGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.slime.getTarget();
            if (livingEntity == null) {
                return false;
            }
            if (!this.slime.canAttack(livingEntity)) {
                return false;
            }
            return this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void start() {
            this.growTiredTimer = SlimeAttackGoal.reducedTickDelay(300);
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = this.slime.getTarget();
            if (livingEntity == null) {
                return false;
            }
            if (!this.slime.canAttack(livingEntity)) {
                return false;
            }
            return --this.growTiredTimer > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            LivingEntity livingEntity = this.slime.getTarget();
            if (livingEntity != null) {
                this.slime.lookAt(livingEntity, 10.0f, 10.0f);
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setDirection(this.slime.getYRot(), this.slime.isDealsDamage());
            }
        }
    }

    static class SlimeRandomDirectionGoal
    extends Goal {
        private final Slime slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION)) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
                this.chosenDegrees = this.slime.getRandom().nextInt(360);
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setDirection(this.chosenDegrees, false);
            }
        }
    }

    static class SlimeKeepOnJumpingGoal
    extends Goal {
        private final Slime slime;

        public SlimeKeepOnJumpingGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        @Override
        public void tick() {
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setWantedMovement(1.0);
            }
        }
    }
}

