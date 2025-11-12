/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;

public interface SpawnPlacementType {
    public boolean isSpawnPositionOk(LevelReader var1, BlockPos var2, @Nullable EntityType<?> var3);

    default public BlockPos adjustSpawnPosition(LevelReader levelReader, BlockPos blockPos) {
        return blockPos;
    }
}

