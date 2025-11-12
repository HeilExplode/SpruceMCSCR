/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.core.Direction;

public class SegmentedAnglePrecision {
    private final int mask;
    private final int precision;
    private final float degreeToAngle;
    private final float angleToDegree;

    public SegmentedAnglePrecision(int n) {
        if (n < 2) {
            throw new IllegalArgumentException("Precision cannot be less than 2 bits");
        }
        if (n > 30) {
            throw new IllegalArgumentException("Precision cannot be greater than 30 bits");
        }
        int n2 = 1 << n;
        this.mask = n2 - 1;
        this.precision = n;
        this.degreeToAngle = (float)n2 / 360.0f;
        this.angleToDegree = 360.0f / (float)n2;
    }

    public boolean isSameAxis(int n, int n2) {
        int n3 = this.getMask() >> 1;
        return (n & n3) == (n2 & n3);
    }

    public int fromDirection(Direction direction) {
        if (direction.getAxis().isVertical()) {
            return 0;
        }
        int n = direction.get2DDataValue();
        return n << this.precision - 2;
    }

    public int fromDegreesWithTurns(float f) {
        return Math.round(f * this.degreeToAngle);
    }

    public int fromDegrees(float f) {
        return this.normalize(this.fromDegreesWithTurns(f));
    }

    public float toDegreesWithTurns(int n) {
        return (float)n * this.angleToDegree;
    }

    public float toDegrees(int n) {
        float f = this.toDegreesWithTurns(this.normalize(n));
        return f >= 180.0f ? f - 360.0f : f;
    }

    public int normalize(int n) {
        return n & this.mask;
    }

    public int getMask() {
        return this.mask;
    }
}

