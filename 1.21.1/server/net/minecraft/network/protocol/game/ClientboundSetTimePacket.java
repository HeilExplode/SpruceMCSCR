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

public class ClientboundSetTimePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetTimePacket> STREAM_CODEC = Packet.codec(ClientboundSetTimePacket::write, ClientboundSetTimePacket::new);
    private final long gameTime;
    private final long dayTime;

    public ClientboundSetTimePacket(long l, long l2, boolean bl) {
        this.gameTime = l;
        long l3 = l2;
        if (!bl && (l3 = -l3) == 0L) {
            l3 = -1L;
        }
        this.dayTime = l3;
    }

    private ClientboundSetTimePacket(FriendlyByteBuf friendlyByteBuf) {
        this.gameTime = friendlyByteBuf.readLong();
        this.dayTime = friendlyByteBuf.readLong();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.gameTime);
        friendlyByteBuf.writeLong(this.dayTime);
    }

    @Override
    public PacketType<ClientboundSetTimePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_TIME;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetTime(this);
    }

    public long getGameTime() {
        return this.gameTime;
    }

    public long getDayTime() {
        return this.dayTime;
    }
}

