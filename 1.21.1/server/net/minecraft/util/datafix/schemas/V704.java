/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V99;

public class V704
extends Schema {
    protected static final Map<String, String> ITEM_TO_BLOCKENTITY = (Map)DataFixUtils.make(() -> {
        HashMap hashMap = Maps.newHashMap();
        hashMap.put("minecraft:furnace", "minecraft:furnace");
        hashMap.put("minecraft:lit_furnace", "minecraft:furnace");
        hashMap.put("minecraft:chest", "minecraft:chest");
        hashMap.put("minecraft:trapped_chest", "minecraft:chest");
        hashMap.put("minecraft:ender_chest", "minecraft:ender_chest");
        hashMap.put("minecraft:jukebox", "minecraft:jukebox");
        hashMap.put("minecraft:dispenser", "minecraft:dispenser");
        hashMap.put("minecraft:dropper", "minecraft:dropper");
        hashMap.put("minecraft:sign", "minecraft:sign");
        hashMap.put("minecraft:mob_spawner", "minecraft:mob_spawner");
        hashMap.put("minecraft:spawner", "minecraft:mob_spawner");
        hashMap.put("minecraft:noteblock", "minecraft:noteblock");
        hashMap.put("minecraft:brewing_stand", "minecraft:brewing_stand");
        hashMap.put("minecraft:enhanting_table", "minecraft:enchanting_table");
        hashMap.put("minecraft:command_block", "minecraft:command_block");
        hashMap.put("minecraft:beacon", "minecraft:beacon");
        hashMap.put("minecraft:skull", "minecraft:skull");
        hashMap.put("minecraft:daylight_detector", "minecraft:daylight_detector");
        hashMap.put("minecraft:hopper", "minecraft:hopper");
        hashMap.put("minecraft:banner", "minecraft:banner");
        hashMap.put("minecraft:flower_pot", "minecraft:flower_pot");
        hashMap.put("minecraft:repeating_command_block", "minecraft:command_block");
        hashMap.put("minecraft:chain_command_block", "minecraft:command_block");
        hashMap.put("minecraft:shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:white_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:orange_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:magenta_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:light_blue_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:yellow_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:lime_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:pink_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:gray_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:silver_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:cyan_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:purple_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:blue_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:brown_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:green_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:red_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:black_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:bed", "minecraft:bed");
        hashMap.put("minecraft:light_gray_shulker_box", "minecraft:shulker_box");
        hashMap.put("minecraft:banner", "minecraft:banner");
        hashMap.put("minecraft:white_banner", "minecraft:banner");
        hashMap.put("minecraft:orange_banner", "minecraft:banner");
        hashMap.put("minecraft:magenta_banner", "minecraft:banner");
        hashMap.put("minecraft:light_blue_banner", "minecraft:banner");
        hashMap.put("minecraft:yellow_banner", "minecraft:banner");
        hashMap.put("minecraft:lime_banner", "minecraft:banner");
        hashMap.put("minecraft:pink_banner", "minecraft:banner");
        hashMap.put("minecraft:gray_banner", "minecraft:banner");
        hashMap.put("minecraft:silver_banner", "minecraft:banner");
        hashMap.put("minecraft:light_gray_banner", "minecraft:banner");
        hashMap.put("minecraft:cyan_banner", "minecraft:banner");
        hashMap.put("minecraft:purple_banner", "minecraft:banner");
        hashMap.put("minecraft:blue_banner", "minecraft:banner");
        hashMap.put("minecraft:brown_banner", "minecraft:banner");
        hashMap.put("minecraft:green_banner", "minecraft:banner");
        hashMap.put("minecraft:red_banner", "minecraft:banner");
        hashMap.put("minecraft:black_banner", "minecraft:banner");
        hashMap.put("minecraft:standing_sign", "minecraft:sign");
        hashMap.put("minecraft:wall_sign", "minecraft:sign");
        hashMap.put("minecraft:piston_head", "minecraft:piston");
        hashMap.put("minecraft:daylight_detector_inverted", "minecraft:daylight_detector");
        hashMap.put("minecraft:unpowered_comparator", "minecraft:comparator");
        hashMap.put("minecraft:powered_comparator", "minecraft:comparator");
        hashMap.put("minecraft:wall_banner", "minecraft:banner");
        hashMap.put("minecraft:standing_banner", "minecraft:banner");
        hashMap.put("minecraft:structure_block", "minecraft:structure_block");
        hashMap.put("minecraft:end_portal", "minecraft:end_portal");
        hashMap.put("minecraft:end_gateway", "minecraft:end_gateway");
        hashMap.put("minecraft:sign", "minecraft:sign");
        hashMap.put("minecraft:shield", "minecraft:banner");
        hashMap.put("minecraft:white_bed", "minecraft:bed");
        hashMap.put("minecraft:orange_bed", "minecraft:bed");
        hashMap.put("minecraft:magenta_bed", "minecraft:bed");
        hashMap.put("minecraft:light_blue_bed", "minecraft:bed");
        hashMap.put("minecraft:yellow_bed", "minecraft:bed");
        hashMap.put("minecraft:lime_bed", "minecraft:bed");
        hashMap.put("minecraft:pink_bed", "minecraft:bed");
        hashMap.put("minecraft:gray_bed", "minecraft:bed");
        hashMap.put("minecraft:silver_bed", "minecraft:bed");
        hashMap.put("minecraft:light_gray_bed", "minecraft:bed");
        hashMap.put("minecraft:cyan_bed", "minecraft:bed");
        hashMap.put("minecraft:purple_bed", "minecraft:bed");
        hashMap.put("minecraft:blue_bed", "minecraft:bed");
        hashMap.put("minecraft:brown_bed", "minecraft:bed");
        hashMap.put("minecraft:green_bed", "minecraft:bed");
        hashMap.put("minecraft:red_bed", "minecraft:bed");
        hashMap.put("minecraft:black_bed", "minecraft:bed");
        hashMap.put("minecraft:oak_sign", "minecraft:sign");
        hashMap.put("minecraft:spruce_sign", "minecraft:sign");
        hashMap.put("minecraft:birch_sign", "minecraft:sign");
        hashMap.put("minecraft:jungle_sign", "minecraft:sign");
        hashMap.put("minecraft:acacia_sign", "minecraft:sign");
        hashMap.put("minecraft:dark_oak_sign", "minecraft:sign");
        hashMap.put("minecraft:crimson_sign", "minecraft:sign");
        hashMap.put("minecraft:warped_sign", "minecraft:sign");
        hashMap.put("minecraft:skeleton_skull", "minecraft:skull");
        hashMap.put("minecraft:wither_skeleton_skull", "minecraft:skull");
        hashMap.put("minecraft:zombie_head", "minecraft:skull");
        hashMap.put("minecraft:player_head", "minecraft:skull");
        hashMap.put("minecraft:creeper_head", "minecraft:skull");
        hashMap.put("minecraft:dragon_head", "minecraft:skull");
        hashMap.put("minecraft:barrel", "minecraft:barrel");
        hashMap.put("minecraft:conduit", "minecraft:conduit");
        hashMap.put("minecraft:smoker", "minecraft:smoker");
        hashMap.put("minecraft:blast_furnace", "minecraft:blast_furnace");
        hashMap.put("minecraft:lectern", "minecraft:lectern");
        hashMap.put("minecraft:bell", "minecraft:bell");
        hashMap.put("minecraft:jigsaw", "minecraft:jigsaw");
        hashMap.put("minecraft:campfire", "minecraft:campfire");
        hashMap.put("minecraft:bee_nest", "minecraft:beehive");
        hashMap.put("minecraft:beehive", "minecraft:beehive");
        hashMap.put("minecraft:sculk_sensor", "minecraft:sculk_sensor");
        hashMap.put("minecraft:decorated_pot", "minecraft:decorated_pot");
        hashMap.put("minecraft:crafter", "minecraft:crafter");
        return ImmutableMap.copyOf((Map)hashMap);
    });
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> dynamicOps, T t) {
            return V99.addNames(new Dynamic(dynamicOps, t), ITEM_TO_BLOCKENTITY, V99.ITEM_TO_ENTITY);
        }
    };

    public V704(int n, Schema schema) {
        super(n, schema);
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
    }

    public Type<?> getChoiceType(DSL.TypeReference typeReference, String string) {
        if (Objects.equals(typeReference.typeName(), References.BLOCK_ENTITY.typeName())) {
            return super.getChoiceType(typeReference, NamespacedSchema.ensureNamespaced(string));
        }
        return super.getChoiceType(typeReference, string);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap hashMap = Maps.newHashMap();
        V704.registerInventory(schema, hashMap, "minecraft:furnace");
        V704.registerInventory(schema, hashMap, "minecraft:chest");
        schema.registerSimple((Map)hashMap, "minecraft:ender_chest");
        schema.register((Map)hashMap, "minecraft:jukebox", string -> DSL.optionalFields((String)"RecordItem", (TypeTemplate)References.ITEM_STACK.in(schema)));
        V704.registerInventory(schema, hashMap, "minecraft:dispenser");
        V704.registerInventory(schema, hashMap, "minecraft:dropper");
        schema.registerSimple((Map)hashMap, "minecraft:sign");
        schema.register((Map)hashMap, "minecraft:mob_spawner", string -> References.UNTAGGED_SPAWNER.in(schema));
        schema.registerSimple((Map)hashMap, "minecraft:noteblock");
        schema.registerSimple((Map)hashMap, "minecraft:piston");
        V704.registerInventory(schema, hashMap, "minecraft:brewing_stand");
        schema.registerSimple((Map)hashMap, "minecraft:enchanting_table");
        schema.registerSimple((Map)hashMap, "minecraft:end_portal");
        schema.registerSimple((Map)hashMap, "minecraft:beacon");
        schema.registerSimple((Map)hashMap, "minecraft:skull");
        schema.registerSimple((Map)hashMap, "minecraft:daylight_detector");
        V704.registerInventory(schema, hashMap, "minecraft:hopper");
        schema.registerSimple((Map)hashMap, "minecraft:comparator");
        schema.register((Map)hashMap, "minecraft:flower_pot", string -> DSL.optionalFields((String)"Item", (TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)References.ITEM_NAME.in(schema))));
        schema.registerSimple((Map)hashMap, "minecraft:banner");
        schema.registerSimple((Map)hashMap, "minecraft:structure_block");
        schema.registerSimple((Map)hashMap, "minecraft:end_gateway");
        schema.registerSimple((Map)hashMap, "minecraft:command_block");
        return hashMap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.BLOCK_ENTITY, () -> DSL.optionalFields((String)"components", (TypeTemplate)References.DATA_COMPONENTS.in(schema), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", NamespacedSchema.namespacedString(), (Map)map2)));
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)References.ITEM_NAME.in(schema), (String)"tag", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"EntityTag", (Object)References.ENTITY_TREE.in(schema)), Pair.of((Object)"BlockEntityTag", (Object)References.BLOCK_ENTITY.in(schema)), Pair.of((Object)"CanDestroy", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))), Pair.of((Object)"CanPlaceOn", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))), Pair.of((Object)"Items", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"ChargedProjectiles", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)))})), (Hook.HookFunction)ADD_NAMES, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
    }
}

