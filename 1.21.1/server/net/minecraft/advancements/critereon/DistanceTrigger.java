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
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
        Vec3 vec32 = serverPlayer.position();
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(serverPlayer.serverLevel(), vec3, vec32));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(TriggerInstance::startPosition), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> fallFromHeight(EntityPredicate.Builder builder, DistancePredicate distancePredicate, LocationPredicate.Builder builder2) {
            return CriteriaTriggers.FALL_FROM_HEIGHT.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build()), Optional.of(distancePredicate)));
        }

        public static Criterion<TriggerInstance> rideEntityInLava(EntityPredicate.Builder builder, DistancePredicate distancePredicate) {
            return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.empty(), Optional.of(distancePredicate)));
        }

        public static Criterion<TriggerInstance> travelledThroughNether(DistancePredicate distancePredicate) {
            return CriteriaTriggers.NETHER_TRAVEL.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(distancePredicate)));
        }

        public boolean matches(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches(serverLevel, vec3.x, vec3.y, vec3.z)) {
                return false;
            }
            return !this.distance.isPresent() || this.distance.get().matches(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
        }
    }
}

