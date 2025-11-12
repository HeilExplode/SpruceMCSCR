/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class EnchantmentTagsProvider
extends TagsProvider<Enchantment> {
    public EnchantmentTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registries.ENCHANTMENT, completableFuture);
    }

    protected void tooltipOrder(HolderLookup.Provider provider, ResourceKey<Enchantment> ... resourceKeyArray) {
        this.tag(EnchantmentTags.TOOLTIP_ORDER).add(resourceKeyArray);
        Set<ResourceKey<Enchantment>> set = Set.of(resourceKeyArray);
        List list = provider.lookupOrThrow(Registries.ENCHANTMENT).listElements().filter(reference -> !set.contains(reference.unwrapKey().get())).map(Holder::getRegisteredName).collect(Collectors.toList());
        if (!list.isEmpty()) {
            throw new IllegalStateException("Not all enchantments were registered for tooltip ordering. Missing: " + String.join((CharSequence)", ", list));
        }
    }
}

