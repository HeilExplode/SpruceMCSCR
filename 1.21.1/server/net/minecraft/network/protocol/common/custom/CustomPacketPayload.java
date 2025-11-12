/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacketPayload {
    public Type<? extends CustomPacketPayload> type();

    public static <B extends ByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> streamMemberEncoder, StreamDecoder<B, T> streamDecoder) {
        return StreamCodec.ofMember(streamMemberEncoder, streamDecoder);
    }

    public static <T extends CustomPacketPayload> Type<T> createType(String string) {
        return new Type(ResourceLocation.withDefaultNamespace(string));
    }

    public static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> codec(final FallbackProvider<B> fallbackProvider, List<TypeAndCodec<? super B, ?>> list) {
        final Map<ResourceLocation, StreamCodec> map = list.stream().collect(Collectors.toUnmodifiableMap(typeAndCodec -> typeAndCodec.type().id(), TypeAndCodec::codec));
        return new StreamCodec<B, CustomPacketPayload>(){

            private StreamCodec<? super B, ? extends CustomPacketPayload> findCodec(ResourceLocation resourceLocation) {
                StreamCodec streamCodec = (StreamCodec)map.get(resourceLocation);
                if (streamCodec != null) {
                    return streamCodec;
                }
                return fallbackProvider.create(resourceLocation);
            }

            private <T extends CustomPacketPayload> void writeCap(B b, Type<T> type, CustomPacketPayload customPacketPayload) {
                ((FriendlyByteBuf)((Object)b)).writeResourceLocation(type.id());
                StreamCodec streamCodec = this.findCodec(type.id);
                streamCodec.encode(b, customPacketPayload);
            }

            @Override
            public void encode(B b, CustomPacketPayload customPacketPayload) {
                this.writeCap(b, customPacketPayload.type(), customPacketPayload);
            }

            @Override
            public CustomPacketPayload decode(B b) {
                ResourceLocation resourceLocation = ((FriendlyByteBuf)((Object)b)).readResourceLocation();
                return (CustomPacketPayload)this.findCodec(resourceLocation).decode(b);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((Object)((FriendlyByteBuf)((Object)object))), (CustomPacketPayload)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((Object)((FriendlyByteBuf)((Object)object))));
            }
        };
    }

    public static final class Type<T extends CustomPacketPayload>
    extends Record {
        final ResourceLocation id;

        public Type(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Type.class, "id", "id"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Type.class, "id", "id"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Type.class, "id", "id"}, this, object);
        }

        public ResourceLocation id() {
            return this.id;
        }
    }

    public static interface FallbackProvider<B extends FriendlyByteBuf> {
        public StreamCodec<B, ? extends CustomPacketPayload> create(ResourceLocation var1);
    }

    public record TypeAndCodec<B extends FriendlyByteBuf, T extends CustomPacketPayload>(Type<T> type, StreamCodec<B, T> codec) {
    }
}

