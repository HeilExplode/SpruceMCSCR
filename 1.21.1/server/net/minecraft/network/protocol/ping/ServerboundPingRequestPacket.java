/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.ping;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.ping.ServerPingPacketListener;

public class ServerboundPingRequestPacket
implements Packet<ServerPingPacketListener> {
    public static final StreamCodec<ByteBuf, ServerboundPingRequestPacket> STREAM_CODEC = Packet.codec(ServerboundPingRequestPacket::write, ServerboundPingRequestPacket::new);
    private final long time;

    public ServerboundPingRequestPacket(long l) {
        this.time = l;
    }

    private ServerboundPingRequestPacket(ByteBuf byteBuf) {
        this.time = byteBuf.readLong();
    }

    private void write(ByteBuf byteBuf) {
        byteBuf.writeLong(this.time);
    }

    @Override
    public PacketType<ServerboundPingRequestPacket> type() {
        return PingPacketTypes.SERVERBOUND_PING_REQUEST;
    }

    @Override
    public void handle(ServerPingPacketListener serverPingPacketListener) {
        serverPingPacketListener.handlePingRequest(this);
    }

    public long getTime() {
        return this.time;
    }
}

