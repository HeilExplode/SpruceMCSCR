/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
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

public class EnchantWithLevelsFunction
extends LootItemConditionalFunction {
    public static final MapCodec<EnchantWithLevelsFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> EnchantWithLevelsFunction.commonFields(instance).and(instance.group((App)NumberProviders.CODEC.fieldOf("levels").forGetter(enchantWithLevelsFunction -> enchantWithLevelsFunction.levels), (App)RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter(enchantWithLevelsFunction -> enchantWithLevelsFunction.options))).apply((Applicative)instance, EnchantWithLevelsFunction::new));
    private final NumberProvider levels;
    private final Optional<HolderSet<Enchantment>> options;

    EnchantWithLevelsFunction(List<LootItemCondition> list, NumberProvider numberProvider, Optional<HolderSet<Enchantment>> optional) {
        super(list);
        this.levels = numberProvider;
        this.options = optional;
    }

    public LootItemFunctionType<EnchantWithLevelsFunction> getType() {
        return LootItemFunctions.ENCHANT_WITH_LEVELS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.levels.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        RandomSource randomSource = lootContext.getRandom();
        RegistryAccess registryAccess = lootContext.getLevel().registryAccess();
        return EnchantmentHelper.enchantItem(randomSource, itemStack, this.levels.getInt(lootContext), registryAccess, this.options);
    }

    public static Builder enchantWithLevels(HolderLookup.Provider provider, NumberProvider numberProvider) {
        return new Builder(numberProvider).fromOptions(provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final NumberProvider levels;
        private Optional<HolderSet<Enchantment>> options = Optional.empty();

        public Builder(NumberProvider numberProvider) {
            this.levels = numberProvider;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder fromOptions(HolderSet<Enchantment> holderSet) {
            this.options = Optional.of(holderSet);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantWithLevelsFunction(this.getConditions(), this.levels, this.options);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

