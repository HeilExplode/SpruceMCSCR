/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature
extends Feature<DripstoneClusterConfiguration> {
    public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        DripstoneClusterConfiguration dripstoneClusterConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        if (!DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos)) {
            return false;
        }
        int n = dripstoneClusterConfiguration.height.sample(randomSource);
        float f = dripstoneClusterConfiguration.wetness.sample(randomSource);
        float f2 = dripstoneClusterConfiguration.density.sample(randomSource);
        int n2 = dripstoneClusterConfiguration.radius.sample(randomSource);
        int n3 = dripstoneClusterConfiguration.radius.sample(randomSource);
        for (int i = -n2; i <= n2; ++i) {
            for (int j = -n3; j <= n3; ++j) {
                double d = this.getChanceOfStalagmiteOrStalactite(n2, n3, i, j, dripstoneClusterConfiguration);
                BlockPos blockPos2 = blockPos.offset(i, 0, j);
                this.placeColumn(worldGenLevel, randomSource, blockPos2, i, j, f, d, n, f2, dripstoneClusterConfiguration);
            }
        }
        return true;
    }

    private void placeColumn(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos, int n, int n2, float f, double d, int n3, float f2, DripstoneClusterConfiguration dripstoneClusterConfiguration) {
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        boolean bl;
        Column column;
        boolean bl2;
        Optional<Column> optional = Column.scan(worldGenLevel, blockPos, dripstoneClusterConfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);
        if (optional.isEmpty()) {
            return;
        }
        OptionalInt optionalInt = optional.get().getCeiling();
        OptionalInt optionalInt2 = optional.get().getFloor();
        if (optionalInt.isEmpty() && optionalInt2.isEmpty()) {
            return;
        }
        boolean bl3 = bl2 = randomSource.nextFloat() < f;
        if (bl2 && optionalInt2.isPresent() && this.canPlacePool(worldGenLevel, blockPos.atY(optionalInt2.getAsInt()))) {
            int n10 = optionalInt2.getAsInt();
            column = optional.get().withFloor(OptionalInt.of(n10 - 1));
            worldGenLevel.setBlock(blockPos.atY(n10), Blocks.WATER.defaultBlockState(), 2);
        } else {
            column = optional.get();
        }
        OptionalInt optionalInt3 = column.getFloor();
        boolean bl4 = bl = randomSource.nextDouble() < d;
        if (optionalInt.isPresent() && bl && !this.isLava(worldGenLevel, blockPos.atY(optionalInt.getAsInt()))) {
            n9 = dripstoneClusterConfiguration.dripstoneBlockLayerThickness.sample(randomSource);
            this.replaceBlocksWithDripstoneBlocks(worldGenLevel, blockPos.atY(optionalInt.getAsInt()), n9, Direction.UP);
            n8 = optionalInt3.isPresent() ? Math.min(n3, optionalInt.getAsInt() - optionalInt3.getAsInt()) : n3;
            n7 = this.getDripstoneHeight(randomSource, n, n2, f2, n8, dripstoneClusterConfiguration);
        } else {
            n7 = 0;
        }
        int n11 = n8 = randomSource.nextDouble() < d ? 1 : 0;
        if (optionalInt3.isPresent() && n8 != 0 && !this.isLava(worldGenLevel, blockPos.atY(optionalInt3.getAsInt()))) {
            n6 = dripstoneClusterConfiguration.dripstoneBlockLayerThickness.sample(randomSource);
            this.replaceBlocksWithDripstoneBlocks(worldGenLevel, blockPos.atY(optionalInt3.getAsInt()), n6, Direction.DOWN);
            n9 = optionalInt.isPresent() ? Math.max(0, n7 + Mth.randomBetweenInclusive(randomSource, -dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff, dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff)) : this.getDripstoneHeight(randomSource, n, n2, f2, n3, dripstoneClusterConfiguration);
        } else {
            n9 = 0;
        }
        if (optionalInt.isPresent() && optionalInt3.isPresent() && optionalInt.getAsInt() - n7 <= optionalInt3.getAsInt() + n9) {
            n5 = optionalInt3.getAsInt();
            int n12 = optionalInt.getAsInt();
            int n13 = Math.max(n12 - n7, n5 + 1);
            int n14 = Math.min(n5 + n9, n12 - 1);
            int n15 = Mth.randomBetweenInclusive(randomSource, n13, n14 + 1);
            int n16 = n15 - 1;
            n6 = n12 - n15;
            n4 = n16 - n5;
        } else {
            n6 = n7;
            n4 = n9;
        }
        int n17 = n5 = randomSource.nextBoolean() && n6 > 0 && n4 > 0 && column.getHeight().isPresent() && n6 + n4 == column.getHeight().getAsInt() ? 1 : 0;
        if (optionalInt.isPresent()) {
            DripstoneUtils.growPointedDripstone(worldGenLevel, blockPos.atY(optionalInt.getAsInt() - 1), Direction.DOWN, n6, n5 != 0);
        }
        if (optionalInt3.isPresent()) {
            DripstoneUtils.growPointedDripstone(worldGenLevel, blockPos.atY(optionalInt3.getAsInt() + 1), Direction.UP, n4, n5 != 0);
        }
    }

    private boolean isLava(LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos).is(Blocks.LAVA);
    }

    private int getDripstoneHeight(RandomSource randomSource, int n, int n2, float f, int n3, DripstoneClusterConfiguration dripstoneClusterConfiguration) {
        if (randomSource.nextFloat() > f) {
            return 0;
        }
        int n4 = Math.abs(n) + Math.abs(n2);
        float f2 = (float)Mth.clampedMap((double)n4, 0.0, (double)dripstoneClusterConfiguration.maxDistanceFromCenterAffectingHeightBias, (double)n3 / 2.0, 0.0);
        return (int)DripstoneClusterFeature.randomBetweenBiased(randomSource, 0.0f, n3, f2, dripstoneClusterConfiguration.heightDeviation);
    }

    private boolean canPlacePool(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        BlockState blockState = worldGenLevel.getBlockState(blockPos);
        if (blockState.is(Blocks.WATER) || blockState.is(Blocks.DRIPSTONE_BLOCK) || blockState.is(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        if (worldGenLevel.getBlockState(blockPos.above()).getFluidState().is(FluidTags.WATER)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.canBeAdjacentToWater(worldGenLevel, blockPos.relative(direction))) continue;
            return false;
        }
        return this.canBeAdjacentToWater(worldGenLevel, blockPos.below());
    }

    private boolean canBeAdjacentToWater(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return blockState.is(BlockTags.BASE_STONE_OVERWORLD) || blockState.getFluidState().is(FluidTags.WATER);
    }

    private void replaceBlocksWithDripstoneBlocks(WorldGenLevel worldGenLevel, BlockPos blockPos, int n, Direction direction) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 0; i < n; ++i) {
            if (!DripstoneUtils.placeDripstoneBlockIfPossible(worldGenLevel, mutableBlockPos)) {
                return;
            }
            mutableBlockPos.move(direction);
        }
    }

    private double getChanceOfStalagmiteOrStalactite(int n, int n2, int n3, int n4, DripstoneClusterConfiguration dripstoneClusterConfiguration) {
        int n5 = n - Math.abs(n3);
        int n6 = n2 - Math.abs(n4);
        int n7 = Math.min(n5, n6);
        return Mth.clampedMap(n7, 0.0f, dripstoneClusterConfiguration.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, dripstoneClusterConfiguration.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0f);
    }

    private static float randomBetweenBiased(RandomSource randomSource, float f, float f2, float f3, float f4) {
        return ClampedNormalFloat.sample(randomSource, f3, f4, f, f2);
    }
}

