/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.util.datafix.fixes.References;

public class ChunkBedBlockEntityInjecterFix
extends DataFix {
    public ChunkBedBlockEntityInjecterFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getOutputSchema().getType(References.CHUNK);
        Type type2 = type.findFieldType("Level");
        Type type3 = type2.findFieldType("TileEntities");
        if (!(type3 instanceof List.ListType)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        }
        List.ListType listType = (List.ListType)type3;
        return this.cap(type2, listType);
    }

    private <TE> TypeRewriteRule cap(Type<?> type, List.ListType<TE> listType) {
        Type type2 = listType.getElement();
        OpticFinder opticFinder = DSL.fieldFinder((String)"Level", type);
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"TileEntities", listType);
        int n = 416;
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhere("InjectBedBlockEntityType", (Type)this.getInputSchema().findChoiceType(References.BLOCK_ENTITY), (Type)this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY), dynamicOps -> pair -> pair), (TypeRewriteRule)this.fixTypeEverywhereTyped("BedBlockEntityInjecter", this.getOutputSchema().getType(References.CHUNK), typed -> {
            Typed typed2 = typed.getTyped(opticFinder);
            Dynamic dynamic = (Dynamic)typed2.get(DSL.remainderFinder());
            int n = dynamic.get("xPos").asInt(0);
            int n2 = dynamic.get("zPos").asInt(0);
            ArrayList arrayList = Lists.newArrayList((Iterable)((Iterable)typed2.getOrCreate(opticFinder2)));
            List list = dynamic.get("Sections").asList(Function.identity());
            for (Dynamic dynamic2 : list) {
                int n3 = dynamic2.get("Y").asInt(0);
                Streams.mapWithIndex((IntStream)dynamic2.get("Blocks").asIntStream(), (n4, l) -> {
                    if (416 == (n4 & 0xFF) << 4) {
                        int n5 = (int)l;
                        int n6 = n5 & 0xF;
                        int n7 = n5 >> 8 & 0xF;
                        int n8 = n5 >> 4 & 0xF;
                        HashMap hashMap = Maps.newHashMap();
                        hashMap.put(dynamic2.createString("id"), dynamic2.createString("minecraft:bed"));
                        hashMap.put(dynamic2.createString("x"), dynamic2.createInt(n6 + (n << 4)));
                        hashMap.put(dynamic2.createString("y"), dynamic2.createInt(n7 + (n3 << 4)));
                        hashMap.put(dynamic2.createString("z"), dynamic2.createInt(n8 + (n2 << 4)));
                        hashMap.put(dynamic2.createString("color"), dynamic2.createShort((short)14));
                        return hashMap;
                    }
                    return null;
                }).forEachOrdered(map -> {
                    if (map != null) {
                        arrayList.add(((Pair)type2.read(dynamic2.createMap(map)).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity."))).getFirst());
                    }
                });
            }
            if (!arrayList.isEmpty()) {
                return typed.set(opticFinder, typed2.set(opticFinder2, (Object)arrayList));
            }
            return typed;
        }));
    }
}

