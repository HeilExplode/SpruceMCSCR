/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
    public static final SlotAccess NULL = new SlotAccess(){

        @Override
        public ItemStack get() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean set(ItemStack itemStack) {
            return false;
        }
    };

    public static SlotAccess of(final Supplier<ItemStack> supplier, final Consumer<ItemStack> consumer) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return (ItemStack)supplier.get();
            }

            @Override
            public boolean set(ItemStack itemStack) {
                consumer.accept(itemStack);
                return true;
            }
        };
    }

    public static SlotAccess forContainer(final Container container, final int n, final Predicate<ItemStack> predicate) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return container.getItem(n);
            }

            @Override
            public boolean set(ItemStack itemStack) {
                if (!predicate.test(itemStack)) {
                    return false;
                }
                container.setItem(n, itemStack);
                return true;
            }
        };
    }

    public static SlotAccess forContainer(Container container, int n) {
        return SlotAccess.forContainer(container, n, itemStack -> true);
    }

    public static SlotAccess forEquipmentSlot(final LivingEntity livingEntity, final EquipmentSlot equipmentSlot, final Predicate<ItemStack> predicate) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return livingEntity.getItemBySlot(equipmentSlot);
            }

            @Override
            public boolean set(ItemStack itemStack) {
                if (!predicate.test(itemStack)) {
                    return false;
                }
                livingEntity.setItemSlot(equipmentSlot, itemStack);
                return true;
            }
        };
    }

    public static SlotAccess forEquipmentSlot(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        return SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot, itemStack -> true);
    }

    public ItemStack get();

    public boolean set(ItemStack var1);
}

