/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMask {
    private final int minY;
    private final BitSet mask;
    private Mask additionalMask = (n, n2, n3) -> false;

    public CarvingMask(int n4, int n5) {
        this.minY = n5;
        this.mask = new BitSet(256 * n4);
    }

    public void setAdditionalMask(Mask mask) {
        this.additionalMask = mask;
    }

    public CarvingMask(long[] lArray, int n4) {
        this.minY = n4;
        this.mask = BitSet.valueOf(lArray);
    }

    private int getIndex(int n, int n2, int n3) {
        return n & 0xF | (n3 & 0xF) << 4 | n2 - this.minY << 8;
    }

    public void set(int n, int n2, int n3) {
        this.mask.set(this.getIndex(n, n2, n3));
    }

    public boolean get(int n, int n2, int n3) {
        return this.additionalMask.test(n, n2, n3) || this.mask.get(this.getIndex(n, n2, n3));
    }

    public Stream<BlockPos> stream(ChunkPos chunkPos) {
        return this.mask.stream().mapToObj(n -> {
            int n2 = n & 0xF;
            int n3 = n >> 4 & 0xF;
            int n4 = n >> 8;
            return chunkPos.getBlockAt(n2, n4 + this.minY, n3);
        });
    }

    public long[] toArray() {
        return this.mask.toLongArray();
    }

    public static interface Mask {
        public boolean test(int var1, int var2, int var3);
    }
}

