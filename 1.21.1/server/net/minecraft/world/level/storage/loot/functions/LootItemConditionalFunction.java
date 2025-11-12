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
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootItemConditionalFunction
implements LootItemFunction {
    protected final List<LootItemCondition> predicates;
    private final Predicate<LootContext> compositePredicates;

    protected LootItemConditionalFunction(List<LootItemCondition> list) {
        this.predicates = list;
        this.compositePredicates = Util.allOf(list);
    }

    public abstract LootItemFunctionType<? extends LootItemConditionalFunction> getType();

    protected static <T extends LootItemConditionalFunction> Products.P1<RecordCodecBuilder.Mu<T>, List<LootItemCondition>> commonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group((App)LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(lootItemConditionalFunction -> lootItemConditionalFunction.predicates));
    }

    @Override
    public final ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return this.compositePredicates.test(lootContext) ? this.run(itemStack, lootContext) : itemStack;
    }

    protected abstract ItemStack run(ItemStack var1, LootContext var2);

    @Override
    public void validate(ValidationContext validationContext) {
        LootItemFunction.super.validate(validationContext);
        for (int i = 0; i < this.predicates.size(); ++i) {
            this.predicates.get(i).validate(validationContext.forChild(".conditions[" + i + "]"));
        }
    }

    protected static Builder<?> simpleBuilder(Function<List<LootItemCondition>, LootItemFunction> function) {
        return new DummyBuilder(function);
    }

    @Override
    public /* synthetic */ Object apply(Object object, Object object2) {
        return this.apply((ItemStack)object, (LootContext)object2);
    }

    static final class DummyBuilder
    extends Builder<DummyBuilder> {
        private final Function<List<LootItemCondition>, LootItemFunction> constructor;

        public DummyBuilder(Function<List<LootItemCondition>, LootItemFunction> function) {
            this.constructor = function;
        }

        @Override
        protected DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return this.constructor.apply(this.getConditions());
        }

        @Override
        protected /* synthetic */ Builder getThis() {
            return this.getThis();
        }
    }

    public static abstract class Builder<T extends Builder<T>>
    implements LootItemFunction.Builder,
    ConditionUserBuilder<T> {
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

        @Override
        public T when(LootItemCondition.Builder builder) {
            this.conditions.add((Object)builder.build());
            return this.getThis();
        }

        @Override
        public final T unwrap() {
            return this.getThis();
        }

        protected abstract T getThis();

        protected List<LootItemCondition> getConditions() {
            return this.conditions.build();
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

