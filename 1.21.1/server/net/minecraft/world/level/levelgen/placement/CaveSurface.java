/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum CaveSurface implements StringRepresentable
{
    CEILING(Direction.UP, 1, "ceiling"),
    FLOOR(Direction.DOWN, -1, "floor");

    public static final Codec<CaveSurface> CODEC;
    private final Direction direction;
    private final int y;
    private final String id;

    private CaveSurface(Direction direction, int n2, String string2) {
        this.direction = direction;
        this.y = n2;
        this.id = string2;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(CaveSurface::values);
    }
}

