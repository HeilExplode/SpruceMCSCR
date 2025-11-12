/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

public class DecoratedPotRecipe
extends CustomRecipe {
    public DecoratedPotRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (!this.canCraftInDimensions(craftingInput.width(), craftingInput.height())) {
            return false;
        }
        block3: for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            switch (i) {
                case 1: 
                case 3: 
                case 5: 
                case 7: {
                    if (itemStack.is(ItemTags.DECORATED_POT_INGREDIENTS)) continue block3;
                    return false;
                }
                default: {
                    if (itemStack.is(Items.AIR)) continue block3;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        PotDecorations potDecorations = new PotDecorations(craftingInput.getItem(1).getItem(), craftingInput.getItem(3).getItem(), craftingInput.getItem(5).getItem(), craftingInput.getItem(7).getItem());
        return DecoratedPotBlockEntity.createDecoratedPotItem(potDecorations);
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n == 3 && n2 == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.DECORATED_POT_RECIPE;
    }
}

