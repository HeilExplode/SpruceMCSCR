/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.math.Fraction
 */
package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

public final class BundleContents
implements TooltipComponent {
    public static final BundleContents EMPTY = new BundleContents(List.of());
    public static final Codec<BundleContents> CODEC = ItemStack.CODEC.listOf().xmap(BundleContents::new, bundleContents -> bundleContents.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(BundleContents::new, bundleContents -> bundleContents.items);
    private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction((int)1, (int)16);
    private static final int NO_STACK_INDEX = -1;
    final List<ItemStack> items;
    final Fraction weight;

    BundleContents(List<ItemStack> list, Fraction fraction) {
        this.items = list;
        this.weight = fraction;
    }

    public BundleContents(List<ItemStack> list) {
        this(list, BundleContents.computeContentWeight(list));
    }

    private static Fraction computeContentWeight(List<ItemStack> list) {
        Fraction fraction = Fraction.ZERO;
        for (ItemStack itemStack : list) {
            fraction = fraction.add(BundleContents.getWeight(itemStack).multiplyBy(Fraction.getFraction((int)itemStack.getCount(), (int)1)));
        }
        return fraction;
    }

    static Fraction getWeight(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            return BUNDLE_IN_BUNDLE_WEIGHT.add(bundleContents.weight());
        }
        List list = itemStack.getOrDefault(DataComponents.BEES, List.of());
        if (!list.isEmpty()) {
            return Fraction.ONE;
        }
        return Fraction.getFraction((int)1, (int)itemStack.getMaxStackSize());
    }

    public ItemStack getItemUnsafe(int n) {
        return this.items.get(n);
    }

    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Iterable<ItemStack> items() {
        return this.items;
    }

    public Iterable<ItemStack> itemsCopy() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public int size() {
        return this.items.size();
    }

    public Fraction weight() {
        return this.weight;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BundleContents) {
            BundleContents bundleContents = (BundleContents)object;
            return this.weight.equals((Object)bundleContents.weight) && ItemStack.listMatches(this.items, bundleContents.items);
        }
        return false;
    }

    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    public String toString() {
        return "BundleContents" + String.valueOf(this.items);
    }

    public static class Mutable {
        private final List<ItemStack> items;
        private Fraction weight;

        public Mutable(BundleContents bundleContents) {
            this.items = new ArrayList<ItemStack>(bundleContents.items);
            this.weight = bundleContents.weight;
        }

        public Mutable clearItems() {
            this.items.clear();
            this.weight = Fraction.ZERO;
            return this;
        }

        private int findStackIndex(ItemStack itemStack) {
            if (!itemStack.isStackable()) {
                return -1;
            }
            for (int i = 0; i < this.items.size(); ++i) {
                if (!ItemStack.isSameItemSameComponents(this.items.get(i), itemStack)) continue;
                return i;
            }
            return -1;
        }

        private int getMaxAmountToAdd(ItemStack itemStack) {
            Fraction fraction = Fraction.ONE.subtract(this.weight);
            return Math.max(fraction.divideBy(BundleContents.getWeight(itemStack)).intValue(), 0);
        }

        public int tryInsert(ItemStack itemStack) {
            if (itemStack.isEmpty() || !itemStack.getItem().canFitInsideContainerItems()) {
                return 0;
            }
            int n = Math.min(itemStack.getCount(), this.getMaxAmountToAdd(itemStack));
            if (n == 0) {
                return 0;
            }
            this.weight = this.weight.add(BundleContents.getWeight(itemStack).multiplyBy(Fraction.getFraction((int)n, (int)1)));
            int n2 = this.findStackIndex(itemStack);
            if (n2 != -1) {
                ItemStack itemStack2 = this.items.remove(n2);
                ItemStack itemStack3 = itemStack2.copyWithCount(itemStack2.getCount() + n);
                itemStack.shrink(n);
                this.items.add(0, itemStack3);
            } else {
                this.items.add(0, itemStack.split(n));
            }
            return n;
        }

        public int tryTransfer(Slot slot, Player player) {
            ItemStack itemStack = slot.getItem();
            int n = this.getMaxAmountToAdd(itemStack);
            return this.tryInsert(slot.safeTake(itemStack.getCount(), n, player));
        }

        @Nullable
        public ItemStack removeOne() {
            if (this.items.isEmpty()) {
                return null;
            }
            ItemStack itemStack = this.items.remove(0).copy();
            this.weight = this.weight.subtract(BundleContents.getWeight(itemStack).multiplyBy(Fraction.getFraction((int)itemStack.getCount(), (int)1)));
            return itemStack;
        }

        public Fraction weight() {
            return this.weight;
        }

        public BundleContents toImmutable() {
            return new BundleContents(List.copyOf(this.items), this.weight);
        }
    }
}

