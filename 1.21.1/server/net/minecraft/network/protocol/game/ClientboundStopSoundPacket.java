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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class ClientboundStopSoundPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundStopSoundPacket> STREAM_CODEC = Packet.codec(ClientboundStopSoundPacket::write, ClientboundStopSoundPacket::new);
    private static final int HAS_SOURCE = 1;
    private static final int HAS_SOUND = 2;
    @Nullable
    private final ResourceLocation name;
    @Nullable
    private final SoundSource source;

    public ClientboundStopSoundPacket(@Nullable ResourceLocation resourceLocation, @Nullable SoundSource soundSource) {
        this.name = resourceLocation;
        this.source = soundSource;
    }

    private ClientboundStopSoundPacket(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        this.source = (by & 1) > 0 ? friendlyByteBuf.readEnum(SoundSource.class) : null;
        this.name = (by & 2) > 0 ? friendlyByteBuf.readResourceLocation() : null;
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        if (this.source != null) {
            if (this.name != null) {
                friendlyByteBuf.writeByte(3);
                friendlyByteBuf.writeEnum(this.source);
                friendlyByteBuf.writeResourceLocation(this.name);
            } else {
                friendlyByteBuf.writeByte(1);
                friendlyByteBuf.writeEnum(this.source);
            }
        } else if (this.name != null) {
            friendlyByteBuf.writeByte(2);
            friendlyByteBuf.writeResourceLocation(this.name);
        } else {
            friendlyByteBuf.writeByte(0);
        }
    }

    @Override
    public PacketType<ClientboundStopSoundPacket> type() {
        return GamePacketTypes.CLIENTBOUND_STOP_SOUND;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleStopSoundEvent(this);
    }

    @Nullable
    public ResourceLocation getName() {
        return this.name;
    }

    @Nullable
    public SoundSource getSource() {
        return this.source;
    }
}

