/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BeaconMenu
extends AbstractContainerMenu {
    private static final int PAYMENT_SLOT = 0;
    private static final int SLOT_COUNT = 1;
    private static final int DATA_COUNT = 3;
    private static final int INV_SLOT_START = 1;
    private static final int INV_SLOT_END = 28;
    private static final int USE_ROW_SLOT_START = 28;
    private static final int USE_ROW_SLOT_END = 37;
    private static final int NO_EFFECT = 0;
    private final Container beacon = new SimpleContainer(this, 1){

        @Override
        public boolean canPlaceItem(int n, ItemStack itemStack) {
            return itemStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    private final PaymentSlot paymentSlot;
    private final ContainerLevelAccess access;
    private final ContainerData beaconData;

    public BeaconMenu(int n, Container container) {
        this(n, container, new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }

    public BeaconMenu(int n, Container container, ContainerData containerData, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.BEACON, n);
        int n2;
        BeaconMenu.checkContainerDataCount(containerData, 3);
        this.beaconData = containerData;
        this.access = containerLevelAccess;
        this.paymentSlot = new PaymentSlot(this, this.beacon, 0, 136, 110);
        this.addSlot(this.paymentSlot);
        this.addDataSlots(containerData);
        int n3 = 36;
        int n4 = 137;
        for (n2 = 0; n2 < 3; ++n2) {
            for (int i = 0; i < 9; ++i) {
                this.addSlot(new Slot(container, i + n2 * 9 + 9, 36 + i * 18, 137 + n2 * 18));
            }
        }
        for (n2 = 0; n2 < 9; ++n2) {
            this.addSlot(new Slot(container, n2, 36 + n2 * 18, 195));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) {
            return;
        }
        ItemStack itemStack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
        if (!itemStack.isEmpty()) {
            player.drop(itemStack, false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return BeaconMenu.stillValid(this.access, player, Blocks.BEACON);
    }

    @Override
    public void setData(int n, int n2) {
        super.setData(n, n2);
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n == 0) {
                if (!this.moveItemStackTo(itemStack2, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (!this.paymentSlot.hasItem() && this.paymentSlot.mayPlace(itemStack2) && itemStack2.getCount() == 1 ? !this.moveItemStackTo(itemStack2, 0, 1, false) : (n >= 1 && n < 28 ? !this.moveItemStackTo(itemStack2, 28, 37, false) : (n >= 28 && n < 37 ? !this.moveItemStackTo(itemStack2, 1, 28, false) : !this.moveItemStackTo(itemStack2, 1, 37, false)))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }

    public int getLevels() {
        return this.beaconData.get(0);
    }

    public static int encodeEffect(@Nullable Holder<MobEffect> holder) {
        return holder == null ? 0 : BuiltInRegistries.MOB_EFFECT.asHolderIdMap().getId(holder) + 1;
    }

    @Nullable
    public static Holder<MobEffect> decodeEffect(int n) {
        return n == 0 ? null : BuiltInRegistries.MOB_EFFECT.asHolderIdMap().byId(n - 1);
    }

    @Nullable
    public Holder<MobEffect> getPrimaryEffect() {
        return BeaconMenu.decodeEffect(this.beaconData.get(1));
    }

    @Nullable
    public Holder<MobEffect> getSecondaryEffect() {
        return BeaconMenu.decodeEffect(this.beaconData.get(2));
    }

    public void updateEffects(Optional<Holder<MobEffect>> optional, Optional<Holder<MobEffect>> optional2) {
        if (this.paymentSlot.hasItem()) {
            this.beaconData.set(1, BeaconMenu.encodeEffect(optional.orElse(null)));
            this.beaconData.set(2, BeaconMenu.encodeEffect(optional2.orElse(null)));
            this.paymentSlot.remove(1);
            this.access.execute(Level::blockEntityChanged);
        }
    }

    public boolean hasPayment() {
        return !this.beacon.getItem(0).isEmpty();
    }

    class PaymentSlot
    extends Slot {
        public PaymentSlot(BeaconMenu beaconMenu, Container container, int n, int n2, int n3) {
            super(container, n, n2, n3);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return itemStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}

