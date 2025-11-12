/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 */
package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GrindstoneMenu
extends AbstractContainerMenu {
    public static final int MAX_NAME_LENGTH = 35;
    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private final Container resultSlots = new ResultContainer();
    final Container repairSlots = new SimpleContainer(2){

        @Override
        public void setChanged() {
            super.setChanged();
            GrindstoneMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;

    public GrindstoneMenu(int n, Inventory inventory) {
        this(n, inventory, ContainerLevelAccess.NULL);
    }

    public GrindstoneMenu(int n, Inventory inventory, final ContainerLevelAccess containerLevelAccess) {
        super(MenuType.GRINDSTONE, n);
        int n2;
        this.access = containerLevelAccess;
        this.addSlot(new Slot(this, this.repairSlots, 0, 49, 19){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments(itemStack);
            }
        });
        this.addSlot(new Slot(this, this.repairSlots, 1, 49, 40){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments(itemStack);
            }
        });
        this.addSlot(new Slot(this.resultSlots, 2, 129, 34){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                containerLevelAccess.execute((level, blockPos) -> {
                    if (level instanceof ServerLevel) {
                        ExperienceOrb.award((ServerLevel)level, Vec3.atCenterOf(blockPos), this.getExperienceAmount((Level)level));
                    }
                    level.levelEvent(1042, (BlockPos)blockPos, 0);
                });
                GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
                GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
            }

            private int getExperienceAmount(Level level) {
                int n = 0;
                n += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0));
                if ((n += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1))) > 0) {
                    int n2 = (int)Math.ceil((double)n / 2.0);
                    return n2 + level.random.nextInt(n2);
                }
                return 0;
            }

            private int getExperienceFromItem(ItemStack itemStack) {
                int n = 0;
                ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                    Holder holder = (Holder)entry.getKey();
                    int n2 = entry.getIntValue();
                    if (holder.is(EnchantmentTags.CURSE)) continue;
                    n += ((Enchantment)holder.value()).getMinCost(n2);
                }
                return n;
            }
        });
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
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.repairSlots) {
            this.createResult();
        }
    }

    private void createResult() {
        this.resultSlots.setItem(0, this.computeResult(this.repairSlots.getItem(0), this.repairSlots.getItem(1)));
        this.broadcastChanges();
    }

    private ItemStack computeResult(ItemStack itemStack, ItemStack itemStack2) {
        boolean bl;
        boolean bl2;
        boolean bl3 = bl2 = !itemStack.isEmpty() || !itemStack2.isEmpty();
        if (!bl2) {
            return ItemStack.EMPTY;
        }
        if (itemStack.getCount() > 1 || itemStack2.getCount() > 1) {
            return ItemStack.EMPTY;
        }
        boolean bl4 = bl = !itemStack.isEmpty() && !itemStack2.isEmpty();
        if (!bl) {
            ItemStack itemStack3;
            ItemStack itemStack4 = itemStack3 = !itemStack.isEmpty() ? itemStack : itemStack2;
            if (!EnchantmentHelper.hasAnyEnchantments(itemStack3)) {
                return ItemStack.EMPTY;
            }
            return this.removeNonCursesFrom(itemStack3.copy());
        }
        return this.mergeItems(itemStack, itemStack2);
    }

    private ItemStack mergeItems(ItemStack itemStack, ItemStack itemStack2) {
        ItemStack itemStack3;
        if (!itemStack.is(itemStack2.getItem())) {
            return ItemStack.EMPTY;
        }
        int n = Math.max(itemStack.getMaxDamage(), itemStack2.getMaxDamage());
        int n2 = itemStack.getMaxDamage() - itemStack.getDamageValue();
        int n3 = itemStack2.getMaxDamage() - itemStack2.getDamageValue();
        int n4 = n2 + n3 + n * 5 / 100;
        int n5 = 1;
        if (!itemStack.isDamageableItem()) {
            if (itemStack.getMaxStackSize() < 2 || !ItemStack.matches(itemStack, itemStack2)) {
                return ItemStack.EMPTY;
            }
            n5 = 2;
        }
        if ((itemStack3 = itemStack.copyWithCount(n5)).isDamageableItem()) {
            itemStack3.set(DataComponents.MAX_DAMAGE, n);
            itemStack3.setDamageValue(Math.max(n - n4, 0));
        }
        this.mergeEnchantsFrom(itemStack3, itemStack2);
        return this.removeNonCursesFrom(itemStack3);
    }

    private void mergeEnchantsFrom(ItemStack itemStack, ItemStack itemStack2) {
        EnchantmentHelper.updateEnchantments(itemStack, mutable -> {
            ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack2);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                Holder holder = (Holder)entry.getKey();
                if (holder.is(EnchantmentTags.CURSE) && mutable.getLevel(holder) != 0) continue;
                mutable.upgrade(holder, entry.getIntValue());
            }
        });
    }

    private ItemStack removeNonCursesFrom(ItemStack itemStack) {
        ItemEnchantments itemEnchantments = EnchantmentHelper.updateEnchantments(itemStack, mutable -> mutable.removeIf(holder -> !holder.is(EnchantmentTags.CURSE)));
        if (itemStack.is(Items.ENCHANTED_BOOK) && itemEnchantments.isEmpty()) {
            itemStack = itemStack.transmuteCopy(Items.BOOK);
        }
        int n = 0;
        for (int i = 0; i < itemEnchantments.size(); ++i) {
            n = AnvilMenu.calculateIncreasedRepairCost(n);
        }
        itemStack.set(DataComponents.REPAIR_COST, n);
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.repairSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return GrindstoneMenu.stillValid(this.access, player, Blocks.GRINDSTONE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            ItemStack itemStack3 = this.repairSlots.getItem(0);
            ItemStack itemStack4 = this.repairSlots.getItem(1);
            if (n == 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (n == 0 || n == 1 ? !this.moveItemStackTo(itemStack2, 3, 39, false) : (itemStack3.isEmpty() || itemStack4.isEmpty() ? !this.moveItemStackTo(itemStack2, 0, 2, false) : (n >= 3 && n < 30 ? !this.moveItemStackTo(itemStack2, 30, 39, false) : n >= 30 && n < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false)))) {
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
}

