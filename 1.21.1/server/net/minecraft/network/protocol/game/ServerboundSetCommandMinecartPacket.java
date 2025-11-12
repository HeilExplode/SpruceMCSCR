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
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;

public class ServerboundSetCommandMinecartPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetCommandMinecartPacket> STREAM_CODEC = Packet.codec(ServerboundSetCommandMinecartPacket::write, ServerboundSetCommandMinecartPacket::new);
    private final int entity;
    private final String command;
    private final boolean trackOutput;

    public ServerboundSetCommandMinecartPacket(int n, String string, boolean bl) {
        this.entity = n;
        this.command = string;
        this.trackOutput = bl;
    }

    private ServerboundSetCommandMinecartPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entity = friendlyByteBuf.readVarInt();
        this.command = friendlyByteBuf.readUtf();
        this.trackOutput = friendlyByteBuf.readBoolean();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entity);
        friendlyByteBuf.writeUtf(this.command);
        friendlyByteBuf.writeBoolean(this.trackOutput);
    }

    @Override
    public PacketType<ServerboundSetCommandMinecartPacket> type() {
        return GamePacketTypes.SERVERBOUND_SET_COMMAND_MINECART;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSetCommandMinecart(this);
    }

    @Nullable
    public BaseCommandBlock getCommandBlock(Level level) {
        Entity entity = level.getEntity(this.entity);
        if (entity instanceof MinecartCommandBlock) {
            return ((MinecartCommandBlock)entity).getCommandBlock();
        }
        return null;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }
}

