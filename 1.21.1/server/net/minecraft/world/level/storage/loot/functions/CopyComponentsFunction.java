/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyComponentsFunction
extends LootItemConditionalFunction {
    public static final MapCodec<CopyComponentsFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyComponentsFunction.commonFields(instance).and(instance.group((App)Source.CODEC.fieldOf("source").forGetter(copyComponentsFunction -> copyComponentsFunction.source), (App)DataComponentType.CODEC.listOf().optionalFieldOf("include").forGetter(copyComponentsFunction -> copyComponentsFunction.include), (App)DataComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter(copyComponentsFunction -> copyComponentsFunction.exclude))).apply((Applicative)instance, CopyComponentsFunction::new));
    private final Source source;
    private final Optional<List<DataComponentType<?>>> include;
    private final Optional<List<DataComponentType<?>>> exclude;
    private final Predicate<DataComponentType<?>> bakedPredicate;

    CopyComponentsFunction(List<LootItemCondition> list, Source source, Optional<List<DataComponentType<?>>> optional, Optional<List<DataComponentType<?>>> optional2) {
        super(list);
        this.source = source;
        this.include = optional.map(List::copyOf);
        this.exclude = optional2.map(List::copyOf);
        ArrayList arrayList = new ArrayList(2);
        optional2.ifPresent(list2 -> arrayList.add(dataComponentType -> !list2.contains(dataComponentType)));
        optional.ifPresent(list2 -> arrayList.add(list2::contains));
        this.bakedPredicate = Util.allOf(arrayList);
    }

    public LootItemFunctionType<CopyComponentsFunction> getType() {
        return LootItemFunctions.COPY_COMPONENTS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.source.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        DataComponentMap dataComponentMap = this.source.get(lootContext);
        itemStack.applyComponents(dataComponentMap.filter(this.bakedPredicate));
        return itemStack;
    }

    public static Builder copyComponents(Source source) {
        return new Builder(source);
    }

    public static enum Source implements StringRepresentable
    {
        BLOCK_ENTITY("block_entity");

        public static final Codec<Source> CODEC;
        private final String name;

        private Source(String string2) {
            this.name = string2;
        }

        public DataComponentMap get(LootContext lootContext) {
            switch (this.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: 
            }
            BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            return blockEntity != null ? blockEntity.collectComponents() : DataComponentMap.EMPTY;
        }

        public Set<LootContextParam<?>> getReferencedContextParams() {
            switch (this.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: 
            }
            return Set.of(LootContextParams.BLOCK_ENTITY);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromValues(Source::values);
        }
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final Source source;
        private Optional<ImmutableList.Builder<DataComponentType<?>>> include = Optional.empty();
        private Optional<ImmutableList.Builder<DataComponentType<?>>> exclude = Optional.empty();

        Builder(Source source) {
            this.source = source;
        }

        public Builder include(DataComponentType<?> dataComponentType) {
            if (this.include.isEmpty()) {
                this.include = Optional.of(ImmutableList.builder());
            }
            this.include.get().add(dataComponentType);
            return this;
        }

        public Builder exclude(DataComponentType<?> dataComponentType) {
            if (this.exclude.isEmpty()) {
                this.exclude = Optional.of(ImmutableList.builder());
            }
            this.exclude.get().add(dataComponentType);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyComponentsFunction(this.getConditions(), this.source, this.include.map(ImmutableList.Builder::build), this.exclude.map(ImmutableList.Builder::build));
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

