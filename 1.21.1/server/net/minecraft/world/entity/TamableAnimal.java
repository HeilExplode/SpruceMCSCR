/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.scores.PlayerTeam;

public abstract class TamableAnimal
extends Animal
implements OwnableEntity {
    public static final int TELEPORT_WHEN_DISTANCE_IS_SQ = 144;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 1;
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID);
    private boolean orderedToSit;

    protected TamableAnimal(EntityType<? extends TamableAnimal> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
        builder.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.getOwnerUUID() != null) {
            compoundTag.putUUID("Owner", this.getOwnerUUID());
        }
        compoundTag.putBoolean("Sitting", this.orderedToSit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        UUID uUID;
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.hasUUID("Owner")) {
            uUID = compoundTag.getUUID("Owner");
        } else {
            String string = compoundTag.getString("Owner");
            uUID = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), string);
        }
        if (uUID != null) {
            try {
                this.setOwnerUUID(uUID);
                this.setTame(true, false);
            }
            catch (Throwable throwable) {
                this.setTame(false, true);
            }
        }
        this.orderedToSit = compoundTag.getBoolean("Sitting");
        this.setInSittingPose(this.orderedToSit);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public boolean handleLeashAtDistance(Entity entity, float f) {
        if (this.isInSittingPose()) {
            if (f > 10.0f) {
                this.dropLeash(true, true);
            }
            return false;
        }
        return super.handleLeashAtDistance(entity, f);
    }

    protected void spawnTamingParticles(boolean bl) {
        SimpleParticleType simpleParticleType = ParticleTypes.HEART;
        if (!bl) {
            simpleParticleType = ParticleTypes.SMOKE;
        }
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            double d3 = this.random.nextGaussian() * 0.02;
            ((Level)this.level()).addParticle(simpleParticleType, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, d2, d3);
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 7) {
            this.spawnTamingParticles(true);
        } else if (by == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(by);
        }
    }

    public boolean isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    public void setTame(boolean bl, boolean bl2) {
        byte by = this.entityData.get(DATA_FLAGS_ID);
        if (bl) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(by | 4));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(by & 0xFFFFFFFB));
        }
        if (bl2) {
            this.applyTamingSideEffects();
        }
    }

    protected void applyTamingSideEffects() {
    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean bl) {
        byte by = this.entityData.get(DATA_FLAGS_ID);
        if (bl) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(by | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(by & 0xFFFFFFFE));
        }
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uUID) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uUID));
    }

    public void tame(Player player) {
        this.setTame(true, true);
        this.setOwnerUUID(player.getUUID());
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
        }
    }

    @Override
    public boolean canAttack(LivingEntity livingEntity) {
        if (this.isOwnedBy(livingEntity)) {
            return false;
        }
        return super.canAttack(livingEntity);
    }

    public boolean isOwnedBy(LivingEntity livingEntity) {
        return livingEntity == this.getOwner();
    }

    public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
        return true;
    }

    @Override
    public PlayerTeam getTeam() {
        LivingEntity livingEntity;
        if (this.isTame() && (livingEntity = this.getOwner()) != null) {
            return livingEntity.getTeam();
        }
        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (this.isTame()) {
            LivingEntity livingEntity = this.getOwner();
            if (entity == livingEntity) {
                return true;
            }
            if (livingEntity != null) {
                return livingEntity.isAlliedTo(entity);
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!((Level)this.level()).isClientSide && ((Level)this.level()).getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
            this.getOwner().sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }
        super.die(damageSource);
    }

    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean bl) {
        this.orderedToSit = bl;
    }

    public void tryToTeleportToOwner() {
        LivingEntity livingEntity = this.getOwner();
        if (livingEntity != null) {
            this.teleportToAroundBlockPos(livingEntity.blockPosition());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingEntity = this.getOwner();
        return livingEntity != null && this.distanceToSqr(this.getOwner()) >= 144.0;
    }

    private void teleportToAroundBlockPos(BlockPos blockPos) {
        for (int i = 0; i < 10; ++i) {
            int n = this.random.nextIntBetweenInclusive(-3, 3);
            int n2 = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(n) < 2 && Math.abs(n2) < 2) continue;
            int n3 = this.random.nextIntBetweenInclusive(-1, 1);
            if (!this.maybeTeleportTo(blockPos.getX() + n, blockPos.getY() + n3, blockPos.getZ() + n2)) continue;
            return;
        }
    }

    private boolean maybeTeleportTo(int n, int n2, int n3) {
        if (!this.canTeleportTo(new BlockPos(n, n2, n3))) {
            return false;
        }
        this.moveTo((double)n + 0.5, n2, (double)n3 + 0.5, this.getYRot(), this.getXRot());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos blockPos) {
        PathType pathType = WalkNodeEvaluator.getPathTypeStatic(this, blockPos);
        if (pathType != PathType.WALKABLE) {
            return false;
        }
        BlockState blockState = ((Level)this.level()).getBlockState(blockPos.below());
        if (!this.canFlyToOwner() && blockState.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos blockPos2 = blockPos.subtract(this.blockPosition());
        return this.level().noCollision(this, this.getBoundingBox().move(blockPos2));
    }

    public final boolean unableToMoveToOwner() {
        return this.isOrderedToSit() || this.isPassenger() || this.mayBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canFlyToOwner() {
        return false;
    }

    @Override
    public /* synthetic */ EntityGetter level() {
        return super.level();
    }

    public class TamableAnimalPanicGoal
    extends PanicGoal {
        public TamableAnimalPanicGoal(double d, TagKey<DamageType> tagKey) {
            super((PathfinderMob)TamableAnimal.this, d, tagKey);
        }

        public TamableAnimalPanicGoal(double d) {
            super(TamableAnimal.this, d);
        }

        @Override
        public void tick() {
            if (!TamableAnimal.this.unableToMoveToOwner() && TamableAnimal.this.shouldTryTeleportToOwner()) {
                TamableAnimal.this.tryToTeleportToOwner();
            }
            super.tick();
        }
    }
}

