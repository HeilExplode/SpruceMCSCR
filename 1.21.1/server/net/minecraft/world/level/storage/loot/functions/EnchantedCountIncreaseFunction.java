/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantedCountIncreaseFunction
extends LootItemConditionalFunction {
    public static final int NO_LIMIT = 0;
    public static final MapCodec<EnchantedCountIncreaseFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> EnchantedCountIncreaseFunction.commonFields(instance).and(instance.group((App)Enchantment.CODEC.fieldOf("enchantment").forGetter(enchantedCountIncreaseFunction -> enchantedCountIncreaseFunction.enchantment), (App)NumberProviders.CODEC.fieldOf("count").forGetter(enchantedCountIncreaseFunction -> enchantedCountIncreaseFunction.value), (App)Codec.INT.optionalFieldOf("limit", (Object)0).forGetter(enchantedCountIncreaseFunction -> enchantedCountIncreaseFunction.limit))).apply((Applicative)instance, EnchantedCountIncreaseFunction::new));
    private final Holder<Enchantment> enchantment;
    private final NumberProvider value;
    private final int limit;

    EnchantedCountIncreaseFunction(List<LootItemCondition> list, Holder<Enchantment> holder, NumberProvider numberProvider, int n) {
        super(list);
        this.enchantment = holder;
        this.value = numberProvider;
        this.limit = n;
    }

    public LootItemFunctionType<EnchantedCountIncreaseFunction> getType() {
        return LootItemFunctions.ENCHANTED_COUNT_INCREASE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union((Set)ImmutableSet.of(LootContextParams.ATTACKING_ENTITY), this.value.getReferencedContextParams());
    }

    private boolean hasLimit() {
        return this.limit > 0;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            int n = EnchantmentHelper.getEnchantmentLevel(this.enchantment, livingEntity);
            if (n == 0) {
                return itemStack;
            }
            float f = (float)n * this.value.getFloat(lootContext);
            itemStack.grow(Math.round(f));
            if (this.hasLimit()) {
                itemStack.limitSize(this.limit);
            }
        }
        return itemStack;
    }

    public static Builder lootingMultiplier(HolderLookup.Provider provider, NumberProvider numberProvider) {
        HolderLookup.RegistryLookup<Enchantment> registryLookup = provider.lookupOrThrow(Registries.ENCHANTMENT);
        return new Builder(registryLookup.getOrThrow(Enchantments.LOOTING), numberProvider);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final Holder<Enchantment> enchantment;
        private final NumberProvider count;
        private int limit = 0;

        public Builder(Holder<Enchantment> holder, NumberProvider numberProvider) {
            this.enchantment = holder;
            this.count = numberProvider;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder setLimit(int n) {
            this.limit = n;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantedCountIncreaseFunction(this.getConditions(), this.enchantment, this.count, this.limit);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

