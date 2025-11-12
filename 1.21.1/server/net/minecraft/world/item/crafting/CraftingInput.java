/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class CraftingInput
implements RecipeInput {
    public static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());
    private final int width;
    private final int height;
    private final List<ItemStack> items;
    private final StackedContents stackedContents = new StackedContents();
    private final int ingredientCount;

    private CraftingInput(int n, int n2, List<ItemStack> list) {
        this.width = n;
        this.height = n2;
        this.items = list;
        int n3 = 0;
        for (ItemStack itemStack : list) {
            if (itemStack.isEmpty()) continue;
            ++n3;
            this.stackedContents.accountStack(itemStack, 1);
        }
        this.ingredientCount = n3;
    }

    public static CraftingInput of(int n, int n2, List<ItemStack> list) {
        return CraftingInput.ofPositioned(n, n2, list).input();
    }

    public static Positioned ofPositioned(int n, int n2, List<ItemStack> list) {
        int n3;
        int n4;
        if (n == 0 || n2 == 0) {
            return Positioned.EMPTY;
        }
        int n5 = n - 1;
        int n6 = 0;
        int n7 = n2 - 1;
        int n8 = 0;
        for (n4 = 0; n4 < n2; ++n4) {
            n3 = 1;
            for (int i = 0; i < n; ++i) {
                ItemStack itemStack = list.get(i + n4 * n);
                if (itemStack.isEmpty()) continue;
                n5 = Math.min(n5, i);
                n6 = Math.max(n6, i);
                n3 = 0;
            }
            if (n3 != 0) continue;
            n7 = Math.min(n7, n4);
            n8 = Math.max(n8, n4);
        }
        n4 = n6 - n5 + 1;
        n3 = n8 - n7 + 1;
        if (n4 <= 0 || n3 <= 0) {
            return Positioned.EMPTY;
        }
        if (n4 == n && n3 == n2) {
            return new Positioned(new CraftingInput(n, n2, list), n5, n7);
        }
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>(n4 * n3);
        for (int i = 0; i < n3; ++i) {
            for (int j = 0; j < n4; ++j) {
                int n9 = j + n5 + (i + n7) * n;
                arrayList.add(list.get(n9));
            }
        }
        return new Positioned(new CraftingInput(n4, n3, arrayList), n5, n7);
    }

    @Override
    public ItemStack getItem(int n) {
        return this.items.get(n);
    }

    public ItemStack getItem(int n, int n2) {
        return this.items.get(n + n2 * this.width);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.ingredientCount == 0;
    }

    public StackedContents stackedContents() {
        return this.stackedContents;
    }

    public List<ItemStack> items() {
        return this.items;
    }

    public int ingredientCount() {
        return this.ingredientCount;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CraftingInput) {
            CraftingInput craftingInput = (CraftingInput)object;
            return this.width == craftingInput.width && this.height == craftingInput.height && this.ingredientCount == craftingInput.ingredientCount && ItemStack.listMatches(this.items, craftingInput.items);
        }
        return false;
    }

    public int hashCode() {
        int n = ItemStack.hashStackList(this.items);
        n = 31 * n + this.width;
        n = 31 * n + this.height;
        return n;
    }

    public record Positioned(CraftingInput input, int left, int top) {
        public static final Positioned EMPTY = new Positioned(EMPTY, 0, 0);
    }
}

