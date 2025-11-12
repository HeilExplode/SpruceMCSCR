/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class DefaultRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int n, int n2) {
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos = RandomPos.generateRandomDirection(pathfinderMob.getRandom(), n, n2);
            return DefaultRandomPos.generateRandomPosTowardDirection(pathfinderMob, n, bl, blockPos);
        });
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int n, int n2, Vec3 vec3, double d) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), n, n2, 0, vec3.x, vec3.z, d);
            if (blockPos == null) {
                return null;
            }
            return DefaultRandomPos.generateRandomPosTowardDirection(pathfinderMob, n, bl, blockPos);
        });
    }

    @Nullable
    public static Vec3 getPosAway(PathfinderMob pathfinderMob, int n, int n2, Vec3 vec3) {
        Vec3 vec32 = pathfinderMob.position().subtract(vec3);
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), n, n2, 0, vec3.x, vec3.z, 1.5707963705062866);
            if (blockPos == null) {
                return null;
            }
            return DefaultRandomPos.generateRandomPosTowardDirection(pathfinderMob, n, bl, blockPos);
        });
    }

    @Nullable
    private static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfinderMob, int n, boolean bl, BlockPos blockPos) {
        BlockPos blockPos2 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, n, pathfinderMob.getRandom(), blockPos);
        if (GoalUtils.isOutsideLimits(blockPos2, pathfinderMob) || GoalUtils.isRestricted(bl, pathfinderMob, blockPos2) || GoalUtils.isNotStable(pathfinderMob.getNavigation(), blockPos2) || GoalUtils.hasMalus(pathfinderMob, blockPos2)) {
            return null;
        }
        return blockPos2;
    }
}

