/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic<E extends PathfinderMob>
extends Behavior<E> {
    private static final int PANIC_MIN_DURATION = 100;
    private static final int PANIC_MAX_DURATION = 120;
    private static final int PANIC_DISTANCE_HORIZONTAL = 5;
    private static final int PANIC_DISTANCE_VERTICAL = 4;
    private final float speedMultiplier;
    private final Function<PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes;

    public AnimalPanic(float f) {
        this(f, pathfinderMob -> DamageTypeTags.PANIC_CAUSES);
    }

    public AnimalPanic(float f, Function<PathfinderMob, TagKey<DamageType>> function) {
        super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED), 100, 120);
        this.speedMultiplier = f;
        this.panicCausingDamageTypes = function;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        return ((LivingEntity)e).getBrain().getMemory(MemoryModuleType.HURT_BY).map(damageSource -> damageSource.is(this.panicCausingDamageTypes.apply((PathfinderMob)e))).orElse(false) != false || ((LivingEntity)e).getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E e, long l) {
        return true;
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
        ((LivingEntity)e).getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void stop(ServerLevel serverLevel, E e, long l) {
        Brain<?> brain = ((LivingEntity)e).getBrain();
        brain.eraseMemory(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void tick(ServerLevel serverLevel, E e, long l) {
        Vec3 vec3;
        if (((Mob)e).getNavigation().isDone() && (vec3 = this.getPanicPos(e, serverLevel)) != null) {
            ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedMultiplier, 0));
        }
    }

    @Nullable
    private Vec3 getPanicPos(E e, ServerLevel serverLevel) {
        Optional<Vec3> optional;
        if (((Entity)e).isOnFire() && (optional = this.lookForWater(serverLevel, (Entity)e).map(Vec3::atBottomCenterOf)).isPresent()) {
            return optional.get();
        }
        return LandRandomPos.getPos(e, 5, 4);
    }

    private Optional<BlockPos> lookForWater(BlockGetter blockGetter, Entity entity) {
        BlockPos blockPos3 = entity.blockPosition();
        if (!blockGetter.getBlockState(blockPos3).getCollisionShape(blockGetter, blockPos3).isEmpty()) {
            return Optional.empty();
        }
        Predicate<BlockPos> predicate = Mth.ceil(entity.getBbWidth()) == 2 ? blockPos2 -> BlockPos.squareOutSouthEast(blockPos2).allMatch(blockPos -> blockGetter.getFluidState((BlockPos)blockPos).is(FluidTags.WATER)) : blockPos -> blockGetter.getFluidState((BlockPos)blockPos).is(FluidTags.WATER);
        return BlockPos.findClosestMatch(blockPos3, 5, 1, predicate);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }
}

