/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.DecoderException
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net.minecraft.network.VarInt;

public class CompressionDecoder
extends ByteToMessageDecoder {
    public static final int MAXIMUM_COMPRESSED_LENGTH = 0x200000;
    public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 0x800000;
    private final Inflater inflater;
    private int threshold;
    private boolean validateDecompressed;

    public CompressionDecoder(int n, boolean bl) {
        this.threshold = n;
        this.validateDecompressed = bl;
        this.inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() == 0) {
            return;
        }
        int n = VarInt.read(byteBuf);
        if (n == 0) {
            list.add(byteBuf.readBytes(byteBuf.readableBytes()));
            return;
        }
        if (this.validateDecompressed) {
            if (n < this.threshold) {
                throw new DecoderException("Badly compressed packet - size of " + n + " is below server threshold of " + this.threshold);
            }
            if (n > 0x800000) {
                throw new DecoderException("Badly compressed packet - size of " + n + " is larger than protocol maximum of 8388608");
            }
        }
        this.setupInflaterInput(byteBuf);
        ByteBuf byteBuf2 = this.inflate(channelHandlerContext, n);
        this.inflater.reset();
        list.add(byteBuf2);
    }

    private void setupInflaterInput(ByteBuf byteBuf) {
        ByteBuffer byteBuffer;
        if (byteBuf.nioBufferCount() > 0) {
            byteBuffer = byteBuf.nioBuffer();
            byteBuf.skipBytes(byteBuf.readableBytes());
        } else {
            byteBuffer = ByteBuffer.allocateDirect(byteBuf.readableBytes());
            byteBuf.readBytes(byteBuffer);
            byteBuffer.flip();
        }
        this.inflater.setInput(byteBuffer);
    }

    private ByteBuf inflate(ChannelHandlerContext channelHandlerContext, int n) throws DataFormatException {
        ByteBuf byteBuf = channelHandlerContext.alloc().directBuffer(n);
        try {
            ByteBuffer byteBuffer = byteBuf.internalNioBuffer(0, n);
            int n2 = byteBuffer.position();
            this.inflater.inflate(byteBuffer);
            int n3 = byteBuffer.position() - n2;
            if (n3 != n) {
                throw new DecoderException("Badly compressed packet - actual length of uncompressed payload " + n3 + " is does not match declared size " + n);
            }
            byteBuf.writerIndex(byteBuf.writerIndex() + n3);
            return byteBuf;
        }
        catch (Exception exception) {
            byteBuf.release();
            throw exception;
        }
    }

    public void setThreshold(int n, boolean bl) {
        this.threshold = n;
        this.validateDecompressed = bl;
    }
}

