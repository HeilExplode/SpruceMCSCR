/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior
implements DispenseItemBehavior {
    private static final int DEFAULT_ACCURACY = 6;

    @Override
    public final ItemStack dispense(BlockSource blockSource, ItemStack itemStack) {
        ItemStack itemStack2 = this.execute(blockSource, itemStack);
        this.playSound(blockSource);
        this.playAnimation(blockSource, blockSource.state().getValue(DispenserBlock.FACING));
        return itemStack2;
    }

    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(blockSource);
        ItemStack itemStack2 = itemStack.split(1);
        DefaultDispenseItemBehavior.spawnItem(blockSource.level(), itemStack2, 6, direction, position);
        return itemStack;
    }

    public static void spawnItem(Level level, ItemStack itemStack, int n, Direction direction, Position position) {
        double d = position.x();
        double d2 = position.y();
        double d3 = position.z();
        d2 = direction.getAxis() == Direction.Axis.Y ? (d2 -= 0.125) : (d2 -= 0.15625);
        ItemEntity itemEntity = new ItemEntity(level, d, d2, d3, itemStack);
        double d4 = level.random.nextDouble() * 0.1 + 0.2;
        itemEntity.setDeltaMovement(level.random.triangle((double)direction.getStepX() * d4, 0.0172275 * (double)n), level.random.triangle(0.2, 0.0172275 * (double)n), level.random.triangle((double)direction.getStepZ() * d4, 0.0172275 * (double)n));
        level.addFreshEntity(itemEntity);
    }

    protected void playSound(BlockSource blockSource) {
        DefaultDispenseItemBehavior.playDefaultSound(blockSource);
    }

    protected void playAnimation(BlockSource blockSource, Direction direction) {
        DefaultDispenseItemBehavior.playDefaultAnimation(blockSource, direction);
    }

    private static void playDefaultSound(BlockSource blockSource) {
        blockSource.level().levelEvent(1000, blockSource.pos(), 0);
    }

    private static void playDefaultAnimation(BlockSource blockSource, Direction direction) {
        blockSource.level().levelEvent(2000, blockSource.pos(), direction.get3DDataValue());
    }

    protected ItemStack consumeWithRemainder(BlockSource blockSource, ItemStack itemStack, ItemStack itemStack2) {
        itemStack.shrink(1);
        if (itemStack.isEmpty()) {
            return itemStack2;
        }
        this.addToInventoryOrDispense(blockSource, itemStack2);
        return itemStack;
    }

    private void addToInventoryOrDispense(BlockSource blockSource, ItemStack itemStack) {
        ItemStack itemStack2 = blockSource.blockEntity().insertItem(itemStack);
        if (itemStack2.isEmpty()) {
            return;
        }
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        DefaultDispenseItemBehavior.spawnItem(blockSource.level(), itemStack2, 6, direction, DispenserBlock.getDispensePosition(blockSource));
        DefaultDispenseItemBehavior.playDefaultSound(blockSource);
        DefaultDispenseItemBehavior.playDefaultAnimation(blockSource, direction);
    }
}

