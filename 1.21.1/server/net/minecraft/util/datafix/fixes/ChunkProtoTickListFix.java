/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.mutable.MutableInt;

public class ChunkProtoTickListFix
extends DataFix {
    private static final int SECTION_WIDTH = 16;
    private static final ImmutableSet<String> ALWAYS_WATERLOGGED = ImmutableSet.of((Object)"minecraft:bubble_column", (Object)"minecraft:kelp", (Object)"minecraft:kelp_plant", (Object)"minecraft:seagrass", (Object)"minecraft:tall_seagrass");

    public ChunkProtoTickListFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder opticFinder = type.findField("Level");
        OpticFinder opticFinder2 = opticFinder.type().findField("Sections");
        OpticFinder opticFinder3 = ((List.ListType)opticFinder2.type()).getElement().finder();
        OpticFinder opticFinder4 = opticFinder3.type().findField("block_states");
        OpticFinder opticFinder5 = opticFinder3.type().findField("biomes");
        OpticFinder opticFinder6 = opticFinder4.type().findField("palette");
        OpticFinder opticFinder7 = opticFinder.type().findField("TileTicks");
        return this.fixTypeEverywhereTyped("ChunkProtoTickListFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> {
            typed = typed.update(DSL.remainderFinder(), dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.get("LiquidTicks").result().map(dynamic2 -> dynamic.set("fluid_ticks", dynamic2).remove("LiquidTicks")), (Object)dynamic));
            Dynamic dynamic3 = (Dynamic)typed.get(DSL.remainderFinder());
            MutableInt mutableInt = new MutableInt();
            Int2ObjectArrayMap int2ObjectArrayMap = new Int2ObjectArrayMap();
            typed.getOptionalTyped(opticFinder2).ifPresent(arg_0 -> ChunkProtoTickListFix.lambda$makeRule$7(opticFinder3, opticFinder5, mutableInt, opticFinder4, (Int2ObjectMap)int2ObjectArrayMap, opticFinder6, arg_0));
            byte by = mutableInt.getValue().byteValue();
            typed = typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("yPos", dynamic -> dynamic.createByte(by)));
            if (typed.getOptionalTyped(opticFinder7).isPresent() || dynamic3.get("fluid_ticks").result().isPresent()) {
                return typed;
            }
            int n = dynamic3.get("xPos").asInt(0);
            int n2 = dynamic3.get("zPos").asInt(0);
            Dynamic<?> dynamic4 = this.makeTickList(dynamic3, (Int2ObjectMap<Supplier<PoorMansPalettedContainer>>)int2ObjectArrayMap, by, n, n2, "LiquidsToBeTicked", ChunkProtoTickListFix::getLiquid);
            Dynamic<?> dynamic5 = this.makeTickList(dynamic3, (Int2ObjectMap<Supplier<PoorMansPalettedContainer>>)int2ObjectArrayMap, by, n, n2, "ToBeTicked", ChunkProtoTickListFix::getBlock);
            Optional optional = opticFinder7.type().readTyped(dynamic5).result();
            if (optional.isPresent()) {
                typed = typed.set(opticFinder7, (Typed)((Pair)optional.get()).getFirst());
            }
            return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", dynamic4));
        }));
    }

    private Dynamic<?> makeTickList(Dynamic<?> dynamic2, Int2ObjectMap<Supplier<PoorMansPalettedContainer>> int2ObjectMap, byte by, int n2, int n3, String string, Function<Dynamic<?>, String> function) {
        Stream<Object> stream = Stream.empty();
        List list = dynamic2.get(string).asList(Function.identity());
        for (int i = 0; i < list.size(); ++i) {
            int n4 = i + by;
            Supplier supplier = (Supplier)int2ObjectMap.get(n4);
            Stream<Dynamic> stream2 = ((Dynamic)list.get(i)).asStream().mapToInt(dynamic -> dynamic.asShort((short)-1)).filter(n -> n > 0).mapToObj(arg_0 -> this.lambda$makeTickList$15(dynamic2, (Supplier)supplier, n2, n4, n3, function, arg_0));
            stream = Stream.concat(stream, stream2);
        }
        return dynamic2.createList(stream);
    }

    private static String getBlock(@Nullable Dynamic<?> dynamic) {
        return dynamic != null ? dynamic.get("Name").asString("minecraft:air") : "minecraft:air";
    }

    private static String getLiquid(@Nullable Dynamic<?> dynamic) {
        if (dynamic == null) {
            return "minecraft:empty";
        }
        String string = dynamic.get("Name").asString("");
        if ("minecraft:water".equals(string)) {
            return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
        }
        if ("minecraft:lava".equals(string)) {
            return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
        }
        if (ALWAYS_WATERLOGGED.contains((Object)string) || dynamic.get("Properties").get("waterlogged").asBoolean(false)) {
            return "minecraft:water";
        }
        return "minecraft:empty";
    }

    private Dynamic<?> createTick(Dynamic<?> dynamic, @Nullable Supplier<PoorMansPalettedContainer> supplier, int n, int n2, int n3, int n4, Function<Dynamic<?>, String> function) {
        int n5 = n4 & 0xF;
        int n6 = n4 >>> 4 & 0xF;
        int n7 = n4 >>> 8 & 0xF;
        String string = function.apply(supplier != null ? supplier.get().get(n5, n6, n7) : null);
        return dynamic.createMap((Map)ImmutableMap.builder().put((Object)dynamic.createString("i"), (Object)dynamic.createString(string)).put((Object)dynamic.createString("x"), (Object)dynamic.createInt(n * 16 + n5)).put((Object)dynamic.createString("y"), (Object)dynamic.createInt(n2 * 16 + n6)).put((Object)dynamic.createString("z"), (Object)dynamic.createInt(n3 * 16 + n7)).put((Object)dynamic.createString("t"), (Object)dynamic.createInt(0)).put((Object)dynamic.createString("p"), (Object)dynamic.createInt(0)).build());
    }

    private /* synthetic */ Dynamic lambda$makeTickList$15(Dynamic dynamic, Supplier supplier, int n, int n2, int n3, Function function, int n4) {
        return this.createTick(dynamic, supplier, n, n2, n3, n4, function);
    }

    private static /* synthetic */ void lambda$makeRule$7(OpticFinder opticFinder, OpticFinder opticFinder2, MutableInt mutableInt, OpticFinder opticFinder3, Int2ObjectMap int2ObjectMap, OpticFinder opticFinder4, Typed typed) {
        typed.getAllTyped(opticFinder).forEach(typed2 -> {
            Dynamic dynamic = (Dynamic)typed2.get(DSL.remainderFinder());
            int n = dynamic.get("Y").asInt(Integer.MAX_VALUE);
            if (n == Integer.MAX_VALUE) {
                return;
            }
            if (typed2.getOptionalTyped(opticFinder2).isPresent()) {
                mutableInt.setValue(Math.min(n, mutableInt.getValue()));
            }
            typed2.getOptionalTyped(opticFinder3).ifPresent(typed -> int2ObjectMap.put(n, (Object)Suppliers.memoize(() -> {
                List list = typed.getOptionalTyped(opticFinder4).map(typed -> typed.write().result().map(dynamic -> dynamic.asList(Function.identity())).orElse(Collections.emptyList())).orElse(Collections.emptyList());
                long[] lArray = ((Dynamic)typed.get(DSL.remainderFinder())).get("data").asLongStream().toArray();
                return new PoorMansPalettedContainer(list, lArray);
            })));
        });
    }

    public static final class PoorMansPalettedContainer {
        private static final long SIZE_BITS = 4L;
        private final List<? extends Dynamic<?>> palette;
        private final long[] data;
        private final int bits;
        private final long mask;
        private final int valuesPerLong;

        public PoorMansPalettedContainer(List<? extends Dynamic<?>> list, long[] lArray) {
            this.palette = list;
            this.data = lArray;
            this.bits = Math.max(4, ChunkHeightAndBiomeFix.ceillog2(list.size()));
            this.mask = (1L << this.bits) - 1L;
            this.valuesPerLong = (char)(64 / this.bits);
        }

        @Nullable
        public Dynamic<?> get(int n, int n2, int n3) {
            int n4 = this.palette.size();
            if (n4 < 1) {
                return null;
            }
            if (n4 == 1) {
                return this.palette.get(0);
            }
            int n5 = this.getIndex(n, n2, n3);
            int n6 = n5 / this.valuesPerLong;
            if (n6 < 0 || n6 >= this.data.length) {
                return null;
            }
            long l = this.data[n6];
            int n7 = (n5 - n6 * this.valuesPerLong) * this.bits;
            int n8 = (int)(l >> n7 & this.mask);
            if (n8 < 0 || n8 >= n4) {
                return null;
            }
            return this.palette.get(n8);
        }

        private int getIndex(int n, int n2, int n3) {
            return (n2 << 4 | n3) << 4 | n;
        }

        public List<? extends Dynamic<?>> palette() {
            return this.palette;
        }

        public long[] data() {
            return this.data;
        }
    }
}

