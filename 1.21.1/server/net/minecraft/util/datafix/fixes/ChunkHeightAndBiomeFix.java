/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.ChunkProtoTickListFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

public class ChunkHeightAndBiomeFix
extends DataFix {
    public static final String DATAFIXER_CONTEXT_TAG = "__context";
    private static final String NAME = "ChunkHeightAndBiomeFix";
    private static final int OLD_SECTION_COUNT = 16;
    private static final int NEW_SECTION_COUNT = 24;
    private static final int NEW_MIN_SECTION_Y = -4;
    public static final int BLOCKS_PER_SECTION = 4096;
    private static final int LONGS_PER_SECTION = 64;
    private static final int HEIGHTMAP_BITS = 9;
    private static final long HEIGHTMAP_MASK = 511L;
    private static final int HEIGHTMAP_OFFSET = 64;
    private static final String[] HEIGHTMAP_TYPES = new String[]{"WORLD_SURFACE_WG", "WORLD_SURFACE", "WORLD_SURFACE_IGNORE_SNOW", "OCEAN_FLOOR_WG", "OCEAN_FLOOR", "MOTION_BLOCKING", "MOTION_BLOCKING_NO_LEAVES"};
    private static final Set<String> STATUS_IS_OR_AFTER_SURFACE = Set.of("surface", "carvers", "liquid_carvers", "features", "light", "spawn", "heightmaps", "full");
    private static final Set<String> STATUS_IS_OR_AFTER_NOISE = Set.of("noise", "surface", "carvers", "liquid_carvers", "features", "light", "spawn", "heightmaps", "full");
    private static final Set<String> BLOCKS_BEFORE_FEATURE_STATUS = Set.of("minecraft:air", "minecraft:basalt", "minecraft:bedrock", "minecraft:blackstone", "minecraft:calcite", "minecraft:cave_air", "minecraft:coarse_dirt", "minecraft:crimson_nylium", "minecraft:dirt", "minecraft:end_stone", "minecraft:grass_block", "minecraft:gravel", "minecraft:ice", "minecraft:lava", "minecraft:mycelium", "minecraft:nether_wart_block", "minecraft:netherrack", "minecraft:orange_terracotta", "minecraft:packed_ice", "minecraft:podzol", "minecraft:powder_snow", "minecraft:red_sand", "minecraft:red_sandstone", "minecraft:sand", "minecraft:sandstone", "minecraft:snow_block", "minecraft:soul_sand", "minecraft:soul_soil", "minecraft:stone", "minecraft:terracotta", "minecraft:warped_nylium", "minecraft:warped_wart_block", "minecraft:water", "minecraft:white_terracotta");
    private static final int BIOME_CONTAINER_LAYER_SIZE = 16;
    private static final int BIOME_CONTAINER_SIZE = 64;
    private static final int BIOME_CONTAINER_TOP_LAYER_OFFSET = 1008;
    public static final String DEFAULT_BIOME = "minecraft:plains";
    private static final Int2ObjectMap<String> BIOMES_BY_ID = new Int2ObjectOpenHashMap();

    public ChunkHeightAndBiomeFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder opticFinder = type.findField("Level");
        OpticFinder opticFinder2 = opticFinder.type().findField("Sections");
        Schema schema = this.getOutputSchema();
        Type type2 = schema.getType(References.CHUNK);
        Type type3 = type2.findField("Level").type();
        Type type4 = type3.findField("Sections").type();
        return this.fixTypeEverywhereTyped(NAME, type, type2, typed -> typed.updateTyped(opticFinder, type3, typed3 -> {
            Dynamic dynamic2 = (Dynamic)typed3.get(DSL.remainderFinder());
            OptionalDynamic optionalDynamic = ((Dynamic)typed.get(DSL.remainderFinder())).get(DATAFIXER_CONTEXT_TAG);
            String string = optionalDynamic.get("dimension").asString().result().orElse("");
            String string2 = optionalDynamic.get("generator").asString().result().orElse("");
            boolean bl = "minecraft:overworld".equals(string);
            MutableBoolean mutableBoolean = new MutableBoolean();
            int n = bl ? -4 : 0;
            Dynamic<?>[] dynamicArray = ChunkHeightAndBiomeFix.getBiomeContainers(dynamic2, bl, n, mutableBoolean);
            Dynamic<?> dynamic3 = ChunkHeightAndBiomeFix.makePalettedContainer(dynamic2.createList(Stream.of(dynamic2.createMap((Map)ImmutableMap.of((Object)dynamic2.createString("Name"), (Object)dynamic2.createString("minecraft:air"))))));
            HashSet hashSet = Sets.newHashSet();
            MutableObject mutableObject = new MutableObject(() -> null);
            typed3 = typed3.updateTyped(opticFinder2, type4, typed -> {
                IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
                Dynamic dynamic3 = (Dynamic)typed.write().result().orElseThrow(() -> new IllegalStateException("Malformed Chunk.Level.Sections"));
                List list = dynamic3.asStream().map(arg_0 -> ChunkHeightAndBiomeFix.lambda$makeRule$6(hashSet, dynamic3, n, dynamicArray, (IntSet)intOpenHashSet, mutableObject, arg_0)).collect(Collectors.toCollection(ArrayList::new));
                for (int i = 0; i < dynamicArray.length; ++i) {
                    int n2 = i + n;
                    if (!intOpenHashSet.add(n2)) continue;
                    Dynamic dynamic4 = dynamic2.createMap(Map.of(dynamic2.createString("Y"), dynamic2.createInt(n2)));
                    dynamic4 = dynamic4.set("block_states", dynamic3);
                    dynamic4 = dynamic4.set("biomes", dynamicArray[i]);
                    list.add(dynamic4);
                }
                return Util.readTypedOrThrow(type4, dynamic2.createList(list.stream()));
            });
            return typed3.update(DSL.remainderFinder(), dynamic -> {
                if (bl) {
                    dynamic = this.predictChunkStatusBeforeSurface((Dynamic<?>)dynamic, hashSet);
                }
                return ChunkHeightAndBiomeFix.updateChunkTag(dynamic, bl, mutableBoolean.booleanValue(), "minecraft:noise".equals(string2), (Supplier)mutableObject.getValue());
            });
        }));
    }

    private Dynamic<?> predictChunkStatusBeforeSurface(Dynamic<?> dynamic2, Set<String> set) {
        return dynamic2.update("Status", dynamic -> {
            boolean bl;
            String string = dynamic.asString("empty");
            if (STATUS_IS_OR_AFTER_SURFACE.contains(string)) {
                return dynamic;
            }
            set.remove("minecraft:air");
            boolean bl2 = !set.isEmpty();
            set.removeAll(BLOCKS_BEFORE_FEATURE_STATUS);
            boolean bl3 = bl = !set.isEmpty();
            if (bl) {
                return dynamic.createString("liquid_carvers");
            }
            if ("noise".equals(string) || bl2) {
                return dynamic.createString("noise");
            }
            if ("biomes".equals(string)) {
                return dynamic.createString("structure_references");
            }
            return dynamic;
        });
    }

    private static Dynamic<?>[] getBiomeContainers(Dynamic<?> dynamic, boolean bl, int n3, MutableBoolean mutableBoolean) {
        Object[] objectArray = new Dynamic[bl ? 24 : 16];
        int[] nArray = dynamic.get("Biomes").asIntStreamOpt().result().map(IntStream::toArray).orElse(null);
        if (nArray != null && nArray.length == 1536) {
            mutableBoolean.setValue(true);
            for (int i = 0; i < 24; ++i) {
                int n4 = i;
                objectArray[i] = ChunkHeightAndBiomeFix.makeBiomeContainer(dynamic, n2 -> ChunkHeightAndBiomeFix.getOldBiome(nArray, n4 * 64 + n2));
            }
        } else if (nArray != null && nArray.length == 1024) {
            int n5;
            int n6 = 0;
            while (n6 < 16) {
                int n7 = n6 - n3;
                n5 = n6++;
                objectArray[n7] = ChunkHeightAndBiomeFix.makeBiomeContainer(dynamic, n2 -> ChunkHeightAndBiomeFix.getOldBiome(nArray, n5 * 64 + n2));
            }
            if (bl) {
                Dynamic<?> dynamic2 = ChunkHeightAndBiomeFix.makeBiomeContainer(dynamic, n -> ChunkHeightAndBiomeFix.getOldBiome(nArray, n % 16));
                Dynamic<?> dynamic3 = ChunkHeightAndBiomeFix.makeBiomeContainer(dynamic, n -> ChunkHeightAndBiomeFix.getOldBiome(nArray, n % 16 + 1008));
                for (n5 = 0; n5 < 4; ++n5) {
                    objectArray[n5] = dynamic2;
                }
                for (n5 = 20; n5 < 24; ++n5) {
                    objectArray[n5] = dynamic3;
                }
            }
        } else {
            Arrays.fill(objectArray, ChunkHeightAndBiomeFix.makePalettedContainer(dynamic.createList(Stream.of(dynamic.createString(DEFAULT_BIOME)))));
        }
        return objectArray;
    }

    private static int getOldBiome(int[] nArray, int n) {
        return nArray[n] & 0xFF;
    }

    private static Dynamic<?> updateChunkTag(Dynamic<?> dynamic, boolean bl, boolean bl2, boolean bl3, Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> supplier) {
        Dynamic dynamic2;
        String string;
        dynamic = dynamic.remove("Biomes");
        if (!bl) {
            return ChunkHeightAndBiomeFix.updateCarvingMasks(dynamic, 16, 0);
        }
        if (bl2) {
            return ChunkHeightAndBiomeFix.updateCarvingMasks(dynamic, 24, 0);
        }
        dynamic = ChunkHeightAndBiomeFix.updateHeightmaps(dynamic);
        dynamic = ChunkHeightAndBiomeFix.addPaddingEntries(dynamic, "LiquidsToBeTicked");
        dynamic = ChunkHeightAndBiomeFix.addPaddingEntries(dynamic, "PostProcessing");
        dynamic = ChunkHeightAndBiomeFix.addPaddingEntries(dynamic, "ToBeTicked");
        dynamic = ChunkHeightAndBiomeFix.updateCarvingMasks(dynamic, 24, 4);
        dynamic = dynamic.update("UpgradeData", ChunkHeightAndBiomeFix::shiftUpgradeData);
        if (!bl3) {
            return dynamic;
        }
        Optional optional = dynamic.get("Status").result();
        if (optional.isPresent() && !"empty".equals(string = (dynamic2 = (Dynamic)optional.get()).asString(""))) {
            dynamic = dynamic.set("blending_data", dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("old_noise"), (Object)dynamic.createBoolean(STATUS_IS_OR_AFTER_NOISE.contains(string)))));
            ChunkProtoTickListFix.PoorMansPalettedContainer poorMansPalettedContainer = supplier.get();
            if (poorMansPalettedContainer != null) {
                BitSet bitSet = new BitSet(256);
                boolean bl4 = string.equals("noise");
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        boolean bl5;
                        Dynamic<?> dynamic3 = poorMansPalettedContainer.get(j, 0, i);
                        boolean bl6 = dynamic3 != null && "minecraft:bedrock".equals(dynamic3.get("Name").asString(""));
                        boolean bl7 = bl5 = dynamic3 != null && "minecraft:air".equals(dynamic3.get("Name").asString(""));
                        if (bl5) {
                            bitSet.set(i * 16 + j);
                        }
                        bl4 |= bl6;
                    }
                }
                if (bl4 && bitSet.cardinality() != bitSet.size()) {
                    Dynamic dynamic4 = "full".equals(string) ? dynamic.createString("heightmaps") : dynamic2;
                    dynamic = dynamic.set("below_zero_retrogen", dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("target_status"), (Object)dynamic4, (Object)dynamic.createString("missing_bedrock"), (Object)dynamic.createLongList(LongStream.of(bitSet.toLongArray())))));
                    dynamic = dynamic.set("Status", dynamic.createString("empty"));
                }
                dynamic = dynamic.set("isLightOn", dynamic.createBoolean(false));
            }
        }
        return dynamic;
    }

    private static <T> Dynamic<T> shiftUpgradeData(Dynamic<T> dynamic2) {
        return dynamic2.update("Indices", dynamic -> {
            HashMap hashMap = new HashMap();
            dynamic.getMapValues().ifSuccess(map2 -> map2.forEach((dynamic, dynamic2) -> {
                try {
                    dynamic.asString().result().map(Integer::parseInt).ifPresent(n -> {
                        int n2 = n - -4;
                        hashMap.put(dynamic.createString(Integer.toString(n2)), dynamic2);
                    });
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }));
            return dynamic.createMap(hashMap);
        });
    }

    private static Dynamic<?> updateCarvingMasks(Dynamic<?> dynamic, int n, int n2) {
        Dynamic dynamic2 = dynamic.get("CarvingMasks").orElseEmptyMap();
        dynamic2 = dynamic2.updateMapValues(pair -> {
            long[] lArray = BitSet.valueOf(((Dynamic)pair.getSecond()).asByteBuffer().array()).toLongArray();
            long[] lArray2 = new long[64 * n];
            System.arraycopy(lArray, 0, lArray2, 64 * n2, lArray.length);
            return Pair.of((Object)((Dynamic)pair.getFirst()), (Object)dynamic.createLongList(LongStream.of(lArray2)));
        });
        return dynamic.set("CarvingMasks", dynamic2);
    }

    private static Dynamic<?> addPaddingEntries(Dynamic<?> dynamic, String string) {
        List list = dynamic.get(string).orElseEmptyList().asStream().collect(Collectors.toCollection(ArrayList::new));
        if (list.size() == 24) {
            return dynamic;
        }
        Dynamic dynamic2 = dynamic.emptyList();
        for (int i = 0; i < 4; ++i) {
            list.add(0, dynamic2);
            list.add(dynamic2);
        }
        return dynamic.set(string, dynamic.createList(list.stream()));
    }

    private static Dynamic<?> updateHeightmaps(Dynamic<?> dynamic2) {
        return dynamic2.update("Heightmaps", dynamic -> {
            for (String string : HEIGHTMAP_TYPES) {
                dynamic = dynamic.update(string, ChunkHeightAndBiomeFix::getFixedHeightmap);
            }
            return dynamic;
        });
    }

    private static Dynamic<?> getFixedHeightmap(Dynamic<?> dynamic) {
        return dynamic.createLongList(dynamic.asLongStream().map(l -> {
            long l2 = 0L;
            int n = 0;
            while (n + 9 <= 64) {
                long l3 = l >> n & 0x1FFL;
                long l4 = l3 == 0L ? 0L : Math.min(l3 + 64L, 511L);
                l2 |= l4 << n;
                n += 9;
            }
            return l2;
        }));
    }

    private static Dynamic<?> makeBiomeContainer(Dynamic<?> dynamic, Int2IntFunction int2IntFunction) {
        int n2;
        Int2IntLinkedOpenHashMap int2IntLinkedOpenHashMap = new Int2IntLinkedOpenHashMap();
        for (int i = 0; i < 64; ++i) {
            n2 = int2IntFunction.applyAsInt(i);
            if (int2IntLinkedOpenHashMap.containsKey(n2)) continue;
            int2IntLinkedOpenHashMap.put(n2, int2IntLinkedOpenHashMap.size());
        }
        Dynamic dynamic2 = dynamic.createList(int2IntLinkedOpenHashMap.keySet().stream().map(n -> dynamic.createString((String)BIOMES_BY_ID.getOrDefault(n.intValue(), (Object)DEFAULT_BIOME))));
        n2 = ChunkHeightAndBiomeFix.ceillog2(int2IntLinkedOpenHashMap.size());
        if (n2 == 0) {
            return ChunkHeightAndBiomeFix.makePalettedContainer(dynamic2);
        }
        int n3 = 64 / n2;
        int n4 = (64 + n3 - 1) / n3;
        long[] lArray = new long[n4];
        int n5 = 0;
        int n6 = 0;
        for (int i = 0; i < 64; ++i) {
            int n7 = int2IntFunction.applyAsInt(i);
            int n8 = n5++;
            lArray[n8] = lArray[n8] | (long)int2IntLinkedOpenHashMap.get(n7) << n6;
            if ((n6 += n2) + n2 <= 64) continue;
            n6 = 0;
        }
        Dynamic dynamic3 = dynamic.createLongList(Arrays.stream(lArray));
        return ChunkHeightAndBiomeFix.makePalettedContainer(dynamic2, dynamic3);
    }

    private static Dynamic<?> makePalettedContainer(Dynamic<?> dynamic) {
        return dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("palette"), dynamic));
    }

    private static Dynamic<?> makePalettedContainer(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        return dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("palette"), dynamic, (Object)dynamic.createString("data"), dynamic2));
    }

    private static Dynamic<?> makeOptimizedPalettedContainer(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        List list = dynamic.asStream().collect(Collectors.toCollection(ArrayList::new));
        if (list.size() == 1) {
            return ChunkHeightAndBiomeFix.makePalettedContainer(dynamic);
        }
        dynamic = ChunkHeightAndBiomeFix.padPaletteEntries(dynamic, dynamic2, list);
        return ChunkHeightAndBiomeFix.makePalettedContainer(dynamic, dynamic2);
    }

    private static Dynamic<?> padPaletteEntries(Dynamic<?> dynamic, Dynamic<?> dynamic2, List<Dynamic<?>> list) {
        int n;
        int n2;
        long l = dynamic2.asLongStream().count() * 64L;
        long l2 = l / 4096L;
        if (l2 > (long)(n2 = ChunkHeightAndBiomeFix.ceillog2(n = list.size()))) {
            Dynamic dynamic3 = dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("Name"), (Object)dynamic.createString("minecraft:air")));
            int n3 = (1 << (int)(l2 - 1L)) + 1;
            int n4 = n3 - n;
            for (int i = 0; i < n4; ++i) {
                list.add(dynamic3);
            }
            return dynamic.createList(list.stream());
        }
        return dynamic;
    }

    public static int ceillog2(int n) {
        if (n == 0) {
            return 0;
        }
        return (int)Math.ceil(Math.log(n) / Math.log(2.0));
    }

    private static /* synthetic */ Dynamic lambda$makeRule$6(Set set, Dynamic dynamic, int n, Dynamic[] dynamicArray, IntSet intSet, MutableObject mutableObject, Dynamic dynamic2) {
        int n2 = dynamic2.get("Y").asInt(0);
        Dynamic dynamic3 = (Dynamic)DataFixUtils.orElse(dynamic2.get("Palette").result().flatMap(dynamic4 -> {
            dynamic4.asStream().map(dynamic -> dynamic.get("Name").asString("minecraft:air")).forEach(set::add);
            return dynamic2.get("BlockStates").result().map(dynamic2 -> ChunkHeightAndBiomeFix.makeOptimizedPalettedContainer(dynamic4, dynamic2));
        }), (Object)dynamic);
        Dynamic dynamic5 = dynamic2;
        int n3 = n2 - n;
        if (n3 >= 0 && n3 < dynamicArray.length) {
            dynamic5 = dynamic5.set("biomes", dynamicArray[n3]);
        }
        intSet.add(n2);
        if (dynamic2.get("Y").asInt(Integer.MAX_VALUE) == 0) {
            mutableObject.setValue(() -> {
                List list = dynamic3.get("palette").asList(Function.identity());
                long[] lArray = dynamic3.get("data").asLongStream().toArray();
                return new ChunkProtoTickListFix.PoorMansPalettedContainer(list, lArray);
            });
        }
        return dynamic5.set("block_states", dynamic3).remove("Palette").remove("BlockStates");
    }

    static {
        BIOMES_BY_ID.put(0, (Object)"minecraft:ocean");
        BIOMES_BY_ID.put(1, (Object)DEFAULT_BIOME);
        BIOMES_BY_ID.put(2, (Object)"minecraft:desert");
        BIOMES_BY_ID.put(3, (Object)"minecraft:mountains");
        BIOMES_BY_ID.put(4, (Object)"minecraft:forest");
        BIOMES_BY_ID.put(5, (Object)"minecraft:taiga");
        BIOMES_BY_ID.put(6, (Object)"minecraft:swamp");
        BIOMES_BY_ID.put(7, (Object)"minecraft:river");
        BIOMES_BY_ID.put(8, (Object)"minecraft:nether_wastes");
        BIOMES_BY_ID.put(9, (Object)"minecraft:the_end");
        BIOMES_BY_ID.put(10, (Object)"minecraft:frozen_ocean");
        BIOMES_BY_ID.put(11, (Object)"minecraft:frozen_river");
        BIOMES_BY_ID.put(12, (Object)"minecraft:snowy_tundra");
        BIOMES_BY_ID.put(13, (Object)"minecraft:snowy_mountains");
        BIOMES_BY_ID.put(14, (Object)"minecraft:mushroom_fields");
        BIOMES_BY_ID.put(15, (Object)"minecraft:mushroom_field_shore");
        BIOMES_BY_ID.put(16, (Object)"minecraft:beach");
        BIOMES_BY_ID.put(17, (Object)"minecraft:desert_hills");
        BIOMES_BY_ID.put(18, (Object)"minecraft:wooded_hills");
        BIOMES_BY_ID.put(19, (Object)"minecraft:taiga_hills");
        BIOMES_BY_ID.put(20, (Object)"minecraft:mountain_edge");
        BIOMES_BY_ID.put(21, (Object)"minecraft:jungle");
        BIOMES_BY_ID.put(22, (Object)"minecraft:jungle_hills");
        BIOMES_BY_ID.put(23, (Object)"minecraft:jungle_edge");
        BIOMES_BY_ID.put(24, (Object)"minecraft:deep_ocean");
        BIOMES_BY_ID.put(25, (Object)"minecraft:stone_shore");
        BIOMES_BY_ID.put(26, (Object)"minecraft:snowy_beach");
        BIOMES_BY_ID.put(27, (Object)"minecraft:birch_forest");
        BIOMES_BY_ID.put(28, (Object)"minecraft:birch_forest_hills");
        BIOMES_BY_ID.put(29, (Object)"minecraft:dark_forest");
        BIOMES_BY_ID.put(30, (Object)"minecraft:snowy_taiga");
        BIOMES_BY_ID.put(31, (Object)"minecraft:snowy_taiga_hills");
        BIOMES_BY_ID.put(32, (Object)"minecraft:giant_tree_taiga");
        BIOMES_BY_ID.put(33, (Object)"minecraft:giant_tree_taiga_hills");
        BIOMES_BY_ID.put(34, (Object)"minecraft:wooded_mountains");
        BIOMES_BY_ID.put(35, (Object)"minecraft:savanna");
        BIOMES_BY_ID.put(36, (Object)"minecraft:savanna_plateau");
        BIOMES_BY_ID.put(37, (Object)"minecraft:badlands");
        BIOMES_BY_ID.put(38, (Object)"minecraft:wooded_badlands_plateau");
        BIOMES_BY_ID.put(39, (Object)"minecraft:badlands_plateau");
        BIOMES_BY_ID.put(40, (Object)"minecraft:small_end_islands");
        BIOMES_BY_ID.put(41, (Object)"minecraft:end_midlands");
        BIOMES_BY_ID.put(42, (Object)"minecraft:end_highlands");
        BIOMES_BY_ID.put(43, (Object)"minecraft:end_barrens");
        BIOMES_BY_ID.put(44, (Object)"minecraft:warm_ocean");
        BIOMES_BY_ID.put(45, (Object)"minecraft:lukewarm_ocean");
        BIOMES_BY_ID.put(46, (Object)"minecraft:cold_ocean");
        BIOMES_BY_ID.put(47, (Object)"minecraft:deep_warm_ocean");
        BIOMES_BY_ID.put(48, (Object)"minecraft:deep_lukewarm_ocean");
        BIOMES_BY_ID.put(49, (Object)"minecraft:deep_cold_ocean");
        BIOMES_BY_ID.put(50, (Object)"minecraft:deep_frozen_ocean");
        BIOMES_BY_ID.put(127, (Object)"minecraft:the_void");
        BIOMES_BY_ID.put(129, (Object)"minecraft:sunflower_plains");
        BIOMES_BY_ID.put(130, (Object)"minecraft:desert_lakes");
        BIOMES_BY_ID.put(131, (Object)"minecraft:gravelly_mountains");
        BIOMES_BY_ID.put(132, (Object)"minecraft:flower_forest");
        BIOMES_BY_ID.put(133, (Object)"minecraft:taiga_mountains");
        BIOMES_BY_ID.put(134, (Object)"minecraft:swamp_hills");
        BIOMES_BY_ID.put(140, (Object)"minecraft:ice_spikes");
        BIOMES_BY_ID.put(149, (Object)"minecraft:modified_jungle");
        BIOMES_BY_ID.put(151, (Object)"minecraft:modified_jungle_edge");
        BIOMES_BY_ID.put(155, (Object)"minecraft:tall_birch_forest");
        BIOMES_BY_ID.put(156, (Object)"minecraft:tall_birch_hills");
        BIOMES_BY_ID.put(157, (Object)"minecraft:dark_forest_hills");
        BIOMES_BY_ID.put(158, (Object)"minecraft:snowy_taiga_mountains");
        BIOMES_BY_ID.put(160, (Object)"minecraft:giant_spruce_taiga");
        BIOMES_BY_ID.put(161, (Object)"minecraft:giant_spruce_taiga_hills");
        BIOMES_BY_ID.put(162, (Object)"minecraft:modified_gravelly_mountains");
        BIOMES_BY_ID.put(163, (Object)"minecraft:shattered_savanna");
        BIOMES_BY_ID.put(164, (Object)"minecraft:shattered_savanna_plateau");
        BIOMES_BY_ID.put(165, (Object)"minecraft:eroded_badlands");
        BIOMES_BY_ID.put(166, (Object)"minecraft:modified_wooded_badlands_plateau");
        BIOMES_BY_ID.put(167, (Object)"minecraft:modified_badlands_plateau");
        BIOMES_BY_ID.put(168, (Object)"minecraft:bamboo_jungle");
        BIOMES_BY_ID.put(169, (Object)"minecraft:bamboo_jungle_hills");
        BIOMES_BY_ID.put(170, (Object)"minecraft:soul_sand_valley");
        BIOMES_BY_ID.put(171, (Object)"minecraft:crimson_forest");
        BIOMES_BY_ID.put(172, (Object)"minecraft:warped_forest");
        BIOMES_BY_ID.put(173, (Object)"minecraft:basalt_deltas");
        BIOMES_BY_ID.put(174, (Object)"minecraft:dripstone_caves");
        BIOMES_BY_ID.put(175, (Object)"minecraft:lush_caves");
        BIOMES_BY_ID.put(177, (Object)"minecraft:meadow");
        BIOMES_BY_ID.put(178, (Object)"minecraft:grove");
        BIOMES_BY_ID.put(179, (Object)"minecraft:snowy_slopes");
        BIOMES_BY_ID.put(180, (Object)"minecraft:snowcapped_peaks");
        BIOMES_BY_ID.put(181, (Object)"minecraft:lofty_peaks");
        BIOMES_BY_ID.put(182, (Object)"minecraft:stony_peaks");
    }
}

