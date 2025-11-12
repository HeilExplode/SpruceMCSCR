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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientboundSetEntityMotionPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetEntityMotionPacket> STREAM_CODEC = Packet.codec(ClientboundSetEntityMotionPacket::write, ClientboundSetEntityMotionPacket::new);
    private final int id;
    private final int xa;
    private final int ya;
    private final int za;

    public ClientboundSetEntityMotionPacket(Entity entity) {
        this(entity.getId(), entity.getDeltaMovement());
    }

    public ClientboundSetEntityMotionPacket(int n, Vec3 vec3) {
        this.id = n;
        double d = 3.9;
        double d2 = Mth.clamp(vec3.x, -3.9, 3.9);
        double d3 = Mth.clamp(vec3.y, -3.9, 3.9);
        double d4 = Mth.clamp(vec3.z, -3.9, 3.9);
        this.xa = (int)(d2 * 8000.0);
        this.ya = (int)(d3 * 8000.0);
        this.za = (int)(d4 * 8000.0);
    }

    private ClientboundSetEntityMotionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readVarInt();
        this.xa = friendlyByteBuf.readShort();
        this.ya = friendlyByteBuf.readShort();
        this.za = friendlyByteBuf.readShort();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeShort(this.xa);
        friendlyByteBuf.writeShort(this.ya);
        friendlyByteBuf.writeShort(this.za);
    }

    @Override
    public PacketType<ClientboundSetEntityMotionPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_ENTITY_MOTION;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetEntityMotion(this);
    }

    public int getId() {
        return this.id;
    }

    public double getXa() {
        return (double)this.xa / 8000.0;
    }

    public double getYa() {
        return (double)this.ya / 8000.0;
    }

    public double getZa() {
        return (double)this.za / 8000.0;
    }
}

