/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 */
package net.minecraft.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.UnconfiguredPipelineHandler;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {
    public static void handleInboundTerminalPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        if (packet.isTerminal()) {
            channelHandlerContext.channel().config().setAutoRead(false);
            channelHandlerContext.pipeline().addBefore(channelHandlerContext.name(), "inbound_config", (ChannelHandler)new UnconfiguredPipelineHandler.Inbound());
            channelHandlerContext.pipeline().remove(channelHandlerContext.name());
        }
    }

    public static void handleOutboundTerminalPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        if (packet.isTerminal()) {
            channelHandlerContext.pipeline().addAfter(channelHandlerContext.name(), "outbound_config", (ChannelHandler)new UnconfiguredPipelineHandler.Outbound());
            channelHandlerContext.pipeline().remove(channelHandlerContext.name());
        }
    }
}

