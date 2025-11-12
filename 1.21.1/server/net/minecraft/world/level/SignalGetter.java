/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter
extends BlockGetter {
    public static final Direction[] DIRECTIONS = Direction.values();

    default public int getDirectSignal(BlockPos blockPos, Direction direction) {
        return this.getBlockState(blockPos).getDirectSignal(this, blockPos, direction);
    }

    default public int getDirectSignalTo(BlockPos blockPos) {
        int n = 0;
        if ((n = Math.max(n, this.getDirectSignal(blockPos.below(), Direction.DOWN))) >= 15) {
            return n;
        }
        if ((n = Math.max(n, this.getDirectSignal(blockPos.above(), Direction.UP))) >= 15) {
            return n;
        }
        if ((n = Math.max(n, this.getDirectSignal(blockPos.north(), Direction.NORTH))) >= 15) {
            return n;
        }
        if ((n = Math.max(n, this.getDirectSignal(blockPos.south(), Direction.SOUTH))) >= 15) {
            return n;
        }
        if ((n = Math.max(n, this.getDirectSignal(blockPos.west(), Direction.WEST))) >= 15) {
            return n;
        }
        if ((n = Math.max(n, this.getDirectSignal(blockPos.east(), Direction.EAST))) >= 15) {
            return n;
        }
        return n;
    }

    default public int getControlInputSignal(BlockPos blockPos, Direction direction, boolean bl) {
        BlockState blockState = this.getBlockState(blockPos);
        if (bl) {
            return DiodeBlock.isDiode(blockState) ? this.getDirectSignal(blockPos, direction) : 0;
        }
        if (blockState.is(Blocks.REDSTONE_BLOCK)) {
            return 15;
        }
        if (blockState.is(Blocks.REDSTONE_WIRE)) {
            return blockState.getValue(RedStoneWireBlock.POWER);
        }
        if (blockState.isSignalSource()) {
            return this.getDirectSignal(blockPos, direction);
        }
        return 0;
    }

    default public boolean hasSignal(BlockPos blockPos, Direction direction) {
        return this.getSignal(blockPos, direction) > 0;
    }

    default public int getSignal(BlockPos blockPos, Direction direction) {
        BlockState blockState = this.getBlockState(blockPos);
        int n = blockState.getSignal(this, blockPos, direction);
        if (blockState.isRedstoneConductor(this, blockPos)) {
            return Math.max(n, this.getDirectSignalTo(blockPos));
        }
        return n;
    }

    default public boolean hasNeighborSignal(BlockPos blockPos) {
        if (this.getSignal(blockPos.below(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.above(), Direction.UP) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getSignal(blockPos.east(), Direction.EAST) > 0;
    }

    default public int getBestNeighborSignal(BlockPos blockPos) {
        int n = 0;
        for (Direction direction : DIRECTIONS) {
            int n2 = this.getSignal(blockPos.relative(direction), direction);
            if (n2 >= 15) {
                return 15;
            }
            if (n2 <= n) continue;
            n = n2;
        }
        return n;
    }
}

