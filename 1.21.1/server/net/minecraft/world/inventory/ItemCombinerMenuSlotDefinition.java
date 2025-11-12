/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
    private final List<SlotDefinition> slots;
    private final SlotDefinition resultSlot;

    ItemCombinerMenuSlotDefinition(List<SlotDefinition> list, SlotDefinition slotDefinition) {
        if (list.isEmpty() || slotDefinition.equals(SlotDefinition.EMPTY)) {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
        this.slots = list;
        this.resultSlot = slotDefinition;
    }

    public static Builder create() {
        return new Builder();
    }

    public boolean hasSlot(int n) {
        return this.slots.size() >= n;
    }

    public SlotDefinition getSlot(int n) {
        return this.slots.get(n);
    }

    public SlotDefinition getResultSlot() {
        return this.resultSlot;
    }

    public List<SlotDefinition> getSlots() {
        return this.slots;
    }

    public int getNumOfInputSlots() {
        return this.slots.size();
    }

    public int getResultSlotIndex() {
        return this.getNumOfInputSlots();
    }

    public List<Integer> getInputSlotIndexes() {
        return this.slots.stream().map(SlotDefinition::slotIndex).collect(Collectors.toList());
    }

    public record SlotDefinition(int slotIndex, int x, int y, Predicate<ItemStack> mayPlace) {
        static final SlotDefinition EMPTY = new SlotDefinition(0, 0, 0, itemStack -> true);
    }

    public static class Builder {
        private final List<SlotDefinition> slots = new ArrayList<SlotDefinition>();
        private SlotDefinition resultSlot = SlotDefinition.EMPTY;

        public Builder withSlot(int n, int n2, int n3, Predicate<ItemStack> predicate) {
            this.slots.add(new SlotDefinition(n, n2, n3, predicate));
            return this;
        }

        public Builder withResultSlot(int n, int n2, int n3) {
            this.resultSlot = new SlotDefinition(n, n2, n3, itemStack -> false);
            return this;
        }

        public ItemCombinerMenuSlotDefinition build() {
            return new ItemCombinerMenuSlotDefinition(this.slots, this.resultSlot);
        }
    }
}

