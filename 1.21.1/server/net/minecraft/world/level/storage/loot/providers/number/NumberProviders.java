/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.ScoreboardValue;
import net.minecraft.world.level.storage.loot.providers.number.StorageValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class NumberProviders {
    private static final Codec<NumberProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.byNameCodec().dispatch(NumberProvider::getType, LootNumberProviderType::codec);
    public static final Codec<NumberProvider> CODEC = Codec.lazyInitialized(() -> {
        Codec codec = Codec.withAlternative(TYPED_CODEC, (Codec)UniformGenerator.CODEC.codec());
        return Codec.either(ConstantValue.INLINE_CODEC, (Codec)codec).xmap(Either::unwrap, numberProvider -> {
            Either either;
            if (numberProvider instanceof ConstantValue) {
                ConstantValue constantValue = (ConstantValue)numberProvider;
                either = Either.left((Object)constantValue);
            } else {
                either = Either.right((Object)numberProvider);
            }
            return either;
        });
    });
    public static final LootNumberProviderType CONSTANT = NumberProviders.register("constant", ConstantValue.CODEC);
    public static final LootNumberProviderType UNIFORM = NumberProviders.register("uniform", UniformGenerator.CODEC);
    public static final LootNumberProviderType BINOMIAL = NumberProviders.register("binomial", BinomialDistributionGenerator.CODEC);
    public static final LootNumberProviderType SCORE = NumberProviders.register("score", ScoreboardValue.CODEC);
    public static final LootNumberProviderType STORAGE = NumberProviders.register("storage", StorageValue.CODEC);
    public static final LootNumberProviderType ENCHANTMENT_LEVEL = NumberProviders.register("enchantment_level", EnchantmentLevelProvider.CODEC);

    private static LootNumberProviderType register(String string, MapCodec<? extends NumberProvider> mapCodec) {
        return Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, ResourceLocation.withDefaultNamespace(string), new LootNumberProviderType(mapCodec));
    }
}

