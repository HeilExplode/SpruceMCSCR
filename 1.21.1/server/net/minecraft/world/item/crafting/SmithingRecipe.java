/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.block.Blocks;

public interface SmithingRecipe
extends Recipe<SmithingRecipeInput> {
    @Override
    default public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    default public boolean canCraftInDimensions(int n, int n2) {
        return n >= 3 && n2 >= 1;
    }

    @Override
    default public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    public boolean isTemplateIngredient(ItemStack var1);

    public boolean isBaseIngredient(ItemStack var1);

    public boolean isAdditionIngredient(ItemStack var1);
}

