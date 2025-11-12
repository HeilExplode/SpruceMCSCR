/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

public class GoatAi {
    public static final int RAM_PREPARE_TIME = 20;
    public static final int RAM_MAX_DISTANCE = 7;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    private static final float SPEED_MULTIPLIER_WHEN_PREPARING_TO_RAM = 1.25f;
    private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(600, 1200);
    public static final int MAX_LONG_JUMP_HEIGHT = 5;
    public static final int MAX_LONG_JUMP_WIDTH = 5;
    public static final float MAX_JUMP_VELOCITY_MULTIPLIER = 3.5714288f;
    private static final UniformInt TIME_BETWEEN_RAMS = UniformInt.of(600, 6000);
    private static final UniformInt TIME_BETWEEN_RAMS_SCREAMER = UniformInt.of(100, 300);
    private static final TargetingConditions RAM_TARGET_CONDITIONS = TargetingConditions.forCombat().selector(livingEntity -> !livingEntity.getType().equals(EntityType.GOAT) && livingEntity.level().getWorldBorder().isWithinBounds(livingEntity.getBoundingBox()));
    private static final float SPEED_MULTIPLIER_WHEN_RAMMING = 3.0f;
    public static final int RAM_MIN_DISTANCE = 4;
    public static final float ADULT_RAM_KNOCKBACK_FORCE = 2.5f;
    public static final float BABY_RAM_KNOCKBACK_FORCE = 1.0f;

    protected static void initMemories(Goat goat, RandomSource randomSource) {
        goat.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, TIME_BETWEEN_LONG_JUMPS.sample(randomSource));
        goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, TIME_BETWEEN_RAMS.sample(randomSource));
    }

    protected static Brain<?> makeBrain(Brain<Goat> brain) {
        GoatAi.initCoreActivity(brain);
        GoatAi.initIdleActivity(brain);
        GoatAi.initLongJumpActivity(brain);
        GoatAi.initRamActivity(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Goat> brain) {
        brain.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Goat>>)ImmutableList.of((Object)new Swim(0.8f), new AnimalPanic(2.0f), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.RAM_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Goat> brain) {
        brain.addActivityWithConditions(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Goat>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)0, (Object)new AnimalMakeLove(EntityType.GOAT)), (Object)Pair.of((Object)1, (Object)new FollowTemptation(livingEntity -> Float.valueOf(1.25f))), (Object)Pair.of((Object)2, BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25f)), (Object)Pair.of((Object)3, new RunOne(ImmutableList.of((Object)Pair.of(RandomStroll.stroll(1.0f), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)2), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1))))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.RAM_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    private static void initLongJumpActivity(Brain<Goat> brain) {
        brain.addActivityWithConditions(Activity.LONG_JUMP, (ImmutableList<Pair<Integer, BehaviorControl<Goat>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new LongJumpMidJump(TIME_BETWEEN_LONG_JUMPS, SoundEvents.GOAT_STEP)), (Object)Pair.of((Object)1, new LongJumpToRandomPos<Goat>(TIME_BETWEEN_LONG_JUMPS, 5, 5, 3.5714288f, goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_LONG_JUMP : SoundEvents.GOAT_LONG_JUMP))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.TEMPTING_PLAYER, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    private static void initRamActivity(Brain<Goat> brain) {
        brain.addActivityWithConditions(Activity.RAM, (ImmutableList<Pair<Integer, BehaviorControl<Goat>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new RamTarget(goat -> goat.isScreamingGoat() ? TIME_BETWEEN_RAMS_SCREAMER : TIME_BETWEEN_RAMS, RAM_TARGET_CONDITIONS, 3.0f, goat -> goat.isBaby() ? 1.0 : 2.5, goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_RAM_IMPACT : SoundEvents.GOAT_RAM_IMPACT, goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_HORN_BREAK : SoundEvents.GOAT_HORN_BREAK)), (Object)Pair.of((Object)1, new PrepareRamNearestTarget<Goat>(goat -> goat.isScreamingGoat() ? TIME_BETWEEN_RAMS_SCREAMER.getMinValue() : TIME_BETWEEN_RAMS.getMinValue(), 4, 7, 1.25f, RAM_TARGET_CONDITIONS, 20, goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_PREPARE_RAM : SoundEvents.GOAT_PREPARE_RAM))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.TEMPTING_PLAYER, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    public static void updateActivity(Goat goat) {
        goat.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.RAM, (Object)Activity.LONG_JUMP, (Object)Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return itemStack -> itemStack.is(ItemTags.GOAT_FOOD);
    }
}

