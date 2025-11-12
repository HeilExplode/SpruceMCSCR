/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public record BonusLevelTableCondition(Holder<Enchantment> enchantment, List<Float> values) implements LootItemCondition
{
    public static final MapCodec<BonusLevelTableCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Enchantment.CODEC.fieldOf("enchantment").forGetter(BonusLevelTableCondition::enchantment), (App)ExtraCodecs.nonEmptyList(Codec.FLOAT.listOf()).fieldOf("chances").forGetter(BonusLevelTableCondition::values)).apply((Applicative)instance, BonusLevelTableCondition::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TABLE_BONUS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        int n = itemStack != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemStack) : 0;
        float f = this.values.get(Math.min(n, this.values.size() - 1)).floatValue();
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.Builder bonusLevelFlatChance(Holder<Enchantment> holder, float ... fArray) {
        ArrayList<Float> arrayList = new ArrayList<Float>(fArray.length);
        for (float f : fArray) {
            arrayList.add(Float.valueOf(f));
        }
        return () -> new BonusLevelTableCondition(holder, arrayList);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }
}

