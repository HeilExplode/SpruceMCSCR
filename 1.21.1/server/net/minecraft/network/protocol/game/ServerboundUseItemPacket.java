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
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundUseItemPacket> STREAM_CODEC = Packet.codec(ServerboundUseItemPacket::write, ServerboundUseItemPacket::new);
    private final InteractionHand hand;
    private final int sequence;
    private final float yRot;
    private final float xRot;

    public ServerboundUseItemPacket(InteractionHand interactionHand, int n, float f, float f2) {
        this.hand = interactionHand;
        this.sequence = n;
        this.yRot = f;
        this.xRot = f2;
    }

    private ServerboundUseItemPacket(FriendlyByteBuf friendlyByteBuf) {
        this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
        this.sequence = friendlyByteBuf.readVarInt();
        this.yRot = friendlyByteBuf.readFloat();
        this.xRot = friendlyByteBuf.readFloat();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.hand);
        friendlyByteBuf.writeVarInt(this.sequence);
        friendlyByteBuf.writeFloat(this.yRot);
        friendlyByteBuf.writeFloat(this.xRot);
    }

    @Override
    public PacketType<ServerboundUseItemPacket> type() {
        return GamePacketTypes.SERVERBOUND_USE_ITEM;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleUseItem(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public int getSequence() {
        return this.sequence;
    }

    public float getYRot() {
        return this.yRot;
    }

    public float getXRot() {
        return this.xRot;
    }
}

