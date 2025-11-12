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
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, @Nullable Entity entity) {
        LootContext lootContext = entity != null ? EntityPredicate.createContext(serverPlayer, entity) : null;
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(serverPlayer, lootContext));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<MobEffectsPredicate> effects, Optional<ContextAwarePredicate> source) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(TriggerInstance::effects), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("source").forGetter(TriggerInstance::source)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> hasEffects(MobEffectsPredicate.Builder builder) {
            return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), builder.build(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> gotEffectsFrom(EntityPredicate.Builder builder) {
            return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(builder.build()))));
        }

        public boolean matches(ServerPlayer serverPlayer, @Nullable LootContext lootContext) {
            if (this.effects.isPresent() && !this.effects.get().matches(serverPlayer)) {
                return false;
            }
            return !this.source.isPresent() || lootContext != null && this.source.get().matches(lootContext);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(this.source, ".source");
        }
    }
}

