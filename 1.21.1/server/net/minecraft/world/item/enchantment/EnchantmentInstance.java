/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentInstance
extends WeightedEntry.IntrusiveBase {
    public final Holder<Enchantment> enchantment;
    public final int level;

    public EnchantmentInstance(Holder<Enchantment> holder, int n) {
        super(holder.value().getWeight());
        this.enchantment = holder;
        this.level = n;
    }
}

