/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.shorts.ShortArrayList
 *  it.unimi.dsi.fastutil.shorts.ShortList
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.datafix.fixes.References;

public class ChunkToProtochunkFix
extends DataFix {
    private static final int NUM_SECTIONS = 16;

    public ChunkToProtochunkFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        return this.writeFixAndRead("ChunkToProtoChunkFix", this.getInputSchema().getType(References.CHUNK), this.getOutputSchema().getType(References.CHUNK), dynamic -> dynamic.update("Level", ChunkToProtochunkFix::fixChunkData));
    }

    private static <T> Dynamic<T> fixChunkData(Dynamic<T> dynamic) {
        boolean bl;
        boolean bl2 = dynamic.get("TerrainPopulated").asBoolean(false);
        boolean bl3 = bl = dynamic.get("LightPopulated").asNumber().result().isEmpty() || dynamic.get("LightPopulated").asBoolean(false);
        String string = bl2 ? (bl ? "mobs_spawned" : "decorated") : "carved";
        return ChunkToProtochunkFix.repackTicks(ChunkToProtochunkFix.repackBiomes(dynamic)).set("Status", dynamic.createString(string)).set("hasLegacyStructureData", dynamic.createBoolean(true));
    }

    private static <T> Dynamic<T> repackBiomes(Dynamic<T> dynamic) {
        return dynamic.update("Biomes", dynamic2 -> (Dynamic)DataFixUtils.orElse(dynamic2.asByteBufferOpt().result().map(byteBuffer -> {
            int[] nArray = new int[256];
            for (int i = 0; i < nArray.length; ++i) {
                if (i >= byteBuffer.capacity()) continue;
                nArray[i] = byteBuffer.get(i) & 0xFF;
            }
            return dynamic.createIntList(Arrays.stream(nArray));
        }), (Object)dynamic2));
    }

    private static <T> Dynamic<T> repackTicks(Dynamic<T> dynamic) {
        return (Dynamic)DataFixUtils.orElse(dynamic.get("TileTicks").asStreamOpt().result().map(stream -> {
            List list = IntStream.range(0, 16).mapToObj(n -> new ShortArrayList()).collect(Collectors.toList());
            stream.forEach(dynamic -> {
                int n = dynamic.get("x").asInt(0);
                int n2 = dynamic.get("y").asInt(0);
                int n3 = dynamic.get("z").asInt(0);
                short s = ChunkToProtochunkFix.packOffsetCoordinates(n, n2, n3);
                ((ShortList)list.get(n2 >> 4)).add(s);
            });
            return dynamic.remove("TileTicks").set("ToBeTicked", dynamic.createList(list.stream().map(shortList -> dynamic.createList(shortList.intStream().mapToObj(n -> dynamic.createShort((short)n))))));
        }), dynamic);
    }

    private static short packOffsetCoordinates(int n, int n2, int n3) {
        return (short)(n & 0xF | (n2 & 0xF) << 4 | (n3 & 0xF) << 8);
    }
}

