/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

public abstract class PathfinderMob
extends Mob {
    protected static final float DEFAULT_WALK_TARGET_VALUE = 0.0f;

    protected PathfinderMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super((EntityType<? extends Mob>)entityType, level);
    }

    public float getWalkTargetValue(BlockPos blockPos) {
        return this.getWalkTargetValue(blockPos, this.level());
    }

    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.0f;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType mobSpawnType) {
        return this.getWalkTargetValue(this.blockPosition(), levelAccessor) >= 0.0f;
    }

    public boolean isPathFinding() {
        return !this.getNavigation().isDone();
    }

    public boolean isPanicking() {
        if (this.brain.hasMemoryValue(MemoryModuleType.IS_PANICKING)) {
            return this.brain.getMemory(MemoryModuleType.IS_PANICKING).isPresent();
        }
        for (WrappedGoal wrappedGoal : this.goalSelector.getAvailableGoals()) {
            if (!wrappedGoal.isRunning() || !(wrappedGoal.getGoal() instanceof PanicGoal)) continue;
            return true;
        }
        return false;
    }

    protected boolean shouldStayCloseToLeashHolder() {
        return true;
    }

    @Override
    public void closeRangeLeashBehaviour(Entity entity) {
        super.closeRangeLeashBehaviour(entity);
        if (this.shouldStayCloseToLeashHolder() && !this.isPanicking()) {
            this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
            float f = 2.0f;
            float f2 = this.distanceTo(entity);
            Vec3 vec3 = new Vec3(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ()).normalize().scale(Math.max(f2 - 2.0f, 0.0f));
            this.getNavigation().moveTo(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z, this.followLeashSpeed());
        }
    }

    @Override
    public boolean handleLeashAtDistance(Entity entity, float f) {
        this.restrictTo(entity.blockPosition(), 5);
        return true;
    }

    protected double followLeashSpeed() {
        return 1.0;
    }
}

