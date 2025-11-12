/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootContextParamSets {
    private static final BiMap<ResourceLocation, LootContextParamSet> REGISTRY = HashBiMap.create();
    public static final Codec<LootContextParamSet> CODEC = ResourceLocation.CODEC.comapFlatMap(resourceLocation -> Optional.ofNullable((LootContextParamSet)REGISTRY.get(resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No parameter set exists with id: '" + String.valueOf(resourceLocation) + "'")), arg_0 -> REGISTRY.inverse().get(arg_0));
    public static final LootContextParamSet EMPTY = LootContextParamSets.register("empty", builder -> {});
    public static final LootContextParamSet CHEST = LootContextParamSets.register("chest", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet COMMAND = LootContextParamSets.register("command", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet SELECTOR = LootContextParamSets.register("selector", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet FISHING = LootContextParamSets.register("fishing", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet ENTITY = LootContextParamSets.register("entity", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.DAMAGE_SOURCE).optional(LootContextParams.ATTACKING_ENTITY).optional(LootContextParams.DIRECT_ATTACKING_ENTITY).optional(LootContextParams.LAST_DAMAGE_PLAYER));
    public static final LootContextParamSet EQUIPMENT = LootContextParamSets.register("equipment", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet ARCHAEOLOGY = LootContextParamSets.register("archaeology", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet GIFT = LootContextParamSets.register("gift", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet PIGLIN_BARTER = LootContextParamSets.register("barter", builder -> builder.required(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet VAULT = LootContextParamSets.register("vault", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet ADVANCEMENT_REWARD = LootContextParamSets.register("advancement_reward", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN));
    public static final LootContextParamSet ADVANCEMENT_ENTITY = LootContextParamSets.register("advancement_entity", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN));
    public static final LootContextParamSet ADVANCEMENT_LOCATION = LootContextParamSets.register("advancement_location", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).required(LootContextParams.BLOCK_STATE));
    public static final LootContextParamSet BLOCK_USE = LootContextParamSets.register("block_use", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE));
    public static final LootContextParamSet ALL_PARAMS = LootContextParamSets.register("generic", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.LAST_DAMAGE_PLAYER).required(LootContextParams.DAMAGE_SOURCE).required(LootContextParams.ATTACKING_ENTITY).required(LootContextParams.DIRECT_ATTACKING_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE).required(LootContextParams.BLOCK_ENTITY).required(LootContextParams.TOOL).required(LootContextParams.EXPLOSION_RADIUS));
    public static final LootContextParamSet BLOCK = LootContextParamSets.register("block", builder -> builder.required(LootContextParams.BLOCK_STATE).required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.BLOCK_ENTITY).optional(LootContextParams.EXPLOSION_RADIUS));
    public static final LootContextParamSet SHEARING = LootContextParamSets.register("shearing", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet ENCHANTED_DAMAGE = LootContextParamSets.register("enchanted_damage", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN).required(LootContextParams.DAMAGE_SOURCE).optional(LootContextParams.DIRECT_ATTACKING_ENTITY).optional(LootContextParams.ATTACKING_ENTITY));
    public static final LootContextParamSet ENCHANTED_ITEM = LootContextParamSets.register("enchanted_item", builder -> builder.required(LootContextParams.TOOL).required(LootContextParams.ENCHANTMENT_LEVEL));
    public static final LootContextParamSet ENCHANTED_LOCATION = LootContextParamSets.register("enchanted_location", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN).required(LootContextParams.ENCHANTMENT_ACTIVE));
    public static final LootContextParamSet ENCHANTED_ENTITY = LootContextParamSets.register("enchanted_entity", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN));
    public static final LootContextParamSet HIT_BLOCK = LootContextParamSets.register("hit_block", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE));

    private static LootContextParamSet register(String string, Consumer<LootContextParamSet.Builder> consumer) {
        LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
        consumer.accept(builder);
        LootContextParamSet lootContextParamSet = builder.build();
        ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace(string);
        LootContextParamSet lootContextParamSet2 = (LootContextParamSet)REGISTRY.put((Object)resourceLocation, (Object)lootContextParamSet);
        if (lootContextParamSet2 != null) {
            throw new IllegalStateException("Loot table parameter set " + String.valueOf(resourceLocation) + " is already registered");
        }
        return lootContextParamSet;
    }
}

