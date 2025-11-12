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

public class AirAndWaterRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int n, int n2, int n3, double d, double d2, double d3) {
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return RandomPos.generateRandomPos(pathfinderMob, () -> AirAndWaterRandomPos.generateRandomPos(pathfinderMob, n, n2, n3, d, d2, d3, bl));
    }

    @Nullable
    public static BlockPos generateRandomPos(PathfinderMob pathfinderMob, int n, int n2, int n3, double d, double d2, double d3, boolean bl) {
        BlockPos blockPos2 = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), n, n2, n3, d, d2, d3);
        if (blockPos2 == null) {
            return null;
        }
        BlockPos blockPos3 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, n, pathfinderMob.getRandom(), blockPos2);
        if (GoalUtils.isOutsideLimits(blockPos3, pathfinderMob) || GoalUtils.isRestricted(bl, pathfinderMob, blockPos3)) {
            return null;
        }
        if (GoalUtils.hasMalus(pathfinderMob, blockPos3 = RandomPos.moveUpOutOfSolid(blockPos3, pathfinderMob.level().getMaxBuildHeight(), blockPos -> GoalUtils.isSolid(pathfinderMob, blockPos)))) {
            return null;
        }
        return blockPos3;
    }
}

