/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.List;
import net.minecraft.world.phys.Vec3;

public enum EntityAttachment {
    PASSENGER(Fallback.AT_HEIGHT),
    VEHICLE(Fallback.AT_FEET),
    NAME_TAG(Fallback.AT_HEIGHT),
    WARDEN_CHEST(Fallback.AT_CENTER);

    private final Fallback fallback;

    private EntityAttachment(Fallback fallback) {
        this.fallback = fallback;
    }

    public List<Vec3> createFallbackPoints(float f, float f2) {
        return this.fallback.create(f, f2);
    }

    public static interface Fallback {
        public static final List<Vec3> ZERO = List.of(Vec3.ZERO);
        public static final Fallback AT_FEET = (f, f2) -> ZERO;
        public static final Fallback AT_HEIGHT = (f, f2) -> List.of(new Vec3(0.0, f2, 0.0));
        public static final Fallback AT_CENTER = (f, f2) -> List.of(new Vec3(0.0, (double)f2 / 2.0, 0.0));

        public List<Vec3> create(float var1, float var2);
    }
}

