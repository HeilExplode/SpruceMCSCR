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

public class Digging<E extends Warden>
extends Behavior<E> {
    public Digging(int n) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), n);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E e, long l) {
        return ((Entity)e).getRemovalReason() == null;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        return ((Entity)e).onGround() || ((Entity)e).isInWater() || ((Entity)e).isInLava();
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        if (((Entity)e).onGround()) {
            ((Entity)e).setPose(Pose.DIGGING);
            ((Entity)e).playSound(SoundEvents.WARDEN_DIG, 5.0f, 1.0f);
        } else {
            ((Entity)e).playSound(SoundEvents.WARDEN_AGITATED, 5.0f, 1.0f);
            this.stop(serverLevel, e, l);
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, E e, long l) {
        if (((Entity)e).getRemovalReason() == null) {
            ((LivingEntity)e).remove(Entity.RemovalReason.DISCARDED);
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

