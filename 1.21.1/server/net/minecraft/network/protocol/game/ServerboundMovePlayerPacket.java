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

public abstract class ServerboundMovePlayerPacket
implements Packet<ServerGamePacketListener> {
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float yRot;
    protected final float xRot;
    protected final boolean onGround;
    protected final boolean hasPos;
    protected final boolean hasRot;

    protected ServerboundMovePlayerPacket(double d, double d2, double d3, float f, float f2, boolean bl, boolean bl2, boolean bl3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.yRot = f;
        this.xRot = f2;
        this.onGround = bl;
        this.hasPos = bl2;
        this.hasRot = bl3;
    }

    @Override
    public abstract PacketType<? extends ServerboundMovePlayerPacket> type();

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleMovePlayer(this);
    }

    public double getX(double d) {
        return this.hasPos ? this.x : d;
    }

    public double getY(double d) {
        return this.hasPos ? this.y : d;
    }

    public double getZ(double d) {
        return this.hasPos ? this.z : d;
    }

    public float getYRot(float f) {
        return this.hasRot ? this.yRot : f;
    }

    public float getXRot(float f) {
        return this.hasRot ? this.xRot : f;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean hasPosition() {
        return this.hasPos;
    }

    public boolean hasRotation() {
        return this.hasRot;
    }

    public static class StatusOnly
    extends ServerboundMovePlayerPacket {
        public static final StreamCodec<FriendlyByteBuf, StatusOnly> STREAM_CODEC = Packet.codec(StatusOnly::write, StatusOnly::read);

        public StatusOnly(boolean bl) {
            super(0.0, 0.0, 0.0, 0.0f, 0.0f, bl, false, false);
        }

        private static StatusOnly read(FriendlyByteBuf friendlyByteBuf) {
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new StatusOnly(bl);
        }

        private void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<StatusOnly> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_STATUS_ONLY;
        }
    }

    public static class Rot
    extends ServerboundMovePlayerPacket {
        public static final StreamCodec<FriendlyByteBuf, Rot> STREAM_CODEC = Packet.codec(Rot::write, Rot::read);

        public Rot(float f, float f2, boolean bl) {
            super(0.0, 0.0, 0.0, f, f2, bl, false, true);
        }

        private static Rot read(FriendlyByteBuf friendlyByteBuf) {
            float f = friendlyByteBuf.readFloat();
            float f2 = friendlyByteBuf.readFloat();
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new Rot(f, f2, bl);
        }

        private void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeFloat(this.yRot);
            friendlyByteBuf.writeFloat(this.xRot);
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<Rot> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_ROT;
        }
    }

    public static class Pos
    extends ServerboundMovePlayerPacket {
        public static final StreamCodec<FriendlyByteBuf, Pos> STREAM_CODEC = Packet.codec(Pos::write, Pos::read);

        public Pos(double d, double d2, double d3, boolean bl) {
            super(d, d2, d3, 0.0f, 0.0f, bl, true, false);
        }

        private static Pos read(FriendlyByteBuf friendlyByteBuf) {
            double d = friendlyByteBuf.readDouble();
            double d2 = friendlyByteBuf.readDouble();
            double d3 = friendlyByteBuf.readDouble();
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new Pos(d, d2, d3, bl);
        }

        private void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeDouble(this.x);
            friendlyByteBuf.writeDouble(this.y);
            friendlyByteBuf.writeDouble(this.z);
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<Pos> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS;
        }
    }

    public static class PosRot
    extends ServerboundMovePlayerPacket {
        public static final StreamCodec<FriendlyByteBuf, PosRot> STREAM_CODEC = Packet.codec(PosRot::write, PosRot::read);

        public PosRot(double d, double d2, double d3, float f, float f2, boolean bl) {
            super(d, d2, d3, f, f2, bl, true, true);
        }

        private static PosRot read(FriendlyByteBuf friendlyByteBuf) {
            double d = friendlyByteBuf.readDouble();
            double d2 = friendlyByteBuf.readDouble();
            double d3 = friendlyByteBuf.readDouble();
            float f = friendlyByteBuf.readFloat();
            float f2 = friendlyByteBuf.readFloat();
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new PosRot(d, d2, d3, f, f2, bl);
        }

        private void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeDouble(this.x);
            friendlyByteBuf.writeDouble(this.y);
            friendlyByteBuf.writeDouble(this.z);
            friendlyByteBuf.writeFloat(this.yRot);
            friendlyByteBuf.writeFloat(this.xRot);
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<PosRot> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS_ROT;
        }
    }
}

