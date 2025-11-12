/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;

public enum Mirror implements StringRepresentable
{
    NONE("none", OctahedralGroup.IDENTITY),
    LEFT_RIGHT("left_right", OctahedralGroup.INVERT_Z),
    FRONT_BACK("front_back", OctahedralGroup.INVERT_X);

    public static final Codec<Mirror> CODEC;
    private final String id;
    private final Component symbol;
    private final OctahedralGroup rotation;

    private Mirror(String string2, OctahedralGroup octahedralGroup) {
        this.id = string2;
        this.symbol = Component.translatable("mirror." + string2);
        this.rotation = octahedralGroup;
    }

    public int mirror(int n, int n2) {
        int n3 = n2 / 2;
        int n4 = n > n3 ? n - n2 : n;
        switch (this.ordinal()) {
            case 2: {
                return (n2 - n4) % n2;
            }
            case 1: {
                return (n3 - n4 + n2) % n2;
            }
        }
        return n;
    }

    public Rotation getRotation(Direction direction) {
        Direction.Axis axis = direction.getAxis();
        return this == LEFT_RIGHT && axis == Direction.Axis.Z || this == FRONT_BACK && axis == Direction.Axis.X ? Rotation.CLOCKWISE_180 : Rotation.NONE;
    }

    public Direction mirror(Direction direction) {
        if (this == FRONT_BACK && direction.getAxis() == Direction.Axis.X) {
            return direction.getOpposite();
        }
        if (this == LEFT_RIGHT && direction.getAxis() == Direction.Axis.Z) {
            return direction.getOpposite();
        }
        return direction;
    }

    public OctahedralGroup rotation() {
        return this.rotation;
    }

    public Component symbol() {
        return this.symbol;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Mirror::values);
    }
}

