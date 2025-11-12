/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundHorseScreenOpenPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundHorseScreenOpenPacket> STREAM_CODEC = Packet.codec(ClientboundHorseScreenOpenPacket::write, ClientboundHorseScreenOpenPacket::new);
    private final int containerId;
    private final int inventoryColumns;
    private final int entityId;

    public ClientboundHorseScreenOpenPacket(int n, int n2, int n3) {
        this.containerId = n;
        this.inventoryColumns = n2;
        this.entityId = n3;
    }

    private ClientboundHorseScreenOpenPacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readUnsignedByte();
        this.inventoryColumns = friendlyByteBuf.readVarInt();
        this.entityId = friendlyByteBuf.readInt();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeVarInt(this.inventoryColumns);
        friendlyByteBuf.writeInt(this.entityId);
    }

    @Override
    public PacketType<ClientboundHorseScreenOpenPacket> type() {
        return GamePacketTypes.CLIENTBOUND_HORSE_SCREEN_OPEN;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleHorseScreenOpen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getInventoryColumns() {
        return this.inventoryColumns;
    }

    public int getEntityId() {
        return this.entityId;
    }
}

