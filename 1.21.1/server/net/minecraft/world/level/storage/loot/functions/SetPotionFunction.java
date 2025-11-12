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
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetPotionFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetPotionFunction.commonFields(instance).and((App)Potion.CODEC.fieldOf("id").forGetter(setPotionFunction -> setPotionFunction.potion)).apply((Applicative)instance, SetPotionFunction::new));
    private final Holder<Potion> potion;

    private SetPotionFunction(List<LootItemCondition> list, Holder<Potion> holder) {
        super(list);
        this.potion = holder;
    }

    public LootItemFunctionType<SetPotionFunction> getType() {
        return LootItemFunctions.SET_POTION;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, this.potion, PotionContents::withPotion);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setPotion(Holder<Potion> holder) {
        return SetPotionFunction.simpleBuilder(list -> new SetPotionFunction((List<LootItemCondition>)list, holder));
    }
}

