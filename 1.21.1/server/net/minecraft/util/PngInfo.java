/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record PngInfo(int width, int height) {
    private static final long PNG_HEADER = -8552249625308161526L;
    private static final int IHDR_TYPE = 1229472850;
    private static final int IHDR_SIZE = 13;

    public static PngInfo fromStream(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        if (dataInputStream.readLong() != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        }
        if (dataInputStream.readInt() != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        }
        if (dataInputStream.readInt() != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        }
        int n = dataInputStream.readInt();
        int n2 = dataInputStream.readInt();
        return new PngInfo(n, n2);
    }

    public static PngInfo fromBytes(byte[] byArray) throws IOException {
        return PngInfo.fromStream(new ByteArrayInputStream(byArray));
    }

    public static void validateHeader(ByteBuffer byteBuffer) throws IOException {
        ByteOrder byteOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        if (byteBuffer.getLong(0) != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        }
        if (byteBuffer.getInt(8) != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        }
        if (byteBuffer.getInt(12) != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        }
        byteBuffer.order(byteOrder);
    }
}

