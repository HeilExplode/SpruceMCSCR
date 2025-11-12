/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;

public interface EquipmentUser {
    public void setItemSlot(EquipmentSlot var1, ItemStack var2);

    public ItemStack getItemBySlot(EquipmentSlot var1);

    public void setDropChance(EquipmentSlot var1, float var2);

    default public void equip(EquipmentTable equipmentTable, LootParams lootParams) {
        this.equip(equipmentTable.lootTable(), lootParams, equipmentTable.slotDropChances());
    }

    default public void equip(ResourceKey<LootTable> resourceKey, LootParams lootParams, Map<EquipmentSlot, Float> map) {
        this.equip(resourceKey, lootParams, 0L, map);
    }

    default public void equip(ResourceKey<LootTable> resourceKey, LootParams lootParams, long l, Map<EquipmentSlot, Float> map) {
        if (resourceKey.equals(BuiltInLootTables.EMPTY)) {
            return;
        }
        LootTable lootTable = lootParams.getLevel().getServer().reloadableRegistries().getLootTable(resourceKey);
        if (lootTable == LootTable.EMPTY) {
            return;
        }
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams, l);
        ArrayList<EquipmentSlot> arrayList = new ArrayList<EquipmentSlot>();
        for (ItemStack itemStack : objectArrayList) {
            EquipmentSlot equipmentSlot = this.resolveSlot(itemStack, arrayList);
            if (equipmentSlot == null) continue;
            ItemStack itemStack2 = equipmentSlot.limit(itemStack);
            this.setItemSlot(equipmentSlot, itemStack2);
            Float f = map.get(equipmentSlot);
            if (f != null) {
                this.setDropChance(equipmentSlot, f.floatValue());
            }
            arrayList.add(equipmentSlot);
        }
    }

    @Nullable
    default public EquipmentSlot resolveSlot(ItemStack itemStack, List<EquipmentSlot> list) {
        if (itemStack.isEmpty()) {
            return null;
        }
        Equipable equipable = Equipable.get(itemStack);
        if (equipable != null) {
            EquipmentSlot equipmentSlot = equipable.getEquipmentSlot();
            if (!list.contains(equipmentSlot)) {
                return equipmentSlot;
            }
        } else if (!list.contains(EquipmentSlot.MAINHAND)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}

