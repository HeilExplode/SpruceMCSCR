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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, LightningBolt lightningBolt, List<Entity> list) {
        List list2 = list.stream().map(entity -> EntityPredicate.createContext(serverPlayer, entity)).collect(Collectors.toList());
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, lightningBolt);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, list2));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> lightning, Optional<ContextAwarePredicate> bystander) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("lightning").forGetter(TriggerInstance::lightning), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("bystander").forGetter(TriggerInstance::bystander)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> lightningStrike(Optional<EntityPredicate> optional, Optional<EntityPredicate> optional2) {
            return CriteriaTriggers.LIGHTNING_STRIKE.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), EntityPredicate.wrap(optional2)));
        }

        public boolean matches(LootContext lootContext, List<LootContext> list) {
            if (this.lightning.isPresent() && !this.lightning.get().matches(lootContext)) {
                return false;
            }
            if (this.bystander.isPresent()) {
                if (list.stream().noneMatch(this.bystander.get()::matches)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(this.lightning, ".lightning");
            criterionValidator.validateEntity(this.bystander, ".bystander");
        }
    }
}

