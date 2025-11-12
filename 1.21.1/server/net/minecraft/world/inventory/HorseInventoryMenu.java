/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorseInventoryMenu
extends AbstractContainerMenu {
    private final Container horseContainer;
    private final Container armorContainer;
    private final AbstractHorse horse;
    private static final int SLOT_BODY_ARMOR = 1;
    private static final int SLOT_HORSE_INVENTORY_START = 2;

    public HorseInventoryMenu(int n, Inventory inventory, Container container, final AbstractHorse abstractHorse, int n2) {
        super(null, n);
        int n3;
        int n4;
        this.horseContainer = container;
        this.armorContainer = abstractHorse.getBodyArmorAccess();
        this.horse = abstractHorse;
        int n5 = 3;
        container.startOpen(inventory.player);
        int n6 = -18;
        this.addSlot(new Slot(this, container, 0, 8, 18){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(Items.SADDLE) && !this.hasItem() && abstractHorse.isSaddleable();
            }

            @Override
            public boolean isActive() {
                return abstractHorse.isSaddleable();
            }
        });
        this.addSlot(new ArmorSlot(this, this.armorContainer, abstractHorse, EquipmentSlot.BODY, 0, 8, 36, null){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return abstractHorse.isBodyArmorItem(itemStack);
            }

            @Override
            public boolean isActive() {
                return abstractHorse.canUseSlot(EquipmentSlot.BODY);
            }
        });
        if (n2 > 0) {
            for (n4 = 0; n4 < 3; ++n4) {
                for (n3 = 0; n3 < n2; ++n3) {
                    this.addSlot(new Slot(container, 1 + n3 + n4 * n2, 80 + n3 * 18, 18 + n4 * 18));
                }
            }
        }
        for (n4 = 0; n4 < 3; ++n4) {
            for (n3 = 0; n3 < 9; ++n3) {
                this.addSlot(new Slot(inventory, n3 + n4 * 9 + 9, 8 + n3 * 18, 102 + n4 * 18 + -18));
            }
        }
        for (n4 = 0; n4 < 9; ++n4) {
            this.addSlot(new Slot(inventory, n4, 8 + n4 * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !this.horse.hasInventoryChanged(this.horseContainer) && this.horseContainer.stillValid(player) && this.armorContainer.stillValid(player) && this.horse.isAlive() && player.canInteractWithEntity(this.horse, 4.0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int n2 = this.horseContainer.getContainerSize() + 1;
            if (n < n2) {
                if (!this.moveItemStackTo(itemStack2, n2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemStack2) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemStack2)) {
                if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (n2 <= 1 || !this.moveItemStackTo(itemStack2, 2, n2, false)) {
                int n3;
                int n4 = n2;
                int n5 = n3 = n4 + 27;
                int n6 = n5 + 9;
                if (n >= n5 && n < n6 ? !this.moveItemStackTo(itemStack2, n4, n3, false) : (n >= n4 && n < n3 ? !this.moveItemStackTo(itemStack2, n5, n6, false) : !this.moveItemStackTo(itemStack2, n5, n3, false))) {
                    return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.horseContainer.stopOpen(player);
    }
}

