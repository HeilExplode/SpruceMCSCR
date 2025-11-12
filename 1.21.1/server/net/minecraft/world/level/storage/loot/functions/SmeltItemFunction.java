/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SmeltItemFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SmeltItemFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SmeltItemFunction.commonFields(instance).apply((Applicative)instance, SmeltItemFunction::new));

    private SmeltItemFunction(List<LootItemCondition> list) {
        super(list);
    }

    public LootItemFunctionType<SmeltItemFunction> getType() {
        return LootItemFunctions.FURNACE_SMELT;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ItemStack itemStack2;
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        Optional<RecipeHolder<SmeltingRecipe>> optional = lootContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemStack), lootContext.getLevel());
        if (optional.isPresent() && !(itemStack2 = optional.get().value().getResultItem(lootContext.getLevel().registryAccess())).isEmpty()) {
            return itemStack2.copyWithCount(itemStack.getCount());
        }
        LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", (Object)itemStack);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> smelted() {
        return SmeltItemFunction.simpleBuilder(SmeltItemFunction::new);
    }
}

