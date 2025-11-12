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
import java.util.Set;
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
import org.slf4j.Logger;

public class SetItemDamageFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SetItemDamageFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetItemDamageFunction.commonFields(instance).and(instance.group((App)NumberProviders.CODEC.fieldOf("damage").forGetter(setItemDamageFunction -> setItemDamageFunction.damage), (App)Codec.BOOL.fieldOf("add").orElse((Object)false).forGetter(setItemDamageFunction -> setItemDamageFunction.add))).apply((Applicative)instance, SetItemDamageFunction::new));
    private final NumberProvider damage;
    private final boolean add;

    private SetItemDamageFunction(List<LootItemCondition> list, NumberProvider numberProvider, boolean bl) {
        super(list);
        this.damage = numberProvider;
        this.add = bl;
    }

    public LootItemFunctionType<SetItemDamageFunction> getType() {
        return LootItemFunctions.SET_DAMAGE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.damage.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isDamageableItem()) {
            int n = itemStack.getMaxDamage();
            float f = this.add ? 1.0f - (float)itemStack.getDamageValue() / (float)n : 0.0f;
            float f2 = 1.0f - Mth.clamp(this.damage.getFloat(lootContext) + f, 0.0f, 1.0f);
            itemStack.setDamageValue(Mth.floor(f2 * (float)n));
        } else {
            LOGGER.warn("Couldn't set damage of loot item {}", (Object)itemStack);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider) {
        return SetItemDamageFunction.simpleBuilder(list -> new SetItemDamageFunction((List<LootItemCondition>)list, numberProvider, false));
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider, boolean bl) {
        return SetItemDamageFunction.simpleBuilder(list -> new SetItemDamageFunction((List<LootItemCondition>)list, numberProvider, bl));
    }
}

