/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public abstract class RecipeBookMenu<I extends RecipeInput, R extends Recipe<I>>
extends AbstractContainerMenu {
    public RecipeBookMenu(MenuType<?> menuType, int n) {
        super(menuType, n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handlePlacement(boolean bl, RecipeHolder<?> recipeHolder, ServerPlayer serverPlayer) {
        RecipeHolder<?> recipeHolder2 = recipeHolder;
        this.beginPlacingRecipe();
        try {
            new ServerPlaceRecipe(this).recipeClicked(serverPlayer, recipeHolder2, bl);
        }
        finally {
            this.finishPlacingRecipe(recipeHolder2);
        }
    }

    protected void beginPlacingRecipe() {
    }

    protected void finishPlacingRecipe(RecipeHolder<R> recipeHolder) {
    }

    public abstract void fillCraftSlotsStackedContents(StackedContents var1);

    public abstract void clearCraftingContent();

    public abstract boolean recipeMatches(RecipeHolder<R> var1);

    public abstract int getResultSlotIndex();

    public abstract int getGridWidth();

    public abstract int getGridHeight();

    public abstract int getSize();

    public abstract RecipeBookType getRecipeBookType();

    public abstract boolean shouldMoveToInventory(int var1);
}

