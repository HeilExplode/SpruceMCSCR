/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ClientboundSoundPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSoundPacket> STREAM_CODEC = Packet.codec(ClientboundSoundPacket::write, ClientboundSoundPacket::new);
    public static final float LOCATION_ACCURACY = 8.0f;
    private final Holder<SoundEvent> sound;
    private final SoundSource source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundPacket(Holder<SoundEvent> holder, SoundSource soundSource, double d, double d2, double d3, float f, float f2, long l) {
        this.sound = holder;
        this.source = soundSource;
        this.x = (int)(d * 8.0);
        this.y = (int)(d2 * 8.0);
        this.z = (int)(d3 * 8.0);
        this.volume = f;
        this.pitch = f2;
        this.seed = l;
    }

    private ClientboundSoundPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.sound = (Holder)SoundEvent.STREAM_CODEC.decode(registryFriendlyByteBuf);
        this.source = registryFriendlyByteBuf.readEnum(SoundSource.class);
        this.x = registryFriendlyByteBuf.readInt();
        this.y = registryFriendlyByteBuf.readInt();
        this.z = registryFriendlyByteBuf.readInt();
        this.volume = registryFriendlyByteBuf.readFloat();
        this.pitch = registryFriendlyByteBuf.readFloat();
        this.seed = registryFriendlyByteBuf.readLong();
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        SoundEvent.STREAM_CODEC.encode(registryFriendlyByteBuf, this.sound);
        registryFriendlyByteBuf.writeEnum(this.source);
        registryFriendlyByteBuf.writeInt(this.x);
        registryFriendlyByteBuf.writeInt(this.y);
        registryFriendlyByteBuf.writeInt(this.z);
        registryFriendlyByteBuf.writeFloat(this.volume);
        registryFriendlyByteBuf.writeFloat(this.pitch);
        registryFriendlyByteBuf.writeLong(this.seed);
    }

    @Override
    public PacketType<ClientboundSoundPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SOUND;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSoundEvent(this);
    }

    public Holder<SoundEvent> getSound() {
        return this.sound;
    }

    public SoundSource getSource() {
        return this.source;
    }

    public double getX() {
        return (float)this.x / 8.0f;
    }

    public double getY() {
        return (float)this.y / 8.0f;
    }

    public double getZ() {
        return (float)this.z / 8.0f;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public long getSeed() {
        return this.seed;
    }
}

