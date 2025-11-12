/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.component;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public final class ItemContainerContents {
    private static final int NO_SLOT = -1;
    private static final int MAX_SIZE = 256;
    public static final ItemContainerContents EMPTY = new ItemContainerContents(NonNullList.create());
    public static final Codec<ItemContainerContents> CODEC = Slot.CODEC.sizeLimitedListOf(256).xmap(ItemContainerContents::fromSlots, ItemContainerContents::asSlots);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemContainerContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(256)).map(ItemContainerContents::new, itemContainerContents -> itemContainerContents.items);
    private final NonNullList<ItemStack> items;
    private final int hashCode;

    private ItemContainerContents(NonNullList<ItemStack> nonNullList) {
        if (nonNullList.size() > 256) {
            throw new IllegalArgumentException("Got " + nonNullList.size() + " items, but maximum is 256");
        }
        this.items = nonNullList;
        this.hashCode = ItemStack.hashStackList(nonNullList);
    }

    private ItemContainerContents(int n) {
        this(NonNullList.withSize(n, ItemStack.EMPTY));
    }

    private ItemContainerContents(List<ItemStack> list) {
        this(list.size());
        for (int i = 0; i < list.size(); ++i) {
            this.items.set(i, list.get(i));
        }
    }

    private static ItemContainerContents fromSlots(List<Slot> list) {
        OptionalInt optionalInt = list.stream().mapToInt(Slot::index).max();
        if (optionalInt.isEmpty()) {
            return EMPTY;
        }
        ItemContainerContents itemContainerContents = new ItemContainerContents(optionalInt.getAsInt() + 1);
        for (Slot slot : list) {
            itemContainerContents.items.set(slot.index(), slot.item());
        }
        return itemContainerContents;
    }

    public static ItemContainerContents fromItems(List<ItemStack> list) {
        int n = ItemContainerContents.findLastNonEmptySlot(list);
        if (n == -1) {
            return EMPTY;
        }
        ItemContainerContents itemContainerContents = new ItemContainerContents(n + 1);
        for (int i = 0; i <= n; ++i) {
            itemContainerContents.items.set(i, list.get(i).copy());
        }
        return itemContainerContents;
    }

    private static int findLastNonEmptySlot(List<ItemStack> list) {
        for (int i = list.size() - 1; i >= 0; --i) {
            if (list.get(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private List<Slot> asSlots() {
        ArrayList<Slot> arrayList = new ArrayList<Slot>();
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.items.get(i);
            if (itemStack.isEmpty()) continue;
            arrayList.add(new Slot(i, itemStack));
        }
        return arrayList;
    }

    public void copyInto(NonNullList<ItemStack> nonNullList) {
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
            nonNullList.set(i, itemStack.copy());
        }
    }

    public ItemStack copyOne() {
        return this.items.isEmpty() ? ItemStack.EMPTY : this.items.get(0).copy();
    }

    public Stream<ItemStack> stream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Stream<ItemStack> nonEmptyStream() {
        return this.items.stream().filter(itemStack -> !itemStack.isEmpty()).map(ItemStack::copy);
    }

    public Iterable<ItemStack> nonEmptyItems() {
        return Iterables.filter(this.items, itemStack -> !itemStack.isEmpty());
    }

    public Iterable<ItemStack> nonEmptyItemsCopy() {
        return Iterables.transform(this.nonEmptyItems(), ItemStack::copy);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ItemContainerContents)) return false;
        ItemContainerContents itemContainerContents = (ItemContainerContents)object;
        if (!ItemStack.listMatches(this.items, itemContainerContents.items)) return false;
        return true;
    }

    public int hashCode() {
        return this.hashCode;
    }

    record Slot(int index, ItemStack item) {
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)255).fieldOf("slot").forGetter(Slot::index), (App)ItemStack.CODEC.fieldOf("item").forGetter(Slot::item)).apply((Applicative)instance, Slot::new));
    }
}

