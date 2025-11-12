/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem
extends SignItem {
    public HangingSignItem(Block block, Block block2, Item.Properties properties) {
        super(properties, block, block2, Direction.UP);
    }

    @Override
    protected boolean canPlace(LevelReader levelReader, BlockState blockState, BlockPos blockPos) {
        WallHangingSignBlock wallHangingSignBlock;
        Block block = blockState.getBlock();
        if (block instanceof WallHangingSignBlock && !(wallHangingSignBlock = (WallHangingSignBlock)block).canPlace(blockState, levelReader, blockPos)) {
            return false;
        }
        return super.canPlace(levelReader, blockState, blockPos);
    }
}

