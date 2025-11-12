/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record BlockBox(BlockPos min, BlockPos max) implements Iterable<BlockPos>
{
    public static final StreamCodec<ByteBuf, BlockBox> STREAM_CODEC = new StreamCodec<ByteBuf, BlockBox>(){

        @Override
        public BlockBox decode(ByteBuf byteBuf) {
            return new BlockBox(FriendlyByteBuf.readBlockPos(byteBuf), FriendlyByteBuf.readBlockPos(byteBuf));
        }

        @Override
        public void encode(ByteBuf byteBuf, BlockBox blockBox) {
            FriendlyByteBuf.writeBlockPos(byteBuf, blockBox.min());
            FriendlyByteBuf.writeBlockPos(byteBuf, blockBox.max());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (BlockBox)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public BlockBox(BlockPos blockPos, BlockPos blockPos2) {
        this.min = BlockPos.min(blockPos, blockPos2);
        this.max = BlockPos.max(blockPos, blockPos2);
    }

    public static BlockBox of(BlockPos blockPos) {
        return new BlockBox(blockPos, blockPos);
    }

    public static BlockBox of(BlockPos blockPos, BlockPos blockPos2) {
        return new BlockBox(blockPos, blockPos2);
    }

    public BlockBox include(BlockPos blockPos) {
        return new BlockBox(BlockPos.min(this.min, blockPos), BlockPos.max(this.max, blockPos));
    }

    public boolean isBlock() {
        return this.min.equals(this.max);
    }

    public boolean contains(BlockPos blockPos) {
        return blockPos.getX() >= this.min.getX() && blockPos.getY() >= this.min.getY() && blockPos.getZ() >= this.min.getZ() && blockPos.getX() <= this.max.getX() && blockPos.getY() <= this.max.getY() && blockPos.getZ() <= this.max.getZ();
    }

    public AABB aabb() {
        return AABB.encapsulatingFullBlocks(this.min, this.max);
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return BlockPos.betweenClosed(this.min, this.max).iterator();
    }

    public int sizeX() {
        return this.max.getX() - this.min.getX() + 1;
    }

    public int sizeY() {
        return this.max.getY() - this.min.getY() + 1;
    }

    public int sizeZ() {
        return this.max.getZ() - this.min.getZ() + 1;
    }

    public BlockBox extend(Direction direction, int n) {
        if (n == 0) {
            return this;
        }
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            return BlockBox.of(this.min, BlockPos.max(this.min, this.max.relative(direction, n)));
        }
        return BlockBox.of(BlockPos.min(this.min.relative(direction, n), this.max), this.max);
    }

    public BlockBox move(Direction direction, int n) {
        if (n == 0) {
            return this;
        }
        return new BlockBox(this.min.relative(direction, n), this.max.relative(direction, n));
    }

    public BlockBox offset(Vec3i vec3i) {
        return new BlockBox(this.min.offset(vec3i), this.max.offset(vec3i));
    }
}

