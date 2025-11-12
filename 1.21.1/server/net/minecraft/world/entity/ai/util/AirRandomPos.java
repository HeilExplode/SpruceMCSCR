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
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class AirRandomPos {
    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int n, int n2, int n3, Vec3 vec3, double d) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, n);
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos = AirAndWaterRandomPos.generateRandomPos(pathfinderMob, n, n2, n3, vec3.x, vec3.z, d, bl);
            if (blockPos == null || GoalUtils.isWater(pathfinderMob, blockPos)) {
                return null;
            }
            return blockPos;
        });
    }
}

