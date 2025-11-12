/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record EnchantmentLevelProvider(LevelBasedValue amount) implements NumberProvider
{
    public static final MapCodec<EnchantmentLevelProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentLevelProvider::amount)).apply((Applicative)instance, EnchantmentLevelProvider::new));

    @Override
    public float getFloat(LootContext lootContext) {
        int n = lootContext.getParam(LootContextParams.ENCHANTMENT_LEVEL);
        return this.amount.calculate(n);
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.ENCHANTMENT_LEVEL;
    }

    public static EnchantmentLevelProvider forEnchantmentLevel(LevelBasedValue levelBasedValue) {
        return new EnchantmentLevelProvider(levelBasedValue);
    }
}

