/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.ProtocolCodecBuilder;

public class ProtocolInfoBuilder<T extends PacketListener, B extends ByteBuf> {
    final ConnectionProtocol protocol;
    final PacketFlow flow;
    private final List<CodecEntry<T, ?, B>> codecs = new ArrayList();
    @Nullable
    private BundlerInfo bundlerInfo;

    public ProtocolInfoBuilder(ConnectionProtocol connectionProtocol, PacketFlow packetFlow) {
        this.protocol = connectionProtocol;
        this.flow = packetFlow;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B> addPacket(PacketType<P> packetType, StreamCodec<? super B, P> streamCodec) {
        this.codecs.add(new CodecEntry(packetType, streamCodec));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B> withBundlePacket(PacketType<P> packetType, Function<Iterable<Packet<? super T>>, P> function, D d) {
        StreamCodec streamCodec = StreamCodec.unit(d);
        PacketType<BundleDelimiterPacket<? super T>> packetType2 = d.type();
        this.codecs.add(new CodecEntry(packetType2, streamCodec));
        this.bundlerInfo = BundlerInfo.createForPacket(packetType, function, d);
        return this;
    }

    StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> function, List<CodecEntry<T, ?, B>> list) {
        ProtocolCodecBuilder protocolCodecBuilder = new ProtocolCodecBuilder(this.flow);
        for (CodecEntry codecEntry : list) {
            codecEntry.addToBuilder(protocolCodecBuilder, function);
        }
        return protocolCodecBuilder.build();
    }

    public ProtocolInfo<T> build(Function<ByteBuf, B> function) {
        return new Implementation(this.protocol, this.flow, this.buildPacketCodec(function, this.codecs), this.bundlerInfo);
    }

    public ProtocolInfo.Unbound<T, B> buildUnbound() {
        final List<CodecEntry<T, ?, B>> list = List.copyOf(this.codecs);
        final BundlerInfo bundlerInfo = this.bundlerInfo;
        return new ProtocolInfo.Unbound<T, B>(){

            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> function) {
                return new Implementation(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec(function, list), bundlerInfo);
            }

            @Override
            public ConnectionProtocol id() {
                return ProtocolInfoBuilder.this.protocol;
            }

            @Override
            public PacketFlow flow() {
                return ProtocolInfoBuilder.this.flow;
            }

            @Override
            public void listPackets(ProtocolInfo.Unbound.PacketVisitor packetVisitor) {
                for (int i = 0; i < list.size(); ++i) {
                    CodecEntry codecEntry = (CodecEntry)list.get(i);
                    packetVisitor.accept(codecEntry.type, i);
                }
            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> ProtocolInfo.Unbound<L, B> protocol(ConnectionProtocol connectionProtocol, PacketFlow packetFlow, Consumer<ProtocolInfoBuilder<L, B>> consumer) {
        ProtocolInfoBuilder protocolInfoBuilder = new ProtocolInfoBuilder(connectionProtocol, packetFlow);
        consumer.accept(protocolInfoBuilder);
        return protocolInfoBuilder.buildUnbound();
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf> ProtocolInfo.Unbound<T, B> serverboundProtocol(ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B>> consumer) {
        return ProtocolInfoBuilder.protocol(connectionProtocol, PacketFlow.SERVERBOUND, consumer);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf> ProtocolInfo.Unbound<T, B> clientboundProtocol(ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B>> consumer) {
        return ProtocolInfoBuilder.protocol(connectionProtocol, PacketFlow.CLIENTBOUND, consumer);
    }

    static final class CodecEntry<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf>
    extends Record {
        final PacketType<P> type;
        private final StreamCodec<? super B, P> serializer;

        CodecEntry(PacketType<P> packetType, StreamCodec<? super B, P> streamCodec) {
            this.type = packetType;
            this.serializer = streamCodec;
        }

        public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> protocolCodecBuilder, Function<ByteBuf, B> function) {
            StreamCodec<ByteBuf, P> streamCodec = this.serializer.mapStream(function);
            protocolCodecBuilder.add(this.type, streamCodec);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CodecEntry.class, "type;serializer", "type", "serializer"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CodecEntry.class, "type;serializer", "type", "serializer"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CodecEntry.class, "type;serializer", "type", "serializer"}, this, object);
        }

        public PacketType<P> type() {
            return this.type;
        }

        public StreamCodec<? super B, P> serializer() {
            return this.serializer;
        }
    }

    record Implementation<L extends PacketListener>(ConnectionProtocol id, PacketFlow flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo) implements ProtocolInfo<L>
    {
    }
}

