/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

public class DeltaFeature
extends Feature<DeltaFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of((Object)Blocks.BEDROCK, (Object)Blocks.NETHER_BRICKS, (Object)Blocks.NETHER_BRICK_FENCE, (Object)Blocks.NETHER_BRICK_STAIRS, (Object)Blocks.NETHER_WART, (Object)Blocks.CHEST, (Object)Blocks.SPAWNER);
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final double RIM_SPAWN_CHANCE = 0.9;

    public DeltaFeature(Codec<DeltaFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DeltaFeatureConfiguration> featurePlaceContext) {
        boolean bl = false;
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        DeltaFeatureConfiguration deltaFeatureConfiguration = featurePlaceContext.config();
        BlockPos blockPos = featurePlaceContext.origin();
        boolean bl2 = randomSource.nextDouble() < 0.9;
        int n = bl2 ? deltaFeatureConfiguration.rimSize().sample(randomSource) : 0;
        int n2 = bl2 ? deltaFeatureConfiguration.rimSize().sample(randomSource) : 0;
        boolean bl3 = bl2 && n != 0 && n2 != 0;
        int n3 = deltaFeatureConfiguration.size().sample(randomSource);
        int n4 = deltaFeatureConfiguration.size().sample(randomSource);
        int n5 = Math.max(n3, n4);
        for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, n3, 0, n4)) {
            BlockPos blockPos3;
            if (blockPos2.distManhattan(blockPos) > n5) break;
            if (!DeltaFeature.isClear(worldGenLevel, blockPos2, deltaFeatureConfiguration)) continue;
            if (bl3) {
                bl = true;
                this.setBlock(worldGenLevel, blockPos2, deltaFeatureConfiguration.rim());
            }
            if (!DeltaFeature.isClear(worldGenLevel, blockPos3 = blockPos2.offset(n, 0, n2), deltaFeatureConfiguration)) continue;
            bl = true;
            this.setBlock(worldGenLevel, blockPos3, deltaFeatureConfiguration.contents());
        }
        return bl;
    }

    private static boolean isClear(LevelAccessor levelAccessor, BlockPos blockPos, DeltaFeatureConfiguration deltaFeatureConfiguration) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.is(deltaFeatureConfiguration.contents().getBlock())) {
            return false;
        }
        if (CANNOT_REPLACE.contains((Object)blockState.getBlock())) {
            return false;
        }
        for (Direction direction : DIRECTIONS) {
            boolean bl = levelAccessor.getBlockState(blockPos.relative(direction)).isAir();
            if ((!bl || direction == Direction.UP) && (bl || direction != Direction.UP)) continue;
            return false;
        }
        return true;
    }
}

