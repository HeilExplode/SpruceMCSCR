/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class RepairItemRecipe
extends CustomRecipe {
    public RepairItemRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Nullable
    private Pair<ItemStack, ItemStack> getItemsToCombine(CraftingInput craftingInput) {
        ItemStack itemStack = null;
        ItemStack itemStack2 = null;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack3 = craftingInput.getItem(i);
            if (itemStack3.isEmpty()) continue;
            if (itemStack == null) {
                itemStack = itemStack3;
                continue;
            }
            if (itemStack2 == null) {
                itemStack2 = itemStack3;
                continue;
            }
            return null;
        }
        if (itemStack != null && itemStack2 != null && RepairItemRecipe.canCombine(itemStack, itemStack2)) {
            return Pair.of((Object)itemStack, itemStack2);
        }
        return null;
    }

    private static boolean canCombine(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.is(itemStack.getItem()) && itemStack.getCount() == 1 && itemStack2.getCount() == 1 && itemStack.has(DataComponents.MAX_DAMAGE) && itemStack2.has(DataComponents.MAX_DAMAGE) && itemStack.has(DataComponents.DAMAGE) && itemStack2.has(DataComponents.DAMAGE);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        return this.getItemsToCombine(craftingInput) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        Pair<ItemStack, ItemStack> pair = this.getItemsToCombine(craftingInput);
        if (pair == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = (ItemStack)pair.getFirst();
        ItemStack itemStack2 = (ItemStack)pair.getSecond();
        int n = Math.max(itemStack.getMaxDamage(), itemStack2.getMaxDamage());
        int n2 = itemStack.getMaxDamage() - itemStack.getDamageValue();
        int n3 = itemStack2.getMaxDamage() - itemStack2.getDamageValue();
        int n4 = n2 + n3 + n * 5 / 100;
        ItemStack itemStack3 = new ItemStack(itemStack.getItem());
        itemStack3.set(DataComponents.MAX_DAMAGE, n);
        itemStack3.setDamageValue(Math.max(n - n4, 0));
        ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        ItemEnchantments itemEnchantments2 = EnchantmentHelper.getEnchantmentsForCrafting(itemStack2);
        EnchantmentHelper.updateEnchantments(itemStack3, mutable -> provider.lookupOrThrow(Registries.ENCHANTMENT).listElements().filter(reference -> reference.is(EnchantmentTags.CURSE)).forEach(reference -> {
            int n = Math.max(itemEnchantments.getLevel((Holder<Enchantment>)reference), itemEnchantments2.getLevel((Holder<Enchantment>)reference));
            if (n > 0) {
                mutable.upgrade((Holder<Enchantment>)reference, n);
            }
        }));
        return itemStack3;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}

