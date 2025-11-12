/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufUtil
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.VarInt;

public class Utf8String {
    public static String read(ByteBuf byteBuf, int n) {
        int n2 = ByteBufUtil.utf8MaxBytes((int)n);
        int n3 = VarInt.read(byteBuf);
        if (n3 > n2) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + n3 + " > " + n2 + ")");
        }
        if (n3 < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        int n4 = byteBuf.readableBytes();
        if (n3 > n4) {
            throw new DecoderException("Not enough bytes in buffer, expected " + n3 + ", but got " + n4);
        }
        String string = byteBuf.toString(byteBuf.readerIndex(), n3, StandardCharsets.UTF_8);
        byteBuf.readerIndex(byteBuf.readerIndex() + n3);
        if (string.length() > n) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + string.length() + " > " + n + ")");
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void write(ByteBuf byteBuf, CharSequence charSequence, int n) {
        if (charSequence.length() > n) {
            throw new EncoderException("String too big (was " + charSequence.length() + " characters, max " + n + ")");
        }
        int n2 = ByteBufUtil.utf8MaxBytes((CharSequence)charSequence);
        ByteBuf byteBuf2 = byteBuf.alloc().buffer(n2);
        try {
            int n3 = ByteBufUtil.writeUtf8((ByteBuf)byteBuf2, (CharSequence)charSequence);
            int n4 = ByteBufUtil.utf8MaxBytes((int)n);
            if (n3 > n4) {
                throw new EncoderException("String too big (was " + n3 + " bytes encoded, max " + n4 + ")");
            }
            VarInt.write(byteBuf, n3);
            byteBuf.writeBytes(byteBuf2);
        }
        finally {
            byteBuf2.release();
        }
    }
}

