/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 */
package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrafterBlockEntity
extends RandomizableContainerBlockEntity
implements CraftingContainer {
    public static final int CONTAINER_WIDTH = 3;
    public static final int CONTAINER_HEIGHT = 3;
    public static final int CONTAINER_SIZE = 9;
    public static final int SLOT_DISABLED = 1;
    public static final int SLOT_ENABLED = 0;
    public static final int DATA_TRIGGERED = 9;
    public static final int NUM_DATA = 10;
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private int craftingTicksRemaining = 0;
    protected final ContainerData containerData = new ContainerData(this){
        private final int[] slotStates = new int[9];
        private int triggered = 0;

        @Override
        public int get(int n) {
            return n == 9 ? this.triggered : this.slotStates[n];
        }

        @Override
        public void set(int n, int n2) {
            if (n == 9) {
                this.triggered = n2;
            } else {
                this.slotStates[n] = n2;
            }
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public CrafterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CRAFTER, blockPos, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.crafter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int n, Inventory inventory) {
        return new CrafterMenu(n, inventory, this, this.containerData);
    }

    public void setSlotState(int n, boolean bl) {
        if (!this.slotCanBeDisabled(n)) {
            return;
        }
        this.containerData.set(n, bl ? 0 : 1);
        this.setChanged();
    }

    public boolean isSlotDisabled(int n) {
        if (n >= 0 && n < 9) {
            return this.containerData.get(n) == 1;
        }
        return false;
    }

    @Override
    public boolean canPlaceItem(int n, ItemStack itemStack) {
        if (this.containerData.get(n) == 1) {
            return false;
        }
        ItemStack itemStack2 = this.items.get(n);
        int n2 = itemStack2.getCount();
        if (n2 >= itemStack2.getMaxStackSize()) {
            return false;
        }
        if (itemStack2.isEmpty()) {
            return true;
        }
        return !this.smallerStackExist(n2, itemStack2, n);
    }

    private boolean smallerStackExist(int n, ItemStack itemStack, int n2) {
        for (int i = n2 + 1; i < 9; ++i) {
            ItemStack itemStack2;
            if (this.isSlotDisabled(i) || !(itemStack2 = this.getItem(i)).isEmpty() && (itemStack2.getCount() >= n || !ItemStack.isSameItemSameComponents(itemStack2, itemStack))) continue;
            return true;
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.craftingTicksRemaining = compoundTag.getInt("crafting_ticks_remaining");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items, provider);
        }
        int[] nArray = compoundTag.getIntArray("disabled_slots");
        for (int i = 0; i < 9; ++i) {
            this.containerData.set(i, 0);
        }
        for (int n : nArray) {
            if (!this.slotCanBeDisabled(n)) continue;
            this.containerData.set(n, 1);
        }
        this.containerData.set(9, compoundTag.getInt("triggered"));
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.putInt("crafting_ticks_remaining", this.craftingTicksRemaining);
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items, provider);
        }
        this.addDisabledSlots(compoundTag);
        this.addTriggered(compoundTag);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int n) {
        return this.items.get(n);
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        if (this.isSlotDisabled(n)) {
            this.setSlotState(n, true);
        }
        super.setItem(n, itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public void fillStackedContents(StackedContents stackedContents) {
        for (ItemStack itemStack : this.items) {
            stackedContents.accountSimpleStack(itemStack);
        }
    }

    private void addDisabledSlots(CompoundTag compoundTag) {
        IntArrayList intArrayList = new IntArrayList();
        for (int i = 0; i < 9; ++i) {
            if (!this.isSlotDisabled(i)) continue;
            intArrayList.add(i);
        }
        compoundTag.putIntArray("disabled_slots", (List<Integer>)intArrayList);
    }

    private void addTriggered(CompoundTag compoundTag) {
        compoundTag.putInt("triggered", this.containerData.get(9));
    }

    public void setTriggered(boolean bl) {
        this.containerData.set(9, bl ? 1 : 0);
    }

    @VisibleForTesting
    public boolean isTriggered() {
        return this.containerData.get(9) == 1;
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, CrafterBlockEntity crafterBlockEntity) {
        int n = crafterBlockEntity.craftingTicksRemaining - 1;
        if (n < 0) {
            return;
        }
        crafterBlockEntity.craftingTicksRemaining = n;
        if (n == 0) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(CrafterBlock.CRAFTING, false), 3);
        }
    }

    public void setCraftingTicksRemaining(int n) {
        this.craftingTicksRemaining = n;
    }

    public int getRedstoneSignal() {
        int n = 0;
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty() && !this.isSlotDisabled(i)) continue;
            ++n;
        }
        return n;
    }

    private boolean slotCanBeDisabled(int n) {
        return n > -1 && n < 9 && this.items.get(n).isEmpty();
    }

    public /* synthetic */ List getItems() {
        return this.getItems();
    }
}

