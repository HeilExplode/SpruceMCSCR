/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class NestedLootTable
extends LootPoolSingletonContainer {
    public static final MapCodec<NestedLootTable> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.either(ResourceKey.codec(Registries.LOOT_TABLE), LootTable.DIRECT_CODEC).fieldOf("value").forGetter(nestedLootTable -> nestedLootTable.contents)).and(NestedLootTable.singletonFields(instance)).apply((Applicative)instance, NestedLootTable::new));
    private final Either<ResourceKey<LootTable>, LootTable> contents;

    private NestedLootTable(Either<ResourceKey<LootTable>, LootTable> either, int n, int n2, List<LootItemCondition> list, List<LootItemFunction> list2) {
        super(n, n2, list, list2);
        this.contents = either;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.LOOT_TABLE;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        ((LootTable)this.contents.map(resourceKey -> lootContext.getResolver().get(Registries.LOOT_TABLE, resourceKey).map(Holder::value).orElse(LootTable.EMPTY), lootTable -> lootTable)).getRandomItemsRaw(lootContext, consumer);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        Optional optional = this.contents.left();
        if (optional.isPresent()) {
            ResourceKey resourceKey2 = (ResourceKey)optional.get();
            if (!validationContext.allowsReferences()) {
                validationContext.reportProblem("Uses reference to " + String.valueOf(resourceKey2.location()) + ", but references are not allowed");
                return;
            }
            if (validationContext.hasVisitedElement(resourceKey2)) {
                validationContext.reportProblem("Table " + String.valueOf(resourceKey2.location()) + " is recursively called");
                return;
            }
        }
        super.validate(validationContext);
        this.contents.ifLeft(resourceKey -> validationContext.resolver().get(Registries.LOOT_TABLE, resourceKey).ifPresentOrElse(reference -> ((LootTable)reference.value()).validate(validationContext.enterElement("->{" + String.valueOf(resourceKey.location()) + "}", (ResourceKey<?>)resourceKey)), () -> validationContext.reportProblem("Unknown loot table called " + String.valueOf(resourceKey.location())))).ifRight(lootTable -> lootTable.validate(validationContext.forChild("->{inline}")));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceKey<LootTable> resourceKey) {
        return NestedLootTable.simpleBuilder((n, n2, list, list2) -> new NestedLootTable((Either<ResourceKey<LootTable>, LootTable>)Either.left((Object)resourceKey), n, n2, list, list2));
    }

    public static LootPoolSingletonContainer.Builder<?> inlineLootTable(LootTable lootTable) {
        return NestedLootTable.simpleBuilder((n, n2, list, list2) -> new NestedLootTable((Either<ResourceKey<LootTable>, LootTable>)Either.right((Object)lootTable), n, n2, list, list2));
    }
}

