/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentPredicate(Optional<HolderSet<Enchantment>> enchantments, MinMaxBounds.Ints level) {
    public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", (Object)MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)).apply((Applicative)instance, EnchantmentPredicate::new));

    public EnchantmentPredicate(Holder<Enchantment> holder, MinMaxBounds.Ints ints) {
        this(Optional.of(HolderSet.direct(holder)), ints);
    }

    public EnchantmentPredicate(HolderSet<Enchantment> holderSet, MinMaxBounds.Ints ints) {
        this(Optional.of(holderSet), ints);
    }

    public boolean containedIn(ItemEnchantments itemEnchantments) {
        if (this.enchantments.isPresent()) {
            for (Holder holder : this.enchantments.get()) {
                if (!this.matchesEnchantment(itemEnchantments, holder)) continue;
                return true;
            }
            return false;
        }
        if (this.level != MinMaxBounds.Ints.ANY) {
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                if (!this.level.matches(entry.getIntValue())) continue;
                return true;
            }
            return false;
        }
        return !itemEnchantments.isEmpty();
    }

    private boolean matchesEnchantment(ItemEnchantments itemEnchantments, Holder<Enchantment> holder) {
        int n = itemEnchantments.getLevel(holder);
        if (n == 0) {
            return false;
        }
        if (this.level == MinMaxBounds.Ints.ANY) {
            return true;
        }
        return this.level.matches(n);
    }
}

