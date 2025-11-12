/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum Rotation implements StringRepresentable
{
    NONE("none", OctahedralGroup.IDENTITY),
    CLOCKWISE_90("clockwise_90", OctahedralGroup.ROT_90_Y_NEG),
    CLOCKWISE_180("180", OctahedralGroup.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90("counterclockwise_90", OctahedralGroup.ROT_90_Y_POS);

    public static final Codec<Rotation> CODEC;
    private final String id;
    private final OctahedralGroup rotation;

    private Rotation(String string2, OctahedralGroup octahedralGroup) {
        this.id = string2;
        this.rotation = octahedralGroup;
    }

    public Rotation getRotated(Rotation rotation) {
        switch (rotation.ordinal()) {
            case 2: {
                switch (this.ordinal()) {
                    case 0: {
                        return CLOCKWISE_180;
                    }
                    case 1: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case 2: {
                        return NONE;
                    }
                    case 3: {
                        return CLOCKWISE_90;
                    }
                }
            }
            case 3: {
                switch (this.ordinal()) {
                    case 0: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case 1: {
                        return NONE;
                    }
                    case 2: {
                        return CLOCKWISE_90;
                    }
                    case 3: {
                        return CLOCKWISE_180;
                    }
                }
            }
            case 1: {
                switch (this.ordinal()) {
                    case 0: {
                        return CLOCKWISE_90;
                    }
                    case 1: {
                        return CLOCKWISE_180;
                    }
                    case 2: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case 3: {
                        return NONE;
                    }
                }
            }
        }
        return this;
    }

    public OctahedralGroup rotation() {
        return this.rotation;
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return direction;
        }
        switch (this.ordinal()) {
            case 2: {
                return direction.getOpposite();
            }
            case 3: {
                return direction.getCounterClockWise();
            }
            case 1: {
                return direction.getClockWise();
            }
        }
        return direction;
    }

    public int rotate(int n, int n2) {
        switch (this.ordinal()) {
            case 2: {
                return (n + n2 / 2) % n2;
            }
            case 3: {
                return (n + n2 * 3 / 4) % n2;
            }
            case 1: {
                return (n + n2 / 4) % n2;
            }
        }
        return n;
    }

    public static Rotation getRandom(RandomSource randomSource) {
        return Util.getRandom(Rotation.values(), randomSource);
    }

    public static List<Rotation> getShuffled(RandomSource randomSource) {
        return Util.shuffledCopy(Rotation.values(), randomSource);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Rotation::values);
    }
}

