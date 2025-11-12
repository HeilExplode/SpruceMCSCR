/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Shoot
extends Behavior<Breeze> {
    private static final int ATTACK_RANGE_MIN_SQRT = 4;
    private static final int ATTACK_RANGE_MAX_SQRT = 256;
    private static final int UNCERTAINTY_BASE = 5;
    private static final int UNCERTAINTY_MULTIPLIER = 4;
    private static final float PROJECTILE_MOVEMENT_SCALE = 0.7f;
    private static final int SHOOT_INITIAL_DELAY_TICKS = Math.round(15.0f);
    private static final int SHOOT_RECOVER_DELAY_TICKS = Math.round(4.0f);
    private static final int SHOOT_COOLDOWN_TICKS = Math.round(10.0f);

    @VisibleForTesting
    public Shoot() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.BREEZE_SHOOT_COOLDOWN, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT_CHARGING, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT_RECOVERING, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_JUMP_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), SHOOT_INITIAL_DELAY_TICKS + 1 + SHOOT_RECOVER_DELAY_TICKS);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Breeze breeze) {
        if (breeze.getPose() != Pose.STANDING) {
            return false;
        }
        return breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(livingEntity -> Shoot.isTargetWithinRange(breeze, livingEntity)).map(bl -> {
            if (!bl.booleanValue()) {
                breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
            }
            return bl;
        }).orElse(false);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Breeze breeze, long l) {
        return breeze.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && breeze.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void start(ServerLevel serverLevel, Breeze breeze, long l) {
        breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(livingEntity -> breeze.setPose(Pose.SHOOTING));
        breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, SHOOT_INITIAL_DELAY_TICKS);
        breeze.playSound(SoundEvents.BREEZE_INHALE, 1.0f, 1.0f);
    }

    @Override
    protected void stop(ServerLevel serverLevel, Breeze breeze, long l) {
        if (breeze.getPose() == Pose.SHOOTING) {
            breeze.setPose(Pose.STANDING);
        }
        breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, SHOOT_COOLDOWN_TICKS);
        breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Breeze breeze, long l) {
        Brain<Breeze> brain = breeze.getBrain();
        LivingEntity livingEntity = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (livingEntity == null) {
            return;
        }
        breeze.lookAt(EntityAnchorArgument.Anchor.EYES, livingEntity.position());
        if (brain.getMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() || brain.getMemory(MemoryModuleType.BREEZE_SHOOT_RECOVERING).isPresent()) {
            return;
        }
        brain.setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_RECOVERING, Unit.INSTANCE, SHOOT_RECOVER_DELAY_TICKS);
        if (Shoot.isFacingTarget(breeze, livingEntity)) {
            double d = livingEntity.getX() - breeze.getX();
            double d2 = livingEntity.getY(livingEntity.isPassenger() ? 0.8 : 0.3) - breeze.getY(0.5);
            double d3 = livingEntity.getZ() - breeze.getZ();
            BreezeWindCharge breezeWindCharge = new BreezeWindCharge(breeze, (Level)serverLevel);
            breeze.playSound(SoundEvents.BREEZE_SHOOT, 1.5f, 1.0f);
            breezeWindCharge.shoot(d, d2, d3, 0.7f, 5 - serverLevel.getDifficulty().getId() * 4);
            serverLevel.addFreshEntity(breezeWindCharge);
        }
    }

    @VisibleForTesting
    public static boolean isFacingTarget(Breeze breeze, LivingEntity livingEntity) {
        Vec3 vec3;
        Vec3 vec32 = breeze.getViewVector(1.0f);
        return vec32.dot(vec3 = livingEntity.position().subtract(breeze.position()).normalize()) > 0.5;
    }

    private static boolean isTargetWithinRange(Breeze breeze, LivingEntity livingEntity) {
        double d = breeze.position().distanceToSqr(livingEntity.position());
        return d > 4.0 && d < 256.0;
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Breeze)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Breeze)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Breeze)livingEntity, l);
    }
}

