/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public enum Direction implements StringRepresentable
{
    DOWN(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vec3i(1, 0, 0));

    public static final StringRepresentable.EnumCodec<Direction> CODEC;
    public static final Codec<Direction> VERTICAL_CODEC;
    public static final IntFunction<Direction> BY_ID;
    public static final StreamCodec<ByteBuf, Direction> STREAM_CODEC;
    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Axis axis;
    private final AxisDirection axisDirection;
    private final Vec3i normal;
    private static final Direction[] VALUES;
    private static final Direction[] BY_3D_DATA;
    private static final Direction[] BY_2D_DATA;

    private Direction(int n2, int n3, int n4, String string2, AxisDirection axisDirection, Axis axis, Vec3i vec3i) {
        this.data3d = n2;
        this.data2d = n4;
        this.oppositeIndex = n3;
        this.name = string2;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.normal = vec3i;
    }

    public static Direction[] orderedByNearest(Entity entity) {
        Direction direction;
        float f = entity.getViewXRot(1.0f) * ((float)Math.PI / 180);
        float f2 = -entity.getViewYRot(1.0f) * ((float)Math.PI / 180);
        float f3 = Mth.sin(f);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f2);
        float f6 = Mth.cos(f2);
        boolean bl = f5 > 0.0f;
        boolean bl2 = f3 < 0.0f;
        boolean bl3 = f6 > 0.0f;
        float f7 = bl ? f5 : -f5;
        float f8 = bl2 ? -f3 : f3;
        float f9 = bl3 ? f6 : -f6;
        float f10 = f7 * f4;
        float f11 = f9 * f4;
        Direction direction2 = bl ? EAST : WEST;
        Direction direction3 = bl2 ? UP : DOWN;
        Direction direction4 = direction = bl3 ? SOUTH : NORTH;
        if (f7 > f9) {
            if (f8 > f10) {
                return Direction.makeDirectionArray(direction3, direction2, direction);
            }
            if (f11 > f8) {
                return Direction.makeDirectionArray(direction2, direction, direction3);
            }
            return Direction.makeDirectionArray(direction2, direction3, direction);
        }
        if (f8 > f11) {
            return Direction.makeDirectionArray(direction3, direction, direction2);
        }
        if (f10 > f8) {
            return Direction.makeDirectionArray(direction, direction2, direction3);
        }
        return Direction.makeDirectionArray(direction, direction3, direction2);
    }

    private static Direction[] makeDirectionArray(Direction direction, Direction direction2, Direction direction3) {
        return new Direction[]{direction, direction2, direction3, direction3.getOpposite(), direction2.getOpposite(), direction.getOpposite()};
    }

    public static Direction rotate(Matrix4f matrix4f, Direction direction) {
        Vec3i vec3i = direction.getNormal();
        Vector4f vector4f = matrix4f.transform(new Vector4f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), 0.0f));
        return Direction.getNearest(vector4f.x(), vector4f.y(), vector4f.z());
    }

    public static Collection<Direction> allShuffled(RandomSource randomSource) {
        return Util.shuffledCopy(Direction.values(), randomSource);
    }

    public static Stream<Direction> stream() {
        return Stream.of(VALUES);
    }

    public Quaternionf getRotation() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new Quaternionf().rotationX((float)Math.PI);
            case 1 -> new Quaternionf();
            case 2 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, (float)Math.PI);
            case 3 -> new Quaternionf().rotationX(1.5707964f);
            case 4 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, 1.5707964f);
            case 5 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, -1.5707964f);
        };
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public static Direction getFacingAxis(Entity entity, Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (EAST.isFacingAngle(entity.getViewYRot(1.0f))) {
                    yield EAST;
                }
                yield WEST;
            }
            case 2 -> {
                if (SOUTH.isFacingAngle(entity.getViewYRot(1.0f))) {
                    yield SOUTH;
                }
                yield NORTH;
            }
            case 1 -> entity.getViewXRot(1.0f) < 0.0f ? UP : DOWN;
        };
    }

    public Direction getOpposite() {
        return Direction.from3DDataValue(this.oppositeIndex);
    }

    public Direction getClockWise(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getClockWiseX();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getClockWise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.getClockWiseZ();
        };
    }

    public Direction getCounterClockWise(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getCounterClockWiseX();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getCounterClockWise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.getCounterClockWiseZ();
        };
    }

    public Direction getClockWise() {
        return switch (this.ordinal()) {
            case 2 -> EAST;
            case 5 -> SOUTH;
            case 3 -> WEST;
            case 4 -> NORTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getClockWiseX() {
        return switch (this.ordinal()) {
            case 1 -> NORTH;
            case 2 -> DOWN;
            case 0 -> SOUTH;
            case 3 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getCounterClockWiseX() {
        return switch (this.ordinal()) {
            case 1 -> SOUTH;
            case 3 -> DOWN;
            case 0 -> NORTH;
            case 2 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getClockWiseZ() {
        return switch (this.ordinal()) {
            case 1 -> EAST;
            case 5 -> DOWN;
            case 0 -> WEST;
            case 4 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getCounterClockWiseZ() {
        return switch (this.ordinal()) {
            case 1 -> WEST;
            case 4 -> DOWN;
            case 0 -> EAST;
            case 5 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    public Direction getCounterClockWise() {
        return switch (this.ordinal()) {
            case 2 -> WEST;
            case 5 -> NORTH;
            case 3 -> EAST;
            case 4 -> SOUTH;
            default -> throw new IllegalStateException("Unable to get CCW facing of " + String.valueOf(this));
        };
    }

    public int getStepX() {
        return this.normal.getX();
    }

    public int getStepY() {
        return this.normal.getY();
    }

    public int getStepZ() {
        return this.normal.getZ();
    }

    public Vector3f step() {
        return new Vector3f((float)this.getStepX(), (float)this.getStepY(), (float)this.getStepZ());
    }

    public String getName() {
        return this.name;
    }

    public Axis getAxis() {
        return this.axis;
    }

    @Nullable
    public static Direction byName(@Nullable String string) {
        return CODEC.byName(string);
    }

    public static Direction from3DDataValue(int n) {
        return BY_3D_DATA[Mth.abs(n % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int n) {
        return BY_2D_DATA[Mth.abs(n % BY_2D_DATA.length)];
    }

    @Nullable
    public static Direction fromDelta(int n, int n2, int n3) {
        if (n == 0) {
            if (n2 == 0) {
                if (n3 > 0) {
                    return SOUTH;
                }
                if (n3 < 0) {
                    return NORTH;
                }
            } else if (n3 == 0) {
                if (n2 > 0) {
                    return UP;
                }
                return DOWN;
            }
        } else if (n2 == 0 && n3 == 0) {
            if (n > 0) {
                return EAST;
            }
            return WEST;
        }
        return null;
    }

    public static Direction fromYRot(double d) {
        return Direction.from2DDataValue(Mth.floor(d / 90.0 + 0.5) & 3);
    }

    public static Direction fromAxisAndDirection(Axis axis, AxisDirection axisDirection) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (axisDirection == AxisDirection.POSITIVE) {
                    yield EAST;
                }
                yield WEST;
            }
            case 1 -> {
                if (axisDirection == AxisDirection.POSITIVE) {
                    yield UP;
                }
                yield DOWN;
            }
            case 2 -> axisDirection == AxisDirection.POSITIVE ? SOUTH : NORTH;
        };
    }

    public float toYRot() {
        return (this.data2d & 3) * 90;
    }

    public static Direction getRandom(RandomSource randomSource) {
        return Util.getRandom(VALUES, randomSource);
    }

    public static Direction getNearest(double d, double d2, double d3) {
        return Direction.getNearest((float)d, (float)d2, (float)d3);
    }

    public static Direction getNearest(float f, float f2, float f3) {
        Direction direction = NORTH;
        float f4 = Float.MIN_VALUE;
        for (Direction direction2 : VALUES) {
            float f5 = f * (float)direction2.normal.getX() + f2 * (float)direction2.normal.getY() + f3 * (float)direction2.normal.getZ();
            if (!(f5 > f4)) continue;
            f4 = f5;
            direction = direction2;
        }
        return direction;
    }

    public static Direction getNearest(Vec3 vec3) {
        return Direction.getNearest(vec3.x, vec3.y, vec3.z);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private static DataResult<Direction> verifyVertical(Direction direction) {
        return direction.getAxis().isVertical() ? DataResult.success((Object)direction) : DataResult.error(() -> "Expected a vertical direction");
    }

    public static Direction get(AxisDirection axisDirection, Axis axis) {
        for (Direction direction : VALUES) {
            if (direction.getAxisDirection() != axisDirection || direction.getAxis() != axis) continue;
            return direction;
        }
        throw new IllegalArgumentException("No such direction: " + String.valueOf((Object)axisDirection) + " " + String.valueOf(axis));
    }

    public Vec3i getNormal() {
        return this.normal;
    }

    public boolean isFacingAngle(float f) {
        float f2 = f * ((float)Math.PI / 180);
        float f3 = -Mth.sin(f2);
        float f4 = Mth.cos(f2);
        return (float)this.normal.getX() * f3 + (float)this.normal.getZ() * f4 > 0.0f;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Direction::values);
        VERTICAL_CODEC = CODEC.validate(Direction::verifyVertical);
        BY_ID = ByIdMap.continuous(Direction::get3DDataValue, Direction.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Direction::get3DDataValue);
        VALUES = Direction.values();
        BY_3D_DATA = (Direction[])Arrays.stream(VALUES).sorted(Comparator.comparingInt(direction -> direction.data3d)).toArray(Direction[]::new);
        BY_2D_DATA = (Direction[])Arrays.stream(VALUES).filter(direction -> direction.getAxis().isHorizontal()).sorted(Comparator.comparingInt(direction -> direction.data2d)).toArray(Direction[]::new);
    }

    public static enum Axis implements StringRepresentable,
    Predicate<Direction>
    {
        X("x"){

            @Override
            public int choose(int n, int n2, int n3) {
                return n;
            }

            @Override
            public double choose(double d, double d2, double d3) {
                return d;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Y("y"){

            @Override
            public int choose(int n, int n2, int n3) {
                return n2;
            }

            @Override
            public double choose(double d, double d2, double d3) {
                return d2;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Z("z"){

            @Override
            public int choose(int n, int n2, int n3) {
                return n3;
            }

            @Override
            public double choose(double d, double d2, double d3) {
                return d3;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        };

        public static final Axis[] VALUES;
        public static final StringRepresentable.EnumCodec<Axis> CODEC;
        private final String name;

        Axis(String string2) {
            this.name = string2;
        }

        @Nullable
        public static Axis byName(String string) {
            return CODEC.byName(string);
        }

        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public String toString() {
            return this.name;
        }

        public static Axis getRandom(RandomSource randomSource) {
            return Util.getRandom(VALUES, randomSource);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis() == this;
        }

        public Plane getPlane() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> Plane.HORIZONTAL;
                case 1 -> Plane.VERTICAL;
            };
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract int choose(int var1, int var2, int var3);

        public abstract double choose(double var1, double var3, double var5);

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }

        static {
            VALUES = Axis.values();
            CODEC = StringRepresentable.fromEnum(Axis::values);
        }
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int n2, String string2) {
            this.step = n2;
            this.name = string2;
        }

        public int getStep() {
            return this.step;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        public AxisDirection opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public static enum Plane implements Iterable<Direction>,
    Predicate<Direction>
    {
        HORIZONTAL(new Direction[]{NORTH, EAST, SOUTH, WEST}, new Axis[]{Axis.X, Axis.Z}),
        VERTICAL(new Direction[]{UP, DOWN}, new Axis[]{Axis.Y});

        private final Direction[] faces;
        private final Axis[] axis;

        private Plane(Direction[] directionArray, Axis[] axisArray) {
            this.faces = directionArray;
            this.axis = axisArray;
        }

        public Direction getRandomDirection(RandomSource randomSource) {
            return Util.getRandom(this.faces, randomSource);
        }

        public Axis getRandomAxis(RandomSource randomSource) {
            return Util.getRandom(this.axis, randomSource);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis().getPlane() == this;
        }

        @Override
        public Iterator<Direction> iterator() {
            return Iterators.forArray((Object[])this.faces);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.faces);
        }

        public List<Direction> shuffledCopy(RandomSource randomSource) {
            return Util.shuffledCopy(this.faces, randomSource);
        }

        public int length() {
            return this.faces.length;
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }
    }
}

