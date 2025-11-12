/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.Croak;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToPreferredBlock;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindLand;
import net.minecraft.world.entity.ai.behavior.TryFindLandNearWater;
import net.minecraft.world.entity.ai.behavior.TryLaySpawnOnWaterNearLand;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FrogAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_ON_LAND = 1.0f;
    private static final float SPEED_MULTIPLIER_IN_WATER = 0.75f;
    private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(100, 140);
    private static final int MAX_LONG_JUMP_HEIGHT = 2;
    private static final int MAX_LONG_JUMP_WIDTH = 4;
    private static final float MAX_JUMP_VELOCITY_MULTIPLIER = 3.5714288f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25f;

    protected static void initMemories(Frog frog, RandomSource randomSource) {
        frog.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, TIME_BETWEEN_LONG_JUMPS.sample(randomSource));
    }

    protected static Brain<?> makeBrain(Brain<Frog> brain) {
        FrogAi.initCoreActivity(brain);
        FrogAi.initIdleActivity(brain);
        FrogAi.initSwimActivity(brain);
        FrogAi.initLaySpawnActivity(brain);
        FrogAi.initTongueActivity(brain);
        FrogAi.initJumpActivity(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Frog> brain) {
        brain.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Frog>>)ImmutableList.of(new AnimalPanic(2.0f), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Frog> brain) {
        brain.addActivityWithConditions(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Frog>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)0, (Object)new AnimalMakeLove(EntityType.FROG)), (Object)Pair.of((Object)1, (Object)new FollowTemptation(livingEntity -> Float.valueOf(1.25f))), (Object)Pair.of((Object)2, StartAttacking.create(FrogAi::canAttack, frog -> frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))), (Object)Pair.of((Object)3, TryFindLand.create(6, 1.0f)), (Object)Pair.of((Object)4, new RunOne((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(RandomStroll.stroll(1.0f), (Object)1), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)1), (Object)Pair.of((Object)new Croak(), (Object)3), (Object)Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), (Object)2))))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_IN_WATER, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    private static void initSwimActivity(Brain<Frog> brain) {
        brain.addActivityWithConditions(Activity.SWIM, (ImmutableList<Pair<Integer, BehaviorControl<Frog>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)1, (Object)new FollowTemptation(livingEntity -> Float.valueOf(1.25f))), (Object)Pair.of((Object)2, StartAttacking.create(FrogAi::canAttack, frog -> frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))), (Object)Pair.of((Object)3, TryFindLand.create(8, 1.5f)), (Object)Pair.of((Object)5, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Set<MemoryModuleType<?>>)ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of((Object)Pair.of(RandomStroll.swim(0.75f), (Object)1), (Object)Pair.of(RandomStroll.stroll(1.0f, true), (Object)1), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)1), (Object)Pair.of(BehaviorBuilder.triggerIf(Entity::isInWaterOrBubble), (Object)5))))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_IN_WATER, (Object)((Object)MemoryStatus.VALUE_PRESENT))));
    }

    private static void initLaySpawnActivity(Brain<Frog> brain) {
        brain.addActivityWithConditions(Activity.LAY_SPAWN, (ImmutableList<Pair<Integer, BehaviorControl<Frog>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)1, StartAttacking.create(FrogAi::canAttack, frog -> frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))), (Object)Pair.of((Object)2, TryFindLandNearWater.create(8, 1.0f)), (Object)Pair.of((Object)3, TryLaySpawnOnWaterNearLand.create(Blocks.FROGSPAWN)), (Object)Pair.of((Object)4, new RunOne(ImmutableList.of((Object)Pair.of(RandomStroll.stroll(1.0f), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)1), (Object)Pair.of((Object)new Croak(), (Object)2), (Object)Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), (Object)1))))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_PREGNANT, (Object)((Object)MemoryStatus.VALUE_PRESENT))));
    }

    private static void initJumpActivity(Brain<Frog> brain) {
        brain.addActivityWithConditions(Activity.LONG_JUMP, (ImmutableList<Pair<Integer, BehaviorControl<Frog>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new LongJumpMidJump(TIME_BETWEEN_LONG_JUMPS, SoundEvents.FROG_STEP)), (Object)Pair.of((Object)1, new LongJumpToPreferredBlock<Frog>(TIME_BETWEEN_LONG_JUMPS, 2, 4, 3.5714288f, frog -> SoundEvents.FROG_LONG_JUMP, BlockTags.FROG_PREFER_JUMP_TO, 0.5f, FrogAi::isAcceptableLandingSpot))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.TEMPTING_PLAYER, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_IN_WATER, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    private static void initTongueActivity(Brain<Frog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.TONGUE, 0, (ImmutableList<BehaviorControl<Frog>>)ImmutableList.of(StopAttackingIfTargetInvalid.create(), (Object)new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT)), MemoryModuleType.ATTACK_TARGET);
    }

    private static <E extends Mob> boolean isAcceptableLandingSpot(E e, BlockPos blockPos) {
        Level level = e.level();
        BlockPos blockPos2 = blockPos.below();
        if (!(level.getFluidState(blockPos).isEmpty() && level.getFluidState(blockPos2).isEmpty() && level.getFluidState(blockPos.above()).isEmpty())) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState.is(BlockTags.FROG_PREFER_JUMP_TO) || blockState2.is(BlockTags.FROG_PREFER_JUMP_TO)) {
            return true;
        }
        PathfindingContext pathfindingContext = new PathfindingContext(e.level(), e);
        PathType pathType = WalkNodeEvaluator.getPathTypeStatic(pathfindingContext, blockPos.mutable());
        PathType pathType2 = WalkNodeEvaluator.getPathTypeStatic(pathfindingContext, blockPos2.mutable());
        if (pathType == PathType.TRAPDOOR || blockState.isAir() && pathType2 == PathType.TRAPDOOR) {
            return true;
        }
        return LongJumpToRandomPos.defaultAcceptableLandingSpot(e, blockPos);
    }

    private static boolean canAttack(Frog frog) {
        return !BehaviorUtils.isBreeding(frog);
    }

    public static void updateActivity(Frog frog) {
        frog.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.TONGUE, (Object)Activity.LAY_SPAWN, (Object)Activity.LONG_JUMP, (Object)Activity.SWIM, (Object)Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return itemStack -> itemStack.is(ItemTags.FROG_FOOD);
    }
}

