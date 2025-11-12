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
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBookCoverFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetBookCoverFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetBookCoverFunction.commonFields(instance).and(instance.group((App)Filterable.codec(Codec.string((int)0, (int)32)).optionalFieldOf("title").forGetter(setBookCoverFunction -> setBookCoverFunction.title), (App)Codec.STRING.optionalFieldOf("author").forGetter(setBookCoverFunction -> setBookCoverFunction.author), (App)ExtraCodecs.intRange(0, 3).optionalFieldOf("generation").forGetter(setBookCoverFunction -> setBookCoverFunction.generation))).apply((Applicative)instance, SetBookCoverFunction::new));
    private final Optional<String> author;
    private final Optional<Filterable<String>> title;
    private final Optional<Integer> generation;

    public SetBookCoverFunction(List<LootItemCondition> list, Optional<Filterable<String>> optional, Optional<String> optional2, Optional<Integer> optional3) {
        super(list);
        this.author = optional2;
        this.title = optional;
        this.generation = optional3;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, this::apply);
        return itemStack;
    }

    private WrittenBookContent apply(WrittenBookContent writtenBookContent) {
        return new WrittenBookContent(this.title.orElseGet(writtenBookContent::title), this.author.orElseGet(writtenBookContent::author), this.generation.orElseGet(writtenBookContent::generation), writtenBookContent.pages(), writtenBookContent.resolved());
    }

    public LootItemFunctionType<SetBookCoverFunction> getType() {
        return LootItemFunctions.SET_BOOK_COVER;
    }
}

