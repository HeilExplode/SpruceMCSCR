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
import net.minecraft.world.entity.player.Abilities;

public class ServerboundPlayerAbilitiesPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundPlayerAbilitiesPacket> STREAM_CODEC = Packet.codec(ServerboundPlayerAbilitiesPacket::write, ServerboundPlayerAbilitiesPacket::new);
    private static final int FLAG_FLYING = 2;
    private final boolean isFlying;

    public ServerboundPlayerAbilitiesPacket(Abilities abilities) {
        this.isFlying = abilities.flying;
    }

    private ServerboundPlayerAbilitiesPacket(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        this.isFlying = (by & 2) != 0;
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        int n = 0;
        if (this.isFlying) {
            n = (byte)(n | 2);
        }
        friendlyByteBuf.writeByte(n);
    }

    @Override
    public PacketType<ServerboundPlayerAbilitiesPacket> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_ABILITIES;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePlayerAbilities(this);
    }

    public boolean isFlying() {
        return this.isFlying;
    }
}

