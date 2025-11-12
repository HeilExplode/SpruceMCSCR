/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.OptionalDynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.BannerPatternFormatFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackComponentizationFix
extends DataFix {
    private static final int HIDE_ENCHANTMENTS = 1;
    private static final int HIDE_MODIFIERS = 2;
    private static final int HIDE_UNBREAKABLE = 4;
    private static final int HIDE_CAN_DESTROY = 8;
    private static final int HIDE_CAN_PLACE = 16;
    private static final int HIDE_ADDITIONAL = 32;
    private static final int HIDE_DYE = 64;
    private static final int HIDE_UPGRADES = 128;
    private static final Set<String> POTION_HOLDER_IDS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");
    private static final Set<String> BUCKETED_MOB_IDS = Set.of("minecraft:pufferfish_bucket", "minecraft:salmon_bucket", "minecraft:cod_bucket", "minecraft:tropical_fish_bucket", "minecraft:axolotl_bucket", "minecraft:tadpole_bucket");
    private static final List<String> BUCKETED_MOB_TAGS = List.of("NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag");
    private static final Set<String> BOOLEAN_BLOCK_STATE_PROPERTIES = Set.of("attached", "bottom", "conditional", "disarmed", "drag", "enabled", "extended", "eye", "falling", "hanging", "has_bottle_0", "has_bottle_1", "has_bottle_2", "has_record", "has_book", "inverted", "in_wall", "lit", "locked", "occupied", "open", "persistent", "powered", "short", "signal_fire", "snowy", "triggered", "unstable", "waterlogged", "berries", "bloom", "shrieking", "can_summon", "up", "down", "north", "east", "south", "west", "slot_0_occupied", "slot_1_occupied", "slot_2_occupied", "slot_3_occupied", "slot_4_occupied", "slot_5_occupied", "cracked", "crafting");
    private static final Splitter PROPERTY_SPLITTER = Splitter.on((char)',');

    public ItemStackComponentizationFix(Schema schema) {
        super(schema, true);
    }

    private static void fixItemStack(ItemStackData itemStackData, Dynamic<?> dynamic3) {
        Object object;
        Object object2;
        int n = itemStackData.removeTag("HideFlags").asInt(0);
        itemStackData.moveTagToComponent("Damage", "minecraft:damage", dynamic3.createInt(0));
        itemStackData.moveTagToComponent("RepairCost", "minecraft:repair_cost", dynamic3.createInt(0));
        itemStackData.moveTagToComponent("CustomModelData", "minecraft:custom_model_data");
        itemStackData.removeTag("BlockStateTag").result().ifPresent(dynamic -> itemStackData.setComponent("minecraft:block_state", ItemStackComponentizationFix.fixBlockStateTag(dynamic)));
        itemStackData.moveTagToComponent("EntityTag", "minecraft:entity_data");
        itemStackData.fixSubTag("BlockEntityTag", false, dynamic -> {
            String string = NamespacedSchema.ensureNamespaced(dynamic.get("id").asString(""));
            Dynamic dynamic2 = (dynamic = ItemStackComponentizationFix.fixBlockEntityTag(itemStackData, dynamic, string)).remove("id");
            if (dynamic2.equals((Object)dynamic.emptyMap())) {
                return dynamic2;
            }
            return dynamic;
        });
        itemStackData.moveTagToComponent("BlockEntityTag", "minecraft:block_entity_data");
        if (itemStackData.removeTag("Unbreakable").asBoolean(false)) {
            object2 = dynamic3.emptyMap();
            if ((n & 4) != 0) {
                object2 = object2.set("show_in_tooltip", dynamic3.createBoolean(false));
            }
            itemStackData.setComponent("minecraft:unbreakable", (Dynamic<?>)object2);
        }
        ItemStackComponentizationFix.fixEnchantments(itemStackData, dynamic3, "Enchantments", "minecraft:enchantments", (n & 1) != 0);
        if (itemStackData.is("minecraft:enchanted_book")) {
            ItemStackComponentizationFix.fixEnchantments(itemStackData, dynamic3, "StoredEnchantments", "minecraft:stored_enchantments", (n & 0x20) != 0);
        }
        itemStackData.fixSubTag("display", false, dynamic -> ItemStackComponentizationFix.fixDisplay(itemStackData, dynamic, n));
        ItemStackComponentizationFix.fixAdventureModeChecks(itemStackData, dynamic3, n);
        ItemStackComponentizationFix.fixAttributeModifiers(itemStackData, dynamic3, n);
        object2 = itemStackData.removeTag("Trim").result();
        if (((Optional)object2).isPresent()) {
            object = (Dynamic)((Optional)object2).get();
            if ((n & 0x80) != 0) {
                object = object.set("show_in_tooltip", object.createBoolean(false));
            }
            itemStackData.setComponent("minecraft:trim", (Dynamic<?>)object);
        }
        if ((n & 0x20) != 0) {
            itemStackData.setComponent("minecraft:hide_additional_tooltip", dynamic3.emptyMap());
        }
        if (itemStackData.is("minecraft:crossbow")) {
            itemStackData.removeTag("Charged");
            itemStackData.moveTagToComponent("ChargedProjectiles", "minecraft:charged_projectiles", dynamic3.createList(Stream.empty()));
        }
        if (itemStackData.is("minecraft:bundle")) {
            itemStackData.moveTagToComponent("Items", "minecraft:bundle_contents", dynamic3.createList(Stream.empty()));
        }
        if (itemStackData.is("minecraft:filled_map")) {
            itemStackData.moveTagToComponent("map", "minecraft:map_id");
            object = itemStackData.removeTag("Decorations").asStream().map(ItemStackComponentizationFix::fixMapDecoration).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (dynamic, dynamic2) -> dynamic));
            if (!object.isEmpty()) {
                itemStackData.setComponent("minecraft:map_decorations", dynamic3.createMap((Map)object));
            }
        }
        if (itemStackData.is(POTION_HOLDER_IDS)) {
            ItemStackComponentizationFix.fixPotionContents(itemStackData, dynamic3);
        }
        if (itemStackData.is("minecraft:writable_book")) {
            ItemStackComponentizationFix.fixWritableBook(itemStackData, dynamic3);
        }
        if (itemStackData.is("minecraft:written_book")) {
            ItemStackComponentizationFix.fixWrittenBook(itemStackData, dynamic3);
        }
        if (itemStackData.is("minecraft:suspicious_stew")) {
            itemStackData.moveTagToComponent("effects", "minecraft:suspicious_stew_effects");
        }
        if (itemStackData.is("minecraft:debug_stick")) {
            itemStackData.moveTagToComponent("DebugProperty", "minecraft:debug_stick_state");
        }
        if (itemStackData.is(BUCKETED_MOB_IDS)) {
            ItemStackComponentizationFix.fixBucketedMobData(itemStackData, dynamic3);
        }
        if (itemStackData.is("minecraft:goat_horn")) {
            itemStackData.moveTagToComponent("instrument", "minecraft:instrument");
        }
        if (itemStackData.is("minecraft:knowledge_book")) {
            itemStackData.moveTagToComponent("Recipes", "minecraft:recipes");
        }
        if (itemStackData.is("minecraft:compass")) {
            ItemStackComponentizationFix.fixLodestoneTracker(itemStackData, dynamic3);
        }
        if (itemStackData.is("minecraft:firework_rocket")) {
            ItemStackComponentizationFix.fixFireworkRocket(itemStackData);
        }
        if (itemStackData.is("minecraft:firework_star")) {
            ItemStackComponentizationFix.fixFireworkStar(itemStackData);
        }
        if (itemStackData.is("minecraft:player_head")) {
            itemStackData.removeTag("SkullOwner").result().ifPresent(dynamic -> itemStackData.setComponent("minecraft:profile", ItemStackComponentizationFix.fixProfile(dynamic)));
        }
    }

    private static Dynamic<?> fixBlockStateTag(Dynamic<?> dynamic) {
        return (Dynamic)DataFixUtils.orElse(dynamic.asMapOpt().result().map(stream -> stream.collect(Collectors.toMap(Pair::getFirst, pair -> {
            Optional optional;
            String string = ((Dynamic)pair.getFirst()).asString("");
            Dynamic dynamic = (Dynamic)pair.getSecond();
            if (BOOLEAN_BLOCK_STATE_PROPERTIES.contains(string) && (optional = dynamic.asBoolean().result()).isPresent()) {
                return dynamic.createString(String.valueOf(optional.get()));
            }
            optional = dynamic.asNumber().result();
            if (optional.isPresent()) {
                return dynamic.createString(((Number)optional.get()).toString());
            }
            return dynamic;
        }))).map(arg_0 -> dynamic.createMap(arg_0)), dynamic);
    }

    private static Dynamic<?> fixDisplay(ItemStackData itemStackData, Dynamic<?> dynamic, int n) {
        Optional optional;
        boolean bl;
        itemStackData.setComponent("minecraft:custom_name", dynamic.get("Name"));
        itemStackData.setComponent("minecraft:lore", dynamic.get("Lore"));
        Optional<Integer> optional2 = dynamic.get("color").asNumber().result().map(Number::intValue);
        boolean bl2 = bl = (n & 0x40) != 0;
        if (optional2.isPresent() || bl) {
            optional = dynamic.emptyMap().set("rgb", dynamic.createInt(optional2.orElse(10511680).intValue()));
            if (bl) {
                optional = optional.set("show_in_tooltip", dynamic.createBoolean(false));
            }
            itemStackData.setComponent("minecraft:dyed_color", (Dynamic<?>)optional);
        }
        if ((optional = dynamic.get("LocName").asString().result()).isPresent()) {
            itemStackData.setComponent("minecraft:item_name", ComponentDataFixUtils.createTranslatableComponent(dynamic.getOps(), (String)optional.get()));
        }
        if (itemStackData.is("minecraft:filled_map")) {
            itemStackData.setComponent("minecraft:map_color", dynamic.get("MapColor"));
            dynamic = dynamic.remove("MapColor");
        }
        return dynamic.remove("Name").remove("Lore").remove("color").remove("LocName");
    }

    private static <T> Dynamic<T> fixBlockEntityTag(ItemStackData itemStackData, Dynamic<T> dynamic2, String string) {
        itemStackData.setComponent("minecraft:lock", dynamic2.get("Lock"));
        dynamic2 = dynamic2.remove("Lock");
        Optional optional = dynamic2.get("LootTable").result();
        if (optional.isPresent()) {
            String string2 = dynamic2.emptyMap().set("loot_table", (Dynamic)optional.get());
            long l = dynamic2.get("LootTableSeed").asLong(0L);
            if (l != 0L) {
                string2 = string2.set("seed", dynamic2.createLong(l));
            }
            itemStackData.setComponent("minecraft:container_loot", (Dynamic<?>)string2);
            dynamic2 = dynamic2.remove("LootTable").remove("LootTableSeed");
        }
        return switch (string) {
            case "minecraft:skull" -> {
                itemStackData.setComponent("minecraft:note_block_sound", dynamic2.get("note_block_sound"));
                yield dynamic2.remove("note_block_sound");
            }
            case "minecraft:decorated_pot" -> {
                itemStackData.setComponent("minecraft:pot_decorations", dynamic2.get("sherds"));
                Optional var6_7 = dynamic2.get("item").result();
                if (var6_7.isPresent()) {
                    itemStackData.setComponent("minecraft:container", dynamic2.createList(Stream.of(dynamic2.emptyMap().set("slot", dynamic2.createInt(0)).set("item", (Dynamic)var6_7.get()))));
                }
                yield dynamic2.remove("sherds").remove("item");
            }
            case "minecraft:banner" -> {
                itemStackData.setComponent("minecraft:banner_patterns", dynamic2.get("patterns"));
                Optional var6_8 = dynamic2.get("Base").asNumber().result();
                if (var6_8.isPresent()) {
                    itemStackData.setComponent("minecraft:base_color", dynamic2.createString(BannerPatternFormatFix.fixColor(((Number)var6_8.get()).intValue())));
                }
                yield dynamic2.remove("patterns").remove("Base");
            }
            case "minecraft:shulker_box", "minecraft:chest", "minecraft:trapped_chest", "minecraft:furnace", "minecraft:ender_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:brewing_stand", "minecraft:hopper", "minecraft:barrel", "minecraft:smoker", "minecraft:blast_furnace", "minecraft:campfire", "minecraft:chiseled_bookshelf", "minecraft:crafter" -> {
                List var6_9 = dynamic2.get("Items").asList(dynamic -> dynamic.emptyMap().set("slot", dynamic.createInt(dynamic.get("Slot").asByte((byte)0) & 0xFF)).set("item", dynamic.remove("Slot")));
                if (!var6_9.isEmpty()) {
                    itemStackData.setComponent("minecraft:container", dynamic2.createList(var6_9.stream()));
                }
                yield dynamic2.remove("Items");
            }
            case "minecraft:beehive" -> {
                itemStackData.setComponent("minecraft:bees", dynamic2.get("bees"));
                yield dynamic2.remove("bees");
            }
            default -> dynamic2;
        };
    }

    private static void fixEnchantments(ItemStackData itemStackData, Dynamic<?> dynamic2, String string, String string2, boolean bl) {
        OptionalDynamic<?> optionalDynamic = itemStackData.removeTag(string);
        List list = optionalDynamic.asList(Function.identity()).stream().flatMap(dynamic -> ItemStackComponentizationFix.parseEnchantment(dynamic).stream()).toList();
        if (!list.isEmpty() || bl) {
            Dynamic dynamic3 = dynamic2.emptyMap();
            Dynamic dynamic4 = dynamic2.emptyMap();
            for (Pair pair : list) {
                dynamic4 = dynamic4.set((String)pair.getFirst(), dynamic2.createInt(((Integer)pair.getSecond()).intValue()));
            }
            dynamic3 = dynamic3.set("levels", dynamic4);
            if (bl) {
                dynamic3 = dynamic3.set("show_in_tooltip", dynamic2.createBoolean(false));
            }
            itemStackData.setComponent(string2, dynamic3);
        }
        if (optionalDynamic.result().isPresent() && list.isEmpty()) {
            itemStackData.setComponent("minecraft:enchantment_glint_override", dynamic2.createBoolean(true));
        }
    }

    private static Optional<Pair<String, Integer>> parseEnchantment(Dynamic<?> dynamic) {
        return dynamic.get("id").asString().apply2stable((string, number) -> Pair.of((Object)string, (Object)Mth.clamp(number.intValue(), 0, 255)), dynamic.get("lvl").asNumber()).result();
    }

    private static void fixAdventureModeChecks(ItemStackData itemStackData, Dynamic<?> dynamic, int n) {
        ItemStackComponentizationFix.fixBlockStatePredicates(itemStackData, dynamic, "CanDestroy", "minecraft:can_break", (n & 8) != 0);
        ItemStackComponentizationFix.fixBlockStatePredicates(itemStackData, dynamic, "CanPlaceOn", "minecraft:can_place_on", (n & 0x10) != 0);
    }

    private static void fixBlockStatePredicates(ItemStackData itemStackData, Dynamic<?> dynamic2, String string, String string2, boolean bl) {
        Optional optional = itemStackData.removeTag(string).result();
        if (optional.isEmpty()) {
            return;
        }
        Dynamic dynamic3 = dynamic2.emptyMap().set("predicates", dynamic2.createList(((Dynamic)optional.get()).asStream().map(dynamic -> (Dynamic)DataFixUtils.orElse((Optional)dynamic.asString().map(string -> ItemStackComponentizationFix.fixBlockStatePredicate(dynamic, string)).result(), (Object)dynamic))));
        if (bl) {
            dynamic3 = dynamic3.set("show_in_tooltip", dynamic2.createBoolean(false));
        }
        itemStackData.setComponent(string2, dynamic3);
    }

    private static Dynamic<?> fixBlockStatePredicate(Dynamic<?> dynamic, String string) {
        int n = string.indexOf(91);
        int n2 = string.indexOf(123);
        int n3 = string.length();
        if (n != -1) {
            n3 = n;
        }
        if (n2 != -1) {
            n3 = Math.min(n3, n2);
        }
        String string2 = string.substring(0, n3);
        Dynamic dynamic2 = dynamic.emptyMap().set("blocks", dynamic.createString(string2.trim()));
        int n4 = string.indexOf(93);
        if (n != -1 && n4 != -1) {
            Dynamic dynamic3 = dynamic.emptyMap();
            Iterable iterable = PROPERTY_SPLITTER.split((CharSequence)string.substring(n + 1, n4));
            for (String string3 : iterable) {
                int n5 = string3.indexOf(61);
                if (n5 == -1) continue;
                String string4 = string3.substring(0, n5).trim();
                String string5 = string3.substring(n5 + 1).trim();
                dynamic3 = dynamic3.set(string4, dynamic.createString(string5));
            }
            dynamic2 = dynamic2.set("state", dynamic3);
        }
        int n6 = string.indexOf(125);
        if (n2 != -1 && n6 != -1) {
            dynamic2 = dynamic2.set("nbt", dynamic.createString(string.substring(n2, n6 + 1)));
        }
        return dynamic2;
    }

    private static void fixAttributeModifiers(ItemStackData itemStackData, Dynamic<?> dynamic, int n) {
        OptionalDynamic<?> optionalDynamic = itemStackData.removeTag("AttributeModifiers");
        if (optionalDynamic.result().isEmpty()) {
            return;
        }
        boolean bl = (n & 2) != 0;
        List list = optionalDynamic.asList(ItemStackComponentizationFix::fixAttributeModifier);
        Dynamic dynamic2 = dynamic.emptyMap().set("modifiers", dynamic.createList(list.stream()));
        if (bl) {
            dynamic2 = dynamic2.set("show_in_tooltip", dynamic.createBoolean(false));
        }
        itemStackData.setComponent("minecraft:attribute_modifiers", dynamic2);
    }

    private static Dynamic<?> fixAttributeModifier(Dynamic<?> dynamic2) {
        Dynamic dynamic3 = dynamic2.emptyMap().set("name", dynamic2.createString("")).set("amount", dynamic2.createDouble(0.0)).set("operation", dynamic2.createString("add_value"));
        dynamic3 = Dynamic.copyField(dynamic2, (String)"AttributeName", (Dynamic)dynamic3, (String)"type");
        dynamic3 = Dynamic.copyField(dynamic2, (String)"Slot", (Dynamic)dynamic3, (String)"slot");
        dynamic3 = Dynamic.copyField(dynamic2, (String)"UUID", (Dynamic)dynamic3, (String)"uuid");
        dynamic3 = Dynamic.copyField(dynamic2, (String)"Name", (Dynamic)dynamic3, (String)"name");
        dynamic3 = Dynamic.copyField(dynamic2, (String)"Amount", (Dynamic)dynamic3, (String)"amount");
        dynamic3 = Dynamic.copyAndFixField(dynamic2, (String)"Operation", (Dynamic)dynamic3, (String)"operation", dynamic -> dynamic.createString(switch (dynamic.asInt(0)) {
            default -> "add_value";
            case 1 -> "add_multiplied_base";
            case 2 -> "add_multiplied_total";
        }));
        return dynamic3;
    }

    private static Pair<Dynamic<?>, Dynamic<?>> fixMapDecoration(Dynamic<?> dynamic) {
        Dynamic dynamic2 = (Dynamic)DataFixUtils.orElseGet((Optional)dynamic.get("id").result(), () -> dynamic.createString(""));
        Dynamic dynamic3 = dynamic.emptyMap().set("type", dynamic.createString(ItemStackComponentizationFix.fixMapDecorationType(dynamic.get("type").asInt(0)))).set("x", dynamic.createDouble(dynamic.get("x").asDouble(0.0))).set("z", dynamic.createDouble(dynamic.get("z").asDouble(0.0))).set("rotation", dynamic.createFloat((float)dynamic.get("rot").asDouble(0.0)));
        return Pair.of((Object)dynamic2, (Object)dynamic3);
    }

    private static String fixMapDecorationType(int n) {
        return switch (n) {
            default -> "player";
            case 1 -> "frame";
            case 2 -> "red_marker";
            case 3 -> "blue_marker";
            case 4 -> "target_x";
            case 5 -> "target_point";
            case 6 -> "player_off_map";
            case 7 -> "player_off_limits";
            case 8 -> "mansion";
            case 9 -> "monument";
            case 10 -> "banner_white";
            case 11 -> "banner_orange";
            case 12 -> "banner_magenta";
            case 13 -> "banner_light_blue";
            case 14 -> "banner_yellow";
            case 15 -> "banner_lime";
            case 16 -> "banner_pink";
            case 17 -> "banner_gray";
            case 18 -> "banner_light_gray";
            case 19 -> "banner_cyan";
            case 20 -> "banner_purple";
            case 21 -> "banner_blue";
            case 22 -> "banner_brown";
            case 23 -> "banner_green";
            case 24 -> "banner_red";
            case 25 -> "banner_black";
            case 26 -> "red_x";
            case 27 -> "village_desert";
            case 28 -> "village_plains";
            case 29 -> "village_savanna";
            case 30 -> "village_snowy";
            case 31 -> "village_taiga";
            case 32 -> "jungle_temple";
            case 33 -> "swamp_hut";
        };
    }

    private static void fixPotionContents(ItemStackData itemStackData, Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = dynamic.emptyMap();
        Optional<String> optional = itemStackData.removeTag("Potion").asString().result().filter(string -> !string.equals("minecraft:empty"));
        if (optional.isPresent()) {
            dynamic2 = dynamic2.set("potion", dynamic.createString(optional.get()));
        }
        dynamic2 = itemStackData.moveTagInto("CustomPotionColor", dynamic2, "custom_color");
        if (!(dynamic2 = itemStackData.moveTagInto("custom_potion_effects", dynamic2, "custom_effects")).equals((Object)dynamic.emptyMap())) {
            itemStackData.setComponent("minecraft:potion_contents", dynamic2);
        }
    }

    private static void fixWritableBook(ItemStackData itemStackData, Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = ItemStackComponentizationFix.fixBookPages(itemStackData, dynamic);
        if (dynamic2 != null) {
            itemStackData.setComponent("minecraft:writable_book_content", dynamic.emptyMap().set("pages", dynamic2));
        }
    }

    private static void fixWrittenBook(ItemStackData itemStackData, Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = ItemStackComponentizationFix.fixBookPages(itemStackData, dynamic);
        String string = itemStackData.removeTag("title").asString("");
        Optional optional = itemStackData.removeTag("filtered_title").asString().result();
        Dynamic dynamic3 = dynamic.emptyMap();
        dynamic3 = dynamic3.set("title", ItemStackComponentizationFix.createFilteredText(dynamic, string, optional));
        dynamic3 = itemStackData.moveTagInto("author", dynamic3, "author");
        dynamic3 = itemStackData.moveTagInto("resolved", dynamic3, "resolved");
        dynamic3 = itemStackData.moveTagInto("generation", dynamic3, "generation");
        if (dynamic2 != null) {
            dynamic3 = dynamic3.set("pages", dynamic2);
        }
        itemStackData.setComponent("minecraft:written_book_content", dynamic3);
    }

    @Nullable
    private static Dynamic<?> fixBookPages(ItemStackData itemStackData, Dynamic<?> dynamic2) {
        List list = itemStackData.removeTag("pages").asList(dynamic -> dynamic.asString(""));
        Map map = itemStackData.removeTag("filtered_pages").asMap(dynamic -> dynamic.asString("0"), dynamic -> dynamic.asString(""));
        if (list.isEmpty()) {
            return null;
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (int i = 0; i < list.size(); ++i) {
            String string = (String)list.get(i);
            String string2 = (String)map.get(String.valueOf(i));
            arrayList.add(ItemStackComponentizationFix.createFilteredText(dynamic2, string, Optional.ofNullable(string2)));
        }
        return dynamic2.createList(arrayList.stream());
    }

    private static Dynamic<?> createFilteredText(Dynamic<?> dynamic, String string, Optional<String> optional) {
        Dynamic dynamic2 = dynamic.emptyMap().set("raw", dynamic.createString(string));
        if (optional.isPresent()) {
            dynamic2 = dynamic2.set("filtered", dynamic.createString(optional.get()));
        }
        return dynamic2;
    }

    private static void fixBucketedMobData(ItemStackData itemStackData, Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = dynamic.emptyMap();
        for (String string : BUCKETED_MOB_TAGS) {
            dynamic2 = itemStackData.moveTagInto(string, dynamic2, string);
        }
        if (!dynamic2.equals((Object)dynamic.emptyMap())) {
            itemStackData.setComponent("minecraft:bucket_entity_data", dynamic2);
        }
    }

    private static void fixLodestoneTracker(ItemStackData itemStackData, Dynamic<?> dynamic) {
        Optional optional = itemStackData.removeTag("LodestonePos").result();
        Optional optional2 = itemStackData.removeTag("LodestoneDimension").result();
        if (optional.isEmpty() && optional2.isEmpty()) {
            return;
        }
        boolean bl = itemStackData.removeTag("LodestoneTracked").asBoolean(true);
        Dynamic dynamic2 = dynamic.emptyMap();
        if (optional.isPresent() && optional2.isPresent()) {
            dynamic2 = dynamic2.set("target", dynamic.emptyMap().set("pos", (Dynamic)optional.get()).set("dimension", (Dynamic)optional2.get()));
        }
        if (!bl) {
            dynamic2 = dynamic2.set("tracked", dynamic.createBoolean(false));
        }
        itemStackData.setComponent("minecraft:lodestone_tracker", dynamic2);
    }

    private static void fixFireworkStar(ItemStackData itemStackData) {
        itemStackData.fixSubTag("Explosion", true, dynamic -> {
            itemStackData.setComponent("minecraft:firework_explosion", ItemStackComponentizationFix.fixFireworkExplosion(dynamic));
            return dynamic.remove("Type").remove("Colors").remove("FadeColors").remove("Trail").remove("Flicker");
        });
    }

    private static void fixFireworkRocket(ItemStackData itemStackData) {
        itemStackData.fixSubTag("Fireworks", true, dynamic -> {
            Stream<Dynamic> stream = dynamic.get("Explosions").asStream().map(ItemStackComponentizationFix::fixFireworkExplosion);
            int n = dynamic.get("Flight").asInt(0);
            itemStackData.setComponent("minecraft:fireworks", dynamic.emptyMap().set("explosions", dynamic.createList(stream)).set("flight_duration", dynamic.createByte((byte)n)));
            return dynamic.remove("Explosions").remove("Flight");
        });
    }

    private static Dynamic<?> fixFireworkExplosion(Dynamic<?> dynamic) {
        dynamic = dynamic.set("shape", dynamic.createString(switch (dynamic.get("Type").asInt(0)) {
            default -> "small_ball";
            case 1 -> "large_ball";
            case 2 -> "star";
            case 3 -> "creeper";
            case 4 -> "burst";
        })).remove("Type");
        dynamic = dynamic.renameField("Colors", "colors");
        dynamic = dynamic.renameField("FadeColors", "fade_colors");
        dynamic = dynamic.renameField("Trail", "has_trail");
        dynamic = dynamic.renameField("Flicker", "has_twinkle");
        return dynamic;
    }

    public static Dynamic<?> fixProfile(Dynamic<?> dynamic) {
        Optional optional = dynamic.asString().result();
        if (optional.isPresent()) {
            if (ItemStackComponentizationFix.isValidPlayerName((String)optional.get())) {
                return dynamic.emptyMap().set("name", dynamic.createString((String)optional.get()));
            }
            return dynamic.emptyMap();
        }
        String string = dynamic.get("Name").asString("");
        Optional optional2 = dynamic.get("Id").result();
        Dynamic<?> dynamic2 = ItemStackComponentizationFix.fixProfileProperties(dynamic.get("Properties"));
        Dynamic dynamic3 = dynamic.emptyMap();
        if (ItemStackComponentizationFix.isValidPlayerName(string)) {
            dynamic3 = dynamic3.set("name", dynamic.createString(string));
        }
        if (optional2.isPresent()) {
            dynamic3 = dynamic3.set("id", (Dynamic)optional2.get());
        }
        if (dynamic2 != null) {
            dynamic3 = dynamic3.set("properties", dynamic2);
        }
        return dynamic3;
    }

    private static boolean isValidPlayerName(String string) {
        if (string.length() > 16) {
            return false;
        }
        return string.chars().filter(n -> n <= 32 || n >= 127).findAny().isEmpty();
    }

    @Nullable
    private static Dynamic<?> fixProfileProperties(OptionalDynamic<?> optionalDynamic) {
        Map map = optionalDynamic.asMap(dynamic -> dynamic.asString(""), dynamic2 -> dynamic2.asList(dynamic -> {
            String string = dynamic.get("Value").asString("");
            Optional optional = dynamic.get("Signature").asString().result();
            return Pair.of((Object)string, (Object)optional);
        }));
        if (map.isEmpty()) {
            return null;
        }
        return optionalDynamic.createList(map.entrySet().stream().flatMap(entry -> ((List)entry.getValue()).stream().map(pair -> {
            Dynamic dynamic = optionalDynamic.emptyMap().set("name", optionalDynamic.createString((String)entry.getKey())).set("value", optionalDynamic.createString((String)pair.getFirst()));
            Optional optional = (Optional)pair.getSecond();
            if (optional.isPresent()) {
                return dynamic.set("signature", optionalDynamic.createString((String)optional.get()));
            }
            return dynamic;
        })));
    }

    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("ItemStack componentization", this.getInputSchema().getType(References.ITEM_STACK), this.getOutputSchema().getType(References.ITEM_STACK), dynamic -> {
            Optional<Dynamic> optional = ItemStackData.read(dynamic).map(itemStackData -> {
                ItemStackComponentizationFix.fixItemStack(itemStackData, itemStackData.tag);
                return itemStackData.write();
            });
            return (Dynamic)DataFixUtils.orElse(optional, (Object)dynamic);
        });
    }

    static class ItemStackData {
        private final String item;
        private final int count;
        private Dynamic<?> components;
        private final Dynamic<?> remainder;
        Dynamic<?> tag;

        private ItemStackData(String string, int n, Dynamic<?> dynamic) {
            this.item = NamespacedSchema.ensureNamespaced(string);
            this.count = n;
            this.components = dynamic.emptyMap();
            this.tag = dynamic.get("tag").orElseEmptyMap();
            this.remainder = dynamic.remove("tag");
        }

        public static Optional<ItemStackData> read(Dynamic<?> dynamic) {
            return dynamic.get("id").asString().apply2stable((string, number) -> new ItemStackData((String)string, number.intValue(), (Dynamic<?>)dynamic.remove("id").remove("Count")), dynamic.get("Count").asNumber()).result();
        }

        public OptionalDynamic<?> removeTag(String string) {
            OptionalDynamic optionalDynamic = this.tag.get(string);
            this.tag = this.tag.remove(string);
            return optionalDynamic;
        }

        public void setComponent(String string, Dynamic<?> dynamic) {
            this.components = this.components.set(string, dynamic);
        }

        public void setComponent(String string, OptionalDynamic<?> optionalDynamic) {
            optionalDynamic.result().ifPresent(dynamic -> {
                this.components = this.components.set(string, dynamic);
            });
        }

        public Dynamic<?> moveTagInto(String string, Dynamic<?> dynamic, String string2) {
            Optional optional = this.removeTag(string).result();
            if (optional.isPresent()) {
                return dynamic.set(string2, (Dynamic)optional.get());
            }
            return dynamic;
        }

        public void moveTagToComponent(String string, String string2, Dynamic<?> dynamic) {
            Optional optional = this.removeTag(string).result();
            if (optional.isPresent() && !((Dynamic)optional.get()).equals(dynamic)) {
                this.setComponent(string2, (Dynamic)optional.get());
            }
        }

        public void moveTagToComponent(String string, String string2) {
            this.removeTag(string).result().ifPresent(dynamic -> this.setComponent(string2, (Dynamic<?>)dynamic));
        }

        public void fixSubTag(String string, boolean bl, UnaryOperator<Dynamic<?>> unaryOperator) {
            OptionalDynamic optionalDynamic = this.tag.get(string);
            if (bl && optionalDynamic.result().isEmpty()) {
                return;
            }
            Dynamic dynamic = optionalDynamic.orElseEmptyMap();
            this.tag = (dynamic = (Dynamic)unaryOperator.apply(dynamic)).equals((Object)dynamic.emptyMap()) ? this.tag.remove(string) : this.tag.set(string, dynamic);
        }

        public Dynamic<?> write() {
            Dynamic dynamic = this.tag.emptyMap().set("id", this.tag.createString(this.item)).set("count", this.tag.createInt(this.count));
            if (!this.tag.equals((Object)this.tag.emptyMap())) {
                this.components = this.components.set("minecraft:custom_data", this.tag);
            }
            if (!this.components.equals((Object)this.tag.emptyMap())) {
                dynamic = dynamic.set("components", this.components);
            }
            return ItemStackData.mergeRemainder(dynamic, this.remainder);
        }

        private static <T> Dynamic<T> mergeRemainder(Dynamic<T> dynamic, Dynamic<?> dynamic2) {
            DynamicOps dynamicOps = dynamic.getOps();
            return dynamicOps.getMap(dynamic.getValue()).flatMap(mapLike -> dynamicOps.mergeToMap(dynamic2.convert(dynamicOps).getValue(), mapLike)).map(object -> new Dynamic(dynamicOps, object)).result().orElse(dynamic);
        }

        public boolean is(String string) {
            return this.item.equals(string);
        }

        public boolean is(Set<String> set) {
            return set.contains(this.item);
        }

        public boolean hasComponent(String string) {
            return this.components.get(string).result().isPresent();
        }
    }
}

