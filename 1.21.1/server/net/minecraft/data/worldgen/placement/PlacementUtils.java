/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.data.worldgen.placement.VillagePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class PlacementUtils {
    public static final PlacementModifier HEIGHTMAP = HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING);
    public static final PlacementModifier HEIGHTMAP_TOP_SOLID = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR_WG);
    public static final PlacementModifier HEIGHTMAP_WORLD_SURFACE = HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG);
    public static final PlacementModifier HEIGHTMAP_OCEAN_FLOOR = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR);
    public static final PlacementModifier FULL_RANGE = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top());
    public static final PlacementModifier RANGE_10_10 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(10), VerticalAnchor.belowTop(10));
    public static final PlacementModifier RANGE_8_8 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.belowTop(8));
    public static final PlacementModifier RANGE_4_4 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(4), VerticalAnchor.belowTop(4));
    public static final PlacementModifier RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(256));

    public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapContext) {
        AquaticPlacements.bootstrap(bootstrapContext);
        CavePlacements.bootstrap(bootstrapContext);
        EndPlacements.bootstrap(bootstrapContext);
        MiscOverworldPlacements.bootstrap(bootstrapContext);
        NetherPlacements.bootstrap(bootstrapContext);
        OrePlacements.bootstrap(bootstrapContext);
        TreePlacements.bootstrap(bootstrapContext);
        VegetationPlacements.bootstrap(bootstrapContext);
        VillagePlacements.bootstrap(bootstrapContext);
    }

    public static ResourceKey<PlacedFeature> createKey(String string) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.withDefaultNamespace(string));
    }

    public static void register(BootstrapContext<PlacedFeature> bootstrapContext, ResourceKey<PlacedFeature> resourceKey, Holder<ConfiguredFeature<?, ?>> holder, List<PlacementModifier> list) {
        bootstrapContext.register(resourceKey, new PlacedFeature(holder, List.copyOf(list)));
    }

    public static void register(BootstrapContext<PlacedFeature> bootstrapContext, ResourceKey<PlacedFeature> resourceKey, Holder<ConfiguredFeature<?, ?>> holder, PlacementModifier ... placementModifierArray) {
        PlacementUtils.register(bootstrapContext, resourceKey, holder, List.of(placementModifierArray));
    }

    public static PlacementModifier countExtra(int n, float f, int n2) {
        float f2 = 1.0f / f;
        if (Math.abs(f2 - (float)((int)f2)) > 1.0E-5f) {
            throw new IllegalStateException("Chance data cannot be represented as list weight");
        }
        SimpleWeightedRandomList<IntProvider> simpleWeightedRandomList = SimpleWeightedRandomList.builder().add(ConstantInt.of(n), (int)f2 - 1).add(ConstantInt.of(n + n2), 1).build();
        return CountPlacement.of(new WeightedListInt(simpleWeightedRandomList));
    }

    public static PlacementFilter isEmpty() {
        return BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE);
    }

    public static BlockPredicateFilter filteredByBlockSurvival(Block block) {
        return BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO));
    }

    public static Holder<PlacedFeature> inlinePlaced(Holder<ConfiguredFeature<?, ?>> holder, PlacementModifier ... placementModifierArray) {
        return Holder.direct(new PlacedFeature(holder, List.of(placementModifierArray)));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> inlinePlaced(F f, FC FC, PlacementModifier ... placementModifierArray) {
        return PlacementUtils.inlinePlaced(Holder.direct(new ConfiguredFeature<FC, F>(f, FC)), placementModifierArray);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> onlyWhenEmpty(F f, FC FC) {
        return PlacementUtils.filtered(f, FC, BlockPredicate.ONLY_IN_AIR_PREDICATE);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> filtered(F f, FC FC, BlockPredicate blockPredicate) {
        return PlacementUtils.inlinePlaced(f, FC, BlockPredicateFilter.forPredicate(blockPredicate));
    }
}

