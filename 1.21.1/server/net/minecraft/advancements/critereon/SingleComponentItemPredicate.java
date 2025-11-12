/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.critereon;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface SingleComponentItemPredicate<T>
extends ItemSubPredicate {
    @Override
    default public boolean matches(ItemStack itemStack) {
        T t = itemStack.get(this.componentType());
        return t != null && this.matches(itemStack, t);
    }

    public DataComponentType<T> componentType();

    public boolean matches(ItemStack var1, T var2);
}

