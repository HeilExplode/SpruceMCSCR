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
import net.minecraft.util.debugchart.RemoteDebugSampleType;

public record ServerboundDebugSampleSubscriptionPacket(RemoteDebugSampleType sampleType) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundDebugSampleSubscriptionPacket> STREAM_CODEC = Packet.codec(ServerboundDebugSampleSubscriptionPacket::write, ServerboundDebugSampleSubscriptionPacket::new);

    private ServerboundDebugSampleSubscriptionPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readEnum(RemoteDebugSampleType.class));
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.sampleType);
    }

    @Override
    public PacketType<ServerboundDebugSampleSubscriptionPacket> type() {
        return GamePacketTypes.SERVERBOUND_DEBUG_SAMPLE_SUBSCRIPTION;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleDebugSampleSubscription(this);
    }
}

