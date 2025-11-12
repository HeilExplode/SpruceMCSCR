/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundBlockDestructionPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundBlockDestructionPacket> STREAM_CODEC = Packet.codec(ClientboundBlockDestructionPacket::write, ClientboundBlockDestructionPacket::new);
    private final int id;
    private final BlockPos pos;
    private final int progress;

    public ClientboundBlockDestructionPacket(int n, BlockPos blockPos, int n2) {
        this.id = n;
        this.pos = blockPos;
        this.progress = n2;
    }

    private ClientboundBlockDestructionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readVarInt();
        this.pos = friendlyByteBuf.readBlockPos();
        this.progress = friendlyByteBuf.readUnsignedByte();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeByte(this.progress);
    }

    @Override
    public PacketType<ClientboundBlockDestructionPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_DESTRUCTION;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBlockDestruction(this);
    }

    public int getId() {
        return this.id;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getProgress() {
        return this.progress;
    }
}

