/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ShapelessRecipe
implements CraftingRecipe {
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;

    public ShapelessRecipe(String string, CraftingBookCategory craftingBookCategory, ItemStack itemStack, NonNullList<Ingredient> nonNullList) {
        this.group = string;
        this.category = craftingBookCategory;
        this.result = itemStack;
        this.ingredients = nonNullList;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() != this.ingredients.size()) {
            return false;
        }
        if (craftingInput.size() == 1 && this.ingredients.size() == 1) {
            return ((Ingredient)this.ingredients.getFirst()).test(craftingInput.getItem(0));
        }
        return craftingInput.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= this.ingredients.size();
    }

    public static class Serializer
    implements RecipeSerializer<ShapelessRecipe> {
        private static final MapCodec<ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(shapelessRecipe -> shapelessRecipe.group), (App)CraftingBookCategory.CODEC.fieldOf("category").orElse((Object)CraftingBookCategory.MISC).forGetter(shapelessRecipe -> shapelessRecipe.category), (App)ItemStack.STRICT_CODEC.fieldOf("result").forGetter(shapelessRecipe -> shapelessRecipe.result), (App)Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(list -> {
            Ingredient[] ingredientArray = (Ingredient[])list.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
            if (ingredientArray.length == 0) {
                return DataResult.error(() -> "No ingredients for shapeless recipe");
            }
            if (ingredientArray.length > 9) {
                return DataResult.error(() -> "Too many ingredients for shapeless recipe");
            }
            return DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredientArray));
        }, DataResult::success).forGetter(shapelessRecipe -> shapelessRecipe.ingredients)).apply((Applicative)instance, ShapelessRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<ShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapelessRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            String string = registryFriendlyByteBuf.readUtf();
            CraftingBookCategory craftingBookCategory = registryFriendlyByteBuf.readEnum(CraftingBookCategory.class);
            int n = registryFriendlyByteBuf.readVarInt();
            NonNullList<Ingredient> nonNullList = NonNullList.withSize(n, Ingredient.EMPTY);
            nonNullList.replaceAll(ingredient -> (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf));
            ItemStack itemStack = (ItemStack)ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
            return new ShapelessRecipe(string, craftingBookCategory, itemStack, nonNullList);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, ShapelessRecipe shapelessRecipe) {
            registryFriendlyByteBuf.writeUtf(shapelessRecipe.group);
            registryFriendlyByteBuf.writeEnum(shapelessRecipe.category);
            registryFriendlyByteBuf.writeVarInt(shapelessRecipe.ingredients.size());
            for (Ingredient ingredient : shapelessRecipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, shapelessRecipe.result);
        }
    }
}

