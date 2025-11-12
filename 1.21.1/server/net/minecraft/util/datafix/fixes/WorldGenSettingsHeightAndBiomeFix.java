/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WorldGenSettingsHeightAndBiomeFix
extends DataFix {
    private static final String NAME = "WorldGenSettingsHeightAndBiomeFix";
    public static final String WAS_PREVIOUSLY_INCREASED_KEY = "has_increased_height_already";

    public WorldGenSettingsHeightAndBiomeFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
        OpticFinder opticFinder = type.findField("dimensions");
        Type type2 = this.getOutputSchema().getType(References.WORLD_GEN_SETTINGS);
        Type type3 = type2.findFieldType("dimensions");
        return this.fixTypeEverywhereTyped(NAME, type, type2, typed2 -> {
            OptionalDynamic optionalDynamic = ((Dynamic)typed2.get(DSL.remainderFinder())).get(WAS_PREVIOUSLY_INCREASED_KEY);
            boolean bl = optionalDynamic.result().isEmpty();
            boolean bl2 = optionalDynamic.asBoolean(true);
            return typed2.update(DSL.remainderFinder(), dynamic -> dynamic.remove(WAS_PREVIOUSLY_INCREASED_KEY)).updateTyped(opticFinder, type3, typed -> Util.writeAndReadTypedOrThrow(typed, type3, dynamic2 -> dynamic2.update("minecraft:overworld", dynamic -> dynamic.update("generator", dynamic2 -> {
                String string = dynamic2.get("type").asString("");
                if ("minecraft:noise".equals(string)) {
                    MutableBoolean mutableBoolean = new MutableBoolean();
                    dynamic2 = dynamic2.update("biome_source", dynamic -> {
                        String string = dynamic.get("type").asString("");
                        if ("minecraft:vanilla_layered".equals(string) || bl && "minecraft:multi_noise".equals(string)) {
                            if (dynamic.get("large_biomes").asBoolean(false)) {
                                mutableBoolean.setTrue();
                            }
                            return dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("preset"), (Object)dynamic.createString("minecraft:overworld"), (Object)dynamic.createString("type"), (Object)dynamic.createString("minecraft:multi_noise")));
                        }
                        return dynamic;
                    });
                    if (mutableBoolean.booleanValue()) {
                        return dynamic2.update("settings", dynamic -> {
                            if ("minecraft:overworld".equals(dynamic.asString(""))) {
                                return dynamic.createString("minecraft:large_biomes");
                            }
                            return dynamic;
                        });
                    }
                    return dynamic2;
                }
                if ("minecraft:flat".equals(string)) {
                    if (bl2) {
                        return dynamic2;
                    }
                    return dynamic2.update("settings", dynamic -> dynamic.update("layers", WorldGenSettingsHeightAndBiomeFix::updateLayers));
                }
                return dynamic2;
            }))));
        });
    }

    private static Dynamic<?> updateLayers(Dynamic<?> dynamic) {
        Dynamic dynamic2 = dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("height"), (Object)dynamic.createInt(64), (Object)dynamic.createString("block"), (Object)dynamic.createString("minecraft:air")));
        return dynamic.createList(Stream.concat(Stream.of(dynamic2), dynamic.asStream()));
    }
}

