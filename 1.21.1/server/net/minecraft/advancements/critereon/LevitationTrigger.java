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
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int n) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, vec3, n));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DistancePredicate> distance, MinMaxBounds.Ints duration) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::duration)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> levitated(DistancePredicate distancePredicate) {
            return CriteriaTriggers.LEVITATION.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(distancePredicate), MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int n) {
            if (this.distance.isPresent() && !this.distance.get().matches(vec3.x, vec3.y, vec3.z, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ())) {
                return false;
            }
            return this.duration.matches(n);
        }
    }
}

