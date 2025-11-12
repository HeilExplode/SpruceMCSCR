/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.presets;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

public class WorldPreset {
    public static final Codec<WorldPreset> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions").forGetter(worldPreset -> worldPreset.dimensions)).apply((Applicative)instance, WorldPreset::new)).validate(WorldPreset::requireOverworld);
    public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registries.WORLD_PRESET, DIRECT_CODEC);
    private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

    public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map) {
        this.dimensions = map;
    }

    private ImmutableMap<ResourceKey<LevelStem>, LevelStem> dimensionsInOrder() {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        WorldDimensions.keysInOrder(this.dimensions.keySet().stream()).forEach(resourceKey -> {
            LevelStem levelStem = this.dimensions.get(resourceKey);
            if (levelStem != null) {
                builder.put(resourceKey, (Object)levelStem);
            }
        });
        return builder.build();
    }

    public WorldDimensions createWorldDimensions() {
        return new WorldDimensions((Map<ResourceKey<LevelStem>, LevelStem>)this.dimensionsInOrder());
    }

    public Optional<LevelStem> overworld() {
        return Optional.ofNullable(this.dimensions.get(LevelStem.OVERWORLD));
    }

    private static DataResult<WorldPreset> requireOverworld(WorldPreset worldPreset) {
        if (worldPreset.overworld().isEmpty()) {
            return DataResult.error(() -> "Missing overworld dimension");
        }
        return DataResult.success((Object)worldPreset, (Lifecycle)Lifecycle.stable());
    }
}

