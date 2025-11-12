/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.BandwidthDebugMonitor;

public class MonitorFrameDecoder
extends ChannelInboundHandlerAdapter {
    private final BandwidthDebugMonitor monitor;

    public MonitorFrameDecoder(BandwidthDebugMonitor bandwidthDebugMonitor) {
        this.monitor = bandwidthDebugMonitor;
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
        if (object instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf)object;
            this.monitor.onReceive(byteBuf.readableBytes());
        }
        channelHandlerContext.fireChannelRead(object);
    }
}

