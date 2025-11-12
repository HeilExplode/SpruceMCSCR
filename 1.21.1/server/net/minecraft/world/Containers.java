/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Containers {
    public static void dropContents(Level level, BlockPos blockPos, Container container) {
        Containers.dropContents(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), container);
    }

    public static void dropContents(Level level, Entity entity, Container container) {
        Containers.dropContents(level, entity.getX(), entity.getY(), entity.getZ(), container);
    }

    private static void dropContents(Level level, double d, double d2, double d3, Container container) {
        for (int i = 0; i < container.getContainerSize(); ++i) {
            Containers.dropItemStack(level, d, d2, d3, container.getItem(i));
        }
    }

    public static void dropContents(Level level, BlockPos blockPos, NonNullList<ItemStack> nonNullList) {
        nonNullList.forEach(itemStack -> Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack));
    }

    public static void dropItemStack(Level level, double d, double d2, double d3, ItemStack itemStack) {
        double d4 = EntityType.ITEM.getWidth();
        double d5 = 1.0 - d4;
        double d6 = d4 / 2.0;
        double d7 = Math.floor(d) + level.random.nextDouble() * d5 + d6;
        double d8 = Math.floor(d2) + level.random.nextDouble() * d5;
        double d9 = Math.floor(d3) + level.random.nextDouble() * d5 + d6;
        while (!itemStack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, d7, d8, d9, itemStack.split(level.random.nextInt(21) + 10));
            float f = 0.05f;
            itemEntity.setDeltaMovement(level.random.triangle(0.0, 0.11485000171139836), level.random.triangle(0.2, 0.11485000171139836), level.random.triangle(0.0, 0.11485000171139836));
            level.addFreshEntity(itemEntity);
        }
    }

    public static void dropContentsOnDestroy(BlockState blockState, BlockState blockState2, Level level, BlockPos blockPos) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof Container) {
            Container container = (Container)((Object)blockEntity);
            Containers.dropContents(level, blockPos, container);
            level.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
        }
    }
}

