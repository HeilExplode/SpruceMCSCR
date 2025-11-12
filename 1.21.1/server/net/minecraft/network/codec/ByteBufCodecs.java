/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package net.minecraft.network.codec;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ByteBufCodecs {
    public static final int MAX_INITIAL_COLLECTION_SIZE = 65536;
    public static final StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>(){

        @Override
        public Boolean decode(ByteBuf byteBuf) {
            return byteBuf.readBoolean();
        }

        @Override
        public void encode(ByteBuf byteBuf, Boolean bl) {
            byteBuf.writeBoolean(bl.booleanValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Boolean)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>(){

        @Override
        public Byte decode(ByteBuf byteBuf) {
            return byteBuf.readByte();
        }

        @Override
        public void encode(ByteBuf byteBuf, Byte by) {
            byteBuf.writeByte((int)by.byteValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Byte)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>(){

        @Override
        public Short decode(ByteBuf byteBuf) {
            return byteBuf.readShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Short s) {
            byteBuf.writeShort((int)s.shortValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Short)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readUnsignedShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer n) {
            byteBuf.writeShort(n.intValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readInt();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer n) {
            byteBuf.writeInt(n.intValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return VarInt.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer n) {
            VarInt.write(byteBuf, n);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf byteBuf) {
            return VarLong.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Long l) {
            VarLong.write(byteBuf, l);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>(){

        @Override
        public Float decode(ByteBuf byteBuf) {
            return Float.valueOf(byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, Float f) {
            byteBuf.writeFloat(f.floatValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Float)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>(){

        @Override
        public Double decode(ByteBuf byteBuf) {
            return byteBuf.readDouble();
        }

        @Override
        public void encode(ByteBuf byteBuf, Double d) {
            byteBuf.writeDouble(d.doubleValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Double)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>(){

        @Override
        public byte[] decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readByteArray(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, byte[] byArray) {
            FriendlyByteBuf.writeByteArray(byteBuf, byArray);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (byte[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = ByteBufCodecs.stringUtf8(Short.MAX_VALUE);
    public static final StreamCodec<ByteBuf, Tag> TAG = ByteBufCodecs.tagCodec(() -> NbtAccounter.create(0x200000L));
    public static final StreamCodec<ByteBuf, Tag> TRUSTED_TAG = ByteBufCodecs.tagCodec(NbtAccounter::unlimitedHeap);
    public static final StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = ByteBufCodecs.compoundTagCodec(() -> NbtAccounter.create(0x200000L));
    public static final StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = ByteBufCodecs.compoundTagCodec(NbtAccounter::unlimitedHeap);
    public static final StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>(){

        @Override
        public Optional<CompoundTag> decode(ByteBuf byteBuf) {
            return Optional.ofNullable(FriendlyByteBuf.readNbt(byteBuf));
        }

        @Override
        public void encode(ByteBuf byteBuf, Optional<CompoundTag> optional) {
            FriendlyByteBuf.writeNbt(byteBuf, optional.orElse(null));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<ByteBuf, Vector3f>(){

        @Override
        public Vector3f decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readVector3f(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Vector3f vector3f) {
            FriendlyByteBuf.writeVector3f(byteBuf, vector3f);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Vector3f)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Quaternionf> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionf>(){

        @Override
        public Quaternionf decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readQuaternion(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Quaternionf quaternionf) {
            FriendlyByteBuf.writeQuaternion(byteBuf, quaternionf);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Quaternionf)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>(){
        private static final int MAX_PROPERTY_NAME_LENGTH = 64;
        private static final int MAX_PROPERTY_VALUE_LENGTH = Short.MAX_VALUE;
        private static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
        private static final int MAX_PROPERTIES = 16;

        @Override
        public PropertyMap decode(ByteBuf byteBuf2) {
            int n = ByteBufCodecs.readCount(byteBuf2, 16);
            PropertyMap propertyMap = new PropertyMap();
            for (int i = 0; i < n; ++i) {
                String string = Utf8String.read(byteBuf2, 64);
                String string2 = Utf8String.read(byteBuf2, Short.MAX_VALUE);
                String string3 = FriendlyByteBuf.readNullable(byteBuf2, byteBuf -> Utf8String.read(byteBuf, 1024));
                Property property = new Property(string, string2, string3);
                propertyMap.put((Object)property.name(), (Object)property);
            }
            return propertyMap;
        }

        @Override
        public void encode(ByteBuf byteBuf2, PropertyMap propertyMap) {
            ByteBufCodecs.writeCount(byteBuf2, propertyMap.size(), 16);
            for (Property property : propertyMap.values()) {
                Utf8String.write(byteBuf2, property.name(), 64);
                Utf8String.write(byteBuf2, property.value(), Short.MAX_VALUE);
                FriendlyByteBuf.writeNullable(byteBuf2, property.signature(), (byteBuf, string) -> Utf8String.write(byteBuf, string, 1024));
            }
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (PropertyMap)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = new StreamCodec<ByteBuf, GameProfile>(){

        @Override
        public GameProfile decode(ByteBuf byteBuf) {
            UUID uUID = (UUID)UUIDUtil.STREAM_CODEC.decode(byteBuf);
            String string = Utf8String.read(byteBuf, 16);
            GameProfile gameProfile = new GameProfile(uUID, string);
            gameProfile.getProperties().putAll((Multimap)GAME_PROFILE_PROPERTIES.decode(byteBuf));
            return gameProfile;
        }

        @Override
        public void encode(ByteBuf byteBuf, GameProfile gameProfile) {
            UUIDUtil.STREAM_CODEC.encode(byteBuf, gameProfile.getId());
            Utf8String.write(byteBuf, gameProfile.getName(), 16);
            GAME_PROFILE_PROPERTIES.encode(byteBuf, gameProfile.getProperties());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (GameProfile)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public static StreamCodec<ByteBuf, byte[]> byteArray(final int n) {
        return new StreamCodec<ByteBuf, byte[]>(){

            @Override
            public byte[] decode(ByteBuf byteBuf) {
                return FriendlyByteBuf.readByteArray(byteBuf, n);
            }

            @Override
            public void encode(ByteBuf byteBuf, byte[] byArray) {
                if (byArray.length > n) {
                    throw new EncoderException("ByteArray with size " + byArray.length + " is bigger than allowed " + n);
                }
                FriendlyByteBuf.writeByteArray(byteBuf, byArray);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (byte[])object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, String> stringUtf8(final int n) {
        return new StreamCodec<ByteBuf, String>(){

            @Override
            public String decode(ByteBuf byteBuf) {
                return Utf8String.read(byteBuf, n);
            }

            @Override
            public void encode(ByteBuf byteBuf, String string) {
                Utf8String.write(byteBuf, string, n);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (String)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, Tag> tagCodec(final Supplier<NbtAccounter> supplier) {
        return new StreamCodec<ByteBuf, Tag>(){

            @Override
            public Tag decode(ByteBuf byteBuf) {
                Tag tag = FriendlyByteBuf.readNbt(byteBuf, (NbtAccounter)supplier.get());
                if (tag == null) {
                    throw new DecoderException("Expected non-null compound tag");
                }
                return tag;
            }

            @Override
            public void encode(ByteBuf byteBuf, Tag tag) {
                if (tag == EndTag.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                }
                FriendlyByteBuf.writeNbt(byteBuf, tag);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (Tag)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> supplier) {
        return ByteBufCodecs.tagCodec(supplier).map(tag -> {
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                return compoundTag;
            }
            throw new DecoderException("Not a compound tag: " + String.valueOf(tag));
        }, compoundTag -> compoundTag);
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> codec) {
        return ByteBufCodecs.fromCodec(codec, NbtAccounter::unlimitedHeap);
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
        return ByteBufCodecs.fromCodec(codec, () -> NbtAccounter.create(0x200000L));
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec, Supplier<NbtAccounter> supplier) {
        return ByteBufCodecs.tagCodec(supplier).map(tag -> codec.parse((DynamicOps)NbtOps.INSTANCE, tag).getOrThrow(string -> new DecoderException("Failed to decode: " + string + " " + String.valueOf(tag))), object -> (Tag)codec.encodeStart((DynamicOps)NbtOps.INSTANCE, object).getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(object))));
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> codec) {
        return ByteBufCodecs.fromCodecWithRegistries(codec, NbtAccounter::unlimitedHeap);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
        return ByteBufCodecs.fromCodecWithRegistries(codec, () -> NbtAccounter.create(0x200000L));
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> codec, Supplier<NbtAccounter> supplier) {
        final StreamCodec<ByteBuf, Tag> streamCodec = ByteBufCodecs.tagCodec(supplier);
        return new StreamCodec<RegistryFriendlyByteBuf, T>(){

            @Override
            public T decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                Tag tag = (Tag)streamCodec.decode(registryFriendlyByteBuf);
                RegistryOps<Tag> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                return codec.parse(registryOps, (Object)tag).getOrThrow(string -> new DecoderException("Failed to decode: " + string + " " + String.valueOf(tag)));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, T t) {
                RegistryOps<Tag> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                Tag tag = (Tag)codec.encodeStart(registryOps, t).getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(t)));
                streamCodec.encode(registryFriendlyByteBuf, tag);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<B, V> streamCodec) {
        return new StreamCodec<B, Optional<V>>(){

            @Override
            public Optional<V> decode(B b) {
                if (b.readBoolean()) {
                    return Optional.of(streamCodec.decode(b));
                }
                return Optional.empty();
            }

            @Override
            public void encode(B b, Optional<V> optional) {
                if (optional.isPresent()) {
                    b.writeBoolean(true);
                    streamCodec.encode(b, optional.get());
                } else {
                    b.writeBoolean(false);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static int readCount(ByteBuf byteBuf, int n) {
        int n2 = VarInt.read(byteBuf);
        if (n2 > n) {
            throw new DecoderException(n2 + " elements exceeded max size of: " + n);
        }
        return n2;
    }

    public static void writeCount(ByteBuf byteBuf, int n, int n2) {
        if (n > n2) {
            throw new EncoderException(n + " elements exceeded max size of: " + n2);
        }
        VarInt.write(byteBuf, n);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> intFunction, StreamCodec<? super B, V> streamCodec) {
        return ByteBufCodecs.collection(intFunction, streamCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(final IntFunction<C> intFunction, final StreamCodec<? super B, V> streamCodec, final int n) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B b) {
                int n2 = ByteBufCodecs.readCount(b, n);
                Collection collection = (Collection)intFunction.apply(Math.min(n2, 65536));
                for (int i = 0; i < n2; ++i) {
                    collection.add(streamCodec.decode(b));
                }
                return collection;
            }

            @Override
            public void encode(B b, C c) {
                ByteBufCodecs.writeCount(b, c.size(), n);
                for (Object e : c) {
                    streamCodec.encode(b, e);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (C)((Collection)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> intFunction) {
        return streamCodec -> ByteBufCodecs.collection(intFunction, streamCodec);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return streamCodec -> ByteBufCodecs.collection(ArrayList::new, streamCodec);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int n) {
        return streamCodec -> ByteBufCodecs.collection(ArrayList::new, streamCodec, n);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(IntFunction<? extends M> intFunction, StreamCodec<? super B, K> streamCodec, StreamCodec<? super B, V> streamCodec2) {
        return ByteBufCodecs.map(intFunction, streamCodec, streamCodec2, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(final IntFunction<? extends M> intFunction, final StreamCodec<? super B, K> streamCodec, final StreamCodec<? super B, V> streamCodec2, final int n) {
        return new StreamCodec<B, M>(){

            @Override
            public void encode(B b, M m) {
                ByteBufCodecs.writeCount(b, m.size(), n);
                m.forEach((object, object2) -> {
                    streamCodec.encode(b, object);
                    streamCodec2.encode(b, object2);
                });
            }

            @Override
            public M decode(B b) {
                int n2 = ByteBufCodecs.readCount(b, n);
                Map map = (Map)intFunction.apply(Math.min(n2, 65536));
                for (int i = 0; i < n2; ++i) {
                    Object t = streamCodec.decode(b);
                    Object t2 = streamCodec2.decode(b);
                    map.put(t, t2);
                }
                return map;
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (M)((Map)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(final StreamCodec<? super B, L> streamCodec, final StreamCodec<? super B, R> streamCodec2) {
        return new StreamCodec<B, Either<L, R>>(){

            @Override
            public Either<L, R> decode(B b) {
                if (b.readBoolean()) {
                    return Either.left(streamCodec.decode(b));
                }
                return Either.right(streamCodec2.decode(b));
            }

            @Override
            public void encode(B b, Either<L, R> either) {
                either.ifLeft(object -> {
                    b.writeBoolean(true);
                    streamCodec.encode(b, object);
                }).ifRight(object -> {
                    b.writeBoolean(false);
                    streamCodec2.encode(b, object);
                });
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Either)((Either)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> intFunction, final ToIntFunction<T> toIntFunction) {
        return new StreamCodec<ByteBuf, T>(){

            @Override
            public T decode(ByteBuf byteBuf) {
                int n = VarInt.read(byteBuf);
                return intFunction.apply(n);
            }

            @Override
            public void encode(ByteBuf byteBuf, T t) {
                int n = toIntFunction.applyAsInt(t);
                VarInt.write(byteBuf, n);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> idMap) {
        return ByteBufCodecs.idMapper(idMap::byIdOrThrow, idMap::getIdOrThrow);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(final ResourceKey<? extends Registry<T>> resourceKey, final Function<Registry<T>, IdMap<R>> function) {
        return new StreamCodec<RegistryFriendlyByteBuf, R>(){

            private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                return (IdMap)function.apply(registryFriendlyByteBuf.registryAccess().registryOrThrow(resourceKey));
            }

            @Override
            public R decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                int n = VarInt.read(registryFriendlyByteBuf);
                return this.getRegistryOrThrow(registryFriendlyByteBuf).byIdOrThrow(n);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, R r) {
                int n = this.getRegistryOrThrow(registryFriendlyByteBuf).getIdOrThrow(r);
                VarInt.write(registryFriendlyByteBuf, n);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> resourceKey) {
        return ByteBufCodecs.registry(resourceKey, registry -> registry);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> resourceKey) {
        return ByteBufCodecs.registry(resourceKey, Registry::asHolderIdMap);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(final ResourceKey<? extends Registry<T>> resourceKey, final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>(){
            private static final int DIRECT_HOLDER_ID = 0;

            private IdMap<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                return registryFriendlyByteBuf.registryAccess().registryOrThrow(resourceKey).asHolderIdMap();
            }

            @Override
            public Holder<T> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                int n = VarInt.read(registryFriendlyByteBuf);
                if (n == 0) {
                    return Holder.direct(streamCodec.decode(registryFriendlyByteBuf));
                }
                return this.getRegistryOrThrow(registryFriendlyByteBuf).byIdOrThrow(n - 1);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, Holder<T> holder) {
                switch (holder.kind()) {
                    case REFERENCE: {
                        int n = this.getRegistryOrThrow(registryFriendlyByteBuf).getIdOrThrow(holder);
                        VarInt.write(registryFriendlyByteBuf, n + 1);
                        break;
                    }
                    case DIRECT: {
                        VarInt.write(registryFriendlyByteBuf, 0);
                        streamCodec.encode(registryFriendlyByteBuf, holder.value());
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (Holder)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> resourceKey) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>(){
            private static final int NAMED_SET = -1;
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec;
            {
                this.holderCodec = ByteBufCodecs.holderRegistry(resourceKey);
            }

            @Override
            public HolderSet<T> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                int n = VarInt.read(registryFriendlyByteBuf) - 1;
                if (n == -1) {
                    Registry registry = registryFriendlyByteBuf.registryAccess().registryOrThrow(resourceKey);
                    return registry.getTag(TagKey.create(resourceKey, (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf))).orElseThrow();
                }
                ArrayList<Holder> arrayList = new ArrayList<Holder>(Math.min(n, 65536));
                for (int i = 0; i < n; ++i) {
                    arrayList.add((Holder)this.holderCodec.decode(registryFriendlyByteBuf));
                }
                return HolderSet.direct(arrayList);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, HolderSet<T> holderSet) {
                Optional optional = holderSet.unwrapKey();
                if (optional.isPresent()) {
                    VarInt.write(registryFriendlyByteBuf, 0);
                    ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, optional.get().location());
                } else {
                    VarInt.write(registryFriendlyByteBuf, holderSet.size() + 1);
                    for (Holder holder : holderSet) {
                        this.holderCodec.encode(registryFriendlyByteBuf, holder);
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (HolderSet)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }
}

