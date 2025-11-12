/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.breeze;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.BreezeUtil;
import net.minecraft.world.phys.Vec3;

public class Slide
extends Behavior<Breeze> {
    public Slide() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Breeze breeze) {
        return breeze.onGround() && !breeze.isInWater() && breeze.getPose() == Pose.STANDING;
    }

    @Override
    protected void start(ServerLevel serverLevel, Breeze breeze, long l) {
        Vec3 vec3;
        LivingEntity livingEntity = breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (livingEntity == null) {
            return;
        }
        boolean bl = breeze.withinInnerCircleRange(livingEntity.position());
        Vec3 vec32 = null;
        if (bl && (vec3 = DefaultRandomPos.getPosAway(breeze, 5, 5, livingEntity.position())) != null && BreezeUtil.hasLineOfSight(breeze, vec3) && livingEntity.distanceToSqr(vec3.x, vec3.y, vec3.z) > livingEntity.distanceToSqr(breeze)) {
            vec32 = vec3;
        }
        if (vec32 == null) {
            vec32 = breeze.getRandom().nextBoolean() ? BreezeUtil.randomPointBehindTarget(livingEntity, breeze.getRandom()) : Slide.randomPointInMiddleCircle(breeze, livingEntity);
        }
        breeze.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(BlockPos.containing(vec32), 0.6f, 1));
    }

    private static Vec3 randomPointInMiddleCircle(Breeze breeze, LivingEntity livingEntity) {
        Vec3 vec3 = livingEntity.position().subtract(breeze.position());
        double d = vec3.length() - Mth.lerp(breeze.getRandom().nextDouble(), 8.0, 4.0);
        Vec3 vec32 = vec3.normalize().multiply(d, d, d);
        return breeze.position().add(vec32);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Breeze)livingEntity, l);
    }
}

