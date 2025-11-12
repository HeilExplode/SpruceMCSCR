/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public class StringTag
implements Tag {
    private static final int SELF_SIZE_IN_BYTES = 36;
    public static final TagType<StringTag> TYPE = new TagType.VariableSize<StringTag>(){

        @Override
        public StringTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return StringTag.valueOf(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static String readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(36L);
            String string = dataInput.readUTF();
            nbtAccounter.accountBytes(2L, string.length());
            return string;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            StringTag.skipString(dataInput);
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String getPrettyName() {
            return "TAG_String";
        }

        @Override
        public boolean isValue() {
            return true;
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private static final StringTag EMPTY = new StringTag("");
    private static final char DOUBLE_QUOTE = '\"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';
    private static final char NOT_SET = '\u0000';
    private final String data;

    public static void skipString(DataInput dataInput) throws IOException {
        dataInput.skipBytes(dataInput.readUnsignedShort());
    }

    private StringTag(String string) {
        Objects.requireNonNull(string, "Null string not allowed");
        this.data = string;
    }

    public static StringTag valueOf(String string) {
        if (string.isEmpty()) {
            return EMPTY;
        }
        return new StringTag(string);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 36 + 2 * this.data.length();
    }

    @Override
    public byte getId() {
        return 8;
    }

    public TagType<StringTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return Tag.super.getAsString();
    }

    @Override
    public StringTag copy() {
        return this;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof StringTag && Objects.equals(this.data, ((StringTag)object).data);
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public String getAsString() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitString(this);
    }

    public static String quoteAndEscape(String string) {
        StringBuilder stringBuilder = new StringBuilder(" ");
        int n = 0;
        for (int i = 0; i < string.length(); ++i) {
            int n2 = string.charAt(i);
            if (n2 == 92) {
                stringBuilder.append('\\');
            } else if (n2 == 34 || n2 == 39) {
                if (n == 0) {
                    int n3 = n = n2 == 34 ? 39 : 34;
                }
                if (n == n2) {
                    stringBuilder.append('\\');
                }
            }
            stringBuilder.append((char)n2);
        }
        if (n == 0) {
            n = 34;
        }
        stringBuilder.setCharAt(0, (char)n);
        stringBuilder.append((char)n);
        return stringBuilder.toString();
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.data);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

