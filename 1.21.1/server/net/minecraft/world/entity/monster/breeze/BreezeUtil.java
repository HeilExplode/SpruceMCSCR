/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.breeze;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BreezeUtil {
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 50.0;

    public static Vec3 randomPointBehindTarget(LivingEntity livingEntity, RandomSource randomSource) {
        int n = 90;
        float f = livingEntity.yHeadRot + 180.0f + (float)randomSource.nextGaussian() * 90.0f / 2.0f;
        float f2 = Mth.lerp(randomSource.nextFloat(), 4.0f, 8.0f);
        Vec3 vec3 = Vec3.directionFromRotation(0.0f, f).scale(f2);
        return livingEntity.position().add(vec3);
    }

    public static boolean hasLineOfSight(Breeze breeze, Vec3 vec3) {
        Vec3 vec32 = new Vec3(breeze.getX(), breeze.getY(), breeze.getZ());
        if (vec3.distanceTo(vec32) > 50.0) {
            return false;
        }
        return breeze.level().clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, breeze)).getType() == HitResult.Type.MISS;
    }
}

