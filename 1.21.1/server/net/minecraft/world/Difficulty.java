/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum Difficulty implements StringRepresentable
{
    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    public static final StringRepresentable.EnumCodec<Difficulty> CODEC;
    private static final IntFunction<Difficulty> BY_ID;
    private final int id;
    private final String key;

    private Difficulty(int n2, String string2) {
        this.id = n2;
        this.key = string2;
    }

    public int getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return Component.translatable("options.difficulty." + this.key);
    }

    public Component getInfo() {
        return Component.translatable("options.difficulty." + this.key + ".info");
    }

    public static Difficulty byId(int n) {
        return BY_ID.apply(n);
    }

    @Nullable
    public static Difficulty byName(String string) {
        return CODEC.byName(string);
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String getSerializedName() {
        return this.key;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Difficulty::values);
        BY_ID = ByIdMap.continuous(Difficulty::getId, Difficulty.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

