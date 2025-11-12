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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe>
implements RecipeSerializer<T> {
    private final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CraftingBookCategory.CODEC.fieldOf("category").orElse((Object)CraftingBookCategory.MISC).forGetter(CraftingRecipe::category)).apply((Applicative)instance, factory::create));
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category, factory::create);

    public SimpleCraftingRecipeSerializer(Factory<T> factory) {
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    @FunctionalInterface
    public static interface Factory<T extends CraftingRecipe> {
        public T create(CraftingBookCategory var1);
    }
}

