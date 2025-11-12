/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.AquaticFeatures;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext) {
        AquaticFeatures.bootstrap(bootstrapContext);
        CaveFeatures.bootstrap(bootstrapContext);
        EndFeatures.bootstrap(bootstrapContext);
        MiscOverworldFeatures.bootstrap(bootstrapContext);
        NetherFeatures.bootstrap(bootstrapContext);
        OreFeatures.bootstrap(bootstrapContext);
        PileFeatures.bootstrap(bootstrapContext);
        TreeFeatures.bootstrap(bootstrapContext);
        VegetationFeatures.bootstrap(bootstrapContext);
    }

    private static BlockPredicate simplePatchPredicate(List<Block> list) {
        BlockPredicate blockPredicate = !list.isEmpty() ? BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), list)) : BlockPredicate.ONLY_IN_AIR_PREDICATE;
        return blockPredicate;
    }

    public static RandomPatchConfiguration simpleRandomPatchConfiguration(int n, Holder<PlacedFeature> holder) {
        return new RandomPatchConfiguration(n, 7, 3, holder);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F f, FC FC, List<Block> list, int n) {
        return FeatureUtils.simpleRandomPatchConfiguration(n, PlacementUtils.filtered(f, FC, FeatureUtils.simplePatchPredicate(list)));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F f, FC FC, List<Block> list) {
        return FeatureUtils.simplePatchConfiguration(f, FC, list, 96);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F f, FC FC) {
        return FeatureUtils.simplePatchConfiguration(f, FC, List.of(), 96);
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String string) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.withDefaultNamespace(string));
    }

    public static void register(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext, ResourceKey<ConfiguredFeature<?, ?>> resourceKey, Feature<NoneFeatureConfiguration> feature) {
        FeatureUtils.register(bootstrapContext, resourceKey, feature, FeatureConfiguration.NONE);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext, ResourceKey<ConfiguredFeature<?, ?>> resourceKey, F f, FC FC) {
        bootstrapContext.register(resourceKey, new ConfiguredFeature<FC, F>(f, FC));
    }
}

