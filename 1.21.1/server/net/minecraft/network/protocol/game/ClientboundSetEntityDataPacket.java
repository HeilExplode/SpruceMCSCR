/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.syncher.SynchedEntityData;

public record ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetEntityDataPacket> STREAM_CODEC = Packet.codec(ClientboundSetEntityDataPacket::write, ClientboundSetEntityDataPacket::new);
    public static final int EOF_MARKER = 255;

    private ClientboundSetEntityDataPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(registryFriendlyByteBuf.readVarInt(), ClientboundSetEntityDataPacket.unpack(registryFriendlyByteBuf));
    }

    private static void pack(List<SynchedEntityData.DataValue<?>> list, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        for (SynchedEntityData.DataValue<?> dataValue : list) {
            dataValue.write(registryFriendlyByteBuf);
        }
        registryFriendlyByteBuf.writeByte(255);
    }

    private static List<SynchedEntityData.DataValue<?>> unpack(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        short s;
        ArrayList arrayList = new ArrayList();
        while ((s = registryFriendlyByteBuf.readUnsignedByte()) != 255) {
            arrayList.add(SynchedEntityData.DataValue.read(registryFriendlyByteBuf, s));
        }
        return arrayList;
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeVarInt(this.id);
        ClientboundSetEntityDataPacket.pack(this.packedItems, registryFriendlyByteBuf);
    }

    @Override
    public PacketType<ClientboundSetEntityDataPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_ENTITY_DATA;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetEntityData(this);
    }
}

