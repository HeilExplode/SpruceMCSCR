/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntListIterator
 *  javax.annotation.Nullable
 */
package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<I extends RecipeInput, R extends Recipe<I>>
implements PlaceRecipe<Integer> {
    private static final int ITEM_NOT_FOUND = -1;
    protected final StackedContents stackedContents = new StackedContents();
    protected Inventory inventory;
    protected RecipeBookMenu<I, R> menu;

    public ServerPlaceRecipe(RecipeBookMenu<I, R> recipeBookMenu) {
        this.menu = recipeBookMenu;
    }

    public void recipeClicked(ServerPlayer serverPlayer, @Nullable RecipeHolder<R> recipeHolder, boolean bl) {
        if (recipeHolder == null || !serverPlayer.getRecipeBook().contains(recipeHolder)) {
            return;
        }
        this.inventory = serverPlayer.getInventory();
        if (!this.testClearGrid() && !serverPlayer.isCreative()) {
            return;
        }
        this.stackedContents.clear();
        serverPlayer.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        if (this.stackedContents.canCraft((Recipe<?>)recipeHolder.value(), null)) {
            this.handleRecipeClicked(recipeHolder, bl);
        } else {
            this.clearGrid();
            serverPlayer.connection.send(new ClientboundPlaceGhostRecipePacket(serverPlayer.containerMenu.containerId, recipeHolder));
        }
        serverPlayer.getInventory().setChanged();
    }

    protected void clearGrid() {
        for (int i = 0; i < this.menu.getSize(); ++i) {
            if (!this.menu.shouldMoveToInventory(i)) continue;
            ItemStack itemStack = this.menu.getSlot(i).getItem().copy();
            this.inventory.placeItemBackInInventory(itemStack, false);
            this.menu.getSlot(i).set(itemStack);
        }
        this.menu.clearCraftingContent();
    }

    protected void handleRecipeClicked(RecipeHolder<R> recipeHolder, boolean bl) {
        Object object;
        int n;
        boolean bl2 = this.menu.recipeMatches(recipeHolder);
        int n2 = this.stackedContents.getBiggestCraftableStack(recipeHolder, null);
        if (bl2) {
            for (n = 0; n < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; ++n) {
                if (n == this.menu.getResultSlotIndex() || ((ItemStack)(object = this.menu.getSlot(n).getItem())).isEmpty() || Math.min(n2, ((ItemStack)object).getMaxStackSize()) >= ((ItemStack)object).getCount() + 1) continue;
                return;
            }
        }
        n = this.getStackSize(bl, n2, bl2);
        object = new IntArrayList();
        if (this.stackedContents.canCraft((Recipe<?>)recipeHolder.value(), (IntList)object, n)) {
            int n3 = n;
            IntListIterator intListIterator = object.iterator();
            while (intListIterator.hasNext()) {
                int n4;
                int n5 = (Integer)intListIterator.next();
                ItemStack itemStack = StackedContents.fromStackingIndex(n5);
                if (itemStack.isEmpty() || (n4 = itemStack.getMaxStackSize()) >= n3) continue;
                n3 = n4;
            }
            n = n3;
            if (this.stackedContents.canCraft((Recipe<?>)recipeHolder.value(), (IntList)object, n)) {
                this.clearGrid();
                this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipeHolder, object.iterator(), n);
            }
        }
    }

    @Override
    public void addItemToSlot(Integer n, int n2, int n3, int n4, int n5) {
        Slot slot = this.menu.getSlot(n2);
        ItemStack itemStack = StackedContents.fromStackingIndex(n);
        if (itemStack.isEmpty()) {
            return;
        }
        int n6 = n3;
        while (n6 > 0) {
            if ((n6 = this.moveItemToGrid(slot, itemStack, n6)) != -1) continue;
            return;
        }
    }

    protected int getStackSize(boolean bl, int n, boolean bl2) {
        int n2 = 1;
        if (bl) {
            n2 = n;
        } else if (bl2) {
            n2 = Integer.MAX_VALUE;
            for (int i = 0; i < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++i) {
                ItemStack itemStack;
                if (i == this.menu.getResultSlotIndex() || (itemStack = this.menu.getSlot(i).getItem()).isEmpty() || n2 <= itemStack.getCount()) continue;
                n2 = itemStack.getCount();
            }
            if (n2 != Integer.MAX_VALUE) {
                ++n2;
            }
        }
        return n2;
    }

    protected int moveItemToGrid(Slot slot, ItemStack itemStack, int n) {
        int n2;
        int n3 = this.inventory.findSlotMatchingUnusedItem(itemStack);
        if (n3 == -1) {
            return -1;
        }
        ItemStack itemStack2 = this.inventory.getItem(n3);
        if (n < itemStack2.getCount()) {
            this.inventory.removeItem(n3, n);
            n2 = n;
        } else {
            this.inventory.removeItemNoUpdate(n3);
            n2 = itemStack2.getCount();
        }
        if (slot.getItem().isEmpty()) {
            slot.set(itemStack2.copyWithCount(n2));
        } else {
            slot.getItem().grow(n2);
        }
        return n - n2;
    }

    private boolean testClearGrid() {
        ArrayList arrayList = Lists.newArrayList();
        int n = this.getAmountOfFreeSlotsInInventory();
        for (int i = 0; i < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++i) {
            ItemStack itemStack;
            if (i == this.menu.getResultSlotIndex() || (itemStack = this.menu.getSlot(i).getItem().copy()).isEmpty()) continue;
            int n2 = this.inventory.getSlotWithRemainingSpace(itemStack);
            if (n2 == -1 && arrayList.size() <= n) {
                for (ItemStack itemStack2 : arrayList) {
                    if (!ItemStack.isSameItem(itemStack2, itemStack) || itemStack2.getCount() == itemStack2.getMaxStackSize() || itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize()) continue;
                    itemStack2.grow(itemStack.getCount());
                    itemStack.setCount(0);
                    break;
                }
                if (itemStack.isEmpty()) continue;
                if (arrayList.size() < n) {
                    arrayList.add(itemStack);
                    continue;
                }
                return false;
            }
            if (n2 != -1) continue;
            return false;
        }
        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int n = 0;
        for (ItemStack itemStack : this.inventory.items) {
            if (!itemStack.isEmpty()) continue;
            ++n;
        }
        return n;
    }
}

