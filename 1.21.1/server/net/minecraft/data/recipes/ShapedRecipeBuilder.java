/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

public class ShapedRecipeBuilder
implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public ShapedRecipeBuilder(RecipeCategory recipeCategory, ItemLike itemLike, int n) {
        this.category = recipeCategory;
        this.result = itemLike.asItem();
        this.count = n;
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory recipeCategory, ItemLike itemLike) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 1);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory recipeCategory, ItemLike itemLike, int n) {
        return new ShapedRecipeBuilder(recipeCategory, itemLike, n);
    }

    public ShapedRecipeBuilder define(Character c, TagKey<Item> tagKey) {
        return this.define(c, Ingredient.of(tagKey));
    }

    public ShapedRecipeBuilder define(Character c, ItemLike itemLike) {
        return this.define(c, Ingredient.of(itemLike));
    }

    public ShapedRecipeBuilder define(Character c, Ingredient ingredient) {
        if (this.key.containsKey(c)) {
            throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
        }
        if (c.charValue() == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        this.key.put(c, ingredient);
        return this;
    }

    public ShapedRecipeBuilder pattern(String string) {
        if (!this.rows.isEmpty() && string.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }
        this.rows.add(string);
        return this;
    }

    @Override
    public ShapedRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
        this.criteria.put(string, criterion);
        return this;
    }

    @Override
    public ShapedRecipeBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    public ShapedRecipeBuilder showNotification(boolean bl) {
        this.showNotification = bl;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        ShapedRecipePattern shapedRecipePattern = this.ensureValid(resourceLocation);
        Advancement.Builder builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        ShapedRecipe shapedRecipe = new ShapedRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), shapedRecipePattern, new ItemStack(this.result, this.count), this.showNotification);
        recipeOutput.accept(resourceLocation, shapedRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private ShapedRecipePattern ensureValid(ResourceLocation resourceLocation) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf(resourceLocation));
        }
        return ShapedRecipePattern.of(this.key, this.rows);
    }

    @Override
    public /* synthetic */ RecipeBuilder group(@Nullable String string) {
        return this.group(string);
    }

    public /* synthetic */ RecipeBuilder unlockedBy(String string, Criterion criterion) {
        return this.unlockedBy(string, criterion);
    }
}

