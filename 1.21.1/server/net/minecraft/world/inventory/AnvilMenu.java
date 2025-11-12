/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.inventory;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class AnvilMenu
extends ItemCombinerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_COST = false;
    public static final int MAX_NAME_LENGTH = 50;
    private int repairItemCountCost;
    @Nullable
    private String itemName;
    private final DataSlot cost = DataSlot.standalone();
    private static final int COST_FAIL = 0;
    private static final int COST_BASE = 1;
    private static final int COST_ADDED_BASE = 1;
    private static final int COST_REPAIR_MATERIAL = 1;
    private static final int COST_REPAIR_SACRIFICE = 2;
    private static final int COST_INCOMPATIBLE_PENALTY = 1;
    private static final int COST_RENAME = 1;
    private static final int INPUT_SLOT_X_PLACEMENT = 27;
    private static final int ADDITIONAL_SLOT_X_PLACEMENT = 76;
    private static final int RESULT_SLOT_X_PLACEMENT = 134;
    private static final int SLOT_Y_PLACEMENT = 47;

    public AnvilMenu(int n, Inventory inventory) {
        this(n, inventory, ContainerLevelAccess.NULL);
    }

    public AnvilMenu(int n, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.ANVIL, n, inventory, containerLevelAccess);
        this.addDataSlot(this.cost);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 27, 47, itemStack -> true).withSlot(1, 76, 47, itemStack -> true).withResultSlot(2, 134, 47).build();
    }

    @Override
    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(BlockTags.ANVIL);
    }

    @Override
    protected boolean mayPickup(Player player, boolean bl) {
        return (player.hasInfiniteMaterials() || player.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
    }

    @Override
    protected void onTake(Player player, ItemStack itemStack) {
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-this.cost.get());
        }
        this.inputSlots.setItem(0, ItemStack.EMPTY);
        if (this.repairItemCountCost > 0) {
            ItemStack itemStack2 = this.inputSlots.getItem(1);
            if (!itemStack2.isEmpty() && itemStack2.getCount() > this.repairItemCountCost) {
                itemStack2.shrink(this.repairItemCountCost);
                this.inputSlots.setItem(1, itemStack2);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
        }
        this.cost.set(0);
        this.access.execute((level, blockPos) -> {
            BlockState blockState = level.getBlockState((BlockPos)blockPos);
            if (!player.hasInfiniteMaterials() && blockState.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12f) {
                BlockState blockState2 = AnvilBlock.damage(blockState);
                if (blockState2 == null) {
                    level.removeBlock((BlockPos)blockPos, false);
                    level.levelEvent(1029, (BlockPos)blockPos, 0);
                } else {
                    level.setBlock((BlockPos)blockPos, blockState2, 2);
                    level.levelEvent(1030, (BlockPos)blockPos, 0);
                }
            } else {
                level.levelEvent(1030, (BlockPos)blockPos, 0);
            }
        });
    }

    @Override
    public void createResult() {
        int n;
        int n2;
        ItemStack itemStack = this.inputSlots.getItem(0);
        this.cost.set(1);
        int n3 = 0;
        long l = 0L;
        int n4 = 0;
        if (itemStack.isEmpty() || !EnchantmentHelper.canStoreEnchantments(itemStack)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            return;
        }
        ItemStack itemStack2 = itemStack.copy();
        ItemStack itemStack3 = this.inputSlots.getItem(1);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(itemStack2));
        l += (long)itemStack.getOrDefault(DataComponents.REPAIR_COST, 0).intValue() + (long)itemStack3.getOrDefault(DataComponents.REPAIR_COST, 0).intValue();
        this.repairItemCountCost = 0;
        if (!itemStack3.isEmpty()) {
            n2 = itemStack3.has(DataComponents.STORED_ENCHANTMENTS);
            if (itemStack2.isDamageableItem() && itemStack2.getItem().isValidRepairItem(itemStack, itemStack3)) {
                int n5;
                n = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
                if (n <= 0) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }
                for (n5 = 0; n > 0 && n5 < itemStack3.getCount(); ++n5) {
                    int n6 = itemStack2.getDamageValue() - n;
                    itemStack2.setDamageValue(n6);
                    ++n3;
                    n = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
                }
                this.repairItemCountCost = n5;
            } else {
                int n7;
                int n8;
                if (!(n2 != 0 || itemStack2.is(itemStack3.getItem()) && itemStack2.isDamageableItem())) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }
                if (itemStack2.isDamageableItem() && n2 == 0) {
                    int n9 = itemStack.getMaxDamage() - itemStack.getDamageValue();
                    n8 = itemStack3.getMaxDamage() - itemStack3.getDamageValue();
                    n7 = n8 + itemStack2.getMaxDamage() * 12 / 100;
                    int n10 = n9 + n7;
                    int n11 = itemStack2.getMaxDamage() - n10;
                    if (n11 < 0) {
                        n11 = 0;
                    }
                    if (n11 < itemStack2.getDamageValue()) {
                        itemStack2.setDamageValue(n11);
                        n3 += 2;
                    }
                }
                ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack3);
                n8 = 0;
                n7 = 0;
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                    int n12;
                    Holder holder = (Holder)entry.getKey();
                    int n13 = mutable.getLevel(holder);
                    n12 = n13 == (n12 = entry.getIntValue()) ? n12 + 1 : Math.max(n12, n13);
                    Enchantment enchantment = (Enchantment)holder.value();
                    boolean bl = enchantment.canEnchant(itemStack);
                    if (this.player.getAbilities().instabuild || itemStack.is(Items.ENCHANTED_BOOK)) {
                        bl = true;
                    }
                    for (Holder<Enchantment> holder2 : mutable.keySet()) {
                        if (holder2.equals(holder) || Enchantment.areCompatible(holder, holder2)) continue;
                        bl = false;
                        ++n3;
                    }
                    if (!bl) {
                        n7 = 1;
                        continue;
                    }
                    n8 = 1;
                    if (n12 > enchantment.getMaxLevel()) {
                        n12 = enchantment.getMaxLevel();
                    }
                    mutable.set(holder, n12);
                    int n14 = enchantment.getAnvilCost();
                    if (n2 != 0) {
                        n14 = Math.max(1, n14 / 2);
                    }
                    n3 += n14 * n12;
                    if (itemStack.getCount() <= 1) continue;
                    n3 = 40;
                }
                if (n7 != 0 && n8 == 0) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }
            }
        }
        if (this.itemName == null || StringUtil.isBlank(this.itemName)) {
            if (itemStack.has(DataComponents.CUSTOM_NAME)) {
                n4 = 1;
                n3 += n4;
                itemStack2.remove(DataComponents.CUSTOM_NAME);
            }
        } else if (!this.itemName.equals(itemStack.getHoverName().getString())) {
            n4 = 1;
            n3 += n4;
            itemStack2.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
        }
        n2 = (int)Mth.clamp(l + (long)n3, 0L, Integer.MAX_VALUE);
        this.cost.set(n2);
        if (n3 <= 0) {
            itemStack2 = ItemStack.EMPTY;
        }
        if (n4 == n3 && n4 > 0 && this.cost.get() >= 40) {
            this.cost.set(39);
        }
        if (this.cost.get() >= 40 && !this.player.getAbilities().instabuild) {
            itemStack2 = ItemStack.EMPTY;
        }
        if (!itemStack2.isEmpty()) {
            n = itemStack2.getOrDefault(DataComponents.REPAIR_COST, 0);
            if (n < itemStack3.getOrDefault(DataComponents.REPAIR_COST, 0)) {
                n = itemStack3.getOrDefault(DataComponents.REPAIR_COST, 0);
            }
            if (n4 != n3 || n4 == 0) {
                n = AnvilMenu.calculateIncreasedRepairCost(n);
            }
            itemStack2.set(DataComponents.REPAIR_COST, n);
            EnchantmentHelper.setEnchantments(itemStack2, mutable.toImmutable());
        }
        this.resultSlots.setItem(0, itemStack2);
        this.broadcastChanges();
    }

    public static int calculateIncreasedRepairCost(int n) {
        return (int)Math.min((long)n * 2L + 1L, Integer.MAX_VALUE);
    }

    public boolean setItemName(String string) {
        String string2 = AnvilMenu.validateName(string);
        if (string2 == null || string2.equals(this.itemName)) {
            return false;
        }
        this.itemName = string2;
        if (this.getSlot(2).hasItem()) {
            ItemStack itemStack = this.getSlot(2).getItem();
            if (StringUtil.isBlank(string2)) {
                itemStack.remove(DataComponents.CUSTOM_NAME);
            } else {
                itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(string2));
            }
        }
        this.createResult();
        return true;
    }

    @Nullable
    private static String validateName(String string) {
        String string2 = StringUtil.filterText(string);
        if (string2.length() <= 50) {
            return string2;
        }
        return null;
    }

    public int getCost() {
        return this.cost.get();
    }
}

