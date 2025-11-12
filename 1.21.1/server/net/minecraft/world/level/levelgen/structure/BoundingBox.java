/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class BoundingBox {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 6).map(nArray -> new BoundingBox(nArray[0], nArray[1], nArray[2], nArray[3], nArray[4], nArray[5])), boundingBox -> IntStream.of(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)).stable();
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public BoundingBox(BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public BoundingBox(int n, int n2, int n3, int n4, int n5, int n6) {
        this.minX = n;
        this.minY = n2;
        this.minZ = n3;
        this.maxX = n4;
        this.maxY = n5;
        this.maxZ = n6;
        if (n4 < n || n5 < n2 || n6 < n3) {
            String string = "Invalid bounding box data, inverted bounds for: " + String.valueOf(this);
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw new IllegalStateException(string);
            }
            LOGGER.error(string);
            this.minX = Math.min(n, n4);
            this.minY = Math.min(n2, n5);
            this.minZ = Math.min(n3, n6);
            this.maxX = Math.max(n, n4);
            this.maxY = Math.max(n2, n5);
            this.maxZ = Math.max(n3, n6);
        }
    }

    public static BoundingBox fromCorners(Vec3i vec3i, Vec3i vec3i2) {
        return new BoundingBox(Math.min(vec3i.getX(), vec3i2.getX()), Math.min(vec3i.getY(), vec3i2.getY()), Math.min(vec3i.getZ(), vec3i2.getZ()), Math.max(vec3i.getX(), vec3i2.getX()), Math.max(vec3i.getY(), vec3i2.getY()), Math.max(vec3i.getZ(), vec3i2.getZ()));
    }

    public static BoundingBox infinite() {
        return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static BoundingBox orientBox(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, Direction direction) {
        switch (direction) {
            default: {
                return new BoundingBox(n + n4, n2 + n5, n3 + n6, n + n7 - 1 + n4, n2 + n8 - 1 + n5, n3 + n9 - 1 + n6);
            }
            case NORTH: {
                return new BoundingBox(n + n4, n2 + n5, n3 - n9 + 1 + n6, n + n7 - 1 + n4, n2 + n8 - 1 + n5, n3 + n6);
            }
            case WEST: {
                return new BoundingBox(n - n9 + 1 + n6, n2 + n5, n3 + n4, n + n6, n2 + n8 - 1 + n5, n3 + n7 - 1 + n4);
            }
            case EAST: 
        }
        return new BoundingBox(n + n6, n2 + n5, n3 + n4, n + n9 - 1 + n6, n2 + n8 - 1 + n5, n3 + n7 - 1 + n4);
    }

    public Stream<ChunkPos> intersectingChunks() {
        int n = SectionPos.blockToSectionCoord(this.minX());
        int n2 = SectionPos.blockToSectionCoord(this.minZ());
        int n3 = SectionPos.blockToSectionCoord(this.maxX());
        int n4 = SectionPos.blockToSectionCoord(this.maxZ());
        return ChunkPos.rangeClosed(new ChunkPos(n, n2), new ChunkPos(n3, n4));
    }

    public boolean intersects(BoundingBox boundingBox) {
        return this.maxX >= boundingBox.minX && this.minX <= boundingBox.maxX && this.maxZ >= boundingBox.minZ && this.minZ <= boundingBox.maxZ && this.maxY >= boundingBox.minY && this.minY <= boundingBox.maxY;
    }

    public boolean intersects(int n, int n2, int n3, int n4) {
        return this.maxX >= n && this.minX <= n3 && this.maxZ >= n2 && this.minZ <= n4;
    }

    public static Optional<BoundingBox> encapsulatingPositions(Iterable<BlockPos> iterable) {
        Iterator<BlockPos> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        BoundingBox boundingBox = new BoundingBox(iterator.next());
        iterator.forEachRemaining(boundingBox::encapsulate);
        return Optional.of(boundingBox);
    }

    public static Optional<BoundingBox> encapsulatingBoxes(Iterable<BoundingBox> iterable) {
        Iterator<BoundingBox> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        BoundingBox boundingBox = iterator.next();
        BoundingBox boundingBox2 = new BoundingBox(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        iterator.forEachRemaining(boundingBox2::encapsulate);
        return Optional.of(boundingBox2);
    }

    @Deprecated
    public BoundingBox encapsulate(BoundingBox boundingBox) {
        this.minX = Math.min(this.minX, boundingBox.minX);
        this.minY = Math.min(this.minY, boundingBox.minY);
        this.minZ = Math.min(this.minZ, boundingBox.minZ);
        this.maxX = Math.max(this.maxX, boundingBox.maxX);
        this.maxY = Math.max(this.maxY, boundingBox.maxY);
        this.maxZ = Math.max(this.maxZ, boundingBox.maxZ);
        return this;
    }

    @Deprecated
    public BoundingBox encapsulate(BlockPos blockPos) {
        this.minX = Math.min(this.minX, blockPos.getX());
        this.minY = Math.min(this.minY, blockPos.getY());
        this.minZ = Math.min(this.minZ, blockPos.getZ());
        this.maxX = Math.max(this.maxX, blockPos.getX());
        this.maxY = Math.max(this.maxY, blockPos.getY());
        this.maxZ = Math.max(this.maxZ, blockPos.getZ());
        return this;
    }

    @Deprecated
    public BoundingBox move(int n, int n2, int n3) {
        this.minX += n;
        this.minY += n2;
        this.minZ += n3;
        this.maxX += n;
        this.maxY += n2;
        this.maxZ += n3;
        return this;
    }

    @Deprecated
    public BoundingBox move(Vec3i vec3i) {
        return this.move(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public BoundingBox moved(int n, int n2, int n3) {
        return new BoundingBox(this.minX + n, this.minY + n2, this.minZ + n3, this.maxX + n, this.maxY + n2, this.maxZ + n3);
    }

    public BoundingBox inflatedBy(int n) {
        return this.inflatedBy(n, n, n);
    }

    public BoundingBox inflatedBy(int n, int n2, int n3) {
        return new BoundingBox(this.minX() - n, this.minY() - n2, this.minZ() - n3, this.maxX() + n, this.maxY() + n2, this.maxZ() + n3);
    }

    public boolean isInside(Vec3i vec3i) {
        return this.isInside(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public boolean isInside(int n, int n2, int n3) {
        return n >= this.minX && n <= this.maxX && n3 >= this.minZ && n3 <= this.maxZ && n2 >= this.minY && n2 <= this.maxY;
    }

    public Vec3i getLength() {
        return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
    }

    public int getXSpan() {
        return this.maxX - this.minX + 1;
    }

    public int getYSpan() {
        return this.maxY - this.minY + 1;
    }

    public int getZSpan() {
        return this.maxZ - this.minZ + 1;
    }

    public BlockPos getCenter() {
        return new BlockPos(this.minX + (this.maxX - this.minX + 1) / 2, this.minY + (this.maxY - this.minY + 1) / 2, this.minZ + (this.maxZ - this.minZ + 1) / 2);
    }

    public void forAllCorners(Consumer<BlockPos> consumer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        consumer.accept(mutableBlockPos.set(this.maxX, this.maxY, this.maxZ));
        consumer.accept(mutableBlockPos.set(this.minX, this.maxY, this.maxZ));
        consumer.accept(mutableBlockPos.set(this.maxX, this.minY, this.maxZ));
        consumer.accept(mutableBlockPos.set(this.minX, this.minY, this.maxZ));
        consumer.accept(mutableBlockPos.set(this.maxX, this.maxY, this.minZ));
        consumer.accept(mutableBlockPos.set(this.minX, this.maxY, this.minZ));
        consumer.accept(mutableBlockPos.set(this.maxX, this.minY, this.minZ));
        consumer.accept(mutableBlockPos.set(this.minX, this.minY, this.minZ));
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("minX", this.minX).add("minY", this.minY).add("minZ", this.minZ).add("maxX", this.maxX).add("maxY", this.maxY).add("maxZ", this.maxZ).toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BoundingBox) {
            BoundingBox boundingBox = (BoundingBox)object;
            return this.minX == boundingBox.minX && this.minY == boundingBox.minY && this.minZ == boundingBox.minZ && this.maxX == boundingBox.maxX && this.maxY == boundingBox.maxY && this.maxZ == boundingBox.maxZ;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public int minX() {
        return this.minX;
    }

    public int minY() {
        return this.minY;
    }

    public int minZ() {
        return this.minZ;
    }

    public int maxX() {
        return this.maxX;
    }

    public int maxY() {
        return this.maxY;
    }

    public int maxZ() {
        return this.maxZ;
    }
}

