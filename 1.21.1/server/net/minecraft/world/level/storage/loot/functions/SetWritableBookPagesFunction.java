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
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWritableBookPagesFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetWritableBookPagesFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetWritableBookPagesFunction.commonFields(instance).and(instance.group((App)WritableBookContent.PAGES_CODEC.fieldOf("pages").forGetter(setWritableBookPagesFunction -> setWritableBookPagesFunction.pages), (App)ListOperation.codec(100).forGetter(setWritableBookPagesFunction -> setWritableBookPagesFunction.pageOperation))).apply((Applicative)instance, SetWritableBookPagesFunction::new));
    private final List<Filterable<String>> pages;
    private final ListOperation pageOperation;

    protected SetWritableBookPagesFunction(List<LootItemCondition> list, List<Filterable<String>> list2, ListOperation listOperation) {
        super(list);
        this.pages = list2;
        this.pageOperation = listOperation;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.update(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY, this::apply);
        return itemStack;
    }

    public WritableBookContent apply(WritableBookContent writableBookContent) {
        List<Filterable<String>> list = this.pageOperation.apply(writableBookContent.pages(), this.pages, 100);
        return writableBookContent.withReplacedPages((List)list);
    }

    public LootItemFunctionType<SetWritableBookPagesFunction> getType() {
        return LootItemFunctions.SET_WRITABLE_BOOK_PAGES;
    }
}

