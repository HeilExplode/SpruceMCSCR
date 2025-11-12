/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.entries.TagEntry;

public class LootPoolEntries {
    public static final Codec<LootPoolEntryContainer> CODEC = BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.byNameCodec().dispatch(LootPoolEntryContainer::getType, LootPoolEntryType::codec);
    public static final LootPoolEntryType EMPTY = LootPoolEntries.register("empty", EmptyLootItem.CODEC);
    public static final LootPoolEntryType ITEM = LootPoolEntries.register("item", LootItem.CODEC);
    public static final LootPoolEntryType LOOT_TABLE = LootPoolEntries.register("loot_table", NestedLootTable.CODEC);
    public static final LootPoolEntryType DYNAMIC = LootPoolEntries.register("dynamic", DynamicLoot.CODEC);
    public static final LootPoolEntryType TAG = LootPoolEntries.register("tag", TagEntry.CODEC);
    public static final LootPoolEntryType ALTERNATIVES = LootPoolEntries.register("alternatives", AlternativesEntry.CODEC);
    public static final LootPoolEntryType SEQUENCE = LootPoolEntries.register("sequence", SequentialEntry.CODEC);
    public static final LootPoolEntryType GROUP = LootPoolEntries.register("group", EntryGroup.CODEC);

    private static LootPoolEntryType register(String string, MapCodec<? extends LootPoolEntryContainer> mapCodec) {
        return Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, ResourceLocation.withDefaultNamespace(string), new LootPoolEntryType(mapCodec));
    }
}

