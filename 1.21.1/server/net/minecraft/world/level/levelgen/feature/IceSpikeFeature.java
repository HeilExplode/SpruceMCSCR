/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSpikeFeature
extends Feature<NoneFeatureConfiguration> {
    public IceSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        int n;
        int n2;
        BlockPos blockPos = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > worldGenLevel.getMinBuildHeight() + 2) {
            blockPos = blockPos.below();
        }
        if (!worldGenLevel.getBlockState(blockPos).is(Blocks.SNOW_BLOCK)) {
            return false;
        }
        blockPos = blockPos.above(randomSource.nextInt(4));
        int n3 = randomSource.nextInt(4) + 7;
        int n4 = n3 / 4 + randomSource.nextInt(2);
        if (n4 > 1 && randomSource.nextInt(60) == 0) {
            blockPos = blockPos.above(10 + randomSource.nextInt(30));
        }
        for (n2 = 0; n2 < n3; ++n2) {
            float f = (1.0f - (float)n2 / (float)n3) * (float)n4;
            n = Mth.ceil(f);
            for (int i = -n; i <= n; ++i) {
                float f2 = (float)Mth.abs(i) - 0.25f;
                for (int j = -n; j <= n; ++j) {
                    float f3 = (float)Mth.abs(j) - 0.25f;
                    if ((i != 0 || j != 0) && f2 * f2 + f3 * f3 > f * f || (i == -n || i == n || j == -n || j == n) && randomSource.nextFloat() > 0.75f) continue;
                    BlockState blockState = worldGenLevel.getBlockState(blockPos.offset(i, n2, j));
                    if (blockState.isAir() || IceSpikeFeature.isDirt(blockState) || blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.ICE)) {
                        this.setBlock(worldGenLevel, blockPos.offset(i, n2, j), Blocks.PACKED_ICE.defaultBlockState());
                    }
                    if (n2 == 0 || n <= 1 || !(blockState = worldGenLevel.getBlockState(blockPos.offset(i, -n2, j))).isAir() && !IceSpikeFeature.isDirt(blockState) && !blockState.is(Blocks.SNOW_BLOCK) && !blockState.is(Blocks.ICE)) continue;
                    this.setBlock(worldGenLevel, blockPos.offset(i, -n2, j), Blocks.PACKED_ICE.defaultBlockState());
                }
            }
        }
        n2 = n4 - 1;
        if (n2 < 0) {
            n2 = 0;
        } else if (n2 > 1) {
            n2 = 1;
        }
        for (int i = -n2; i <= n2; ++i) {
            for (n = -n2; n <= n2; ++n) {
                BlockState blockState;
                BlockPos blockPos2 = blockPos.offset(i, -1, n);
                int n5 = 50;
                if (Math.abs(i) == 1 && Math.abs(n) == 1) {
                    n5 = randomSource.nextInt(5);
                }
                while (blockPos2.getY() > 50 && ((blockState = worldGenLevel.getBlockState(blockPos2)).isAir() || IceSpikeFeature.isDirt(blockState) || blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.ICE) || blockState.is(Blocks.PACKED_ICE))) {
                    this.setBlock(worldGenLevel, blockPos2, Blocks.PACKED_ICE.defaultBlockState());
                    blockPos2 = blockPos2.below();
                    if (--n5 > 0) continue;
                    blockPos2 = blockPos2.below(randomSource.nextInt(5) + 1);
                    n5 = randomSource.nextInt(5);
                }
            }
        }
        return true;
    }
}

