/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.authlib.GameProfile
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.util.UndashedUuid
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.util.UndashedUuid;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class UUIDUtil {
    public static final Codec<UUID> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 4).map(UUIDUtil::uuidFromIntArray), uUID -> Arrays.stream(UUIDUtil.uuidToIntArray(uUID)));
    public static final Codec<Set<UUID>> CODEC_SET = Codec.list(CODEC).xmap(Sets::newHashSet, Lists::newArrayList);
    public static final Codec<Set<UUID>> CODEC_LINKED_SET = Codec.list(CODEC).xmap(Sets::newLinkedHashSet, Lists::newArrayList);
    public static final Codec<UUID> STRING_CODEC = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)UUID.fromString(string), (Lifecycle)Lifecycle.stable());
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error(() -> "Invalid UUID " + string + ": " + illegalArgumentException.getMessage());
        }
    }, UUID::toString);
    public static final Codec<UUID> AUTHLIB_CODEC = Codec.withAlternative((Codec)Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)UndashedUuid.fromStringLenient((String)string), (Lifecycle)Lifecycle.stable());
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error(() -> "Invalid UUID " + string + ": " + illegalArgumentException.getMessage());
        }
    }, UndashedUuid::toString), CODEC);
    public static final Codec<UUID> LENIENT_CODEC = Codec.withAlternative(CODEC, STRING_CODEC);
    public static final StreamCodec<ByteBuf, UUID> STREAM_CODEC = new StreamCodec<ByteBuf, UUID>(){

        @Override
        public UUID decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readUUID(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, UUID uUID) {
            FriendlyByteBuf.writeUUID(byteBuf, uUID);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (UUID)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final int UUID_BYTES = 16;
    private static final String UUID_PREFIX_OFFLINE_PLAYER = "OfflinePlayer:";

    private UUIDUtil() {
    }

    public static UUID uuidFromIntArray(int[] nArray) {
        return new UUID((long)nArray[0] << 32 | (long)nArray[1] & 0xFFFFFFFFL, (long)nArray[2] << 32 | (long)nArray[3] & 0xFFFFFFFFL);
    }

    public static int[] uuidToIntArray(UUID uUID) {
        long l = uUID.getMostSignificantBits();
        long l2 = uUID.getLeastSignificantBits();
        return UUIDUtil.leastMostToIntArray(l, l2);
    }

    private static int[] leastMostToIntArray(long l, long l2) {
        return new int[]{(int)(l >> 32), (int)l, (int)(l2 >> 32), (int)l2};
    }

    public static byte[] uuidToByteArray(UUID uUID) {
        byte[] byArray = new byte[16];
        ByteBuffer.wrap(byArray).order(ByteOrder.BIG_ENDIAN).putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits());
        return byArray;
    }

    public static UUID readUUID(Dynamic<?> dynamic) {
        int[] nArray = dynamic.asIntStream().toArray();
        if (nArray.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + nArray.length + ".");
        }
        return UUIDUtil.uuidFromIntArray(nArray);
    }

    public static UUID createOfflinePlayerUUID(String string) {
        return UUID.nameUUIDFromBytes((UUID_PREFIX_OFFLINE_PLAYER + string).getBytes(StandardCharsets.UTF_8));
    }

    public static GameProfile createOfflineProfile(String string) {
        UUID uUID = UUIDUtil.createOfflinePlayerUUID(string);
        return new GameProfile(uUID, string);
    }
}

