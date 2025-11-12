/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

public class FollowTemptation
extends Behavior<PathfinderMob> {
    public static final int TEMPTATION_COOLDOWN = 100;
    public static final double DEFAULT_CLOSE_ENOUGH_DIST = 2.5;
    public static final double BACKED_UP_CLOSE_ENOUGH_DIST = 3.5;
    private final Function<LivingEntity, Float> speedModifier;
    private final Function<LivingEntity, Double> closeEnoughDistance;

    public FollowTemptation(Function<LivingEntity, Float> function) {
        this(function, livingEntity -> 2.5);
    }

    public FollowTemptation(Function<LivingEntity, Float> function, Function<LivingEntity, Double> function2) {
        super((Map)Util.make(() -> {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            builder.put(MemoryModuleType.LOOK_TARGET, (Object)MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.WALK_TARGET, (Object)MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, (Object)MemoryStatus.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_TEMPTED, (Object)MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.TEMPTING_PLAYER, (Object)MemoryStatus.VALUE_PRESENT);
            builder.put(MemoryModuleType.BREED_TARGET, (Object)MemoryStatus.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_PANICKING, (Object)MemoryStatus.VALUE_ABSENT);
            return builder.build();
        }));
        this.speedModifier = function;
        this.closeEnoughDistance = function2;
    }

    protected float getSpeedModifier(PathfinderMob pathfinderMob) {
        return this.speedModifier.apply(pathfinderMob).floatValue();
    }

    private Optional<Player> getTemptingPlayer(PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
    }

    @Override
    protected boolean timedOut(long l) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return this.getTemptingPlayer(pathfinderMob).isPresent() && !pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET) && !pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        pathfinderMob.getBrain().setMemory(MemoryModuleType.IS_TEMPTED, true);
    }

    @Override
    protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        Brain<?> brain = pathfinderMob.getBrain();
        brain.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        brain.setMemory(MemoryModuleType.IS_TEMPTED, false);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        Player player = this.getTemptingPlayer(pathfinderMob).get();
        Brain<?> brain = pathfinderMob.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true));
        double d = this.closeEnoughDistance.apply(pathfinderMob);
        if (pathfinderMob.distanceToSqr(player) < Mth.square(d)) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(player, false), this.getSpeedModifier(pathfinderMob), 2));
        }
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }
}

