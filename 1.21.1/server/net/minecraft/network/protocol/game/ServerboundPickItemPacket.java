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

public class ServerboundPickItemPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundPickItemPacket> STREAM_CODEC = Packet.codec(ServerboundPickItemPacket::write, ServerboundPickItemPacket::new);
    private final int slot;

    public ServerboundPickItemPacket(int n) {
        this.slot = n;
    }

    private ServerboundPickItemPacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readVarInt();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.slot);
    }

    @Override
    public PacketType<ServerboundPickItemPacket> type() {
        return GamePacketTypes.SERVERBOUND_PICK_ITEM;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePickItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}

