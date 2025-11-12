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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;

public class TwistingVinesFeature
extends Feature<TwistingVinesConfig> {
    public TwistingVinesFeature(Codec<TwistingVinesConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<TwistingVinesConfig> featurePlaceContext) {
        BlockPos blockPos;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        if (TwistingVinesFeature.isInvalidPlacementLocation(worldGenLevel, blockPos = featurePlaceContext.origin())) {
            return false;
        }
        RandomSource randomSource = featurePlaceContext.random();
        TwistingVinesConfig twistingVinesConfig = featurePlaceContext.config();
        int n = twistingVinesConfig.spreadWidth();
        int n2 = twistingVinesConfig.spreadHeight();
        int n3 = twistingVinesConfig.maxHeight();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < n * n; ++i) {
            mutableBlockPos.set(blockPos).move(Mth.nextInt(randomSource, -n, n), Mth.nextInt(randomSource, -n2, n2), Mth.nextInt(randomSource, -n, n));
            if (!TwistingVinesFeature.findFirstAirBlockAboveGround(worldGenLevel, mutableBlockPos) || TwistingVinesFeature.isInvalidPlacementLocation(worldGenLevel, mutableBlockPos)) continue;
            int n4 = Mth.nextInt(randomSource, 1, n3);
            if (randomSource.nextInt(6) == 0) {
                n4 *= 2;
            }
            if (randomSource.nextInt(5) == 0) {
                n4 = 1;
            }
            int n5 = 17;
            int n6 = 25;
            TwistingVinesFeature.placeWeepingVinesColumn(worldGenLevel, randomSource, mutableBlockPos, n4, 17, 25);
        }
        return true;
    }

    private static boolean findFirstAirBlockAboveGround(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos) {
        do {
            mutableBlockPos.move(0, -1, 0);
            if (!levelAccessor.isOutsideBuildHeight(mutableBlockPos)) continue;
            return false;
        } while (levelAccessor.getBlockState(mutableBlockPos).isAir());
        mutableBlockPos.move(0, 1, 0);
        return true;
    }

    public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, int n, int n2, int n3) {
        for (int i = 1; i <= n; ++i) {
            if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
                if (i == n || !levelAccessor.isEmptyBlock((BlockPos)mutableBlockPos.above())) {
                    levelAccessor.setBlock(mutableBlockPos, (BlockState)Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Mth.nextInt(randomSource, n2, n3)), 2);
                    break;
                }
                levelAccessor.setBlock(mutableBlockPos, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
            }
            mutableBlockPos.move(Direction.UP);
        }
    }

    private static boolean isInvalidPlacementLocation(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (!levelAccessor.isEmptyBlock(blockPos)) {
            return true;
        }
        BlockState blockState = levelAccessor.getBlockState(blockPos.below());
        return !blockState.is(Blocks.NETHERRACK) && !blockState.is(Blocks.WARPED_NYLIUM) && !blockState.is(Blocks.WARPED_WART_BLOCK);
    }
}

