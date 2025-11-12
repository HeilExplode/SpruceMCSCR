/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathNavigationRegion
implements BlockGetter,
CollisionGetter {
    protected final int centerX;
    protected final int centerZ;
    protected final ChunkAccess[][] chunks;
    protected boolean allEmpty;
    protected final Level level;
    private final Supplier<Holder<Biome>> plains;

    public PathNavigationRegion(Level level, BlockPos blockPos, BlockPos blockPos2) {
        int n;
        int n2;
        this.level = level;
        this.plains = Suppliers.memoize(() -> level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));
        this.centerX = SectionPos.blockToSectionCoord(blockPos.getX());
        this.centerZ = SectionPos.blockToSectionCoord(blockPos.getZ());
        int n3 = SectionPos.blockToSectionCoord(blockPos2.getX());
        int n4 = SectionPos.blockToSectionCoord(blockPos2.getZ());
        this.chunks = new ChunkAccess[n3 - this.centerX + 1][n4 - this.centerZ + 1];
        ChunkSource chunkSource = level.getChunkSource();
        this.allEmpty = true;
        for (n2 = this.centerX; n2 <= n3; ++n2) {
            for (n = this.centerZ; n <= n4; ++n) {
                this.chunks[n2 - this.centerX][n - this.centerZ] = chunkSource.getChunkNow(n2, n);
            }
        }
        for (n2 = SectionPos.blockToSectionCoord(blockPos.getX()); n2 <= SectionPos.blockToSectionCoord(blockPos2.getX()); ++n2) {
            for (n = SectionPos.blockToSectionCoord(blockPos.getZ()); n <= SectionPos.blockToSectionCoord(blockPos2.getZ()); ++n) {
                ChunkAccess chunkAccess = this.chunks[n2 - this.centerX][n - this.centerZ];
                if (chunkAccess == null || chunkAccess.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) continue;
                this.allEmpty = false;
                return;
            }
        }
    }

    private ChunkAccess getChunk(BlockPos blockPos) {
        return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    private ChunkAccess getChunk(int n, int n2) {
        int n3 = n - this.centerX;
        int n4 = n2 - this.centerZ;
        if (n3 < 0 || n3 >= this.chunks.length || n4 < 0 || n4 >= this.chunks[n3].length) {
            return new EmptyLevelChunk(this.level, new ChunkPos(n, n2), this.plains.get());
        }
        ChunkAccess chunkAccess = this.chunks[n3][n4];
        return chunkAccess != null ? chunkAccess : new EmptyLevelChunk(this.level, new ChunkPos(n, n2), this.plains.get());
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public BlockGetter getChunkForCollisions(int n, int n2) {
        return this.getChunk(n, n2);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB) {
        return List.of();
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        return chunkAccess.getBlockEntity(blockPos);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        return chunkAccess.getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        return chunkAccess.getFluidState(blockPos);
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    public ProfilerFiller getProfiler() {
        return this.level.getProfiler();
    }
}

