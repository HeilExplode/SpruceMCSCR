/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufAllocator
 *  io.netty.buffer.ByteBufInputStream
 *  io.netty.buffer.ByteBufOutputStream
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  io.netty.util.ByteProcessor
 *  io.netty.util.ReferenceCounted
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import io.netty.util.ReferenceCounted;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FriendlyByteBuf
extends ByteBuf {
    public static final int DEFAULT_NBT_QUOTA = 0x200000;
    private final ByteBuf source;
    public static final short MAX_STRING_LENGTH = Short.MAX_VALUE;
    public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
    private static final int PUBLIC_KEY_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_LENGTH = 512;
    private static final Gson GSON = new Gson();

    public FriendlyByteBuf(ByteBuf byteBuf) {
        this.source = byteBuf;
    }

    @Deprecated
    public <T> T readWithCodecTrusted(DynamicOps<Tag> dynamicOps, Codec<T> codec) {
        return this.readWithCodec(dynamicOps, codec, NbtAccounter.unlimitedHeap());
    }

    @Deprecated
    public <T> T readWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec, NbtAccounter nbtAccounter) {
        Tag tag = this.readNbt(nbtAccounter);
        return (T)codec.parse(dynamicOps, (Object)tag).getOrThrow(string -> new DecoderException("Failed to decode: " + string + " " + String.valueOf(tag)));
    }

    @Deprecated
    public <T> FriendlyByteBuf writeWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec, T t) {
        Tag tag = (Tag)codec.encodeStart(dynamicOps, t).getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(t)));
        this.writeNbt(tag);
        return this;
    }

    public <T> T readJsonWithCodec(Codec<T> codec) {
        JsonElement jsonElement = GsonHelper.fromJson(GSON, this.readUtf(), JsonElement.class);
        DataResult dataResult = codec.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement);
        return (T)dataResult.getOrThrow(string -> new DecoderException("Failed to decode json: " + string));
    }

    public <T> void writeJsonWithCodec(Codec<T> codec, T t) {
        DataResult dataResult = codec.encodeStart((DynamicOps)JsonOps.INSTANCE, t);
        this.writeUtf(GSON.toJson((JsonElement)dataResult.getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(t)))));
    }

    public static <T> IntFunction<T> limitValue(IntFunction<T> intFunction, int n) {
        return n2 -> {
            if (n2 > n) {
                throw new DecoderException("Value " + n2 + " is larger than limit " + n);
            }
            return intFunction.apply(n2);
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> intFunction, StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        int n = this.readVarInt();
        Collection collection = (Collection)intFunction.apply(n);
        for (int i = 0; i < n; ++i) {
            collection.add(streamDecoder.decode(this));
        }
        return (C)collection;
    }

    public <T> void writeCollection(Collection<T> collection, StreamEncoder<? super FriendlyByteBuf, T> streamEncoder) {
        this.writeVarInt(collection.size());
        for (T t : collection) {
            streamEncoder.encode(this, t);
        }
    }

    public <T> List<T> readList(StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        return this.readCollection(Lists::newArrayListWithCapacity, streamDecoder);
    }

    public IntList readIntIdList() {
        int n = this.readVarInt();
        IntArrayList intArrayList = new IntArrayList();
        for (int i = 0; i < n; ++i) {
            intArrayList.add(this.readVarInt());
        }
        return intArrayList;
    }

    public void writeIntIdList(IntList intList) {
        this.writeVarInt(intList.size());
        intList.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> intFunction, StreamDecoder<? super FriendlyByteBuf, K> streamDecoder, StreamDecoder<? super FriendlyByteBuf, V> streamDecoder2) {
        int n = this.readVarInt();
        Map map = (Map)intFunction.apply(n);
        for (int i = 0; i < n; ++i) {
            K k = streamDecoder.decode(this);
            V v = streamDecoder2.decode(this);
            map.put(k, v);
        }
        return (M)map;
    }

    public <K, V> Map<K, V> readMap(StreamDecoder<? super FriendlyByteBuf, K> streamDecoder, StreamDecoder<? super FriendlyByteBuf, V> streamDecoder2) {
        return this.readMap(Maps::newHashMapWithExpectedSize, streamDecoder, streamDecoder2);
    }

    public <K, V> void writeMap(Map<K, V> map, StreamEncoder<? super FriendlyByteBuf, K> streamEncoder, StreamEncoder<? super FriendlyByteBuf, V> streamEncoder2) {
        this.writeVarInt(map.size());
        map.forEach((object, object2) -> {
            streamEncoder.encode(this, object);
            streamEncoder2.encode(this, object2);
        });
    }

    public void readWithCount(Consumer<FriendlyByteBuf> consumer) {
        int n = this.readVarInt();
        for (int i = 0; i < n; ++i) {
            consumer.accept(this);
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> clazz) {
        Enum[] enumArray = (Enum[])clazz.getEnumConstants();
        BitSet bitSet = new BitSet(enumArray.length);
        for (int i = 0; i < enumArray.length; ++i) {
            bitSet.set(i, enumSet.contains(enumArray[i]));
        }
        this.writeFixedBitSet(bitSet, enumArray.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> clazz) {
        Enum[] enumArray = (Enum[])clazz.getEnumConstants();
        BitSet bitSet = this.readFixedBitSet(enumArray.length);
        EnumSet<Enum> enumSet = EnumSet.noneOf(clazz);
        for (int i = 0; i < enumArray.length; ++i) {
            if (!bitSet.get(i)) continue;
            enumSet.add(enumArray[i]);
        }
        return enumSet;
    }

    public <T> void writeOptional(Optional<T> optional, StreamEncoder<? super FriendlyByteBuf, T> streamEncoder) {
        if (optional.isPresent()) {
            this.writeBoolean(true);
            streamEncoder.encode(this, optional.get());
        } else {
            this.writeBoolean(false);
        }
    }

    public <T> Optional<T> readOptional(StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        if (this.readBoolean()) {
            return Optional.of(streamDecoder.decode(this));
        }
        return Optional.empty();
    }

    @Nullable
    public <T> T readNullable(StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        return FriendlyByteBuf.readNullable(this, streamDecoder);
    }

    @Nullable
    public static <T, B extends ByteBuf> T readNullable(B b, StreamDecoder<? super B, T> streamDecoder) {
        if (b.readBoolean()) {
            return streamDecoder.decode(b);
        }
        return null;
    }

    public <T> void writeNullable(@Nullable T t, StreamEncoder<? super FriendlyByteBuf, T> streamEncoder) {
        FriendlyByteBuf.writeNullable(this, t, streamEncoder);
    }

    public static <T, B extends ByteBuf> void writeNullable(B b, @Nullable T t, StreamEncoder<? super B, T> streamEncoder) {
        if (t != null) {
            b.writeBoolean(true);
            streamEncoder.encode(b, t);
        } else {
            b.writeBoolean(false);
        }
    }

    public byte[] readByteArray() {
        return FriendlyByteBuf.readByteArray(this);
    }

    public static byte[] readByteArray(ByteBuf byteBuf) {
        return FriendlyByteBuf.readByteArray(byteBuf, byteBuf.readableBytes());
    }

    public FriendlyByteBuf writeByteArray(byte[] byArray) {
        FriendlyByteBuf.writeByteArray(this, byArray);
        return this;
    }

    public static void writeByteArray(ByteBuf byteBuf, byte[] byArray) {
        VarInt.write(byteBuf, byArray.length);
        byteBuf.writeBytes(byArray);
    }

    public byte[] readByteArray(int n) {
        return FriendlyByteBuf.readByteArray(this, n);
    }

    public static byte[] readByteArray(ByteBuf byteBuf, int n) {
        int n2 = VarInt.read(byteBuf);
        if (n2 > n) {
            throw new DecoderException("ByteArray with size " + n2 + " is bigger than allowed " + n);
        }
        byte[] byArray = new byte[n2];
        byteBuf.readBytes(byArray);
        return byArray;
    }

    public FriendlyByteBuf writeVarIntArray(int[] nArray) {
        this.writeVarInt(nArray.length);
        for (int n : nArray) {
            this.writeVarInt(n);
        }
        return this;
    }

    public int[] readVarIntArray() {
        return this.readVarIntArray(this.readableBytes());
    }

    public int[] readVarIntArray(int n) {
        int n2 = this.readVarInt();
        if (n2 > n) {
            throw new DecoderException("VarIntArray with size " + n2 + " is bigger than allowed " + n);
        }
        int[] nArray = new int[n2];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = this.readVarInt();
        }
        return nArray;
    }

    public FriendlyByteBuf writeLongArray(long[] lArray) {
        this.writeVarInt(lArray.length);
        for (long l : lArray) {
            this.writeLong(l);
        }
        return this;
    }

    public long[] readLongArray() {
        return this.readLongArray(null);
    }

    public long[] readLongArray(@Nullable long[] lArray) {
        return this.readLongArray(lArray, this.readableBytes() / 8);
    }

    public long[] readLongArray(@Nullable long[] lArray, int n) {
        int n2 = this.readVarInt();
        if (lArray == null || lArray.length != n2) {
            if (n2 > n) {
                throw new DecoderException("LongArray with size " + n2 + " is bigger than allowed " + n);
            }
            lArray = new long[n2];
        }
        for (int i = 0; i < lArray.length; ++i) {
            lArray[i] = this.readLong();
        }
        return lArray;
    }

    public BlockPos readBlockPos() {
        return FriendlyByteBuf.readBlockPos(this);
    }

    public static BlockPos readBlockPos(ByteBuf byteBuf) {
        return BlockPos.of(byteBuf.readLong());
    }

    public FriendlyByteBuf writeBlockPos(BlockPos blockPos) {
        FriendlyByteBuf.writeBlockPos(this, blockPos);
        return this;
    }

    public static void writeBlockPos(ByteBuf byteBuf, BlockPos blockPos) {
        byteBuf.writeLong(blockPos.asLong());
    }

    public ChunkPos readChunkPos() {
        return new ChunkPos(this.readLong());
    }

    public FriendlyByteBuf writeChunkPos(ChunkPos chunkPos) {
        this.writeLong(chunkPos.toLong());
        return this;
    }

    public SectionPos readSectionPos() {
        return SectionPos.of(this.readLong());
    }

    public FriendlyByteBuf writeSectionPos(SectionPos sectionPos) {
        this.writeLong(sectionPos.asLong());
        return this;
    }

    public GlobalPos readGlobalPos() {
        ResourceKey<Level> resourceKey = this.readResourceKey(Registries.DIMENSION);
        BlockPos blockPos = this.readBlockPos();
        return GlobalPos.of(resourceKey, blockPos);
    }

    public void writeGlobalPos(GlobalPos globalPos) {
        this.writeResourceKey(globalPos.dimension());
        this.writeBlockPos(globalPos.pos());
    }

    public Vector3f readVector3f() {
        return FriendlyByteBuf.readVector3f(this);
    }

    public static Vector3f readVector3f(ByteBuf byteBuf) {
        return new Vector3f(byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat());
    }

    public void writeVector3f(Vector3f vector3f) {
        FriendlyByteBuf.writeVector3f(this, vector3f);
    }

    public static void writeVector3f(ByteBuf byteBuf, Vector3f vector3f) {
        byteBuf.writeFloat(vector3f.x());
        byteBuf.writeFloat(vector3f.y());
        byteBuf.writeFloat(vector3f.z());
    }

    public Quaternionf readQuaternion() {
        return FriendlyByteBuf.readQuaternion(this);
    }

    public static Quaternionf readQuaternion(ByteBuf byteBuf) {
        return new Quaternionf(byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat());
    }

    public void writeQuaternion(Quaternionf quaternionf) {
        FriendlyByteBuf.writeQuaternion(this, quaternionf);
    }

    public static void writeQuaternion(ByteBuf byteBuf, Quaternionf quaternionf) {
        byteBuf.writeFloat(quaternionf.x);
        byteBuf.writeFloat(quaternionf.y);
        byteBuf.writeFloat(quaternionf.z);
        byteBuf.writeFloat(quaternionf.w);
    }

    public Vec3 readVec3() {
        return new Vec3(this.readDouble(), this.readDouble(), this.readDouble());
    }

    public void writeVec3(Vec3 vec3) {
        this.writeDouble(vec3.x());
        this.writeDouble(vec3.y());
        this.writeDouble(vec3.z());
    }

    public <T extends Enum<T>> T readEnum(Class<T> clazz) {
        return (T)((Enum[])clazz.getEnumConstants())[this.readVarInt()];
    }

    public FriendlyByteBuf writeEnum(Enum<?> enum_) {
        return this.writeVarInt(enum_.ordinal());
    }

    public <T> T readById(IntFunction<T> intFunction) {
        int n = this.readVarInt();
        return intFunction.apply(n);
    }

    public <T> FriendlyByteBuf writeById(ToIntFunction<T> toIntFunction, T t) {
        int n = toIntFunction.applyAsInt(t);
        return this.writeVarInt(n);
    }

    public int readVarInt() {
        return VarInt.read(this.source);
    }

    public long readVarLong() {
        return VarLong.read(this.source);
    }

    public FriendlyByteBuf writeUUID(UUID uUID) {
        FriendlyByteBuf.writeUUID(this, uUID);
        return this;
    }

    public static void writeUUID(ByteBuf byteBuf, UUID uUID) {
        byteBuf.writeLong(uUID.getMostSignificantBits());
        byteBuf.writeLong(uUID.getLeastSignificantBits());
    }

    public UUID readUUID() {
        return FriendlyByteBuf.readUUID(this);
    }

    public static UUID readUUID(ByteBuf byteBuf) {
        return new UUID(byteBuf.readLong(), byteBuf.readLong());
    }

    public FriendlyByteBuf writeVarInt(int n) {
        VarInt.write(this.source, n);
        return this;
    }

    public FriendlyByteBuf writeVarLong(long l) {
        VarLong.write(this.source, l);
        return this;
    }

    public FriendlyByteBuf writeNbt(@Nullable Tag tag) {
        FriendlyByteBuf.writeNbt(this, tag);
        return this;
    }

    public static void writeNbt(ByteBuf byteBuf, @Nullable Tag tag) {
        if (tag == null) {
            tag = EndTag.INSTANCE;
        }
        try {
            NbtIo.writeAnyTag(tag, (DataOutput)new ByteBufOutputStream(byteBuf));
        }
        catch (IOException iOException) {
            throw new EncoderException((Throwable)iOException);
        }
    }

    @Nullable
    public CompoundTag readNbt() {
        return FriendlyByteBuf.readNbt(this);
    }

    @Nullable
    public static CompoundTag readNbt(ByteBuf byteBuf) {
        Tag tag = FriendlyByteBuf.readNbt(byteBuf, NbtAccounter.create(0x200000L));
        if (tag == null || tag instanceof CompoundTag) {
            return (CompoundTag)tag;
        }
        throw new DecoderException("Not a compound tag: " + String.valueOf(tag));
    }

    @Nullable
    public static Tag readNbt(ByteBuf byteBuf, NbtAccounter nbtAccounter) {
        try {
            Tag tag = NbtIo.readAnyTag((DataInput)new ByteBufInputStream(byteBuf), nbtAccounter);
            if (tag.getId() == 0) {
                return null;
            }
            return tag;
        }
        catch (IOException iOException) {
            throw new EncoderException((Throwable)iOException);
        }
    }

    @Nullable
    public Tag readNbt(NbtAccounter nbtAccounter) {
        return FriendlyByteBuf.readNbt(this, nbtAccounter);
    }

    public String readUtf() {
        return this.readUtf(Short.MAX_VALUE);
    }

    public String readUtf(int n) {
        return Utf8String.read(this.source, n);
    }

    public FriendlyByteBuf writeUtf(String string) {
        return this.writeUtf(string, Short.MAX_VALUE);
    }

    public FriendlyByteBuf writeUtf(String string, int n) {
        Utf8String.write(this.source, string, n);
        return this;
    }

    public ResourceLocation readResourceLocation() {
        return ResourceLocation.parse(this.readUtf(Short.MAX_VALUE));
    }

    public FriendlyByteBuf writeResourceLocation(ResourceLocation resourceLocation) {
        this.writeUtf(resourceLocation.toString());
        return this;
    }

    public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> resourceKey) {
        ResourceLocation resourceLocation = this.readResourceLocation();
        return ResourceKey.create(resourceKey, resourceLocation);
    }

    public void writeResourceKey(ResourceKey<?> resourceKey) {
        this.writeResourceLocation(resourceKey.location());
    }

    public <T> ResourceKey<? extends Registry<T>> readRegistryKey() {
        ResourceLocation resourceLocation = this.readResourceLocation();
        return ResourceKey.createRegistryKey(resourceLocation);
    }

    public Date readDate() {
        return new Date(this.readLong());
    }

    public FriendlyByteBuf writeDate(Date date) {
        this.writeLong(date.getTime());
        return this;
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant instant) {
        this.writeLong(instant.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return Crypt.byteToPublicKey(this.readByteArray(512));
        }
        catch (CryptException cryptException) {
            throw new DecoderException("Malformed public key bytes", (Throwable)cryptException);
        }
    }

    public FriendlyByteBuf writePublicKey(PublicKey publicKey) {
        this.writeByteArray(publicKey.getEncoded());
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        BlockPos blockPos = this.readBlockPos();
        Direction direction = this.readEnum(Direction.class);
        float f = this.readFloat();
        float f2 = this.readFloat();
        float f3 = this.readFloat();
        boolean bl = this.readBoolean();
        return new BlockHitResult(new Vec3((double)blockPos.getX() + (double)f, (double)blockPos.getY() + (double)f2, (double)blockPos.getZ() + (double)f3), direction, blockPos, bl);
    }

    public void writeBlockHitResult(BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        this.writeBlockPos(blockPos);
        this.writeEnum(blockHitResult.getDirection());
        Vec3 vec3 = blockHitResult.getLocation();
        this.writeFloat((float)(vec3.x - (double)blockPos.getX()));
        this.writeFloat((float)(vec3.y - (double)blockPos.getY()));
        this.writeFloat((float)(vec3.z - (double)blockPos.getZ()));
        this.writeBoolean(blockHitResult.isInside());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet bitSet) {
        this.writeLongArray(bitSet.toLongArray());
    }

    public BitSet readFixedBitSet(int n) {
        byte[] byArray = new byte[Mth.positiveCeilDiv(n, 8)];
        this.readBytes(byArray);
        return BitSet.valueOf(byArray);
    }

    public void writeFixedBitSet(BitSet bitSet, int n) {
        if (bitSet.length() > n) {
            throw new EncoderException("BitSet is larger than expected size (" + bitSet.length() + ">" + n + ")");
        }
        byte[] byArray = bitSet.toByteArray();
        this.writeBytes(Arrays.copyOf(byArray, Mth.positiveCeilDiv(n, 8)));
    }

    public boolean isContiguous() {
        return this.source.isContiguous();
    }

    public int maxFastWritableBytes() {
        return this.source.maxFastWritableBytes();
    }

    public int capacity() {
        return this.source.capacity();
    }

    public FriendlyByteBuf capacity(int n) {
        this.source.capacity(n);
        return this;
    }

    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    public ByteOrder order() {
        return this.source.order();
    }

    public ByteBuf order(ByteOrder byteOrder) {
        return this.source.order(byteOrder);
    }

    public ByteBuf unwrap() {
        return this.source;
    }

    public boolean isDirect() {
        return this.source.isDirect();
    }

    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    public int readerIndex() {
        return this.source.readerIndex();
    }

    public FriendlyByteBuf readerIndex(int n) {
        this.source.readerIndex(n);
        return this;
    }

    public int writerIndex() {
        return this.source.writerIndex();
    }

    public FriendlyByteBuf writerIndex(int n) {
        this.source.writerIndex(n);
        return this;
    }

    public FriendlyByteBuf setIndex(int n, int n2) {
        this.source.setIndex(n, n2);
        return this;
    }

    public int readableBytes() {
        return this.source.readableBytes();
    }

    public int writableBytes() {
        return this.source.writableBytes();
    }

    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    public boolean isReadable() {
        return this.source.isReadable();
    }

    public boolean isReadable(int n) {
        return this.source.isReadable(n);
    }

    public boolean isWritable() {
        return this.source.isWritable();
    }

    public boolean isWritable(int n) {
        return this.source.isWritable(n);
    }

    public FriendlyByteBuf clear() {
        this.source.clear();
        return this;
    }

    public FriendlyByteBuf markReaderIndex() {
        this.source.markReaderIndex();
        return this;
    }

    public FriendlyByteBuf resetReaderIndex() {
        this.source.resetReaderIndex();
        return this;
    }

    public FriendlyByteBuf markWriterIndex() {
        this.source.markWriterIndex();
        return this;
    }

    public FriendlyByteBuf resetWriterIndex() {
        this.source.resetWriterIndex();
        return this;
    }

    public FriendlyByteBuf discardReadBytes() {
        this.source.discardReadBytes();
        return this;
    }

    public FriendlyByteBuf discardSomeReadBytes() {
        this.source.discardSomeReadBytes();
        return this;
    }

    public FriendlyByteBuf ensureWritable(int n) {
        this.source.ensureWritable(n);
        return this;
    }

    public int ensureWritable(int n, boolean bl) {
        return this.source.ensureWritable(n, bl);
    }

    public boolean getBoolean(int n) {
        return this.source.getBoolean(n);
    }

    public byte getByte(int n) {
        return this.source.getByte(n);
    }

    public short getUnsignedByte(int n) {
        return this.source.getUnsignedByte(n);
    }

    public short getShort(int n) {
        return this.source.getShort(n);
    }

    public short getShortLE(int n) {
        return this.source.getShortLE(n);
    }

    public int getUnsignedShort(int n) {
        return this.source.getUnsignedShort(n);
    }

    public int getUnsignedShortLE(int n) {
        return this.source.getUnsignedShortLE(n);
    }

    public int getMedium(int n) {
        return this.source.getMedium(n);
    }

    public int getMediumLE(int n) {
        return this.source.getMediumLE(n);
    }

    public int getUnsignedMedium(int n) {
        return this.source.getUnsignedMedium(n);
    }

    public int getUnsignedMediumLE(int n) {
        return this.source.getUnsignedMediumLE(n);
    }

    public int getInt(int n) {
        return this.source.getInt(n);
    }

    public int getIntLE(int n) {
        return this.source.getIntLE(n);
    }

    public long getUnsignedInt(int n) {
        return this.source.getUnsignedInt(n);
    }

    public long getUnsignedIntLE(int n) {
        return this.source.getUnsignedIntLE(n);
    }

    public long getLong(int n) {
        return this.source.getLong(n);
    }

    public long getLongLE(int n) {
        return this.source.getLongLE(n);
    }

    public char getChar(int n) {
        return this.source.getChar(n);
    }

    public float getFloat(int n) {
        return this.source.getFloat(n);
    }

    public double getDouble(int n) {
        return this.source.getDouble(n);
    }

    public FriendlyByteBuf getBytes(int n, ByteBuf byteBuf) {
        this.source.getBytes(n, byteBuf);
        return this;
    }

    public FriendlyByteBuf getBytes(int n, ByteBuf byteBuf, int n2) {
        this.source.getBytes(n, byteBuf, n2);
        return this;
    }

    public FriendlyByteBuf getBytes(int n, ByteBuf byteBuf, int n2, int n3) {
        this.source.getBytes(n, byteBuf, n2, n3);
        return this;
    }

    public FriendlyByteBuf getBytes(int n, byte[] byArray) {
        this.source.getBytes(n, byArray);
        return this;
    }

    public FriendlyByteBuf getBytes(int n, byte[] byArray, int n2, int n3) {
        this.source.getBytes(n, byArray, n2, n3);
        return this;
    }

    public FriendlyByteBuf getBytes(int n, ByteBuffer byteBuffer) {
        this.source.getBytes(n, byteBuffer);
        return this;
    }

    public FriendlyByteBuf getBytes(int n, OutputStream outputStream, int n2) throws IOException {
        this.source.getBytes(n, outputStream, n2);
        return this;
    }

    public int getBytes(int n, GatheringByteChannel gatheringByteChannel, int n2) throws IOException {
        return this.source.getBytes(n, gatheringByteChannel, n2);
    }

    public int getBytes(int n, FileChannel fileChannel, long l, int n2) throws IOException {
        return this.source.getBytes(n, fileChannel, l, n2);
    }

    public CharSequence getCharSequence(int n, int n2, Charset charset) {
        return this.source.getCharSequence(n, n2, charset);
    }

    public FriendlyByteBuf setBoolean(int n, boolean bl) {
        this.source.setBoolean(n, bl);
        return this;
    }

    public FriendlyByteBuf setByte(int n, int n2) {
        this.source.setByte(n, n2);
        return this;
    }

    public FriendlyByteBuf setShort(int n, int n2) {
        this.source.setShort(n, n2);
        return this;
    }

    public FriendlyByteBuf setShortLE(int n, int n2) {
        this.source.setShortLE(n, n2);
        return this;
    }

    public FriendlyByteBuf setMedium(int n, int n2) {
        this.source.setMedium(n, n2);
        return this;
    }

    public FriendlyByteBuf setMediumLE(int n, int n2) {
        this.source.setMediumLE(n, n2);
        return this;
    }

    public FriendlyByteBuf setInt(int n, int n2) {
        this.source.setInt(n, n2);
        return this;
    }

    public FriendlyByteBuf setIntLE(int n, int n2) {
        this.source.setIntLE(n, n2);
        return this;
    }

    public FriendlyByteBuf setLong(int n, long l) {
        this.source.setLong(n, l);
        return this;
    }

    public FriendlyByteBuf setLongLE(int n, long l) {
        this.source.setLongLE(n, l);
        return this;
    }

    public FriendlyByteBuf setChar(int n, int n2) {
        this.source.setChar(n, n2);
        return this;
    }

    public FriendlyByteBuf setFloat(int n, float f) {
        this.source.setFloat(n, f);
        return this;
    }

    public FriendlyByteBuf setDouble(int n, double d) {
        this.source.setDouble(n, d);
        return this;
    }

    public FriendlyByteBuf setBytes(int n, ByteBuf byteBuf) {
        this.source.setBytes(n, byteBuf);
        return this;
    }

    public FriendlyByteBuf setBytes(int n, ByteBuf byteBuf, int n2) {
        this.source.setBytes(n, byteBuf, n2);
        return this;
    }

    public FriendlyByteBuf setBytes(int n, ByteBuf byteBuf, int n2, int n3) {
        this.source.setBytes(n, byteBuf, n2, n3);
        return this;
    }

    public FriendlyByteBuf setBytes(int n, byte[] byArray) {
        this.source.setBytes(n, byArray);
        return this;
    }

    public FriendlyByteBuf setBytes(int n, byte[] byArray, int n2, int n3) {
        this.source.setBytes(n, byArray, n2, n3);
        return this;
    }

    public FriendlyByteBuf setBytes(int n, ByteBuffer byteBuffer) {
        this.source.setBytes(n, byteBuffer);
        return this;
    }

    public int setBytes(int n, InputStream inputStream, int n2) throws IOException {
        return this.source.setBytes(n, inputStream, n2);
    }

    public int setBytes(int n, ScatteringByteChannel scatteringByteChannel, int n2) throws IOException {
        return this.source.setBytes(n, scatteringByteChannel, n2);
    }

    public int setBytes(int n, FileChannel fileChannel, long l, int n2) throws IOException {
        return this.source.setBytes(n, fileChannel, l, n2);
    }

    public FriendlyByteBuf setZero(int n, int n2) {
        this.source.setZero(n, n2);
        return this;
    }

    public int setCharSequence(int n, CharSequence charSequence, Charset charset) {
        return this.source.setCharSequence(n, charSequence, charset);
    }

    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    public byte readByte() {
        return this.source.readByte();
    }

    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    public short readShort() {
        return this.source.readShort();
    }

    public short readShortLE() {
        return this.source.readShortLE();
    }

    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    public int readMedium() {
        return this.source.readMedium();
    }

    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    public int readInt() {
        return this.source.readInt();
    }

    public int readIntLE() {
        return this.source.readIntLE();
    }

    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    public long readLong() {
        return this.source.readLong();
    }

    public long readLongLE() {
        return this.source.readLongLE();
    }

    public char readChar() {
        return this.source.readChar();
    }

    public float readFloat() {
        return this.source.readFloat();
    }

    public double readDouble() {
        return this.source.readDouble();
    }

    public ByteBuf readBytes(int n) {
        return this.source.readBytes(n);
    }

    public ByteBuf readSlice(int n) {
        return this.source.readSlice(n);
    }

    public ByteBuf readRetainedSlice(int n) {
        return this.source.readRetainedSlice(n);
    }

    public FriendlyByteBuf readBytes(ByteBuf byteBuf) {
        this.source.readBytes(byteBuf);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf byteBuf, int n) {
        this.source.readBytes(byteBuf, n);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf byteBuf, int n, int n2) {
        this.source.readBytes(byteBuf, n, n2);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] byArray) {
        this.source.readBytes(byArray);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] byArray, int n, int n2) {
        this.source.readBytes(byArray, n, n2);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuffer byteBuffer) {
        this.source.readBytes(byteBuffer);
        return this;
    }

    public FriendlyByteBuf readBytes(OutputStream outputStream, int n) throws IOException {
        this.source.readBytes(outputStream, n);
        return this;
    }

    public int readBytes(GatheringByteChannel gatheringByteChannel, int n) throws IOException {
        return this.source.readBytes(gatheringByteChannel, n);
    }

    public CharSequence readCharSequence(int n, Charset charset) {
        return this.source.readCharSequence(n, charset);
    }

    public int readBytes(FileChannel fileChannel, long l, int n) throws IOException {
        return this.source.readBytes(fileChannel, l, n);
    }

    public FriendlyByteBuf skipBytes(int n) {
        this.source.skipBytes(n);
        return this;
    }

    public FriendlyByteBuf writeBoolean(boolean bl) {
        this.source.writeBoolean(bl);
        return this;
    }

    public FriendlyByteBuf writeByte(int n) {
        this.source.writeByte(n);
        return this;
    }

    public FriendlyByteBuf writeShort(int n) {
        this.source.writeShort(n);
        return this;
    }

    public FriendlyByteBuf writeShortLE(int n) {
        this.source.writeShortLE(n);
        return this;
    }

    public FriendlyByteBuf writeMedium(int n) {
        this.source.writeMedium(n);
        return this;
    }

    public FriendlyByteBuf writeMediumLE(int n) {
        this.source.writeMediumLE(n);
        return this;
    }

    public FriendlyByteBuf writeInt(int n) {
        this.source.writeInt(n);
        return this;
    }

    public FriendlyByteBuf writeIntLE(int n) {
        this.source.writeIntLE(n);
        return this;
    }

    public FriendlyByteBuf writeLong(long l) {
        this.source.writeLong(l);
        return this;
    }

    public FriendlyByteBuf writeLongLE(long l) {
        this.source.writeLongLE(l);
        return this;
    }

    public FriendlyByteBuf writeChar(int n) {
        this.source.writeChar(n);
        return this;
    }

    public FriendlyByteBuf writeFloat(float f) {
        this.source.writeFloat(f);
        return this;
    }

    public FriendlyByteBuf writeDouble(double d) {
        this.source.writeDouble(d);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf byteBuf) {
        this.source.writeBytes(byteBuf);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf byteBuf, int n) {
        this.source.writeBytes(byteBuf, n);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf byteBuf, int n, int n2) {
        this.source.writeBytes(byteBuf, n, n2);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] byArray) {
        this.source.writeBytes(byArray);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] byArray, int n, int n2) {
        this.source.writeBytes(byArray, n, n2);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuffer byteBuffer) {
        this.source.writeBytes(byteBuffer);
        return this;
    }

    public int writeBytes(InputStream inputStream, int n) throws IOException {
        return this.source.writeBytes(inputStream, n);
    }

    public int writeBytes(ScatteringByteChannel scatteringByteChannel, int n) throws IOException {
        return this.source.writeBytes(scatteringByteChannel, n);
    }

    public int writeBytes(FileChannel fileChannel, long l, int n) throws IOException {
        return this.source.writeBytes(fileChannel, l, n);
    }

    public FriendlyByteBuf writeZero(int n) {
        this.source.writeZero(n);
        return this;
    }

    public int writeCharSequence(CharSequence charSequence, Charset charset) {
        return this.source.writeCharSequence(charSequence, charset);
    }

    public int indexOf(int n, int n2, byte by) {
        return this.source.indexOf(n, n2, by);
    }

    public int bytesBefore(byte by) {
        return this.source.bytesBefore(by);
    }

    public int bytesBefore(int n, byte by) {
        return this.source.bytesBefore(n, by);
    }

    public int bytesBefore(int n, int n2, byte by) {
        return this.source.bytesBefore(n, n2, by);
    }

    public int forEachByte(ByteProcessor byteProcessor) {
        return this.source.forEachByte(byteProcessor);
    }

    public int forEachByte(int n, int n2, ByteProcessor byteProcessor) {
        return this.source.forEachByte(n, n2, byteProcessor);
    }

    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return this.source.forEachByteDesc(byteProcessor);
    }

    public int forEachByteDesc(int n, int n2, ByteProcessor byteProcessor) {
        return this.source.forEachByteDesc(n, n2, byteProcessor);
    }

    public ByteBuf copy() {
        return this.source.copy();
    }

    public ByteBuf copy(int n, int n2) {
        return this.source.copy(n, n2);
    }

    public ByteBuf slice() {
        return this.source.slice();
    }

    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    public ByteBuf slice(int n, int n2) {
        return this.source.slice(n, n2);
    }

    public ByteBuf retainedSlice(int n, int n2) {
        return this.source.retainedSlice(n, n2);
    }

    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    public ByteBuffer nioBuffer(int n, int n2) {
        return this.source.nioBuffer(n, n2);
    }

    public ByteBuffer internalNioBuffer(int n, int n2) {
        return this.source.internalNioBuffer(n, n2);
    }

    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int n, int n2) {
        return this.source.nioBuffers(n, n2);
    }

    public boolean hasArray() {
        return this.source.hasArray();
    }

    public byte[] array() {
        return this.source.array();
    }

    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    public String toString(Charset charset) {
        return this.source.toString(charset);
    }

    public String toString(int n, int n2, Charset charset) {
        return this.source.toString(n, n2, charset);
    }

    public int hashCode() {
        return this.source.hashCode();
    }

    public boolean equals(Object object) {
        return this.source.equals(object);
    }

    public int compareTo(ByteBuf byteBuf) {
        return this.source.compareTo(byteBuf);
    }

    public String toString() {
        return this.source.toString();
    }

    public FriendlyByteBuf retain(int n) {
        this.source.retain(n);
        return this;
    }

    public FriendlyByteBuf retain() {
        this.source.retain();
        return this;
    }

    public FriendlyByteBuf touch() {
        this.source.touch();
        return this;
    }

    public FriendlyByteBuf touch(Object object) {
        this.source.touch(object);
        return this;
    }

    public int refCnt() {
        return this.source.refCnt();
    }

    public boolean release() {
        return this.source.release();
    }

    public boolean release(int n) {
        return this.source.release(n);
    }

    public /* synthetic */ ByteBuf touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ByteBuf touch() {
        return this.touch();
    }

    public /* synthetic */ ByteBuf retain() {
        return this.retain();
    }

    public /* synthetic */ ByteBuf retain(int n) {
        return this.retain(n);
    }

    public /* synthetic */ ByteBuf writeZero(int n) {
        return this.writeZero(n);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuffer byteBuffer) {
        return this.writeBytes(byteBuffer);
    }

    public /* synthetic */ ByteBuf writeBytes(byte[] byArray, int n, int n2) {
        return this.writeBytes(byArray, n, n2);
    }

    public /* synthetic */ ByteBuf writeBytes(byte[] byArray) {
        return this.writeBytes(byArray);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf, int n, int n2) {
        return this.writeBytes(byteBuf, n, n2);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf, int n) {
        return this.writeBytes(byteBuf, n);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf) {
        return this.writeBytes(byteBuf);
    }

    public /* synthetic */ ByteBuf writeDouble(double d) {
        return this.writeDouble(d);
    }

    public /* synthetic */ ByteBuf writeFloat(float f) {
        return this.writeFloat(f);
    }

    public /* synthetic */ ByteBuf writeChar(int n) {
        return this.writeChar(n);
    }

    public /* synthetic */ ByteBuf writeLongLE(long l) {
        return this.writeLongLE(l);
    }

    public /* synthetic */ ByteBuf writeLong(long l) {
        return this.writeLong(l);
    }

    public /* synthetic */ ByteBuf writeIntLE(int n) {
        return this.writeIntLE(n);
    }

    public /* synthetic */ ByteBuf writeInt(int n) {
        return this.writeInt(n);
    }

    public /* synthetic */ ByteBuf writeMediumLE(int n) {
        return this.writeMediumLE(n);
    }

    public /* synthetic */ ByteBuf writeMedium(int n) {
        return this.writeMedium(n);
    }

    public /* synthetic */ ByteBuf writeShortLE(int n) {
        return this.writeShortLE(n);
    }

    public /* synthetic */ ByteBuf writeShort(int n) {
        return this.writeShort(n);
    }

    public /* synthetic */ ByteBuf writeByte(int n) {
        return this.writeByte(n);
    }

    public /* synthetic */ ByteBuf writeBoolean(boolean bl) {
        return this.writeBoolean(bl);
    }

    public /* synthetic */ ByteBuf skipBytes(int n) {
        return this.skipBytes(n);
    }

    public /* synthetic */ ByteBuf readBytes(OutputStream outputStream, int n) throws IOException {
        return this.readBytes(outputStream, n);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuffer byteBuffer) {
        return this.readBytes(byteBuffer);
    }

    public /* synthetic */ ByteBuf readBytes(byte[] byArray, int n, int n2) {
        return this.readBytes(byArray, n, n2);
    }

    public /* synthetic */ ByteBuf readBytes(byte[] byArray) {
        return this.readBytes(byArray);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf, int n, int n2) {
        return this.readBytes(byteBuf, n, n2);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf, int n) {
        return this.readBytes(byteBuf, n);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf) {
        return this.readBytes(byteBuf);
    }

    public /* synthetic */ ByteBuf setZero(int n, int n2) {
        return this.setZero(n, n2);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuffer byteBuffer) {
        return this.setBytes(n, byteBuffer);
    }

    public /* synthetic */ ByteBuf setBytes(int n, byte[] byArray, int n2, int n3) {
        return this.setBytes(n, byArray, n2, n3);
    }

    public /* synthetic */ ByteBuf setBytes(int n, byte[] byArray) {
        return this.setBytes(n, byArray);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuf byteBuf, int n2, int n3) {
        return this.setBytes(n, byteBuf, n2, n3);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuf byteBuf, int n2) {
        return this.setBytes(n, byteBuf, n2);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuf byteBuf) {
        return this.setBytes(n, byteBuf);
    }

    public /* synthetic */ ByteBuf setDouble(int n, double d) {
        return this.setDouble(n, d);
    }

    public /* synthetic */ ByteBuf setFloat(int n, float f) {
        return this.setFloat(n, f);
    }

    public /* synthetic */ ByteBuf setChar(int n, int n2) {
        return this.setChar(n, n2);
    }

    public /* synthetic */ ByteBuf setLongLE(int n, long l) {
        return this.setLongLE(n, l);
    }

    public /* synthetic */ ByteBuf setLong(int n, long l) {
        return this.setLong(n, l);
    }

    public /* synthetic */ ByteBuf setIntLE(int n, int n2) {
        return this.setIntLE(n, n2);
    }

    public /* synthetic */ ByteBuf setInt(int n, int n2) {
        return this.setInt(n, n2);
    }

    public /* synthetic */ ByteBuf setMediumLE(int n, int n2) {
        return this.setMediumLE(n, n2);
    }

    public /* synthetic */ ByteBuf setMedium(int n, int n2) {
        return this.setMedium(n, n2);
    }

    public /* synthetic */ ByteBuf setShortLE(int n, int n2) {
        return this.setShortLE(n, n2);
    }

    public /* synthetic */ ByteBuf setShort(int n, int n2) {
        return this.setShort(n, n2);
    }

    public /* synthetic */ ByteBuf setByte(int n, int n2) {
        return this.setByte(n, n2);
    }

    public /* synthetic */ ByteBuf setBoolean(int n, boolean bl) {
        return this.setBoolean(n, bl);
    }

    public /* synthetic */ ByteBuf getBytes(int n, OutputStream outputStream, int n2) throws IOException {
        return this.getBytes(n, outputStream, n2);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuffer byteBuffer) {
        return this.getBytes(n, byteBuffer);
    }

    public /* synthetic */ ByteBuf getBytes(int n, byte[] byArray, int n2, int n3) {
        return this.getBytes(n, byArray, n2, n3);
    }

    public /* synthetic */ ByteBuf getBytes(int n, byte[] byArray) {
        return this.getBytes(n, byArray);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuf byteBuf, int n2, int n3) {
        return this.getBytes(n, byteBuf, n2, n3);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuf byteBuf, int n2) {
        return this.getBytes(n, byteBuf, n2);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuf byteBuf) {
        return this.getBytes(n, byteBuf);
    }

    public /* synthetic */ ByteBuf ensureWritable(int n) {
        return this.ensureWritable(n);
    }

    public /* synthetic */ ByteBuf discardSomeReadBytes() {
        return this.discardSomeReadBytes();
    }

    public /* synthetic */ ByteBuf discardReadBytes() {
        return this.discardReadBytes();
    }

    public /* synthetic */ ByteBuf resetWriterIndex() {
        return this.resetWriterIndex();
    }

    public /* synthetic */ ByteBuf markWriterIndex() {
        return this.markWriterIndex();
    }

    public /* synthetic */ ByteBuf resetReaderIndex() {
        return this.resetReaderIndex();
    }

    public /* synthetic */ ByteBuf markReaderIndex() {
        return this.markReaderIndex();
    }

    public /* synthetic */ ByteBuf clear() {
        return this.clear();
    }

    public /* synthetic */ ByteBuf setIndex(int n, int n2) {
        return this.setIndex(n, n2);
    }

    public /* synthetic */ ByteBuf writerIndex(int n) {
        return this.writerIndex(n);
    }

    public /* synthetic */ ByteBuf readerIndex(int n) {
        return this.readerIndex(n);
    }

    public /* synthetic */ ByteBuf capacity(int n) {
        return this.capacity(n);
    }

    public /* synthetic */ ReferenceCounted touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ReferenceCounted touch() {
        return this.touch();
    }

    public /* synthetic */ ReferenceCounted retain(int n) {
        return this.retain(n);
    }

    public /* synthetic */ ReferenceCounted retain() {
        return this.retain();
    }
}

