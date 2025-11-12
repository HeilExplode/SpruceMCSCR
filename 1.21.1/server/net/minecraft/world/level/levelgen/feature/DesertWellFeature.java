/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertWellFeature
extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sand = Blocks.SAND.defaultBlockState();
    private final BlockState sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
    private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();

    public DesertWellFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        int n;
        int n2;
        int n3;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        blockPos = blockPos.above();
        while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > worldGenLevel.getMinBuildHeight() + 2) {
            blockPos = blockPos.below();
        }
        if (!IS_SAND.test(worldGenLevel.getBlockState(blockPos))) {
            return false;
        }
        for (n3 = -2; n3 <= 2; ++n3) {
            for (n2 = -2; n2 <= 2; ++n2) {
                if (!worldGenLevel.isEmptyBlock(blockPos.offset(n3, -1, n2)) || !worldGenLevel.isEmptyBlock(blockPos.offset(n3, -2, n2))) continue;
                return false;
            }
        }
        for (n3 = -2; n3 <= 0; ++n3) {
            for (n2 = -2; n2 <= 2; ++n2) {
                for (int i = -2; i <= 2; ++i) {
                    worldGenLevel.setBlock(blockPos.offset(n2, n3, i), this.sandstone, 2);
                }
            }
        }
        worldGenLevel.setBlock(blockPos, this.water, 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            worldGenLevel.setBlock(blockPos.relative(direction), this.water, 2);
        }
        BlockPos blockPos2 = blockPos.below();
        worldGenLevel.setBlock(blockPos2, this.sand, 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            worldGenLevel.setBlock(blockPos2.relative(direction), this.sand, 2);
        }
        for (n = -2; n <= 2; ++n) {
            for (int i = -2; i <= 2; ++i) {
                if (n != -2 && n != 2 && i != -2 && i != 2) continue;
                worldGenLevel.setBlock(blockPos.offset(n, 1, i), this.sandstone, 2);
            }
        }
        worldGenLevel.setBlock(blockPos.offset(2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(-2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(0, 1, 2), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(0, 1, -2), this.sandSlab, 2);
        for (n = -1; n <= 1; ++n) {
            for (int i = -1; i <= 1; ++i) {
                if (n == 0 && i == 0) {
                    worldGenLevel.setBlock(blockPos.offset(n, 4, i), this.sandstone, 2);
                    continue;
                }
                worldGenLevel.setBlock(blockPos.offset(n, 4, i), this.sandSlab, 2);
            }
        }
        for (n = 1; n <= 3; ++n) {
            worldGenLevel.setBlock(blockPos.offset(-1, n, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(-1, n, 1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(1, n, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(1, n, 1), this.sandstone, 2);
        }
        BlockPos blockPos3 = blockPos;
        List<BlockPos> list = List.of(blockPos3, blockPos3.east(), blockPos3.south(), blockPos3.west(), blockPos3.north());
        RandomSource randomSource = featurePlaceContext.random();
        DesertWellFeature.placeSusSand(worldGenLevel, Util.getRandom(list, randomSource).below(1));
        DesertWellFeature.placeSusSand(worldGenLevel, Util.getRandom(list, randomSource).below(2));
        return true;
    }

    private static void placeSusSand(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        worldGenLevel.setBlock(blockPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
        worldGenLevel.getBlockEntity(blockPos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent(brushableBlockEntity -> brushableBlockEntity.setLootTable(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY, blockPos.asLong()));
    }
}

