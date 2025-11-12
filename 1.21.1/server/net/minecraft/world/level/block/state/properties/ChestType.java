/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum ChestType implements StringRepresentable
{
    SINGLE("single"),
    LEFT("left"),
    RIGHT("right");

    private final String name;

    private ChestType(String string2) {
        this.name = string2;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public ChestType getOpposite() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> SINGLE;
            case 1 -> RIGHT;
            case 2 -> LEFT;
        };
    }
}

