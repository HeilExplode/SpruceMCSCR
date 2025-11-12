/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.presets;

import java.lang.runtime.SwitchBootstraps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class WorldPresets {
    public static final ResourceKey<WorldPreset> NORMAL = WorldPresets.register("normal");
    public static final ResourceKey<WorldPreset> FLAT = WorldPresets.register("flat");
    public static final ResourceKey<WorldPreset> LARGE_BIOMES = WorldPresets.register("large_biomes");
    public static final ResourceKey<WorldPreset> AMPLIFIED = WorldPresets.register("amplified");
    public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = WorldPresets.register("single_biome_surface");
    public static final ResourceKey<WorldPreset> DEBUG = WorldPresets.register("debug_all_block_states");

    public static void bootstrap(BootstrapContext<WorldPreset> bootstrapContext) {
        new Bootstrap(bootstrapContext).bootstrap();
    }

    private static ResourceKey<WorldPreset> register(String string) {
        return ResourceKey.create(Registries.WORLD_PRESET, ResourceLocation.withDefaultNamespace(string));
    }

    public static Optional<ResourceKey<WorldPreset>> fromSettings(WorldDimensions worldDimensions) {
        return worldDimensions.get(LevelStem.OVERWORLD).flatMap(levelStem -> {
            ChunkGenerator chunkGenerator = levelStem.generator();
            Objects.requireNonNull(chunkGenerator);
            ChunkGenerator chunkGenerator2 = chunkGenerator;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{FlatLevelSource.class, DebugLevelSource.class, NoiseBasedChunkGenerator.class}, (Object)chunkGenerator2, n)) {
                case 0 -> {
                    FlatLevelSource var3_3 = (FlatLevelSource)chunkGenerator2;
                    yield Optional.of(FLAT);
                }
                case 1 -> {
                    DebugLevelSource var4_4 = (DebugLevelSource)chunkGenerator2;
                    yield Optional.of(DEBUG);
                }
                case 2 -> {
                    NoiseBasedChunkGenerator var5_5 = (NoiseBasedChunkGenerator)chunkGenerator2;
                    yield Optional.of(NORMAL);
                }
                default -> Optional.empty();
            };
        });
    }

    public static WorldDimensions createNormalWorldDimensions(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().createWorldDimensions();
    }

    public static LevelStem getNormalOverworld(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().overworld().orElseThrow();
    }

    static class Bootstrap {
        private final BootstrapContext<WorldPreset> context;
        private final HolderGetter<NoiseGeneratorSettings> noiseSettings;
        private final HolderGetter<Biome> biomes;
        private final HolderGetter<PlacedFeature> placedFeatures;
        private final HolderGetter<StructureSet> structureSets;
        private final HolderGetter<MultiNoiseBiomeSourceParameterList> multiNoiseBiomeSourceParameterLists;
        private final Holder<DimensionType> overworldDimensionType;
        private final LevelStem netherStem;
        private final LevelStem endStem;

        Bootstrap(BootstrapContext<WorldPreset> bootstrapContext) {
            this.context = bootstrapContext;
            HolderGetter<DimensionType> holderGetter = bootstrapContext.lookup(Registries.DIMENSION_TYPE);
            this.noiseSettings = bootstrapContext.lookup(Registries.NOISE_SETTINGS);
            this.biomes = bootstrapContext.lookup(Registries.BIOME);
            this.placedFeatures = bootstrapContext.lookup(Registries.PLACED_FEATURE);
            this.structureSets = bootstrapContext.lookup(Registries.STRUCTURE_SET);
            this.multiNoiseBiomeSourceParameterLists = bootstrapContext.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            this.overworldDimensionType = holderGetter.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
            Holder.Reference<DimensionType> reference = holderGetter.getOrThrow(BuiltinDimensionTypes.NETHER);
            Holder.Reference<NoiseGeneratorSettings> reference2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
            Holder.Reference<MultiNoiseBiomeSourceParameterList> reference3 = this.multiNoiseBiomeSourceParameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
            this.netherStem = new LevelStem(reference, new NoiseBasedChunkGenerator((BiomeSource)MultiNoiseBiomeSource.createFromPreset(reference3), reference2));
            Holder.Reference<DimensionType> reference4 = holderGetter.getOrThrow(BuiltinDimensionTypes.END);
            Holder.Reference<NoiseGeneratorSettings> reference5 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
            this.endStem = new LevelStem(reference4, new NoiseBasedChunkGenerator((BiomeSource)TheEndBiomeSource.create(this.biomes), reference5));
        }

        private LevelStem makeOverworld(ChunkGenerator chunkGenerator) {
            return new LevelStem(this.overworldDimensionType, chunkGenerator);
        }

        private LevelStem makeNoiseBasedOverworld(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
            return this.makeOverworld(new NoiseBasedChunkGenerator(biomeSource, holder));
        }

        private WorldPreset createPresetWithCustomOverworld(LevelStem levelStem) {
            return new WorldPreset(Map.of(LevelStem.OVERWORLD, levelStem, LevelStem.NETHER, this.netherStem, LevelStem.END, this.endStem));
        }

        private void registerCustomOverworldPreset(ResourceKey<WorldPreset> resourceKey, LevelStem levelStem) {
            this.context.register(resourceKey, this.createPresetWithCustomOverworld(levelStem));
        }

        private void registerOverworlds(BiomeSource biomeSource) {
            Holder.Reference<NoiseGeneratorSettings> reference = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            this.registerCustomOverworldPreset(NORMAL, this.makeNoiseBasedOverworld(biomeSource, reference));
            Holder.Reference<NoiseGeneratorSettings> reference2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
            this.registerCustomOverworldPreset(LARGE_BIOMES, this.makeNoiseBasedOverworld(biomeSource, reference2));
            Holder.Reference<NoiseGeneratorSettings> reference3 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
            this.registerCustomOverworldPreset(AMPLIFIED, this.makeNoiseBasedOverworld(biomeSource, reference3));
        }

        public void bootstrap() {
            Holder.Reference<MultiNoiseBiomeSourceParameterList> reference = this.multiNoiseBiomeSourceParameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
            this.registerOverworlds(MultiNoiseBiomeSource.createFromPreset(reference));
            Holder.Reference<NoiseGeneratorSettings> reference2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            Holder.Reference<Biome> reference3 = this.biomes.getOrThrow(Biomes.PLAINS);
            this.registerCustomOverworldPreset(SINGLE_BIOME_SURFACE, this.makeNoiseBasedOverworld(new FixedBiomeSource(reference3), reference2));
            this.registerCustomOverworldPreset(FLAT, this.makeOverworld(new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(this.biomes, this.structureSets, this.placedFeatures))));
            this.registerCustomOverworldPreset(DEBUG, this.makeOverworld(new DebugLevelSource(reference3)));
        }
    }
}

