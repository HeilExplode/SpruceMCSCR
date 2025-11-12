/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record LootItemRandomChanceCondition(NumberProvider chance) implements LootItemCondition
{
    public static final MapCodec<LootItemRandomChanceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)NumberProviders.CODEC.fieldOf("chance").forGetter(LootItemRandomChanceCondition::chance)).apply((Applicative)instance, LootItemRandomChanceCondition::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        float f = this.chance.getFloat(lootContext);
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.Builder randomChance(float f) {
        return () -> new LootItemRandomChanceCondition(ConstantValue.exactly(f));
    }

    public static LootItemCondition.Builder randomChance(NumberProvider numberProvider) {
        return () -> new LootItemRandomChanceCondition(numberProvider);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }
}

