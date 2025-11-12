/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class EnchantRandomlyFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<EnchantRandomlyFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> EnchantRandomlyFunction.commonFields(instance).and(instance.group((App)RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter(enchantRandomlyFunction -> enchantRandomlyFunction.options), (App)Codec.BOOL.optionalFieldOf("only_compatible", (Object)true).forGetter(enchantRandomlyFunction -> enchantRandomlyFunction.onlyCompatible))).apply((Applicative)instance, EnchantRandomlyFunction::new));
    private final Optional<HolderSet<Enchantment>> options;
    private final boolean onlyCompatible;

    EnchantRandomlyFunction(List<LootItemCondition> list, Optional<HolderSet<Enchantment>> optional, boolean bl) {
        super(list);
        this.options = optional;
        this.onlyCompatible = bl;
    }

    public LootItemFunctionType<EnchantRandomlyFunction> getType() {
        return LootItemFunctions.ENCHANT_RANDOMLY;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        RandomSource randomSource = lootContext.getRandom();
        boolean bl = itemStack.is(Items.BOOK);
        boolean bl2 = !bl && this.onlyCompatible;
        Stream<Holder> stream = this.options.map(HolderSet::stream).orElseGet(() -> lootContext.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().map(Function.identity())).filter(holder -> !bl2 || ((Enchantment)holder.value()).canEnchant(itemStack));
        List<Holder> list = stream.toList();
        Optional<Holder> optional = Util.getRandomSafe(list, randomSource);
        if (optional.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", (Object)itemStack);
            return itemStack;
        }
        return EnchantRandomlyFunction.enchantItem(itemStack, optional.get(), randomSource);
    }

    private static ItemStack enchantItem(ItemStack itemStack, Holder<Enchantment> holder, RandomSource randomSource) {
        int n = Mth.nextInt(randomSource, holder.value().getMinLevel(), holder.value().getMaxLevel());
        if (itemStack.is(Items.BOOK)) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        itemStack.enchant(holder, n);
        return itemStack;
    }

    public static Builder randomEnchantment() {
        return new Builder();
    }

    public static Builder randomApplicableEnchantment(HolderLookup.Provider provider) {
        return EnchantRandomlyFunction.randomEnchantment().withOneOf(provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private Optional<HolderSet<Enchantment>> options = Optional.empty();
        private boolean onlyCompatible = true;

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEnchantment(Holder<Enchantment> holder) {
            this.options = Optional.of(HolderSet.direct(holder));
            return this;
        }

        public Builder withOneOf(HolderSet<Enchantment> holderSet) {
            this.options = Optional.of(holderSet);
            return this;
        }

        public Builder allowingIncompatibleEnchantments() {
            this.onlyCompatible = false;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantRandomlyFunction(this.getConditions(), this.options, this.onlyCompatible);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

