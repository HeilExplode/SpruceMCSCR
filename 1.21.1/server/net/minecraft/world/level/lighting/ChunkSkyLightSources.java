/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChunkSkyLightSources {
    private static final int SIZE = 16;
    public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
    private final int minY;
    private final BitStorage heightmap;
    private final BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();

    public ChunkSkyLightSources(LevelHeightAccessor levelHeightAccessor) {
        this.minY = levelHeightAccessor.getMinBuildHeight() - 1;
        int n = levelHeightAccessor.getMaxBuildHeight();
        int n2 = Mth.ceillog2(n - this.minY + 1);
        this.heightmap = new SimpleBitStorage(n2, 256);
    }

    public void fillFrom(ChunkAccess chunkAccess) {
        int n = chunkAccess.getHighestFilledSectionIndex();
        if (n == -1) {
            this.fill(this.minY);
            return;
        }
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int n2 = Math.max(this.findLowestSourceY(chunkAccess, n, j, i), this.minY);
                this.set(ChunkSkyLightSources.index(j, i), n2);
            }
        }
    }

    private int findLowestSourceY(ChunkAccess chunkAccess, int n, int n2, int n3) {
        int n4 = SectionPos.sectionToBlockCoord(chunkAccess.getSectionYFromSectionIndex(n) + 1);
        BlockPos.MutableBlockPos mutableBlockPos = this.mutablePos1.set(n2, n4, n3);
        BlockPos.MutableBlockPos mutableBlockPos2 = this.mutablePos2.setWithOffset((Vec3i)mutableBlockPos, Direction.DOWN);
        BlockState blockState = Blocks.AIR.defaultBlockState();
        for (int i = n; i >= 0; --i) {
            int n5;
            LevelChunkSection levelChunkSection = chunkAccess.getSection(i);
            if (levelChunkSection.hasOnlyAir()) {
                blockState = Blocks.AIR.defaultBlockState();
                n5 = chunkAccess.getSectionYFromSectionIndex(i);
                mutableBlockPos.setY(SectionPos.sectionToBlockCoord(n5));
                mutableBlockPos2.setY(mutableBlockPos.getY() - 1);
                continue;
            }
            for (n5 = 15; n5 >= 0; --n5) {
                BlockState blockState2 = levelChunkSection.getBlockState(n2, n5, n3);
                if (ChunkSkyLightSources.isEdgeOccluded(chunkAccess, mutableBlockPos, blockState, mutableBlockPos2, blockState2)) {
                    return mutableBlockPos.getY();
                }
                blockState = blockState2;
                mutableBlockPos.set(mutableBlockPos2);
                mutableBlockPos2.move(Direction.DOWN);
            }
        }
        return this.minY;
    }

    public boolean update(BlockGetter blockGetter, int n, int n2, int n3) {
        BlockState blockState;
        BlockPos.MutableBlockPos mutableBlockPos;
        BlockState blockState2;
        int n4 = n2 + 1;
        int n5 = ChunkSkyLightSources.index(n, n3);
        int n6 = this.get(n5);
        if (n4 < n6) {
            return false;
        }
        BlockPos.MutableBlockPos mutableBlockPos2 = this.mutablePos1.set(n, n2 + 1, n3);
        if (this.updateEdge(blockGetter, n5, n6, mutableBlockPos2, blockState2 = blockGetter.getBlockState(mutableBlockPos2), mutableBlockPos = this.mutablePos2.set(n, n2, n3), blockState = blockGetter.getBlockState(mutableBlockPos))) {
            return true;
        }
        BlockPos.MutableBlockPos mutableBlockPos3 = this.mutablePos1.set(n, n2 - 1, n3);
        BlockState blockState3 = blockGetter.getBlockState(mutableBlockPos3);
        return this.updateEdge(blockGetter, n5, n6, mutableBlockPos, blockState, mutableBlockPos3, blockState3);
    }

    private boolean updateEdge(BlockGetter blockGetter, int n, int n2, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        int n3 = blockPos.getY();
        if (ChunkSkyLightSources.isEdgeOccluded(blockGetter, blockPos, blockState, blockPos2, blockState2)) {
            if (n3 > n2) {
                this.set(n, n3);
                return true;
            }
        } else if (n3 == n2) {
            this.set(n, this.findLowestSourceBelow(blockGetter, blockPos2, blockState2));
            return true;
        }
        return false;
    }

    private int findLowestSourceBelow(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        BlockPos.MutableBlockPos mutableBlockPos = this.mutablePos1.set(blockPos);
        BlockPos.MutableBlockPos mutableBlockPos2 = this.mutablePos2.setWithOffset((Vec3i)blockPos, Direction.DOWN);
        BlockState blockState2 = blockState;
        while (mutableBlockPos2.getY() >= this.minY) {
            BlockState blockState3 = blockGetter.getBlockState(mutableBlockPos2);
            if (ChunkSkyLightSources.isEdgeOccluded(blockGetter, mutableBlockPos, blockState2, mutableBlockPos2, blockState3)) {
                return mutableBlockPos.getY();
            }
            blockState2 = blockState3;
            mutableBlockPos.set(mutableBlockPos2);
            mutableBlockPos2.move(Direction.DOWN);
        }
        return this.minY;
    }

    private static boolean isEdgeOccluded(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        if (blockState2.getLightBlock(blockGetter, blockPos2) != 0) {
            return true;
        }
        VoxelShape voxelShape = LightEngine.getOcclusionShape(blockGetter, blockPos, blockState, Direction.DOWN);
        VoxelShape voxelShape2 = LightEngine.getOcclusionShape(blockGetter, blockPos2, blockState2, Direction.UP);
        return Shapes.faceShapeOccludes(voxelShape, voxelShape2);
    }

    public int getLowestSourceY(int n, int n2) {
        int n3 = this.get(ChunkSkyLightSources.index(n, n2));
        return this.extendSourcesBelowWorld(n3);
    }

    public int getHighestLowestSourceY() {
        int n = Integer.MIN_VALUE;
        for (int i = 0; i < this.heightmap.getSize(); ++i) {
            int n2 = this.heightmap.get(i);
            if (n2 <= n) continue;
            n = n2;
        }
        return this.extendSourcesBelowWorld(n + this.minY);
    }

    private void fill(int n) {
        int n2 = n - this.minY;
        for (int i = 0; i < this.heightmap.getSize(); ++i) {
            this.heightmap.set(i, n2);
        }
    }

    private void set(int n, int n2) {
        this.heightmap.set(n, n2 - this.minY);
    }

    private int get(int n) {
        return this.heightmap.get(n) + this.minY;
    }

    private int extendSourcesBelowWorld(int n) {
        if (n == this.minY) {
            return Integer.MIN_VALUE;
        }
        return n;
    }

    private static int index(int n, int n2) {
        return n + n2 * 16;
    }
}

