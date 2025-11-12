/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DispenserBlockEntity
extends RandomizableContainerBlockEntity {
    public static final int CONTAINER_SIZE = 9;
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    protected DispenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public DispenserBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(BlockEntityType.DISPENSER, blockPos, blockState);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    public int getRandomSlot(RandomSource randomSource) {
        this.unpackLootTable(null);
        int n = -1;
        int n2 = 1;
        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i).isEmpty() || randomSource.nextInt(n2++) != 0) continue;
            n = i;
        }
        return n;
    }

    public ItemStack insertItem(ItemStack itemStack) {
        int n = this.getMaxStackSize(itemStack);
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack2 = this.items.get(i);
            if (!itemStack2.isEmpty() && !ItemStack.isSameItemSameComponents(itemStack, itemStack2)) continue;
            int n2 = Math.min(itemStack.getCount(), n - itemStack2.getCount());
            if (n2 > 0) {
                if (itemStack2.isEmpty()) {
                    this.setItem(i, itemStack.split(n2));
                } else {
                    itemStack.shrink(n2);
                    itemStack2.grow(n2);
                }
            }
            if (itemStack.isEmpty()) break;
        }
        return itemStack;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.dispenser");
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items, provider);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items, provider);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    protected AbstractContainerMenu createMenu(int n, Inventory inventory) {
        return new DispenserMenu(n, inventory, this);
    }
}

