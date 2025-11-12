/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public abstract class SingleItemRecipe
implements Recipe<SingleRecipeInput> {
    protected final Ingredient ingredient;
    protected final ItemStack result;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;
    protected final String group;

    public SingleItemRecipe(RecipeType<?> recipeType, RecipeSerializer<?> recipeSerializer, String string, Ingredient ingredient, ItemStack itemStack) {
        this.type = recipeType;
        this.serializer = recipeSerializer;
        this.group = string;
        this.ingredient = ingredient;
        this.result = itemStack;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonNullList = NonNullList.create();
        nonNullList.add(this.ingredient);
        return nonNullList;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return true;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput singleRecipeInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    public static interface Factory<T extends SingleItemRecipe> {
        public T create(String var1, Ingredient var2, ItemStack var3);
    }

    public static class Serializer<T extends SingleItemRecipe>
    implements RecipeSerializer<T> {
        final Factory<T> factory;
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        protected Serializer(Factory<T> factory) {
            this.factory = factory;
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(singleItemRecipe -> singleItemRecipe.group), (App)Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(singleItemRecipe -> singleItemRecipe.ingredient), (App)ItemStack.STRICT_CODEC.fieldOf("result").forGetter(singleItemRecipe -> singleItemRecipe.result)).apply((Applicative)instance, factory::create));
            this.streamCodec = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, singleItemRecipe -> singleItemRecipe.group, Ingredient.CONTENTS_STREAM_CODEC, singleItemRecipe -> singleItemRecipe.ingredient, ItemStack.STREAM_CODEC, singleItemRecipe -> singleItemRecipe.result, factory::create);
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }
    }
}

