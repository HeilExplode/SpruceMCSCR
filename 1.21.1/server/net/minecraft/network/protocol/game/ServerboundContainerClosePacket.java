/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundContainerClosePacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundContainerClosePacket> STREAM_CODEC = Packet.codec(ServerboundContainerClosePacket::write, ServerboundContainerClosePacket::new);
    private final int containerId;

    public ServerboundContainerClosePacket(int n) {
        this.containerId = n;
    }

    private ServerboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readByte();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
    }

    @Override
    public PacketType<ServerboundContainerClosePacket> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_CLOSE;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleContainerClose(this);
    }

    public int getContainerId() {
        return this.containerId;
    }
}

