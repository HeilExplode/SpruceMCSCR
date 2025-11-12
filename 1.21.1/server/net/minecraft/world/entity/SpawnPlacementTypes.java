/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public interface SpawnPlacementTypes {
    public static final SpawnPlacementType NO_RESTRICTIONS = (levelReader, blockPos, entityType) -> true;
    public static final SpawnPlacementType IN_WATER = (levelReader, blockPos, entityType) -> {
        if (entityType == null || !levelReader.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        BlockPos blockPos2 = blockPos.above();
        return levelReader.getFluidState(blockPos).is(FluidTags.WATER) && !levelReader.getBlockState(blockPos2).isRedstoneConductor(levelReader, blockPos2);
    };
    public static final SpawnPlacementType IN_LAVA = (levelReader, blockPos, entityType) -> {
        if (entityType == null || !levelReader.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        return levelReader.getFluidState(blockPos).is(FluidTags.LAVA);
    };
    public static final SpawnPlacementType ON_GROUND = new SpawnPlacementType(){

        @Override
        public boolean isSpawnPositionOk(LevelReader levelReader, BlockPos blockPos, @Nullable EntityType<?> entityType) {
            if (entityType == null || !levelReader.getWorldBorder().isWithinBounds(blockPos)) {
                return false;
            }
            BlockPos blockPos2 = blockPos.above();
            BlockPos blockPos3 = blockPos.below();
            BlockState blockState = levelReader.getBlockState(blockPos3);
            if (!blockState.isValidSpawn(levelReader, blockPos3, entityType)) {
                return false;
            }
            return this.isValidEmptySpawnBlock(levelReader, blockPos, entityType) && this.isValidEmptySpawnBlock(levelReader, blockPos2, entityType);
        }

        private boolean isValidEmptySpawnBlock(LevelReader levelReader, BlockPos blockPos, EntityType<?> entityType) {
            BlockState blockState = levelReader.getBlockState(blockPos);
            return NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos, blockState, blockState.getFluidState(), entityType);
        }

        @Override
        public BlockPos adjustSpawnPosition(LevelReader levelReader, BlockPos blockPos) {
            BlockPos blockPos2 = blockPos.below();
            if (levelReader.getBlockState(blockPos2).isPathfindable(PathComputationType.LAND)) {
                return blockPos2;
            }
            return blockPos;
        }
    };
}

