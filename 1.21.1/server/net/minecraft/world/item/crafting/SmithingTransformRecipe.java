/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe
implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public SmithingTransformRecipe(Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, ItemStack itemStack) {
        this.template = ingredient;
        this.base = ingredient2;
        this.addition = ingredient3;
        this.result = itemStack;
    }

    @Override
    public boolean matches(SmithingRecipeInput smithingRecipeInput, Level level) {
        return this.template.test(smithingRecipeInput.template()) && this.base.test(smithingRecipeInput.base()) && this.addition.test(smithingRecipeInput.addition());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput smithingRecipeInput, HolderLookup.Provider provider) {
        ItemStack itemStack = smithingRecipeInput.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemStack.applyComponents(this.result.getComponentsPatch());
        return itemStack;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack itemStack) {
        return this.template.test(itemStack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack itemStack) {
        return this.base.test(itemStack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack itemStack) {
        return this.addition.test(itemStack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTransformRecipe> {
        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Ingredient.CODEC.fieldOf("template").forGetter(smithingTransformRecipe -> smithingTransformRecipe.template), (App)Ingredient.CODEC.fieldOf("base").forGetter(smithingTransformRecipe -> smithingTransformRecipe.base), (App)Ingredient.CODEC.fieldOf("addition").forGetter(smithingTransformRecipe -> smithingTransformRecipe.addition), (App)ItemStack.STRICT_CODEC.fieldOf("result").forGetter(smithingTransformRecipe -> smithingTransformRecipe.result)).apply((Applicative)instance, SmithingTransformRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SmithingTransformRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            Ingredient ingredient = (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
            Ingredient ingredient2 = (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
            Ingredient ingredient3 = (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
            ItemStack itemStack = (ItemStack)ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
            return new SmithingTransformRecipe(ingredient, ingredient2, ingredient3, itemStack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, SmithingTransformRecipe smithingTransformRecipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, smithingTransformRecipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, smithingTransformRecipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, smithingTransformRecipe.addition);
            ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, smithingTransformRecipe.result);
        }
    }
}

