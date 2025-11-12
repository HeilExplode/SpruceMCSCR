/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
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
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V1451_6
extends NamespacedSchema {
    public static final String SPECIAL_OBJECTIVE_MARKER = "_special";
    protected static final Hook.HookFunction UNPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> dynamicOps, T t) {
            Dynamic dynamic = new Dynamic(dynamicOps, t);
            return (T)((Dynamic)DataFixUtils.orElse(dynamic.get("CriteriaName").asString().result().map(string -> {
                int n = string.indexOf(58);
                if (n < 0) {
                    return Pair.of((Object)V1451_6.SPECIAL_OBJECTIVE_MARKER, (Object)string);
                }
                try {
                    ResourceLocation resourceLocation = ResourceLocation.bySeparator(string.substring(0, n), '.');
                    ResourceLocation resourceLocation2 = ResourceLocation.bySeparator(string.substring(n + 1), '.');
                    return Pair.of((Object)resourceLocation.toString(), (Object)resourceLocation2.toString());
                }
                catch (Exception exception) {
                    return Pair.of((Object)V1451_6.SPECIAL_OBJECTIVE_MARKER, (Object)string);
                }
            }).map(pair -> dynamic.set("CriteriaType", dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("type"), (Object)dynamic.createString((String)pair.getFirst()), (Object)dynamic.createString("id"), (Object)dynamic.createString((String)pair.getSecond()))))), (Object)dynamic)).getValue();
        }
    };
    protected static final Hook.HookFunction REPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> dynamicOps, T t) {
            Dynamic dynamic = new Dynamic(dynamicOps, t);
            Optional<Dynamic> optional = dynamic.get("CriteriaType").get().result().flatMap(dynamic2 -> {
                Optional optional = dynamic2.get("type").asString().result();
                Optional optional2 = dynamic2.get("id").asString().result();
                if (optional.isPresent() && optional2.isPresent()) {
                    String string = (String)optional.get();
                    if (string.equals(V1451_6.SPECIAL_OBJECTIVE_MARKER)) {
                        return Optional.of(dynamic.createString((String)optional2.get()));
                    }
                    return Optional.of(dynamic2.createString(V1451_6.packNamespacedWithDot(string) + ":" + V1451_6.packNamespacedWithDot((String)optional2.get())));
                }
                return Optional.empty();
            });
            return (T)((Dynamic)DataFixUtils.orElse(optional.map(dynamic2 -> dynamic.set("CriteriaName", dynamic2).remove("CriteriaType")), (Object)dynamic)).getValue();
        }
    };

    public V1451_6(int n, Schema schema) {
        super(n, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        Supplier<TypeTemplate> supplier = () -> DSL.compoundList((TypeTemplate)References.ITEM_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()));
        schema.registerType(false, References.STATS, () -> DSL.optionalFields((String)"stats", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"minecraft:mined", (Object)DSL.compoundList((TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:crafted", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:used", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:broken", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:picked_up", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:dropped", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:killed", (Object)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:killed_by", (Object)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:custom", (Object)DSL.compoundList((TypeTemplate)DSL.constType(V1451_6.namespacedString()), (TypeTemplate)DSL.constType((Type)DSL.intType())))})));
        Map<String, Supplier<TypeTemplate>> map3 = V1451_6.createCriterionTypes(schema);
        schema.registerType(false, References.OBJECTIVE, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"CriteriaType", (TypeTemplate)DSL.taggedChoiceLazy((String)"type", (Type)DSL.string(), (Map)map3)), (Hook.HookFunction)UNPACK_OBJECTIVE_ID, (Hook.HookFunction)REPACK_OBJECTIVE_ID));
    }

    protected static Map<String, Supplier<TypeTemplate>> createCriterionTypes(Schema schema) {
        Supplier<TypeTemplate> supplier = () -> DSL.optionalFields((String)"id", (TypeTemplate)References.ITEM_NAME.in(schema));
        Supplier<TypeTemplate> supplier2 = () -> DSL.optionalFields((String)"id", (TypeTemplate)References.BLOCK_NAME.in(schema));
        Supplier<TypeTemplate> supplier3 = () -> DSL.optionalFields((String)"id", (TypeTemplate)References.ENTITY_NAME.in(schema));
        HashMap hashMap = Maps.newHashMap();
        hashMap.put("minecraft:mined", supplier2);
        hashMap.put("minecraft:crafted", supplier);
        hashMap.put("minecraft:used", supplier);
        hashMap.put("minecraft:broken", supplier);
        hashMap.put("minecraft:picked_up", supplier);
        hashMap.put("minecraft:dropped", supplier);
        hashMap.put("minecraft:killed", supplier3);
        hashMap.put("minecraft:killed_by", supplier3);
        hashMap.put("minecraft:custom", () -> DSL.optionalFields((String)"id", (TypeTemplate)DSL.constType(V1451_6.namespacedString())));
        hashMap.put(SPECIAL_OBJECTIVE_MARKER, () -> DSL.optionalFields((String)"id", (TypeTemplate)DSL.constType((Type)DSL.string())));
        return hashMap;
    }

    public static String packNamespacedWithDot(String string) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
        return resourceLocation != null ? resourceLocation.getNamespace() + "." + resourceLocation.getPath() : string;
    }
}

