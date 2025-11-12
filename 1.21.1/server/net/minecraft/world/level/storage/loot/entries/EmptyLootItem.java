/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem
extends LootPoolSingletonContainer {
    public static final MapCodec<EmptyLootItem> CODEC = RecordCodecBuilder.mapCodec(instance -> EmptyLootItem.singletonFields(instance).apply((Applicative)instance, EmptyLootItem::new));

    private EmptyLootItem(int n, int n2, List<LootItemCondition> list, List<LootItemFunction> list2) {
        super(n, n2, list, list2);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.EMPTY;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
    }

    public static LootPoolSingletonContainer.Builder<?> emptyItem() {
        return EmptyLootItem.simpleBuilder(EmptyLootItem::new);
    }
}

