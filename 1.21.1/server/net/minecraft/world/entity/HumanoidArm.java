/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

public enum HumanoidArm implements OptionEnum,
StringRepresentable
{
    LEFT(0, "left", "options.mainHand.left"),
    RIGHT(1, "right", "options.mainHand.right");

    public static final Codec<HumanoidArm> CODEC;
    public static final IntFunction<HumanoidArm> BY_ID;
    private final int id;
    private final String name;
    private final String translationKey;

    private HumanoidArm(int n2, String string2, String string3) {
        this.id = n2;
        this.name = string2;
        this.translationKey = string3;
    }

    public HumanoidArm getOpposite() {
        if (this == LEFT) {
            return RIGHT;
        }
        return LEFT;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.translationKey;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(HumanoidArm::values);
        BY_ID = ByIdMap.continuous(HumanoidArm::getId, HumanoidArm.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    }
}

