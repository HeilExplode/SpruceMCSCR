/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;

public record DamageImmunity() {
    public static final DamageImmunity INSTANCE = new DamageImmunity();
    public static final Codec<DamageImmunity> CODEC = Codec.unit(() -> INSTANCE);
}

