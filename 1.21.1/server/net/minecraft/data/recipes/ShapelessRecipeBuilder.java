/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder
implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
    @Nullable
    private String group;

    public ShapelessRecipeBuilder(RecipeCategory recipeCategory, ItemLike itemLike, int n) {
        this.category = recipeCategory;
        this.result = itemLike.asItem();
        this.count = n;
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory recipeCategory, ItemLike itemLike) {
        return new ShapelessRecipeBuilder(recipeCategory, itemLike, 1);
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory recipeCategory, ItemLike itemLike, int n) {
        return new ShapelessRecipeBuilder(recipeCategory, itemLike, n);
    }

    public ShapelessRecipeBuilder requires(TagKey<Item> tagKey) {
        return this.requires(Ingredient.of(tagKey));
    }

    public ShapelessRecipeBuilder requires(ItemLike itemLike) {
        return this.requires(itemLike, 1);
    }

    public ShapelessRecipeBuilder requires(ItemLike itemLike, int n) {
        for (int i = 0; i < n; ++i) {
            this.requires(Ingredient.of(itemLike));
        }
        return this;
    }

    public ShapelessRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ShapelessRecipeBuilder requires(Ingredient ingredient, int n) {
        for (int i = 0; i < n; ++i) {
            this.ingredients.add(ingredient);
        }
        return this;
    }

    @Override
    public ShapelessRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
        this.criteria.put(string, criterion);
        return this;
    }

    @Override
    public ShapelessRecipeBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        this.ensureValid(resourceLocation);
        Advancement.Builder builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), new ItemStack(this.result, this.count), this.ingredients);
        recipeOutput.accept(resourceLocation, shapelessRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation resourceLocation) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf(resourceLocation));
        }
    }

    @Override
    public /* synthetic */ RecipeBuilder group(@Nullable String string) {
        return this.group(string);
    }

    public /* synthetic */ RecipeBuilder unlockedBy(String string, Criterion criterion) {
        return this.unlockedBy(string, criterion);
    }
}

