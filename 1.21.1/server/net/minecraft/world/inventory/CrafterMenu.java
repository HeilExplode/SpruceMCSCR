/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.CrafterSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.NonInteractiveResultSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;

public class CrafterMenu
extends AbstractContainerMenu
implements ContainerListener {
    protected static final int SLOT_COUNT = 9;
    private static final int INV_SLOT_START = 9;
    private static final int INV_SLOT_END = 36;
    private static final int USE_ROW_SLOT_START = 36;
    private static final int USE_ROW_SLOT_END = 45;
    private final ResultContainer resultContainer = new ResultContainer();
    private final ContainerData containerData;
    private final Player player;
    private final CraftingContainer container;

    public CrafterMenu(int n, Inventory inventory) {
        super(MenuType.CRAFTER_3x3, n);
        this.player = inventory.player;
        this.containerData = new SimpleContainerData(10);
        this.container = new TransientCraftingContainer(this, 3, 3);
        this.addSlots(inventory);
    }

    public CrafterMenu(int n, Inventory inventory, CraftingContainer craftingContainer, ContainerData containerData) {
        super(MenuType.CRAFTER_3x3, n);
        this.player = inventory.player;
        this.containerData = containerData;
        this.container = craftingContainer;
        CrafterMenu.checkContainerSize(craftingContainer, 9);
        craftingContainer.startOpen(inventory.player);
        this.addSlots(inventory);
        this.addSlotListener(this);
    }

    private void addSlots(Inventory inventory) {
        int n;
        int n2;
        for (n2 = 0; n2 < 3; ++n2) {
            for (n = 0; n < 3; ++n) {
                int n3 = n + n2 * 3;
                this.addSlot(new CrafterSlot(this.container, n3, 26 + n * 18, 17 + n2 * 18, this));
            }
        }
        for (n2 = 0; n2 < 3; ++n2) {
            for (n = 0; n < 9; ++n) {
                this.addSlot(new Slot(inventory, n + n2 * 9 + 9, 8 + n * 18, 84 + n2 * 18));
            }
        }
        for (n2 = 0; n2 < 9; ++n2) {
            this.addSlot(new Slot(inventory, n2, 8 + n2 * 18, 142));
        }
        this.addSlot(new NonInteractiveResultSlot(this.resultContainer, 0, 134, 35));
        this.addDataSlots(this.containerData);
        this.refreshRecipeResult();
    }

    public void setSlotState(int n, boolean bl) {
        CrafterSlot crafterSlot = (CrafterSlot)this.getSlot(n);
        this.containerData.set(crafterSlot.index, bl ? 0 : 1);
        this.broadcastChanges();
    }

    public boolean isSlotDisabled(int n) {
        if (n > -1 && n < 9) {
            return this.containerData.get(n) == 1;
        }
        return false;
    }

    public boolean isPowered() {
        return this.containerData.get(9) == 1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n < 9 ? !this.moveItemStackTo(itemStack2, 9, 45, true) : !this.moveItemStackTo(itemStack2, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
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

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    private void refreshRecipeResult() {
        Object object = this.player;
        if (object instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)object;
            object = serverPlayer.level();
            CraftingInput craftingInput = this.container.asCraftInput();
            ItemStack itemStack = CrafterBlock.getPotentialResults((Level)object, craftingInput).map(arg_0 -> CrafterMenu.lambda$refreshRecipeResult$0(craftingInput, (Level)object, arg_0)).orElse(ItemStack.EMPTY);
            this.resultContainer.setItem(0, itemStack);
        }
    }

    public Container getContainer() {
        return this.container;
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int n, ItemStack itemStack) {
        this.refreshRecipeResult();
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int n, int n2) {
    }

    private static /* synthetic */ ItemStack lambda$refreshRecipeResult$0(CraftingInput craftingInput, Level level, RecipeHolder recipeHolder) {
        return ((CraftingRecipe)recipeHolder.value()).assemble(craftingInput, level.registryAccess());
    }
}

