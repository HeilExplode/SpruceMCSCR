/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class TreePlacements {
    public static final ResourceKey<PlacedFeature> CRIMSON_FUNGI = PlacementUtils.createKey("crimson_fungi");
    public static final ResourceKey<PlacedFeature> WARPED_FUNGI = PlacementUtils.createKey("warped_fungi");
    public static final ResourceKey<PlacedFeature> OAK_CHECKED = PlacementUtils.createKey("oak_checked");
    public static final ResourceKey<PlacedFeature> DARK_OAK_CHECKED = PlacementUtils.createKey("dark_oak_checked");
    public static final ResourceKey<PlacedFeature> BIRCH_CHECKED = PlacementUtils.createKey("birch_checked");
    public static final ResourceKey<PlacedFeature> ACACIA_CHECKED = PlacementUtils.createKey("acacia_checked");
    public static final ResourceKey<PlacedFeature> SPRUCE_CHECKED = PlacementUtils.createKey("spruce_checked");
    public static final ResourceKey<PlacedFeature> MANGROVE_CHECKED = PlacementUtils.createKey("mangrove_checked");
    public static final ResourceKey<PlacedFeature> CHERRY_CHECKED = PlacementUtils.createKey("cherry_checked");
    public static final ResourceKey<PlacedFeature> PINE_ON_SNOW = PlacementUtils.createKey("pine_on_snow");
    public static final ResourceKey<PlacedFeature> SPRUCE_ON_SNOW = PlacementUtils.createKey("spruce_on_snow");
    public static final ResourceKey<PlacedFeature> PINE_CHECKED = PlacementUtils.createKey("pine_checked");
    public static final ResourceKey<PlacedFeature> JUNGLE_TREE_CHECKED = PlacementUtils.createKey("jungle_tree");
    public static final ResourceKey<PlacedFeature> FANCY_OAK_CHECKED = PlacementUtils.createKey("fancy_oak_checked");
    public static final ResourceKey<PlacedFeature> MEGA_JUNGLE_TREE_CHECKED = PlacementUtils.createKey("mega_jungle_tree_checked");
    public static final ResourceKey<PlacedFeature> MEGA_SPRUCE_CHECKED = PlacementUtils.createKey("mega_spruce_checked");
    public static final ResourceKey<PlacedFeature> MEGA_PINE_CHECKED = PlacementUtils.createKey("mega_pine_checked");
    public static final ResourceKey<PlacedFeature> TALL_MANGROVE_CHECKED = PlacementUtils.createKey("tall_mangrove_checked");
    public static final ResourceKey<PlacedFeature> JUNGLE_BUSH = PlacementUtils.createKey("jungle_bush");
    public static final ResourceKey<PlacedFeature> SUPER_BIRCH_BEES_0002 = PlacementUtils.createKey("super_birch_bees_0002");
    public static final ResourceKey<PlacedFeature> SUPER_BIRCH_BEES = PlacementUtils.createKey("super_birch_bees");
    public static final ResourceKey<PlacedFeature> OAK_BEES_0002 = PlacementUtils.createKey("oak_bees_0002");
    public static final ResourceKey<PlacedFeature> OAK_BEES_002 = PlacementUtils.createKey("oak_bees_002");
    public static final ResourceKey<PlacedFeature> BIRCH_BEES_0002_PLACED = PlacementUtils.createKey("birch_bees_0002");
    public static final ResourceKey<PlacedFeature> BIRCH_BEES_002 = PlacementUtils.createKey("birch_bees_002");
    public static final ResourceKey<PlacedFeature> FANCY_OAK_BEES_0002 = PlacementUtils.createKey("fancy_oak_bees_0002");
    public static final ResourceKey<PlacedFeature> FANCY_OAK_BEES_002 = PlacementUtils.createKey("fancy_oak_bees_002");
    public static final ResourceKey<PlacedFeature> FANCY_OAK_BEES = PlacementUtils.createKey("fancy_oak_bees");
    public static final ResourceKey<PlacedFeature> CHERRY_BEES_005 = PlacementUtils.createKey("cherry_bees_005");

    public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapContext) {
        HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference = holderGetter.getOrThrow(TreeFeatures.CRIMSON_FUNGUS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference2 = holderGetter.getOrThrow(TreeFeatures.WARPED_FUNGUS);
        Holder.Reference<ConfiguredFeature<?, ?>> reference3 = holderGetter.getOrThrow(TreeFeatures.OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> reference4 = holderGetter.getOrThrow(TreeFeatures.DARK_OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> reference5 = holderGetter.getOrThrow(TreeFeatures.BIRCH);
        Holder.Reference<ConfiguredFeature<?, ?>> reference6 = holderGetter.getOrThrow(TreeFeatures.ACACIA);
        Holder.Reference<ConfiguredFeature<?, ?>> reference7 = holderGetter.getOrThrow(TreeFeatures.SPRUCE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference8 = holderGetter.getOrThrow(TreeFeatures.MANGROVE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference9 = holderGetter.getOrThrow(TreeFeatures.CHERRY);
        Holder.Reference<ConfiguredFeature<?, ?>> reference10 = holderGetter.getOrThrow(TreeFeatures.PINE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference11 = holderGetter.getOrThrow(TreeFeatures.JUNGLE_TREE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference12 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> reference13 = holderGetter.getOrThrow(TreeFeatures.MEGA_JUNGLE_TREE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference14 = holderGetter.getOrThrow(TreeFeatures.MEGA_SPRUCE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference15 = holderGetter.getOrThrow(TreeFeatures.MEGA_PINE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference16 = holderGetter.getOrThrow(TreeFeatures.TALL_MANGROVE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference17 = holderGetter.getOrThrow(TreeFeatures.JUNGLE_BUSH);
        Holder.Reference<ConfiguredFeature<?, ?>> reference18 = holderGetter.getOrThrow(TreeFeatures.SUPER_BIRCH_BEES_0002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference19 = holderGetter.getOrThrow(TreeFeatures.SUPER_BIRCH_BEES);
        Holder.Reference<ConfiguredFeature<?, ?>> reference20 = holderGetter.getOrThrow(TreeFeatures.OAK_BEES_0002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference21 = holderGetter.getOrThrow(TreeFeatures.OAK_BEES_002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference22 = holderGetter.getOrThrow(TreeFeatures.BIRCH_BEES_0002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference23 = holderGetter.getOrThrow(TreeFeatures.BIRCH_BEES_002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference24 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK_BEES_0002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference25 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK_BEES_002);
        Holder.Reference<ConfiguredFeature<?, ?>> reference26 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK_BEES);
        Holder.Reference<ConfiguredFeature<?, ?>> reference27 = holderGetter.getOrThrow(TreeFeatures.CHERRY_BEES_005);
        PlacementUtils.register(bootstrapContext, CRIMSON_FUNGI, reference, CountOnEveryLayerPlacement.of(8), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, WARPED_FUNGI, reference2, CountOnEveryLayerPlacement.of(8), BiomeFilter.biome());
        PlacementUtils.register(bootstrapContext, OAK_CHECKED, reference3, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, DARK_OAK_CHECKED, reference4, PlacementUtils.filteredByBlockSurvival(Blocks.DARK_OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, BIRCH_CHECKED, reference5, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
        PlacementUtils.register(bootstrapContext, ACACIA_CHECKED, reference6, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
        PlacementUtils.register(bootstrapContext, SPRUCE_CHECKED, reference7, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstrapContext, MANGROVE_CHECKED, reference8, PlacementUtils.filteredByBlockSurvival(Blocks.MANGROVE_PROPAGULE));
        PlacementUtils.register(bootstrapContext, CHERRY_CHECKED, reference9, PlacementUtils.filteredByBlockSurvival(Blocks.CHERRY_SAPLING));
        BlockPredicate blockPredicate = BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
        List<PlacementModifier> list = List.of(EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.not(BlockPredicate.matchesBlocks(Blocks.POWDER_SNOW)), 8), BlockPredicateFilter.forPredicate(blockPredicate));
        PlacementUtils.register(bootstrapContext, PINE_ON_SNOW, reference10, list);
        PlacementUtils.register(bootstrapContext, SPRUCE_ON_SNOW, reference7, list);
        PlacementUtils.register(bootstrapContext, PINE_CHECKED, reference10, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstrapContext, JUNGLE_TREE_CHECKED, reference11, PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING));
        PlacementUtils.register(bootstrapContext, FANCY_OAK_CHECKED, reference12, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, MEGA_JUNGLE_TREE_CHECKED, reference13, PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING));
        PlacementUtils.register(bootstrapContext, MEGA_SPRUCE_CHECKED, reference14, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstrapContext, MEGA_PINE_CHECKED, reference15, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstrapContext, TALL_MANGROVE_CHECKED, reference16, PlacementUtils.filteredByBlockSurvival(Blocks.MANGROVE_PROPAGULE));
        PlacementUtils.register(bootstrapContext, JUNGLE_BUSH, reference17, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, SUPER_BIRCH_BEES_0002, reference18, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
        PlacementUtils.register(bootstrapContext, SUPER_BIRCH_BEES, reference19, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
        PlacementUtils.register(bootstrapContext, OAK_BEES_0002, reference20, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, OAK_BEES_002, reference21, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, BIRCH_BEES_0002_PLACED, reference22, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
        PlacementUtils.register(bootstrapContext, BIRCH_BEES_002, reference23, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
        PlacementUtils.register(bootstrapContext, FANCY_OAK_BEES_0002, reference24, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, FANCY_OAK_BEES_002, reference25, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, FANCY_OAK_BEES, reference26, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstrapContext, CHERRY_BEES_005, reference27, PlacementUtils.filteredByBlockSurvival(Blocks.CHERRY_SAPLING));
    }
}

