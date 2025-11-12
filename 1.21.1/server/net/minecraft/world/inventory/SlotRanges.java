/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntLists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.SlotRange;

public class SlotRanges {
    private static final List<SlotRange> SLOTS = Util.make(new ArrayList(), arrayList -> {
        SlotRanges.addSingleSlot(arrayList, "contents", 0);
        SlotRanges.addSlotRange(arrayList, "container.", 0, 54);
        SlotRanges.addSlotRange(arrayList, "hotbar.", 0, 9);
        SlotRanges.addSlotRange(arrayList, "inventory.", 9, 27);
        SlotRanges.addSlotRange(arrayList, "enderchest.", 200, 27);
        SlotRanges.addSlotRange(arrayList, "villager.", 300, 8);
        SlotRanges.addSlotRange(arrayList, "horse.", 500, 15);
        int n = EquipmentSlot.MAINHAND.getIndex(98);
        int n2 = EquipmentSlot.OFFHAND.getIndex(98);
        SlotRanges.addSingleSlot(arrayList, "weapon", n);
        SlotRanges.addSingleSlot(arrayList, "weapon.mainhand", n);
        SlotRanges.addSingleSlot(arrayList, "weapon.offhand", n2);
        SlotRanges.addSlots(arrayList, "weapon.*", n, n2);
        n = EquipmentSlot.HEAD.getIndex(100);
        n2 = EquipmentSlot.CHEST.getIndex(100);
        int n3 = EquipmentSlot.LEGS.getIndex(100);
        int n4 = EquipmentSlot.FEET.getIndex(100);
        int n5 = EquipmentSlot.BODY.getIndex(105);
        SlotRanges.addSingleSlot(arrayList, "armor.head", n);
        SlotRanges.addSingleSlot(arrayList, "armor.chest", n2);
        SlotRanges.addSingleSlot(arrayList, "armor.legs", n3);
        SlotRanges.addSingleSlot(arrayList, "armor.feet", n4);
        SlotRanges.addSingleSlot(arrayList, "armor.body", n5);
        SlotRanges.addSlots(arrayList, "armor.*", n, n2, n3, n4, n5);
        SlotRanges.addSingleSlot(arrayList, "horse.saddle", 400);
        SlotRanges.addSingleSlot(arrayList, "horse.chest", 499);
        SlotRanges.addSingleSlot(arrayList, "player.cursor", 499);
        SlotRanges.addSlotRange(arrayList, "player.crafting.", 500, 4);
    });
    public static final Codec<SlotRange> CODEC = StringRepresentable.fromValues(() -> SLOTS.toArray(new SlotRange[0]));
    private static final Function<String, SlotRange> NAME_LOOKUP = StringRepresentable.createNameLookup((StringRepresentable[])SLOTS.toArray(new SlotRange[0]), string -> string);

    private static SlotRange create(String string, int n) {
        return SlotRange.of(string, IntLists.singleton((int)n));
    }

    private static SlotRange create(String string, IntList intList) {
        return SlotRange.of(string, IntLists.unmodifiable((IntList)intList));
    }

    private static SlotRange create(String string, int ... nArray) {
        return SlotRange.of(string, IntList.of((int[])nArray));
    }

    private static void addSingleSlot(List<SlotRange> list, String string, int n) {
        list.add(SlotRanges.create(string, n));
    }

    private static void addSlotRange(List<SlotRange> list, String string, int n, int n2) {
        IntArrayList intArrayList = new IntArrayList(n2);
        for (int i = 0; i < n2; ++i) {
            int n3 = n + i;
            list.add(SlotRanges.create(string + i, n3));
            intArrayList.add(n3);
        }
        list.add(SlotRanges.create(string + "*", (IntList)intArrayList));
    }

    private static void addSlots(List<SlotRange> list, String string, int ... nArray) {
        list.add(SlotRanges.create(string, nArray));
    }

    @Nullable
    public static SlotRange nameToIds(String string) {
        return NAME_LOOKUP.apply(string);
    }

    public static Stream<String> allNames() {
        return SLOTS.stream().map(StringRepresentable::getSerializedName);
    }

    public static Stream<String> singleSlotNames() {
        return SLOTS.stream().filter(slotRange -> slotRange.size() == 1).map(StringRepresentable::getSerializedName);
    }
}

