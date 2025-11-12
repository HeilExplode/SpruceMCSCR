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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetOminousBottleAmplifierFunction
extends LootItemConditionalFunction {
    static final MapCodec<SetOminousBottleAmplifierFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetOminousBottleAmplifierFunction.commonFields(instance).and((App)NumberProviders.CODEC.fieldOf("amplifier").forGetter(setOminousBottleAmplifierFunction -> setOminousBottleAmplifierFunction.amplifierGenerator)).apply((Applicative)instance, SetOminousBottleAmplifierFunction::new));
    private final NumberProvider amplifierGenerator;

    private SetOminousBottleAmplifierFunction(List<LootItemCondition> list, NumberProvider numberProvider) {
        super(list);
        this.amplifierGenerator = numberProvider;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.amplifierGenerator.getReferencedContextParams();
    }

    public LootItemFunctionType<SetOminousBottleAmplifierFunction> getType() {
        return LootItemFunctions.SET_OMINOUS_BOTTLE_AMPLIFIER;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        int n = Mth.clamp(this.amplifierGenerator.getInt(lootContext), 0, 4);
        itemStack.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, n);
        return itemStack;
    }

    public NumberProvider amplifier() {
        return this.amplifierGenerator;
    }

    public static LootItemConditionalFunction.Builder<?> setAmplifier(NumberProvider numberProvider) {
        return SetOminousBottleAmplifierFunction.simpleBuilder(list -> new SetOminousBottleAmplifierFunction((List<LootItemCondition>)list, numberProvider));
    }
}

