/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BookCloningRecipe
extends CustomRecipe {
    public BookCloningRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        int n = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack2 = craftingInput.getItem(i);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.is(Items.WRITTEN_BOOK)) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.is(Items.WRITABLE_BOOK)) {
                ++n;
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && n > 0;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        Object object;
        int n = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInput.size(); ++i) {
            object = craftingInput.getItem(i);
            if (((ItemStack)object).isEmpty()) continue;
            if (((ItemStack)object).is(Items.WRITTEN_BOOK)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = object;
                continue;
            }
            if (((ItemStack)object).is(Items.WRITABLE_BOOK)) {
                ++n;
                continue;
            }
            return ItemStack.EMPTY;
        }
        WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (itemStack.isEmpty() || n < 1 || writtenBookContent == null) {
            return ItemStack.EMPTY;
        }
        object = writtenBookContent.tryCraftCopy();
        if (object == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack2 = itemStack.copyWithCount(n);
        itemStack2.set(DataComponents.WRITTEN_BOOK_CONTENT, object);
        return itemStack2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.getItem().hasCraftingRemainingItem()) {
                nonNullList.set(i, new ItemStack(itemStack.getItem().getCraftingRemainingItem()));
                continue;
            }
            if (!(itemStack.getItem() instanceof WrittenBookItem)) continue;
            nonNullList.set(i, itemStack.copyWithCount(1));
            break;
        }
        return nonNullList;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n >= 3 && n2 >= 3;
    }
}

