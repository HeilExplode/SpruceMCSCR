/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class BulkSectionAccess
implements AutoCloseable {
    private final LevelAccessor level;
    private final Long2ObjectMap<LevelChunkSection> acquiredSections = new Long2ObjectOpenHashMap();
    @Nullable
    private LevelChunkSection lastSection;
    private long lastSectionKey;

    public BulkSectionAccess(LevelAccessor levelAccessor) {
        this.level = levelAccessor;
    }

    @Nullable
    public LevelChunkSection getSection(BlockPos blockPos) {
        int n = this.level.getSectionIndex(blockPos.getY());
        if (n < 0 || n >= this.level.getSectionsCount()) {
            return null;
        }
        long l2 = SectionPos.asLong(blockPos);
        if (this.lastSection == null || this.lastSectionKey != l2) {
            this.lastSection = (LevelChunkSection)this.acquiredSections.computeIfAbsent(l2, l -> {
                ChunkAccess chunkAccess = this.level.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
                LevelChunkSection levelChunkSection = chunkAccess.getSection(n);
                levelChunkSection.acquire();
                return levelChunkSection;
            });
            this.lastSectionKey = l2;
        }
        return this.lastSection;
    }

    public BlockState getBlockState(BlockPos blockPos) {
        LevelChunkSection levelChunkSection = this.getSection(blockPos);
        if (levelChunkSection == null) {
            return Blocks.AIR.defaultBlockState();
        }
        int n = SectionPos.sectionRelative(blockPos.getX());
        int n2 = SectionPos.sectionRelative(blockPos.getY());
        int n3 = SectionPos.sectionRelative(blockPos.getZ());
        return levelChunkSection.getBlockState(n, n2, n3);
    }

    @Override
    public void close() {
        for (LevelChunkSection levelChunkSection : this.acquiredSections.values()) {
            levelChunkSection.release();
        }
    }
}

