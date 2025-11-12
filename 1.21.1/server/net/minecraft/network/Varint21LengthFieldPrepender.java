/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.EncoderException
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.VarInt;

@ChannelHandler.Sharable
public class Varint21LengthFieldPrepender
extends MessageToByteEncoder<ByteBuf> {
    public static final int MAX_VARINT21_BYTES = 3;

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
        int n = byteBuf.readableBytes();
        int n2 = VarInt.getByteSize(n);
        if (n2 > 3) {
            throw new EncoderException("Packet too large: size " + n + " is over 8");
        }
        byteBuf2.ensureWritable(n2 + n);
        VarInt.write(byteBuf2, n);
        byteBuf2.writeBytes(byteBuf, byteBuf.readerIndex(), n);
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (ByteBuf)object, byteBuf);
    }
}

