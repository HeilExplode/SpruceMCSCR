/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

public class PlayerEnderChestContainer
extends SimpleContainer {
    @Nullable
    private EnderChestBlockEntity activeChest;

    public PlayerEnderChestContainer() {
        super(27);
    }

    public void setActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
        this.activeChest = enderChestBlockEntity;
    }

    public boolean isActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
        return this.activeChest == enderChestBlockEntity;
    }

    @Override
    public void fromTag(ListTag listTag, HolderLookup.Provider provider) {
        int n;
        for (n = 0; n < this.getContainerSize(); ++n) {
            this.setItem(n, ItemStack.EMPTY);
        }
        for (n = 0; n < listTag.size(); ++n) {
            CompoundTag compoundTag = listTag.getCompound(n);
            int n2 = compoundTag.getByte("Slot") & 0xFF;
            if (n2 < 0 || n2 >= this.getContainerSize()) continue;
            this.setItem(n2, ItemStack.parse(provider, compoundTag).orElse(ItemStack.EMPTY));
        }
    }

    @Override
    public ListTag createTag(HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) continue;
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putByte("Slot", (byte)i);
            listTag.add(itemStack.save(provider, compoundTag));
        }
        return listTag;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.activeChest != null && !this.activeChest.stillValid(player)) {
            return false;
        }
        return super.stillValid(player);
    }

    @Override
    public void startOpen(Player player) {
        if (this.activeChest != null) {
            this.activeChest.startOpen(player);
        }
        super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        if (this.activeChest != null) {
            this.activeChest.stopOpen(player);
        }
        super.stopOpen(player);
        this.activeChest = null;
    }
}

