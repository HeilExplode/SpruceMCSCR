/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetItemCountFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetItemCountFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetItemCountFunction.commonFields(instance).and(instance.group((App)NumberProviders.CODEC.fieldOf("count").forGetter(setItemCountFunction -> setItemCountFunction.value), (App)Codec.BOOL.fieldOf("add").orElse((Object)false).forGetter(setItemCountFunction -> setItemCountFunction.add))).apply((Applicative)instance, SetItemCountFunction::new));
    private final NumberProvider value;
    private final boolean add;

    private SetItemCountFunction(List<LootItemCondition> list, NumberProvider numberProvider, boolean bl) {
        super(list);
        this.value = numberProvider;
        this.add = bl;
    }

    public LootItemFunctionType<SetItemCountFunction> getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        int n = this.add ? itemStack.getCount() : 0;
        itemStack.setCount(n + this.value.getInt(lootContext));
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider) {
        return SetItemCountFunction.simpleBuilder(list -> new SetItemCountFunction((List<LootItemCondition>)list, numberProvider, false));
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider, boolean bl) {
        return SetItemCountFunction.simpleBuilder(list -> new SetItemCountFunction((List<LootItemCondition>)list, numberProvider, bl));
    }
}

