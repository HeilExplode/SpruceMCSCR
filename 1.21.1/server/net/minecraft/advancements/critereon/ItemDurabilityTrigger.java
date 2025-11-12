/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, n));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, MinMaxBounds.Ints durability, MinMaxBounds.Ints delta) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::durability), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("delta", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::delta)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> changedDurability(Optional<ItemPredicate> optional, MinMaxBounds.Ints ints) {
            return TriggerInstance.changedDurability(Optional.empty(), optional, ints);
        }

        public static Criterion<TriggerInstance> changedDurability(Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2, MinMaxBounds.Ints ints) {
            return CriteriaTriggers.ITEM_DURABILITY_CHANGED.createCriterion(new TriggerInstance(optional, optional2, ints, MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ItemStack itemStack, int n) {
            if (this.item.isPresent() && !this.item.get().test(itemStack)) {
                return false;
            }
            if (!this.durability.matches(itemStack.getMaxDamage() - n)) {
                return false;
            }
            return this.delta.matches(itemStack.getDamageValue() - n);
        }
    }
}

