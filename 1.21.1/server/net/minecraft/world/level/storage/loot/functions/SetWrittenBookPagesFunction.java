/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWrittenBookPagesFunction
extends LootItemConditionalFunction {
    public static final Codec<Component> PAGE_CODEC = ComponentSerialization.CODEC.validate(component -> WrittenBookContent.CONTENT_CODEC.encodeStart((DynamicOps)JavaOps.INSTANCE, component).map(object -> component));
    public static final MapCodec<SetWrittenBookPagesFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetWrittenBookPagesFunction.commonFields(instance).and(instance.group((App)WrittenBookContent.pagesCodec(PAGE_CODEC).fieldOf("pages").forGetter(setWrittenBookPagesFunction -> setWrittenBookPagesFunction.pages), (App)ListOperation.UNLIMITED_CODEC.forGetter(setWrittenBookPagesFunction -> setWrittenBookPagesFunction.pageOperation))).apply((Applicative)instance, SetWrittenBookPagesFunction::new));
    private final List<Filterable<Component>> pages;
    private final ListOperation pageOperation;

    protected SetWrittenBookPagesFunction(List<LootItemCondition> list, List<Filterable<Component>> list2, ListOperation listOperation) {
        super(list);
        this.pages = list2;
        this.pageOperation = listOperation;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, this::apply);
        return itemStack;
    }

    @VisibleForTesting
    public WrittenBookContent apply(WrittenBookContent writtenBookContent) {
        List<Filterable<Component>> list = this.pageOperation.apply(writtenBookContent.pages(), this.pages);
        return writtenBookContent.withReplacedPages((List)list);
    }

    public LootItemFunctionType<SetWrittenBookPagesFunction> getType() {
        return LootItemFunctions.SET_WRITTEN_BOOK_PAGES;
    }
}

