/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public record SlotsPredicate(Map<SlotRange, ItemPredicate> slots) {
    public static final Codec<SlotsPredicate> CODEC = Codec.unboundedMap(SlotRanges.CODEC, ItemPredicate.CODEC).xmap(SlotsPredicate::new, SlotsPredicate::slots);

    public boolean matches(Entity entity) {
        for (Map.Entry<SlotRange, ItemPredicate> entry : this.slots.entrySet()) {
            if (SlotsPredicate.matchSlots(entity, entry.getValue(), entry.getKey().slots())) continue;
            return false;
        }
        return true;
    }

    private static boolean matchSlots(Entity entity, ItemPredicate itemPredicate, IntList intList) {
        for (int i = 0; i < intList.size(); ++i) {
            int n = intList.getInt(i);
            SlotAccess slotAccess = entity.getSlot(n);
            if (!itemPredicate.test(slotAccess.get())) continue;
            return true;
        }
        return false;
    }
}

