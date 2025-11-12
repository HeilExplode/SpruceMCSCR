/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class BlockPileFeature
extends Feature<BlockPileConfiguration> {
    public BlockPileFeature(Codec<BlockPileConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockPileConfiguration> featurePlaceContext) {
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPileConfiguration blockPileConfiguration = featurePlaceContext.config();
        if (blockPos.getY() < worldGenLevel.getMinBuildHeight() + 5) {
            return false;
        }
        int n = 2 + randomSource.nextInt(2);
        int n2 = 2 + randomSource.nextInt(2);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-n, 0, -n2), blockPos.offset(n, 1, n2))) {
            int n3;
            int n4 = blockPos.getX() - blockPos2.getX();
            if ((float)(n4 * n4 + (n3 = blockPos.getZ() - blockPos2.getZ()) * n3) <= randomSource.nextFloat() * 10.0f - randomSource.nextFloat() * 6.0f) {
                this.tryPlaceBlock(worldGenLevel, blockPos2, randomSource, blockPileConfiguration);
                continue;
            }
            if (!((double)randomSource.nextFloat() < 0.031)) continue;
            this.tryPlaceBlock(worldGenLevel, blockPos2, randomSource, blockPileConfiguration);
        }
        return true;
    }

    private boolean mayPlaceOn(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = levelAccessor.getBlockState(blockPos2);
        if (blockState.is(Blocks.DIRT_PATH)) {
            return randomSource.nextBoolean();
        }
        return blockState.isFaceSturdy(levelAccessor, blockPos2, Direction.UP);
    }

    private void tryPlaceBlock(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, BlockPileConfiguration blockPileConfiguration) {
        if (levelAccessor.isEmptyBlock(blockPos) && this.mayPlaceOn(levelAccessor, blockPos, randomSource)) {
            levelAccessor.setBlock(blockPos, blockPileConfiguration.stateProvider.getState(randomSource, blockPos), 4);
        }
    }
}

