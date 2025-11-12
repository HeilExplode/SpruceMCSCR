/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MonsterRoomFeature
extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public MonsterRoomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        BlockPos blockPos;
        int n;
        int n2;
        int n3;
        Predicate<BlockState> predicate = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos blockPos2 = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        int n4 = 3;
        int n5 = randomSource.nextInt(2) + 2;
        int n6 = -n5 - 1;
        int n7 = n5 + 1;
        int n8 = -1;
        int n9 = 4;
        int n10 = randomSource.nextInt(2) + 2;
        int n11 = -n10 - 1;
        int n12 = n10 + 1;
        int n13 = 0;
        for (n3 = n6; n3 <= n7; ++n3) {
            for (n2 = -1; n2 <= 4; ++n2) {
                for (n = n11; n <= n12; ++n) {
                    blockPos = blockPos2.offset(n3, n2, n);
                    boolean bl = worldGenLevel.getBlockState(blockPos).isSolid();
                    if (n2 == -1 && !bl) {
                        return false;
                    }
                    if (n2 == 4 && !bl) {
                        return false;
                    }
                    if (n3 != n6 && n3 != n7 && n != n11 && n != n12 || n2 != 0 || !worldGenLevel.isEmptyBlock(blockPos) || !worldGenLevel.isEmptyBlock(blockPos.above())) continue;
                    ++n13;
                }
            }
        }
        if (n13 < 1 || n13 > 5) {
            return false;
        }
        for (n3 = n6; n3 <= n7; ++n3) {
            for (n2 = 3; n2 >= -1; --n2) {
                for (n = n11; n <= n12; ++n) {
                    blockPos = blockPos2.offset(n3, n2, n);
                    BlockState blockState = worldGenLevel.getBlockState(blockPos);
                    if (n3 == n6 || n2 == -1 || n == n11 || n3 == n7 || n2 == 4 || n == n12) {
                        if (blockPos.getY() >= worldGenLevel.getMinBuildHeight() && !worldGenLevel.getBlockState(blockPos.below()).isSolid()) {
                            worldGenLevel.setBlock(blockPos, AIR, 2);
                            continue;
                        }
                        if (!blockState.isSolid() || blockState.is(Blocks.CHEST)) continue;
                        if (n2 == -1 && randomSource.nextInt(4) != 0) {
                            this.safeSetBlock(worldGenLevel, blockPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), predicate);
                            continue;
                        }
                        this.safeSetBlock(worldGenLevel, blockPos, Blocks.COBBLESTONE.defaultBlockState(), predicate);
                        continue;
                    }
                    if (blockState.is(Blocks.CHEST) || blockState.is(Blocks.SPAWNER)) continue;
                    this.safeSetBlock(worldGenLevel, blockPos, AIR, predicate);
                }
            }
        }
        block6: for (n3 = 0; n3 < 2; ++n3) {
            for (n2 = 0; n2 < 3; ++n2) {
                int n14;
                int n15;
                n = blockPos2.getX() + randomSource.nextInt(n5 * 2 + 1) - n5;
                BlockPos blockPos3 = new BlockPos(n, n15 = blockPos2.getY(), n14 = blockPos2.getZ() + randomSource.nextInt(n10 * 2 + 1) - n10);
                if (!worldGenLevel.isEmptyBlock(blockPos3)) continue;
                int n16 = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (!worldGenLevel.getBlockState(blockPos3.relative(direction)).isSolid()) continue;
                    ++n16;
                }
                if (n16 != 1) continue;
                this.safeSetBlock(worldGenLevel, blockPos3, StructurePiece.reorient(worldGenLevel, blockPos3, Blocks.CHEST.defaultBlockState()), predicate);
                RandomizableContainer.setBlockEntityLootTable(worldGenLevel, randomSource, blockPos3, BuiltInLootTables.SIMPLE_DUNGEON);
                continue block6;
            }
        }
        this.safeSetBlock(worldGenLevel, blockPos2, Blocks.SPAWNER.defaultBlockState(), predicate);
        BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
        if (blockEntity instanceof SpawnerBlockEntity) {
            SpawnerBlockEntity spawnerBlockEntity = (SpawnerBlockEntity)blockEntity;
            spawnerBlockEntity.setEntityId(this.randomEntityId(randomSource), randomSource);
        } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", new Object[]{blockPos2.getX(), blockPos2.getY(), blockPos2.getZ()});
        }
        return true;
    }

    private EntityType<?> randomEntityId(RandomSource randomSource) {
        return Util.getRandom(MOBS, randomSource);
    }
}

