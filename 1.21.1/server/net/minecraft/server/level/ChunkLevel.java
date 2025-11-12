/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.Contract
 */
package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.jetbrains.annotations.Contract;

public class ChunkLevel {
    private static final int FULL_CHUNK_LEVEL = 33;
    private static final int BLOCK_TICKING_LEVEL = 32;
    private static final int ENTITY_TICKING_LEVEL = 31;
    private static final ChunkStep FULL_CHUNK_STEP = ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FULL);
    public static final int RADIUS_AROUND_FULL_CHUNK = FULL_CHUNK_STEP.accumulatedDependencies().getRadius();
    public static final int MAX_LEVEL = 33 + RADIUS_AROUND_FULL_CHUNK;

    @Nullable
    public static ChunkStatus generationStatus(int n) {
        return ChunkLevel.getStatusAroundFullChunk(n - 33, null);
    }

    @Nullable
    @Contract(value="_,!null->!null;_,_->_")
    public static ChunkStatus getStatusAroundFullChunk(int n, @Nullable ChunkStatus chunkStatus) {
        if (n > RADIUS_AROUND_FULL_CHUNK) {
            return chunkStatus;
        }
        if (n <= 0) {
            return ChunkStatus.FULL;
        }
        return FULL_CHUNK_STEP.accumulatedDependencies().get(n);
    }

    public static ChunkStatus getStatusAroundFullChunk(int n) {
        return ChunkLevel.getStatusAroundFullChunk(n, ChunkStatus.EMPTY);
    }

    public static int byStatus(ChunkStatus chunkStatus) {
        return 33 + FULL_CHUNK_STEP.getAccumulatedRadiusOf(chunkStatus);
    }

    public static FullChunkStatus fullStatus(int n) {
        if (n <= 31) {
            return FullChunkStatus.ENTITY_TICKING;
        }
        if (n <= 32) {
            return FullChunkStatus.BLOCK_TICKING;
        }
        if (n <= 33) {
            return FullChunkStatus.FULL;
        }
        return FullChunkStatus.INACCESSIBLE;
    }

    public static int byStatus(FullChunkStatus fullChunkStatus) {
        return switch (fullChunkStatus) {
            default -> throw new MatchException(null, null);
            case FullChunkStatus.INACCESSIBLE -> MAX_LEVEL;
            case FullChunkStatus.FULL -> 33;
            case FullChunkStatus.BLOCK_TICKING -> 32;
            case FullChunkStatus.ENTITY_TICKING -> 31;
        };
    }

    public static boolean isEntityTicking(int n) {
        return n <= 31;
    }

    public static boolean isBlockTicking(int n) {
        return n <= 32;
    }

    public static boolean isLoaded(int n) {
        return n <= MAX_LEVEL;
    }
}

