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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public abstract class ClientboundMoveEntityPacket
implements Packet<ClientGamePacketListener> {
    protected final int entityId;
    protected final short xa;
    protected final short ya;
    protected final short za;
    protected final byte yRot;
    protected final byte xRot;
    protected final boolean onGround;
    protected final boolean hasRot;
    protected final boolean hasPos;

    protected ClientboundMoveEntityPacket(int n, short s, short s2, short s3, byte by, byte by2, boolean bl, boolean bl2, boolean bl3) {
        this.entityId = n;
        this.xa = s;
        this.ya = s2;
        this.za = s3;
        this.yRot = by;
        this.xRot = by2;
        this.onGround = bl;
        this.hasRot = bl2;
        this.hasPos = bl3;
    }

    @Override
    public abstract PacketType<? extends ClientboundMoveEntityPacket> type();

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleMoveEntity(this);
    }

    public String toString() {
        return "Entity_" + super.toString();
    }

    @Nullable
    public Entity getEntity(Level level) {
        return level.getEntity(this.entityId);
    }

    public short getXa() {
        return this.xa;
    }

    public short getYa() {
        return this.ya;
    }

    public short getZa() {
        return this.za;
    }

    public byte getyRot() {
        return this.yRot;
    }

    public byte getxRot() {
        return this.xRot;
    }

    public boolean hasRotation() {
        return this.hasRot;
    }

    public boolean hasPosition() {
        return this.hasPos;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public static class Rot
    extends ClientboundMoveEntityPacket {
        public static final StreamCodec<FriendlyByteBuf, Rot> STREAM_CODEC = Packet.codec(Rot::write, Rot::read);

        public Rot(int n, byte by, byte by2, boolean bl) {
            super(n, (short)0, (short)0, (short)0, by, by2, bl, true, false);
        }

        private static Rot read(FriendlyByteBuf friendlyByteBuf) {
            int n = friendlyByteBuf.readVarInt();
            byte by = friendlyByteBuf.readByte();
            byte by2 = friendlyByteBuf.readByte();
            boolean bl = friendlyByteBuf.readBoolean();
            return new Rot(n, by, by2, bl);
        }

        private void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeVarInt(this.entityId);
            friendlyByteBuf.writeByte(this.yRot);
            friendlyByteBuf.writeByte(this.xRot);
            friendlyByteBuf.writeBoolean(this.onGround);
        }

        @Override
        public PacketType<Rot> type() {
            return GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_ROT;
        }
    }

    public static class Pos
    extends ClientboundMoveEntityPacket {
        public static final StreamCodec<FriendlyByteBuf, Pos> STREAM_CODEC = Packet.codec(Pos::write, Pos::read);

        public Pos(int n, short s, short s2, short s3, boolean bl) {
            super(n, s, s2, s3, (byte)0, (byte)0, bl, false, true);
        }

        private static Pos read(FriendlyByteBuf friendlyByteBuf) {
            int n = friendlyByteBuf.readVarInt();
            short s = friendlyByteBuf.readShort();
            short s2 = friendlyByteBuf.readShort();
            short s3 = friendlyByteBuf.readShort();
            boolean bl = friendlyByteBuf.readBoolean();
            return new Pos(n, s, s2, s3, bl);
        }

        private void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeVarInt(this.entityId);
            friendlyByteBuf.writeShort(this.xa);
            friendlyByteBuf.writeShort(this.ya);
            friendlyByteBuf.writeShort(this.za);
            friendlyByteBuf.writeBoolean(this.onGround);
        }

        @Override
        public PacketType<Pos> type() {
            return GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS;
        }
    }

    public static class PosRot
    extends ClientboundMoveEntityPacket {
        public static final StreamCodec<FriendlyByteBuf, PosRot> STREAM_CODEC = Packet.codec(PosRot::write, PosRot::read);

        public PosRot(int n, short s, short s2, short s3, byte by, byte by2, boolean bl) {
            super(n, s, s2, s3, by, by2, bl, true, true);
        }

        private static PosRot read(FriendlyByteBuf friendlyByteBuf) {
            int n = friendlyByteBuf.readVarInt();
            short s = friendlyByteBuf.readShort();
            short s2 = friendlyByteBuf.readShort();
            short s3 = friendlyByteBuf.readShort();
            byte by = friendlyByteBuf.readByte();
            byte by2 = friendlyByteBuf.readByte();
            boolean bl = friendlyByteBuf.readBoolean();
            return new PosRot(n, s, s2, s3, by, by2, bl);
        }

        private void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeVarInt(this.entityId);
            friendlyByteBuf.writeShort(this.xa);
            friendlyByteBuf.writeShort(this.ya);
            friendlyByteBuf.writeShort(this.za);
            friendlyByteBuf.writeByte(this.yRot);
            friendlyByteBuf.writeByte(this.xRot);
            friendlyByteBuf.writeBoolean(this.onGround);
        }

        @Override
        public PacketType<PosRot> type() {
            return GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS_ROT;
        }
    }
}

