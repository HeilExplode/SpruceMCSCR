/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public record LootDataType<T>(ResourceKey<Registry<T>> registryKey, Codec<T> codec, Validator<T> validator) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<LootItemCondition>(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC, LootDataType.createSimpleValidator());
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<LootItemFunction>(Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC, LootDataType.createSimpleValidator());
    public static final LootDataType<LootTable> TABLE = new LootDataType<LootTable>(Registries.LOOT_TABLE, LootTable.DIRECT_CODEC, LootDataType.createLootTableValidator());

    public void runValidation(ValidationContext validationContext, ResourceKey<T> resourceKey, T t) {
        this.validator.run(validationContext, resourceKey, t);
    }

    public <V> Optional<T> deserialize(ResourceLocation resourceLocation, DynamicOps<V> dynamicOps, V v) {
        DataResult dataResult = this.codec.parse(dynamicOps, v);
        dataResult.error().ifPresent(error -> LOGGER.error("Couldn't parse element {}/{} - {}", new Object[]{this.registryKey.location(), resourceLocation, error.message()}));
        return dataResult.result();
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(PREDICATE, MODIFIER, TABLE);
    }

    private static <T extends LootContextUser> Validator<T> createSimpleValidator() {
        return (validationContext, resourceKey, lootContextUser) -> lootContextUser.validate(validationContext.enterElement("{" + String.valueOf(resourceKey.registry()) + "/" + String.valueOf(resourceKey.location()) + "}", resourceKey));
    }

    private static Validator<LootTable> createLootTableValidator() {
        return (validationContext, resourceKey, lootTable) -> lootTable.validate(validationContext.setParams(lootTable.getParamSet()).enterElement("{" + String.valueOf(resourceKey.registry()) + "/" + String.valueOf(resourceKey.location()) + "}", resourceKey));
    }

    @FunctionalInterface
    public static interface Validator<T> {
        public void run(ValidationContext var1, ResourceKey<T> var2, T var3);
    }
}

