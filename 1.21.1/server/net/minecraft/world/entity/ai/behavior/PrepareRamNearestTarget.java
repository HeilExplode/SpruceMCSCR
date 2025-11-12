/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class PrepareRamNearestTarget<E extends PathfinderMob>
extends Behavior<E> {
    public static final int TIME_OUT_DURATION = 160;
    private final ToIntFunction<E> getCooldownOnFail;
    private final int minRamDistance;
    private final int maxRamDistance;
    private final float walkSpeed;
    private final TargetingConditions ramTargeting;
    private final int ramPrepareTime;
    private final Function<E, SoundEvent> getPrepareRamSound;
    private Optional<Long> reachedRamPositionTimestamp = Optional.empty();
    private Optional<RamCandidate> ramCandidate = Optional.empty();

    public PrepareRamNearestTarget(ToIntFunction<E> toIntFunction, int n, int n2, float f, TargetingConditions targetingConditions, int n3, Function<E, SoundEvent> function) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.RAM_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.RAM_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 160);
        this.getCooldownOnFail = toIntFunction;
        this.minRamDistance = n;
        this.maxRamDistance = n2;
        this.walkSpeed = f;
        this.ramTargeting = targetingConditions;
        this.ramPrepareTime = n3;
        this.getPrepareRamSound = function;
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        Brain<?> brain = pathfinderMob.getBrain();
        brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap(nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findClosest(livingEntity -> this.ramTargeting.test(pathfinderMob, (LivingEntity)livingEntity))).ifPresent(livingEntity -> this.chooseRamPosition(pathfinderMob, (LivingEntity)livingEntity));
    }

    @Override
    protected void stop(ServerLevel serverLevel, E e, long l) {
        Brain<Vec3> brain = ((LivingEntity)e).getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
            serverLevel.broadcastEntityEvent((Entity)e, (byte)59);
            brain.setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getCooldownOnFail.applyAsInt(e));
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return this.ramCandidate.isPresent() && this.ramCandidate.get().getTarget().isAlive();
    }

    @Override
    protected void tick(ServerLevel serverLevel, E e, long l) {
        boolean bl;
        if (this.ramCandidate.isEmpty()) {
            return;
        }
        ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.ramCandidate.get().getStartPosition(), this.walkSpeed, 0));
        ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.ramCandidate.get().getTarget(), true));
        boolean bl2 = bl = !this.ramCandidate.get().getTarget().blockPosition().equals(this.ramCandidate.get().getTargetPosition());
        if (bl) {
            serverLevel.broadcastEntityEvent((Entity)e, (byte)59);
            ((Mob)e).getNavigation().stop();
            this.chooseRamPosition((PathfinderMob)e, this.ramCandidate.get().target);
        } else {
            BlockPos blockPos = ((Entity)e).blockPosition();
            if (blockPos.equals(this.ramCandidate.get().getStartPosition())) {
                serverLevel.broadcastEntityEvent((Entity)e, (byte)58);
                if (this.reachedRamPositionTimestamp.isEmpty()) {
                    this.reachedRamPositionTimestamp = Optional.of(l);
                }
                if (l - this.reachedRamPositionTimestamp.get() >= (long)this.ramPrepareTime) {
                    ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.RAM_TARGET, this.getEdgeOfBlock(blockPos, this.ramCandidate.get().getTargetPosition()));
                    serverLevel.playSound(null, (Entity)e, this.getPrepareRamSound.apply(e), SoundSource.NEUTRAL, 1.0f, ((LivingEntity)e).getVoicePitch());
                    this.ramCandidate = Optional.empty();
                }
            }
        }
    }

    private Vec3 getEdgeOfBlock(BlockPos blockPos, BlockPos blockPos2) {
        double d = 0.5;
        double d2 = 0.5 * (double)Mth.sign(blockPos2.getX() - blockPos.getX());
        double d3 = 0.5 * (double)Mth.sign(blockPos2.getZ() - blockPos.getZ());
        return Vec3.atBottomCenterOf(blockPos2).add(d2, 0.0, d3);
    }

    private Optional<BlockPos> calculateRammingStartPosition(PathfinderMob pathfinderMob, LivingEntity livingEntity) {
        BlockPos blockPos2 = livingEntity.blockPosition();
        if (!this.isWalkableBlock(pathfinderMob, blockPos2)) {
            return Optional.empty();
        }
        ArrayList arrayList = Lists.newArrayList();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            mutableBlockPos.set(blockPos2);
            for (int i = 0; i < this.maxRamDistance; ++i) {
                if (this.isWalkableBlock(pathfinderMob, mutableBlockPos.move(direction))) continue;
                mutableBlockPos.move(direction.getOpposite());
                break;
            }
            if (mutableBlockPos.distManhattan(blockPos2) < this.minRamDistance) continue;
            arrayList.add(mutableBlockPos.immutable());
        }
        PathNavigation pathNavigation = pathfinderMob.getNavigation();
        return arrayList.stream().sorted(Comparator.comparingDouble(pathfinderMob.blockPosition()::distSqr)).filter(blockPos -> {
            Path path = pathNavigation.createPath((BlockPos)blockPos, 0);
            return path != null && path.canReach();
        }).findFirst();
    }

    private boolean isWalkableBlock(PathfinderMob pathfinderMob, BlockPos blockPos) {
        return pathfinderMob.getNavigation().isStableDestination(blockPos) && pathfinderMob.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic(pathfinderMob, blockPos)) == 0.0f;
    }

    private void chooseRamPosition(PathfinderMob pathfinderMob, LivingEntity livingEntity) {
        this.reachedRamPositionTimestamp = Optional.empty();
        this.ramCandidate = this.calculateRammingStartPosition(pathfinderMob, livingEntity).map(blockPos -> new RamCandidate((BlockPos)blockPos, livingEntity.blockPosition(), livingEntity));
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }

    public static class RamCandidate {
        private final BlockPos startPosition;
        private final BlockPos targetPosition;
        final LivingEntity target;

        public RamCandidate(BlockPos blockPos, BlockPos blockPos2, LivingEntity livingEntity) {
            this.startPosition = blockPos;
            this.targetPosition = blockPos2;
            this.target = livingEntity;
        }

        public BlockPos getStartPosition() {
            return this.startPosition;
        }

        public BlockPos getTargetPosition() {
            return this.targetPosition;
        }

        public LivingEntity getTarget() {
            return this.target;
        }
    }
}

