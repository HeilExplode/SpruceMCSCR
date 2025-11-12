/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<T extends RecipeInput> {
    public static final Codec<Recipe<?>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER).dispatch(Recipe::getSerializer, RecipeSerializer::streamCodec);

    public boolean matches(T var1, Level var2);

    public ItemStack assemble(T var1, HolderLookup.Provider var2);

    public boolean canCraftInDimensions(int var1, int var2);

    public ItemStack getResultItem(HolderLookup.Provider var1);

    default public NonNullList<ItemStack> getRemainingItems(T t) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(t.size(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            Item item = t.getItem(i).getItem();
            if (!item.hasCraftingRemainingItem()) continue;
            nonNullList.set(i, new ItemStack(item.getCraftingRemainingItem()));
        }
        return nonNullList;
    }

    default public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    default public boolean isSpecial() {
        return false;
    }

    default public boolean showNotification() {
        return true;
    }

    default public String getGroup() {
        return "";
    }

    default public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }

    public RecipeSerializer<?> getSerializer();

    public RecipeType<?> getType();

    default public boolean isIncomplete() {
        NonNullList<Ingredient> nonNullList = this.getIngredients();
        return nonNullList.isEmpty() || nonNullList.stream().anyMatch(ingredient -> ingredient.getItems().length == 0);
    }
}

