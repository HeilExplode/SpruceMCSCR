/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.Products$P1
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolEntryContainer
implements ComposableEntryContainer {
    protected final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositeCondition;

    protected LootPoolEntryContainer(List<LootItemCondition> list) {
        this.conditions = list;
        this.compositeCondition = Util.allOf(list);
    }

    protected static <T extends LootPoolEntryContainer> Products.P1<RecordCodecBuilder.Mu<T>, List<LootItemCondition>> commonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group((App)LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(lootPoolEntryContainer -> lootPoolEntryContainer.conditions));
    }

    public void validate(ValidationContext validationContext) {
        for (int i = 0; i < this.conditions.size(); ++i) {
            this.conditions.get(i).validate(validationContext.forChild(".condition[" + i + "]"));
        }
    }

    protected final boolean canRun(LootContext lootContext) {
        return this.compositeCondition.test(lootContext);
    }

    public abstract LootPoolEntryType getType();

    public static abstract class Builder<T extends Builder<T>>
    implements ConditionUserBuilder<T> {
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

        protected abstract T getThis();

        @Override
        public T when(LootItemCondition.Builder builder) {
            this.conditions.add((Object)builder.build());
            return this.getThis();
        }

        @Override
        public final T unwrap() {
            return this.getThis();
        }

        protected List<LootItemCondition> getConditions() {
            return this.conditions.build();
        }

        public AlternativesEntry.Builder otherwise(Builder<?> builder) {
            return new AlternativesEntry.Builder(this, builder);
        }

        public EntryGroup.Builder append(Builder<?> builder) {
            return new EntryGroup.Builder(this, builder);
        }

        public SequentialEntry.Builder then(Builder<?> builder) {
            return new SequentialEntry.Builder(this, builder);
        }

        public abstract LootPoolEntryContainer build();

        @Override
        public /* synthetic */ ConditionUserBuilder unwrap() {
            return this.unwrap();
        }

        @Override
        public /* synthetic */ ConditionUserBuilder when(LootItemCondition.Builder builder) {
            return this.when(builder);
        }
    }
}

