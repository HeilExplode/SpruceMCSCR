/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
    public static final ResourceLocation ROOT_RECIPE_ADVANCEMENT = ResourceLocation.withDefaultNamespace("recipes/root");

    public RecipeBuilder unlockedBy(String var1, Criterion<?> var2);

    public RecipeBuilder group(@Nullable String var1);

    public Item getResult();

    public void save(RecipeOutput var1, ResourceLocation var2);

    default public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, RecipeBuilder.getDefaultRecipeId(this.getResult()));
    }

    default public void save(RecipeOutput recipeOutput, String string) {
        ResourceLocation resourceLocation = RecipeBuilder.getDefaultRecipeId(this.getResult());
        ResourceLocation resourceLocation2 = ResourceLocation.parse(string);
        if (resourceLocation2.equals(resourceLocation)) {
            throw new IllegalStateException("Recipe " + string + " should remove its 'save' argument as it is equal to default one");
        }
        this.save(recipeOutput, resourceLocation2);
    }

    public static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem());
    }

    public static CraftingBookCategory determineBookCategory(RecipeCategory recipeCategory) {
        return switch (recipeCategory) {
            case RecipeCategory.BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
            case RecipeCategory.TOOLS, RecipeCategory.COMBAT -> CraftingBookCategory.EQUIPMENT;
            case RecipeCategory.REDSTONE -> CraftingBookCategory.REDSTONE;
            default -> CraftingBookCategory.MISC;
        };
    }
}

