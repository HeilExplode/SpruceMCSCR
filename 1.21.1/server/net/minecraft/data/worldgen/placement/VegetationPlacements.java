/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  javax.annotation.Nullable
 */
package net.minecraft.data.worldgen.placement;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public class VegetationPlacements {
    public static final ResourceKey<PlacedFeature> BAMBOO_LIGHT = PlacementUtils.createKey("bamboo_light");
    public static final ResourceKey<PlacedFeature> BAMBOO = PlacementUtils.createKey("bamboo");
    public static final ResourceKey<PlacedFeature> VINES = PlacementUtils.createKey("vines");
    public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = PlacementUtils.createKey("patch_sunflower");
    public static final ResourceKey<PlacedFeature> PATCH_PUMPKIN = PlacementUtils.createKey("patch_pumpkin");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = PlacementUtils.createKey("patch_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_FOREST = PlacementUtils.createKey("patch_grass_forest");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_BADLANDS = PlacementUtils.createKey("patch_grass_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_SAVANNA = PlacementUtils.createKey("patch_grass_savanna");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_NORMAL = PlacementUtils.createKey("patch_grass_normal");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA_2 = PlacementUtils.createKey("patch_grass_taiga_2");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA = PlacementUtils.createKey("patch_grass_taiga");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_JUNGLE = PlacementUtils.createKey("patch_grass_jungle");
    public static final ResourceKey<PlacedFeature> GRASS_BONEMEAL = PlacementUtils.createKey("grass_bonemeal");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_2 = PlacementUtils.createKey("patch_dead_bush_2");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH = PlacementUtils.createKey("patch_dead_bush");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_BADLANDS = PlacementUtils.createKey("patch_dead_bush_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_MELON = PlacementUtils.createKey("patch_melon");
    public static final ResourceKey<PlacedFeature> PATCH_MELON_SPARSE = PlacementUtils.createKey("patch_melon_sparse");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_COMMON = PlacementUtils.createKey("patch_berry_common");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_RARE = PlacementUtils.createKey("patch_berry_rare");
    public static final ResourceKey<PlacedFeature> PATCH_WATERLILY = PlacementUtils.createKey("patch_waterlily");
    public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS_2 = PlacementUtils.createKey("patch_tall_grass_2");
    public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS = PlacementUtils.createKey("patch_tall_grass");
    public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = PlacementUtils.createKey("patch_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DESERT = PlacementUtils.createKey("patch_cactus_desert");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DECORATED = PlacementUtils.createKey("patch_cactus_decorated");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_SWAMP = PlacementUtils.createKey("patch_sugar_cane_swamp");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_DESERT = PlacementUtils.createKey("patch_sugar_cane_desert");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_BADLANDS = PlacementUtils.createKey("patch_sugar_cane_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE = PlacementUtils.createKey("patch_sugar_cane");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NETHER = PlacementUtils.createKey("brown_mushroom_nether");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NETHER = PlacementUtils.createKey("red_mushroom_nether");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NORMAL = PlacementUtils.createKey("brown_mushroom_normal");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NORMAL = PlacementUtils.createKey("red_mushroom_normal");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_TAIGA = PlacementUtils.createKey("brown_mushroom_taiga");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_TAIGA = PlacementUtils.createKey("red_mushroom_taiga");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("brown_mushroom_old_growth");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("red_mushroom_old_growth");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_SWAMP = PlacementUtils.createKey("brown_mushroom_swamp");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_SWAMP = PlacementUtils.createKey("red_mushroom_swamp");
    public static final ResourceKey<PlacedFeature> FLOWER_WARM = PlacementUtils.createKey("flower_warm");
    public static final ResourceKey<PlacedFeature> FLOWER_DEFAULT = PlacementUtils.createKey("flower_default");
    public static final ResourceKey<PlacedFeature> FLOWER_FLOWER_FOREST = PlacementUtils.createKey("flower_flower_forest");
    public static final ResourceKey<PlacedFeature> FLOWER_SWAMP = PlacementUtils.createKey("flower_swamp");
    public static final ResourceKey<PlacedFeature> FLOWER_PLAINS = PlacementUtils.createKey("flower_plains");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = PlacementUtils.createKey("flower_meadow");
    public static final ResourceKey<PlacedFeature> FLOWER_CHERRY = PlacementUtils.createKey("flower_cherry");
    public static final ResourceKey<PlacedFeature> TREES_PLAINS = PlacementUtils.createKey("trees_plains");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_VEGETATION = PlacementUtils.createKey("dark_forest_vegetation");
    public static final ResourceKey<PlacedFeature> FLOWER_FOREST_FLOWERS = PlacementUtils.createKey("flower_forest_flowers");
    public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = PlacementUtils.createKey("forest_flowers");
    public static final ResourceKey<PlacedFeature> TREES_FLOWER_FOREST = PlacementUtils.createKey("trees_flower_forest");
    public static final ResourceKey<PlacedFeature> TREES_MEADOW = PlacementUtils.createKey("trees_meadow");
    public static final ResourceKey<PlacedFeature> TREES_CHERRY = PlacementUtils.createKey("trees_cherry");
    public static final ResourceKey<PlacedFeature> TREES_TAIGA = PlacementUtils.createKey("trees_taiga");
    public static final ResourceKey<PlacedFeature> TREES_GROVE = PlacementUtils.createKey("trees_grove");
    public static final ResourceKey<PlacedFeature> TREES_BADLANDS = PlacementUtils.createKey("trees_badlands");
    public static final ResourceKey<PlacedFeature> TREES_SNOWY = PlacementUtils.createKey("trees_snowy");
    public static final ResourceKey<PlacedFeature> TREES_SWAMP = PlacementUtils.createKey("trees_swamp");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_SAVANNA = PlacementUtils.createKey("trees_windswept_savanna");
    public static final ResourceKey<PlacedFeature> TREES_SAVANNA = PlacementUtils.createKey("trees_savanna");
    public static final ResourceKey<PlacedFeature> BIRCH_TALL = PlacementUtils.createKey("birch_tall");
    public static final ResourceKey<PlacedFeature> TREES_BIRCH = PlacementUtils.createKey("trees_birch");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_FOREST = PlacementUtils.createKey("trees_windswept_forest");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_HILLS = PlacementUtils.createKey("trees_windswept_hills");
    public static final ResourceKey<PlacedFeature> TREES_WATER = PlacementUtils.createKey("trees_water");
    public static final ResourceKey<PlacedFeature> TREES_BIRCH_AND_OAK = PlacementUtils.createKey("trees_birch_and_oak");
    public static final ResourceKey<PlacedFeature> TREES_SPARSE_JUNGLE = PlacementUtils.createKey("trees_sparse_jungle");
    public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacementUtils.createKey("trees_old_growth_spruce_taiga");
    public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_PINE_TAIGA = PlacementUtils.createKey("trees_old_growth_pine_taiga");
    public static final ResourceKey<PlacedFeature> TREES_JUNGLE = PlacementUtils.createKey("trees_jungle");
    public static final ResourceKey<PlacedFeature> BAMBOO_VEGETATION = PlacementUtils.createKey("bamboo_vegetation");
    public static final ResourceKey<PlacedFeature> MUSHROOM_ISLAND_VEGETATION = PlacementUtils.createKey("mushroom_island_vegetation");
    public static final ResourceKey<PlacedFeature> TREES_MANGROVE = PlacementUtils.createKey("trees_mangrove");
    private static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);

    public static List<PlacementModifier> worldSurfaceSquaredWithCount(int n) {
        return List.of(CountPlacement.of(n), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    }

    private static List<PlacementModifier> getMushroomPlacement(int n, @Nullable PlacementModifier placementModifier) {
        ImmutableList.Builder builder = ImmutableList.builder();
        if (placementModifier != null) {
            builder.add((Object)placementModifier);
        }
        if (n != 0) {
            builder.add((Object)RarityFilter.onAverageOnceEvery(n));
        }
        builder.add((Object)InSquarePlacement.spread());
        builder.add((Object)PlacementUtils.HEIGHTMAP);
        builder.add((Object)BiomeFilter.biome());
        return builder.build();
    }

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase(PlacementModifier placementModifier) {
        return ImmutableList.builder().add((Object)placementModifier).add((Object)InSquarePlacement.spread()).add((Object)TREE_THRESHOLD).add((Object)PlacementUtils.HEIGHTMAP_OCEAN_FLOOR).add((Object)BiomeFilter.biome());
    }

    public static List<PlacementModifier> treePlacement(PlacementModifier placementModifier) {
        return VegetationPlacements.treePlacementBase(placementModifier).build();
    }

    public static List<PlacementModifier> treePlacement(PlacementModifier placementModifier, Block block) {
        return VegetationPlacements.treePlacementBase(placementModifier).add((Object)BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO))).build();
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapContext) {
        HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference = holderGetter.getOrThrow(VegetationFeatures.BAMBOO_NO_PODZOL);
        Holder.Reference<ConfiguredFeature<?, ?>> reference2 = holderGetter.getOrThrow(VegetationFeatures.BAMBOO_SOME_PODZOL);
        Holder.Reference<ConfiguredFeature<?, ?>> reference3 = holderGetter.getOrThrow(VegetationFeatures.VINES);
        Holder.Reference<ConfiguredFeature<?, ?>> reference4 = holderGetter.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
        Holder.Reference<ConfiguredFeature<?, ?>> reference5 = holderGetter.getOrThrow(VegetationFeatures.PATCH_PUMPKIN);
        Holder.Reference<ConfiguredFeature<?, ?>> reference6 = holderGetter.getOrThrow(VegetationFeatures.PATCH_GRASS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference7 = holderGetter.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference8 = holderGetter.getOrThrow(VegetationFeatures.PATCH_GRASS_JUNGLE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference9 = holderGetter.getOrThrow(VegetationFeatures.SINGLE_PIECE_OF_GRASS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference10 = holderGetter.getOrThrow(VegetationFeatures.PATCH_DEAD_BUSH);
        Holder.Reference<ConfiguredFeature<?, ?>> reference11 = holderGetter.getOrThrow(VegetationFeatures.PATCH_MELON);
        Holder.Reference<ConfiguredFeature<?, ?>> reference12 = holderGetter.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
        Holder.Reference<ConfiguredFeature<?, ?>> reference13 = holderGetter.getOrThrow(VegetationFeatures.PATCH_WATERLILY);
        Holder.Reference<ConfiguredFeature<?, ?>> reference14 = holderGetter.getOrThrow(VegetationFeatures.PATCH_TALL_GRASS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference15 = holderGetter.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
        Holder.Reference<ConfiguredFeature<?, ?>> reference16 = holderGetter.getOrThrow(VegetationFeatures.PATCH_CACTUS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference17 = holderGetter.getOrThrow(VegetationFeatures.PATCH_SUGAR_CANE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference18 = holderGetter.getOrThrow(VegetationFeatures.PATCH_BROWN_MUSHROOM);
        Holder.Reference<ConfiguredFeature<?, ?>> reference19 = holderGetter.getOrThrow(VegetationFeatures.PATCH_RED_MUSHROOM);
        Holder.Reference<ConfiguredFeature<?, ?>> reference20 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_DEFAULT);
        Holder.Reference<ConfiguredFeature<?, ?>> reference21 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_FLOWER_FOREST);
        Holder.Reference<ConfiguredFeature<?, ?>> reference22 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_SWAMP);
        Holder.Reference<ConfiguredFeature<?, ?>> reference23 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
        Holder.Reference<ConfiguredFeature<?, ?>> reference24 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_MEADOW);
        Holder.Reference<ConfiguredFeature<?, ?>> reference25 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_CHERRY);
        Holder.Reference<ConfiguredFeature<?, ?>> reference26 = holderGetter.getOrThrow(VegetationFeatures.TREES_PLAINS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference27 = holderGetter.getOrThrow(VegetationFeatures.DARK_FOREST_VEGETATION);
        Holder.Reference<ConfiguredFeature<?, ?>> reference28 = holderGetter.getOrThrow(VegetationFeatures.FOREST_FLOWERS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference29 = holderGetter.getOrThrow(VegetationFeatures.TREES_FLOWER_FOREST);
        Holder.Reference<ConfiguredFeature<?, ?>> reference30 = holderGetter.getOrThrow(VegetationFeatures.MEADOW_TREES);
        Holder.Reference<ConfiguredFeature<?, ?>> reference31 = holderGetter.getOrThrow(VegetationFeatures.TREES_TAIGA);
        Holder.Reference<ConfiguredFeature<?, ?>> reference32 = holderGetter.getOrThrow(VegetationFeatures.TREES_GROVE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference33 = holderGetter.getOrThrow(TreeFeatures.OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> reference34 = holderGetter.getOrThrow(TreeFeatures.SPRUCE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference35 = holderGetter.getOrThrow(TreeFeatures.CHERRY_BEES_005);
        Holder.Reference<ConfiguredFeature<?, ?>> reference36 = holderGetter.getOrThrow(TreeFeatures.SWAMP_OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> reference37 = holderGetter.getOrThrow(VegetationFeatures.TREES_SAVANNA);
        Holder.Reference<ConfiguredFeature<?, ?>> reference38 = holderGetter.getOrThrow(VegetationFeatures.BIRCH_TALL);
        Holder.Reference<ConfiguredFeature<?, ?>> reference39 = holderGetter.getOrThrow(TreeFeatures.BIRCH_BEES_0002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference40 = holderGetter.getOrThrow(VegetationFeatures.TREES_WINDSWEPT_HILLS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference41 = holderGetter.getOrThrow(VegetationFeatures.TREES_WATER);
        Holder.Reference<ConfiguredFeature<?, ?>> reference42 = holderGetter.getOrThrow(VegetationFeatures.TREES_BIRCH_AND_OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> reference43 = holderGetter.getOrThrow(VegetationFeatures.TREES_SPARSE_JUNGLE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference44 = holderGetter.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA);
        Holder.Reference<ConfiguredFeature<?, ?>> reference45 = holderGetter.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
        Holder.Reference<ConfiguredFeature<?, ?>> reference46 = holderGetter.getOrThrow(VegetationFeatures.TREES_JUNGLE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference47 = holderGetter.getOrThrow(VegetationFeatures.BAMBOO_VEGETATION);
        Holder.Reference<ConfiguredFeature<?, ?>> reference48 = holderGetter.getOrThrow(VegetationFeatures.MUSHROOM_ISLAND_VEGETATION);
        Holder.Reference<ConfiguredFeature<?, ?>> reference49 = holderGetter.getOrThrow(VegetationFeatures.MANGROVE_VEGETATION);
        PlacementUtils.register(bootstrapContext, BAMBOO_LIGHT, reference, RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, BAMBOO, reference2, NoiseBasedCountPlacement.of(160, 80.0, 0.3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, VINES, reference3, CountPlacement.of(127), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_SUNFLOWER, reference4, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_PUMPKIN, reference5, RarityFilter.onAverageOnceEvery(300), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_PLAIN, reference6, NoiseThresholdCountPlacement.of(-0.8, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_FOREST, reference6, VegetationPlacements.worldSurfaceSquaredWithCount(2));
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_BADLANDS, reference6, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_SAVANNA, reference6, VegetationPlacements.worldSurfaceSquaredWithCount(20));
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_NORMAL, reference6, VegetationPlacements.worldSurfaceSquaredWithCount(5));
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_TAIGA_2, reference7, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_TAIGA, reference7, VegetationPlacements.worldSurfaceSquaredWithCount(7));
        PlacementUtils.register(bootstrapContext, PATCH_GRASS_JUNGLE, reference8, VegetationPlacements.worldSurfaceSquaredWithCount(25));
        PlacementUtils.register(bootstrapContext, GRASS_BONEMEAL, reference9, PlacementUtils.isEmpty());
        PlacementUtils.register(bootstrapContext, PATCH_DEAD_BUSH_2, reference10, VegetationPlacements.worldSurfaceSquaredWithCount(2));
        PlacementUtils.register(bootstrapContext, PATCH_DEAD_BUSH, reference10, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_DEAD_BUSH_BADLANDS, reference10, VegetationPlacements.worldSurfaceSquaredWithCount(20));
        PlacementUtils.register(bootstrapContext, PATCH_MELON, reference11, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_MELON_SPARSE, reference11, RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_BERRY_COMMON, reference12, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_BERRY_RARE, reference12, RarityFilter.onAverageOnceEvery(384), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_WATERLILY, reference13, VegetationPlacements.worldSurfaceSquaredWithCount(4));
        PlacementUtils.register(bootstrapContext, PATCH_TALL_GRASS_2, reference14, NoiseThresholdCountPlacement.of(-0.8, 0, 7), RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_TALL_GRASS, reference14, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_LARGE_FERN, reference15, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_CACTUS_DESERT, reference16, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_CACTUS_DECORATED, reference16, RarityFilter.onAverageOnceEvery(13), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_SUGAR_CANE_SWAMP, reference17, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_SUGAR_CANE_DESERT, reference17, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_SUGAR_CANE_BADLANDS, reference17, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, PATCH_SUGAR_CANE, reference17, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, BROWN_MUSHROOM_NETHER, reference18, RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, RED_MUSHROOM_NETHER, reference19, RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, BROWN_MUSHROOM_NORMAL, reference18, VegetationPlacements.getMushroomPlacement(256, null));
        PlacementUtils.register(bootstrapContext, RED_MUSHROOM_NORMAL, reference19, VegetationPlacements.getMushroomPlacement(512, null));
        PlacementUtils.register(bootstrapContext, BROWN_MUSHROOM_TAIGA, reference18, VegetationPlacements.getMushroomPlacement(4, null));
        PlacementUtils.register(bootstrapContext, RED_MUSHROOM_TAIGA, reference19, VegetationPlacements.getMushroomPlacement(256, null));
        PlacementUtils.register(bootstrapContext, BROWN_MUSHROOM_OLD_GROWTH, reference18, VegetationPlacements.getMushroomPlacement(4, CountPlacement.of(3)));
        PlacementUtils.register(bootstrapContext, RED_MUSHROOM_OLD_GROWTH, reference19, VegetationPlacements.getMushroomPlacement(171, null));
        PlacementUtils.register(bootstrapContext, BROWN_MUSHROOM_SWAMP, reference18, VegetationPlacements.getMushroomPlacement(0, CountPlacement.of(2)));
        PlacementUtils.register(bootstrapContext, RED_MUSHROOM_SWAMP, reference19, VegetationPlacements.getMushroomPlacement(64, null));
        PlacementUtils.register(bootstrapContext, FLOWER_WARM, reference20, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FLOWER_DEFAULT, reference20, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FLOWER_FLOWER_FOREST, reference21, CountPlacement.of(3), RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FLOWER_SWAMP, reference22, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FLOWER_PLAINS, reference23, NoiseThresholdCountPlacement.of(-0.8, 15, 4), RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FLOWER_CHERRY, reference25, NoiseThresholdCountPlacement.of(-0.8, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FLOWER_MEADOW, reference24, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        SurfaceWaterDepthFilter surfaceWaterDepthFilter = SurfaceWaterDepthFilter.forMaxDepth(0);
        PlacementUtils.register(bootstrapContext, TREES_PLAINS, reference26, PlacementUtils.countExtra(0, 0.05f, 1), InSquarePlacement.spread(), surfaceWaterDepthFilter, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, DARK_FOREST_VEGETATION, reference27, CountPlacement.of(16), InSquarePlacement.spread(), surfaceWaterDepthFilter, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FLOWER_FOREST_FLOWERS, reference28, RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, CountPlacement.of(ClampedInt.of(UniformInt.of(-1, 3), 0, 3)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, FOREST_FLOWERS, reference28, RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, TREES_FLOWER_FOREST, reference29, VegetationPlacements.treePlacement(PlacementUtils.countExtra(6, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_MEADOW, reference30, VegetationPlacements.treePlacement(RarityFilter.onAverageOnceEvery(100)));
        PlacementUtils.register(bootstrapContext, TREES_CHERRY, reference35, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1), Blocks.CHERRY_SAPLING));
        PlacementUtils.register(bootstrapContext, TREES_TAIGA, reference31, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_GROVE, reference32, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_BADLANDS, reference33, VegetationPlacements.treePlacement(PlacementUtils.countExtra(5, 0.1f, 1), Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, TREES_SNOWY, reference34, VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.1f, 1), Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstrapContext, TREES_SWAMP, reference36, PlacementUtils.countExtra(2, 0.1f, 1), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(2), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(), BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)));
        PlacementUtils.register(bootstrapContext, TREES_WINDSWEPT_SAVANNA, reference37, VegetationPlacements.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_SAVANNA, reference37, VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, BIRCH_TALL, reference38, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_BIRCH, reference39, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1), Blocks.BIRCH_SAPLING));
        PlacementUtils.register(bootstrapContext, TREES_WINDSWEPT_FOREST, reference40, VegetationPlacements.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_WINDSWEPT_HILLS, reference40, VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_WATER, reference41, VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_BIRCH_AND_OAK, reference42, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_SPARSE_JUNGLE, reference43, VegetationPlacements.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_OLD_GROWTH_SPRUCE_TAIGA, reference44, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_OLD_GROWTH_PINE_TAIGA, reference45, VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, TREES_JUNGLE, reference46, VegetationPlacements.treePlacement(PlacementUtils.countExtra(50, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, BAMBOO_VEGETATION, reference47, VegetationPlacements.treePlacement(PlacementUtils.countExtra(30, 0.1f, 1)));
        PlacementUtils.register(bootstrapContext, MUSHROOM_ISLAND_VEGETATION, reference48, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, TREES_MANGROVE, reference49, CountPlacement.of(25), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(5), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(), BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.MANGROVE_PROPAGULE.defaultBlockState(), BlockPos.ZERO)));
    }
}

