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
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate, Optional<DamageSourcePredicate> killingBlow) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entityPredicate), (App)DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(TriggerInstance::killingBlow)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerKilledEntity() {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), optional2));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), optional));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.of(builder.build())));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build())));
        }

        public static Criterion<TriggerInstance> playerKilledEntityNearSculkCatalyst() {
            return CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer() {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), optional2));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), optional));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.of(builder.build())));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build())));
        }

        public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource) {
            if (this.killingBlow.isPresent() && !this.killingBlow.get().matches(serverPlayer, damageSource)) {
                return false;
            }
            return this.entityPredicate.isEmpty() || this.entityPredicate.get().matches(lootContext);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(this.entityPredicate, ".entity");
        }
    }
}

