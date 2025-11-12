/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
    public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(lootPool -> lootPool.entries), (App)LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(lootPool -> lootPool.conditions), (App)LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(lootPool -> lootPool.functions), (App)NumberProviders.CODEC.fieldOf("rolls").forGetter(lootPool -> lootPool.rolls), (App)NumberProviders.CODEC.fieldOf("bonus_rolls").orElse((Object)ConstantValue.exactly(0.0f)).forGetter(lootPool -> lootPool.bonusRolls)).apply((Applicative)instance, LootPool::new));
    private final List<LootPoolEntryContainer> entries;
    private final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositeCondition;
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final NumberProvider rolls;
    private final NumberProvider bonusRolls;

    LootPool(List<LootPoolEntryContainer> list, List<LootItemCondition> list2, List<LootItemFunction> list3, NumberProvider numberProvider, NumberProvider numberProvider2) {
        this.entries = list;
        this.conditions = list2;
        this.compositeCondition = Util.allOf(list2);
        this.functions = list3;
        this.compositeFunction = LootItemFunctions.compose(list3);
        this.rolls = numberProvider;
        this.bonusRolls = numberProvider2;
    }

    private void addRandomItem(Consumer<ItemStack> consumer, LootContext lootContext) {
        RandomSource randomSource = lootContext.getRandom();
        ArrayList arrayList = Lists.newArrayList();
        MutableInt mutableInt = new MutableInt();
        for (LootPoolEntryContainer lootPoolEntryContainer : this.entries) {
            lootPoolEntryContainer.expand(lootContext, lootPoolEntry -> {
                int n = lootPoolEntry.getWeight(lootContext.getLuck());
                if (n > 0) {
                    arrayList.add(lootPoolEntry);
                    mutableInt.add(n);
                }
            });
        }
        int n = arrayList.size();
        if (mutableInt.intValue() == 0 || n == 0) {
            return;
        }
        if (n == 1) {
            ((LootPoolEntry)arrayList.get(0)).createItemStack(consumer, lootContext);
            return;
        }
        int n2 = randomSource.nextInt(mutableInt.intValue());
        for (LootPoolEntry lootPoolEntry2 : arrayList) {
            if ((n2 -= lootPoolEntry2.getWeight(lootContext.getLuck())) >= 0) continue;
            lootPoolEntry2.createItemStack(consumer, lootContext);
            return;
        }
    }

    public void addRandomItems(Consumer<ItemStack> consumer, LootContext lootContext) {
        if (!this.compositeCondition.test(lootContext)) {
            return;
        }
        Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
        int n = this.rolls.getInt(lootContext) + Mth.floor(this.bonusRolls.getFloat(lootContext) * lootContext.getLuck());
        for (int i = 0; i < n; ++i) {
            this.addRandomItem(consumer2, lootContext);
        }
    }

    public void validate(ValidationContext validationContext) {
        int n;
        for (n = 0; n < this.conditions.size(); ++n) {
            this.conditions.get(n).validate(validationContext.forChild(".condition[" + n + "]"));
        }
        for (n = 0; n < this.functions.size(); ++n) {
            this.functions.get(n).validate(validationContext.forChild(".functions[" + n + "]"));
        }
        for (n = 0; n < this.entries.size(); ++n) {
            this.entries.get(n).validate(validationContext.forChild(".entries[" + n + "]"));
        }
        this.rolls.validate(validationContext.forChild(".rolls"));
        this.bonusRolls.validate(validationContext.forChild(".bonusRolls"));
    }

    public static Builder lootPool() {
        return new Builder();
    }

    public static class Builder
    implements FunctionUserBuilder<Builder>,
    ConditionUserBuilder<Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private NumberProvider rolls = ConstantValue.exactly(1.0f);
        private NumberProvider bonusRolls = ConstantValue.exactly(0.0f);

        public Builder setRolls(NumberProvider numberProvider) {
            this.rolls = numberProvider;
            return this;
        }

        @Override
        public Builder unwrap() {
            return this;
        }

        public Builder setBonusRolls(NumberProvider numberProvider) {
            this.bonusRolls = numberProvider;
            return this;
        }

        public Builder add(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add((Object)builder.build());
            return this;
        }

        @Override
        public Builder when(LootItemCondition.Builder builder) {
            this.conditions.add((Object)builder.build());
            return this;
        }

        @Override
        public Builder apply(LootItemFunction.Builder builder) {
            this.functions.add((Object)builder.build());
            return this;
        }

        public LootPool build() {
            return new LootPool((List<LootPoolEntryContainer>)this.entries.build(), (List<LootItemCondition>)this.conditions.build(), (List<LootItemFunction>)this.functions.build(), this.rolls, this.bonusRolls);
        }

        @Override
        public /* synthetic */ FunctionUserBuilder unwrap() {
            return this.unwrap();
        }

        @Override
        public /* synthetic */ FunctionUserBuilder apply(LootItemFunction.Builder builder) {
            return this.apply(builder);
        }

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

