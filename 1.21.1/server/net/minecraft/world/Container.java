/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Container
extends Clearable {
    public static final float DEFAULT_DISTANCE_BUFFER = 4.0f;

    public int getContainerSize();

    public boolean isEmpty();

    public ItemStack getItem(int var1);

    public ItemStack removeItem(int var1, int var2);

    public ItemStack removeItemNoUpdate(int var1);

    public void setItem(int var1, ItemStack var2);

    default public int getMaxStackSize() {
        return 99;
    }

    default public int getMaxStackSize(ItemStack itemStack) {
        return Math.min(this.getMaxStackSize(), itemStack.getMaxStackSize());
    }

    public void setChanged();

    public boolean stillValid(Player var1);

    default public void startOpen(Player player) {
    }

    default public void stopOpen(Player player) {
    }

    default public boolean canPlaceItem(int n, ItemStack itemStack) {
        return true;
    }

    default public boolean canTakeItem(Container container, int n, ItemStack itemStack) {
        return true;
    }

    default public int countItem(Item item) {
        int n = 0;
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.getItem().equals(item)) continue;
            n += itemStack.getCount();
        }
        return n;
    }

    default public boolean hasAnyOf(Set<Item> set) {
        return this.hasAnyMatching(itemStack -> !itemStack.isEmpty() && set.contains(itemStack.getItem()));
    }

    default public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (!predicate.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    public static boolean stillValidBlockEntity(BlockEntity blockEntity, Player player) {
        return Container.stillValidBlockEntity(blockEntity, player, 4.0f);
    }

    public static boolean stillValidBlockEntity(BlockEntity blockEntity, Player player, float f) {
        Level level = blockEntity.getLevel();
        BlockPos blockPos = blockEntity.getBlockPos();
        if (level == null) {
            return false;
        }
        if (level.getBlockEntity(blockPos) != blockEntity) {
            return false;
        }
        return player.canInteractWithBlock(blockPos, f);
    }
}

