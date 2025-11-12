/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrafterSlot
extends Slot {
    private final CrafterMenu menu;

    public CrafterSlot(Container container, int n, int n2, int n3, CrafterMenu crafterMenu) {
        super(container, n, n2, n3);
        this.menu = crafterMenu;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return !this.menu.isSlotDisabled(this.index) && super.mayPlace(itemStack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.menu.slotsChanged(this.container);
    }
}

