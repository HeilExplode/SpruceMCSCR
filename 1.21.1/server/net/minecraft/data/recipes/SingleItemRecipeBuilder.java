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
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder
implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final Ingredient ingredient;
    private final int count;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
    @Nullable
    private String group;
    private final SingleItemRecipe.Factory<?> factory;

    public SingleItemRecipeBuilder(RecipeCategory recipeCategory, SingleItemRecipe.Factory<?> factory, Ingredient ingredient, ItemLike itemLike, int n) {
        this.category = recipeCategory;
        this.factory = factory;
        this.result = itemLike.asItem();
        this.ingredient = ingredient;
        this.count = n;
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike) {
        return new SingleItemRecipeBuilder(recipeCategory, StonecutterRecipe::new, ingredient, itemLike, 1);
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, int n) {
        return new SingleItemRecipeBuilder(recipeCategory, StonecutterRecipe::new, ingredient, itemLike, n);
    }

    @Override
    public SingleItemRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
        this.criteria.put(string, criterion);
        return this;
    }

    @Override
    public SingleItemRecipeBuilder group(@Nullable String string) {
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
        Object obj = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.ingredient, new ItemStack(this.result, this.count));
        recipeOutput.accept(resourceLocation, (Recipe<?>)obj, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
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

