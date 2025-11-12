/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.EnchantmentActiveCheck;
import net.minecraft.world.level.storage.loot.predicates.EntityHasScoreCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

public class LootItemConditions {
    public static final LootItemConditionType INVERTED = LootItemConditions.register("inverted", InvertedLootItemCondition.CODEC);
    public static final LootItemConditionType ANY_OF = LootItemConditions.register("any_of", AnyOfCondition.CODEC);
    public static final LootItemConditionType ALL_OF = LootItemConditions.register("all_of", AllOfCondition.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE = LootItemConditions.register("random_chance", LootItemRandomChanceCondition.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE_WITH_ENCHANTED_BONUS = LootItemConditions.register("random_chance_with_enchanted_bonus", LootItemRandomChanceWithEnchantedBonusCondition.CODEC);
    public static final LootItemConditionType ENTITY_PROPERTIES = LootItemConditions.register("entity_properties", LootItemEntityPropertyCondition.CODEC);
    public static final LootItemConditionType KILLED_BY_PLAYER = LootItemConditions.register("killed_by_player", LootItemKilledByPlayerCondition.CODEC);
    public static final LootItemConditionType ENTITY_SCORES = LootItemConditions.register("entity_scores", EntityHasScoreCondition.CODEC);
    public static final LootItemConditionType BLOCK_STATE_PROPERTY = LootItemConditions.register("block_state_property", LootItemBlockStatePropertyCondition.CODEC);
    public static final LootItemConditionType MATCH_TOOL = LootItemConditions.register("match_tool", MatchTool.CODEC);
    public static final LootItemConditionType TABLE_BONUS = LootItemConditions.register("table_bonus", BonusLevelTableCondition.CODEC);
    public static final LootItemConditionType SURVIVES_EXPLOSION = LootItemConditions.register("survives_explosion", ExplosionCondition.CODEC);
    public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = LootItemConditions.register("damage_source_properties", DamageSourceCondition.CODEC);
    public static final LootItemConditionType LOCATION_CHECK = LootItemConditions.register("location_check", LocationCheck.CODEC);
    public static final LootItemConditionType WEATHER_CHECK = LootItemConditions.register("weather_check", WeatherCheck.CODEC);
    public static final LootItemConditionType REFERENCE = LootItemConditions.register("reference", ConditionReference.CODEC);
    public static final LootItemConditionType TIME_CHECK = LootItemConditions.register("time_check", TimeCheck.CODEC);
    public static final LootItemConditionType VALUE_CHECK = LootItemConditions.register("value_check", ValueCheckCondition.CODEC);
    public static final LootItemConditionType ENCHANTMENT_ACTIVE_CHECK = LootItemConditions.register("enchantment_active_check", EnchantmentActiveCheck.CODEC);

    private static LootItemConditionType register(String string, MapCodec<? extends LootItemCondition> mapCodec) {
        return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, ResourceLocation.withDefaultNamespace(string), new LootItemConditionType(mapCodec));
    }
}

