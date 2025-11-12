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
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction
extends LootItemConditionalFunction {
    public static final MapCodec<FilteredFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> FilteredFunction.commonFields(instance).and(instance.group((App)ItemPredicate.CODEC.fieldOf("item_filter").forGetter(filteredFunction -> filteredFunction.filter), (App)LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter(filteredFunction -> filteredFunction.modifier))).apply((Applicative)instance, FilteredFunction::new));
    private final ItemPredicate filter;
    private final LootItemFunction modifier;

    private FilteredFunction(List<LootItemCondition> list, ItemPredicate itemPredicate, LootItemFunction lootItemFunction) {
        super(list);
        this.filter = itemPredicate;
        this.modifier = lootItemFunction;
    }

    public LootItemFunctionType<FilteredFunction> getType() {
        return LootItemFunctions.FILTERED;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (this.filter.test(itemStack)) {
            return (ItemStack)this.modifier.apply(itemStack, lootContext);
        }
        return itemStack;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        this.modifier.validate(validationContext.forChild(".modifier"));
    }
}

