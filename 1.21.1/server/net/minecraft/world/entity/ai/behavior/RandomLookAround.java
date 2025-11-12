/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class RandomLookAround
extends Behavior<Mob> {
    private final IntProvider interval;
    private final float maxYaw;
    private final float minPitch;
    private final float pitchRange;

    public RandomLookAround(IntProvider intProvider, float f, float f2, float f3) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.GAZE_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT)));
        if (f2 > f3) {
            throw new IllegalArgumentException("Minimum pitch is larger than maximum pitch! " + f2 + " > " + f3);
        }
        this.interval = intProvider;
        this.maxYaw = f;
        this.minPitch = f2;
        this.pitchRange = f3 - f2;
    }

    @Override
    protected void start(ServerLevel serverLevel, Mob mob, long l) {
        RandomSource randomSource = mob.getRandom();
        float f = Mth.clamp(randomSource.nextFloat() * this.pitchRange + this.minPitch, -90.0f, 90.0f);
        float f2 = Mth.wrapDegrees(mob.getYRot() + 2.0f * randomSource.nextFloat() * this.maxYaw - this.maxYaw);
        Vec3 vec3 = Vec3.directionFromRotation(f, f2);
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(mob.getEyePosition().add(vec3)));
        mob.getBrain().setMemory(MemoryModuleType.GAZE_COOLDOWN_TICKS, this.interval.sample(randomSource));
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Mob)livingEntity, l);
    }
}

