/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  org.joml.Vector3f
 */
package net.minecraft.world.phys;

import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3f;

public class Vec3
implements Position {
    public static final Codec<Vec3> CODEC = Codec.DOUBLE.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 3).map(list -> new Vec3((Double)list.get(0), (Double)list.get(1), (Double)list.get(2))), vec3 -> List.of(Double.valueOf(vec3.x()), Double.valueOf(vec3.y()), Double.valueOf(vec3.z())));
    public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);
    public final double x;
    public final double y;
    public final double z;

    public static Vec3 fromRGB24(int n) {
        double d = (double)(n >> 16 & 0xFF) / 255.0;
        double d2 = (double)(n >> 8 & 0xFF) / 255.0;
        double d3 = (double)(n & 0xFF) / 255.0;
        return new Vec3(d, d2, d3);
    }

    public static Vec3 atLowerCornerOf(Vec3i vec3i) {
        return new Vec3(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static Vec3 atLowerCornerWithOffset(Vec3i vec3i, double d, double d2, double d3) {
        return new Vec3((double)vec3i.getX() + d, (double)vec3i.getY() + d2, (double)vec3i.getZ() + d3);
    }

    public static Vec3 atCenterOf(Vec3i vec3i) {
        return Vec3.atLowerCornerWithOffset(vec3i, 0.5, 0.5, 0.5);
    }

    public static Vec3 atBottomCenterOf(Vec3i vec3i) {
        return Vec3.atLowerCornerWithOffset(vec3i, 0.5, 0.0, 0.5);
    }

    public static Vec3 upFromBottomCenterOf(Vec3i vec3i, double d) {
        return Vec3.atLowerCornerWithOffset(vec3i, 0.5, d, 0.5);
    }

    public Vec3(double d, double d2, double d3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
    }

    public Vec3(Vector3f vector3f) {
        this(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public Vec3 vectorTo(Vec3 vec3) {
        return new Vec3(vec3.x - this.x, vec3.y - this.y, vec3.z - this.z);
    }

    public Vec3 normalize() {
        double d = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        if (d < 1.0E-4) {
            return ZERO;
        }
        return new Vec3(this.x / d, this.y / d, this.z / d);
    }

    public double dot(Vec3 vec3) {
        return this.x * vec3.x + this.y * vec3.y + this.z * vec3.z;
    }

    public Vec3 cross(Vec3 vec3) {
        return new Vec3(this.y * vec3.z - this.z * vec3.y, this.z * vec3.x - this.x * vec3.z, this.x * vec3.y - this.y * vec3.x);
    }

    public Vec3 subtract(Vec3 vec3) {
        return this.subtract(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 subtract(double d, double d2, double d3) {
        return this.add(-d, -d2, -d3);
    }

    public Vec3 add(Vec3 vec3) {
        return this.add(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 add(double d, double d2, double d3) {
        return new Vec3(this.x + d, this.y + d2, this.z + d3);
    }

    public boolean closerThan(Position position, double d) {
        return this.distanceToSqr(position.x(), position.y(), position.z()) < d * d;
    }

    public double distanceTo(Vec3 vec3) {
        double d = vec3.x - this.x;
        double d2 = vec3.y - this.y;
        double d3 = vec3.z - this.z;
        return Math.sqrt(d * d + d2 * d2 + d3 * d3);
    }

    public double distanceToSqr(Vec3 vec3) {
        double d = vec3.x - this.x;
        double d2 = vec3.y - this.y;
        double d3 = vec3.z - this.z;
        return d * d + d2 * d2 + d3 * d3;
    }

    public double distanceToSqr(double d, double d2, double d3) {
        double d4 = d - this.x;
        double d5 = d2 - this.y;
        double d6 = d3 - this.z;
        return d4 * d4 + d5 * d5 + d6 * d6;
    }

    public boolean closerThan(Vec3 vec3, double d, double d2) {
        double d3 = vec3.x() - this.x;
        double d4 = vec3.y() - this.y;
        double d5 = vec3.z() - this.z;
        return Mth.lengthSquared(d3, d5) < Mth.square(d) && Math.abs(d4) < d2;
    }

    public Vec3 scale(double d) {
        return this.multiply(d, d, d);
    }

    public Vec3 reverse() {
        return this.scale(-1.0);
    }

    public Vec3 multiply(Vec3 vec3) {
        return this.multiply(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 multiply(double d, double d2, double d3) {
        return new Vec3(this.x * d, this.y * d2, this.z * d3);
    }

    public Vec3 offsetRandom(RandomSource randomSource, float f) {
        return this.add((randomSource.nextFloat() - 0.5f) * f, (randomSource.nextFloat() - 0.5f) * f, (randomSource.nextFloat() - 0.5f) * f);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSqr() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double horizontalDistance() {
        return Math.sqrt(this.x * this.x + this.z * this.z);
    }

    public double horizontalDistanceSqr() {
        return this.x * this.x + this.z * this.z;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Vec3)) {
            return false;
        }
        Vec3 vec3 = (Vec3)object;
        if (Double.compare(vec3.x, this.x) != 0) {
            return false;
        }
        if (Double.compare(vec3.y, this.y) != 0) {
            return false;
        }
        return Double.compare(vec3.z, this.z) == 0;
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(this.x);
        int n = (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.y);
        n = 31 * n + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.z);
        n = 31 * n + (int)(l ^ l >>> 32);
        return n;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public Vec3 lerp(Vec3 vec3, double d) {
        return new Vec3(Mth.lerp(d, this.x, vec3.x), Mth.lerp(d, this.y, vec3.y), Mth.lerp(d, this.z, vec3.z));
    }

    public Vec3 xRot(float f) {
        float f2 = Mth.cos(f);
        float f3 = Mth.sin(f);
        double d = this.x;
        double d2 = this.y * (double)f2 + this.z * (double)f3;
        double d3 = this.z * (double)f2 - this.y * (double)f3;
        return new Vec3(d, d2, d3);
    }

    public Vec3 yRot(float f) {
        float f2 = Mth.cos(f);
        float f3 = Mth.sin(f);
        double d = this.x * (double)f2 + this.z * (double)f3;
        double d2 = this.y;
        double d3 = this.z * (double)f2 - this.x * (double)f3;
        return new Vec3(d, d2, d3);
    }

    public Vec3 zRot(float f) {
        float f2 = Mth.cos(f);
        float f3 = Mth.sin(f);
        double d = this.x * (double)f2 + this.y * (double)f3;
        double d2 = this.y * (double)f2 - this.x * (double)f3;
        double d3 = this.z;
        return new Vec3(d, d2, d3);
    }

    public static Vec3 directionFromRotation(Vec2 vec2) {
        return Vec3.directionFromRotation(vec2.x, vec2.y);
    }

    public static Vec3 directionFromRotation(float f, float f2) {
        float f3 = Mth.cos(-f2 * ((float)Math.PI / 180) - (float)Math.PI);
        float f4 = Mth.sin(-f2 * ((float)Math.PI / 180) - (float)Math.PI);
        float f5 = -Mth.cos(-f * ((float)Math.PI / 180));
        float f6 = Mth.sin(-f * ((float)Math.PI / 180));
        return new Vec3(f4 * f5, f6, f3 * f5);
    }

    public Vec3 align(EnumSet<Direction.Axis> enumSet) {
        double d = enumSet.contains(Direction.Axis.X) ? (double)Mth.floor(this.x) : this.x;
        double d2 = enumSet.contains(Direction.Axis.Y) ? (double)Mth.floor(this.y) : this.y;
        double d3 = enumSet.contains(Direction.Axis.Z) ? (double)Mth.floor(this.z) : this.z;
        return new Vec3(d, d2, d3);
    }

    public double get(Direction.Axis axis) {
        return axis.choose(this.x, this.y, this.z);
    }

    public Vec3 with(Direction.Axis axis, double d) {
        double d2 = axis == Direction.Axis.X ? d : this.x;
        double d3 = axis == Direction.Axis.Y ? d : this.y;
        double d4 = axis == Direction.Axis.Z ? d : this.z;
        return new Vec3(d2, d3, d4);
    }

    public Vec3 relative(Direction direction, double d) {
        Vec3i vec3i = direction.getNormal();
        return new Vec3(this.x + d * (double)vec3i.getX(), this.y + d * (double)vec3i.getY(), this.z + d * (double)vec3i.getZ());
    }

    @Override
    public final double x() {
        return this.x;
    }

    @Override
    public final double y() {
        return this.y;
    }

    @Override
    public final double z() {
        return this.z;
    }

    public Vector3f toVector3f() {
        return new Vector3f((float)this.x, (float)this.y, (float)this.z);
    }
}

