/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;

public class BrewingStandMenu
extends AbstractContainerMenu {
    private static final int BOTTLE_SLOT_START = 0;
    private static final int BOTTLE_SLOT_END = 2;
    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int SLOT_COUNT = 5;
    private static final int DATA_COUNT = 2;
    private static final int INV_SLOT_START = 5;
    private static final int INV_SLOT_END = 32;
    private static final int USE_ROW_SLOT_START = 32;
    private static final int USE_ROW_SLOT_END = 41;
    private final Container brewingStand;
    private final ContainerData brewingStandData;
    private final Slot ingredientSlot;

    public BrewingStandMenu(int n, Inventory inventory) {
        this(n, inventory, new SimpleContainer(5), new SimpleContainerData(2));
    }

    public BrewingStandMenu(int n, Inventory inventory, Container container, ContainerData containerData) {
        super(MenuType.BREWING_STAND, n);
        int n2;
        BrewingStandMenu.checkContainerSize(container, 5);
        BrewingStandMenu.checkContainerDataCount(containerData, 2);
        this.brewingStand = container;
        this.brewingStandData = containerData;
        PotionBrewing potionBrewing = inventory.player.level().potionBrewing();
        this.addSlot(new PotionSlot(container, 0, 56, 51));
        this.addSlot(new PotionSlot(container, 1, 79, 58));
        this.addSlot(new PotionSlot(container, 2, 102, 51));
        this.ingredientSlot = this.addSlot(new IngredientsSlot(potionBrewing, container, 3, 79, 17));
        this.addSlot(new FuelSlot(container, 4, 17, 17));
        this.addDataSlots(containerData);
        for (n2 = 0; n2 < 3; ++n2) {
            for (int i = 0; i < 9; ++i) {
                this.addSlot(new Slot(inventory, i + n2 * 9 + 9, 8 + i * 18, 84 + n2 * 18));
            }
        }
        for (n2 = 0; n2 < 9; ++n2) {
            this.addSlot(new Slot(inventory, n2, 8 + n2 * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.brewingStand.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n >= 0 && n <= 2 || n == 3 || n == 4) {
                if (!this.moveItemStackTo(itemStack2, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (FuelSlot.mayPlaceItem(itemStack) ? this.moveItemStackTo(itemStack2, 4, 5, false) || this.ingredientSlot.mayPlace(itemStack2) && !this.moveItemStackTo(itemStack2, 3, 4, false) : (this.ingredientSlot.mayPlace(itemStack2) ? !this.moveItemStackTo(itemStack2, 3, 4, false) : (PotionSlot.mayPlaceItem(itemStack) ? !this.moveItemStackTo(itemStack2, 0, 3, false) : (n >= 5 && n < 32 ? !this.moveItemStackTo(itemStack2, 32, 41, false) : (n >= 32 && n < 41 ? !this.moveItemStackTo(itemStack2, 5, 32, false) : !this.moveItemStackTo(itemStack2, 5, 41, false)))))) {
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
            slot.onTake(player, itemStack);
        }
        return itemStack;
    }

    public int getFuel() {
        return this.brewingStandData.get(1);
    }

    public int getBrewingTicks() {
        return this.brewingStandData.get(0);
    }

    static class PotionSlot
    extends Slot {
        public PotionSlot(Container container, int n, int n2, int n3) {
            super(container, n, n2, n3);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return PotionSlot.mayPlaceItem(itemStack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void onTake(Player player, ItemStack itemStack) {
            Optional<Holder<Potion>> optional = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
            if (optional.isPresent() && player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                CriteriaTriggers.BREWED_POTION.trigger(serverPlayer, optional.get());
            }
            super.onTake(player, itemStack);
        }

        public static boolean mayPlaceItem(ItemStack itemStack) {
            return itemStack.is(Items.POTION) || itemStack.is(Items.SPLASH_POTION) || itemStack.is(Items.LINGERING_POTION) || itemStack.is(Items.GLASS_BOTTLE);
        }
    }

    static class IngredientsSlot
    extends Slot {
        private final PotionBrewing potionBrewing;

        public IngredientsSlot(PotionBrewing potionBrewing, Container container, int n, int n2, int n3) {
            super(container, n, n2, n3);
            this.potionBrewing = potionBrewing;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return this.potionBrewing.isIngredient(itemStack);
        }
    }

    static class FuelSlot
    extends Slot {
        public FuelSlot(Container container, int n, int n2, int n3) {
            super(container, n, n2, n3);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return FuelSlot.mayPlaceItem(itemStack);
        }

        public static boolean mayPlaceItem(ItemStack itemStack) {
            return itemStack.is(Items.BLAZE_POWDER);
        }
    }
}

