/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    @Override
    public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection) {
        ArrayList arrayList = Lists.newArrayList();
        HashSet hashSet = Sets.newHashSet();
        for (Entity entity : collection) {
            hashSet.add(entity.getType());
            arrayList.add(EntityPredicate.createContext(serverPlayer, entity));
        }
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(arrayList, hashSet.size()));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, MinMaxBounds.Ints uniqueEntityTypes) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(TriggerInstance::victims), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("unique_entity_types", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::uniqueEntityTypes)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> crossbowKilled(EntityPredicate.Builder ... builderArray) {
            return CriteriaTriggers.KILLED_BY_CROSSBOW.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(builderArray), MinMaxBounds.Ints.ANY));
        }

        public static Criterion<TriggerInstance> crossbowKilled(MinMaxBounds.Ints ints) {
            return CriteriaTriggers.KILLED_BY_CROSSBOW.createCriterion(new TriggerInstance(Optional.empty(), List.of(), ints));
        }

        public boolean matches(Collection<LootContext> collection, int n) {
            if (!this.victims.isEmpty()) {
                ArrayList arrayList = Lists.newArrayList(collection);
                for (ContextAwarePredicate contextAwarePredicate : this.victims) {
                    boolean bl = false;
                    Iterator iterator = arrayList.iterator();
                    while (iterator.hasNext()) {
                        LootContext lootContext = (LootContext)iterator.next();
                        if (!contextAwarePredicate.matches(lootContext)) continue;
                        iterator.remove();
                        bl = true;
                        break;
                    }
                    if (bl) continue;
                    return false;
                }
            }
            return this.uniqueEntityTypes.matches(n);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntities(this.victims, ".victims");
        }
    }
}

