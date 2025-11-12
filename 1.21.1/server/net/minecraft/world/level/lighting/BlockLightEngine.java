/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;

public final class BlockLightEngine
extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public BlockLightEngine(LightChunkGetter lightChunkGetter) {
        this(lightChunkGetter, new BlockLightSectionStorage(lightChunkGetter));
    }

    @VisibleForTesting
    public BlockLightEngine(LightChunkGetter lightChunkGetter, BlockLightSectionStorage blockLightSectionStorage) {
        super(lightChunkGetter, blockLightSectionStorage);
    }

    @Override
    protected void checkNode(long l) {
        int n;
        long l2 = SectionPos.blockToSection(l);
        if (!((BlockLightSectionStorage)this.storage).storingLightForSection(l2)) {
            return;
        }
        BlockState blockState = this.getState(this.mutablePos.set(l));
        int n2 = this.getEmission(l, blockState);
        if (n2 < (n = ((BlockLightSectionStorage)this.storage).getStoredLevel(l))) {
            ((BlockLightSectionStorage)this.storage).setStoredLevel(l, 0);
            this.enqueueDecrease(l, LightEngine.QueueEntry.decreaseAllDirections(n));
        } else {
            this.enqueueDecrease(l, PULL_LIGHT_IN_ENTRY);
        }
        if (n2 > 0) {
            this.enqueueIncrease(l, LightEngine.QueueEntry.increaseLightFromEmission(n2, BlockLightEngine.isEmptyShape(blockState)));
        }
    }

    @Override
    protected void propagateIncrease(long l, long l2, int n) {
        BlockState blockState = null;
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int n2;
            int n3;
            long l3;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(l2, direction) || !((BlockLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(l3 = BlockPos.offset(l, direction))) || (n3 = n - 1) <= (n2 = ((BlockLightSectionStorage)this.storage).getStoredLevel(l3))) continue;
            this.mutablePos.set(l3);
            BlockState blockState2 = this.getState(this.mutablePos);
            int n4 = n - this.getOpacity(blockState2, this.mutablePos);
            if (n4 <= n2) continue;
            if (blockState == null) {
                BlockState blockState3 = blockState = LightEngine.QueueEntry.isFromEmptyShape(l2) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(l));
            }
            if (this.shapeOccludes(l, blockState, l3, blockState2, direction)) continue;
            ((BlockLightSectionStorage)this.storage).setStoredLevel(l3, n4);
            if (n4 <= 1) continue;
            this.enqueueIncrease(l3, LightEngine.QueueEntry.increaseSkipOneDirection(n4, BlockLightEngine.isEmptyShape(blockState2), direction.getOpposite()));
        }
    }

    @Override
    protected void propagateDecrease(long l, long l2) {
        int n = LightEngine.QueueEntry.getFromLevel(l2);
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int n2;
            long l3;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(l2, direction) || !((BlockLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(l3 = BlockPos.offset(l, direction))) || (n2 = ((BlockLightSectionStorage)this.storage).getStoredLevel(l3)) == 0) continue;
            if (n2 <= n - 1) {
                BlockState blockState = this.getState(this.mutablePos.set(l3));
                int n3 = this.getEmission(l3, blockState);
                ((BlockLightSectionStorage)this.storage).setStoredLevel(l3, 0);
                if (n3 < n2) {
                    this.enqueueDecrease(l3, LightEngine.QueueEntry.decreaseSkipOneDirection(n2, direction.getOpposite()));
                }
                if (n3 <= 0) continue;
                this.enqueueIncrease(l3, LightEngine.QueueEntry.increaseLightFromEmission(n3, BlockLightEngine.isEmptyShape(blockState)));
                continue;
            }
            this.enqueueIncrease(l3, LightEngine.QueueEntry.increaseOnlyOneDirection(n2, false, direction.getOpposite()));
        }
    }

    private int getEmission(long l, BlockState blockState) {
        int n = blockState.getLightEmission();
        if (n > 0 && ((BlockLightSectionStorage)this.storage).lightOnInSection(SectionPos.blockToSection(l))) {
            return n;
        }
        return 0;
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        this.setLightEnabled(chunkPos, true);
        LightChunk lightChunk = this.chunkSource.getChunkForLighting(chunkPos.x, chunkPos.z);
        if (lightChunk != null) {
            lightChunk.findBlockLightSources((blockPos, blockState) -> {
                int n = blockState.getLightEmission();
                this.enqueueIncrease(blockPos.asLong(), LightEngine.QueueEntry.increaseLightFromEmission(n, BlockLightEngine.isEmptyShape(blockState)));
            });
        }
    }
}

