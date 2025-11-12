/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public record ConditionReference(ResourceKey<LootItemCondition> name) implements LootItemCondition
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<ConditionReference> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceKey.codec(Registries.PREDICATE).fieldOf("name").forGetter(ConditionReference::name)).apply((Applicative)instance, ConditionReference::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        if (!validationContext.allowsReferences()) {
            validationContext.reportProblem("Uses reference to " + String.valueOf(this.name.location()) + ", but references are not allowed");
            return;
        }
        if (validationContext.hasVisitedElement(this.name)) {
            validationContext.reportProblem("Condition " + String.valueOf(this.name.location()) + " is recursively called");
            return;
        }
        LootItemCondition.super.validate(validationContext);
        validationContext.resolver().get(Registries.PREDICATE, this.name).ifPresentOrElse(reference -> ((LootItemCondition)reference.value()).validate(validationContext.enterElement(".{" + String.valueOf(this.name.location()) + "}", this.name)), () -> validationContext.reportProblem("Unknown condition table called " + String.valueOf(this.name.location())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean test(LootContext lootContext) {
        LootItemCondition lootItemCondition = lootContext.getResolver().get(Registries.PREDICATE, this.name).map(Holder.Reference::value).orElse(null);
        if (lootItemCondition == null) {
            LOGGER.warn("Tried using unknown condition table called {}", (Object)this.name.location());
            return false;
        }
        LootContext.VisitedEntry<LootItemCondition> visitedEntry = LootContext.createVisitedEntry(lootItemCondition);
        if (lootContext.pushVisitedElement(visitedEntry)) {
            try {
                boolean bl = lootItemCondition.test(lootContext);
                return bl;
            }
            finally {
                lootContext.popVisitedElement(visitedEntry);
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return false;
    }

    public static LootItemCondition.Builder conditionReference(ResourceKey<LootItemCondition> resourceKey) {
        return () -> new ConditionReference(resourceKey);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }
}

