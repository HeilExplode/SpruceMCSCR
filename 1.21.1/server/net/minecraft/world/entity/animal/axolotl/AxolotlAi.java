/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindWater;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.PlayDead;
import net.minecraft.world.entity.animal.axolotl.ValidatePlayDead;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AxolotlAi {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.2f;
    private static final float SPEED_MULTIPLIER_ON_LAND = 0.15f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 0.5f;
    private static final float SPEED_MULTIPLIER_WHEN_CHASING_IN_WATER = 0.6f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT_IN_WATER = 0.6f;

    protected static Brain<?> makeBrain(Brain<Axolotl> brain) {
        AxolotlAi.initCoreActivity(brain);
        AxolotlAi.initIdleActivity(brain);
        AxolotlAi.initFightActivity(brain);
        AxolotlAi.initPlayDeadActivity(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initPlayDeadActivity(Brain<Axolotl> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.PLAY_DEAD, (ImmutableList<Pair<Integer, BehaviorControl<Axolotl>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new PlayDead()), (Object)Pair.of((Object)1, EraseMemoryIf.create(BehaviorUtils::isBreeding, MemoryModuleType.PLAY_DEAD_TICKS))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.PLAY_DEAD_TICKS, (Object)((Object)MemoryStatus.VALUE_PRESENT))), (Set<MemoryModuleType<?>>)ImmutableSet.of(MemoryModuleType.PLAY_DEAD_TICKS));
    }

    private static void initFightActivity(Brain<Axolotl> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, (ImmutableList<BehaviorControl<Axolotl>>)ImmutableList.of(StopAttackingIfTargetInvalid.create(Axolotl::onStopAttacking), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(AxolotlAi::getSpeedModifierChasing), MeleeAttack.create(20), EraseMemoryIf.create(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCoreActivity(Brain<Axolotl> brain) {
        brain.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Axolotl>>)ImmutableList.of((Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), ValidatePlayDead.create(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Axolotl> brain) {
        brain.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Axolotl>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)1, (Object)new AnimalMakeLove(EntityType.AXOLOTL, 0.2f, 2)), (Object)Pair.of((Object)2, new RunOne(ImmutableList.of((Object)Pair.of((Object)new FollowTemptation(AxolotlAi::getSpeedModifier), (Object)1), (Object)Pair.of(BabyFollowAdult.create(ADULT_FOLLOW_RANGE, AxolotlAi::getSpeedModifierFollowingAdult), (Object)1)))), (Object)Pair.of((Object)3, StartAttacking.create(AxolotlAi::findNearestValidAttackTarget)), (Object)Pair.of((Object)3, TryFindWater.create(6, 0.15f)), (Object)Pair.of((Object)4, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Set<MemoryModuleType<?>>)ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of((Object)Pair.of(RandomStroll.swim(0.5f), (Object)2), (Object)Pair.of(RandomStroll.stroll(0.15f, false), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(AxolotlAi::canSetWalkTargetFromLookTarget, AxolotlAi::getSpeedModifier, 3), (Object)3), (Object)Pair.of(BehaviorBuilder.triggerIf(Entity::isInWaterOrBubble), (Object)5), (Object)Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), (Object)5))))));
    }

    private static boolean canSetWalkTargetFromLookTarget(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        Optional<PositionTracker> optional = livingEntity.getBrain().getMemory(MemoryModuleType.LOOK_TARGET);
        if (optional.isPresent()) {
            BlockPos blockPos = optional.get().currentBlockPosition();
            return level.isWaterAt(blockPos) == livingEntity.isInWaterOrBubble();
        }
        return false;
    }

    public static void updateActivity(Axolotl axolotl) {
        Brain<Axolotl> brain = axolotl.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != Activity.PLAY_DEAD) {
            brain.setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.PLAY_DEAD, (Object)Activity.FIGHT, (Object)Activity.IDLE));
            if (activity == Activity.FIGHT && brain.getActiveNonCoreActivity().orElse(null) != Activity.FIGHT) {
                brain.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
            }
        }
    }

    private static float getSpeedModifierChasing(LivingEntity livingEntity) {
        return livingEntity.isInWaterOrBubble() ? 0.6f : 0.15f;
    }

    private static float getSpeedModifierFollowingAdult(LivingEntity livingEntity) {
        return livingEntity.isInWaterOrBubble() ? 0.6f : 0.15f;
    }

    private static float getSpeedModifier(LivingEntity livingEntity) {
        return livingEntity.isInWaterOrBubble() ? 0.5f : 0.15f;
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Axolotl axolotl) {
        if (BehaviorUtils.isBreeding(axolotl)) {
            return Optional.empty();
        }
        return axolotl.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    public static Predicate<ItemStack> getTemptations() {
        return itemStack -> itemStack.is(ItemTags.AXOLOTL_FOOD);
    }
}

