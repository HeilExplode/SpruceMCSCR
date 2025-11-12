/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.io.IOException;
import java.io.InputStream;

public class FastBufferedInputStream
extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final InputStream in;
    private final byte[] buffer;
    private int limit;
    private int position;

    public FastBufferedInputStream(InputStream inputStream) {
        this(inputStream, 8192);
    }

    public FastBufferedInputStream(InputStream inputStream, int n) {
        this.in = inputStream;
        this.buffer = new byte[n];
    }

    @Override
    public int read() throws IOException {
        if (this.position >= this.limit) {
            this.fill();
            if (this.position >= this.limit) {
                return -1;
            }
        }
        return Byte.toUnsignedInt(this.buffer[this.position++]);
    }

    @Override
    public int read(byte[] byArray, int n, int n2) throws IOException {
        int n3 = this.bytesInBuffer();
        if (n3 <= 0) {
            if (n2 >= this.buffer.length) {
                return this.in.read(byArray, n, n2);
            }
            this.fill();
            n3 = this.bytesInBuffer();
            if (n3 <= 0) {
                return -1;
            }
        }
        if (n2 > n3) {
            n2 = n3;
        }
        System.arraycopy(this.buffer, this.position, byArray, n, n2);
        this.position += n2;
        return n2;
    }

    @Override
    public long skip(long l) throws IOException {
        if (l <= 0L) {
            return 0L;
        }
        long l2 = this.bytesInBuffer();
        if (l2 <= 0L) {
            return this.in.skip(l);
        }
        if (l > l2) {
            l = l2;
        }
        this.position = (int)((long)this.position + l);
        return l;
    }

    @Override
    public int available() throws IOException {
        return this.bytesInBuffer() + this.in.available();
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    private int bytesInBuffer() {
        return this.limit - this.position;
    }

    private void fill() throws IOException {
        this.limit = 0;
        this.position = 0;
        int n = this.in.read(this.buffer, 0, this.buffer.length);
        if (n > 0) {
            this.limit = n;
        }
    }
}

