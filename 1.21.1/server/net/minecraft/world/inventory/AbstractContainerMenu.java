/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.inventory;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public abstract class AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SLOT_CLICKED_OUTSIDE = -999;
    public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;
    public static final int QUICKCRAFT_TYPE_GREEDY = 1;
    public static final int QUICKCRAFT_TYPE_CLONE = 2;
    public static final int QUICKCRAFT_HEADER_START = 0;
    public static final int QUICKCRAFT_HEADER_CONTINUE = 1;
    public static final int QUICKCRAFT_HEADER_END = 2;
    public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final NonNullList<Slot> slots = NonNullList.create();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    private ItemStack carried = ItemStack.EMPTY;
    private final NonNullList<ItemStack> remoteSlots = NonNullList.create();
    private final IntList remoteDataSlots = new IntArrayList();
    private ItemStack remoteCarried = ItemStack.EMPTY;
    private int stateId;
    @Nullable
    private final MenuType<?> menuType;
    public final int containerId;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    @Nullable
    private ContainerSynchronizer synchronizer;
    private boolean suppressRemoteUpdates;

    protected AbstractContainerMenu(@Nullable MenuType<?> menuType, int n) {
        this.menuType = menuType;
        this.containerId = n;
    }

    protected static boolean stillValid(ContainerLevelAccess containerLevelAccess, Player player, Block block) {
        return containerLevelAccess.evaluate((level, blockPos) -> {
            if (!level.getBlockState((BlockPos)blockPos).is(block)) {
                return false;
            }
            return player.canInteractWithBlock((BlockPos)blockPos, 4.0);
        }, true);
    }

    public MenuType<?> getType() {
        if (this.menuType == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        }
        return this.menuType;
    }

    protected static void checkContainerSize(Container container, int n) {
        int n2 = container.getContainerSize();
        if (n2 < n) {
            throw new IllegalArgumentException("Container size " + n2 + " is smaller than expected " + n);
        }
    }

    protected static void checkContainerDataCount(ContainerData containerData, int n) {
        int n2 = containerData.getCount();
        if (n2 < n) {
            throw new IllegalArgumentException("Container data count " + n2 + " is smaller than expected " + n);
        }
    }

    public boolean isValidSlotIndex(int n) {
        return n == -1 || n == -999 || n < this.slots.size();
    }

    protected Slot addSlot(Slot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
        this.lastSlots.add(ItemStack.EMPTY);
        this.remoteSlots.add(ItemStack.EMPTY);
        return slot;
    }

    protected DataSlot addDataSlot(DataSlot dataSlot) {
        this.dataSlots.add(dataSlot);
        this.remoteDataSlots.add(0);
        return dataSlot;
    }

    protected void addDataSlots(ContainerData containerData) {
        for (int i = 0; i < containerData.getCount(); ++i) {
            this.addDataSlot(DataSlot.forContainer(containerData, i));
        }
    }

    public void addSlotListener(ContainerListener containerListener) {
        if (this.containerListeners.contains(containerListener)) {
            return;
        }
        this.containerListeners.add(containerListener);
        this.broadcastChanges();
    }

    public void setSynchronizer(ContainerSynchronizer containerSynchronizer) {
        this.synchronizer = containerSynchronizer;
        this.sendAllDataToRemote();
    }

    public void sendAllDataToRemote() {
        int n;
        int n2 = this.slots.size();
        for (n = 0; n < n2; ++n) {
            this.remoteSlots.set(n, this.slots.get(n).getItem().copy());
        }
        this.remoteCarried = this.getCarried().copy();
        n2 = this.dataSlots.size();
        for (n = 0; n < n2; ++n) {
            this.remoteDataSlots.set(n, this.dataSlots.get(n).get());
        }
        if (this.synchronizer != null) {
            this.synchronizer.sendInitialData(this, this.remoteSlots, this.remoteCarried, this.remoteDataSlots.toIntArray());
        }
    }

    public void removeSlotListener(ContainerListener containerListener) {
        this.containerListeners.remove(containerListener);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        for (Slot slot : this.slots) {
            nonNullList.add(slot.getItem());
        }
        return nonNullList;
    }

    public void broadcastChanges() {
        Object object;
        int n;
        for (n = 0; n < this.slots.size(); ++n) {
            object = this.slots.get(n).getItem();
            com.google.common.base.Supplier supplier = Suppliers.memoize(((ItemStack)object)::copy);
            this.triggerSlotListeners(n, (ItemStack)object, (Supplier<ItemStack>)supplier);
            this.synchronizeSlotToRemote(n, (ItemStack)object, (Supplier<ItemStack>)supplier);
        }
        this.synchronizeCarriedToRemote();
        for (n = 0; n < this.dataSlots.size(); ++n) {
            object = this.dataSlots.get(n);
            int n2 = ((DataSlot)object).get();
            if (((DataSlot)object).checkAndClearUpdateFlag()) {
                this.updateDataSlotListeners(n, n2);
            }
            this.synchronizeDataSlotToRemote(n, n2);
        }
    }

    public void broadcastFullState() {
        Object object;
        int n;
        for (n = 0; n < this.slots.size(); ++n) {
            object = this.slots.get(n).getItem();
            this.triggerSlotListeners(n, (ItemStack)object, ((ItemStack)object)::copy);
        }
        for (n = 0; n < this.dataSlots.size(); ++n) {
            object = this.dataSlots.get(n);
            if (!((DataSlot)object).checkAndClearUpdateFlag()) continue;
            this.updateDataSlotListeners(n, ((DataSlot)object).get());
        }
        this.sendAllDataToRemote();
    }

    private void updateDataSlotListeners(int n, int n2) {
        for (ContainerListener containerListener : this.containerListeners) {
            containerListener.dataChanged(this, n, n2);
        }
    }

    private void triggerSlotListeners(int n, ItemStack itemStack, Supplier<ItemStack> supplier) {
        ItemStack itemStack2 = this.lastSlots.get(n);
        if (!ItemStack.matches(itemStack2, itemStack)) {
            ItemStack itemStack3 = supplier.get();
            this.lastSlots.set(n, itemStack3);
            for (ContainerListener containerListener : this.containerListeners) {
                containerListener.slotChanged(this, n, itemStack3);
            }
        }
    }

    private void synchronizeSlotToRemote(int n, ItemStack itemStack, Supplier<ItemStack> supplier) {
        if (this.suppressRemoteUpdates) {
            return;
        }
        ItemStack itemStack2 = this.remoteSlots.get(n);
        if (!ItemStack.matches(itemStack2, itemStack)) {
            ItemStack itemStack3 = supplier.get();
            this.remoteSlots.set(n, itemStack3);
            if (this.synchronizer != null) {
                this.synchronizer.sendSlotChange(this, n, itemStack3);
            }
        }
    }

    private void synchronizeDataSlotToRemote(int n, int n2) {
        if (this.suppressRemoteUpdates) {
            return;
        }
        int n3 = this.remoteDataSlots.getInt(n);
        if (n3 != n2) {
            this.remoteDataSlots.set(n, n2);
            if (this.synchronizer != null) {
                this.synchronizer.sendDataChange(this, n, n2);
            }
        }
    }

    private void synchronizeCarriedToRemote() {
        if (this.suppressRemoteUpdates) {
            return;
        }
        if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
            this.remoteCarried = this.getCarried().copy();
            if (this.synchronizer != null) {
                this.synchronizer.sendCarriedChange(this, this.remoteCarried);
            }
        }
    }

    public void setRemoteSlot(int n, ItemStack itemStack) {
        this.remoteSlots.set(n, itemStack.copy());
    }

    public void setRemoteSlotNoCopy(int n, ItemStack itemStack) {
        if (n < 0 || n >= this.remoteSlots.size()) {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", (Object)n, (Object)this.remoteSlots.size());
            return;
        }
        this.remoteSlots.set(n, itemStack);
    }

    public void setRemoteCarried(ItemStack itemStack) {
        this.remoteCarried = itemStack.copy();
    }

    public boolean clickMenuButton(Player player, int n) {
        return false;
    }

    public Slot getSlot(int n) {
        return this.slots.get(n);
    }

    public abstract ItemStack quickMoveStack(Player var1, int var2);

    public void clicked(int n, int n2, ClickType clickType, Player player) {
        try {
            this.doClick(n, n2, clickType, player);
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>");
            crashReportCategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashReportCategory.setDetail("Slot Count", this.slots.size());
            crashReportCategory.setDetail("Slot", n);
            crashReportCategory.setDetail("Button", n2);
            crashReportCategory.setDetail("Type", (Object)clickType);
            throw new ReportedException(crashReport);
        }
    }

    private void doClick(int n, int n2, ClickType clickType, Player player) {
        block39: {
            block50: {
                block46: {
                    ItemStack itemStack3;
                    Slot slot;
                    ItemStack itemStack4;
                    Inventory inventory;
                    block49: {
                        block48: {
                            block47: {
                                block44: {
                                    ClickAction clickAction;
                                    block45: {
                                        block43: {
                                            block37: {
                                                block42: {
                                                    ItemStack itemStack5;
                                                    block41: {
                                                        block40: {
                                                            block38: {
                                                                inventory = player.getInventory();
                                                                if (clickType != ClickType.QUICK_CRAFT) break block37;
                                                                int n3 = this.quickcraftStatus;
                                                                this.quickcraftStatus = AbstractContainerMenu.getQuickcraftHeader(n2);
                                                                if (n3 == 1 && this.quickcraftStatus == 2 || n3 == this.quickcraftStatus) break block38;
                                                                this.resetQuickCraft();
                                                                break block39;
                                                            }
                                                            if (!this.getCarried().isEmpty()) break block40;
                                                            this.resetQuickCraft();
                                                            break block39;
                                                        }
                                                        if (this.quickcraftStatus != 0) break block41;
                                                        this.quickcraftType = AbstractContainerMenu.getQuickcraftType(n2);
                                                        if (AbstractContainerMenu.isValidQuickcraftType(this.quickcraftType, player)) {
                                                            this.quickcraftStatus = 1;
                                                            this.quickcraftSlots.clear();
                                                        } else {
                                                            this.resetQuickCraft();
                                                        }
                                                        break block39;
                                                    }
                                                    if (this.quickcraftStatus != 1) break block42;
                                                    Slot slot2 = this.slots.get(n);
                                                    if (!AbstractContainerMenu.canItemQuickReplace(slot2, itemStack5 = this.getCarried(), true) || !slot2.mayPlace(itemStack5) || this.quickcraftType != 2 && itemStack5.getCount() <= this.quickcraftSlots.size() || !this.canDragTo(slot2)) break block39;
                                                    this.quickcraftSlots.add(slot2);
                                                    break block39;
                                                }
                                                if (this.quickcraftStatus == 2) {
                                                    if (!this.quickcraftSlots.isEmpty()) {
                                                        if (this.quickcraftSlots.size() == 1) {
                                                            int n4 = this.quickcraftSlots.iterator().next().index;
                                                            this.resetQuickCraft();
                                                            this.doClick(n4, this.quickcraftType, ClickType.PICKUP, player);
                                                            return;
                                                        }
                                                        ItemStack itemStack6 = this.getCarried().copy();
                                                        if (itemStack6.isEmpty()) {
                                                            this.resetQuickCraft();
                                                            return;
                                                        }
                                                        int n5 = this.getCarried().getCount();
                                                        for (Slot slot3 : this.quickcraftSlots) {
                                                            ItemStack itemStack7 = this.getCarried();
                                                            if (slot3 == null || !AbstractContainerMenu.canItemQuickReplace(slot3, itemStack7, true) || !slot3.mayPlace(itemStack7) || this.quickcraftType != 2 && itemStack7.getCount() < this.quickcraftSlots.size() || !this.canDragTo(slot3)) continue;
                                                            int n6 = slot3.hasItem() ? slot3.getItem().getCount() : 0;
                                                            int n7 = Math.min(itemStack6.getMaxStackSize(), slot3.getMaxStackSize(itemStack6));
                                                            int n8 = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemStack6) + n6, n7);
                                                            n5 -= n8 - n6;
                                                            slot3.setByPlayer(itemStack6.copyWithCount(n8));
                                                        }
                                                        itemStack6.setCount(n5);
                                                        this.setCarried(itemStack6);
                                                    }
                                                    this.resetQuickCraft();
                                                } else {
                                                    this.resetQuickCraft();
                                                }
                                                break block39;
                                            }
                                            if (this.quickcraftStatus == 0) break block43;
                                            this.resetQuickCraft();
                                            break block39;
                                        }
                                        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE || n2 != 0 && n2 != 1) break block44;
                                        ClickAction clickAction2 = clickAction = n2 == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
                                        if (n != -999) break block45;
                                        if (this.getCarried().isEmpty()) break block39;
                                        if (clickAction == ClickAction.PRIMARY) {
                                            player.drop(this.getCarried(), true);
                                            this.setCarried(ItemStack.EMPTY);
                                        } else {
                                            player.drop(this.getCarried().split(1), true);
                                        }
                                        break block39;
                                    }
                                    if (clickType == ClickType.QUICK_MOVE) {
                                        if (n < 0) {
                                            return;
                                        }
                                        Slot slot4 = this.slots.get(n);
                                        if (!slot4.mayPickup(player)) {
                                            return;
                                        }
                                        ItemStack itemStack8 = this.quickMoveStack(player, n);
                                        while (!itemStack8.isEmpty() && ItemStack.isSameItem(slot4.getItem(), itemStack8)) {
                                            itemStack8 = this.quickMoveStack(player, n);
                                        }
                                    } else {
                                        if (n < 0) {
                                            return;
                                        }
                                        Slot slot5 = this.slots.get(n);
                                        ItemStack itemStack9 = slot5.getItem();
                                        ItemStack itemStack10 = this.getCarried();
                                        player.updateTutorialInventoryAction(itemStack10, slot5.getItem(), clickAction);
                                        if (!this.tryItemClickBehaviourOverride(player, clickAction, slot5, itemStack9, itemStack10)) {
                                            if (itemStack9.isEmpty()) {
                                                if (!itemStack10.isEmpty()) {
                                                    int n9 = clickAction == ClickAction.PRIMARY ? itemStack10.getCount() : 1;
                                                    this.setCarried(slot5.safeInsert(itemStack10, n9));
                                                }
                                            } else if (slot5.mayPickup(player)) {
                                                if (itemStack10.isEmpty()) {
                                                    int n10 = clickAction == ClickAction.PRIMARY ? itemStack9.getCount() : (itemStack9.getCount() + 1) / 2;
                                                    Optional<ItemStack> optional = slot5.tryRemove(n10, Integer.MAX_VALUE, player);
                                                    optional.ifPresent(itemStack -> {
                                                        this.setCarried((ItemStack)itemStack);
                                                        slot5.onTake(player, (ItemStack)itemStack);
                                                    });
                                                } else if (slot5.mayPlace(itemStack10)) {
                                                    if (ItemStack.isSameItemSameComponents(itemStack9, itemStack10)) {
                                                        int n11 = clickAction == ClickAction.PRIMARY ? itemStack10.getCount() : 1;
                                                        this.setCarried(slot5.safeInsert(itemStack10, n11));
                                                    } else if (itemStack10.getCount() <= slot5.getMaxStackSize(itemStack10)) {
                                                        this.setCarried(itemStack9);
                                                        slot5.setByPlayer(itemStack10);
                                                    }
                                                } else if (ItemStack.isSameItemSameComponents(itemStack9, itemStack10)) {
                                                    Optional<ItemStack> optional = slot5.tryRemove(itemStack9.getCount(), itemStack10.getMaxStackSize() - itemStack10.getCount(), player);
                                                    optional.ifPresent(itemStack2 -> {
                                                        itemStack10.grow(itemStack2.getCount());
                                                        slot5.onTake(player, (ItemStack)itemStack2);
                                                    });
                                                }
                                            }
                                        }
                                        slot5.setChanged();
                                    }
                                    break block39;
                                }
                                if (clickType != ClickType.SWAP || (n2 < 0 || n2 >= 9) && n2 != 40) break block46;
                                itemStack4 = inventory.getItem(n2);
                                slot = this.slots.get(n);
                                itemStack3 = slot.getItem();
                                if (itemStack4.isEmpty() && itemStack3.isEmpty()) break block39;
                                if (!itemStack4.isEmpty()) break block47;
                                if (!slot.mayPickup(player)) break block39;
                                inventory.setItem(n2, itemStack3);
                                slot.onSwapCraft(itemStack3.getCount());
                                slot.setByPlayer(ItemStack.EMPTY);
                                slot.onTake(player, itemStack3);
                                break block39;
                            }
                            if (!itemStack3.isEmpty()) break block48;
                            if (!slot.mayPlace(itemStack4)) break block39;
                            int n12 = slot.getMaxStackSize(itemStack4);
                            if (itemStack4.getCount() > n12) {
                                slot.setByPlayer(itemStack4.split(n12));
                            } else {
                                inventory.setItem(n2, ItemStack.EMPTY);
                                slot.setByPlayer(itemStack4);
                            }
                            break block39;
                        }
                        if (!slot.mayPickup(player) || !slot.mayPlace(itemStack4)) break block39;
                        int n13 = slot.getMaxStackSize(itemStack4);
                        if (itemStack4.getCount() <= n13) break block49;
                        slot.setByPlayer(itemStack4.split(n13));
                        slot.onTake(player, itemStack3);
                        if (inventory.add(itemStack3)) break block39;
                        player.drop(itemStack3, true);
                        break block39;
                    }
                    inventory.setItem(n2, itemStack3);
                    slot.setByPlayer(itemStack4);
                    slot.onTake(player, itemStack3);
                    break block39;
                }
                if (clickType != ClickType.CLONE || !player.hasInfiniteMaterials() || !this.getCarried().isEmpty() || n < 0) break block50;
                Slot slot = this.slots.get(n);
                if (!slot.hasItem()) break block39;
                ItemStack itemStack11 = slot.getItem();
                this.setCarried(itemStack11.copyWithCount(itemStack11.getMaxStackSize()));
                break block39;
            }
            if (clickType == ClickType.THROW && this.getCarried().isEmpty() && n >= 0) {
                Slot slot = this.slots.get(n);
                int n14 = n2 == 0 ? 1 : slot.getItem().getCount();
                ItemStack itemStack12 = slot.safeTake(n14, Integer.MAX_VALUE, player);
                player.drop(itemStack12, true);
            } else if (clickType == ClickType.PICKUP_ALL && n >= 0) {
                Slot slot = this.slots.get(n);
                ItemStack itemStack13 = this.getCarried();
                if (!(itemStack13.isEmpty() || slot.hasItem() && slot.mayPickup(player))) {
                    int n15 = n2 == 0 ? 0 : this.slots.size() - 1;
                    int n16 = n2 == 0 ? 1 : -1;
                    for (int i = 0; i < 2; ++i) {
                        for (int j = n15; j >= 0 && j < this.slots.size() && itemStack13.getCount() < itemStack13.getMaxStackSize(); j += n16) {
                            Slot slot6 = this.slots.get(j);
                            if (!slot6.hasItem() || !AbstractContainerMenu.canItemQuickReplace(slot6, itemStack13, true) || !slot6.mayPickup(player) || !this.canTakeItemForPickAll(itemStack13, slot6)) continue;
                            ItemStack itemStack14 = slot6.getItem();
                            if (i == 0 && itemStack14.getCount() == itemStack14.getMaxStackSize()) continue;
                            ItemStack itemStack15 = slot6.safeTake(itemStack14.getCount(), itemStack13.getMaxStackSize() - itemStack13.getCount(), player);
                            itemStack13.grow(itemStack15.getCount());
                        }
                    }
                }
            }
        }
    }

    private boolean tryItemClickBehaviourOverride(Player player, ClickAction clickAction, Slot slot, ItemStack itemStack, ItemStack itemStack2) {
        FeatureFlagSet featureFlagSet = player.level().enabledFeatures();
        if (itemStack2.isItemEnabled(featureFlagSet) && itemStack2.overrideStackedOnOther(slot, clickAction, player)) {
            return true;
        }
        return itemStack.isItemEnabled(featureFlagSet) && itemStack.overrideOtherStackedOnMe(itemStack2, slot, clickAction, player, this.createCarriedSlotAccess());
    }

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return AbstractContainerMenu.this.getCarried();
            }

            @Override
            public boolean set(ItemStack itemStack) {
                AbstractContainerMenu.this.setCarried(itemStack);
                return true;
            }
        };
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return true;
    }

    public void removed(Player player) {
        ItemStack itemStack;
        if (player instanceof ServerPlayer && !(itemStack = this.getCarried()).isEmpty()) {
            if (!player.isAlive() || ((ServerPlayer)player).hasDisconnected()) {
                player.drop(itemStack, false);
            } else {
                player.getInventory().placeItemBackInInventory(itemStack);
            }
            this.setCarried(ItemStack.EMPTY);
        }
    }

    protected void clearContainer(Player player, Container container) {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for (int i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }
            return;
        }
        for (int i = 0; i < container.getContainerSize(); ++i) {
            Inventory inventory = player.getInventory();
            if (!(inventory.player instanceof ServerPlayer)) continue;
            inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
        }
    }

    public void slotsChanged(Container container) {
        this.broadcastChanges();
    }

    public void setItem(int n, int n2, ItemStack itemStack) {
        this.getSlot(n).set(itemStack);
        this.stateId = n2;
    }

    public void initializeContents(int n, List<ItemStack> list, ItemStack itemStack) {
        for (int i = 0; i < list.size(); ++i) {
            this.getSlot(i).set(list.get(i));
        }
        this.carried = itemStack;
        this.stateId = n;
    }

    public void setData(int n, int n2) {
        this.dataSlots.get(n).set(n2);
    }

    public abstract boolean stillValid(Player var1);

    protected boolean moveItemStackTo(ItemStack itemStack, int n, int n2, boolean bl) {
        int n3;
        ItemStack itemStack2;
        Slot slot;
        boolean bl2 = false;
        int n4 = n;
        if (bl) {
            n4 = n2 - 1;
        }
        if (itemStack.isStackable()) {
            while (!itemStack.isEmpty() && (bl ? n4 >= n : n4 < n2)) {
                slot = this.slots.get(n4);
                itemStack2 = slot.getItem();
                if (!itemStack2.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
                    int n5;
                    n3 = itemStack2.getCount() + itemStack.getCount();
                    if (n3 <= (n5 = slot.getMaxStackSize(itemStack2))) {
                        itemStack.setCount(0);
                        itemStack2.setCount(n3);
                        slot.setChanged();
                        bl2 = true;
                    } else if (itemStack2.getCount() < n5) {
                        itemStack.shrink(n5 - itemStack2.getCount());
                        itemStack2.setCount(n5);
                        slot.setChanged();
                        bl2 = true;
                    }
                }
                if (bl) {
                    --n4;
                    continue;
                }
                ++n4;
            }
        }
        if (!itemStack.isEmpty()) {
            n4 = bl ? n2 - 1 : n;
            while (bl ? n4 >= n : n4 < n2) {
                slot = this.slots.get(n4);
                itemStack2 = slot.getItem();
                if (itemStack2.isEmpty() && slot.mayPlace(itemStack)) {
                    n3 = slot.getMaxStackSize(itemStack);
                    slot.setByPlayer(itemStack.split(Math.min(itemStack.getCount(), n3)));
                    slot.setChanged();
                    bl2 = true;
                    break;
                }
                if (bl) {
                    --n4;
                    continue;
                }
                ++n4;
            }
        }
        return bl2;
    }

    public static int getQuickcraftType(int n) {
        return n >> 2 & 3;
    }

    public static int getQuickcraftHeader(int n) {
        return n & 3;
    }

    public static int getQuickcraftMask(int n, int n2) {
        return n & 3 | (n2 & 3) << 2;
    }

    public static boolean isValidQuickcraftType(int n, Player player) {
        if (n == 0) {
            return true;
        }
        if (n == 1) {
            return true;
        }
        return n == 2 && player.hasInfiniteMaterials();
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemStack, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = slot == null || !slot.hasItem();
        if (!bl2 && ItemStack.isSameItemSameComponents(itemStack, slot.getItem())) {
            return slot.getItem().getCount() + (bl ? 0 : itemStack.getCount()) <= itemStack.getMaxStackSize();
        }
        return bl2;
    }

    public static int getQuickCraftPlaceCount(Set<Slot> set, int n, ItemStack itemStack) {
        return switch (n) {
            case 0 -> Mth.floor((float)itemStack.getCount() / (float)set.size());
            case 1 -> 1;
            case 2 -> itemStack.getMaxStackSize();
            default -> itemStack.getCount();
        };
    }

    public boolean canDragTo(Slot slot) {
        return true;
    }

    public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof Container) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)((Object)blockEntity));
        }
        return 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable Container container) {
        if (container == null) {
            return 0;
        }
        float f = 0.0f;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isEmpty()) continue;
            f += (float)itemStack.getCount() / (float)container.getMaxStackSize(itemStack);
        }
        return Mth.lerpDiscrete(f /= (float)container.getContainerSize(), 0, 15);
    }

    public void setCarried(ItemStack itemStack) {
        this.carried = itemStack;
    }

    public ItemStack getCarried() {
        return this.carried;
    }

    public void suppressRemoteUpdates() {
        this.suppressRemoteUpdates = true;
    }

    public void resumeRemoteUpdates() {
        this.suppressRemoteUpdates = false;
    }

    public void transferState(AbstractContainerMenu abstractContainerMenu) {
        Slot slot;
        int n;
        HashBasedTable hashBasedTable = HashBasedTable.create();
        for (n = 0; n < abstractContainerMenu.slots.size(); ++n) {
            slot = abstractContainerMenu.slots.get(n);
            hashBasedTable.put((Object)slot.container, (Object)slot.getContainerSlot(), (Object)n);
        }
        for (n = 0; n < this.slots.size(); ++n) {
            slot = this.slots.get(n);
            Integer n2 = (Integer)hashBasedTable.get((Object)slot.container, (Object)slot.getContainerSlot());
            if (n2 == null) continue;
            this.lastSlots.set(n, abstractContainerMenu.lastSlots.get(n2));
            this.remoteSlots.set(n, abstractContainerMenu.remoteSlots.get(n2));
        }
    }

    public OptionalInt findSlot(Container container, int n) {
        for (int i = 0; i < this.slots.size(); ++i) {
            Slot slot = this.slots.get(i);
            if (slot.container != container || n != slot.getContainerSlot()) continue;
            return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }

    public int getStateId() {
        return this.stateId;
    }

    public int incrementStateId() {
        this.stateId = this.stateId + 1 & Short.MAX_VALUE;
        return this.stateId;
    }
}

