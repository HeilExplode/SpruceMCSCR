/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.Products$P4
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolSingletonContainer
extends LootPoolEntryContainer {
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;
    protected final List<LootItemFunction> functions;
    final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry = new EntryBase(){

        @Override
        public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
            LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, consumer, lootContext), lootContext);
        }
    };

    protected LootPoolSingletonContainer(int n, int n2, List<LootItemCondition> list, List<LootItemFunction> list2) {
        super(list);
        this.weight = n;
        this.quality = n2;
        this.functions = list2;
        this.compositeFunction = LootItemFunctions.compose(list2);
    }

    protected static <T extends LootPoolSingletonContainer> Products.P4<RecordCodecBuilder.Mu<T>, Integer, Integer, List<LootItemCondition>, List<LootItemFunction>> singletonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group((App)Codec.INT.optionalFieldOf("weight", (Object)1).forGetter(lootPoolSingletonContainer -> lootPoolSingletonContainer.weight), (App)Codec.INT.optionalFieldOf("quality", (Object)0).forGetter(lootPoolSingletonContainer -> lootPoolSingletonContainer.quality)).and(LootPoolSingletonContainer.commonFields(instance).t1()).and((App)LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(lootPoolSingletonContainer -> lootPoolSingletonContainer.functions));
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.functions.size(); ++i) {
            this.functions.get(i).validate(validationContext.forChild(".functions[" + i + "]"));
        }
    }

    protected abstract void createItemStack(Consumer<ItemStack> var1, LootContext var2);

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.canRun(lootContext)) {
            consumer.accept(this.entry);
            return true;
        }
        return false;
    }

    public static Builder<?> simpleBuilder(EntryConstructor entryConstructor) {
        return new DummyBuilder(entryConstructor);
    }

    static class DummyBuilder
    extends Builder<DummyBuilder> {
        private final EntryConstructor constructor;

        public DummyBuilder(EntryConstructor entryConstructor) {
            this.constructor = entryConstructor;
        }

        @Override
        protected DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
        }

        @Override
        protected /* synthetic */ LootPoolEntryContainer.Builder getThis() {
            return this.getThis();
        }
    }

    @FunctionalInterface
    protected static interface EntryConstructor {
        public LootPoolSingletonContainer build(int var1, int var2, List<LootItemCondition> var3, List<LootItemFunction> var4);
    }

    public static abstract class Builder<T extends Builder<T>>
    extends LootPoolEntryContainer.Builder<T>
    implements FunctionUserBuilder<T> {
        protected int weight = 1;
        protected int quality = 0;
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();

        @Override
        public T apply(LootItemFunction.Builder builder) {
            this.functions.add((Object)builder.build());
            return (T)((Builder)this.getThis());
        }

        protected List<LootItemFunction> getFunctions() {
            return this.functions.build();
        }

        public T setWeight(int n) {
            this.weight = n;
            return (T)((Builder)this.getThis());
        }

        public T setQuality(int n) {
            this.quality = n;
            return (T)((Builder)this.getThis());
        }

        @Override
        public /* synthetic */ FunctionUserBuilder unwrap() {
            return (FunctionUserBuilder)((Object)super.unwrap());
        }

        @Override
        public /* synthetic */ FunctionUserBuilder apply(LootItemFunction.Builder builder) {
            return this.apply(builder);
        }
    }

    protected abstract class EntryBase
    implements LootPoolEntry {
        protected EntryBase() {
        }

        @Override
        public int getWeight(float f) {
            return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * f), 0);
        }
    }
}

