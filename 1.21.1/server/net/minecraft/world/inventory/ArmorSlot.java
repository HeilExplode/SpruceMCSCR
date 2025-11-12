/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

class ArmorSlot
extends Slot {
    private final LivingEntity owner;
    private final EquipmentSlot slot;
    @Nullable
    private final ResourceLocation emptyIcon;

    public ArmorSlot(Container container, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int n, int n2, int n3, @Nullable ResourceLocation resourceLocation) {
        super(container, n, n2, n3);
        this.owner = livingEntity;
        this.slot = equipmentSlot;
        this.emptyIcon = resourceLocation;
    }

    @Override
    public void setByPlayer(ItemStack itemStack, ItemStack itemStack2) {
        this.owner.onEquipItem(this.slot, itemStack2, itemStack);
        super.setByPlayer(itemStack, itemStack2);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return this.slot == this.owner.getEquipmentSlotForItem(itemStack);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack itemStack = this.getItem();
        if (!itemStack.isEmpty() && !player.isCreative() && EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        return super.mayPickup(player);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        if (this.emptyIcon != null) {
            return Pair.of((Object)InventoryMenu.BLOCK_ATLAS, (Object)this.emptyIcon);
        }
        return super.getNoItemIcon();
    }
}

