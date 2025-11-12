/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.CorruptedFrameException
 *  javax.annotation.Nullable
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.VarInt;

public class Varint21FrameDecoder
extends ByteToMessageDecoder {
    private static final int MAX_VARINT21_BYTES = 3;
    private final ByteBuf helperBuf = Unpooled.directBuffer((int)3);
    @Nullable
    private final BandwidthDebugMonitor monitor;

    public Varint21FrameDecoder(@Nullable BandwidthDebugMonitor bandwidthDebugMonitor) {
        this.monitor = bandwidthDebugMonitor;
    }

    protected void handlerRemoved0(ChannelHandlerContext channelHandlerContext) {
        this.helperBuf.release();
    }

    private static boolean copyVarint(ByteBuf byteBuf, ByteBuf byteBuf2) {
        for (int i = 0; i < 3; ++i) {
            if (!byteBuf.isReadable()) {
                return false;
            }
            byte by = byteBuf.readByte();
            byteBuf2.writeByte((int)by);
            if (VarInt.hasContinuationBit(by)) continue;
            return true;
        }
        throw new CorruptedFrameException("length wider than 21-bit");
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        byteBuf.markReaderIndex();
        this.helperBuf.clear();
        if (!Varint21FrameDecoder.copyVarint(byteBuf, this.helperBuf)) {
            byteBuf.resetReaderIndex();
            return;
        }
        int n = VarInt.read(this.helperBuf);
        if (byteBuf.readableBytes() < n) {
            byteBuf.resetReaderIndex();
            return;
        }
        if (this.monitor != null) {
            this.monitor.onReceive(n + VarInt.getByteSize(n));
        }
        list.add(byteBuf.readBytes(n));
    }
}

