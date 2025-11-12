/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractStringBuilder
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NbtOps
implements DynamicOps<Tag> {
    public static final NbtOps INSTANCE = new NbtOps();
    private static final String WRAPPER_MARKER = "";

    protected NbtOps() {
    }

    public Tag empty() {
        return EndTag.INSTANCE;
    }

    public <U> U convertTo(DynamicOps<U> dynamicOps, Tag tag) {
        return (U)(switch (tag.getId()) {
            case 0 -> dynamicOps.empty();
            case 1 -> dynamicOps.createByte(((NumericTag)tag).getAsByte());
            case 2 -> dynamicOps.createShort(((NumericTag)tag).getAsShort());
            case 3 -> dynamicOps.createInt(((NumericTag)tag).getAsInt());
            case 4 -> dynamicOps.createLong(((NumericTag)tag).getAsLong());
            case 5 -> dynamicOps.createFloat(((NumericTag)tag).getAsFloat());
            case 6 -> dynamicOps.createDouble(((NumericTag)tag).getAsDouble());
            case 7 -> dynamicOps.createByteList(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray()));
            case 8 -> dynamicOps.createString(tag.getAsString());
            case 9 -> this.convertList(dynamicOps, tag);
            case 10 -> this.convertMap(dynamicOps, tag);
            case 11 -> dynamicOps.createIntList(Arrays.stream(((IntArrayTag)tag).getAsIntArray()));
            case 12 -> dynamicOps.createLongList(Arrays.stream(((LongArrayTag)tag).getAsLongArray()));
            default -> throw new IllegalStateException("Unknown tag type: " + String.valueOf(tag));
        });
    }

    public DataResult<Number> getNumberValue(Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return DataResult.success((Object)numericTag.getAsNumber());
        }
        return DataResult.error(() -> "Not a number");
    }

    public Tag createNumeric(Number number) {
        return DoubleTag.valueOf(number.doubleValue());
    }

    public Tag createByte(byte by) {
        return ByteTag.valueOf(by);
    }

    public Tag createShort(short s) {
        return ShortTag.valueOf(s);
    }

    public Tag createInt(int n) {
        return IntTag.valueOf(n);
    }

    public Tag createLong(long l) {
        return LongTag.valueOf(l);
    }

    public Tag createFloat(float f) {
        return FloatTag.valueOf(f);
    }

    public Tag createDouble(double d) {
        return DoubleTag.valueOf(d);
    }

    public Tag createBoolean(boolean bl) {
        return ByteTag.valueOf(bl);
    }

    public DataResult<String> getStringValue(Tag tag) {
        if (tag instanceof StringTag) {
            StringTag stringTag = (StringTag)tag;
            return DataResult.success((Object)stringTag.getAsString());
        }
        return DataResult.error(() -> "Not a string");
    }

    public Tag createString(String string) {
        return StringTag.valueOf(string);
    }

    public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
        return NbtOps.createCollector(tag).map(listCollector -> DataResult.success((Object)listCollector.accept(tag2).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(tag), (Object)tag));
    }

    public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
        return NbtOps.createCollector(tag).map(listCollector -> DataResult.success((Object)listCollector.acceptAll(list).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(tag), (Object)tag));
    }

    public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
        CompoundTag compoundTag;
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }
        if (!(tag2 instanceof StringTag)) {
            return DataResult.error(() -> "key is not a string: " + String.valueOf(tag2), (Object)tag);
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag2 = (CompoundTag)tag;
            compoundTag = compoundTag2.shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag compoundTag3 = compoundTag;
        compoundTag3.put(tag2.getAsString(), tag3);
        return DataResult.success((Object)compoundTag3);
    }

    public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> mapLike) {
        CompoundTag compoundTag;
        Object object;
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }
        if (tag instanceof CompoundTag) {
            object = (CompoundTag)tag;
            compoundTag = ((CompoundTag)object).shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag compoundTag2 = compoundTag;
        object = new ArrayList();
        mapLike.entries().forEach(arg_0 -> NbtOps.lambda$mergeToMap$11((List)object, compoundTag2, arg_0));
        if (!object.isEmpty()) {
            return DataResult.error(() -> NbtOps.lambda$mergeToMap$12((List)object), (Object)compoundTag2);
        }
        return DataResult.success((Object)compoundTag2);
    }

    public DataResult<Tag> mergeToMap(Tag tag, Map<Tag, Tag> map) {
        CompoundTag compoundTag;
        Object object;
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }
        if (tag instanceof CompoundTag) {
            object = (CompoundTag)tag;
            compoundTag = ((CompoundTag)object).shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag compoundTag2 = compoundTag;
        object = new ArrayList();
        for (Map.Entry<Tag, Tag> entry : map.entrySet()) {
            Tag tag2 = entry.getKey();
            if (tag2 instanceof StringTag) {
                compoundTag2.put(tag2.getAsString(), entry.getValue());
                continue;
            }
            object.add(tag2);
        }
        if (!object.isEmpty()) {
            return DataResult.error(() -> NbtOps.lambda$mergeToMap$14((List)object), (Object)compoundTag2);
        }
        return DataResult.success((Object)compoundTag2);
    }

    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success(compoundTag.entrySet().stream().map(entry -> Pair.of((Object)this.createString((String)entry.getKey()), (Object)((Tag)entry.getValue()))));
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(tag));
    }

    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success(biConsumer -> {
                for (Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                    biConsumer.accept(this.createString(entry.getKey()), entry.getValue());
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(tag));
    }

    public DataResult<MapLike<Tag>> getMap(Tag tag) {
        if (tag instanceof CompoundTag) {
            final CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success((Object)new MapLike<Tag>(){

                @Nullable
                public Tag get(Tag tag) {
                    return compoundTag.get(tag.getAsString());
                }

                @Nullable
                public Tag get(String string) {
                    return compoundTag.get(string);
                }

                public Stream<Pair<Tag, Tag>> entries() {
                    return compoundTag.entrySet().stream().map(entry -> Pair.of((Object)NbtOps.this.createString((String)entry.getKey()), (Object)((Tag)entry.getValue())));
                }

                public String toString() {
                    return "MapLike[" + String.valueOf(compoundTag) + "]";
                }

                @Nullable
                public /* synthetic */ Object get(String string) {
                    return this.get(string);
                }

                @Nullable
                public /* synthetic */ Object get(Object object) {
                    return this.get((Tag)object);
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(tag));
    }

    public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
        CompoundTag compoundTag = new CompoundTag();
        stream.forEach(pair -> compoundTag.put(((Tag)pair.getFirst()).getAsString(), (Tag)pair.getSecond()));
        return compoundTag;
    }

    private static Tag tryUnwrap(CompoundTag compoundTag) {
        Tag tag;
        if (compoundTag.size() == 1 && (tag = compoundTag.get(WRAPPER_MARKER)) != null) {
            return tag;
        }
        return compoundTag;
    }

    public DataResult<Stream<Tag>> getStream(Tag tag2) {
        if (tag2 instanceof ListTag) {
            ListTag listTag = (ListTag)tag2;
            if (listTag.getElementType() == 10) {
                return DataResult.success(listTag.stream().map(tag -> NbtOps.tryUnwrap((CompoundTag)tag)));
            }
            return DataResult.success(listTag.stream());
        }
        if (tag2 instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag2;
            return DataResult.success(collectionTag.stream().map(tag -> tag));
        }
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            if (listTag.getElementType() == 10) {
                return DataResult.success(consumer -> {
                    for (Tag tag : listTag) {
                        consumer.accept(NbtOps.tryUnwrap((CompoundTag)tag));
                    }
                });
            }
            return DataResult.success(listTag::forEach);
        }
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            return DataResult.success(collectionTag::forEach);
        }
        return DataResult.error(() -> "Not a list: " + String.valueOf(tag));
    }

    public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
        if (tag instanceof ByteArrayTag) {
            ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
            return DataResult.success((Object)ByteBuffer.wrap(byteArrayTag.getAsByteArray()));
        }
        return super.getByteBuffer((Object)tag);
    }

    public Tag createByteList(ByteBuffer byteBuffer) {
        ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
        byte[] byArray = new byte[byteBuffer.capacity()];
        byteBuffer2.get(0, byArray, 0, byArray.length);
        return new ByteArrayTag(byArray);
    }

    public DataResult<IntStream> getIntStream(Tag tag) {
        if (tag instanceof IntArrayTag) {
            IntArrayTag intArrayTag = (IntArrayTag)tag;
            return DataResult.success((Object)Arrays.stream(intArrayTag.getAsIntArray()));
        }
        return super.getIntStream((Object)tag);
    }

    public Tag createIntList(IntStream intStream) {
        return new IntArrayTag(intStream.toArray());
    }

    public DataResult<LongStream> getLongStream(Tag tag) {
        if (tag instanceof LongArrayTag) {
            LongArrayTag longArrayTag = (LongArrayTag)tag;
            return DataResult.success((Object)Arrays.stream(longArrayTag.getAsLongArray()));
        }
        return super.getLongStream((Object)tag);
    }

    public Tag createLongList(LongStream longStream) {
        return new LongArrayTag(longStream.toArray());
    }

    public Tag createList(Stream<Tag> stream) {
        return InitialListCollector.INSTANCE.acceptAll(stream).result();
    }

    public Tag remove(Tag tag, String string) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = compoundTag.shallowCopy();
            compoundTag2.remove(string);
            return compoundTag2;
        }
        return tag;
    }

    public String toString() {
        return "NBT";
    }

    public RecordBuilder<Tag> mapBuilder() {
        return new NbtRecordBuilder(this);
    }

    private static Optional<ListCollector> createCollector(Tag tag) {
        if (tag instanceof EndTag) {
            return Optional.of(InitialListCollector.INSTANCE);
        }
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            if (collectionTag.isEmpty()) {
                return Optional.of(InitialListCollector.INSTANCE);
            }
            if (collectionTag instanceof ListTag) {
                ListTag listTag = (ListTag)collectionTag;
                return switch (listTag.getElementType()) {
                    case 0 -> Optional.of(InitialListCollector.INSTANCE);
                    case 10 -> Optional.of(new HeterogenousListCollector(listTag));
                    default -> Optional.of(new HomogenousListCollector(listTag));
                };
            }
            if (collectionTag instanceof ByteArrayTag) {
                ByteArrayTag byteArrayTag = (ByteArrayTag)collectionTag;
                return Optional.of(new ByteListCollector(byteArrayTag.getAsByteArray()));
            }
            if (collectionTag instanceof IntArrayTag) {
                IntArrayTag intArrayTag = (IntArrayTag)collectionTag;
                return Optional.of(new IntListCollector(intArrayTag.getAsIntArray()));
            }
            if (collectionTag instanceof LongArrayTag) {
                LongArrayTag longArrayTag = (LongArrayTag)collectionTag;
                return Optional.of(new LongListCollector(longArrayTag.getAsLongArray()));
            }
        }
        return Optional.empty();
    }

    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((Tag)object, string);
    }

    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((Tag)object);
    }

    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((Tag)object);
    }

    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((Tag)object);
    }

    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((Tag)object);
    }

    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((Tag)object);
    }

    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((Tag)object);
    }

    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap(stream);
    }

    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((Tag)object);
    }

    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((Tag)object);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((Tag)object, (MapLike<Tag>)mapLike);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((Tag)object, (Map<Tag, Tag>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((Tag)object, (Tag)object2, (Tag)object3);
    }

    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((Tag)object, (List<Tag>)list);
    }

    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((Tag)object, (Tag)object2);
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((Tag)object);
    }

    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
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
        return this.getNumberValue((Tag)object);
    }

    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (Tag)object);
    }

    public /* synthetic */ Object empty() {
        return this.empty();
    }

    private static /* synthetic */ String lambda$mergeToMap$14(List list) {
        return "some keys are not strings: " + String.valueOf(list);
    }

    private static /* synthetic */ String lambda$mergeToMap$12(List list) {
        return "some keys are not strings: " + String.valueOf(list);
    }

    private static /* synthetic */ void lambda$mergeToMap$11(List list, CompoundTag compoundTag, Pair pair) {
        Tag tag = (Tag)pair.getFirst();
        if (!(tag instanceof StringTag)) {
            list.add(tag);
            return;
        }
        compoundTag.put(tag.getAsString(), (Tag)pair.getSecond());
    }

    static class InitialListCollector
    implements ListCollector {
        public static final InitialListCollector INSTANCE = new InitialListCollector();

        private InitialListCollector() {
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                return new HeterogenousListCollector().accept(compoundTag);
            }
            if (tag instanceof ByteTag) {
                ByteTag byteTag = (ByteTag)tag;
                return new ByteListCollector(byteTag.getAsByte());
            }
            if (tag instanceof IntTag) {
                IntTag intTag = (IntTag)tag;
                return new IntListCollector(intTag.getAsInt());
            }
            if (tag instanceof LongTag) {
                LongTag longTag = (LongTag)tag;
                return new LongListCollector(longTag.getAsLong());
            }
            return new HomogenousListCollector(tag);
        }

        @Override
        public Tag result() {
            return new ListTag();
        }
    }

    static interface ListCollector {
        public ListCollector accept(Tag var1);

        default public ListCollector acceptAll(Iterable<Tag> iterable) {
            ListCollector listCollector = this;
            for (Tag tag : iterable) {
                listCollector = listCollector.accept(tag);
            }
            return listCollector;
        }

        default public ListCollector acceptAll(Stream<Tag> stream) {
            return this.acceptAll(stream::iterator);
        }

        public Tag result();
    }

    class NbtRecordBuilder
    extends RecordBuilder.AbstractStringBuilder<Tag, CompoundTag> {
        protected NbtRecordBuilder(NbtOps nbtOps) {
            super((DynamicOps)nbtOps);
        }

        protected CompoundTag initBuilder() {
            return new CompoundTag();
        }

        protected CompoundTag append(String string, Tag tag, CompoundTag compoundTag) {
            compoundTag.put(string, tag);
            return compoundTag;
        }

        protected DataResult<Tag> build(CompoundTag compoundTag, Tag tag) {
            if (tag == null || tag == EndTag.INSTANCE) {
                return DataResult.success((Object)compoundTag);
            }
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag2 = (CompoundTag)tag;
                CompoundTag compoundTag3 = compoundTag2.shallowCopy();
                for (Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                    compoundTag3.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success((Object)compoundTag3);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }

        protected /* synthetic */ Object append(String string, Object object, Object object2) {
            return this.append(string, (Tag)object, (CompoundTag)object2);
        }

        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((CompoundTag)object, (Tag)object2);
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static class HeterogenousListCollector
    implements ListCollector {
        private final ListTag result = new ListTag();

        public HeterogenousListCollector() {
        }

        public HeterogenousListCollector(Collection<Tag> collection) {
            this.result.addAll(collection);
        }

        public HeterogenousListCollector(IntArrayList intArrayList) {
            intArrayList.forEach(n -> this.result.add(HeterogenousListCollector.wrapElement(IntTag.valueOf(n))));
        }

        public HeterogenousListCollector(ByteArrayList byteArrayList) {
            byteArrayList.forEach(by -> this.result.add(HeterogenousListCollector.wrapElement(ByteTag.valueOf(by))));
        }

        public HeterogenousListCollector(LongArrayList longArrayList) {
            longArrayList.forEach(l -> this.result.add(HeterogenousListCollector.wrapElement(LongTag.valueOf(l))));
        }

        private static boolean isWrapper(CompoundTag compoundTag) {
            return compoundTag.size() == 1 && compoundTag.contains(NbtOps.WRAPPER_MARKER);
        }

        private static Tag wrapIfNeeded(Tag tag) {
            CompoundTag compoundTag;
            if (tag instanceof CompoundTag && !HeterogenousListCollector.isWrapper(compoundTag = (CompoundTag)tag)) {
                return compoundTag;
            }
            return HeterogenousListCollector.wrapElement(tag);
        }

        private static CompoundTag wrapElement(Tag tag) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put(NbtOps.WRAPPER_MARKER, tag);
            return compoundTag;
        }

        @Override
        public ListCollector accept(Tag tag) {
            this.result.add(HeterogenousListCollector.wrapIfNeeded(tag));
            return this;
        }

        @Override
        public Tag result() {
            return this.result;
        }
    }

    static class HomogenousListCollector
    implements ListCollector {
        private final ListTag result = new ListTag();

        HomogenousListCollector(Tag tag) {
            this.result.add(tag);
        }

        HomogenousListCollector(ListTag listTag) {
            this.result.addAll(listTag);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag.getId() != this.result.getElementType()) {
                return new HeterogenousListCollector().acceptAll(this.result).accept(tag);
            }
            this.result.add(tag);
            return this;
        }

        @Override
        public Tag result() {
            return this.result;
        }
    }

    static class ByteListCollector
    implements ListCollector {
        private final ByteArrayList values = new ByteArrayList();

        public ByteListCollector(byte by) {
            this.values.add(by);
        }

        public ByteListCollector(byte[] byArray) {
            this.values.addElements(0, byArray);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof ByteTag) {
                ByteTag byteTag = (ByteTag)tag;
                this.values.add(byteTag.getAsByte());
                return this;
            }
            return new HeterogenousListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new ByteArrayTag(this.values.toByteArray());
        }
    }

    static class IntListCollector
    implements ListCollector {
        private final IntArrayList values = new IntArrayList();

        public IntListCollector(int n) {
            this.values.add(n);
        }

        public IntListCollector(int[] nArray) {
            this.values.addElements(0, nArray);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof IntTag) {
                IntTag intTag = (IntTag)tag;
                this.values.add(intTag.getAsInt());
                return this;
            }
            return new HeterogenousListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new IntArrayTag(this.values.toIntArray());
        }
    }

    static class LongListCollector
    implements ListCollector {
        private final LongArrayList values = new LongArrayList();

        public LongListCollector(long l) {
            this.values.add(l);
        }

        public LongListCollector(long[] lArray) {
            this.values.addElements(0, lArray);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof LongTag) {
                LongTag longTag = (LongTag)tag;
                this.values.add(longTag.getAsLong());
                return this;
            }
            return new HeterogenousListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new LongArrayTag(this.values.toLongArray());
        }
    }
}

