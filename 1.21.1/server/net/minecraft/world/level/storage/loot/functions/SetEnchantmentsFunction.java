/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetEnchantmentsFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetEnchantmentsFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetEnchantmentsFunction.commonFields(instance).and(instance.group((App)Codec.unboundedMap(Enchantment.CODEC, NumberProviders.CODEC).optionalFieldOf("enchantments", Map.of()).forGetter(setEnchantmentsFunction -> setEnchantmentsFunction.enchantments), (App)Codec.BOOL.fieldOf("add").orElse((Object)false).forGetter(setEnchantmentsFunction -> setEnchantmentsFunction.add))).apply((Applicative)instance, SetEnchantmentsFunction::new));
    private final Map<Holder<Enchantment>, NumberProvider> enchantments;
    private final boolean add;

    SetEnchantmentsFunction(List<LootItemCondition> list, Map<Holder<Enchantment>, NumberProvider> map, boolean bl) {
        super(list);
        this.enchantments = Map.copyOf(map);
        this.add = bl;
    }

    public LootItemFunctionType<SetEnchantmentsFunction> getType() {
        return LootItemFunctions.SET_ENCHANTMENTS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return (Set)this.enchantments.values().stream().flatMap(numberProvider -> numberProvider.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.is(Items.BOOK)) {
            itemStack = itemStack.transmuteCopy(Items.ENCHANTED_BOOK);
            itemStack.set(DataComponents.STORED_ENCHANTMENTS, itemStack.remove(DataComponents.ENCHANTMENTS));
        }
        EnchantmentHelper.updateEnchantments(itemStack, mutable -> {
            if (this.add) {
                this.enchantments.forEach((holder, numberProvider) -> mutable.set((Holder<Enchantment>)holder, Mth.clamp(mutable.getLevel((Holder<Enchantment>)holder) + numberProvider.getInt(lootContext), 0, 255)));
            } else {
                this.enchantments.forEach((holder, numberProvider) -> mutable.set((Holder<Enchantment>)holder, Mth.clamp(numberProvider.getInt(lootContext), 0, 255)));
            }
        });
        return itemStack;
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ImmutableMap.Builder<Holder<Enchantment>, NumberProvider> enchantments = ImmutableMap.builder();
        private final boolean add;

        public Builder() {
            this(false);
        }

        public Builder(boolean bl) {
            this.add = bl;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEnchantment(Holder<Enchantment> holder, NumberProvider numberProvider) {
            this.enchantments.put(holder, (Object)numberProvider);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetEnchantmentsFunction(this.getConditions(), (Map<Holder<Enchantment>, NumberProvider>)this.enchantments.build(), this.add);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

