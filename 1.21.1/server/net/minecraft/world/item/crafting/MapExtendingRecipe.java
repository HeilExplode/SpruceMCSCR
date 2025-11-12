/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapExtendingRecipe
extends ShapedRecipe {
    public MapExtendingRecipe(CraftingBookCategory craftingBookCategory) {
        super("", craftingBookCategory, ShapedRecipePattern.of(Map.of(Character.valueOf('#'), Ingredient.of(Items.PAPER), Character.valueOf('x'), Ingredient.of(Items.FILLED_MAP)), "###", "#x#", "###"), new ItemStack(Items.MAP));
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (!super.matches(craftingInput, level)) {
            return false;
        }
        ItemStack itemStack = MapExtendingRecipe.findFilledMap(craftingInput);
        if (itemStack.isEmpty()) {
            return false;
        }
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData == null) {
            return false;
        }
        if (mapItemSavedData.isExplorationMap()) {
            return false;
        }
        return mapItemSavedData.scale < 4;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        ItemStack itemStack = MapExtendingRecipe.findFilledMap(craftingInput).copyWithCount(1);
        itemStack.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
        return itemStack;
    }

    private static ItemStack findFilledMap(CraftingInput craftingInput) {
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (!itemStack.is(Items.FILLED_MAP)) continue;
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_EXTENDING;
    }
}

