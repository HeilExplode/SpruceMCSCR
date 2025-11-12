/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class ChunkPos {
    private static final int SAFETY_MARGIN = 1056;
    public static final long INVALID_CHUNK_POS = ChunkPos.asLong(1875066, 1875066);
    public static final ChunkPos ZERO = new ChunkPos(0, 0);
    private static final long COORD_BITS = 32L;
    private static final long COORD_MASK = 0xFFFFFFFFL;
    private static final int REGION_BITS = 5;
    public static final int REGION_SIZE = 32;
    private static final int REGION_MASK = 31;
    public static final int REGION_MAX_INDEX = 31;
    public final int x;
    public final int z;
    private static final int HASH_A = 1664525;
    private static final int HASH_C = 1013904223;
    private static final int HASH_Z_XOR = -559038737;

    public ChunkPos(int n, int n2) {
        this.x = n;
        this.z = n2;
    }

    public ChunkPos(BlockPos blockPos) {
        this.x = SectionPos.blockToSectionCoord(blockPos.getX());
        this.z = SectionPos.blockToSectionCoord(blockPos.getZ());
    }

    public ChunkPos(long l) {
        this.x = (int)l;
        this.z = (int)(l >> 32);
    }

    public static ChunkPos minFromRegion(int n, int n2) {
        return new ChunkPos(n << 5, n2 << 5);
    }

    public static ChunkPos maxFromRegion(int n, int n2) {
        return new ChunkPos((n << 5) + 31, (n2 << 5) + 31);
    }

    public long toLong() {
        return ChunkPos.asLong(this.x, this.z);
    }

    public static long asLong(int n, int n2) {
        return (long)n & 0xFFFFFFFFL | ((long)n2 & 0xFFFFFFFFL) << 32;
    }

    public static long asLong(BlockPos blockPos) {
        return ChunkPos.asLong(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    public static int getX(long l) {
        return (int)(l & 0xFFFFFFFFL);
    }

    public static int getZ(long l) {
        return (int)(l >>> 32 & 0xFFFFFFFFL);
    }

    public int hashCode() {
        return ChunkPos.hash(this.x, this.z);
    }

    public static int hash(int n, int n2) {
        int n3 = 1664525 * n + 1013904223;
        int n4 = 1664525 * (n2 ^ 0xDEADBEEF) + 1013904223;
        return n3 ^ n4;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ChunkPos) {
            ChunkPos chunkPos = (ChunkPos)object;
            return this.x == chunkPos.x && this.z == chunkPos.z;
        }
        return false;
    }

    public int getMiddleBlockX() {
        return this.getBlockX(8);
    }

    public int getMiddleBlockZ() {
        return this.getBlockZ(8);
    }

    public int getMinBlockX() {
        return SectionPos.sectionToBlockCoord(this.x);
    }

    public int getMinBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z);
    }

    public int getMaxBlockX() {
        return this.getBlockX(15);
    }

    public int getMaxBlockZ() {
        return this.getBlockZ(15);
    }

    public int getRegionX() {
        return this.x >> 5;
    }

    public int getRegionZ() {
        return this.z >> 5;
    }

    public int getRegionLocalX() {
        return this.x & 0x1F;
    }

    public int getRegionLocalZ() {
        return this.z & 0x1F;
    }

    public BlockPos getBlockAt(int n, int n2, int n3) {
        return new BlockPos(this.getBlockX(n), n2, this.getBlockZ(n3));
    }

    public int getBlockX(int n) {
        return SectionPos.sectionToBlockCoord(this.x, n);
    }

    public int getBlockZ(int n) {
        return SectionPos.sectionToBlockCoord(this.z, n);
    }

    public BlockPos getMiddleBlockPosition(int n) {
        return new BlockPos(this.getMiddleBlockX(), n, this.getMiddleBlockZ());
    }

    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    public BlockPos getWorldPosition() {
        return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
    }

    public int getChessboardDistance(ChunkPos chunkPos) {
        return this.getChessboardDistance(chunkPos.x, chunkPos.z);
    }

    public int getChessboardDistance(int n, int n2) {
        return Math.max(Math.abs(this.x - n), Math.abs(this.z - n2));
    }

    public int distanceSquared(ChunkPos chunkPos) {
        return this.distanceSquared(chunkPos.x, chunkPos.z);
    }

    public int distanceSquared(long l) {
        return this.distanceSquared(ChunkPos.getX(l), ChunkPos.getZ(l));
    }

    private int distanceSquared(int n, int n2) {
        int n3 = n - this.x;
        int n4 = n2 - this.z;
        return n3 * n3 + n4 * n4;
    }

    public static Stream<ChunkPos> rangeClosed(ChunkPos chunkPos, int n) {
        return ChunkPos.rangeClosed(new ChunkPos(chunkPos.x - n, chunkPos.z - n), new ChunkPos(chunkPos.x + n, chunkPos.z + n));
    }

    public static Stream<ChunkPos> rangeClosed(final ChunkPos chunkPos, final ChunkPos chunkPos2) {
        int n = Math.abs(chunkPos.x - chunkPos2.x) + 1;
        int n2 = Math.abs(chunkPos.z - chunkPos2.z) + 1;
        final int n3 = chunkPos.x < chunkPos2.x ? 1 : -1;
        final int n4 = chunkPos.z < chunkPos2.z ? 1 : -1;
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<ChunkPos>((long)(n * n2), 64){
            @Nullable
            private ChunkPos pos;

            @Override
            public boolean tryAdvance(Consumer<? super ChunkPos> consumer) {
                if (this.pos == null) {
                    this.pos = chunkPos;
                } else {
                    int n = this.pos.x;
                    int n2 = this.pos.z;
                    if (n == chunkPos2.x) {
                        if (n2 == chunkPos2.z) {
                            return false;
                        }
                        this.pos = new ChunkPos(chunkPos.x, n2 + n4);
                    } else {
                        this.pos = new ChunkPos(n + n3, n2);
                    }
                }
                consumer.accept(this.pos);
                return true;
            }
        }, false);
    }
}

