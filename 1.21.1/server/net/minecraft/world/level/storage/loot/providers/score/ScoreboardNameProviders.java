/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.FixedScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;

public class ScoreboardNameProviders {
    private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE.byNameCodec().dispatch(ScoreboardNameProvider::getType, LootScoreProviderType::codec);
    public static final Codec<ScoreboardNameProvider> CODEC = Codec.lazyInitialized(() -> Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, TYPED_CODEC).xmap(Either::unwrap, scoreboardNameProvider -> {
        Either either;
        if (scoreboardNameProvider instanceof ContextScoreboardNameProvider) {
            ContextScoreboardNameProvider contextScoreboardNameProvider = (ContextScoreboardNameProvider)scoreboardNameProvider;
            either = Either.left((Object)contextScoreboardNameProvider);
        } else {
            either = Either.right((Object)scoreboardNameProvider);
        }
        return either;
    }));
    public static final LootScoreProviderType FIXED = ScoreboardNameProviders.register("fixed", FixedScoreboardNameProvider.CODEC);
    public static final LootScoreProviderType CONTEXT = ScoreboardNameProviders.register("context", ContextScoreboardNameProvider.CODEC);

    private static LootScoreProviderType register(String string, MapCodec<? extends ScoreboardNameProvider> mapCodec) {
        return Registry.register(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, ResourceLocation.withDefaultNamespace(string), new LootScoreProviderType(mapCodec));
    }
}

