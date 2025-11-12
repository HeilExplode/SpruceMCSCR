/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  javax.annotation.concurrent.Immutable
 *  org.apache.commons.lang3.Validate
 *  org.apache.commons.lang3.tuple.Pair
 *  org.slf4j.Logger
 */
package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@Immutable
public class BlockPos
extends Vec3i {
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 3).map(nArray -> new BlockPos(nArray[0], nArray[1], nArray[2])), blockPos -> IntStream.of(blockPos.getX(), blockPos.getY(), blockPos.getZ())).stable();
    public static final StreamCodec<ByteBuf, BlockPos> STREAM_CODEC = new StreamCodec<ByteBuf, BlockPos>(){

        @Override
        public BlockPos decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readBlockPos(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, BlockPos blockPos) {
            FriendlyByteBuf.writeBlockPos(byteBuf, blockPos);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (BlockPos)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private static final int PACKED_X_LENGTH;
    private static final int PACKED_Z_LENGTH;
    public static final int PACKED_Y_LENGTH;
    private static final long PACKED_X_MASK;
    private static final long PACKED_Y_MASK;
    private static final long PACKED_Z_MASK;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET;
    private static final int X_OFFSET;

    public BlockPos(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    public BlockPos(Vec3i vec3i) {
        this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static long offset(long l, Direction direction) {
        return BlockPos.offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public static long offset(long l, int n, int n2, int n3) {
        return BlockPos.asLong(BlockPos.getX(l) + n, BlockPos.getY(l) + n2, BlockPos.getZ(l) + n3);
    }

    public static int getX(long l) {
        return (int)(l << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
    }

    public static int getY(long l) {
        return (int)(l << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long l) {
        return (int)(l << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
    }

    public static BlockPos of(long l) {
        return new BlockPos(BlockPos.getX(l), BlockPos.getY(l), BlockPos.getZ(l));
    }

    public static BlockPos containing(double d, double d2, double d3) {
        return new BlockPos(Mth.floor(d), Mth.floor(d2), Mth.floor(d3));
    }

    public static BlockPos containing(Position position) {
        return BlockPos.containing(position.x(), position.y(), position.z());
    }

    public static BlockPos min(BlockPos blockPos, BlockPos blockPos2) {
        return new BlockPos(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()));
    }

    public static BlockPos max(BlockPos blockPos, BlockPos blockPos2) {
        return new BlockPos(Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
    }

    public long asLong() {
        return BlockPos.asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int n, int n2, int n3) {
        long l = 0L;
        l |= ((long)n & PACKED_X_MASK) << X_OFFSET;
        l |= ((long)n2 & PACKED_Y_MASK) << 0;
        return l |= ((long)n3 & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long l) {
        return l & 0xFFFFFFFFFFFFFFF0L;
    }

    @Override
    public BlockPos offset(int n, int n2, int n3) {
        if (n == 0 && n2 == 0 && n3 == 0) {
            return this;
        }
        return new BlockPos(this.getX() + n, this.getY() + n2, this.getZ() + n3);
    }

    public Vec3 getCenter() {
        return Vec3.atCenterOf(this);
    }

    public Vec3 getBottomCenter() {
        return Vec3.atBottomCenterOf(this);
    }

    @Override
    public BlockPos offset(Vec3i vec3i) {
        return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    @Override
    public BlockPos subtract(Vec3i vec3i) {
        return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
    }

    @Override
    public BlockPos multiply(int n) {
        if (n == 1) {
            return this;
        }
        if (n == 0) {
            return ZERO;
        }
        return new BlockPos(this.getX() * n, this.getY() * n, this.getZ() * n);
    }

    @Override
    public BlockPos above() {
        return this.relative(Direction.UP);
    }

    @Override
    public BlockPos above(int n) {
        return this.relative(Direction.UP, n);
    }

    @Override
    public BlockPos below() {
        return this.relative(Direction.DOWN);
    }

    @Override
    public BlockPos below(int n) {
        return this.relative(Direction.DOWN, n);
    }

    @Override
    public BlockPos north() {
        return this.relative(Direction.NORTH);
    }

    @Override
    public BlockPos north(int n) {
        return this.relative(Direction.NORTH, n);
    }

    @Override
    public BlockPos south() {
        return this.relative(Direction.SOUTH);
    }

    @Override
    public BlockPos south(int n) {
        return this.relative(Direction.SOUTH, n);
    }

    @Override
    public BlockPos west() {
        return this.relative(Direction.WEST);
    }

    @Override
    public BlockPos west(int n) {
        return this.relative(Direction.WEST, n);
    }

    @Override
    public BlockPos east() {
        return this.relative(Direction.EAST);
    }

    @Override
    public BlockPos east(int n) {
        return this.relative(Direction.EAST, n);
    }

    @Override
    public BlockPos relative(Direction direction) {
        return new BlockPos(this.getX() + direction.getStepX(), this.getY() + direction.getStepY(), this.getZ() + direction.getStepZ());
    }

    @Override
    public BlockPos relative(Direction direction, int n) {
        if (n == 0) {
            return this;
        }
        return new BlockPos(this.getX() + direction.getStepX() * n, this.getY() + direction.getStepY() * n, this.getZ() + direction.getStepZ() * n);
    }

    @Override
    public BlockPos relative(Direction.Axis axis, int n) {
        if (n == 0) {
            return this;
        }
        int n2 = axis == Direction.Axis.X ? n : 0;
        int n3 = axis == Direction.Axis.Y ? n : 0;
        int n4 = axis == Direction.Axis.Z ? n : 0;
        return new BlockPos(this.getX() + n2, this.getY() + n3, this.getZ() + n4);
    }

    public BlockPos rotate(Rotation rotation) {
        switch (rotation) {
            default: {
                return this;
            }
            case CLOCKWISE_90: {
                return new BlockPos(-this.getZ(), this.getY(), this.getX());
            }
            case CLOCKWISE_180: {
                return new BlockPos(-this.getX(), this.getY(), -this.getZ());
            }
            case COUNTERCLOCKWISE_90: 
        }
        return new BlockPos(this.getZ(), this.getY(), -this.getX());
    }

    @Override
    public BlockPos cross(Vec3i vec3i) {
        return new BlockPos(this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(), this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(), this.getX() * vec3i.getY() - this.getY() * vec3i.getX());
    }

    public BlockPos atY(int n) {
        return new BlockPos(this.getX(), n, this.getZ());
    }

    public BlockPos immutable() {
        return this;
    }

    public MutableBlockPos mutable() {
        return new MutableBlockPos(this.getX(), this.getY(), this.getZ());
    }

    public Vec3 clampLocationWithin(Vec3 vec3) {
        return new Vec3(Mth.clamp(vec3.x, (double)((float)this.getX() + 1.0E-5f), (double)this.getX() + 1.0 - (double)1.0E-5f), Mth.clamp(vec3.y, (double)((float)this.getY() + 1.0E-5f), (double)this.getY() + 1.0 - (double)1.0E-5f), Mth.clamp(vec3.z, (double)((float)this.getZ() + 1.0E-5f), (double)this.getZ() + 1.0 - (double)1.0E-5f));
    }

    public static Iterable<BlockPos> randomInCube(RandomSource randomSource, int n, BlockPos blockPos, int n2) {
        return BlockPos.randomBetweenClosed(randomSource, n, blockPos.getX() - n2, blockPos.getY() - n2, blockPos.getZ() - n2, blockPos.getX() + n2, blockPos.getY() + n2, blockPos.getZ() + n2);
    }

    @Deprecated
    public static Stream<BlockPos> squareOutSouthEast(BlockPos blockPos) {
        return Stream.of(blockPos, blockPos.south(), blockPos.east(), blockPos.south().east());
    }

    public static Iterable<BlockPos> randomBetweenClosed(final RandomSource randomSource, final int n, final int n2, final int n3, final int n4, int n5, int n6, int n7) {
        final int n8 = n5 - n2 + 1;
        final int n9 = n6 - n3 + 1;
        final int n10 = n7 - n4 + 1;
        return () -> new AbstractIterator<BlockPos>(){
            final MutableBlockPos nextPos = new MutableBlockPos();
            int counter = n;

            protected BlockPos computeNext() {
                if (this.counter <= 0) {
                    return (BlockPos)this.endOfData();
                }
                MutableBlockPos mutableBlockPos = this.nextPos.set(n2 + randomSource.nextInt(n8), n3 + randomSource.nextInt(n9), n4 + randomSource.nextInt(n10));
                --this.counter;
                return mutableBlockPos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<BlockPos> withinManhattan(BlockPos blockPos, final int n, final int n2, final int n3) {
        final int n4 = n + n2 + n3;
        final int n5 = blockPos.getX();
        final int n6 = blockPos.getY();
        final int n7 = blockPos.getZ();
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            protected BlockPos computeNext() {
                if (this.zMirror) {
                    this.zMirror = false;
                    this.cursor.setZ(n7 - (this.cursor.getZ() - n7));
                    return this.cursor;
                }
                MutableBlockPos mutableBlockPos = null;
                while (mutableBlockPos == null) {
                    if (this.y > this.maxY) {
                        ++this.x;
                        if (this.x > this.maxX) {
                            ++this.currentDepth;
                            if (this.currentDepth > n4) {
                                return (BlockPos)this.endOfData();
                            }
                            this.maxX = Math.min(n, this.currentDepth);
                            this.x = -this.maxX;
                        }
                        this.maxY = Math.min(n2, this.currentDepth - Math.abs(this.x));
                        this.y = -this.maxY;
                    }
                    int n8 = this.x;
                    int n22 = this.y;
                    int n32 = this.currentDepth - Math.abs(n8) - Math.abs(n22);
                    if (n32 <= n3) {
                        this.zMirror = n32 != 0;
                        mutableBlockPos = this.cursor.set(n5 + n8, n6 + n22, n7 + n32);
                    }
                    ++this.y;
                }
                return mutableBlockPos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos blockPos, int n, int n2, Predicate<BlockPos> predicate) {
        for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, n, n2, n)) {
            if (!predicate.test(blockPos2)) continue;
            return Optional.of(blockPos2);
        }
        return Optional.empty();
    }

    public static Stream<BlockPos> withinManhattanStream(BlockPos blockPos, int n, int n2, int n3) {
        return StreamSupport.stream(BlockPos.withinManhattan(blockPos, n, n2, n3).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos blockPos, BlockPos blockPos2) {
        return BlockPos.betweenClosed(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()), Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos blockPos, BlockPos blockPos2) {
        return StreamSupport.stream(BlockPos.betweenClosed(blockPos, blockPos2).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox boundingBox) {
        return BlockPos.betweenClosedStream(Math.min(boundingBox.minX(), boundingBox.maxX()), Math.min(boundingBox.minY(), boundingBox.maxY()), Math.min(boundingBox.minZ(), boundingBox.maxZ()), Math.max(boundingBox.minX(), boundingBox.maxX()), Math.max(boundingBox.minY(), boundingBox.maxY()), Math.max(boundingBox.minZ(), boundingBox.maxZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(AABB aABB) {
        return BlockPos.betweenClosedStream(Mth.floor(aABB.minX), Mth.floor(aABB.minY), Mth.floor(aABB.minZ), Mth.floor(aABB.maxX), Mth.floor(aABB.maxY), Mth.floor(aABB.maxZ));
    }

    public static Stream<BlockPos> betweenClosedStream(int n, int n2, int n3, int n4, int n5, int n6) {
        return StreamSupport.stream(BlockPos.betweenClosed(n, n2, n3, n4, n5, n6).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(final int n, final int n2, final int n3, int n4, int n5, int n6) {
        final int n7 = n4 - n + 1;
        final int n8 = n5 - n2 + 1;
        int n9 = n6 - n3 + 1;
        final int n10 = n7 * n8 * n9;
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index;

            protected BlockPos computeNext() {
                if (this.index == n10) {
                    return (BlockPos)this.endOfData();
                }
                int n5 = this.index % n7;
                int n22 = this.index / n7;
                int n32 = n22 % n8;
                int n4 = n22 / n8;
                ++this.index;
                return this.cursor.set(n + n5, n2 + n32, n3 + n4);
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<MutableBlockPos> spiralAround(final BlockPos blockPos, final int n, final Direction direction, final Direction direction2) {
        Validate.validState((direction.getAxis() != direction2.getAxis() ? 1 : 0) != 0, (String)"The two directions cannot be on the same axis", (Object[])new Object[0]);
        return () -> new AbstractIterator<MutableBlockPos>(){
            private final Direction[] directions;
            private final MutableBlockPos cursor;
            private final int legs;
            private int leg;
            private int legSize;
            private int legIndex;
            private int lastX;
            private int lastY;
            private int lastZ;
            {
                this.directions = new Direction[]{direction, direction2, direction.getOpposite(), direction2.getOpposite()};
                this.cursor = blockPos.mutable().move(direction2);
                this.legs = 4 * n;
                this.leg = -1;
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
            }

            protected MutableBlockPos computeNext() {
                this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
                if (this.legIndex >= this.legSize) {
                    if (this.leg >= this.legs) {
                        return (MutableBlockPos)this.endOfData();
                    }
                    ++this.leg;
                    this.legIndex = 0;
                    this.legSize = this.leg / 2 + 1;
                }
                ++this.legIndex;
                return this.cursor;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static int breadthFirstTraversal(BlockPos blockPos2, int n, int n2, BiConsumer<BlockPos, Consumer<BlockPos>> biConsumer, Predicate<BlockPos> predicate) {
        ArrayDeque<Pair> arrayDeque = new ArrayDeque<Pair>();
        LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
        arrayDeque.add(Pair.of((Object)blockPos2, (Object)0));
        int n3 = 0;
        while (!arrayDeque.isEmpty()) {
            Pair pair = (Pair)arrayDeque.poll();
            BlockPos blockPos3 = (BlockPos)pair.getLeft();
            int n4 = (Integer)pair.getRight();
            long l = blockPos3.asLong();
            if (!longOpenHashSet.add(l) || !predicate.test(blockPos3)) continue;
            if (++n3 >= n2) {
                return n3;
            }
            if (n4 >= n) continue;
            biConsumer.accept(blockPos3, blockPos -> arrayDeque.add(Pair.of((Object)blockPos, (Object)(n4 + 1))));
        }
        return n3;
    }

    @Override
    public /* synthetic */ Vec3i cross(Vec3i vec3i) {
        return this.cross(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction.Axis axis, int n) {
        return this.relative(axis, n);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction direction, int n) {
        return this.relative(direction, n);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction direction) {
        return this.relative(direction);
    }

    @Override
    public /* synthetic */ Vec3i east(int n) {
        return this.east(n);
    }

    @Override
    public /* synthetic */ Vec3i east() {
        return this.east();
    }

    @Override
    public /* synthetic */ Vec3i west(int n) {
        return this.west(n);
    }

    @Override
    public /* synthetic */ Vec3i west() {
        return this.west();
    }

    @Override
    public /* synthetic */ Vec3i south(int n) {
        return this.south(n);
    }

    @Override
    public /* synthetic */ Vec3i south() {
        return this.south();
    }

    @Override
    public /* synthetic */ Vec3i north(int n) {
        return this.north(n);
    }

    @Override
    public /* synthetic */ Vec3i north() {
        return this.north();
    }

    @Override
    public /* synthetic */ Vec3i below(int n) {
        return this.below(n);
    }

    @Override
    public /* synthetic */ Vec3i below() {
        return this.below();
    }

    @Override
    public /* synthetic */ Vec3i above(int n) {
        return this.above(n);
    }

    @Override
    public /* synthetic */ Vec3i above() {
        return this.above();
    }

    @Override
    public /* synthetic */ Vec3i multiply(int n) {
        return this.multiply(n);
    }

    @Override
    public /* synthetic */ Vec3i subtract(Vec3i vec3i) {
        return this.subtract(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i offset(Vec3i vec3i) {
        return this.offset(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i offset(int n, int n2, int n3) {
        return this.offset(n, n2, n3);
    }

    static {
        PACKED_Z_LENGTH = PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
        PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
        PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
        PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
        PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
        Z_OFFSET = PACKED_Y_LENGTH;
        X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;
    }

    public static class MutableBlockPos
    extends BlockPos {
        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(int n, int n2, int n3) {
            super(n, n2, n3);
        }

        public MutableBlockPos(double d, double d2, double d3) {
            this(Mth.floor(d), Mth.floor(d2), Mth.floor(d3));
        }

        @Override
        public BlockPos offset(int n, int n2, int n3) {
            return super.offset(n, n2, n3).immutable();
        }

        @Override
        public BlockPos multiply(int n) {
            return super.multiply(n).immutable();
        }

        @Override
        public BlockPos relative(Direction direction, int n) {
            return super.relative(direction, n).immutable();
        }

        @Override
        public BlockPos relative(Direction.Axis axis, int n) {
            return super.relative(axis, n).immutable();
        }

        @Override
        public BlockPos rotate(Rotation rotation) {
            return super.rotate(rotation).immutable();
        }

        public MutableBlockPos set(int n, int n2, int n3) {
            this.setX(n);
            this.setY(n2);
            this.setZ(n3);
            return this;
        }

        public MutableBlockPos set(double d, double d2, double d3) {
            return this.set(Mth.floor(d), Mth.floor(d2), Mth.floor(d3));
        }

        public MutableBlockPos set(Vec3i vec3i) {
            return this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        }

        public MutableBlockPos set(long l) {
            return this.set(MutableBlockPos.getX(l), MutableBlockPos.getY(l), MutableBlockPos.getZ(l));
        }

        public MutableBlockPos set(AxisCycle axisCycle, int n, int n2, int n3) {
            return this.set(axisCycle.cycle(n, n2, n3, Direction.Axis.X), axisCycle.cycle(n, n2, n3, Direction.Axis.Y), axisCycle.cycle(n, n2, n3, Direction.Axis.Z));
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, Direction direction) {
            return this.set(vec3i.getX() + direction.getStepX(), vec3i.getY() + direction.getStepY(), vec3i.getZ() + direction.getStepZ());
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, int n, int n2, int n3) {
            return this.set(vec3i.getX() + n, vec3i.getY() + n2, vec3i.getZ() + n3);
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, Vec3i vec3i2) {
            return this.set(vec3i.getX() + vec3i2.getX(), vec3i.getY() + vec3i2.getY(), vec3i.getZ() + vec3i2.getZ());
        }

        public MutableBlockPos move(Direction direction) {
            return this.move(direction, 1);
        }

        public MutableBlockPos move(Direction direction, int n) {
            return this.set(this.getX() + direction.getStepX() * n, this.getY() + direction.getStepY() * n, this.getZ() + direction.getStepZ() * n);
        }

        public MutableBlockPos move(int n, int n2, int n3) {
            return this.set(this.getX() + n, this.getY() + n2, this.getZ() + n3);
        }

        public MutableBlockPos move(Vec3i vec3i) {
            return this.set(this.getX() + vec3i.getX(), this.getY() + vec3i.getY(), this.getZ() + vec3i.getZ());
        }

        public MutableBlockPos clamp(Direction.Axis axis, int n, int n2) {
            switch (axis) {
                case X: {
                    return this.set(Mth.clamp(this.getX(), n, n2), this.getY(), this.getZ());
                }
                case Y: {
                    return this.set(this.getX(), Mth.clamp(this.getY(), n, n2), this.getZ());
                }
                case Z: {
                    return this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), n, n2));
                }
            }
            throw new IllegalStateException("Unable to clamp axis " + String.valueOf(axis));
        }

        @Override
        public MutableBlockPos setX(int n) {
            super.setX(n);
            return this;
        }

        @Override
        public MutableBlockPos setY(int n) {
            super.setY(n);
            return this;
        }

        @Override
        public MutableBlockPos setZ(int n) {
            super.setZ(n);
            return this;
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }

        @Override
        public /* synthetic */ Vec3i cross(Vec3i vec3i) {
            return super.cross(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction.Axis axis, int n) {
            return this.relative(axis, n);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction direction, int n) {
            return this.relative(direction, n);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction direction) {
            return super.relative(direction);
        }

        @Override
        public /* synthetic */ Vec3i east(int n) {
            return super.east(n);
        }

        @Override
        public /* synthetic */ Vec3i east() {
            return super.east();
        }

        @Override
        public /* synthetic */ Vec3i west(int n) {
            return super.west(n);
        }

        @Override
        public /* synthetic */ Vec3i west() {
            return super.west();
        }

        @Override
        public /* synthetic */ Vec3i south(int n) {
            return super.south(n);
        }

        @Override
        public /* synthetic */ Vec3i south() {
            return super.south();
        }

        @Override
        public /* synthetic */ Vec3i north(int n) {
            return super.north(n);
        }

        @Override
        public /* synthetic */ Vec3i north() {
            return super.north();
        }

        @Override
        public /* synthetic */ Vec3i below(int n) {
            return super.below(n);
        }

        @Override
        public /* synthetic */ Vec3i below() {
            return super.below();
        }

        @Override
        public /* synthetic */ Vec3i above(int n) {
            return super.above(n);
        }

        @Override
        public /* synthetic */ Vec3i above() {
            return super.above();
        }

        @Override
        public /* synthetic */ Vec3i multiply(int n) {
            return this.multiply(n);
        }

        @Override
        public /* synthetic */ Vec3i subtract(Vec3i vec3i) {
            return super.subtract(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i offset(Vec3i vec3i) {
            return super.offset(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i offset(int n, int n2, int n3) {
            return this.offset(n, n2, n3);
        }

        @Override
        public /* synthetic */ Vec3i setZ(int n) {
            return this.setZ(n);
        }

        @Override
        public /* synthetic */ Vec3i setY(int n) {
            return this.setY(n);
        }

        @Override
        public /* synthetic */ Vec3i setX(int n) {
            return this.setX(n);
        }
    }
}

