/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.EnumSet;
import java.util.Set;

public enum RelativeMovement {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4);

    public static final Set<RelativeMovement> ALL;
    public static final Set<RelativeMovement> ROTATION;
    private final int bit;

    private RelativeMovement(int n2) {
        this.bit = n2;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int n) {
        return (n & this.getMask()) == this.getMask();
    }

    public static Set<RelativeMovement> unpack(int n) {
        EnumSet<RelativeMovement> enumSet = EnumSet.noneOf(RelativeMovement.class);
        for (RelativeMovement relativeMovement : RelativeMovement.values()) {
            if (!relativeMovement.isSet(n)) continue;
            enumSet.add(relativeMovement);
        }
        return enumSet;
    }

    public static int pack(Set<RelativeMovement> set) {
        int n = 0;
        for (RelativeMovement relativeMovement : set) {
            n |= relativeMovement.getMask();
        }
        return n;
    }

    static {
        ALL = Set.of(RelativeMovement.values());
        ROTATION = Set.of(X_ROT, Y_ROT);
    }
}

