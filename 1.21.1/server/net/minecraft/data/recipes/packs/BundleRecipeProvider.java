/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.recipes.packs;

import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class BundleRecipeProvider
extends RecipeProvider {
    public BundleRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, completableFuture);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.BUNDLE).define(Character.valueOf('#'), Items.RABBIT_HIDE).define(Character.valueOf('-'), Items.STRING).pattern("-#-").pattern("# #").pattern("###").unlockedBy("has_string", (Criterion)BundleRecipeProvider.has(Items.STRING)).save(recipeOutput);
    }
}

