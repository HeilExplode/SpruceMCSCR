/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class VegetationPatchFeature
extends Feature<VegetationPatchConfiguration> {
    public VegetationPatchFeature(Codec<VegetationPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        VegetationPatchConfiguration vegetationPatchConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        Predicate<BlockState> predicate = blockState -> blockState.is(vegetationPatchConfiguration.replaceable);
        int n = vegetationPatchConfiguration.xzRadius.sample(randomSource) + 1;
        int n2 = vegetationPatchConfiguration.xzRadius.sample(randomSource) + 1;
        Set<BlockPos> set = this.placeGroundPatch(worldGenLevel, vegetationPatchConfiguration, randomSource, blockPos, predicate, n, n2);
        this.distributeVegetation(featurePlaceContext, worldGenLevel, vegetationPatchConfiguration, randomSource, set, n, n2);
        return !set.isEmpty();
    }

    protected Set<BlockPos> placeGroundPatch(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, RandomSource randomSource, BlockPos blockPos, Predicate<BlockState> predicate, int n, int n2) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable();
        Direction direction = vegetationPatchConfiguration.surface.getDirection();
        Direction direction2 = direction.getOpposite();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        for (int i = -n; i <= n; ++i) {
            boolean bl = i == -n || i == n;
            for (int j = -n2; j <= n2; ++j) {
                int n3;
                boolean bl2;
                boolean bl3 = j == -n2 || j == n2;
                boolean bl4 = bl || bl3;
                boolean bl5 = bl && bl3;
                boolean bl6 = bl2 = bl4 && !bl5;
                if (bl5 || bl2 && (vegetationPatchConfiguration.extraEdgeColumnChance == 0.0f || randomSource.nextFloat() > vegetationPatchConfiguration.extraEdgeColumnChance)) continue;
                mutableBlockPos.setWithOffset(blockPos, i, 0, j);
                for (n3 = 0; worldGenLevel.isStateAtPosition(mutableBlockPos, BlockBehaviour.BlockStateBase::isAir) && n3 < vegetationPatchConfiguration.verticalRange; ++n3) {
                    mutableBlockPos.move(direction);
                }
                for (n3 = 0; worldGenLevel.isStateAtPosition(mutableBlockPos, blockState -> !blockState.isAir()) && n3 < vegetationPatchConfiguration.verticalRange; ++n3) {
                    mutableBlockPos.move(direction2);
                }
                mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, vegetationPatchConfiguration.surface.getDirection());
                BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos2);
                if (!worldGenLevel.isEmptyBlock(mutableBlockPos) || !blockState2.isFaceSturdy(worldGenLevel, mutableBlockPos2, vegetationPatchConfiguration.surface.getDirection().getOpposite())) continue;
                int n4 = vegetationPatchConfiguration.depth.sample(randomSource) + (vegetationPatchConfiguration.extraBottomBlockChance > 0.0f && randomSource.nextFloat() < vegetationPatchConfiguration.extraBottomBlockChance ? 1 : 0);
                BlockPos blockPos2 = mutableBlockPos2.immutable();
                boolean bl7 = this.placeGround(worldGenLevel, vegetationPatchConfiguration, predicate, randomSource, mutableBlockPos2, n4);
                if (!bl7) continue;
                hashSet.add(blockPos2);
            }
        }
        return hashSet;
    }

    protected void distributeVegetation(FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext, WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, RandomSource randomSource, Set<BlockPos> set, int n, int n2) {
        for (BlockPos blockPos : set) {
            if (!(vegetationPatchConfiguration.vegetationChance > 0.0f) || !(randomSource.nextFloat() < vegetationPatchConfiguration.vegetationChance)) continue;
            this.placeVegetation(worldGenLevel, vegetationPatchConfiguration, featurePlaceContext.chunkGenerator(), randomSource, blockPos);
        }
    }

    protected boolean placeVegetation(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
        return vegetationPatchConfiguration.vegetationFeature.value().place(worldGenLevel, chunkGenerator, randomSource, blockPos.relative(vegetationPatchConfiguration.surface.getDirection().getOpposite()));
    }

    protected boolean placeGround(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, Predicate<BlockState> predicate, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, int n) {
        for (int i = 0; i < n; ++i) {
            BlockState blockState;
            BlockState blockState2 = vegetationPatchConfiguration.groundState.getState(randomSource, mutableBlockPos);
            if (blockState2.is((blockState = worldGenLevel.getBlockState(mutableBlockPos)).getBlock())) continue;
            if (!predicate.test(blockState)) {
                return i != 0;
            }
            worldGenLevel.setBlock(mutableBlockPos, blockState2, 2);
            mutableBlockPos.move(vegetationPatchConfiguration.surface.getDirection());
        }
        return true;
    }
}

