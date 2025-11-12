/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AlternativesEntry
extends CompositeEntryBase {
    public static final MapCodec<AlternativesEntry> CODEC = AlternativesEntry.createCodec(AlternativesEntry::new);

    AlternativesEntry(List<LootPoolEntryContainer> list, List<LootItemCondition> list2) {
        super(list, list2);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.ALTERNATIVES;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> list) {
        return switch (list.size()) {
            case 0 -> ALWAYS_FALSE;
            case 1 -> list.get(0);
            case 2 -> list.get(0).or(list.get(1));
            default -> (lootContext, consumer) -> {
                for (ComposableEntryContainer composableEntryContainer : list) {
                    if (!composableEntryContainer.expand(lootContext, consumer)) continue;
                    return true;
                }
                return false;
            };
        };
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.children.size() - 1; ++i) {
            if (!((LootPoolEntryContainer)this.children.get((int)i)).conditions.isEmpty()) continue;
            validationContext.reportProblem("Unreachable entry!");
        }
    }

    public static Builder alternatives(LootPoolEntryContainer.Builder<?> ... builderArray) {
        return new Builder(builderArray);
    }

    public static <E> Builder alternatives(Collection<E> collection, Function<E, LootPoolEntryContainer.Builder<?>> function) {
        return new Builder((LootPoolEntryContainer.Builder[])collection.stream().map(function::apply).toArray(LootPoolEntryContainer.Builder[]::new));
    }

    public static class Builder
    extends LootPoolEntryContainer.Builder<Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

        public Builder(LootPoolEntryContainer.Builder<?> ... builderArray) {
            for (LootPoolEntryContainer.Builder<?> builder : builderArray) {
                this.entries.add((Object)builder.build());
            }
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder otherwise(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add((Object)builder.build());
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new AlternativesEntry((List<LootPoolEntryContainer>)this.entries.build(), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntryContainer.Builder getThis() {
            return this.getThis();
        }
    }
}

