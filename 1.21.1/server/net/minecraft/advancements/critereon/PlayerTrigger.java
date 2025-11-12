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
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, triggerInstance -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> located(LocationPredicate.Builder builder) {
            return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().located(builder)))));
        }

        public static Criterion<TriggerInstance> located(EntityPredicate.Builder builder) {
            return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(builder.build()))));
        }

        public static Criterion<TriggerInstance> located(Optional<EntityPredicate> optional) {
            return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(EntityPredicate.wrap(optional)));
        }

        public static Criterion<TriggerInstance> sleptInBed() {
            return CriteriaTriggers.SLEPT_IN_BED.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> raidWon() {
            return CriteriaTriggers.RAID_WIN.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> avoidVibration() {
            return CriteriaTriggers.AVOID_VIBRATION.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> tick() {
            return CriteriaTriggers.TICK.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> walkOnBlockWithEquipment(Block block, Item item) {
            return TriggerInstance.located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item))).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block))));
        }
    }
}

