/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public interface Equipable {
    public EquipmentSlot getEquipmentSlot();

    default public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    default public InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);
        if (!player.canUseSlot(equipmentSlot)) {
            return InteractionResultHolder.pass(itemStack);
        }
        ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
        if (EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && !player.isCreative() || ItemStack.matches(itemStack, itemStack2)) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (!level.isClientSide()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        ItemStack itemStack3 = itemStack2.isEmpty() ? itemStack : itemStack2.copyAndClear();
        ItemStack itemStack4 = player.isCreative() ? itemStack.copy() : itemStack.copyAndClear();
        player.setItemSlot(equipmentSlot, itemStack4);
        return InteractionResultHolder.sidedSuccess(itemStack3, level.isClientSide());
    }

    @Nullable
    public static Equipable get(ItemStack itemStack) {
        BlockItem blockItem;
        Object object = itemStack.getItem();
        if (object instanceof Equipable) {
            Equipable equipable = (Equipable)object;
            return equipable;
        }
        FeatureElement featureElement = itemStack.getItem();
        if (featureElement instanceof BlockItem && (featureElement = (blockItem = (BlockItem)featureElement).getBlock()) instanceof Equipable) {
            object = (Equipable)((Object)featureElement);
            return object;
        }
        return null;
    }
}

