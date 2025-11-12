/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.world.phys;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class AABB {
    private static final double EPSILON = 1.0E-7;
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AABB(double d, double d2, double d3, double d4, double d5, double d6) {
        this.minX = Math.min(d, d4);
        this.minY = Math.min(d2, d5);
        this.minZ = Math.min(d3, d6);
        this.maxX = Math.max(d, d4);
        this.maxY = Math.max(d2, d5);
        this.maxZ = Math.max(d3, d6);
    }

    public AABB(BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);
    }

    public AABB(Vec3 vec3, Vec3 vec32) {
        this(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
    }

    public static AABB of(BoundingBox boundingBox) {
        return new AABB(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX() + 1, boundingBox.maxY() + 1, boundingBox.maxZ() + 1);
    }

    public static AABB unitCubeFromLowerCorner(Vec3 vec3) {
        return new AABB(vec3.x, vec3.y, vec3.z, vec3.x + 1.0, vec3.y + 1.0, vec3.z + 1.0);
    }

    public static AABB encapsulatingFullBlocks(BlockPos blockPos, BlockPos blockPos2) {
        return new AABB(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()), Math.max(blockPos.getX(), blockPos2.getX()) + 1, Math.max(blockPos.getY(), blockPos2.getY()) + 1, Math.max(blockPos.getZ(), blockPos2.getZ()) + 1);
    }

    public AABB setMinX(double d) {
        return new AABB(d, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AABB setMinY(double d) {
        return new AABB(this.minX, d, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AABB setMinZ(double d) {
        return new AABB(this.minX, this.minY, d, this.maxX, this.maxY, this.maxZ);
    }

    public AABB setMaxX(double d) {
        return new AABB(this.minX, this.minY, this.minZ, d, this.maxY, this.maxZ);
    }

    public AABB setMaxY(double d) {
        return new AABB(this.minX, this.minY, this.minZ, this.maxX, d, this.maxZ);
    }

    public AABB setMaxZ(double d) {
        return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, d);
    }

    public double min(Direction.Axis axis) {
        return axis.choose(this.minX, this.minY, this.minZ);
    }

    public double max(Direction.Axis axis) {
        return axis.choose(this.maxX, this.maxY, this.maxZ);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AABB)) {
            return false;
        }
        AABB aABB = (AABB)object;
        if (Double.compare(aABB.minX, this.minX) != 0) {
            return false;
        }
        if (Double.compare(aABB.minY, this.minY) != 0) {
            return false;
        }
        if (Double.compare(aABB.minZ, this.minZ) != 0) {
            return false;
        }
        if (Double.compare(aABB.maxX, this.maxX) != 0) {
            return false;
        }
        if (Double.compare(aABB.maxY, this.maxY) != 0) {
            return false;
        }
        return Double.compare(aABB.maxZ, this.maxZ) == 0;
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(this.minX);
        int n = (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.minY);
        n = 31 * n + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.minZ);
        n = 31 * n + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxX);
        n = 31 * n + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxY);
        n = 31 * n + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxZ);
        n = 31 * n + (int)(l ^ l >>> 32);
        return n;
    }

    public AABB contract(double d, double d2, double d3) {
        double d4 = this.minX;
        double d5 = this.minY;
        double d6 = this.minZ;
        double d7 = this.maxX;
        double d8 = this.maxY;
        double d9 = this.maxZ;
        if (d < 0.0) {
            d4 -= d;
        } else if (d > 0.0) {
            d7 -= d;
        }
        if (d2 < 0.0) {
            d5 -= d2;
        } else if (d2 > 0.0) {
            d8 -= d2;
        }
        if (d3 < 0.0) {
            d6 -= d3;
        } else if (d3 > 0.0) {
            d9 -= d3;
        }
        return new AABB(d4, d5, d6, d7, d8, d9);
    }

    public AABB expandTowards(Vec3 vec3) {
        return this.expandTowards(vec3.x, vec3.y, vec3.z);
    }

    public AABB expandTowards(double d, double d2, double d3) {
        double d4 = this.minX;
        double d5 = this.minY;
        double d6 = this.minZ;
        double d7 = this.maxX;
        double d8 = this.maxY;
        double d9 = this.maxZ;
        if (d < 0.0) {
            d4 += d;
        } else if (d > 0.0) {
            d7 += d;
        }
        if (d2 < 0.0) {
            d5 += d2;
        } else if (d2 > 0.0) {
            d8 += d2;
        }
        if (d3 < 0.0) {
            d6 += d3;
        } else if (d3 > 0.0) {
            d9 += d3;
        }
        return new AABB(d4, d5, d6, d7, d8, d9);
    }

    public AABB inflate(double d, double d2, double d3) {
        double d4 = this.minX - d;
        double d5 = this.minY - d2;
        double d6 = this.minZ - d3;
        double d7 = this.maxX + d;
        double d8 = this.maxY + d2;
        double d9 = this.maxZ + d3;
        return new AABB(d4, d5, d6, d7, d8, d9);
    }

    public AABB inflate(double d) {
        return this.inflate(d, d, d);
    }

    public AABB intersect(AABB aABB) {
        double d = Math.max(this.minX, aABB.minX);
        double d2 = Math.max(this.minY, aABB.minY);
        double d3 = Math.max(this.minZ, aABB.minZ);
        double d4 = Math.min(this.maxX, aABB.maxX);
        double d5 = Math.min(this.maxY, aABB.maxY);
        double d6 = Math.min(this.maxZ, aABB.maxZ);
        return new AABB(d, d2, d3, d4, d5, d6);
    }

    public AABB minmax(AABB aABB) {
        double d = Math.min(this.minX, aABB.minX);
        double d2 = Math.min(this.minY, aABB.minY);
        double d3 = Math.min(this.minZ, aABB.minZ);
        double d4 = Math.max(this.maxX, aABB.maxX);
        double d5 = Math.max(this.maxY, aABB.maxY);
        double d6 = Math.max(this.maxZ, aABB.maxZ);
        return new AABB(d, d2, d3, d4, d5, d6);
    }

    public AABB move(double d, double d2, double d3) {
        return new AABB(this.minX + d, this.minY + d2, this.minZ + d3, this.maxX + d, this.maxY + d2, this.maxZ + d3);
    }

    public AABB move(BlockPos blockPos) {
        return new AABB(this.minX + (double)blockPos.getX(), this.minY + (double)blockPos.getY(), this.minZ + (double)blockPos.getZ(), this.maxX + (double)blockPos.getX(), this.maxY + (double)blockPos.getY(), this.maxZ + (double)blockPos.getZ());
    }

    public AABB move(Vec3 vec3) {
        return this.move(vec3.x, vec3.y, vec3.z);
    }

    public AABB move(Vector3f vector3f) {
        return this.move(vector3f.x, vector3f.y, vector3f.z);
    }

    public boolean intersects(AABB aABB) {
        return this.intersects(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
    }

    public boolean intersects(double d, double d2, double d3, double d4, double d5, double d6) {
        return this.minX < d4 && this.maxX > d && this.minY < d5 && this.maxY > d2 && this.minZ < d6 && this.maxZ > d3;
    }

    public boolean intersects(Vec3 vec3, Vec3 vec32) {
        return this.intersects(Math.min(vec3.x, vec32.x), Math.min(vec3.y, vec32.y), Math.min(vec3.z, vec32.z), Math.max(vec3.x, vec32.x), Math.max(vec3.y, vec32.y), Math.max(vec3.z, vec32.z));
    }

    public boolean contains(Vec3 vec3) {
        return this.contains(vec3.x, vec3.y, vec3.z);
    }

    public boolean contains(double d, double d2, double d3) {
        return d >= this.minX && d < this.maxX && d2 >= this.minY && d2 < this.maxY && d3 >= this.minZ && d3 < this.maxZ;
    }

    public double getSize() {
        double d = this.getXsize();
        double d2 = this.getYsize();
        double d3 = this.getZsize();
        return (d + d2 + d3) / 3.0;
    }

    public double getXsize() {
        return this.maxX - this.minX;
    }

    public double getYsize() {
        return this.maxY - this.minY;
    }

    public double getZsize() {
        return this.maxZ - this.minZ;
    }

    public AABB deflate(double d, double d2, double d3) {
        return this.inflate(-d, -d2, -d3);
    }

    public AABB deflate(double d) {
        return this.inflate(-d);
    }

    public Optional<Vec3> clip(Vec3 vec3, Vec3 vec32) {
        double[] dArray = new double[]{1.0};
        double d = vec32.x - vec3.x;
        double d2 = vec32.y - vec3.y;
        double d3 = vec32.z - vec3.z;
        Direction direction = AABB.getDirection(this, vec3, dArray, null, d, d2, d3);
        if (direction == null) {
            return Optional.empty();
        }
        double d4 = dArray[0];
        return Optional.of(vec3.add(d4 * d, d4 * d2, d4 * d3));
    }

    @Nullable
    public static BlockHitResult clip(Iterable<AABB> iterable, Vec3 vec3, Vec3 vec32, BlockPos blockPos) {
        double[] dArray = new double[]{1.0};
        Direction direction = null;
        double d = vec32.x - vec3.x;
        double d2 = vec32.y - vec3.y;
        double d3 = vec32.z - vec3.z;
        for (AABB aABB : iterable) {
            direction = AABB.getDirection(aABB.move(blockPos), vec3, dArray, direction, d, d2, d3);
        }
        if (direction == null) {
            return null;
        }
        double d4 = dArray[0];
        return new BlockHitResult(vec3.add(d4 * d, d4 * d2, d4 * d3), direction, blockPos, false);
    }

    @Nullable
    private static Direction getDirection(AABB aABB, Vec3 vec3, double[] dArray, @Nullable Direction direction, double d, double d2, double d3) {
        if (d > 1.0E-7) {
            direction = AABB.clipPoint(dArray, direction, d, d2, d3, aABB.minX, aABB.minY, aABB.maxY, aABB.minZ, aABB.maxZ, Direction.WEST, vec3.x, vec3.y, vec3.z);
        } else if (d < -1.0E-7) {
            direction = AABB.clipPoint(dArray, direction, d, d2, d3, aABB.maxX, aABB.minY, aABB.maxY, aABB.minZ, aABB.maxZ, Direction.EAST, vec3.x, vec3.y, vec3.z);
        }
        if (d2 > 1.0E-7) {
            direction = AABB.clipPoint(dArray, direction, d2, d3, d, aABB.minY, aABB.minZ, aABB.maxZ, aABB.minX, aABB.maxX, Direction.DOWN, vec3.y, vec3.z, vec3.x);
        } else if (d2 < -1.0E-7) {
            direction = AABB.clipPoint(dArray, direction, d2, d3, d, aABB.maxY, aABB.minZ, aABB.maxZ, aABB.minX, aABB.maxX, Direction.UP, vec3.y, vec3.z, vec3.x);
        }
        if (d3 > 1.0E-7) {
            direction = AABB.clipPoint(dArray, direction, d3, d, d2, aABB.minZ, aABB.minX, aABB.maxX, aABB.minY, aABB.maxY, Direction.NORTH, vec3.z, vec3.x, vec3.y);
        } else if (d3 < -1.0E-7) {
            direction = AABB.clipPoint(dArray, direction, d3, d, d2, aABB.maxZ, aABB.minX, aABB.maxX, aABB.minY, aABB.maxY, Direction.SOUTH, vec3.z, vec3.x, vec3.y);
        }
        return direction;
    }

    @Nullable
    private static Direction clipPoint(double[] dArray, @Nullable Direction direction, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, Direction direction2, double d9, double d10, double d11) {
        double d12 = (d4 - d9) / d;
        double d13 = d10 + d12 * d2;
        double d14 = d11 + d12 * d3;
        if (0.0 < d12 && d12 < dArray[0] && d5 - 1.0E-7 < d13 && d13 < d6 + 1.0E-7 && d7 - 1.0E-7 < d14 && d14 < d8 + 1.0E-7) {
            dArray[0] = d12;
            return direction2;
        }
        return direction;
    }

    public double distanceToSqr(Vec3 vec3) {
        double d = Math.max(Math.max(this.minX - vec3.x, vec3.x - this.maxX), 0.0);
        double d2 = Math.max(Math.max(this.minY - vec3.y, vec3.y - this.maxY), 0.0);
        double d3 = Math.max(Math.max(this.minZ - vec3.z, vec3.z - this.maxZ), 0.0);
        return Mth.lengthSquared(d, d2, d3);
    }

    public String toString() {
        return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public boolean hasNaN() {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
    }

    public Vec3 getCenter() {
        return new Vec3(Mth.lerp(0.5, this.minX, this.maxX), Mth.lerp(0.5, this.minY, this.maxY), Mth.lerp(0.5, this.minZ, this.maxZ));
    }

    public Vec3 getBottomCenter() {
        return new Vec3(Mth.lerp(0.5, this.minX, this.maxX), this.minY, Mth.lerp(0.5, this.minZ, this.maxZ));
    }

    public Vec3 getMinPosition() {
        return new Vec3(this.minX, this.minY, this.minZ);
    }

    public Vec3 getMaxPosition() {
        return new Vec3(this.maxX, this.maxY, this.maxZ);
    }

    public static AABB ofSize(Vec3 vec3, double d, double d2, double d3) {
        return new AABB(vec3.x - d / 2.0, vec3.y - d2 / 2.0, vec3.z - d3 / 2.0, vec3.x + d / 2.0, vec3.y + d2 / 2.0, vec3.z + d3 / 2.0);
    }
}

