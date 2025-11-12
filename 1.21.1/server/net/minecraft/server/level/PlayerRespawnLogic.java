/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
    @Nullable
    protected static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int n, int n2) {
        int n3;
        boolean bl = serverLevel.dimensionType().hasCeiling();
        LevelChunk levelChunk = serverLevel.getChunk(SectionPos.blockToSectionCoord(n), SectionPos.blockToSectionCoord(n2));
        int n4 = n3 = bl ? serverLevel.getChunkSource().getGenerator().getSpawnHeight(serverLevel) : levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, n & 0xF, n2 & 0xF);
        if (n3 < serverLevel.getMinBuildHeight()) {
            return null;
        }
        int n5 = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, n & 0xF, n2 & 0xF);
        if (n5 <= n3 && n5 > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, n & 0xF, n2 & 0xF)) {
            return null;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n3 + 1; i >= serverLevel.getMinBuildHeight(); --i) {
            mutableBlockPos.set(n, i, n2);
            BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
            if (!blockState.getFluidState().isEmpty()) break;
            if (!Block.isFaceFull(blockState.getCollisionShape(serverLevel, mutableBlockPos), Direction.UP)) continue;
            return ((BlockPos)mutableBlockPos.above()).immutable();
        }
        return null;
    }

    @Nullable
    public static BlockPos getSpawnPosInChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
        if (SharedConstants.debugVoidTerrain(chunkPos)) {
            return null;
        }
        for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); ++i) {
            for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); ++j) {
                BlockPos blockPos = PlayerRespawnLogic.getOverworldRespawnPos(serverLevel, i, j);
                if (blockPos == null) continue;
                return blockPos;
            }
        }
        return null;
    }
}

