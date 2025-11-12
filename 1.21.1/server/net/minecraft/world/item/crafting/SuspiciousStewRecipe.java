/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewRecipe
extends CustomRecipe {
    public SuspiciousStewRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.is(Blocks.BROWN_MUSHROOM.asItem()) && !bl3) {
                bl3 = true;
                continue;
            }
            if (itemStack.is(Blocks.RED_MUSHROOM.asItem()) && !bl2) {
                bl2 = true;
                continue;
            }
            if (itemStack.is(ItemTags.SMALL_FLOWERS) && !bl) {
                bl = true;
                continue;
            }
            if (itemStack.is(Items.BOWL) && !bl4) {
                bl4 = true;
                continue;
            }
            return false;
        }
        return bl && bl3 && bl2 && bl4;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
        for (int i = 0; i < craftingInput.size(); ++i) {
            SuspiciousEffectHolder suspiciousEffectHolder;
            ItemStack itemStack2 = craftingInput.getItem(i);
            if (itemStack2.isEmpty() || (suspiciousEffectHolder = SuspiciousEffectHolder.tryGet(itemStack2.getItem())) == null) continue;
            itemStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, suspiciousEffectHolder.getSuspiciousEffects());
            break;
        }
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n >= 2 && n2 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SUSPICIOUS_STEW;
    }
}

