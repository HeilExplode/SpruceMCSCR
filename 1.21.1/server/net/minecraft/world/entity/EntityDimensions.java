/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EntityDimensions(float width, float height, float eyeHeight, EntityAttachments attachments, boolean fixed) {
    private EntityDimensions(float f, float f2, boolean bl) {
        this(f, f2, EntityDimensions.defaultEyeHeight(f2), EntityAttachments.createDefault(f, f2), bl);
    }

    private static float defaultEyeHeight(float f) {
        return f * 0.85f;
    }

    public AABB makeBoundingBox(Vec3 vec3) {
        return this.makeBoundingBox(vec3.x, vec3.y, vec3.z);
    }

    public AABB makeBoundingBox(double d, double d2, double d3) {
        float f = this.width / 2.0f;
        float f2 = this.height;
        return new AABB(d - (double)f, d2, d3 - (double)f, d + (double)f, d2 + (double)f2, d3 + (double)f);
    }

    public EntityDimensions scale(float f) {
        return this.scale(f, f);
    }

    public EntityDimensions scale(float f, float f2) {
        if (this.fixed || f == 1.0f && f2 == 1.0f) {
            return this;
        }
        return new EntityDimensions(this.width * f, this.height * f2, this.eyeHeight * f2, this.attachments.scale(f, f2, f), false);
    }

    public static EntityDimensions scalable(float f, float f2) {
        return new EntityDimensions(f, f2, false);
    }

    public static EntityDimensions fixed(float f, float f2) {
        return new EntityDimensions(f, f2, true);
    }

    public EntityDimensions withEyeHeight(float f) {
        return new EntityDimensions(this.width, this.height, f, this.attachments, this.fixed);
    }

    public EntityDimensions withAttachments(EntityAttachments.Builder builder) {
        return new EntityDimensions(this.width, this.height, this.eyeHeight, builder.build(this.width, this.height), this.fixed);
    }
}

