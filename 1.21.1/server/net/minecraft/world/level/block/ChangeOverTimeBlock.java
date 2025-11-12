/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
    public static final int SCAN_DISTANCE = 4;

    public Optional<BlockState> getNext(BlockState var1);

    public float getChanceModifier();

    default public void changeOverTime(BlockState blockState2, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        float f = 0.05688889f;
        if (randomSource.nextFloat() < 0.05688889f) {
            this.getNextState(blockState2, serverLevel, blockPos, randomSource).ifPresent(blockState -> serverLevel.setBlockAndUpdate(blockPos, (BlockState)blockState));
        }
    }

    public T getAge();

    default public Optional<BlockState> getNextState(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2;
        int n;
        int n2 = ((Enum)this.getAge()).ordinal();
        int n3 = 0;
        int n4 = 0;
        Iterator<BlockPos> iterator = BlockPos.withinManhattan(blockPos, 4, 4, 4).iterator();
        while (iterator.hasNext() && (n = (blockPos2 = iterator.next()).distManhattan(blockPos)) <= 4) {
            Block block;
            if (blockPos2.equals(blockPos) || !((block = serverLevel.getBlockState(blockPos2).getBlock()) instanceof ChangeOverTimeBlock)) continue;
            ChangeOverTimeBlock changeOverTimeBlock = (ChangeOverTimeBlock)((Object)block);
            block = changeOverTimeBlock.getAge();
            if (this.getAge().getClass() != block.getClass()) continue;
            int n5 = ((Enum)((Object)block)).ordinal();
            if (n5 < n2) {
                return Optional.empty();
            }
            if (n5 > n2) {
                ++n4;
                continue;
            }
            ++n3;
        }
        float f = (float)(n4 + 1) / (float)(n4 + n3 + 1);
        float f2 = f * f * this.getChanceModifier();
        if (randomSource.nextFloat() < f2) {
            return this.getNext(blockState);
        }
        return Optional.empty();
    }
}

