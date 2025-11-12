/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;

public class GeodeFeature
extends Feature<GeodeConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public GeodeFeature(Codec<GeodeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GeodeConfiguration> featurePlaceContext) {
        BlockState blockState;
        int n;
        int n2;
        GeodeConfiguration geodeConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        int n3 = geodeConfiguration.minGenOffset;
        int n4 = geodeConfiguration.maxGenOffset;
        LinkedList linkedList = Lists.newLinkedList();
        int n5 = geodeConfiguration.distributionPoints.sample(randomSource);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(worldGenLevel.getSeed()));
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -4, 1.0);
        LinkedList linkedList2 = Lists.newLinkedList();
        double d = (double)n5 / (double)geodeConfiguration.outerWallDistance.getMaxValue();
        GeodeLayerSettings geodeLayerSettings = geodeConfiguration.geodeLayerSettings;
        GeodeBlockSettings geodeBlockSettings = geodeConfiguration.geodeBlockSettings;
        GeodeCrackSettings geodeCrackSettings = geodeConfiguration.geodeCrackSettings;
        double d2 = 1.0 / Math.sqrt(geodeLayerSettings.filling);
        double d3 = 1.0 / Math.sqrt(geodeLayerSettings.innerLayer + d);
        double d4 = 1.0 / Math.sqrt(geodeLayerSettings.middleLayer + d);
        double d5 = 1.0 / Math.sqrt(geodeLayerSettings.outerLayer + d);
        double d6 = 1.0 / Math.sqrt(geodeCrackSettings.baseCrackSize + randomSource.nextDouble() / 2.0 + (n5 > 3 ? d : 0.0));
        boolean bl = (double)randomSource.nextFloat() < geodeCrackSettings.generateCrackChance;
        int n6 = 0;
        for (n2 = 0; n2 < n5; ++n2) {
            int n7;
            int n8;
            n = geodeConfiguration.outerWallDistance.sample(randomSource);
            BlockPos blockPos2 = blockPos.offset(n, n8 = geodeConfiguration.outerWallDistance.sample(randomSource), n7 = geodeConfiguration.outerWallDistance.sample(randomSource));
            blockState = worldGenLevel.getBlockState(blockPos2);
            if ((blockState.isAir() || blockState.is(BlockTags.GEODE_INVALID_BLOCKS)) && ++n6 > geodeConfiguration.invalidBlocksThreshold) {
                return false;
            }
            linkedList.add(Pair.of((Object)blockPos2, (Object)geodeConfiguration.pointOffset.sample(randomSource)));
        }
        if (bl) {
            n2 = randomSource.nextInt(4);
            n = n5 * 2 + 1;
            if (n2 == 0) {
                linkedList2.add(blockPos.offset(n, 7, 0));
                linkedList2.add(blockPos.offset(n, 5, 0));
                linkedList2.add(blockPos.offset(n, 1, 0));
            } else if (n2 == 1) {
                linkedList2.add(blockPos.offset(0, 7, n));
                linkedList2.add(blockPos.offset(0, 5, n));
                linkedList2.add(blockPos.offset(0, 1, n));
            } else if (n2 == 2) {
                linkedList2.add(blockPos.offset(n, 7, n));
                linkedList2.add(blockPos.offset(n, 5, n));
                linkedList2.add(blockPos.offset(n, 1, n));
            } else {
                linkedList2.add(blockPos.offset(0, 7, 0));
                linkedList2.add(blockPos.offset(0, 5, 0));
                linkedList2.add(blockPos.offset(0, 1, 0));
            }
        }
        ArrayList arrayList = Lists.newArrayList();
        Predicate<BlockState> predicate = GeodeFeature.isReplaceable(geodeConfiguration.geodeBlockSettings.cannotReplace);
        for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos.offset(n3, n3, n3), blockPos.offset(n4, n4, n4))) {
            double d7 = normalNoise.getValue(blockPos3.getX(), blockPos3.getY(), blockPos3.getZ()) * geodeConfiguration.noiseMultiplier;
            double d8 = 0.0;
            double d9 = 0.0;
            for (Object object : linkedList) {
                d8 += Mth.invSqrt(blockPos3.distSqr((Vec3i)object.getFirst()) + (double)((Integer)object.getSecond()).intValue()) + d7;
            }
            for (Object object : linkedList2) {
                d9 += Mth.invSqrt(blockPos3.distSqr((Vec3i)object) + (double)geodeCrackSettings.crackPointOffset) + d7;
            }
            if (d8 < d5) continue;
            if (bl && d9 >= d6 && d8 < d2) {
                this.safeSetBlock(worldGenLevel, blockPos3, Blocks.AIR.defaultBlockState(), predicate);
                for (Direction direction : DIRECTIONS) {
                    BlockPos blockPos4 = blockPos3.relative(direction);
                    FluidState fluidState = worldGenLevel.getFluidState(blockPos4);
                    if (fluidState.isEmpty()) continue;
                    worldGenLevel.scheduleTick(blockPos4, fluidState.getType(), 0);
                }
                continue;
            }
            if (d8 >= d2) {
                this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.fillingProvider.getState(randomSource, blockPos3), predicate);
                continue;
            }
            if (d8 >= d3) {
                boolean bl2;
                boolean bl3 = bl2 = (double)randomSource.nextFloat() < geodeConfiguration.useAlternateLayer0Chance;
                if (bl2) {
                    this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.alternateInnerLayerProvider.getState(randomSource, blockPos3), predicate);
                } else {
                    this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.innerLayerProvider.getState(randomSource, blockPos3), predicate);
                }
                if (geodeConfiguration.placementsRequireLayer0Alternate && !bl2 || !((double)randomSource.nextFloat() < geodeConfiguration.usePotentialPlacementsChance)) continue;
                arrayList.add(blockPos3.immutable());
                continue;
            }
            if (d8 >= d4) {
                this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.middleLayerProvider.getState(randomSource, blockPos3), predicate);
                continue;
            }
            if (!(d8 >= d5)) continue;
            this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.outerLayerProvider.getState(randomSource, blockPos3), predicate);
        }
        List<BlockState> list = geodeBlockSettings.innerPlacements;
        block5: for (BlockPos blockPos5 : arrayList) {
            blockState = (BlockState)Util.getRandom(list, randomSource);
            for (Direction direction : DIRECTIONS) {
                if (blockState.hasProperty(BlockStateProperties.FACING)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.FACING, direction);
                }
                BlockPos blockPos6 = blockPos5.relative(direction);
                BlockState blockState2 = worldGenLevel.getBlockState(blockPos6);
                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, blockState2.getFluidState().isSource());
                }
                if (!BuddingAmethystBlock.canClusterGrowAtState(blockState2)) continue;
                this.safeSetBlock(worldGenLevel, blockPos6, blockState, predicate);
                continue block5;
            }
        }
        return true;
    }
}

