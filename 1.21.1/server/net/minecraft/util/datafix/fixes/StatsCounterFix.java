/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1451_6;
import org.apache.commons.lang3.StringUtils;

public class StatsCounterFix
extends DataFix {
    private static final Set<String> SPECIAL_OBJECTIVE_CRITERIA = Set.of("dummy", "trigger", "deathCount", "playerKillCount", "totalKillCount", "health", "food", "air", "armor", "xp", "level", "killedByTeam.aqua", "killedByTeam.black", "killedByTeam.blue", "killedByTeam.dark_aqua", "killedByTeam.dark_blue", "killedByTeam.dark_gray", "killedByTeam.dark_green", "killedByTeam.dark_purple", "killedByTeam.dark_red", "killedByTeam.gold", "killedByTeam.gray", "killedByTeam.green", "killedByTeam.light_purple", "killedByTeam.red", "killedByTeam.white", "killedByTeam.yellow", "teamkill.aqua", "teamkill.black", "teamkill.blue", "teamkill.dark_aqua", "teamkill.dark_blue", "teamkill.dark_gray", "teamkill.dark_green", "teamkill.dark_purple", "teamkill.dark_red", "teamkill.gold", "teamkill.gray", "teamkill.green", "teamkill.light_purple", "teamkill.red", "teamkill.white", "teamkill.yellow");
    private static final Set<String> SKIP = ImmutableSet.builder().add((Object)"stat.craftItem.minecraft.spawn_egg").add((Object)"stat.useItem.minecraft.spawn_egg").add((Object)"stat.breakItem.minecraft.spawn_egg").add((Object)"stat.pickup.minecraft.spawn_egg").add((Object)"stat.drop.minecraft.spawn_egg").build();
    private static final Map<String, String> CUSTOM_MAP = ImmutableMap.builder().put((Object)"stat.leaveGame", (Object)"minecraft:leave_game").put((Object)"stat.playOneMinute", (Object)"minecraft:play_one_minute").put((Object)"stat.timeSinceDeath", (Object)"minecraft:time_since_death").put((Object)"stat.sneakTime", (Object)"minecraft:sneak_time").put((Object)"stat.walkOneCm", (Object)"minecraft:walk_one_cm").put((Object)"stat.crouchOneCm", (Object)"minecraft:crouch_one_cm").put((Object)"stat.sprintOneCm", (Object)"minecraft:sprint_one_cm").put((Object)"stat.swimOneCm", (Object)"minecraft:swim_one_cm").put((Object)"stat.fallOneCm", (Object)"minecraft:fall_one_cm").put((Object)"stat.climbOneCm", (Object)"minecraft:climb_one_cm").put((Object)"stat.flyOneCm", (Object)"minecraft:fly_one_cm").put((Object)"stat.diveOneCm", (Object)"minecraft:dive_one_cm").put((Object)"stat.minecartOneCm", (Object)"minecraft:minecart_one_cm").put((Object)"stat.boatOneCm", (Object)"minecraft:boat_one_cm").put((Object)"stat.pigOneCm", (Object)"minecraft:pig_one_cm").put((Object)"stat.horseOneCm", (Object)"minecraft:horse_one_cm").put((Object)"stat.aviateOneCm", (Object)"minecraft:aviate_one_cm").put((Object)"stat.jump", (Object)"minecraft:jump").put((Object)"stat.drop", (Object)"minecraft:drop").put((Object)"stat.damageDealt", (Object)"minecraft:damage_dealt").put((Object)"stat.damageTaken", (Object)"minecraft:damage_taken").put((Object)"stat.deaths", (Object)"minecraft:deaths").put((Object)"stat.mobKills", (Object)"minecraft:mob_kills").put((Object)"stat.animalsBred", (Object)"minecraft:animals_bred").put((Object)"stat.playerKills", (Object)"minecraft:player_kills").put((Object)"stat.fishCaught", (Object)"minecraft:fish_caught").put((Object)"stat.talkedToVillager", (Object)"minecraft:talked_to_villager").put((Object)"stat.tradedWithVillager", (Object)"minecraft:traded_with_villager").put((Object)"stat.cakeSlicesEaten", (Object)"minecraft:eat_cake_slice").put((Object)"stat.cauldronFilled", (Object)"minecraft:fill_cauldron").put((Object)"stat.cauldronUsed", (Object)"minecraft:use_cauldron").put((Object)"stat.armorCleaned", (Object)"minecraft:clean_armor").put((Object)"stat.bannerCleaned", (Object)"minecraft:clean_banner").put((Object)"stat.brewingstandInteraction", (Object)"minecraft:interact_with_brewingstand").put((Object)"stat.beaconInteraction", (Object)"minecraft:interact_with_beacon").put((Object)"stat.dropperInspected", (Object)"minecraft:inspect_dropper").put((Object)"stat.hopperInspected", (Object)"minecraft:inspect_hopper").put((Object)"stat.dispenserInspected", (Object)"minecraft:inspect_dispenser").put((Object)"stat.noteblockPlayed", (Object)"minecraft:play_noteblock").put((Object)"stat.noteblockTuned", (Object)"minecraft:tune_noteblock").put((Object)"stat.flowerPotted", (Object)"minecraft:pot_flower").put((Object)"stat.trappedChestTriggered", (Object)"minecraft:trigger_trapped_chest").put((Object)"stat.enderchestOpened", (Object)"minecraft:open_enderchest").put((Object)"stat.itemEnchanted", (Object)"minecraft:enchant_item").put((Object)"stat.recordPlayed", (Object)"minecraft:play_record").put((Object)"stat.furnaceInteraction", (Object)"minecraft:interact_with_furnace").put((Object)"stat.craftingTableInteraction", (Object)"minecraft:interact_with_crafting_table").put((Object)"stat.chestOpened", (Object)"minecraft:open_chest").put((Object)"stat.sleepInBed", (Object)"minecraft:sleep_in_bed").put((Object)"stat.shulkerBoxOpened", (Object)"minecraft:open_shulker_box").build();
    private static final String BLOCK_KEY = "stat.mineBlock";
    private static final String NEW_BLOCK_KEY = "minecraft:mined";
    private static final Map<String, String> ITEM_KEYS = ImmutableMap.builder().put((Object)"stat.craftItem", (Object)"minecraft:crafted").put((Object)"stat.useItem", (Object)"minecraft:used").put((Object)"stat.breakItem", (Object)"minecraft:broken").put((Object)"stat.pickup", (Object)"minecraft:picked_up").put((Object)"stat.drop", (Object)"minecraft:dropped").build();
    private static final Map<String, String> ENTITY_KEYS = ImmutableMap.builder().put((Object)"stat.entityKilledBy", (Object)"minecraft:killed_by").put((Object)"stat.killEntity", (Object)"minecraft:killed").build();
    private static final Map<String, String> ENTITIES = ImmutableMap.builder().put((Object)"Bat", (Object)"minecraft:bat").put((Object)"Blaze", (Object)"minecraft:blaze").put((Object)"CaveSpider", (Object)"minecraft:cave_spider").put((Object)"Chicken", (Object)"minecraft:chicken").put((Object)"Cow", (Object)"minecraft:cow").put((Object)"Creeper", (Object)"minecraft:creeper").put((Object)"Donkey", (Object)"minecraft:donkey").put((Object)"ElderGuardian", (Object)"minecraft:elder_guardian").put((Object)"Enderman", (Object)"minecraft:enderman").put((Object)"Endermite", (Object)"minecraft:endermite").put((Object)"EvocationIllager", (Object)"minecraft:evocation_illager").put((Object)"Ghast", (Object)"minecraft:ghast").put((Object)"Guardian", (Object)"minecraft:guardian").put((Object)"Horse", (Object)"minecraft:horse").put((Object)"Husk", (Object)"minecraft:husk").put((Object)"Llama", (Object)"minecraft:llama").put((Object)"LavaSlime", (Object)"minecraft:magma_cube").put((Object)"MushroomCow", (Object)"minecraft:mooshroom").put((Object)"Mule", (Object)"minecraft:mule").put((Object)"Ozelot", (Object)"minecraft:ocelot").put((Object)"Parrot", (Object)"minecraft:parrot").put((Object)"Pig", (Object)"minecraft:pig").put((Object)"PolarBear", (Object)"minecraft:polar_bear").put((Object)"Rabbit", (Object)"minecraft:rabbit").put((Object)"Sheep", (Object)"minecraft:sheep").put((Object)"Shulker", (Object)"minecraft:shulker").put((Object)"Silverfish", (Object)"minecraft:silverfish").put((Object)"SkeletonHorse", (Object)"minecraft:skeleton_horse").put((Object)"Skeleton", (Object)"minecraft:skeleton").put((Object)"Slime", (Object)"minecraft:slime").put((Object)"Spider", (Object)"minecraft:spider").put((Object)"Squid", (Object)"minecraft:squid").put((Object)"Stray", (Object)"minecraft:stray").put((Object)"Vex", (Object)"minecraft:vex").put((Object)"Villager", (Object)"minecraft:villager").put((Object)"VindicationIllager", (Object)"minecraft:vindication_illager").put((Object)"Witch", (Object)"minecraft:witch").put((Object)"WitherSkeleton", (Object)"minecraft:wither_skeleton").put((Object)"Wolf", (Object)"minecraft:wolf").put((Object)"ZombieHorse", (Object)"minecraft:zombie_horse").put((Object)"PigZombie", (Object)"minecraft:zombie_pigman").put((Object)"ZombieVillager", (Object)"minecraft:zombie_villager").put((Object)"Zombie", (Object)"minecraft:zombie").build();
    private static final String NEW_CUSTOM_KEY = "minecraft:custom";

    public StatsCounterFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Nullable
    private static StatType unpackLegacyKey(String string) {
        if (SKIP.contains(string)) {
            return null;
        }
        String string2 = CUSTOM_MAP.get(string);
        if (string2 != null) {
            return new StatType(NEW_CUSTOM_KEY, string2);
        }
        int n = StringUtils.ordinalIndexOf((CharSequence)string, (CharSequence)".", (int)2);
        if (n < 0) {
            return null;
        }
        String string3 = string.substring(0, n);
        if (BLOCK_KEY.equals(string3)) {
            String string4 = StatsCounterFix.upgradeBlock(string.substring(n + 1).replace('.', ':'));
            return new StatType(NEW_BLOCK_KEY, string4);
        }
        String string5 = ITEM_KEYS.get(string3);
        if (string5 != null) {
            String string6 = string.substring(n + 1).replace('.', ':');
            String string7 = StatsCounterFix.upgradeItem(string6);
            String string8 = string7 == null ? string6 : string7;
            return new StatType(string5, string8);
        }
        String string9 = ENTITY_KEYS.get(string3);
        if (string9 != null) {
            String string10 = string.substring(n + 1).replace('.', ':');
            String string11 = ENTITIES.getOrDefault(string10, string10);
            return new StatType(string9, string11);
        }
        return null;
    }

    public TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.makeStatFixer(), (TypeRewriteRule)this.makeObjectiveFixer());
    }

    private TypeRewriteRule makeStatFixer() {
        Type type = this.getInputSchema().getType(References.STATS);
        Type type2 = this.getOutputSchema().getType(References.STATS);
        return this.fixTypeEverywhereTyped("StatsCounterFix", type, type2, typed -> {
            Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
            HashMap hashMap = Maps.newHashMap();
            Optional optional = dynamic.getMapValues().result();
            if (optional.isPresent()) {
                for (Map.Entry entry : ((Map)optional.get()).entrySet()) {
                    String string;
                    StatType statType;
                    if (!((Dynamic)entry.getValue()).asNumber().result().isPresent() || (statType = StatsCounterFix.unpackLegacyKey(string = ((Dynamic)entry.getKey()).asString(""))) == null) continue;
                    Dynamic dynamic3 = dynamic.createString(statType.type());
                    Dynamic dynamic4 = hashMap.computeIfAbsent(dynamic3, dynamic2 -> dynamic.emptyMap());
                    hashMap.put(dynamic3, dynamic4.set(statType.typeKey(), (Dynamic)entry.getValue()));
                }
            }
            return Util.readTypedOrThrow(type2, dynamic.emptyMap().set("stats", dynamic.createMap((Map)hashMap)));
        });
    }

    private TypeRewriteRule makeObjectiveFixer() {
        Type type = this.getInputSchema().getType(References.OBJECTIVE);
        Type type2 = this.getOutputSchema().getType(References.OBJECTIVE);
        return this.fixTypeEverywhereTyped("ObjectiveStatFix", type, type2, typed -> {
            Dynamic dynamic2 = (Dynamic)typed.get(DSL.remainderFinder());
            Dynamic dynamic3 = dynamic2.update("CriteriaName", dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.asString().result().map(string -> {
                if (SPECIAL_OBJECTIVE_CRITERIA.contains(string)) {
                    return string;
                }
                StatType statType = StatsCounterFix.unpackLegacyKey(string);
                if (statType == null) {
                    return "dummy";
                }
                return V1451_6.packNamespacedWithDot(statType.type) + ":" + V1451_6.packNamespacedWithDot(statType.typeKey);
            }).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)), (Object)dynamic));
            return Util.readTypedOrThrow(type2, dynamic3);
        });
    }

    @Nullable
    private static String upgradeItem(String string) {
        return ItemStackTheFlatteningFix.updateItem(string, 0);
    }

    private static String upgradeBlock(String string) {
        return BlockStateData.upgradeBlock(string);
    }

    static final class StatType
    extends Record {
        final String type;
        final String typeKey;

        StatType(String string, String string2) {
            this.type = string;
            this.typeKey = string2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{StatType.class, "type;typeKey", "type", "typeKey"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StatType.class, "type;typeKey", "type", "typeKey"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StatType.class, "type;typeKey", "type", "typeKey"}, this, object);
        }

        public String type() {
            return this.type;
        }

        public String typeKey() {
            return this.typeKey;
        }
    }
}

