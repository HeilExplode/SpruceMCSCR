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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class ShapedRecipe
implements CraftingRecipe {
    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipe(String string, CraftingBookCategory craftingBookCategory, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack, boolean bl) {
        this.group = string;
        this.category = craftingBookCategory;
        this.pattern = shapedRecipePattern;
        this.result = itemStack;
        this.showNotification = bl;
    }

    public ShapedRecipe(String string, CraftingBookCategory craftingBookCategory, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack) {
        this(string, craftingBookCategory, shapedRecipePattern, itemStack, true);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
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
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n >= this.pattern.width() && n2 >= this.pattern.height();
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        return this.pattern.matches(craftingInput);
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        return this.getResultItem(provider).copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonNullList = this.getIngredients();
        return nonNullList.isEmpty() || nonNullList.stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.getItems().length == 0);
    }

    public static class Serializer
    implements RecipeSerializer<ShapedRecipe> {
        public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(shapedRecipe -> shapedRecipe.group), (App)CraftingBookCategory.CODEC.fieldOf("category").orElse((Object)CraftingBookCategory.MISC).forGetter(shapedRecipe -> shapedRecipe.category), (App)ShapedRecipePattern.MAP_CODEC.forGetter(shapedRecipe -> shapedRecipe.pattern), (App)ItemStack.STRICT_CODEC.fieldOf("result").forGetter(shapedRecipe -> shapedRecipe.result), (App)Codec.BOOL.optionalFieldOf("show_notification", (Object)true).forGetter(shapedRecipe -> shapedRecipe.showNotification)).apply((Applicative)instance, ShapedRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<ShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapedRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            String string = registryFriendlyByteBuf.readUtf();
            CraftingBookCategory craftingBookCategory = registryFriendlyByteBuf.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedRecipePattern = (ShapedRecipePattern)ShapedRecipePattern.STREAM_CODEC.decode(registryFriendlyByteBuf);
            ItemStack itemStack = (ItemStack)ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
            boolean bl = registryFriendlyByteBuf.readBoolean();
            return new ShapedRecipe(string, craftingBookCategory, shapedRecipePattern, itemStack, bl);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, ShapedRecipe shapedRecipe) {
            registryFriendlyByteBuf.writeUtf(shapedRecipe.group);
            registryFriendlyByteBuf.writeEnum(shapedRecipe.category);
            ShapedRecipePattern.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.pattern);
            ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.result);
            registryFriendlyByteBuf.writeBoolean(shapedRecipe.showNotification);
        }
    }
}

