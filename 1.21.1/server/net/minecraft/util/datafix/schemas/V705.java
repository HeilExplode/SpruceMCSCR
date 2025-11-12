/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
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
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V100;
import net.minecraft.util.datafix.schemas.V704;
import net.minecraft.util.datafix.schemas.V99;

public class V705
extends NamespacedSchema {
    static final Map<String, String> ITEM_TO_ENTITY = ImmutableMap.builder().put((Object)"minecraft:armor_stand", (Object)"minecraft:armor_stand").put((Object)"minecraft:painting", (Object)"minecraft:painting").put((Object)"minecraft:armadillo_spawn_egg", (Object)"minecraft:armadillo").put((Object)"minecraft:allay_spawn_egg", (Object)"minecraft:allay").put((Object)"minecraft:axolotl_spawn_egg", (Object)"minecraft:axolotl").put((Object)"minecraft:bat_spawn_egg", (Object)"minecraft:bat").put((Object)"minecraft:bee_spawn_egg", (Object)"minecraft:bee").put((Object)"minecraft:blaze_spawn_egg", (Object)"minecraft:blaze").put((Object)"minecraft:bogged_spawn_egg", (Object)"minecraft:bogged").put((Object)"minecraft:breeze_spawn_egg", (Object)"minecraft:breeze").put((Object)"minecraft:cat_spawn_egg", (Object)"minecraft:cat").put((Object)"minecraft:camel_spawn_egg", (Object)"minecraft:camel").put((Object)"minecraft:cave_spider_spawn_egg", (Object)"minecraft:cave_spider").put((Object)"minecraft:chicken_spawn_egg", (Object)"minecraft:chicken").put((Object)"minecraft:cod_spawn_egg", (Object)"minecraft:cod").put((Object)"minecraft:cow_spawn_egg", (Object)"minecraft:cow").put((Object)"minecraft:creeper_spawn_egg", (Object)"minecraft:creeper").put((Object)"minecraft:dolphin_spawn_egg", (Object)"minecraft:dolphin").put((Object)"minecraft:donkey_spawn_egg", (Object)"minecraft:donkey").put((Object)"minecraft:drowned_spawn_egg", (Object)"minecraft:drowned").put((Object)"minecraft:elder_guardian_spawn_egg", (Object)"minecraft:elder_guardian").put((Object)"minecraft:ender_dragon_spawn_egg", (Object)"minecraft:ender_dragon").put((Object)"minecraft:enderman_spawn_egg", (Object)"minecraft:enderman").put((Object)"minecraft:endermite_spawn_egg", (Object)"minecraft:endermite").put((Object)"minecraft:evoker_spawn_egg", (Object)"minecraft:evoker").put((Object)"minecraft:fox_spawn_egg", (Object)"minecraft:fox").put((Object)"minecraft:frog_spawn_egg", (Object)"minecraft:frog").put((Object)"minecraft:ghast_spawn_egg", (Object)"minecraft:ghast").put((Object)"minecraft:glow_squid_spawn_egg", (Object)"minecraft:glow_squid").put((Object)"minecraft:goat_spawn_egg", (Object)"minecraft:goat").put((Object)"minecraft:guardian_spawn_egg", (Object)"minecraft:guardian").put((Object)"minecraft:hoglin_spawn_egg", (Object)"minecraft:hoglin").put((Object)"minecraft:horse_spawn_egg", (Object)"minecraft:horse").put((Object)"minecraft:husk_spawn_egg", (Object)"minecraft:husk").put((Object)"minecraft:iron_golem_spawn_egg", (Object)"minecraft:iron_golem").put((Object)"minecraft:llama_spawn_egg", (Object)"minecraft:llama").put((Object)"minecraft:magma_cube_spawn_egg", (Object)"minecraft:magma_cube").put((Object)"minecraft:mooshroom_spawn_egg", (Object)"minecraft:mooshroom").put((Object)"minecraft:mule_spawn_egg", (Object)"minecraft:mule").put((Object)"minecraft:ocelot_spawn_egg", (Object)"minecraft:ocelot").put((Object)"minecraft:panda_spawn_egg", (Object)"minecraft:panda").put((Object)"minecraft:parrot_spawn_egg", (Object)"minecraft:parrot").put((Object)"minecraft:phantom_spawn_egg", (Object)"minecraft:phantom").put((Object)"minecraft:pig_spawn_egg", (Object)"minecraft:pig").put((Object)"minecraft:piglin_spawn_egg", (Object)"minecraft:piglin").put((Object)"minecraft:piglin_brute_spawn_egg", (Object)"minecraft:piglin_brute").put((Object)"minecraft:pillager_spawn_egg", (Object)"minecraft:pillager").put((Object)"minecraft:polar_bear_spawn_egg", (Object)"minecraft:polar_bear").put((Object)"minecraft:pufferfish_spawn_egg", (Object)"minecraft:pufferfish").put((Object)"minecraft:rabbit_spawn_egg", (Object)"minecraft:rabbit").put((Object)"minecraft:ravager_spawn_egg", (Object)"minecraft:ravager").put((Object)"minecraft:salmon_spawn_egg", (Object)"minecraft:salmon").put((Object)"minecraft:sheep_spawn_egg", (Object)"minecraft:sheep").put((Object)"minecraft:shulker_spawn_egg", (Object)"minecraft:shulker").put((Object)"minecraft:silverfish_spawn_egg", (Object)"minecraft:silverfish").put((Object)"minecraft:skeleton_spawn_egg", (Object)"minecraft:skeleton").put((Object)"minecraft:skeleton_horse_spawn_egg", (Object)"minecraft:skeleton_horse").put((Object)"minecraft:slime_spawn_egg", (Object)"minecraft:slime").put((Object)"minecraft:sniffer_spawn_egg", (Object)"minecraft:sniffer").put((Object)"minecraft:snow_golem_spawn_egg", (Object)"minecraft:snow_golem").put((Object)"minecraft:spider_spawn_egg", (Object)"minecraft:spider").put((Object)"minecraft:squid_spawn_egg", (Object)"minecraft:squid").put((Object)"minecraft:stray_spawn_egg", (Object)"minecraft:stray").put((Object)"minecraft:strider_spawn_egg", (Object)"minecraft:strider").put((Object)"minecraft:tadpole_spawn_egg", (Object)"minecraft:tadpole").put((Object)"minecraft:trader_llama_spawn_egg", (Object)"minecraft:trader_llama").put((Object)"minecraft:tropical_fish_spawn_egg", (Object)"minecraft:tropical_fish").put((Object)"minecraft:turtle_spawn_egg", (Object)"minecraft:turtle").put((Object)"minecraft:vex_spawn_egg", (Object)"minecraft:vex").put((Object)"minecraft:villager_spawn_egg", (Object)"minecraft:villager").put((Object)"minecraft:vindicator_spawn_egg", (Object)"minecraft:vindicator").put((Object)"minecraft:wandering_trader_spawn_egg", (Object)"minecraft:wandering_trader").put((Object)"minecraft:warden_spawn_egg", (Object)"minecraft:warden").put((Object)"minecraft:witch_spawn_egg", (Object)"minecraft:witch").put((Object)"minecraft:wither_spawn_egg", (Object)"minecraft:wither").put((Object)"minecraft:wither_skeleton_spawn_egg", (Object)"minecraft:wither_skeleton").put((Object)"minecraft:wolf_spawn_egg", (Object)"minecraft:wolf").put((Object)"minecraft:zoglin_spawn_egg", (Object)"minecraft:zoglin").put((Object)"minecraft:zombie_spawn_egg", (Object)"minecraft:zombie").put((Object)"minecraft:zombie_horse_spawn_egg", (Object)"minecraft:zombie_horse").put((Object)"minecraft:zombie_villager_spawn_egg", (Object)"minecraft:zombie_villager").put((Object)"minecraft:zombified_piglin_spawn_egg", (Object)"minecraft:zombified_piglin").put((Object)"minecraft:item_frame", (Object)"minecraft:item_frame").put((Object)"minecraft:boat", (Object)"minecraft:boat").put((Object)"minecraft:oak_boat", (Object)"minecraft:boat").put((Object)"minecraft:oak_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:spruce_boat", (Object)"minecraft:boat").put((Object)"minecraft:spruce_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:birch_boat", (Object)"minecraft:boat").put((Object)"minecraft:birch_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:jungle_boat", (Object)"minecraft:boat").put((Object)"minecraft:jungle_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:acacia_boat", (Object)"minecraft:boat").put((Object)"minecraft:acacia_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:cherry_boat", (Object)"minecraft:boat").put((Object)"minecraft:cherry_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:dark_oak_boat", (Object)"minecraft:boat").put((Object)"minecraft:dark_oak_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:mangrove_boat", (Object)"minecraft:boat").put((Object)"minecraft:mangrove_chest_boat", (Object)"minecraft:chest_boat").put((Object)"minecraft:bamboo_raft", (Object)"minecraft:boat").put((Object)"minecraft:bamboo_chest_raft", (Object)"minecraft:chest_boat").put((Object)"minecraft:minecart", (Object)"minecraft:minecart").put((Object)"minecraft:chest_minecart", (Object)"minecraft:chest_minecart").put((Object)"minecraft:furnace_minecart", (Object)"minecraft:furnace_minecart").put((Object)"minecraft:tnt_minecart", (Object)"minecraft:tnt_minecart").put((Object)"minecraft:hopper_minecart", (Object)"minecraft:hopper_minecart").build();
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> dynamicOps, T t) {
            return V99.addNames(new Dynamic(dynamicOps, t), V704.ITEM_TO_BLOCKENTITY, ITEM_TO_ENTITY);
        }
    };

    public V705(int n, Schema schema) {
        super(n, schema);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> V100.equipment(schema));
    }

    protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap hashMap = Maps.newHashMap();
        schema.register((Map)hashMap, "minecraft:area_effect_cloud", string -> DSL.optionalFields((String)"Particle", (TypeTemplate)References.PARTICLE.in(schema)));
        V705.registerMob(schema, hashMap, "minecraft:armor_stand");
        schema.register((Map)hashMap, "minecraft:arrow", string -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, hashMap, "minecraft:bat");
        V705.registerMob(schema, hashMap, "minecraft:blaze");
        schema.registerSimple((Map)hashMap, "minecraft:boat");
        V705.registerMob(schema, hashMap, "minecraft:cave_spider");
        schema.register((Map)hashMap, "minecraft:chest_minecart", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
        V705.registerMob(schema, hashMap, "minecraft:chicken");
        schema.register((Map)hashMap, "minecraft:commandblock_minecart", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, hashMap, "minecraft:cow");
        V705.registerMob(schema, hashMap, "minecraft:creeper");
        schema.register((Map)hashMap, "minecraft:donkey", string -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in(schema), (TypeTemplate)V100.equipment(schema)));
        schema.registerSimple((Map)hashMap, "minecraft:dragon_fireball");
        V705.registerThrowableProjectile(schema, hashMap, "minecraft:egg");
        V705.registerMob(schema, hashMap, "minecraft:elder_guardian");
        schema.registerSimple((Map)hashMap, "minecraft:ender_crystal");
        V705.registerMob(schema, hashMap, "minecraft:ender_dragon");
        schema.register((Map)hashMap, "minecraft:enderman", string -> DSL.optionalFields((String)"carried", (TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)V100.equipment(schema)));
        V705.registerMob(schema, hashMap, "minecraft:endermite");
        V705.registerThrowableProjectile(schema, hashMap, "minecraft:ender_pearl");
        schema.registerSimple((Map)hashMap, "minecraft:eye_of_ender_signal");
        schema.register((Map)hashMap, "minecraft:falling_block", string -> DSL.optionalFields((String)"Block", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"TileEntityData", (TypeTemplate)References.BLOCK_ENTITY.in(schema)));
        V705.registerThrowableProjectile(schema, hashMap, "minecraft:fireball");
        schema.register((Map)hashMap, "minecraft:fireworks_rocket", string -> DSL.optionalFields((String)"FireworksItem", (TypeTemplate)References.ITEM_STACK.in(schema)));
        schema.register((Map)hashMap, "minecraft:furnace_minecart", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, hashMap, "minecraft:ghast");
        V705.registerMob(schema, hashMap, "minecraft:giant");
        V705.registerMob(schema, hashMap, "minecraft:guardian");
        schema.register((Map)hashMap, "minecraft:hopper_minecart", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
        schema.register((Map)hashMap, "minecraft:horse", string -> DSL.optionalFields((String)"ArmorItem", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in(schema), (TypeTemplate)V100.equipment(schema)));
        V705.registerMob(schema, hashMap, "minecraft:husk");
        schema.register((Map)hashMap, "minecraft:item", string -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in(schema)));
        schema.register((Map)hashMap, "minecraft:item_frame", string -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)hashMap, "minecraft:leash_knot");
        V705.registerMob(schema, hashMap, "minecraft:magma_cube");
        schema.register((Map)hashMap, "minecraft:minecart", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, hashMap, "minecraft:mooshroom");
        schema.register((Map)hashMap, "minecraft:mule", string -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in(schema), (TypeTemplate)V100.equipment(schema)));
        V705.registerMob(schema, hashMap, "minecraft:ocelot");
        schema.registerSimple((Map)hashMap, "minecraft:painting");
        schema.registerSimple((Map)hashMap, "minecraft:parrot");
        V705.registerMob(schema, hashMap, "minecraft:pig");
        V705.registerMob(schema, hashMap, "minecraft:polar_bear");
        schema.register((Map)hashMap, "minecraft:potion", string -> DSL.optionalFields((String)"Potion", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, hashMap, "minecraft:rabbit");
        V705.registerMob(schema, hashMap, "minecraft:sheep");
        V705.registerMob(schema, hashMap, "minecraft:shulker");
        schema.registerSimple((Map)hashMap, "minecraft:shulker_bullet");
        V705.registerMob(schema, hashMap, "minecraft:silverfish");
        V705.registerMob(schema, hashMap, "minecraft:skeleton");
        schema.register((Map)hashMap, "minecraft:skeleton_horse", string -> DSL.optionalFields((String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in(schema), (TypeTemplate)V100.equipment(schema)));
        V705.registerMob(schema, hashMap, "minecraft:slime");
        V705.registerThrowableProjectile(schema, hashMap, "minecraft:small_fireball");
        V705.registerThrowableProjectile(schema, hashMap, "minecraft:snowball");
        V705.registerMob(schema, hashMap, "minecraft:snowman");
        schema.register((Map)hashMap, "minecraft:spawner_minecart", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)References.UNTAGGED_SPAWNER.in(schema)));
        schema.register((Map)hashMap, "minecraft:spectral_arrow", string -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, hashMap, "minecraft:spider");
        V705.registerMob(schema, hashMap, "minecraft:squid");
        V705.registerMob(schema, hashMap, "minecraft:stray");
        schema.registerSimple((Map)hashMap, "minecraft:tnt");
        schema.register((Map)hashMap, "minecraft:tnt_minecart", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        schema.register((Map)hashMap, "minecraft:villager", string -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)), (String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)References.VILLAGER_TRADE.in(schema))), (TypeTemplate)V100.equipment(schema)));
        V705.registerMob(schema, hashMap, "minecraft:villager_golem");
        V705.registerMob(schema, hashMap, "minecraft:witch");
        V705.registerMob(schema, hashMap, "minecraft:wither");
        V705.registerMob(schema, hashMap, "minecraft:wither_skeleton");
        V705.registerThrowableProjectile(schema, hashMap, "minecraft:wither_skull");
        V705.registerMob(schema, hashMap, "minecraft:wolf");
        V705.registerThrowableProjectile(schema, hashMap, "minecraft:xp_bottle");
        schema.registerSimple((Map)hashMap, "minecraft:xp_orb");
        V705.registerMob(schema, hashMap, "minecraft:zombie");
        schema.register((Map)hashMap, "minecraft:zombie_horse", string -> DSL.optionalFields((String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in(schema), (TypeTemplate)V100.equipment(schema)));
        V705.registerMob(schema, hashMap, "minecraft:zombie_pigman");
        schema.register((Map)hashMap, "minecraft:zombie_villager", string -> DSL.optionalFields((String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)References.VILLAGER_TRADE.in(schema))), (TypeTemplate)V100.equipment(schema)));
        schema.registerSimple((Map)hashMap, "minecraft:evocation_fangs");
        V705.registerMob(schema, hashMap, "minecraft:evocation_illager");
        schema.registerSimple((Map)hashMap, "minecraft:illusion_illager");
        schema.register((Map)hashMap, "minecraft:llama", string -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"DecorItem", (TypeTemplate)References.ITEM_STACK.in(schema), (TypeTemplate)V100.equipment(schema)));
        schema.registerSimple((Map)hashMap, "minecraft:llama_spit");
        V705.registerMob(schema, hashMap, "minecraft:vex");
        V705.registerMob(schema, hashMap, "minecraft:vindication_illager");
        return hashMap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy((String)"id", V705.namespacedString(), (Map)map));
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)References.ITEM_NAME.in(schema), (String)"tag", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"EntityTag", (Object)References.ENTITY_TREE.in(schema)), Pair.of((Object)"BlockEntityTag", (Object)References.BLOCK_ENTITY.in(schema)), Pair.of((Object)"CanDestroy", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))), Pair.of((Object)"CanPlaceOn", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))), Pair.of((Object)"Items", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"ChargedProjectiles", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)))})), (Hook.HookFunction)ADD_NAMES, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
    }
}

