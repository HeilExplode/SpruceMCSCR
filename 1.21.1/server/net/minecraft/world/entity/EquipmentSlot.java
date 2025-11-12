/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public enum EquipmentSlot implements StringRepresentable
{
    MAINHAND(Type.HAND, 0, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, 5, "offhand"),
    FEET(Type.HUMANOID_ARMOR, 0, 1, 1, "feet"),
    LEGS(Type.HUMANOID_ARMOR, 1, 1, 2, "legs"),
    CHEST(Type.HUMANOID_ARMOR, 2, 1, 3, "chest"),
    HEAD(Type.HUMANOID_ARMOR, 3, 1, 4, "head"),
    BODY(Type.ANIMAL_ARMOR, 0, 1, 6, "body");

    public static final int NO_COUNT_LIMIT = 0;
    public static final StringRepresentable.EnumCodec<EquipmentSlot> CODEC;
    private final Type type;
    private final int index;
    private final int countLimit;
    private final int filterFlag;
    private final String name;

    private EquipmentSlot(Type type, int n2, int n3, int n4, String string2) {
        this.type = type;
        this.index = n2;
        this.countLimit = n3;
        this.filterFlag = n4;
        this.name = string2;
    }

    private EquipmentSlot(Type type, int n2, int n3, String string2) {
        this(type, n2, 0, n3, string2);
    }

    public Type getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getIndex(int n) {
        return n + this.index;
    }

    public ItemStack limit(ItemStack itemStack) {
        return this.countLimit > 0 ? itemStack.split(this.countLimit) : itemStack;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == Type.HUMANOID_ARMOR || this.type == Type.ANIMAL_ARMOR;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static EquipmentSlot byName(String string) {
        EquipmentSlot equipmentSlot = CODEC.byName(string);
        if (equipmentSlot != null) {
            return equipmentSlot;
        }
        throw new IllegalArgumentException("Invalid slot '" + string + "'");
    }

    static {
        CODEC = StringRepresentable.fromEnum(EquipmentSlot::values);
    }

    public static enum Type {
        HAND,
        HUMANOID_ARMOR,
        ANIMAL_ARMOR;

    }
}

