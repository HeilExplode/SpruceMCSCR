/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.phys.Vec3;

public class LookControl
implements Control {
    protected final Mob mob;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected int lookAtCooldown;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public LookControl(Mob mob) {
        this.mob = mob;
    }

    public void setLookAt(Vec3 vec3) {
        this.setLookAt(vec3.x, vec3.y, vec3.z);
    }

    public void setLookAt(Entity entity) {
        this.setLookAt(entity.getX(), LookControl.getWantedY(entity), entity.getZ());
    }

    public void setLookAt(Entity entity, float f, float f2) {
        this.setLookAt(entity.getX(), LookControl.getWantedY(entity), entity.getZ(), f, f2);
    }

    public void setLookAt(double d, double d2, double d3) {
        this.setLookAt(d, d2, d3, this.mob.getHeadRotSpeed(), this.mob.getMaxHeadXRot());
    }

    public void setLookAt(double d, double d2, double d3, float f, float f2) {
        this.wantedX = d;
        this.wantedY = d2;
        this.wantedZ = d3;
        this.yMaxRotSpeed = f;
        this.xMaxRotAngle = f2;
        this.lookAtCooldown = 2;
    }

    public void tick() {
        if (this.resetXRotOnTick()) {
            this.mob.setXRot(0.0f);
        }
        if (this.lookAtCooldown > 0) {
            --this.lookAtCooldown;
            this.getYRotD().ifPresent(f -> {
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, f.floatValue(), this.yMaxRotSpeed);
            });
            this.getXRotD().ifPresent(f -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), f.floatValue(), this.xMaxRotAngle)));
        } else {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0f);
        }
        this.clampHeadRotationToBody();
    }

    protected void clampHeadRotationToBody() {
        if (!this.mob.getNavigation().isDone()) {
            this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
        }
    }

    protected boolean resetXRotOnTick() {
        return true;
    }

    public boolean isLookingAtTarget() {
        return this.lookAtCooldown > 0;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    protected Optional<Float> getXRotD() {
        double d = this.wantedX - this.mob.getX();
        double d2 = this.wantedY - this.mob.getEyeY();
        double d3 = this.wantedZ - this.mob.getZ();
        double d4 = Math.sqrt(d * d + d3 * d3);
        return Math.abs(d2) > (double)1.0E-5f || Math.abs(d4) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(-(Mth.atan2(d2, d4) * 57.2957763671875)))) : Optional.empty();
    }

    protected Optional<Float> getYRotD() {
        double d = this.wantedX - this.mob.getX();
        double d2 = this.wantedZ - this.mob.getZ();
        return Math.abs(d2) > (double)1.0E-5f || Math.abs(d) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(Mth.atan2(d2, d) * 57.2957763671875) - 90.0f)) : Optional.empty();
    }

    protected float rotateTowards(float f, float f2, float f3) {
        float f4 = Mth.degreesDifference(f, f2);
        float f5 = Mth.clamp(f4, -f3, f3);
        return f + f5;
    }

    private static double getWantedY(Entity entity) {
        if (entity instanceof LivingEntity) {
            return entity.getEyeY();
        }
        return (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
    }
}

