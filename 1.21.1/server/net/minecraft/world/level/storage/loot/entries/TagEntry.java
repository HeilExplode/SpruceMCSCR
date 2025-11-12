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
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry
extends LootPoolSingletonContainer {
    public static final MapCodec<TagEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TagKey.codec(Registries.ITEM).fieldOf("name").forGetter(tagEntry -> tagEntry.tag), (App)Codec.BOOL.fieldOf("expand").forGetter(tagEntry -> tagEntry.expand)).and(TagEntry.singletonFields(instance)).apply((Applicative)instance, TagEntry::new));
    private final TagKey<Item> tag;
    private final boolean expand;

    private TagEntry(TagKey<Item> tagKey, boolean bl, int n, int n2, List<LootItemCondition> list, List<LootItemFunction> list2) {
        super(n, n2, list, list2);
        this.tag = tagKey;
        this.expand = bl;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.TAG;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach(holder -> consumer.accept(new ItemStack((Holder<Item>)holder)));
    }

    private boolean expandTag(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.canRun(lootContext)) {
            for (final Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                consumer.accept(new LootPoolSingletonContainer.EntryBase(this){

                    @Override
                    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
                        consumer.accept(new ItemStack(holder));
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.expand) {
            return this.expandTag(lootContext, consumer);
        }
        return super.expand(lootContext, consumer);
    }

    public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> tagKey) {
        return TagEntry.simpleBuilder((n, n2, list, list2) -> new TagEntry(tagKey, false, n, n2, list, list2));
    }

    public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> tagKey) {
        return TagEntry.simpleBuilder((n, n2, list, list2) -> new TagEntry(tagKey, true, n, n2, list, list2));
    }
}

