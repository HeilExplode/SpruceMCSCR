/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Hash$Strategy
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

public class ItemStackLinkedSet {
    private static final Hash.Strategy<? super ItemStack> TYPE_AND_TAG = new Hash.Strategy<ItemStack>(){

        public int hashCode(@Nullable ItemStack itemStack) {
            return ItemStack.hashItemAndComponents(itemStack);
        }

        public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
            return itemStack == itemStack2 || itemStack != null && itemStack2 != null && itemStack.isEmpty() == itemStack2.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack2);
        }

        public /* synthetic */ boolean equals(@Nullable Object object, @Nullable Object object2) {
            return this.equals((ItemStack)object, (ItemStack)object2);
        }

        public /* synthetic */ int hashCode(@Nullable Object object) {
            return this.hashCode((ItemStack)object);
        }
    };

    public static Set<ItemStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet(TYPE_AND_TAG);
    }
}

