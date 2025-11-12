/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface ProjectileDeflection {
    public static final ProjectileDeflection NONE = (projectile, entity, randomSource) -> {};
    public static final ProjectileDeflection REVERSE = (projectile, entity, randomSource) -> {
        float f = 170.0f + randomSource.nextFloat() * 20.0f;
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.5));
        projectile.setYRot(projectile.getYRot() + f);
        projectile.yRotO += f;
        projectile.hasImpulse = true;
    };
    public static final ProjectileDeflection AIM_DEFLECT = (projectile, entity, randomSource) -> {
        if (entity != null) {
            Vec3 vec3 = entity.getLookAngle().normalize();
            projectile.setDeltaMovement(vec3);
            projectile.hasImpulse = true;
        }
    };
    public static final ProjectileDeflection MOMENTUM_DEFLECT = (projectile, entity, randomSource) -> {
        if (entity != null) {
            Vec3 vec3 = entity.getDeltaMovement().normalize();
            projectile.setDeltaMovement(vec3);
            projectile.hasImpulse = true;
        }
    };

    public void deflect(Projectile var1, @Nullable Entity var2, RandomSource var3);
}

