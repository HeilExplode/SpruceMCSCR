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
import java.util.List;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry
extends CompositeEntryBase {
    public static final MapCodec<SequentialEntry> CODEC = SequentialEntry.createCodec(SequentialEntry::new);

    SequentialEntry(List<LootPoolEntryContainer> list, List<LootItemCondition> list2) {
        super(list, list2);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.SEQUENCE;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> list) {
        return switch (list.size()) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> list.get(0);
            case 2 -> list.get(0).and(list.get(1));
            default -> (lootContext, consumer) -> {
                for (ComposableEntryContainer composableEntryContainer : list) {
                    if (composableEntryContainer.expand(lootContext, consumer)) continue;
                    return false;
                }
                return true;
            };
        };
    }

    public static Builder sequential(LootPoolEntryContainer.Builder<?> ... builderArray) {
        return new Builder(builderArray);
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
        public Builder then(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add((Object)builder.build());
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new SequentialEntry((List<LootPoolEntryContainer>)this.entries.build(), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntryContainer.Builder getThis() {
            return this.getThis();
        }
    }
}

