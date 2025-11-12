/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
    private final int slot;
    public final Container container;
    public int index;
    public final int x;
    public final int y;

    public Slot(Container container, int n, int n2, int n3) {
        this.container = container;
        this.slot = n;
        this.x = n2;
        this.y = n3;
    }

    public void onQuickCraft(ItemStack itemStack, ItemStack itemStack2) {
        int n = itemStack2.getCount() - itemStack.getCount();
        if (n > 0) {
            this.onQuickCraft(itemStack2, n);
        }
    }

    protected void onQuickCraft(ItemStack itemStack, int n) {
    }

    protected void onSwapCraft(int n) {
    }

    protected void checkTakeAchievements(ItemStack itemStack) {
    }

    public void onTake(Player player, ItemStack itemStack) {
        this.setChanged();
    }

    public boolean mayPlace(ItemStack itemStack) {
        return true;
    }

    public ItemStack getItem() {
        return this.container.getItem(this.slot);
    }

    public boolean hasItem() {
        return !this.getItem().isEmpty();
    }

    public void setByPlayer(ItemStack itemStack) {
        this.setByPlayer(itemStack, this.getItem());
    }

    public void setByPlayer(ItemStack itemStack, ItemStack itemStack2) {
        this.set(itemStack);
    }

    public void set(ItemStack itemStack) {
        this.container.setItem(this.slot, itemStack);
        this.setChanged();
    }

    public void setChanged() {
        this.container.setChanged();
    }

    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack itemStack) {
        return Math.min(this.getMaxStackSize(), itemStack.getMaxStackSize());
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return null;
    }

    public ItemStack remove(int n) {
        return this.container.removeItem(this.slot, n);
    }

    public boolean mayPickup(Player player) {
        return true;
    }

    public boolean isActive() {
        return true;
    }

    public Optional<ItemStack> tryRemove(int n, int n2, Player player) {
        if (!this.mayPickup(player)) {
            return Optional.empty();
        }
        if (!this.allowModification(player) && n2 < this.getItem().getCount()) {
            return Optional.empty();
        }
        ItemStack itemStack = this.remove(n = Math.min(n, n2));
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        if (this.getItem().isEmpty()) {
            this.setByPlayer(ItemStack.EMPTY, itemStack);
        }
        return Optional.of(itemStack);
    }

    public ItemStack safeTake(int n, int n2, Player player) {
        Optional<ItemStack> optional = this.tryRemove(n, n2, player);
        optional.ifPresent(itemStack -> this.onTake(player, (ItemStack)itemStack));
        return optional.orElse(ItemStack.EMPTY);
    }

    public ItemStack safeInsert(ItemStack itemStack) {
        return this.safeInsert(itemStack, itemStack.getCount());
    }

    public ItemStack safeInsert(ItemStack itemStack, int n) {
        if (itemStack.isEmpty() || !this.mayPlace(itemStack)) {
            return itemStack;
        }
        ItemStack itemStack2 = this.getItem();
        int n2 = Math.min(Math.min(n, itemStack.getCount()), this.getMaxStackSize(itemStack) - itemStack2.getCount());
        if (itemStack2.isEmpty()) {
            this.setByPlayer(itemStack.split(n2));
        } else if (ItemStack.isSameItemSameComponents(itemStack2, itemStack)) {
            itemStack.shrink(n2);
            itemStack2.grow(n2);
            this.setByPlayer(itemStack2);
        }
        return itemStack;
    }

    public boolean allowModification(Player player) {
        return this.mayPickup(player) && this.mayPlace(this.getItem());
    }

    public int getContainerSlot() {
        return this.slot;
    }

    public boolean isHighlightable() {
        return true;
    }

    public boolean isFake() {
        return false;
    }
}

