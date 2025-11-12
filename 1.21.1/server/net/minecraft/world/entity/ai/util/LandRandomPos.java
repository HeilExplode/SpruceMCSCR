/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class LandRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int n, int n2) {
        return LandRandomPos.getPos(pathfinderMob, n, n2, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int n, int n2, ToDoubleFunction<BlockPos> toDoubleFunction) {
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return RandomPos.generateRandomPos(() -> {
            BlockPos blockPos = RandomPos.generateRandomDirection(pathfinderMob.getRandom(), n, n2);
            BlockPos blockPos2 = LandRandomPos.generateRandomPosTowardDirection(pathfinderMob, n, bl, blockPos);
            if (blockPos2 == null) {
                return null;
            }
            return LandRandomPos.movePosUpOutOfSolid(pathfinderMob, blockPos2);
        }, toDoubleFunction);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int n, int n2, Vec3 vec3) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return LandRandomPos.getPosInDirection(pathfinderMob, n, n2, vec32, bl);
    }

    @Nullable
    public static Vec3 getPosAway(PathfinderMob pathfinderMob, int n, int n2, Vec3 vec3) {
        Vec3 vec32 = pathfinderMob.position().subtract(vec3);
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return LandRandomPos.getPosInDirection(pathfinderMob, n, n2, vec32, bl);
    }

    @Nullable
    private static Vec3 getPosInDirection(PathfinderMob pathfinderMob, int n, int n2, Vec3 vec3, boolean bl) {
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), n, n2, 0, vec3.x, vec3.z, 1.5707963705062866);
            if (blockPos == null) {
                return null;
            }
            BlockPos blockPos2 = LandRandomPos.generateRandomPosTowardDirection(pathfinderMob, n, bl, blockPos);
            if (blockPos2 == null) {
                return null;
            }
            return LandRandomPos.movePosUpOutOfSolid(pathfinderMob, blockPos2);
        });
    }

    @Nullable
    public static BlockPos movePosUpOutOfSolid(PathfinderMob pathfinderMob, BlockPos blockPos2) {
        if (GoalUtils.isWater(pathfinderMob, blockPos2 = RandomPos.moveUpOutOfSolid(blockPos2, pathfinderMob.level().getMaxBuildHeight(), blockPos -> GoalUtils.isSolid(pathfinderMob, blockPos))) || GoalUtils.hasMalus(pathfinderMob, blockPos2)) {
            return null;
        }
        return blockPos2;
    }

    @Nullable
    public static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfinderMob, int n, boolean bl, BlockPos blockPos) {
        BlockPos blockPos2 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, n, pathfinderMob.getRandom(), blockPos);
        if (GoalUtils.isOutsideLimits(blockPos2, pathfinderMob) || GoalUtils.isRestricted(bl, pathfinderMob, blockPos2) || GoalUtils.isNotStable(pathfinderMob.getNavigation(), blockPos2)) {
            return null;
        }
        return blockPos2;
    }
}

