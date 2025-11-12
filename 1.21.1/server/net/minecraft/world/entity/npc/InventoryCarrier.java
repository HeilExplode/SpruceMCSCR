/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.npc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface InventoryCarrier {
    public static final String TAG_INVENTORY = "Inventory";

    public SimpleContainer getInventory();

    public static void pickUpItem(Mob mob, InventoryCarrier inventoryCarrier, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (mob.wantsToPickUp(itemStack)) {
            SimpleContainer simpleContainer = inventoryCarrier.getInventory();
            boolean bl = simpleContainer.canAddItem(itemStack);
            if (!bl) {
                return;
            }
            mob.onItemPickup(itemEntity);
            int n = itemStack.getCount();
            ItemStack itemStack2 = simpleContainer.addItem(itemStack);
            mob.take(itemEntity, n - itemStack2.getCount());
            if (itemStack2.isEmpty()) {
                itemEntity.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    default public void readInventoryFromTag(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (compoundTag.contains(TAG_INVENTORY, 9)) {
            this.getInventory().fromTag(compoundTag.getList(TAG_INVENTORY, 10), provider);
        }
    }

    default public void writeInventoryToTag(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.put(TAG_INVENTORY, this.getInventory().createTag(provider));
    }
}

