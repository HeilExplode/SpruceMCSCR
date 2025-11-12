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

public class ClientboundSetTitlesAnimationPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetTitlesAnimationPacket> STREAM_CODEC = Packet.codec(ClientboundSetTitlesAnimationPacket::write, ClientboundSetTitlesAnimationPacket::new);
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public ClientboundSetTitlesAnimationPacket(int n, int n2, int n3) {
        this.fadeIn = n;
        this.stay = n2;
        this.fadeOut = n3;
    }

    private ClientboundSetTitlesAnimationPacket(FriendlyByteBuf friendlyByteBuf) {
        this.fadeIn = friendlyByteBuf.readInt();
        this.stay = friendlyByteBuf.readInt();
        this.fadeOut = friendlyByteBuf.readInt();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.fadeIn);
        friendlyByteBuf.writeInt(this.stay);
        friendlyByteBuf.writeInt(this.fadeOut);
    }

    @Override
    public PacketType<ClientboundSetTitlesAnimationPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_TITLES_ANIMATION;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.setTitlesAnimation(this);
    }

    public int getFadeIn() {
        return this.fadeIn;
    }

    public int getStay() {
        return this.stay;
    }

    public int getFadeOut() {
        return this.fadeOut;
    }
}

