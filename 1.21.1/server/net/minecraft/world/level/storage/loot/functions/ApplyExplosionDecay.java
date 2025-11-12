/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay
extends LootItemConditionalFunction {
    public static final MapCodec<ApplyExplosionDecay> CODEC = RecordCodecBuilder.mapCodec(instance -> ApplyExplosionDecay.commonFields(instance).apply((Applicative)instance, ApplyExplosionDecay::new));

    private ApplyExplosionDecay(List<LootItemCondition> list) {
        super(list);
    }

    public LootItemFunctionType<ApplyExplosionDecay> getType() {
        return LootItemFunctions.EXPLOSION_DECAY;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Float f = lootContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (f != null) {
            RandomSource randomSource = lootContext.getRandom();
            float f2 = 1.0f / f.floatValue();
            int n = itemStack.getCount();
            int n2 = 0;
            for (int i = 0; i < n; ++i) {
                if (!(randomSource.nextFloat() <= f2)) continue;
                ++n2;
            }
            itemStack.setCount(n2);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> explosionDecay() {
        return ApplyExplosionDecay.simpleBuilder(ApplyExplosionDecay::new);
    }
}

