/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundResetScorePacket(String owner, @Nullable String objectiveName) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundResetScorePacket> STREAM_CODEC = Packet.codec(ClientboundResetScorePacket::write, ClientboundResetScorePacket::new);

    private ClientboundResetScorePacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf(), friendlyByteBuf.readNullable(FriendlyByteBuf::readUtf));
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.owner);
        friendlyByteBuf.writeNullable(this.objectiveName, FriendlyByteBuf::writeUtf);
    }

    @Override
    public PacketType<ClientboundResetScorePacket> type() {
        return GamePacketTypes.CLIENTBOUND_RESET_SCORE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleResetScore(this);
    }
}

