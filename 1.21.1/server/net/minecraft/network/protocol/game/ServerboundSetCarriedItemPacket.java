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

public class ServerboundSetCarriedItemPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetCarriedItemPacket> STREAM_CODEC = Packet.codec(ServerboundSetCarriedItemPacket::write, ServerboundSetCarriedItemPacket::new);
    private final int slot;

    public ServerboundSetCarriedItemPacket(int n) {
        this.slot = n;
    }

    private ServerboundSetCarriedItemPacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readShort();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeShort(this.slot);
    }

    @Override
    public PacketType<ServerboundSetCarriedItemPacket> type() {
        return GamePacketTypes.SERVERBOUND_SET_CARRIED_ITEM;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}

