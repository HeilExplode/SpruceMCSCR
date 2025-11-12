/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class SeagrassFeature
extends Feature<ProbabilityFeatureConfiguration> {
    public SeagrassFeature(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> featurePlaceContext) {
        boolean bl = false;
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        ProbabilityFeatureConfiguration probabilityFeatureConfiguration = featurePlaceContext.config();
        int n = randomSource.nextInt(8) - randomSource.nextInt(8);
        int n2 = randomSource.nextInt(8) - randomSource.nextInt(8);
        int n3 = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + n, blockPos.getZ() + n2);
        BlockPos blockPos2 = new BlockPos(blockPos.getX() + n, n3, blockPos.getZ() + n2);
        if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER)) {
            BlockState blockState;
            boolean bl2 = randomSource.nextDouble() < (double)probabilityFeatureConfiguration.probability;
            BlockState blockState2 = blockState = bl2 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
            if (blockState.canSurvive(worldGenLevel, blockPos2)) {
                if (bl2) {
                    BlockState blockState3 = (BlockState)blockState.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
                    BlockPos blockPos3 = blockPos2.above();
                    if (worldGenLevel.getBlockState(blockPos3).is(Blocks.WATER)) {
                        worldGenLevel.setBlock(blockPos2, blockState, 2);
                        worldGenLevel.setBlock(blockPos3, blockState3, 2);
                    }
                } else {
                    worldGenLevel.setBlock(blockPos2, blockState, 2);
                }
                bl = true;
            }
        }
        return bl;
    }
}

