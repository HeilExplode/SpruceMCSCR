/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;

public class VarLong {
    private static final int MAX_VARLONG_SIZE = 10;
    private static final int DATA_BITS_MASK = 127;
    private static final int CONTINUATION_BIT_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static int getByteSize(long l) {
        for (int i = 1; i < 10; ++i) {
            if ((l & -1L << i * 7) != 0L) continue;
            return i;
        }
        return 10;
    }

    public static boolean hasContinuationBit(byte by) {
        return (by & 0x80) == 128;
    }

    public static long read(ByteBuf byteBuf) {
        byte by;
        long l = 0L;
        int n = 0;
        do {
            by = byteBuf.readByte();
            l |= (long)(by & 0x7F) << n++ * 7;
            if (n <= 10) continue;
            throw new RuntimeException("VarLong too big");
        } while (VarLong.hasContinuationBit(by));
        return l;
    }

    public static ByteBuf write(ByteBuf byteBuf, long l) {
        while (true) {
            if ((l & 0xFFFFFFFFFFFFFF80L) == 0L) {
                byteBuf.writeByte((int)l);
                return byteBuf;
            }
            byteBuf.writeByte((int)(l & 0x7FL) | 0x80);
            l >>>= 7;
        }
    }
}

