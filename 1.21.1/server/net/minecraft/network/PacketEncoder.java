/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ProtocolSwapHandler;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder<T extends PacketListener>
extends MessageToByteEncoder<Packet<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ProtocolInfo<T> protocolInfo;

    public PacketEncoder(ProtocolInfo<T> protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<T> packet, ByteBuf byteBuf) throws Exception {
        PacketType<Packet<T>> packetType = packet.type();
        try {
            this.protocolInfo.codec().encode(byteBuf, packet);
            int n = byteBuf.readableBytes();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {} -> {} bytes", new Object[]{this.protocolInfo.id().id(), packetType, packet.getClass().getName(), n});
            }
            JvmProfiler.INSTANCE.onPacketSent(this.protocolInfo.id(), packetType, channelHandlerContext.channel().remoteAddress(), n);
        }
        catch (Throwable throwable) {
            LOGGER.error("Error sending packet {}", packetType, (Object)throwable);
            if (packet.isSkippable()) {
                throw new SkipPacketException(throwable);
            }
            throw throwable;
        }
        finally {
            ProtocolSwapHandler.handleOutboundTerminalPacket(channelHandlerContext, packet);
        }
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (Packet)object, byteBuf);
    }
}

