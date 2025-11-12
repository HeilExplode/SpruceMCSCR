/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

public class DripstoneUtils {
    protected static double getDripstoneHeight(double d, double d2, double d3, double d4) {
        if (d < d4) {
            d = d4;
        }
        double d5 = 0.384;
        double d6 = d / d2 * 0.384;
        double d7 = 0.75 * Math.pow(d6, 1.3333333333333333);
        double d8 = Math.pow(d6, 0.6666666666666666);
        double d9 = 0.3333333333333333 * Math.log(d6);
        double d10 = d3 * (d7 - d8 - d9);
        d10 = Math.max(d10, 0.0);
        return d10 / 0.384 * d2;
    }

    protected static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel worldGenLevel, BlockPos blockPos, int n) {
        if (DripstoneUtils.isEmptyOrWaterOrLava(worldGenLevel, blockPos)) {
            return false;
        }
        float f = 6.0f;
        float f2 = 6.0f / (float)n;
        for (float f3 = 0.0f; f3 < (float)Math.PI * 2; f3 += f2) {
            int n2;
            int n3 = (int)(Mth.cos(f3) * (float)n);
            if (!DripstoneUtils.isEmptyOrWaterOrLava(worldGenLevel, blockPos.offset(n3, 0, n2 = (int)(Mth.sin(f3) * (float)n)))) continue;
            return false;
        }
        return true;
    }

    protected static boolean isEmptyOrWater(LevelAccessor levelAccessor, BlockPos blockPos) {
        return levelAccessor.isStateAtPosition(blockPos, DripstoneUtils::isEmptyOrWater);
    }

    protected static boolean isEmptyOrWaterOrLava(LevelAccessor levelAccessor, BlockPos blockPos) {
        return levelAccessor.isStateAtPosition(blockPos, DripstoneUtils::isEmptyOrWaterOrLava);
    }

    protected static void buildBaseToTipColumn(Direction direction, int n, boolean bl, Consumer<BlockState> consumer) {
        if (n >= 3) {
            consumer.accept(DripstoneUtils.createPointedDripstone(direction, DripstoneThickness.BASE));
            for (int i = 0; i < n - 3; ++i) {
                consumer.accept(DripstoneUtils.createPointedDripstone(direction, DripstoneThickness.MIDDLE));
            }
        }
        if (n >= 2) {
            consumer.accept(DripstoneUtils.createPointedDripstone(direction, DripstoneThickness.FRUSTUM));
        }
        if (n >= 1) {
            consumer.accept(DripstoneUtils.createPointedDripstone(direction, bl ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
        }
    }

    protected static void growPointedDripstone(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, int n, boolean bl) {
        if (!DripstoneUtils.isDripstoneBase(levelAccessor.getBlockState(blockPos.relative(direction.getOpposite())))) {
            return;
        }
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        DripstoneUtils.buildBaseToTipColumn(direction, n, bl, blockState -> {
            if (blockState.is(Blocks.POINTED_DRIPSTONE)) {
                blockState = (BlockState)blockState.setValue(PointedDripstoneBlock.WATERLOGGED, levelAccessor.isWaterAt(mutableBlockPos));
            }
            levelAccessor.setBlock(mutableBlockPos, (BlockState)blockState, 2);
            mutableBlockPos.move(direction);
        });
    }

    protected static boolean placeDripstoneBlockIfPossible(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.is(BlockTags.DRIPSTONE_REPLACEABLE)) {
            levelAccessor.setBlock(blockPos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
            return true;
        }
        return false;
    }

    private static BlockState createPointedDripstone(Direction direction, DripstoneThickness dripstoneThickness) {
        return (BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)).setValue(PointedDripstoneBlock.THICKNESS, dripstoneThickness);
    }

    public static boolean isDripstoneBaseOrLava(BlockState blockState) {
        return DripstoneUtils.isDripstoneBase(blockState) || blockState.is(Blocks.LAVA);
    }

    public static boolean isDripstoneBase(BlockState blockState) {
        return blockState.is(Blocks.DRIPSTONE_BLOCK) || blockState.is(BlockTags.DRIPSTONE_REPLACEABLE);
    }

    public static boolean isEmptyOrWater(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER);
    }

    public static boolean isNeitherEmptyNorWater(BlockState blockState) {
        return !blockState.isAir() && !blockState.is(Blocks.WATER);
    }

    public static boolean isEmptyOrWaterOrLava(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA);
    }
}

