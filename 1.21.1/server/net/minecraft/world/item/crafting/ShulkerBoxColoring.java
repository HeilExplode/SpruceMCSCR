/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class ShulkerBoxColoring
extends CustomRecipe {
    public ShulkerBoxColoring(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        int n = 0;
        int n2 = 0;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (Block.byItem(itemStack.getItem()) instanceof ShulkerBoxBlock) {
                ++n;
            } else if (itemStack.getItem() instanceof DyeItem) {
                ++n2;
            } else {
                return false;
            }
            if (n2 <= 1 && n <= 1) continue;
            return false;
        }
        return n == 1 && n2 == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        ItemStack itemStack = ItemStack.EMPTY;
        DyeItem dyeItem = (DyeItem)Items.WHITE_DYE;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack2 = craftingInput.getItem(i);
            if (itemStack2.isEmpty()) continue;
            Item item = itemStack2.getItem();
            if (Block.byItem(item) instanceof ShulkerBoxBlock) {
                itemStack = itemStack2;
                continue;
            }
            if (!(item instanceof DyeItem)) continue;
            dyeItem = (DyeItem)item;
        }
        Block block = ShulkerBoxBlock.getBlockByColor(dyeItem.getDyeColor());
        return itemStack.transmuteCopy(block, 1);
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHULKER_BOX_COLORING;
    }
}

