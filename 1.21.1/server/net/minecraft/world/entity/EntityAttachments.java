/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;

public class EntityAttachments {
    private final Map<EntityAttachment, List<Vec3>> attachments;

    EntityAttachments(Map<EntityAttachment, List<Vec3>> map) {
        this.attachments = map;
    }

    public static EntityAttachments createDefault(float f, float f2) {
        return EntityAttachments.builder().build(f, f2);
    }

    public static Builder builder() {
        return new Builder();
    }

    public EntityAttachments scale(float f, float f2, float f3) {
        EnumMap<EntityAttachment, List<Vec3>> enumMap = new EnumMap<EntityAttachment, List<Vec3>>(EntityAttachment.class);
        for (Map.Entry<EntityAttachment, List<Vec3>> entry : this.attachments.entrySet()) {
            enumMap.put(entry.getKey(), EntityAttachments.scalePoints(entry.getValue(), f, f2, f3));
        }
        return new EntityAttachments(enumMap);
    }

    private static List<Vec3> scalePoints(List<Vec3> list, float f, float f2, float f3) {
        ArrayList<Vec3> arrayList = new ArrayList<Vec3>(list.size());
        for (Vec3 vec3 : list) {
            arrayList.add(vec3.multiply(f, f2, f3));
        }
        return arrayList;
    }

    @Nullable
    public Vec3 getNullable(EntityAttachment entityAttachment, int n, float f) {
        List<Vec3> list = this.attachments.get((Object)entityAttachment);
        if (n < 0 || n >= list.size()) {
            return null;
        }
        return EntityAttachments.transformPoint(list.get(n), f);
    }

    public Vec3 get(EntityAttachment entityAttachment, int n, float f) {
        Vec3 vec3 = this.getNullable(entityAttachment, n, f);
        if (vec3 == null) {
            throw new IllegalStateException("Had no attachment point of type: " + String.valueOf((Object)entityAttachment) + " for index: " + n);
        }
        return vec3;
    }

    public Vec3 getClamped(EntityAttachment entityAttachment, int n, float f) {
        List<Vec3> list = this.attachments.get((Object)entityAttachment);
        if (list.isEmpty()) {
            throw new IllegalStateException("Had no attachment points of type: " + String.valueOf((Object)entityAttachment));
        }
        Vec3 vec3 = list.get(Mth.clamp(n, 0, list.size() - 1));
        return EntityAttachments.transformPoint(vec3, f);
    }

    private static Vec3 transformPoint(Vec3 vec3, float f) {
        return vec3.yRot(-f * ((float)Math.PI / 180));
    }

    public static class Builder {
        private final Map<EntityAttachment, List<Vec3>> attachments = new EnumMap<EntityAttachment, List<Vec3>>(EntityAttachment.class);

        Builder() {
        }

        public Builder attach(EntityAttachment entityAttachment, float f, float f2, float f3) {
            return this.attach(entityAttachment, new Vec3(f, f2, f3));
        }

        public Builder attach(EntityAttachment entityAttachment2, Vec3 vec3) {
            this.attachments.computeIfAbsent(entityAttachment2, entityAttachment -> new ArrayList(1)).add(vec3);
            return this;
        }

        public EntityAttachments build(float f, float f2) {
            EnumMap<EntityAttachment, List<Vec3>> enumMap = new EnumMap<EntityAttachment, List<Vec3>>(EntityAttachment.class);
            EntityAttachment[] entityAttachmentArray = EntityAttachment.values();
            int n = entityAttachmentArray.length;
            for (int i = 0; i < n; ++i) {
                EntityAttachment entityAttachment;
                List<Vec3> list = this.attachments.get((Object)(entityAttachment = entityAttachmentArray[i]));
                enumMap.put(entityAttachment, list != null ? List.copyOf(list) : entityAttachment.createFallbackPoints(f, f2));
            }
            return new EntityAttachments(enumMap);
        }
    }
}

