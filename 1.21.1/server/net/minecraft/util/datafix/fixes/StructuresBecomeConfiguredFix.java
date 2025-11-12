/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class StructuresBecomeConfiguredFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Conversion> CONVERSION_MAP = ImmutableMap.builder().put((Object)"mineshaft", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put((Object)"shipwreck", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck")).put((Object)"ocean_ruin", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put((Object)"village", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:village_desert", List.of("minecraft:savanna"), "minecraft:village_savanna", List.of("minecraft:snowy_plains"), "minecraft:village_snowy", List.of("minecraft:taiga"), "minecraft:village_taiga"), "minecraft:village_plains")).put((Object)"ruined_portal", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:ruined_portal_desert", List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "minecraft:ruined_portal_mountain", List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"), "minecraft:ruined_portal_jungle", List.of("minecraft:deep_frozen_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean"), "minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put((Object)"pillager_outpost", (Object)Conversion.trivial("minecraft:pillager_outpost")).put((Object)"mansion", (Object)Conversion.trivial("minecraft:mansion")).put((Object)"jungle_pyramid", (Object)Conversion.trivial("minecraft:jungle_pyramid")).put((Object)"desert_pyramid", (Object)Conversion.trivial("minecraft:desert_pyramid")).put((Object)"igloo", (Object)Conversion.trivial("minecraft:igloo")).put((Object)"swamp_hut", (Object)Conversion.trivial("minecraft:swamp_hut")).put((Object)"stronghold", (Object)Conversion.trivial("minecraft:stronghold")).put((Object)"monument", (Object)Conversion.trivial("minecraft:monument")).put((Object)"fortress", (Object)Conversion.trivial("minecraft:fortress")).put((Object)"endcity", (Object)Conversion.trivial("minecraft:end_city")).put((Object)"buried_treasure", (Object)Conversion.trivial("minecraft:buried_treasure")).put((Object)"nether_fossil", (Object)Conversion.trivial("minecraft:nether_fossil")).put((Object)"bastion_remnant", (Object)Conversion.trivial("minecraft:bastion_remnant")).build();

    public StructuresBecomeConfiguredFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        Type type2 = this.getInputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("StucturesToConfiguredStructures", type, type2, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.update("structures", dynamic3 -> dynamic3.update("starts", dynamic2 -> this.updateStarts((Dynamic<?>)dynamic2, dynamic)).update("References", dynamic2 -> this.updateReferences((Dynamic<?>)dynamic2, dynamic)));
    }

    private Dynamic<?> updateStarts(Dynamic<?> dynamic, Dynamic<?> dynamic3) {
        Map<Dynamic, Dynamic> map = dynamic.getMapValues().result().orElse(Map.of());
        HashMap hashMap = Maps.newHashMap();
        map.forEach((dynamic2, dynamic4) -> {
            if (dynamic4.get("id").asString("INVALID").equals("INVALID")) {
                return;
            }
            Dynamic<?> dynamic5 = this.findUpdatedStructureType((Dynamic<?>)dynamic2, dynamic3);
            if (dynamic5 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic2.asString("<missing key>"));
                return;
            }
            hashMap.computeIfAbsent(dynamic5, dynamic3 -> dynamic4.set("id", dynamic5));
        });
        return dynamic3.createMap((Map)hashMap);
    }

    private Dynamic<?> updateReferences(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        Map<Dynamic, Dynamic> map = dynamic.getMapValues().result().orElse(Map.of());
        HashMap hashMap = Maps.newHashMap();
        map.forEach((dynamic4, dynamic5) -> {
            if (dynamic5.asLongStream().count() == 0L) {
                return;
            }
            Dynamic<?> dynamic6 = this.findUpdatedStructureType((Dynamic<?>)dynamic4, dynamic2);
            if (dynamic6 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic4.asString("<missing key>"));
                return;
            }
            hashMap.compute(dynamic6, (dynamic2, dynamic3) -> {
                if (dynamic3 == null) {
                    return dynamic5;
                }
                return dynamic5.createLongList(LongStream.concat(dynamic3.asLongStream(), dynamic5.asLongStream()));
            });
        });
        return dynamic2.createMap((Map)hashMap);
    }

    @Nullable
    private Dynamic<?> findUpdatedStructureType(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        Optional<String> optional;
        String string = dynamic.asString("UNKNOWN").toLowerCase(Locale.ROOT);
        Conversion conversion = CONVERSION_MAP.get(string);
        if (conversion == null) {
            return null;
        }
        String string2 = conversion.fallback;
        if (!conversion.biomeMapping().isEmpty() && (optional = this.guessConfiguration(dynamic2, conversion)).isPresent()) {
            string2 = optional.get();
        }
        return dynamic2.createString(string2);
    }

    private Optional<String> guessConfiguration(Dynamic<?> dynamic, Conversion conversion) {
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        dynamic.get("sections").asList(Function.identity()).forEach(dynamic2 -> dynamic2.get("biomes").get("palette").asList(Function.identity()).forEach(dynamic -> {
            String string = conversion.biomeMapping().get(dynamic.asString(""));
            if (string != null) {
                object2IntArrayMap.mergeInt((Object)string, 1, Integer::sum);
            }
        }));
        return object2IntArrayMap.object2IntEntrySet().stream().max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey);
    }

    static final class Conversion
    extends Record {
        private final Map<String, String> biomeMapping;
        final String fallback;

        private Conversion(Map<String, String> map, String string) {
            this.biomeMapping = map;
            this.fallback = string;
        }

        public static Conversion trivial(String string) {
            return new Conversion(Map.of(), string);
        }

        public static Conversion biomeMapped(Map<List<String>, String> map, String string) {
            return new Conversion(Conversion.unpack(map), string);
        }

        private static Map<String, String> unpack(Map<List<String>, String> map) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Map.Entry<List<String>, String> entry : map.entrySet()) {
                entry.getKey().forEach(string -> builder.put(string, (Object)((String)entry.getValue())));
            }
            return builder.build();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this, object);
        }

        public Map<String, String> biomeMapping() {
            return this.biomeMapping;
        }

        public String fallback() {
            return this.fallback;
        }
    }
}

