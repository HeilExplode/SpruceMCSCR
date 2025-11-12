/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem
extends LootPoolSingletonContainer {
    public static final MapCodec<LootItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("name").forGetter(lootItem -> lootItem.item)).and(LootItem.singletonFields(instance)).apply((Applicative)instance, LootItem::new));
    private final Holder<Item> item;

    private LootItem(Holder<Item> holder, int n, int n2, List<LootItemCondition> list, List<LootItemFunction> list2) {
        super(n, n2, list, list2);
        this.item = holder;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        consumer.accept(new ItemStack(this.item));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike itemLike) {
        return LootItem.simpleBuilder((n, n2, list, list2) -> new LootItem(itemLike.asItem().builtInRegistryHolder(), n, n2, list, list2));
    }
}

