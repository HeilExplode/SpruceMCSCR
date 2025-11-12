/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.BreezeUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LongJump
extends Behavior<Breeze> {
    private static final int REQUIRED_AIR_BLOCKS_ABOVE = 4;
    private static final int JUMP_COOLDOWN_TICKS = 10;
    private static final int JUMP_COOLDOWN_WHEN_HURT_TICKS = 2;
    private static final int INHALING_DURATION_TICKS = Math.round(10.0f);
    private static final float MAX_JUMP_VELOCITY = 1.4f;
    private static final ObjectArrayList<Integer> ALLOWED_ANGLES = new ObjectArrayList((Collection)Lists.newArrayList((Object[])new Integer[]{40, 55, 60, 75, 80}));

    @VisibleForTesting
    public LongJump() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.REGISTERED, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.REGISTERED), 200);
    }

    public static boolean canRun(ServerLevel serverLevel, Breeze breeze) {
        if (!breeze.onGround() && !breeze.isInWater()) {
            return false;
        }
        if (Swim.shouldSwim(breeze)) {
            return false;
        }
        if (breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_PRESENT)) {
            return true;
        }
        LivingEntity livingEntity = breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (livingEntity == null) {
            return false;
        }
        if (LongJump.outOfAggroRange(breeze, livingEntity)) {
            breeze.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return false;
        }
        if (LongJump.tooCloseForJump(breeze, livingEntity)) {
            return false;
        }
        if (!LongJump.canJumpFromCurrentPosition(serverLevel, breeze)) {
            return false;
        }
        BlockPos blockPos = LongJump.snapToSurface(breeze, BreezeUtil.randomPointBehindTarget(livingEntity, breeze.getRandom()));
        if (blockPos == null) {
            return false;
        }
        BlockState blockState = serverLevel.getBlockState(blockPos.below());
        if (breeze.getType().isBlockDangerous(blockState)) {
            return false;
        }
        if (!BreezeUtil.hasLineOfSight(breeze, blockPos.getCenter()) && !BreezeUtil.hasLineOfSight(breeze, blockPos.above(4).getCenter())) {
            return false;
        }
        breeze.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, blockPos);
        return true;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Breeze breeze) {
        return LongJump.canRun(serverLevel, breeze);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Breeze breeze, long l) {
        return breeze.getPose() != Pose.STANDING && !breeze.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
    }

    @Override
    protected void start(ServerLevel serverLevel, Breeze breeze, long l) {
        if (breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT)) {
            breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, INHALING_DURATION_TICKS);
        }
        breeze.setPose(Pose.INHALING);
        serverLevel.playSound(null, breeze, SoundEvents.BREEZE_CHARGE, SoundSource.HOSTILE, 1.0f, 1.0f);
        breeze.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).ifPresent(blockPos -> breeze.lookAt(EntityAnchorArgument.Anchor.EYES, blockPos.getCenter()));
    }

    @Override
    protected void tick(ServerLevel serverLevel, Breeze breeze, long l) {
        boolean bl = breeze.isInWater();
        if (!bl && breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_PRESENT)) {
            breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
        }
        if (LongJump.isFinishedInhaling(breeze)) {
            Vec3 vec3 = breeze.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).flatMap(blockPos -> LongJump.calculateOptimalJumpVector(breeze, breeze.getRandom(), Vec3.atBottomCenterOf(blockPos))).orElse(null);
            if (vec3 == null) {
                breeze.setPose(Pose.STANDING);
                return;
            }
            if (bl) {
                breeze.getBrain().setMemory(MemoryModuleType.BREEZE_LEAVING_WATER, Unit.INSTANCE);
            }
            breeze.playSound(SoundEvents.BREEZE_JUMP, 1.0f, 1.0f);
            breeze.setPose(Pose.LONG_JUMPING);
            breeze.setYRot(breeze.yBodyRot);
            breeze.setDiscardFriction(true);
            breeze.setDeltaMovement(vec3);
        } else if (LongJump.isFinishedJumping(breeze)) {
            breeze.playSound(SoundEvents.BREEZE_LAND, 1.0f, 1.0f);
            breeze.setPose(Pose.STANDING);
            breeze.setDiscardFriction(false);
            boolean bl2 = breeze.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
            breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, bl2 ? 2L : 10L);
            breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, Breeze breeze, long l) {
        if (breeze.getPose() == Pose.LONG_JUMPING || breeze.getPose() == Pose.INHALING) {
            breeze.setPose(Pose.STANDING);
        }
        breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_TARGET);
        breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_INHALING);
        breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
    }

    private static boolean isFinishedInhaling(Breeze breeze) {
        return breeze.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && breeze.getPose() == Pose.INHALING;
    }

    private static boolean isFinishedJumping(Breeze breeze) {
        boolean bl = breeze.getPose() == Pose.LONG_JUMPING;
        boolean bl2 = breeze.onGround();
        boolean bl3 = breeze.isInWater() && breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_ABSENT);
        return bl && (bl2 || bl3);
    }

    @Nullable
    private static BlockPos snapToSurface(LivingEntity livingEntity, Vec3 vec3) {
        ClipContext clipContext = new ClipContext(vec3, vec3.relative(Direction.DOWN, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity);
        BlockHitResult blockHitResult = livingEntity.level().clip(clipContext);
        if (((HitResult)blockHitResult).getType() == HitResult.Type.BLOCK) {
            return BlockPos.containing(blockHitResult.getLocation()).above();
        }
        ClipContext clipContext2 = new ClipContext(vec3, vec3.relative(Direction.UP, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity);
        BlockHitResult blockHitResult2 = livingEntity.level().clip(clipContext2);
        if (((HitResult)blockHitResult2).getType() == HitResult.Type.BLOCK) {
            return BlockPos.containing(blockHitResult2.getLocation()).above();
        }
        return null;
    }

    private static boolean outOfAggroRange(Breeze breeze, LivingEntity livingEntity) {
        return !livingEntity.closerThan(breeze, 24.0);
    }

    private static boolean tooCloseForJump(Breeze breeze, LivingEntity livingEntity) {
        return livingEntity.distanceTo(breeze) - 4.0f <= 0.0f;
    }

    private static boolean canJumpFromCurrentPosition(ServerLevel serverLevel, Breeze breeze) {
        BlockPos blockPos = breeze.blockPosition();
        for (int i = 1; i <= 4; ++i) {
            BlockPos blockPos2 = blockPos.relative(Direction.UP, i);
            if (serverLevel.getBlockState(blockPos2).isAir() || serverLevel.getFluidState(blockPos2).is(FluidTags.WATER)) continue;
            return false;
        }
        return true;
    }

    private static Optional<Vec3> calculateOptimalJumpVector(Breeze breeze, RandomSource randomSource, Vec3 vec3) {
        List<Integer> list = Util.shuffledCopy(ALLOWED_ANGLES, randomSource);
        for (int n : list) {
            Optional<Vec3> optional = LongJumpUtil.calculateJumpVectorForAngle(breeze, vec3, 1.4f, n, false);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    protected /* synthetic */ boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        return this.checkExtraStartConditions(serverLevel, (Breeze)livingEntity);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Breeze)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Breeze)livingEntity, l);
    }
}

