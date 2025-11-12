/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufAllocator
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 *  io.netty.util.concurrent.GenericFutureListener
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.SocketAddress;
import java.util.Locale;
import net.minecraft.server.ServerInfo;
import net.minecraft.server.network.LegacyProtocolUtils;
import org.slf4j.Logger;

public class LegacyQueryHandler
extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerInfo server;

    public LegacyQueryHandler(ServerInfo serverInfo) {
        this.server = serverInfo;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
        ByteBuf byteBuf = (ByteBuf)object;
        byteBuf.markReaderIndex();
        boolean bl = true;
        try {
            if (byteBuf.readUnsignedByte() != 254) {
                return;
            }
            SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
            int n = byteBuf.readableBytes();
            if (n == 0) {
                LOGGER.debug("Ping: (<1.3.x) from {}", (Object)socketAddress);
                String string = LegacyQueryHandler.createVersion0Response(this.server);
                LegacyQueryHandler.sendFlushAndClose(channelHandlerContext, LegacyQueryHandler.createLegacyDisconnectPacket(channelHandlerContext.alloc(), string));
            } else {
                if (byteBuf.readUnsignedByte() != 1) {
                    return;
                }
                if (byteBuf.isReadable()) {
                    if (!LegacyQueryHandler.readCustomPayloadPacket(byteBuf)) {
                        return;
                    }
                    LOGGER.debug("Ping: (1.6) from {}", (Object)socketAddress);
                } else {
                    LOGGER.debug("Ping: (1.4-1.5.x) from {}", (Object)socketAddress);
                }
                String string = LegacyQueryHandler.createVersion1Response(this.server);
                LegacyQueryHandler.sendFlushAndClose(channelHandlerContext, LegacyQueryHandler.createLegacyDisconnectPacket(channelHandlerContext.alloc(), string));
            }
            byteBuf.release();
            bl = false;
        }
        catch (RuntimeException runtimeException) {
        }
        finally {
            if (bl) {
                byteBuf.resetReaderIndex();
                channelHandlerContext.channel().pipeline().remove((ChannelHandler)this);
                channelHandlerContext.fireChannelRead(object);
            }
        }
    }

    private static boolean readCustomPayloadPacket(ByteBuf byteBuf) {
        short s = byteBuf.readUnsignedByte();
        if (s != 250) {
            return false;
        }
        String string = LegacyProtocolUtils.readLegacyString(byteBuf);
        if (!"MC|PingHost".equals(string)) {
            return false;
        }
        int n = byteBuf.readUnsignedShort();
        if (byteBuf.readableBytes() != n) {
            return false;
        }
        short s2 = byteBuf.readUnsignedByte();
        if (s2 < 73) {
            return false;
        }
        String string2 = LegacyProtocolUtils.readLegacyString(byteBuf);
        int n2 = byteBuf.readInt();
        return n2 <= 65535;
    }

    private static String createVersion0Response(ServerInfo serverInfo) {
        return String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", serverInfo.getMotd(), serverInfo.getPlayerCount(), serverInfo.getMaxPlayers());
    }

    private static String createVersion1Response(ServerInfo serverInfo) {
        return String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, serverInfo.getServerVersion(), serverInfo.getMotd(), serverInfo.getPlayerCount(), serverInfo.getMaxPlayers());
    }

    private static void sendFlushAndClose(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        channelHandlerContext.pipeline().firstContext().writeAndFlush((Object)byteBuf).addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
    }

    private static ByteBuf createLegacyDisconnectPacket(ByteBufAllocator byteBufAllocator, String string) {
        ByteBuf byteBuf = byteBufAllocator.buffer();
        byteBuf.writeByte(255);
        LegacyProtocolUtils.writeLegacyString(byteBuf, string);
        return byteBuf;
    }
}

