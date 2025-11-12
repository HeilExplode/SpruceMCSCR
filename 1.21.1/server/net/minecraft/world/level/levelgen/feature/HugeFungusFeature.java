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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.WeepingVinesFeature;

public class HugeFungusFeature
extends Feature<HugeFungusConfiguration> {
    private static final float HUGE_PROBABILITY = 0.06f;

    public HugeFungusFeature(Codec<HugeFungusConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeFungusConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
        HugeFungusConfiguration hugeFungusConfiguration = featurePlaceContext.config();
        Block block = hugeFungusConfiguration.validBaseState.getBlock();
        BlockPos blockPos2 = null;
        BlockState blockState = worldGenLevel.getBlockState(blockPos.below());
        if (blockState.is(block)) {
            blockPos2 = blockPos;
        }
        if (blockPos2 == null) {
            return false;
        }
        int n2 = Mth.nextInt(randomSource, 4, 13);
        if (randomSource.nextInt(12) == 0) {
            n2 *= 2;
        }
        if (!hugeFungusConfiguration.planted) {
            int n = chunkGenerator.getGenDepth();
            if (blockPos2.getY() + n2 + 1 >= n) {
                return false;
            }
        }
        boolean bl = !hugeFungusConfiguration.planted && randomSource.nextFloat() < 0.06f;
        worldGenLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
        this.placeStem(worldGenLevel, randomSource, hugeFungusConfiguration, blockPos2, n2, bl);
        this.placeHat(worldGenLevel, randomSource, hugeFungusConfiguration, blockPos2, n2, bl);
        return true;
    }

    private static boolean isReplaceable(WorldGenLevel worldGenLevel, BlockPos blockPos, HugeFungusConfiguration hugeFungusConfiguration, boolean bl) {
        if (worldGenLevel.isStateAtPosition(blockPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
            return true;
        }
        if (bl) {
            return hugeFungusConfiguration.replaceableBlocks.test(worldGenLevel, blockPos);
        }
        return false;
    }

    private void placeStem(WorldGenLevel worldGenLevel, RandomSource randomSource, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int n, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockState blockState = hugeFungusConfiguration.stemState;
        int n2 = bl ? 1 : 0;
        for (int i = -n2; i <= n2; ++i) {
            for (int j = -n2; j <= n2; ++j) {
                boolean bl2 = bl && Mth.abs(i) == n2 && Mth.abs(j) == n2;
                for (int k = 0; k < n; ++k) {
                    mutableBlockPos.setWithOffset(blockPos, i, k, j);
                    if (!HugeFungusFeature.isReplaceable(worldGenLevel, mutableBlockPos, hugeFungusConfiguration, true)) continue;
                    if (hugeFungusConfiguration.planted) {
                        if (!worldGenLevel.getBlockState((BlockPos)mutableBlockPos.below()).isAir()) {
                            worldGenLevel.destroyBlock(mutableBlockPos, true);
                        }
                        worldGenLevel.setBlock(mutableBlockPos, blockState, 3);
                        continue;
                    }
                    if (bl2) {
                        if (!(randomSource.nextFloat() < 0.1f)) continue;
                        this.setBlock(worldGenLevel, mutableBlockPos, blockState);
                        continue;
                    }
                    this.setBlock(worldGenLevel, mutableBlockPos, blockState);
                }
            }
        }
    }

    private void placeHat(WorldGenLevel worldGenLevel, RandomSource randomSource, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int n, boolean bl) {
        int n2;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        boolean bl2 = hugeFungusConfiguration.hatState.is(Blocks.NETHER_WART_BLOCK);
        int n3 = Math.min(randomSource.nextInt(1 + n / 3) + 5, n);
        for (int i = n2 = n - n3; i <= n; ++i) {
            int n4;
            int n5 = n4 = i < n - randomSource.nextInt(3) ? 2 : 1;
            if (n3 > 8 && i < n2 + 4) {
                n4 = 3;
            }
            if (bl) {
                ++n4;
            }
            for (int j = -n4; j <= n4; ++j) {
                for (int k = -n4; k <= n4; ++k) {
                    boolean bl3 = j == -n4 || j == n4;
                    boolean bl4 = k == -n4 || k == n4;
                    boolean bl5 = !bl3 && !bl4 && i != n;
                    boolean bl6 = bl3 && bl4;
                    boolean bl7 = i < n2 + 3;
                    mutableBlockPos.setWithOffset(blockPos, j, i, k);
                    if (!HugeFungusFeature.isReplaceable(worldGenLevel, mutableBlockPos, hugeFungusConfiguration, false)) continue;
                    if (hugeFungusConfiguration.planted && !worldGenLevel.getBlockState((BlockPos)mutableBlockPos.below()).isAir()) {
                        worldGenLevel.destroyBlock(mutableBlockPos, true);
                    }
                    if (bl7) {
                        if (bl5) continue;
                        this.placeHatDropBlock(worldGenLevel, randomSource, mutableBlockPos, hugeFungusConfiguration.hatState, bl2);
                        continue;
                    }
                    if (bl5) {
                        this.placeHatBlock(worldGenLevel, randomSource, hugeFungusConfiguration, mutableBlockPos, 0.1f, 0.2f, bl2 ? 0.1f : 0.0f);
                        continue;
                    }
                    if (bl6) {
                        this.placeHatBlock(worldGenLevel, randomSource, hugeFungusConfiguration, mutableBlockPos, 0.01f, 0.7f, bl2 ? 0.083f : 0.0f);
                        continue;
                    }
                    this.placeHatBlock(worldGenLevel, randomSource, hugeFungusConfiguration, mutableBlockPos, 5.0E-4f, 0.98f, bl2 ? 0.07f : 0.0f);
                }
            }
        }
    }

    private void placeHatBlock(LevelAccessor levelAccessor, RandomSource randomSource, HugeFungusConfiguration hugeFungusConfiguration, BlockPos.MutableBlockPos mutableBlockPos, float f, float f2, float f3) {
        if (randomSource.nextFloat() < f) {
            this.setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.decorState);
        } else if (randomSource.nextFloat() < f2) {
            this.setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.hatState);
            if (randomSource.nextFloat() < f3) {
                HugeFungusFeature.tryPlaceWeepingVines(mutableBlockPos, levelAccessor, randomSource);
            }
        }
    }

    private void placeHatDropBlock(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState, boolean bl) {
        if (levelAccessor.getBlockState(blockPos.below()).is(blockState.getBlock())) {
            this.setBlock(levelAccessor, blockPos, blockState);
        } else if ((double)randomSource.nextFloat() < 0.15) {
            this.setBlock(levelAccessor, blockPos, blockState);
            if (bl && randomSource.nextInt(11) == 0) {
                HugeFungusFeature.tryPlaceWeepingVines(blockPos, levelAccessor, randomSource);
            }
        }
    }

    private static void tryPlaceWeepingVines(BlockPos blockPos, LevelAccessor levelAccessor, RandomSource randomSource) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.DOWN);
        if (!levelAccessor.isEmptyBlock(mutableBlockPos)) {
            return;
        }
        int n = Mth.nextInt(randomSource, 1, 5);
        if (randomSource.nextInt(7) == 0) {
            n *= 2;
        }
        int n2 = 23;
        int n3 = 25;
        WeepingVinesFeature.placeWeepingVinesColumn(levelAccessor, randomSource, mutableBlockPos, n, 23, 25);
    }
}

