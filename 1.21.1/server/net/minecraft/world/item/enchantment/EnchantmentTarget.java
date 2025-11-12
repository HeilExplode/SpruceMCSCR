/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EnchantmentTarget implements StringRepresentable
{
    ATTACKER("attacker"),
    DAMAGING_ENTITY("damaging_entity"),
    VICTIM("victim");

    public static final Codec<EnchantmentTarget> CODEC;
    private final String id;

    private EnchantmentTarget(String string2) {
        this.id = string2;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(EnchantmentTarget::values);
    }
}

