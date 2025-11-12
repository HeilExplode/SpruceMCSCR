/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.rcon;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetworkDataOutputStream {
    private final ByteArrayOutputStream outputStream;
    private final DataOutputStream dataOutputStream;

    public NetworkDataOutputStream(int n) {
        this.outputStream = new ByteArrayOutputStream(n);
        this.dataOutputStream = new DataOutputStream(this.outputStream);
    }

    public void writeBytes(byte[] byArray) throws IOException {
        this.dataOutputStream.write(byArray, 0, byArray.length);
    }

    public void writeString(String string) throws IOException {
        this.dataOutputStream.writeBytes(string);
        this.dataOutputStream.write(0);
    }

    public void write(int n) throws IOException {
        this.dataOutputStream.write(n);
    }

    public void writeShort(short s) throws IOException {
        this.dataOutputStream.writeShort(Short.reverseBytes(s));
    }

    public void writeInt(int n) throws IOException {
        this.dataOutputStream.writeInt(Integer.reverseBytes(n));
    }

    public void writeFloat(float f) throws IOException {
        this.dataOutputStream.writeInt(Integer.reverseBytes(Float.floatToIntBits(f)));
    }

    public byte[] toByteArray() {
        return this.outputStream.toByteArray();
    }

    public void reset() {
        this.outputStream.reset();
    }
}

