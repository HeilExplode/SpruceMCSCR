/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WitherBoss
extends Monster
implements PowerableMob,
RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
    private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final int INVULNERABLE_TICKS = 220;
    private final float[] xRotHeads = new float[2];
    private final float[] yRotHeads = new float[2];
    private final float[] xRotOHeads = new float[2];
    private final float[] yRotOHeads = new float[2];
    private final int[] nextHeadUpdate = new int[2];
    private final int[] idleHeadUpdates = new int[2];
    private int destroyBlocksTick;
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
    private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = livingEntity -> !livingEntity.getType().is(EntityTypeTags.WITHER_FRIENDS) && livingEntity.attackable();
    private static final TargetingConditions TARGETING_CONDITIONS = TargetingConditions.forCombat().range(20.0).selector(LIVING_ENTITY_SELECTOR);

    public WitherBoss(EntityType<? extends WitherBoss> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setHealth(this.getMaxHealth());
        this.xpReward = 50;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        flyingPathNavigation.setCanPassDoors(true);
        return flyingPathNavigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WitherDoNothingGoal());
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TARGET_A, 0);
        builder.define(DATA_TARGET_B, 0);
        builder.define(DATA_TARGET_C, 0);
        builder.define(DATA_ID_INV, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Invul", this.getInvulnerableTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setInvulnerableTicks(compoundTag.getInt("Invul"));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public void aiStep() {
        int n;
        Entity entity;
        Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.6, 1.0);
        if (!this.level().isClientSide && this.getAlternativeTarget(0) > 0 && (entity = this.level().getEntity(this.getAlternativeTarget(0))) != null) {
            double d = vec3.y;
            if (this.getY() < entity.getY() || !this.isPowered() && this.getY() < entity.getY() + 5.0) {
                d = Math.max(0.0, d);
                d += 0.3 - d * (double)0.6f;
            }
            vec3 = new Vec3(vec3.x, d, vec3.z);
            Vec3 vec32 = new Vec3(entity.getX() - this.getX(), 0.0, entity.getZ() - this.getZ());
            if (vec32.horizontalDistanceSqr() > 9.0) {
                Vec3 vec33 = vec32.normalize();
                vec3 = vec3.add(vec33.x * 0.3 - vec3.x * 0.6, 0.0, vec33.z * 0.3 - vec3.z * 0.6);
            }
        }
        this.setDeltaMovement(vec3);
        if (vec3.horizontalDistanceSqr() > 0.05) {
            this.setYRot((float)Mth.atan2(vec3.z, vec3.x) * 57.295776f - 90.0f);
        }
        super.aiStep();
        for (n = 0; n < 2; ++n) {
            this.yRotOHeads[n] = this.yRotHeads[n];
            this.xRotOHeads[n] = this.xRotHeads[n];
        }
        for (n = 0; n < 2; ++n) {
            int n2 = this.getAlternativeTarget(n + 1);
            Entity entity2 = null;
            if (n2 > 0) {
                entity2 = this.level().getEntity(n2);
            }
            if (entity2 != null) {
                double d = this.getHeadX(n + 1);
                double d2 = this.getHeadY(n + 1);
                double d3 = this.getHeadZ(n + 1);
                double d4 = entity2.getX() - d;
                double d5 = entity2.getEyeY() - d2;
                double d6 = entity2.getZ() - d3;
                double d7 = Math.sqrt(d4 * d4 + d6 * d6);
                float f = (float)(Mth.atan2(d6, d4) * 57.2957763671875) - 90.0f;
                float f2 = (float)(-(Mth.atan2(d5, d7) * 57.2957763671875));
                this.xRotHeads[n] = this.rotlerp(this.xRotHeads[n], f2, 40.0f);
                this.yRotHeads[n] = this.rotlerp(this.yRotHeads[n], f, 10.0f);
                continue;
            }
            this.yRotHeads[n] = this.rotlerp(this.yRotHeads[n], this.yBodyRot, 10.0f);
        }
        n = this.isPowered() ? 1 : 0;
        for (int i = 0; i < 3; ++i) {
            double d = this.getHeadX(i);
            double d8 = this.getHeadY(i);
            double d9 = this.getHeadZ(i);
            float f = 0.3f * this.getScale();
            this.level().addParticle(ParticleTypes.SMOKE, d + this.random.nextGaussian() * (double)f, d8 + this.random.nextGaussian() * (double)f, d9 + this.random.nextGaussian() * (double)f, 0.0, 0.0, 0.0);
            if (n == 0 || this.level().random.nextInt(4) != 0) continue;
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7f, 0.7f, 0.5f), d + this.random.nextGaussian() * (double)f, d8 + this.random.nextGaussian() * (double)f, d9 + this.random.nextGaussian() * (double)f, 0.0, 0.0, 0.0);
        }
        if (this.getInvulnerableTicks() > 0) {
            float f = 3.3f * this.getScale();
            for (int i = 0; i < 3; ++i) {
                this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7f, 0.7f, 0.9f), this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * f), this.getZ() + this.random.nextGaussian(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        int n;
        int n2;
        if (this.getInvulnerableTicks() > 0) {
            int n3 = this.getInvulnerableTicks() - 1;
            this.bossEvent.setProgress(1.0f - (float)n3 / 220.0f);
            if (n3 <= 0) {
                this.level().explode((Entity)this, this.getX(), this.getEyeY(), this.getZ(), 7.0f, false, Level.ExplosionInteraction.MOB);
                if (!this.isSilent()) {
                    this.level().globalLevelEvent(1023, this.blockPosition(), 0);
                }
            }
            this.setInvulnerableTicks(n3);
            if (this.tickCount % 10 == 0) {
                this.heal(10.0f);
            }
            return;
        }
        super.customServerAiStep();
        for (n2 = 1; n2 < 3; ++n2) {
            List<LivingEntity> list;
            if (this.tickCount < this.nextHeadUpdate[n2 - 1]) continue;
            this.nextHeadUpdate[n2 - 1] = this.tickCount + 10 + this.random.nextInt(10);
            if (this.level().getDifficulty() == Difficulty.NORMAL || this.level().getDifficulty() == Difficulty.HARD) {
                int n4 = n2 - 1;
                int n5 = this.idleHeadUpdates[n4];
                this.idleHeadUpdates[n4] = n5 + 1;
                if (n5 > 15) {
                    float f = 10.0f;
                    float f2 = 5.0f;
                    double d = Mth.nextDouble(this.random, this.getX() - 10.0, this.getX() + 10.0);
                    double d2 = Mth.nextDouble(this.random, this.getY() - 5.0, this.getY() + 5.0);
                    double d3 = Mth.nextDouble(this.random, this.getZ() - 10.0, this.getZ() + 10.0);
                    this.performRangedAttack(n2 + 1, d, d2, d3, true);
                    this.idleHeadUpdates[n2 - 1] = 0;
                }
            }
            if ((n = this.getAlternativeTarget(n2)) > 0) {
                list = (LivingEntity)this.level().getEntity(n);
                if (list == null || !this.canAttack((LivingEntity)((Object)list)) || this.distanceToSqr((Entity)((Object)list)) > 900.0 || !this.hasLineOfSight((Entity)((Object)list))) {
                    this.setAlternativeTarget(n2, 0);
                    continue;
                }
                this.performRangedAttack(n2 + 1, (LivingEntity)((Object)list));
                this.nextHeadUpdate[n2 - 1] = this.tickCount + 40 + this.random.nextInt(20);
                this.idleHeadUpdates[n2 - 1] = 0;
                continue;
            }
            list = this.level().getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0, 8.0, 20.0));
            if (list.isEmpty()) continue;
            LivingEntity livingEntity = (LivingEntity)list.get(this.random.nextInt(list.size()));
            this.setAlternativeTarget(n2, livingEntity.getId());
        }
        if (this.getTarget() != null) {
            this.setAlternativeTarget(0, this.getTarget().getId());
        } else {
            this.setAlternativeTarget(0, 0);
        }
        if (this.destroyBlocksTick > 0) {
            --this.destroyBlocksTick;
            if (this.destroyBlocksTick == 0 && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                n2 = 0;
                n = Mth.floor(this.getBbWidth() / 2.0f + 1.0f);
                int n6 = Mth.floor(this.getBbHeight());
                for (BlockPos blockPos : BlockPos.betweenClosed(this.getBlockX() - n, this.getBlockY(), this.getBlockZ() - n, this.getBlockX() + n, this.getBlockY() + n6, this.getBlockZ() + n)) {
                    BlockState blockState = this.level().getBlockState(blockPos);
                    if (!WitherBoss.canDestroy(blockState)) continue;
                    n2 = this.level().destroyBlock(blockPos, true, this) || n2 != 0 ? 1 : 0;
                }
                if (n2 != 0) {
                    this.level().levelEvent(null, 1022, this.blockPosition(), 0);
                }
            }
        }
        if (this.tickCount % 20 == 0) {
            this.heal(1.0f);
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    public static boolean canDestroy(BlockState blockState) {
        return !blockState.isAir() && !blockState.is(BlockTags.WITHER_IMMUNE);
    }

    public void makeInvulnerable() {
        this.setInvulnerableTicks(220);
        this.bossEvent.setProgress(0.0f);
        this.setHealth(this.getMaxHealth() / 3.0f);
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossEvent.removePlayer(serverPlayer);
    }

    private double getHeadX(int n) {
        if (n <= 0) {
            return this.getX();
        }
        float f = (this.yBodyRot + (float)(180 * (n - 1))) * ((float)Math.PI / 180);
        float f2 = Mth.cos(f);
        return this.getX() + (double)f2 * 1.3 * (double)this.getScale();
    }

    private double getHeadY(int n) {
        float f = n <= 0 ? 3.0f : 2.2f;
        return this.getY() + (double)(f * this.getScale());
    }

    private double getHeadZ(int n) {
        if (n <= 0) {
            return this.getZ();
        }
        float f = (this.yBodyRot + (float)(180 * (n - 1))) * ((float)Math.PI / 180);
        float f2 = Mth.sin(f);
        return this.getZ() + (double)f2 * 1.3 * (double)this.getScale();
    }

    private float rotlerp(float f, float f2, float f3) {
        float f4 = Mth.wrapDegrees(f2 - f);
        if (f4 > f3) {
            f4 = f3;
        }
        if (f4 < -f3) {
            f4 = -f3;
        }
        return f + f4;
    }

    private void performRangedAttack(int n, LivingEntity livingEntity) {
        this.performRangedAttack(n, livingEntity.getX(), livingEntity.getY() + (double)livingEntity.getEyeHeight() * 0.5, livingEntity.getZ(), n == 0 && this.random.nextFloat() < 0.001f);
    }

    private void performRangedAttack(int n, double d, double d2, double d3, boolean bl) {
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1024, this.blockPosition(), 0);
        }
        double d4 = this.getHeadX(n);
        double d5 = this.getHeadY(n);
        double d6 = this.getHeadZ(n);
        double d7 = d - d4;
        double d8 = d2 - d5;
        double d9 = d3 - d6;
        Vec3 vec3 = new Vec3(d7, d8, d9);
        WitherSkull witherSkull = new WitherSkull(this.level(), this, vec3.normalize());
        witherSkull.setOwner(this);
        if (bl) {
            witherSkull.setDangerous(true);
        }
        witherSkull.setPosRaw(d4, d5, d6);
        this.level().addFreshEntity(witherSkull);
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        this.performRangedAttack(0, livingEntity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        Entity entity;
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (damageSource.is(DamageTypeTags.WITHER_IMMUNE_TO) || damageSource.getEntity() instanceof WitherBoss) {
            return false;
        }
        if (this.getInvulnerableTicks() > 0 && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (this.isPowered() && ((entity = damageSource.getDirectEntity()) instanceof AbstractArrow || entity instanceof WindCharge)) {
            return false;
        }
        entity = damageSource.getEntity();
        if (entity != null && entity.getType().is(EntityTypeTags.WITHER_FRIENDS)) {
            return false;
        }
        if (this.destroyBlocksTick <= 0) {
            this.destroyBlocksTick = 20;
        }
        int n = 0;
        while (n < this.idleHeadUpdates.length) {
            int n2 = n++;
            this.idleHeadUpdates[n2] = this.idleHeadUpdates[n2] + 3;
        }
        return super.hurt(damageSource, f);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        super.dropCustomDeathLoot(serverLevel, damageSource, bl);
        ItemEntity itemEntity = this.spawnAtLocation(Items.NETHER_STAR);
        if (itemEntity != null) {
            itemEntity.setExtendedLifetime();
        }
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
            return;
        }
        this.noActionTime = 0;
    }

    @Override
    public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.6f).add(Attributes.FLYING_SPEED, 0.6f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 4.0);
    }

    public float getHeadYRot(int n) {
        return this.yRotHeads[n];
    }

    public float getHeadXRot(int n) {
        return this.xRotHeads[n];
    }

    public int getInvulnerableTicks() {
        return this.entityData.get(DATA_ID_INV);
    }

    public void setInvulnerableTicks(int n) {
        this.entityData.set(DATA_ID_INV, n);
    }

    public int getAlternativeTarget(int n) {
        return this.entityData.get(DATA_TARGETS.get(n));
    }

    public void setAlternativeTarget(int n, int n2) {
        this.entityData.set(DATA_TARGETS.get(n), n2);
    }

    @Override
    public boolean isPowered() {
        return this.getHealth() <= this.getMaxHealth() / 2.0f;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }

    class WitherDoNothingGoal
    extends Goal {
        public WitherDoNothingGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return WitherBoss.this.getInvulnerableTicks() > 0;
        }
    }
}

