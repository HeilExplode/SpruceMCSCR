/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.level.block.Block;

public class ClientboundBlockEventPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockEventPacket> STREAM_CODEC = Packet.codec(ClientboundBlockEventPacket::write, ClientboundBlockEventPacket::new);
    private final BlockPos pos;
    private final int b0;
    private final int b1;
    private final Block block;

    public ClientboundBlockEventPacket(BlockPos blockPos, Block block, int n, int n2) {
        this.pos = blockPos;
        this.block = block;
        this.b0 = n;
        this.b1 = n2;
    }

    private ClientboundBlockEventPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.pos = registryFriendlyByteBuf.readBlockPos();
        this.b0 = registryFriendlyByteBuf.readUnsignedByte();
        this.b1 = registryFriendlyByteBuf.readUnsignedByte();
        this.block = (Block)ByteBufCodecs.registry(Registries.BLOCK).decode(registryFriendlyByteBuf);
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeBlockPos(this.pos);
        registryFriendlyByteBuf.writeByte(this.b0);
        registryFriendlyByteBuf.writeByte(this.b1);
        ByteBufCodecs.registry(Registries.BLOCK).encode(registryFriendlyByteBuf, this.block);
    }

    @Override
    public PacketType<ClientboundBlockEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_EVENT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBlockEvent(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getB0() {
        return this.b0;
    }

    public int getB1() {
        return this.b1;
    }

    public Block getBlock() {
        return this.block;
    }
}

