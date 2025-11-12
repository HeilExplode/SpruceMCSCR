/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor
extends EntityGetter,
LevelReader,
LevelSimulatedRW {
    @Override
    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
        return LevelReader.super.getBlockEntity(blockPos, blockEntityType);
    }

    @Override
    default public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB) {
        return EntityGetter.super.getEntityCollisions(entity, aABB);
    }

    @Override
    default public boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        return EntityGetter.super.isUnobstructed(entity, voxelShape);
    }

    @Override
    default public BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
        return LevelReader.super.getHeightmapPos(types, blockPos);
    }
}

