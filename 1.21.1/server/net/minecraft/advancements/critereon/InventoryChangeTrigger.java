/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack) {
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack2 = inventory.getItem(i);
            if (itemStack2.isEmpty()) {
                ++n2;
                continue;
            }
            ++n3;
            if (itemStack2.getCount() < itemStack2.getMaxStackSize()) continue;
            ++n;
        }
        this.trigger(serverPlayer, inventory, itemStack, n, n2, n3);
    }

    private void trigger(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack, int n, int n2, int n3) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(inventory, itemStack, n, n2, n3));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Slots slots, List<ItemPredicate> items) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)Slots.CODEC.optionalFieldOf("slots", (Object)Slots.ANY).forGetter(TriggerInstance::slots), (App)ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(TriggerInstance::items)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> hasItems(ItemPredicate.Builder ... builderArray) {
            return TriggerInstance.hasItems((ItemPredicate[])Stream.of(builderArray).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
        }

        public static Criterion<TriggerInstance> hasItems(ItemPredicate ... itemPredicateArray) {
            return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), Slots.ANY, List.of(itemPredicateArray)));
        }

        public static Criterion<TriggerInstance> hasItems(ItemLike ... itemLikeArray) {
            ItemPredicate[] itemPredicateArray = new ItemPredicate[itemLikeArray.length];
            for (int i = 0; i < itemLikeArray.length; ++i) {
                itemPredicateArray[i] = new ItemPredicate(Optional.of(HolderSet.direct(itemLikeArray[i].asItem().builtInRegistryHolder())), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of());
            }
            return TriggerInstance.hasItems(itemPredicateArray);
        }

        public boolean matches(Inventory inventory, ItemStack itemStack, int n, int n2, int n3) {
            if (!this.slots.matches(n, n2, n3)) {
                return false;
            }
            if (this.items.isEmpty()) {
                return true;
            }
            if (this.items.size() == 1) {
                return !itemStack.isEmpty() && this.items.get(0).test(itemStack);
            }
            ObjectArrayList objectArrayList = new ObjectArrayList(this.items);
            int n4 = inventory.getContainerSize();
            for (int i = 0; i < n4; ++i) {
                if (objectArrayList.isEmpty()) {
                    return true;
                }
                ItemStack itemStack2 = inventory.getItem(i);
                if (itemStack2.isEmpty()) continue;
                objectArrayList.removeIf(itemPredicate -> itemPredicate.test(itemStack2));
            }
            return objectArrayList.isEmpty();
        }

        public record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
            public static final Codec<Slots> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("occupied", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::occupied), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("full", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::full), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("empty", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::empty)).apply((Applicative)instance, Slots::new));
            public static final Slots ANY = new Slots(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY);

            public boolean matches(int n, int n2, int n3) {
                if (!this.full.matches(n)) {
                    return false;
                }
                if (!this.empty.matches(n2)) {
                    return false;
                }
                return this.occupied.matches(n3);
            }
        }
    }
}

