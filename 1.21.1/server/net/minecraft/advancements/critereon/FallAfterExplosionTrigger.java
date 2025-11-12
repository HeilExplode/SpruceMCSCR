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
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class FallAfterExplosionTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Vec3 vec3, @Nullable Entity entity) {
        Vec3 vec32 = serverPlayer.position();
        LootContext lootContext = entity != null ? EntityPredicate.createContext(serverPlayer, entity) : null;
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.serverLevel(), vec3, vec32, lootContext));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance, Optional<ContextAwarePredicate> cause) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(TriggerInstance::startPosition), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("cause").forGetter(TriggerInstance::cause)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> fallAfterExplosion(DistancePredicate distancePredicate, EntityPredicate.Builder builder) {
            return CriteriaTriggers.FALL_AFTER_EXPLOSION.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(distancePredicate), Optional.of(EntityPredicate.wrap(builder))));
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(this.cause(), ".cause");
        }

        public boolean matches(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, @Nullable LootContext lootContext) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches(serverLevel, vec3.x, vec3.y, vec3.z)) {
                return false;
            }
            if (this.distance.isPresent() && !this.distance.get().matches(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z)) {
                return false;
            }
            return !this.cause.isPresent() || lootContext != null && this.cause.get().matches(lootContext);
        }
    }
}

