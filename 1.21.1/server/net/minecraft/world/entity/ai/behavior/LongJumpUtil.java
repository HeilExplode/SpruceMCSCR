/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public final class LongJumpUtil {
    public static Optional<Vec3> calculateJumpVectorForAngle(Mob mob, Vec3 vec3, float f, int n, boolean bl) {
        Vec3 vec32 = mob.position();
        Vec3 vec33 = new Vec3(vec3.x - vec32.x, 0.0, vec3.z - vec32.z).normalize().scale(0.5);
        Vec3 vec34 = vec3.subtract(vec33);
        Vec3 vec35 = vec34.subtract(vec32);
        float f2 = (float)n * (float)Math.PI / 180.0f;
        double d = Math.atan2(vec35.z, vec35.x);
        double d2 = vec35.subtract(0.0, vec35.y, 0.0).lengthSqr();
        double d3 = Math.sqrt(d2);
        double d4 = vec35.y;
        double d5 = mob.getGravity();
        double d6 = Math.sin(2.0f * f2);
        double d7 = Math.pow(Math.cos(f2), 2.0);
        double d8 = Math.sin(f2);
        double d9 = Math.cos(f2);
        double d10 = Math.sin(d);
        double d11 = Math.cos(d);
        double d12 = d2 * d5 / (d3 * d6 - 2.0 * d4 * d7);
        if (d12 < 0.0) {
            return Optional.empty();
        }
        double d13 = Math.sqrt(d12);
        if (d13 > (double)f) {
            return Optional.empty();
        }
        double d14 = d13 * d9;
        double d15 = d13 * d8;
        if (bl) {
            int n2 = Mth.ceil(d3 / d14) * 2;
            double d16 = 0.0;
            Vec3 vec36 = null;
            EntityDimensions entityDimensions = mob.getDimensions(Pose.LONG_JUMPING);
            for (int i = 0; i < n2 - 1; ++i) {
                double d17 = d8 / d9 * (d16 += d3 / (double)n2) - Math.pow(d16, 2.0) * d5 / (2.0 * d12 * Math.pow(d9, 2.0));
                double d18 = d16 * d11;
                double d19 = d16 * d10;
                Vec3 vec37 = new Vec3(vec32.x + d18, vec32.y + d17, vec32.z + d19);
                if (vec36 != null && !LongJumpUtil.isClearTransition(mob, entityDimensions, vec36, vec37)) {
                    return Optional.empty();
                }
                vec36 = vec37;
            }
        }
        return Optional.of(new Vec3(d14 * d11, d15, d14 * d10).scale(0.95f));
    }

    private static boolean isClearTransition(Mob mob, EntityDimensions entityDimensions, Vec3 vec3, Vec3 vec32) {
        Vec3 vec33 = vec32.subtract(vec3);
        double d = Math.min(entityDimensions.width(), entityDimensions.height());
        int n = Mth.ceil(vec33.length() / d);
        Vec3 vec34 = vec33.normalize();
        Vec3 vec35 = vec3;
        for (int i = 0; i < n; ++i) {
            Vec3 vec36 = vec35 = i == n - 1 ? vec32 : vec35.add(vec34.scale(d * (double)0.9f));
            if (mob.level().noCollision(mob, entityDimensions.makeBoundingBox(vec35))) continue;
            return false;
        }
        return true;
    }
}

