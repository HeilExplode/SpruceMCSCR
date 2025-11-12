/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

public class ShulkerBoxDispenseBehavior
extends OptionalDispenseItemBehavior {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        this.setSuccess(false);
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
            BlockPos blockPos = blockSource.pos().relative(direction);
            Direction direction2 = blockSource.level().isEmptyBlock(blockPos.below()) ? direction : Direction.UP;
            try {
                this.setSuccess(((BlockItem)item).place(new DirectionalPlaceContext((Level)blockSource.level(), blockPos, direction, itemStack, direction2)).consumesAction());
            }
            catch (Exception exception) {
                LOGGER.error("Error trying to place shulker box at {}", (Object)blockPos, (Object)exception);
            }
        }
        return itemStack;
    }
}

