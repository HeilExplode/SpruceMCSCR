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
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, fishingHook.getHookedIn() != null ? fishingHook.getHookedIn() : fishingHook);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, lootContext, collection));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> rod, Optional<ContextAwarePredicate> entity, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ItemPredicate.CODEC.optionalFieldOf("rod").forGetter(TriggerInstance::rod), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> fishedItem(Optional<ItemPredicate> optional, Optional<EntityPredicate> optional2, Optional<ItemPredicate> optional3) {
            return CriteriaTriggers.FISHING_ROD_HOOKED.createCriterion(new TriggerInstance(Optional.empty(), optional, EntityPredicate.wrap(optional2), optional3));
        }

        public boolean matches(ItemStack itemStack, LootContext lootContext, Collection<ItemStack> collection) {
            if (this.rod.isPresent() && !this.rod.get().test(itemStack)) {
                return false;
            }
            if (this.entity.isPresent() && !this.entity.get().matches(lootContext)) {
                return false;
            }
            if (this.item.isPresent()) {
                boolean bl = false;
                Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity)entity;
                    if (this.item.get().test(itemEntity.getItem())) {
                        bl = true;
                    }
                }
                for (ItemStack itemStack2 : collection) {
                    if (!this.item.get().test(itemStack2)) continue;
                    bl = true;
                    break;
                }
                if (!bl) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(this.entity, ".entity");
        }
    }
}

