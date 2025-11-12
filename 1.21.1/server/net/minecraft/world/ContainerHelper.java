/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ContainerHelper {
    public static final String TAG_ITEMS = "Items";

    public static ItemStack removeItem(List<ItemStack> list, int n, int n2) {
        if (n < 0 || n >= list.size() || list.get(n).isEmpty() || n2 <= 0) {
            return ItemStack.EMPTY;
        }
        return list.get(n).split(n2);
    }

    public static ItemStack takeItem(List<ItemStack> list, int n) {
        if (n < 0 || n >= list.size()) {
            return ItemStack.EMPTY;
        }
        return list.set(n, ItemStack.EMPTY);
    }

    public static CompoundTag saveAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList, HolderLookup.Provider provider) {
        return ContainerHelper.saveAllItems(compoundTag, nonNullList, true, provider);
    }

    public static CompoundTag saveAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList, boolean bl, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = nonNullList.get(i);
            if (itemStack.isEmpty()) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putByte("Slot", (byte)i);
            listTag.add(itemStack.save(provider, compoundTag2));
        }
        if (!listTag.isEmpty() || bl) {
            compoundTag.put(TAG_ITEMS, listTag);
        }
        return compoundTag;
    }

    public static void loadAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList, HolderLookup.Provider provider) {
        ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            int n = compoundTag2.getByte("Slot") & 0xFF;
            if (n < 0 || n >= nonNullList.size()) continue;
            nonNullList.set(n, ItemStack.parse(provider, compoundTag2).orElse(ItemStack.EMPTY));
        }
    }

    public static int clearOrCountMatchingItems(Container container, Predicate<ItemStack> predicate, int n, boolean bl) {
        int n2 = 0;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            int n3 = ContainerHelper.clearOrCountMatchingItems(itemStack, predicate, n - n2, bl);
            if (n3 > 0 && !bl && itemStack.isEmpty()) {
                container.setItem(i, ItemStack.EMPTY);
            }
            n2 += n3;
        }
        return n2;
    }

    public static int clearOrCountMatchingItems(ItemStack itemStack, Predicate<ItemStack> predicate, int n, boolean bl) {
        if (itemStack.isEmpty() || !predicate.test(itemStack)) {
            return 0;
        }
        if (bl) {
            return itemStack.getCount();
        }
        int n2 = n < 0 ? itemStack.getCount() : Math.min(n, itemStack.getCount());
        itemStack.shrink(n2);
        return n2;
    }
}

