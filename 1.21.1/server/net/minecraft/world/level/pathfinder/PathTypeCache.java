/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.HashCommon
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;

public class PathTypeCache {
    private static final int SIZE = 4096;
    private static final int MASK = 4095;
    private final long[] positions = new long[4096];
    private final PathType[] pathTypes = new PathType[4096];

    public PathType getOrCompute(BlockGetter blockGetter, BlockPos blockPos) {
        long l = blockPos.asLong();
        int n = PathTypeCache.index(l);
        PathType pathType = this.get(n, l);
        if (pathType != null) {
            return pathType;
        }
        return this.compute(blockGetter, blockPos, n, l);
    }

    @Nullable
    private PathType get(int n, long l) {
        if (this.positions[n] == l) {
            return this.pathTypes[n];
        }
        return null;
    }

    private PathType compute(BlockGetter blockGetter, BlockPos blockPos, int n, long l) {
        PathType pathType = WalkNodeEvaluator.getPathTypeFromState(blockGetter, blockPos);
        this.positions[n] = l;
        this.pathTypes[n] = pathType;
        return pathType;
    }

    public void invalidate(BlockPos blockPos) {
        long l = blockPos.asLong();
        int n = PathTypeCache.index(l);
        if (this.positions[n] == l) {
            this.pathTypes[n] = null;
        }
    }

    private static int index(long l) {
        return (int)HashCommon.mix((long)l) & 0xFFF;
    }
}

