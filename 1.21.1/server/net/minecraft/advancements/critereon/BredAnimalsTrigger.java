/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Animal animal, Animal animal2, @Nullable AgeableMob ageableMob) {
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
        LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, animal2);
        LootContext lootContext3 = ageableMob != null ? EntityPredicate.createContext(serverPlayer, ageableMob) : null;
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2, lootContext3));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> parent, Optional<ContextAwarePredicate> partner, Optional<ContextAwarePredicate> child) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent").forGetter(TriggerInstance::parent), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("partner").forGetter(TriggerInstance::partner), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("child").forGetter(TriggerInstance::child)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> bredAnimals() {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> bredAnimals(EntityPredicate.Builder builder) {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(builder))));
        }

        public static Criterion<TriggerInstance> bredAnimals(Optional<EntityPredicate> optional, Optional<EntityPredicate> optional2, Optional<EntityPredicate> optional3) {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), EntityPredicate.wrap(optional2), EntityPredicate.wrap(optional3)));
        }

        public boolean matches(LootContext lootContext, LootContext lootContext2, @Nullable LootContext lootContext3) {
            if (this.child.isPresent() && (lootContext3 == null || !this.child.get().matches(lootContext3))) {
                return false;
            }
            return TriggerInstance.matches(this.parent, lootContext) && TriggerInstance.matches(this.partner, lootContext2) || TriggerInstance.matches(this.parent, lootContext2) && TriggerInstance.matches(this.partner, lootContext);
        }

        private static boolean matches(Optional<ContextAwarePredicate> optional, LootContext lootContext) {
            return optional.isEmpty() || optional.get().matches(lootContext);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(this.parent, ".parent");
            criterionValidator.validateEntity(this.partner, ".partner");
            criterionValidator.validateEntity(this.child, ".child");
        }
    }
}

