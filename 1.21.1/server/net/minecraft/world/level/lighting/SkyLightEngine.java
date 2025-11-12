/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.world.level.lighting;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import org.jetbrains.annotations.VisibleForTesting;

public final class SkyLightEngine
extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
    private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
    private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final ChunkSkyLightSources emptyChunkSources;

    public SkyLightEngine(LightChunkGetter lightChunkGetter) {
        this(lightChunkGetter, new SkyLightSectionStorage(lightChunkGetter));
    }

    @VisibleForTesting
    protected SkyLightEngine(LightChunkGetter lightChunkGetter, SkyLightSectionStorage skyLightSectionStorage) {
        super(lightChunkGetter, skyLightSectionStorage);
        this.emptyChunkSources = new ChunkSkyLightSources(lightChunkGetter.getLevel());
    }

    private static boolean isSourceLevel(int n) {
        return n == 15;
    }

    private int getLowestSourceY(int n, int n2, int n3) {
        ChunkSkyLightSources chunkSkyLightSources = this.getChunkSources(SectionPos.blockToSectionCoord(n), SectionPos.blockToSectionCoord(n2));
        if (chunkSkyLightSources == null) {
            return n3;
        }
        return chunkSkyLightSources.getLowestSourceY(SectionPos.sectionRelative(n), SectionPos.sectionRelative(n2));
    }

    @Nullable
    private ChunkSkyLightSources getChunkSources(int n, int n2) {
        LightChunk lightChunk = this.chunkSource.getChunkForLighting(n, n2);
        return lightChunk != null ? lightChunk.getSkyLightSources() : null;
    }

    @Override
    protected void checkNode(long l) {
        boolean bl;
        int n;
        int n2 = BlockPos.getX(l);
        int n3 = BlockPos.getY(l);
        int n4 = BlockPos.getZ(l);
        long l2 = SectionPos.blockToSection(l);
        int n5 = n = ((SkyLightSectionStorage)this.storage).lightOnInSection(l2) ? this.getLowestSourceY(n2, n4, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (n != Integer.MAX_VALUE) {
            this.updateSourcesInColumn(n2, n4, n);
        }
        if (!((SkyLightSectionStorage)this.storage).storingLightForSection(l2)) {
            return;
        }
        boolean bl2 = bl = n3 >= n;
        if (bl) {
            this.enqueueDecrease(l, REMOVE_SKY_SOURCE_ENTRY);
            this.enqueueIncrease(l, ADD_SKY_SOURCE_ENTRY);
        } else {
            int n6 = ((SkyLightSectionStorage)this.storage).getStoredLevel(l);
            if (n6 > 0) {
                ((SkyLightSectionStorage)this.storage).setStoredLevel(l, 0);
                this.enqueueDecrease(l, LightEngine.QueueEntry.decreaseAllDirections(n6));
            } else {
                this.enqueueDecrease(l, PULL_LIGHT_IN_ENTRY);
            }
        }
    }

    private void updateSourcesInColumn(int n, int n2, int n3) {
        int n4 = SectionPos.sectionToBlockCoord(((SkyLightSectionStorage)this.storage).getBottomSectionY());
        this.removeSourcesBelow(n, n2, n3, n4);
        this.addSourcesAbove(n, n2, n3, n4);
    }

    private void removeSourcesBelow(int n, int n2, int n3, int n4) {
        if (n3 <= n4) {
            return;
        }
        int n5 = SectionPos.blockToSectionCoord(n);
        int n6 = SectionPos.blockToSectionCoord(n2);
        int n7 = n3 - 1;
        int n8 = SectionPos.blockToSectionCoord(n7);
        while (((SkyLightSectionStorage)this.storage).hasLightDataAtOrBelow(n8)) {
            if (((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.asLong(n5, n8, n6))) {
                int n9 = SectionPos.sectionToBlockCoord(n8);
                int n10 = n9 + 15;
                for (int i = Math.min(n10, n7); i >= n9; --i) {
                    long l = BlockPos.asLong(n, i, n2);
                    if (!SkyLightEngine.isSourceLevel(((SkyLightSectionStorage)this.storage).getStoredLevel(l))) {
                        return;
                    }
                    ((SkyLightSectionStorage)this.storage).setStoredLevel(l, 0);
                    this.enqueueDecrease(l, i == n3 - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
                }
            }
            --n8;
        }
    }

    private void addSourcesAbove(int n, int n2, int n3, int n4) {
        int n5 = SectionPos.blockToSectionCoord(n);
        int n6 = SectionPos.blockToSectionCoord(n2);
        int n7 = Math.max(Math.max(this.getLowestSourceY(n - 1, n2, Integer.MIN_VALUE), this.getLowestSourceY(n + 1, n2, Integer.MIN_VALUE)), Math.max(this.getLowestSourceY(n, n2 - 1, Integer.MIN_VALUE), this.getLowestSourceY(n, n2 + 1, Integer.MIN_VALUE)));
        int n8 = Math.max(n3, n4);
        long l = SectionPos.asLong(n5, SectionPos.blockToSectionCoord(n8), n6);
        while (!((SkyLightSectionStorage)this.storage).isAboveData(l)) {
            if (((SkyLightSectionStorage)this.storage).storingLightForSection(l)) {
                int n9 = SectionPos.sectionToBlockCoord(SectionPos.y(l));
                int n10 = n9 + 15;
                for (int i = Math.max(n9, n8); i <= n10; ++i) {
                    long l2 = BlockPos.asLong(n, i, n2);
                    if (SkyLightEngine.isSourceLevel(((SkyLightSectionStorage)this.storage).getStoredLevel(l2))) {
                        return;
                    }
                    ((SkyLightSectionStorage)this.storage).setStoredLevel(l2, 15);
                    if (i >= n7 && i != n3) continue;
                    this.enqueueIncrease(l2, ADD_SKY_SOURCE_ENTRY);
                }
            }
            l = SectionPos.offset(l, Direction.UP);
        }
    }

    @Override
    protected void propagateIncrease(long l, long l2, int n) {
        BlockState blockState = null;
        int n2 = this.countEmptySectionsBelowIfAtBorder(l);
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int n3;
            int n4;
            long l3;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(l2, direction) || !((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(l3 = BlockPos.offset(l, direction))) || (n4 = n - 1) <= (n3 = ((SkyLightSectionStorage)this.storage).getStoredLevel(l3))) continue;
            this.mutablePos.set(l3);
            BlockState blockState2 = this.getState(this.mutablePos);
            int n5 = n - this.getOpacity(blockState2, this.mutablePos);
            if (n5 <= n3) continue;
            if (blockState == null) {
                BlockState blockState3 = blockState = LightEngine.QueueEntry.isFromEmptyShape(l2) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(l));
            }
            if (this.shapeOccludes(l, blockState, l3, blockState2, direction)) continue;
            ((SkyLightSectionStorage)this.storage).setStoredLevel(l3, n5);
            if (n5 > 1) {
                this.enqueueIncrease(l3, LightEngine.QueueEntry.increaseSkipOneDirection(n5, SkyLightEngine.isEmptyShape(blockState2), direction.getOpposite()));
            }
            this.propagateFromEmptySections(l3, direction, n5, true, n2);
        }
    }

    @Override
    protected void propagateDecrease(long l, long l2) {
        int n = this.countEmptySectionsBelowIfAtBorder(l);
        int n2 = LightEngine.QueueEntry.getFromLevel(l2);
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int n3;
            long l3;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(l2, direction) || !((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(l3 = BlockPos.offset(l, direction))) || (n3 = ((SkyLightSectionStorage)this.storage).getStoredLevel(l3)) == 0) continue;
            if (n3 <= n2 - 1) {
                ((SkyLightSectionStorage)this.storage).setStoredLevel(l3, 0);
                this.enqueueDecrease(l3, LightEngine.QueueEntry.decreaseSkipOneDirection(n3, direction.getOpposite()));
                this.propagateFromEmptySections(l3, direction, n3, false, n);
                continue;
            }
            this.enqueueIncrease(l3, LightEngine.QueueEntry.increaseOnlyOneDirection(n3, false, direction.getOpposite()));
        }
    }

    private int countEmptySectionsBelowIfAtBorder(long l) {
        int n = BlockPos.getY(l);
        int n2 = SectionPos.sectionRelative(n);
        if (n2 != 0) {
            return 0;
        }
        int n3 = BlockPos.getX(l);
        int n4 = BlockPos.getZ(l);
        int n5 = SectionPos.sectionRelative(n3);
        int n6 = SectionPos.sectionRelative(n4);
        if (n5 == 0 || n5 == 15 || n6 == 0 || n6 == 15) {
            int n7 = SectionPos.blockToSectionCoord(n3);
            int n8 = SectionPos.blockToSectionCoord(n);
            int n9 = SectionPos.blockToSectionCoord(n4);
            int n10 = 0;
            while (!((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.asLong(n7, n8 - n10 - 1, n9)) && ((SkyLightSectionStorage)this.storage).hasLightDataAtOrBelow(n8 - n10 - 1)) {
                ++n10;
            }
            return n10;
        }
        return 0;
    }

    private void propagateFromEmptySections(long l, Direction direction, int n, boolean bl, int n2) {
        if (n2 == 0) {
            return;
        }
        int n3 = BlockPos.getX(l);
        int n4 = BlockPos.getZ(l);
        if (!SkyLightEngine.crossedSectionEdge(direction, SectionPos.sectionRelative(n3), SectionPos.sectionRelative(n4))) {
            return;
        }
        int n5 = BlockPos.getY(l);
        int n6 = SectionPos.blockToSectionCoord(n3);
        int n7 = SectionPos.blockToSectionCoord(n4);
        int n8 = SectionPos.blockToSectionCoord(n5) - 1;
        int n9 = n8 - n2 + 1;
        while (n8 >= n9) {
            if (!((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.asLong(n6, n8, n7))) {
                --n8;
                continue;
            }
            int n10 = SectionPos.sectionToBlockCoord(n8);
            for (int i = 15; i >= 0; --i) {
                long l2 = BlockPos.asLong(n3, n10 + i, n4);
                if (bl) {
                    ((SkyLightSectionStorage)this.storage).setStoredLevel(l2, n);
                    if (n <= 1) continue;
                    this.enqueueIncrease(l2, LightEngine.QueueEntry.increaseSkipOneDirection(n, true, direction.getOpposite()));
                    continue;
                }
                ((SkyLightSectionStorage)this.storage).setStoredLevel(l2, 0);
                this.enqueueDecrease(l2, LightEngine.QueueEntry.decreaseSkipOneDirection(n, direction.getOpposite()));
            }
            --n8;
        }
    }

    private static boolean crossedSectionEdge(Direction direction, int n, int n2) {
        return switch (direction) {
            case Direction.NORTH -> {
                if (n2 == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (n2 == 0) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (n == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (n == 0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
        super.setLightEnabled(chunkPos, bl);
        if (bl) {
            ChunkSkyLightSources chunkSkyLightSources = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z), this.emptyChunkSources);
            int n = chunkSkyLightSources.getHighestLowestSourceY() - 1;
            int n2 = SectionPos.blockToSectionCoord(n) + 1;
            long l = SectionPos.getZeroNode(chunkPos.x, chunkPos.z);
            int n3 = ((SkyLightSectionStorage)this.storage).getTopSectionY(l);
            int n4 = Math.max(((SkyLightSectionStorage)this.storage).getBottomSectionY(), n2);
            for (int i = n3 - 1; i >= n4; --i) {
                DataLayer dataLayer = ((SkyLightSectionStorage)this.storage).getDataLayerToWrite(SectionPos.asLong(chunkPos.x, i, chunkPos.z));
                if (dataLayer == null || !dataLayer.isEmpty()) continue;
                dataLayer.fill(15);
            }
        }
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        long l = SectionPos.getZeroNode(chunkPos.x, chunkPos.z);
        ((SkyLightSectionStorage)this.storage).setLightEnabled(l, true);
        ChunkSkyLightSources chunkSkyLightSources = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z), this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources2 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z - 1), this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources3 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z + 1), this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources4 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x - 1, chunkPos.z), this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources5 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x + 1, chunkPos.z), this.emptyChunkSources);
        int n = ((SkyLightSectionStorage)this.storage).getTopSectionY(l);
        int n2 = ((SkyLightSectionStorage)this.storage).getBottomSectionY();
        int n3 = SectionPos.sectionToBlockCoord(chunkPos.x);
        int n4 = SectionPos.sectionToBlockCoord(chunkPos.z);
        for (int i = n - 1; i >= n2; --i) {
            long l2 = SectionPos.asLong(chunkPos.x, i, chunkPos.z);
            DataLayer dataLayer = ((SkyLightSectionStorage)this.storage).getDataLayerToWrite(l2);
            if (dataLayer == null) continue;
            int n5 = SectionPos.sectionToBlockCoord(i);
            int n6 = n5 + 15;
            boolean bl = false;
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    int n7 = chunkSkyLightSources.getLowestSourceY(k, j);
                    if (n7 > n6) continue;
                    int n8 = j == 0 ? chunkSkyLightSources2.getLowestSourceY(k, 15) : chunkSkyLightSources.getLowestSourceY(k, j - 1);
                    int n9 = j == 15 ? chunkSkyLightSources3.getLowestSourceY(k, 0) : chunkSkyLightSources.getLowestSourceY(k, j + 1);
                    int n10 = k == 0 ? chunkSkyLightSources4.getLowestSourceY(15, j) : chunkSkyLightSources.getLowestSourceY(k - 1, j);
                    int n11 = k == 15 ? chunkSkyLightSources5.getLowestSourceY(0, j) : chunkSkyLightSources.getLowestSourceY(k + 1, j);
                    int n12 = Math.max(Math.max(n8, n9), Math.max(n10, n11));
                    for (int i2 = n6; i2 >= Math.max(n5, n7); --i2) {
                        dataLayer.set(k, SectionPos.sectionRelative(i2), j, 15);
                        if (i2 != n7 && i2 >= n12) continue;
                        long l3 = BlockPos.asLong(n3 + k, i2, n4 + j);
                        this.enqueueIncrease(l3, LightEngine.QueueEntry.increaseSkySourceInDirections(i2 == n7, i2 < n8, i2 < n9, i2 < n10, i2 < n11));
                    }
                    if (n7 >= n5) continue;
                    bl = true;
                }
            }
            if (!bl) break;
        }
    }
}

