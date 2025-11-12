/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

public class DoNothing
implements BehaviorControl<LivingEntity> {
    private final int minDuration;
    private final int maxDuration;
    private Behavior.Status status = Behavior.Status.STOPPED;
    private long endTimestamp;

    public DoNothing(int n, int n2) {
        this.minDuration = n;
        this.maxDuration = n2;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStart(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.status = Behavior.Status.RUNNING;
        int n = this.minDuration + serverLevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
        this.endTimestamp = l + (long)n;
        return true;
    }

    @Override
    public final void tickOrStop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        if (l > this.endTimestamp) {
            this.doStop(serverLevel, livingEntity, l);
        }
    }

    @Override
    public final void doStop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }
}

