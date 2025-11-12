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
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class HoverRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int n, int n2, double d, double d2, float f, int n3, int n4) {
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos2 = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), n, n2, 0, d, d2, f);
            if (blockPos2 == null) {
                return null;
            }
            BlockPos blockPos3 = LandRandomPos.generateRandomPosTowardDirection(pathfinderMob, n, bl, blockPos2);
            if (blockPos3 == null) {
                return null;
            }
            if (GoalUtils.isWater(pathfinderMob, blockPos3 = RandomPos.moveUpToAboveSolid(blockPos3, pathfinderMob.getRandom().nextInt(n3 - n4 + 1) + n4, pathfinderMob.level().getMaxBuildHeight(), blockPos -> GoalUtils.isSolid(pathfinderMob, blockPos))) || GoalUtils.hasMalus(pathfinderMob, blockPos3)) {
                return null;
            }
            return blockPos3;
        });
    }
}

