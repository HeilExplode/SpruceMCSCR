/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.io.DataOutput;
import java.io.IOException;

public class DelegateDataOutput
implements DataOutput {
    private final DataOutput parent;

    public DelegateDataOutput(DataOutput dataOutput) {
        this.parent = dataOutput;
    }

    @Override
    public void write(int n) throws IOException {
        this.parent.write(n);
    }

    @Override
    public void write(byte[] byArray) throws IOException {
        this.parent.write(byArray);
    }

    @Override
    public void write(byte[] byArray, int n, int n2) throws IOException {
        this.parent.write(byArray, n, n2);
    }

    @Override
    public void writeBoolean(boolean bl) throws IOException {
        this.parent.writeBoolean(bl);
    }

    @Override
    public void writeByte(int n) throws IOException {
        this.parent.writeByte(n);
    }

    @Override
    public void writeShort(int n) throws IOException {
        this.parent.writeShort(n);
    }

    @Override
    public void writeChar(int n) throws IOException {
        this.parent.writeChar(n);
    }

    @Override
    public void writeInt(int n) throws IOException {
        this.parent.writeInt(n);
    }

    @Override
    public void writeLong(long l) throws IOException {
        this.parent.writeLong(l);
    }

    @Override
    public void writeFloat(float f) throws IOException {
        this.parent.writeFloat(f);
    }

    @Override
    public void writeDouble(double d) throws IOException {
        this.parent.writeDouble(d);
    }

    @Override
    public void writeBytes(String string) throws IOException {
        this.parent.writeBytes(string);
    }

    @Override
    public void writeChars(String string) throws IOException {
        this.parent.writeChars(string);
    }

    @Override
    public void writeUTF(String string) throws IOException {
        this.parent.writeUTF(string);
    }
}

