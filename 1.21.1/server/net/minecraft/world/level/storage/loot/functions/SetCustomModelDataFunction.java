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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetCustomModelDataFunction
extends LootItemConditionalFunction {
    static final MapCodec<SetCustomModelDataFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetCustomModelDataFunction.commonFields(instance).and((App)NumberProviders.CODEC.fieldOf("value").forGetter(setCustomModelDataFunction -> setCustomModelDataFunction.valueProvider)).apply((Applicative)instance, SetCustomModelDataFunction::new));
    private final NumberProvider valueProvider;

    private SetCustomModelDataFunction(List<LootItemCondition> list, NumberProvider numberProvider) {
        super(list);
        this.valueProvider = numberProvider;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.valueProvider.getReferencedContextParams();
    }

    public LootItemFunctionType<SetCustomModelDataFunction> getType() {
        return LootItemFunctions.SET_CUSTOM_MODEL_DATA;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(this.valueProvider.getInt(lootContext)));
        return itemStack;
    }
}

