/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractUniversalBuilder
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.Unit;

public class NullOps
implements DynamicOps<Unit> {
    public static final NullOps INSTANCE = new NullOps();

    private NullOps() {
    }

    public <U> U convertTo(DynamicOps<U> dynamicOps, Unit unit) {
        return (U)dynamicOps.empty();
    }

    public Unit empty() {
        return Unit.INSTANCE;
    }

    public Unit emptyMap() {
        return Unit.INSTANCE;
    }

    public Unit emptyList() {
        return Unit.INSTANCE;
    }

    public Unit createNumeric(Number number) {
        return Unit.INSTANCE;
    }

    public Unit createByte(byte by) {
        return Unit.INSTANCE;
    }

    public Unit createShort(short s) {
        return Unit.INSTANCE;
    }

    public Unit createInt(int n) {
        return Unit.INSTANCE;
    }

    public Unit createLong(long l) {
        return Unit.INSTANCE;
    }

    public Unit createFloat(float f) {
        return Unit.INSTANCE;
    }

    public Unit createDouble(double d) {
        return Unit.INSTANCE;
    }

    public Unit createBoolean(boolean bl) {
        return Unit.INSTANCE;
    }

    public Unit createString(String string) {
        return Unit.INSTANCE;
    }

    public DataResult<Number> getNumberValue(Unit unit) {
        return DataResult.error(() -> "Not a number");
    }

    public DataResult<Boolean> getBooleanValue(Unit unit) {
        return DataResult.error(() -> "Not a boolean");
    }

    public DataResult<String> getStringValue(Unit unit) {
        return DataResult.error(() -> "Not a string");
    }

    public DataResult<Unit> mergeToList(Unit unit, Unit unit2) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToList(Unit unit, List<Unit> list) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit unit, Unit unit2, Unit unit3) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit unit, Map<Unit, Unit> map) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit unit, MapLike<Unit> mapLike) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Stream<Pair<Unit, Unit>>> getMapValues(Unit unit) {
        return DataResult.error(() -> "Not a map");
    }

    public DataResult<Consumer<BiConsumer<Unit, Unit>>> getMapEntries(Unit unit) {
        return DataResult.error(() -> "Not a map");
    }

    public DataResult<MapLike<Unit>> getMap(Unit unit) {
        return DataResult.error(() -> "Not a map");
    }

    public DataResult<Stream<Unit>> getStream(Unit unit) {
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<Consumer<Consumer<Unit>>> getList(Unit unit) {
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<ByteBuffer> getByteBuffer(Unit unit) {
        return DataResult.error(() -> "Not a byte list");
    }

    public DataResult<IntStream> getIntStream(Unit unit) {
        return DataResult.error(() -> "Not an int list");
    }

    public DataResult<LongStream> getLongStream(Unit unit) {
        return DataResult.error(() -> "Not a long list");
    }

    public Unit createMap(Stream<Pair<Unit, Unit>> stream) {
        return Unit.INSTANCE;
    }

    public Unit createMap(Map<Unit, Unit> map) {
        return Unit.INSTANCE;
    }

    public Unit createList(Stream<Unit> stream) {
        return Unit.INSTANCE;
    }

    public Unit createByteList(ByteBuffer byteBuffer) {
        return Unit.INSTANCE;
    }

    public Unit createIntList(IntStream intStream) {
        return Unit.INSTANCE;
    }

    public Unit createLongList(LongStream longStream) {
        return Unit.INSTANCE;
    }

    public Unit remove(Unit unit, String string) {
        return unit;
    }

    public RecordBuilder<Unit> mapBuilder() {
        return new NullMapBuilder(this);
    }

    public String toString() {
        return "Null";
    }

    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((Unit)((Object)object), string);
    }

    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((Unit)((Object)object));
    }

    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((Unit)((Object)object));
    }

    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((Unit)((Object)object));
    }

    public /* synthetic */ Object createList(Stream stream) {
        return this.createList((Stream<Unit>)stream);
    }

    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((Unit)((Object)object));
    }

    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((Unit)((Object)object));
    }

    public /* synthetic */ Object createMap(Map map) {
        return this.createMap((Map<Unit, Unit>)map);
    }

    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((Unit)((Object)object));
    }

    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap((Stream<Pair<Unit, Unit>>)stream);
    }

    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((Unit)((Object)object));
    }

    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((Unit)((Object)object));
    }

    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((Unit)((Object)object), (MapLike<Unit>)mapLike);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((Unit)((Object)object), (Map<Unit, Unit>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((Unit)((Object)object), (Unit)((Object)object2), (Unit)((Object)object3));
    }

    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((Unit)((Object)object), (List<Unit>)list);
    }

    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((Unit)((Object)object), (Unit)((Object)object2));
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((Unit)((Object)object));
    }

    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    public /* synthetic */ DataResult getBooleanValue(Object object) {
        return this.getBooleanValue((Unit)((Object)object));
    }

    public /* synthetic */ Object createDouble(double d) {
        return this.createDouble(d);
    }

    public /* synthetic */ Object createFloat(float f) {
        return this.createFloat(f);
    }

    public /* synthetic */ Object createLong(long l) {
        return this.createLong(l);
    }

    public /* synthetic */ Object createInt(int n) {
        return this.createInt(n);
    }

    public /* synthetic */ Object createShort(short s) {
        return this.createShort(s);
    }

    public /* synthetic */ Object createByte(byte by) {
        return this.createByte(by);
    }

    public /* synthetic */ Object createNumeric(Number number) {
        return this.createNumeric(number);
    }

    public /* synthetic */ DataResult getNumberValue(Object object) {
        return this.getNumberValue((Unit)((Object)object));
    }

    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (Unit)((Object)object));
    }

    public /* synthetic */ Object emptyList() {
        return this.emptyList();
    }

    public /* synthetic */ Object emptyMap() {
        return this.emptyMap();
    }

    public /* synthetic */ Object empty() {
        return this.empty();
    }

    static final class NullMapBuilder
    extends RecordBuilder.AbstractUniversalBuilder<Unit, Unit> {
        public NullMapBuilder(DynamicOps<Unit> dynamicOps) {
            super(dynamicOps);
        }

        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        protected Unit append(Unit unit, Unit unit2, Unit unit3) {
            return unit3;
        }

        protected DataResult<Unit> build(Unit unit, Unit unit2) {
            return DataResult.success((Object)((Object)unit2));
        }

        protected /* synthetic */ Object append(Object object, Object object2, Object object3) {
            return this.append((Unit)((Object)object), (Unit)((Object)object2), (Unit)((Object)object3));
        }

        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((Unit)((Object)object), (Unit)((Object)object2));
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

