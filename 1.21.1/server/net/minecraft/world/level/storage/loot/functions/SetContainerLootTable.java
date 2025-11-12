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
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable
extends LootItemConditionalFunction {
    public static final MapCodec<SetContainerLootTable> CODEC = RecordCodecBuilder.mapCodec(instance -> SetContainerLootTable.commonFields(instance).and(instance.group((App)ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("name").forGetter(setContainerLootTable -> setContainerLootTable.name), (App)Codec.LONG.optionalFieldOf("seed", (Object)0L).forGetter(setContainerLootTable -> setContainerLootTable.seed), (App)BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter(setContainerLootTable -> setContainerLootTable.type))).apply((Applicative)instance, SetContainerLootTable::new));
    private final ResourceKey<LootTable> name;
    private final long seed;
    private final Holder<BlockEntityType<?>> type;

    private SetContainerLootTable(List<LootItemCondition> list, ResourceKey<LootTable> resourceKey, long l, Holder<BlockEntityType<?>> holder) {
        super(list);
        this.name = resourceKey;
        this.seed = l;
        this.type = holder;
    }

    public LootItemFunctionType<SetContainerLootTable> getType() {
        return LootItemFunctions.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        itemStack.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.name, this.seed));
        return itemStack;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        if (!validationContext.allowsReferences()) {
            validationContext.reportProblem("Uses reference to " + String.valueOf(this.name.location()) + ", but references are not allowed");
            return;
        }
        if (validationContext.resolver().get(Registries.LOOT_TABLE, this.name).isEmpty()) {
            validationContext.reportProblem("Missing loot table used for container: " + String.valueOf(this.name.location()));
        }
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceKey<LootTable> resourceKey) {
        return SetContainerLootTable.simpleBuilder(list -> new SetContainerLootTable((List<LootItemCondition>)list, resourceKey, 0L, (Holder<BlockEntityType<?>>)blockEntityType.builtInRegistryHolder()));
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceKey<LootTable> resourceKey, long l) {
        return SetContainerLootTable.simpleBuilder(list -> new SetContainerLootTable((List<LootItemCondition>)list, resourceKey, l, (Holder<BlockEntityType<?>>)blockEntityType.builtInRegistryHolder()));
    }
}

