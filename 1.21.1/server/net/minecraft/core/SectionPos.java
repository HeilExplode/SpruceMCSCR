/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongConsumer
 */
package net.minecraft.core;

import it.unimi.dsi.fastutil.longs.LongConsumer;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.EntityAccess;

public class SectionPos
extends Vec3i {
    public static final int SECTION_BITS = 4;
    public static final int SECTION_SIZE = 16;
    public static final int SECTION_MASK = 15;
    public static final int SECTION_HALF_SIZE = 8;
    public static final int SECTION_MAX_INDEX = 15;
    private static final int PACKED_X_LENGTH = 22;
    private static final int PACKED_Y_LENGTH = 20;
    private static final int PACKED_Z_LENGTH = 22;
    private static final long PACKED_X_MASK = 0x3FFFFFL;
    private static final long PACKED_Y_MASK = 1048575L;
    private static final long PACKED_Z_MASK = 0x3FFFFFL;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = 20;
    private static final int X_OFFSET = 42;
    private static final int RELATIVE_X_SHIFT = 8;
    private static final int RELATIVE_Y_SHIFT = 0;
    private static final int RELATIVE_Z_SHIFT = 4;

    SectionPos(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    public static SectionPos of(int n, int n2, int n3) {
        return new SectionPos(n, n2, n3);
    }

    public static SectionPos of(BlockPos blockPos) {
        return new SectionPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    public static SectionPos of(ChunkPos chunkPos, int n) {
        return new SectionPos(chunkPos.x, n, chunkPos.z);
    }

    public static SectionPos of(EntityAccess entityAccess) {
        return SectionPos.of(entityAccess.blockPosition());
    }

    public static SectionPos of(Position position) {
        return new SectionPos(SectionPos.blockToSectionCoord(position.x()), SectionPos.blockToSectionCoord(position.y()), SectionPos.blockToSectionCoord(position.z()));
    }

    public static SectionPos of(long l) {
        return new SectionPos(SectionPos.x(l), SectionPos.y(l), SectionPos.z(l));
    }

    public static SectionPos bottomOf(ChunkAccess chunkAccess) {
        return SectionPos.of(chunkAccess.getPos(), chunkAccess.getMinSection());
    }

    public static long offset(long l, Direction direction) {
        return SectionPos.offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public static long offset(long l, int n, int n2, int n3) {
        return SectionPos.asLong(SectionPos.x(l) + n, SectionPos.y(l) + n2, SectionPos.z(l) + n3);
    }

    public static int posToSectionCoord(double d) {
        return SectionPos.blockToSectionCoord(Mth.floor(d));
    }

    public static int blockToSectionCoord(int n) {
        return n >> 4;
    }

    public static int blockToSectionCoord(double d) {
        return Mth.floor(d) >> 4;
    }

    public static int sectionRelative(int n) {
        return n & 0xF;
    }

    public static short sectionRelativePos(BlockPos blockPos) {
        int n = SectionPos.sectionRelative(blockPos.getX());
        int n2 = SectionPos.sectionRelative(blockPos.getY());
        int n3 = SectionPos.sectionRelative(blockPos.getZ());
        return (short)(n << 8 | n3 << 4 | n2 << 0);
    }

    public static int sectionRelativeX(short s) {
        return s >>> 8 & 0xF;
    }

    public static int sectionRelativeY(short s) {
        return s >>> 0 & 0xF;
    }

    public static int sectionRelativeZ(short s) {
        return s >>> 4 & 0xF;
    }

    public int relativeToBlockX(short s) {
        return this.minBlockX() + SectionPos.sectionRelativeX(s);
    }

    public int relativeToBlockY(short s) {
        return this.minBlockY() + SectionPos.sectionRelativeY(s);
    }

    public int relativeToBlockZ(short s) {
        return this.minBlockZ() + SectionPos.sectionRelativeZ(s);
    }

    public BlockPos relativeToBlockPos(short s) {
        return new BlockPos(this.relativeToBlockX(s), this.relativeToBlockY(s), this.relativeToBlockZ(s));
    }

    public static int sectionToBlockCoord(int n) {
        return n << 4;
    }

    public static int sectionToBlockCoord(int n, int n2) {
        return SectionPos.sectionToBlockCoord(n) + n2;
    }

    public static int x(long l) {
        return (int)(l << 0 >> 42);
    }

    public static int y(long l) {
        return (int)(l << 44 >> 44);
    }

    public static int z(long l) {
        return (int)(l << 22 >> 42);
    }

    public int x() {
        return this.getX();
    }

    public int y() {
        return this.getY();
    }

    public int z() {
        return this.getZ();
    }

    public int minBlockX() {
        return SectionPos.sectionToBlockCoord(this.x());
    }

    public int minBlockY() {
        return SectionPos.sectionToBlockCoord(this.y());
    }

    public int minBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z());
    }

    public int maxBlockX() {
        return SectionPos.sectionToBlockCoord(this.x(), 15);
    }

    public int maxBlockY() {
        return SectionPos.sectionToBlockCoord(this.y(), 15);
    }

    public int maxBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z(), 15);
    }

    public static long blockToSection(long l) {
        return SectionPos.asLong(SectionPos.blockToSectionCoord(BlockPos.getX(l)), SectionPos.blockToSectionCoord(BlockPos.getY(l)), SectionPos.blockToSectionCoord(BlockPos.getZ(l)));
    }

    public static long getZeroNode(int n, int n2) {
        return SectionPos.getZeroNode(SectionPos.asLong(n, 0, n2));
    }

    public static long getZeroNode(long l) {
        return l & 0xFFFFFFFFFFF00000L;
    }

    public BlockPos origin() {
        return new BlockPos(SectionPos.sectionToBlockCoord(this.x()), SectionPos.sectionToBlockCoord(this.y()), SectionPos.sectionToBlockCoord(this.z()));
    }

    public BlockPos center() {
        int n = 8;
        return this.origin().offset(8, 8, 8);
    }

    public ChunkPos chunk() {
        return new ChunkPos(this.x(), this.z());
    }

    public static long asLong(BlockPos blockPos) {
        return SectionPos.asLong(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    public static long asLong(int n, int n2, int n3) {
        long l = 0L;
        l |= ((long)n & 0x3FFFFFL) << 42;
        l |= ((long)n2 & 0xFFFFFL) << 0;
        return l |= ((long)n3 & 0x3FFFFFL) << 20;
    }

    public long asLong() {
        return SectionPos.asLong(this.x(), this.y(), this.z());
    }

    @Override
    public SectionPos offset(int n, int n2, int n3) {
        if (n == 0 && n2 == 0 && n3 == 0) {
            return this;
        }
        return new SectionPos(this.x() + n, this.y() + n2, this.z() + n3);
    }

    public Stream<BlockPos> blocksInside() {
        return BlockPos.betweenClosedStream(this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ());
    }

    public static Stream<SectionPos> cube(SectionPos sectionPos, int n) {
        int n2 = sectionPos.x();
        int n3 = sectionPos.y();
        int n4 = sectionPos.z();
        return SectionPos.betweenClosedStream(n2 - n, n3 - n, n4 - n, n2 + n, n3 + n, n4 + n);
    }

    public static Stream<SectionPos> aroundChunk(ChunkPos chunkPos, int n, int n2, int n3) {
        int n4 = chunkPos.x;
        int n5 = chunkPos.z;
        return SectionPos.betweenClosedStream(n4 - n, n2, n5 - n, n4 + n, n3 - 1, n5 + n);
    }

    public static Stream<SectionPos> betweenClosedStream(final int n, final int n2, final int n3, final int n4, final int n5, final int n6) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<SectionPos>((long)((n4 - n + 1) * (n5 - n2 + 1) * (n6 - n3 + 1)), 64){
            final Cursor3D cursor;
            {
                super(l, n8);
                this.cursor = new Cursor3D(n, n2, n3, n4, n5, n6);
            }

            @Override
            public boolean tryAdvance(Consumer<? super SectionPos> consumer) {
                if (this.cursor.advance()) {
                    consumer.accept(new SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
                    return true;
                }
                return false;
            }
        }, false);
    }

    public static void aroundAndAtBlockPos(BlockPos blockPos, LongConsumer longConsumer) {
        SectionPos.aroundAndAtBlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ(), longConsumer);
    }

    public static void aroundAndAtBlockPos(long l, LongConsumer longConsumer) {
        SectionPos.aroundAndAtBlockPos(BlockPos.getX(l), BlockPos.getY(l), BlockPos.getZ(l), longConsumer);
    }

    public static void aroundAndAtBlockPos(int n, int n2, int n3, LongConsumer longConsumer) {
        int n4 = SectionPos.blockToSectionCoord(n - 1);
        int n5 = SectionPos.blockToSectionCoord(n + 1);
        int n6 = SectionPos.blockToSectionCoord(n2 - 1);
        int n7 = SectionPos.blockToSectionCoord(n2 + 1);
        int n8 = SectionPos.blockToSectionCoord(n3 - 1);
        int n9 = SectionPos.blockToSectionCoord(n3 + 1);
        if (n4 == n5 && n6 == n7 && n8 == n9) {
            longConsumer.accept(SectionPos.asLong(n4, n6, n8));
        } else {
            for (int i = n4; i <= n5; ++i) {
                for (int j = n6; j <= n7; ++j) {
                    for (int k = n8; k <= n9; ++k) {
                        longConsumer.accept(SectionPos.asLong(i, j, k));
                    }
                }
            }
        }
    }

    @Override
    public /* synthetic */ Vec3i offset(int n, int n2, int n3) {
        return this.offset(n, n2, n3);
    }
}

