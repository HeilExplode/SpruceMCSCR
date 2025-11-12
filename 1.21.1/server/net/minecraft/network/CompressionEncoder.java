/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;
import net.minecraft.network.VarInt;

public class CompressionEncoder
extends MessageToByteEncoder<ByteBuf> {
    private final byte[] encodeBuf = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public CompressionEncoder(int n) {
        this.threshold = n;
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
        int n = byteBuf.readableBytes();
        if (n > 0x800000) {
            throw new IllegalArgumentException("Packet too big (is " + n + ", should be less than 8388608)");
        }
        if (n < this.threshold) {
            VarInt.write(byteBuf2, 0);
            byteBuf2.writeBytes(byteBuf);
        } else {
            byte[] byArray = new byte[n];
            byteBuf.readBytes(byArray);
            VarInt.write(byteBuf2, byArray.length);
            this.deflater.setInput(byArray, 0, n);
            this.deflater.finish();
            while (!this.deflater.finished()) {
                int n2 = this.deflater.deflate(this.encodeBuf);
                byteBuf2.writeBytes(this.encodeBuf, 0, n2);
            }
            this.deflater.reset();
        }
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int n) {
        this.threshold = n;
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (ByteBuf)object, byteBuf);
    }
}

