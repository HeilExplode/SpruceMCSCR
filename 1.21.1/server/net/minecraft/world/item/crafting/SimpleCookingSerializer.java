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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe>
implements RecipeSerializer<T> {
    private final AbstractCookingRecipe.Factory<T> factory;
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public SimpleCookingSerializer(AbstractCookingRecipe.Factory<T> factory, int n) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(abstractCookingRecipe -> abstractCookingRecipe.group), (App)CookingBookCategory.CODEC.fieldOf("category").orElse((Object)CookingBookCategory.MISC).forGetter(abstractCookingRecipe -> abstractCookingRecipe.category), (App)Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(abstractCookingRecipe -> abstractCookingRecipe.ingredient), (App)ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(abstractCookingRecipe -> abstractCookingRecipe.result), (App)Codec.FLOAT.fieldOf("experience").orElse((Object)Float.valueOf(0.0f)).forGetter(abstractCookingRecipe -> Float.valueOf(abstractCookingRecipe.experience)), (App)Codec.INT.fieldOf("cookingtime").orElse((Object)n).forGetter(abstractCookingRecipe -> abstractCookingRecipe.cookingTime)).apply((Applicative)instance, factory::create));
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    private T fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        String string = registryFriendlyByteBuf.readUtf();
        CookingBookCategory cookingBookCategory = registryFriendlyByteBuf.readEnum(CookingBookCategory.class);
        Ingredient ingredient = (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
        ItemStack itemStack = (ItemStack)ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
        float f = registryFriendlyByteBuf.readFloat();
        int n = registryFriendlyByteBuf.readVarInt();
        return this.factory.create(string, cookingBookCategory, ingredient, itemStack, f, n);
    }

    private void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, T t) {
        registryFriendlyByteBuf.writeUtf(((AbstractCookingRecipe)t).group);
        registryFriendlyByteBuf.writeEnum(((AbstractCookingRecipe)t).category());
        Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, ((AbstractCookingRecipe)t).ingredient);
        ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, ((AbstractCookingRecipe)t).result);
        registryFriendlyByteBuf.writeFloat(((AbstractCookingRecipe)t).experience);
        registryFriendlyByteBuf.writeVarInt(((AbstractCookingRecipe)t).cookingTime);
    }

    public AbstractCookingRecipe create(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int n) {
        return this.factory.create(string, cookingBookCategory, ingredient, itemStack, f, n);
    }
}

