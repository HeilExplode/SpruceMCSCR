/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.world.level.chunk.status;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Locale;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class ChunkDependencies {
    private final ImmutableList<ChunkStatus> dependencyByRadius;
    private final int[] radiusByDependency;

    public ChunkDependencies(ImmutableList<ChunkStatus> immutableList) {
        this.dependencyByRadius = immutableList;
        int n = immutableList.isEmpty() ? 0 : ((ChunkStatus)immutableList.getFirst()).getIndex() + 1;
        this.radiusByDependency = new int[n];
        for (int i = 0; i < immutableList.size(); ++i) {
            ChunkStatus chunkStatus = (ChunkStatus)immutableList.get(i);
            int n2 = chunkStatus.getIndex();
            for (int j = 0; j <= n2; ++j) {
                this.radiusByDependency[j] = i;
            }
        }
    }

    @VisibleForTesting
    public ImmutableList<ChunkStatus> asList() {
        return this.dependencyByRadius;
    }

    public int size() {
        return this.dependencyByRadius.size();
    }

    public int getRadiusOf(ChunkStatus chunkStatus) {
        int n = chunkStatus.getIndex();
        if (n >= this.radiusByDependency.length) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Requesting a ChunkStatus(%s) outside of dependency range(%s)", chunkStatus, this.dependencyByRadius));
        }
        return this.radiusByDependency[n];
    }

    public int getRadius() {
        return Math.max(0, this.dependencyByRadius.size() - 1);
    }

    public ChunkStatus get(int n) {
        return (ChunkStatus)this.dependencyByRadius.get(n);
    }

    public String toString() {
        return this.dependencyByRadius.toString();
    }
}

