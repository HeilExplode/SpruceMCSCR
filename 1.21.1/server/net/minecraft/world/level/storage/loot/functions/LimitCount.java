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
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount
extends LootItemConditionalFunction {
    public static final MapCodec<LimitCount> CODEC = RecordCodecBuilder.mapCodec(instance -> LimitCount.commonFields(instance).and((App)IntRange.CODEC.fieldOf("limit").forGetter(limitCount -> limitCount.limiter)).apply((Applicative)instance, LimitCount::new));
    private final IntRange limiter;

    private LimitCount(List<LootItemCondition> list, IntRange intRange) {
        super(list);
        this.limiter = intRange;
    }

    public LootItemFunctionType<LimitCount> getType() {
        return LootItemFunctions.LIMIT_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.limiter.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        int n = this.limiter.clamp(lootContext, itemStack.getCount());
        itemStack.setCount(n);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> limitCount(IntRange intRange) {
        return LimitCount.simpleBuilder(list -> new LimitCount((List<LootItemCondition>)list, intRange));
    }
}

