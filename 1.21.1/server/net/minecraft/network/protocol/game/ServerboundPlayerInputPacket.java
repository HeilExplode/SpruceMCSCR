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

public class ServerboundPlayerInputPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundPlayerInputPacket> STREAM_CODEC = Packet.codec(ServerboundPlayerInputPacket::write, ServerboundPlayerInputPacket::new);
    private static final int FLAG_JUMPING = 1;
    private static final int FLAG_SHIFT_KEY_DOWN = 2;
    private final float xxa;
    private final float zza;
    private final boolean isJumping;
    private final boolean isShiftKeyDown;

    public ServerboundPlayerInputPacket(float f, float f2, boolean bl, boolean bl2) {
        this.xxa = f;
        this.zza = f2;
        this.isJumping = bl;
        this.isShiftKeyDown = bl2;
    }

    private ServerboundPlayerInputPacket(FriendlyByteBuf friendlyByteBuf) {
        this.xxa = friendlyByteBuf.readFloat();
        this.zza = friendlyByteBuf.readFloat();
        byte by = friendlyByteBuf.readByte();
        this.isJumping = (by & 1) > 0;
        this.isShiftKeyDown = (by & 2) > 0;
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(this.xxa);
        friendlyByteBuf.writeFloat(this.zza);
        byte by = 0;
        if (this.isJumping) {
            by = (byte)(by | 1);
        }
        if (this.isShiftKeyDown) {
            by = (byte)(by | 2);
        }
        friendlyByteBuf.writeByte(by);
    }

    @Override
    public PacketType<ServerboundPlayerInputPacket> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_INPUT;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePlayerInput(this);
    }

    public float getXxa() {
        return this.xxa;
    }

    public float getZza() {
        return this.zza;
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    public boolean isShiftKeyDown() {
        return this.isShiftKeyDown;
    }
}

