/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;

public interface TagType<T extends Tag> {
    public T load(DataInput var1, NbtAccounter var2) throws IOException;

    public StreamTagVisitor.ValueResult parse(DataInput var1, StreamTagVisitor var2, NbtAccounter var3) throws IOException;

    default public void parseRoot(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
        switch (streamTagVisitor.visitRootEntry(this)) {
            case CONTINUE: {
                this.parse(dataInput, streamTagVisitor, nbtAccounter);
                break;
            }
            case HALT: {
                break;
            }
            case BREAK: {
                this.skip(dataInput, nbtAccounter);
            }
        }
    }

    public void skip(DataInput var1, int var2, NbtAccounter var3) throws IOException;

    public void skip(DataInput var1, NbtAccounter var2) throws IOException;

    default public boolean isValue() {
        return false;
    }

    public String getName();

    public String getPrettyName();

    public static TagType<EndTag> createInvalid(final int n) {
        return new TagType<EndTag>(){

            private IOException createException() {
                return new IOException("Invalid tag id: " + n);
            }

            @Override
            public EndTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
                throw this.createException();
            }

            @Override
            public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput dataInput, int n2, NbtAccounter nbtAccounter) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
                throw this.createException();
            }

            @Override
            public String getName() {
                return "INVALID[" + n + "]";
            }

            @Override
            public String getPrettyName() {
                return "UNKNOWN_" + n;
            }

            @Override
            public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
                return this.load(dataInput, nbtAccounter);
            }
        };
    }

    public static interface VariableSize<T extends Tag>
    extends TagType<T> {
        @Override
        default public void skip(DataInput dataInput, int n, NbtAccounter nbtAccounter) throws IOException {
            for (int i = 0; i < n; ++i) {
                this.skip(dataInput, nbtAccounter);
            }
        }
    }

    public static interface StaticSize<T extends Tag>
    extends TagType<T> {
        @Override
        default public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(this.size());
        }

        @Override
        default public void skip(DataInput dataInput, int n, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(this.size() * n);
        }

        public int size();
    }
}

