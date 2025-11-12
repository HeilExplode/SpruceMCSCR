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

public class Emerging<E extends Warden>
extends Behavior<E> {
    public Emerging(int n) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.IS_EMERGING, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED)), n);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E e, long l) {
        return true;
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        ((Entity)e).setPose(Pose.EMERGING);
        ((Entity)e).playSound(SoundEvents.WARDEN_EMERGE, 5.0f, 1.0f);
    }

    @Override
    protected void stop(ServerLevel serverLevel, E e, long l) {
        if (((Entity)e).hasPose(Pose.EMERGING)) {
            ((Entity)e).setPose(Pose.STANDING);
        }
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

