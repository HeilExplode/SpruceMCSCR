/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public final class NoiseColumn
implements BlockColumn {
    private final int minY;
    private final BlockState[] column;

    public NoiseColumn(int n, BlockState[] blockStateArray) {
        this.minY = n;
        this.column = blockStateArray;
    }

    @Override
    public BlockState getBlock(int n) {
        int n2 = n - this.minY;
        if (n2 < 0 || n2 >= this.column.length) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.column[n2];
    }

    @Override
    public void setBlock(int n, BlockState blockState) {
        int n2 = n - this.minY;
        if (n2 < 0 || n2 >= this.column.length) {
            throw new IllegalArgumentException("Outside of column height: " + n);
        }
        this.column[n2] = blockState;
    }
}

