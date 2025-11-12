/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Sniffing<E extends Warden>
extends Behavior<E> {
    private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_XZ = 6.0;
    private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_Y = 20.0;

    public Sniffing(int n) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.IS_SNIFFING, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.NEAREST_ATTACKABLE, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.DISTURBANCE_LOCATION, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.SNIFF_COOLDOWN, (Object)((Object)MemoryStatus.REGISTERED)), n);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E e, long l) {
        return true;
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        ((Entity)e).playSound(SoundEvents.WARDEN_SNIFF, 5.0f, 1.0f);
    }

    @Override
    protected void stop(ServerLevel serverLevel, E e, long l) {
        if (((Entity)e).hasPose(Pose.SNIFFING)) {
            ((Entity)e).setPose(Pose.STANDING);
        }
        ((Warden)e).getBrain().eraseMemory(MemoryModuleType.IS_SNIFFING);
        ((Warden)e).getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).filter(arg_0 -> e.canTargetEntity(arg_0)).ifPresent(livingEntity -> {
            if (e.closerThan((Entity)livingEntity, 6.0, 20.0)) {
                e.increaseAngerAt((Entity)livingEntity);
            }
            if (!e.getBrain().hasMemoryValue(MemoryModuleType.DISTURBANCE_LOCATION)) {
                WardenAi.setDisturbanceLocation(e, livingEntity.blockPosition());
            }
        });
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((Warden)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Warden)livingEntity), l);
    }
}

