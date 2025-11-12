/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public interface CraftingRecipe
extends Recipe<CraftingInput> {
    @Override
    default public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public CraftingBookCategory category();
}

