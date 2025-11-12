/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  javax.annotation.concurrent.Immutable
 */
package net.minecraft.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;

@Immutable
public class Vec3i
implements Comparable<Vec3i> {
    public static final Codec<Vec3i> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 3).map(nArray -> new Vec3i(nArray[0], nArray[1], nArray[2])), vec3i -> IntStream.of(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    private int x;
    private int y;
    private int z;

    public static Codec<Vec3i> offsetCodec(int n) {
        return CODEC.validate(vec3i -> {
            if (Math.abs(vec3i.getX()) < n && Math.abs(vec3i.getY()) < n && Math.abs(vec3i.getZ()) < n) {
                return DataResult.success((Object)vec3i);
            }
            return DataResult.error(() -> "Position out of range, expected at most " + n + ": " + String.valueOf(vec3i));
        });
    }

    public Vec3i(int n, int n2, int n3) {
        this.x = n;
        this.y = n2;
        this.z = n3;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Vec3i)) {
            return false;
        }
        Vec3i vec3i = (Vec3i)object;
        if (this.getX() != vec3i.getX()) {
            return false;
        }
        if (this.getY() != vec3i.getY()) {
            return false;
        }
        return this.getZ() == vec3i.getZ();
    }

    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    @Override
    public int compareTo(Vec3i vec3i) {
        if (this.getY() == vec3i.getY()) {
            if (this.getZ() == vec3i.getZ()) {
                return this.getX() - vec3i.getX();
            }
            return this.getZ() - vec3i.getZ();
        }
        return this.getY() - vec3i.getY();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    protected Vec3i setX(int n) {
        this.x = n;
        return this;
    }

    protected Vec3i setY(int n) {
        this.y = n;
        return this;
    }

    protected Vec3i setZ(int n) {
        this.z = n;
        return this;
    }

    public Vec3i offset(int n, int n2, int n3) {
        if (n == 0 && n2 == 0 && n3 == 0) {
            return this;
        }
        return new Vec3i(this.getX() + n, this.getY() + n2, this.getZ() + n3);
    }

    public Vec3i offset(Vec3i vec3i) {
        return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public Vec3i subtract(Vec3i vec3i) {
        return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
    }

    public Vec3i multiply(int n) {
        if (n == 1) {
            return this;
        }
        if (n == 0) {
            return ZERO;
        }
        return new Vec3i(this.getX() * n, this.getY() * n, this.getZ() * n);
    }

    public Vec3i above() {
        return this.above(1);
    }

    public Vec3i above(int n) {
        return this.relative(Direction.UP, n);
    }

    public Vec3i below() {
        return this.below(1);
    }

    public Vec3i below(int n) {
        return this.relative(Direction.DOWN, n);
    }

    public Vec3i north() {
        return this.north(1);
    }

    public Vec3i north(int n) {
        return this.relative(Direction.NORTH, n);
    }

    public Vec3i south() {
        return this.south(1);
    }

    public Vec3i south(int n) {
        return this.relative(Direction.SOUTH, n);
    }

    public Vec3i west() {
        return this.west(1);
    }

    public Vec3i west(int n) {
        return this.relative(Direction.WEST, n);
    }

    public Vec3i east() {
        return this.east(1);
    }

    public Vec3i east(int n) {
        return this.relative(Direction.EAST, n);
    }

    public Vec3i relative(Direction direction) {
        return this.relative(direction, 1);
    }

    public Vec3i relative(Direction direction, int n) {
        if (n == 0) {
            return this;
        }
        return new Vec3i(this.getX() + direction.getStepX() * n, this.getY() + direction.getStepY() * n, this.getZ() + direction.getStepZ() * n);
    }

    public Vec3i relative(Direction.Axis axis, int n) {
        if (n == 0) {
            return this;
        }
        int n2 = axis == Direction.Axis.X ? n : 0;
        int n3 = axis == Direction.Axis.Y ? n : 0;
        int n4 = axis == Direction.Axis.Z ? n : 0;
        return new Vec3i(this.getX() + n2, this.getY() + n3, this.getZ() + n4);
    }

    public Vec3i cross(Vec3i vec3i) {
        return new Vec3i(this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(), this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(), this.getX() * vec3i.getY() - this.getY() * vec3i.getX());
    }

    public boolean closerThan(Vec3i vec3i, double d) {
        return this.distSqr(vec3i) < Mth.square(d);
    }

    public boolean closerToCenterThan(Position position, double d) {
        return this.distToCenterSqr(position) < Mth.square(d);
    }

    public double distSqr(Vec3i vec3i) {
        return this.distToLowCornerSqr(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public double distToCenterSqr(Position position) {
        return this.distToCenterSqr(position.x(), position.y(), position.z());
    }

    public double distToCenterSqr(double d, double d2, double d3) {
        double d4 = (double)this.getX() + 0.5 - d;
        double d5 = (double)this.getY() + 0.5 - d2;
        double d6 = (double)this.getZ() + 0.5 - d3;
        return d4 * d4 + d5 * d5 + d6 * d6;
    }

    public double distToLowCornerSqr(double d, double d2, double d3) {
        double d4 = (double)this.getX() - d;
        double d5 = (double)this.getY() - d2;
        double d6 = (double)this.getZ() - d3;
        return d4 * d4 + d5 * d5 + d6 * d6;
    }

    public int distManhattan(Vec3i vec3i) {
        float f = Math.abs(vec3i.getX() - this.getX());
        float f2 = Math.abs(vec3i.getY() - this.getY());
        float f3 = Math.abs(vec3i.getZ() - this.getZ());
        return (int)(f + f2 + f3);
    }

    public int get(Direction.Axis axis) {
        return axis.choose(this.x, this.y, this.z);
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    public String toShortString() {
        return this.getX() + ", " + this.getY() + ", " + this.getZ();
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((Vec3i)object);
    }
}

