/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundCustomQueryPacket(int transactionId, CustomQueryPayload payload) implements Packet<ClientLoginPacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomQueryPacket> STREAM_CODEC = Packet.codec(ClientboundCustomQueryPacket::write, ClientboundCustomQueryPacket::new);
    private static final int MAX_PAYLOAD_SIZE = 0x100000;

    private ClientboundCustomQueryPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readVarInt(), ClientboundCustomQueryPacket.readPayload(friendlyByteBuf.readResourceLocation(), friendlyByteBuf));
    }

    private static CustomQueryPayload readPayload(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        return ClientboundCustomQueryPacket.readUnknownPayload(resourceLocation, friendlyByteBuf);
    }

    private static DiscardedQueryPayload readUnknownPayload(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        int n = friendlyByteBuf.readableBytes();
        if (n < 0 || n > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        friendlyByteBuf.skipBytes(n);
        return new DiscardedQueryPayload(resourceLocation);
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.transactionId);
        friendlyByteBuf.writeResourceLocation(this.payload.id());
        this.payload.write(friendlyByteBuf);
    }

    @Override
    public PacketType<ClientboundCustomQueryPacket> type() {
        return LoginPacketTypes.CLIENTBOUND_CUSTOM_QUERY;
    }

    @Override
    public void handle(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.handleCustomQuery(this);
    }
}

