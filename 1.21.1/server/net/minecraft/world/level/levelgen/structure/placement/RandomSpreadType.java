/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum RandomSpreadType implements StringRepresentable
{
    LINEAR("linear"),
    TRIANGULAR("triangular");

    public static final Codec<RandomSpreadType> CODEC;
    private final String id;

    private RandomSpreadType(String string2) {
        this.id = string2;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public int evaluate(RandomSource randomSource, int n) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> randomSource.nextInt(n);
            case 1 -> (randomSource.nextInt(n) + randomSource.nextInt(n)) / 2;
        };
    }

    static {
        CODEC = StringRepresentable.fromEnum(RandomSpreadType::values);
    }
}

