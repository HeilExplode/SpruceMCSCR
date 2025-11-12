/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;

public class RootSystemFeature
extends Feature<RootSystemConfiguration> {
    public RootSystemFeature(Codec<RootSystemConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RootSystemConfiguration> featurePlaceContext) {
        BlockPos blockPos;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        if (!worldGenLevel.getBlockState(blockPos = featurePlaceContext.origin()).isAir()) {
            return false;
        }
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos2 = featurePlaceContext.origin();
        RootSystemConfiguration rootSystemConfiguration = featurePlaceContext.config();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable();
        if (RootSystemFeature.placeDirtAndTree(worldGenLevel, featurePlaceContext.chunkGenerator(), rootSystemConfiguration, randomSource, mutableBlockPos, blockPos2)) {
            RootSystemFeature.placeRoots(worldGenLevel, rootSystemConfiguration, randomSource, blockPos2, mutableBlockPos);
        }
        return true;
    }

    private static boolean spaceForTree(WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 1; i <= rootSystemConfiguration.requiredVerticalSpaceForTree; ++i) {
            mutableBlockPos.move(Direction.UP);
            BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
            if (RootSystemFeature.isAllowedTreeSpace(blockState, i, rootSystemConfiguration.allowedVerticalWaterForTree)) continue;
            return false;
        }
        return true;
    }

    private static boolean isAllowedTreeSpace(BlockState blockState, int n, int n2) {
        if (blockState.isAir()) {
            return true;
        }
        int n3 = n + 1;
        return n3 <= n2 && blockState.getFluidState().is(FluidTags.WATER);
    }

    private static boolean placeDirtAndTree(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, BlockPos blockPos) {
        for (int i = 0; i < rootSystemConfiguration.rootColumnMaxHeight; ++i) {
            mutableBlockPos.move(Direction.UP);
            if (!rootSystemConfiguration.allowedTreePosition.test(worldGenLevel, mutableBlockPos) || !RootSystemFeature.spaceForTree(worldGenLevel, rootSystemConfiguration, mutableBlockPos)) continue;
            Vec3i vec3i = mutableBlockPos.below();
            if (worldGenLevel.getFluidState((BlockPos)vec3i).is(FluidTags.LAVA) || !worldGenLevel.getBlockState((BlockPos)vec3i).isSolid()) {
                return false;
            }
            if (!rootSystemConfiguration.treeFeature.value().place(worldGenLevel, chunkGenerator, randomSource, mutableBlockPos)) continue;
            RootSystemFeature.placeDirt(blockPos, blockPos.getY() + i, worldGenLevel, rootSystemConfiguration, randomSource);
            return true;
        }
        return false;
    }

    private static void placeDirt(BlockPos blockPos, int n, WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource) {
        int n2 = blockPos.getX();
        int n3 = blockPos.getZ();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = blockPos.getY(); i < n; ++i) {
            RootSystemFeature.placeRootedDirt(worldGenLevel, rootSystemConfiguration, randomSource, n2, n3, mutableBlockPos.set(n2, i, n3));
        }
    }

    private static void placeRootedDirt(WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource, int n, int n2, BlockPos.MutableBlockPos mutableBlockPos) {
        int n3 = rootSystemConfiguration.rootRadius;
        Predicate<BlockState> predicate = blockState -> blockState.is(rootSystemConfiguration.rootReplaceable);
        for (int i = 0; i < rootSystemConfiguration.rootPlacementAttempts; ++i) {
            mutableBlockPos.setWithOffset(mutableBlockPos, randomSource.nextInt(n3) - randomSource.nextInt(n3), 0, randomSource.nextInt(n3) - randomSource.nextInt(n3));
            if (predicate.test(worldGenLevel.getBlockState(mutableBlockPos))) {
                worldGenLevel.setBlock(mutableBlockPos, rootSystemConfiguration.rootStateProvider.getState(randomSource, mutableBlockPos), 2);
            }
            mutableBlockPos.setX(n);
            mutableBlockPos.setZ(n2);
        }
    }

    private static void placeRoots(WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos) {
        int n = rootSystemConfiguration.hangingRootRadius;
        int n2 = rootSystemConfiguration.hangingRootsVerticalSpan;
        for (int i = 0; i < rootSystemConfiguration.hangingRootPlacementAttempts; ++i) {
            BlockState blockState;
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(n) - randomSource.nextInt(n), randomSource.nextInt(n2) - randomSource.nextInt(n2), randomSource.nextInt(n) - randomSource.nextInt(n));
            if (!worldGenLevel.isEmptyBlock(mutableBlockPos) || !(blockState = rootSystemConfiguration.hangingRootStateProvider.getState(randomSource, mutableBlockPos)).canSurvive(worldGenLevel, mutableBlockPos) || !worldGenLevel.getBlockState((BlockPos)mutableBlockPos.above()).isFaceSturdy(worldGenLevel, mutableBlockPos, Direction.DOWN)) continue;
            worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
        }
    }
}

