/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundCustomChatCompletionsPacket(Action action, List<String> entries) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomChatCompletionsPacket> STREAM_CODEC = Packet.codec(ClientboundCustomChatCompletionsPacket::write, ClientboundCustomChatCompletionsPacket::new);

    private ClientboundCustomChatCompletionsPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readEnum(Action.class), friendlyByteBuf.readList(FriendlyByteBuf::readUtf));
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.action);
        friendlyByteBuf.writeCollection(this.entries, FriendlyByteBuf::writeUtf);
    }

    @Override
    public PacketType<ClientboundCustomChatCompletionsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CUSTOM_CHAT_COMPLETIONS;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCustomChatCompletions(this);
    }

    public static enum Action {
        ADD,
        REMOVE,
        SET;

    }
}

