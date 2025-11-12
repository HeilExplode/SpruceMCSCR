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
import java.util.function.BiFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;

public class SequenceFunction
implements LootItemFunction {
    public static final MapCodec<SequenceFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootItemFunctions.TYPED_CODEC.listOf().fieldOf("functions").forGetter(sequenceFunction -> sequenceFunction.functions)).apply((Applicative)instance, SequenceFunction::new));
    public static final Codec<SequenceFunction> INLINE_CODEC = LootItemFunctions.TYPED_CODEC.listOf().xmap(SequenceFunction::new, sequenceFunction -> sequenceFunction.functions);
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    private SequenceFunction(List<LootItemFunction> list) {
        this.functions = list;
        this.compositeFunction = LootItemFunctions.compose(list);
    }

    public static SequenceFunction of(List<LootItemFunction> list) {
        return new SequenceFunction(List.copyOf(list));
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return this.compositeFunction.apply(itemStack, lootContext);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        LootItemFunction.super.validate(validationContext);
        for (int i = 0; i < this.functions.size(); ++i) {
            this.functions.get(i).validate(validationContext.forChild(".function[" + i + "]"));
        }
    }

    public LootItemFunctionType<SequenceFunction> getType() {
        return LootItemFunctions.SEQUENCE;
    }

    @Override
    public /* synthetic */ Object apply(Object object, Object object2) {
        return this.apply((ItemStack)object, (LootContext)object2);
    }
}

