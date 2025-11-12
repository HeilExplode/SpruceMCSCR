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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ScatteredOreFeature
extends Feature<OreConfiguration> {
    private static final int MAX_DIST_FROM_ORIGIN = 7;

    ScatteredOreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        OreConfiguration oreConfiguration = featurePlaceContext.config();
        BlockPos blockPos = featurePlaceContext.origin();
        int n = randomSource.nextInt(oreConfiguration.size + 1);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block0: for (int i = 0; i < n; ++i) {
            this.offsetTargetPos(mutableBlockPos, randomSource, blockPos, Math.min(i, 7));
            BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
            for (OreConfiguration.TargetBlockState targetBlockState : oreConfiguration.targetStates) {
                if (!OreFeature.canPlaceOre(blockState, worldGenLevel::getBlockState, randomSource, oreConfiguration, targetBlockState, mutableBlockPos)) continue;
                worldGenLevel.setBlock(mutableBlockPos, targetBlockState.state, 2);
                continue block0;
            }
        }
        return true;
    }

    private void offsetTargetPos(BlockPos.MutableBlockPos mutableBlockPos, RandomSource randomSource, BlockPos blockPos, int n) {
        int n2 = this.getRandomPlacementInOneAxisRelativeToOrigin(randomSource, n);
        int n3 = this.getRandomPlacementInOneAxisRelativeToOrigin(randomSource, n);
        int n4 = this.getRandomPlacementInOneAxisRelativeToOrigin(randomSource, n);
        mutableBlockPos.setWithOffset(blockPos, n2, n3, n4);
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(RandomSource randomSource, int n) {
        return Math.round((randomSource.nextFloat() - randomSource.nextFloat()) * (float)n);
    }
}

