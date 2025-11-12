/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 */
package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TagVisitor;

public class ListTag
extends CollectionTag<Tag> {
    private static final int SELF_SIZE_IN_BYTES = 37;
    public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public ListTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                ListTag listTag = 1.loadList(dataInput, nbtAccounter);
                return listTag;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        private static ListTag loadList(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(37L);
            byte by = dataInput.readByte();
            int n = dataInput.readInt();
            if (by == 0 && n > 0) {
                throw new NbtFormatException("Missing type on ListTag");
            }
            nbtAccounter.accountBytes(4L, n);
            TagType<?> tagType = TagTypes.getType(by);
            ArrayList arrayList = Lists.newArrayListWithCapacity((int)n);
            for (int i = 0; i < n; ++i) {
                arrayList.add(tagType.load(dataInput, nbtAccounter));
            }
            return new ListTag(arrayList, by);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                StreamTagVisitor.ValueResult valueResult = 1.parseList(dataInput, streamTagVisitor, nbtAccounter);
                return valueResult;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        /*
         * Exception decompiling
         */
        private static StreamTagVisitor.ValueResult parseList(DataInput var0, StreamTagVisitor var1_1, NbtAccounter var2_2) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [8[CASE], 4[SWITCH]], but top level block is 9[SWITCH]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                TagType<?> tagType = TagTypes.getType(dataInput.readByte());
                int n = dataInput.readInt();
                tagType.skip(dataInput, n, nbtAccounter);
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private final List<Tag> list;
    private byte type;

    ListTag(List<Tag> list, byte by) {
        this.list = list;
        this.type = by;
    }

    public ListTag() {
        this(Lists.newArrayList(), 0);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        this.type = this.list.isEmpty() ? (byte)0 : this.list.get(0).getId();
        dataOutput.writeByte(this.type);
        dataOutput.writeInt(this.list.size());
        for (Tag tag : this.list) {
            tag.write(dataOutput);
        }
    }

    @Override
    public int sizeInBytes() {
        int n = 37;
        n += 4 * this.list.size();
        for (Tag tag : this.list) {
            n += tag.sizeInBytes();
        }
        return n;
    }

    @Override
    public byte getId() {
        return 9;
    }

    public TagType<ListTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    private void updateTypeAfterRemove() {
        if (this.list.isEmpty()) {
            this.type = 0;
        }
    }

    @Override
    public Tag remove(int n) {
        Tag tag = this.list.remove(n);
        this.updateTypeAfterRemove();
        return tag;
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public CompoundTag getCompound(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 10) {
            return (CompoundTag)tag;
        }
        return new CompoundTag();
    }

    public ListTag getList(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 9) {
            return (ListTag)tag;
        }
        return new ListTag();
    }

    public short getShort(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 2) {
            return ((ShortTag)tag).getAsShort();
        }
        return 0;
    }

    public int getInt(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 3) {
            return ((IntTag)tag).getAsInt();
        }
        return 0;
    }

    public int[] getIntArray(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 11) {
            return ((IntArrayTag)tag).getAsIntArray();
        }
        return new int[0];
    }

    public long[] getLongArray(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 12) {
            return ((LongArrayTag)tag).getAsLongArray();
        }
        return new long[0];
    }

    public double getDouble(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 6) {
            return ((DoubleTag)tag).getAsDouble();
        }
        return 0.0;
    }

    public float getFloat(int n) {
        Tag tag;
        if (n >= 0 && n < this.list.size() && (tag = this.list.get(n)).getId() == 5) {
            return ((FloatTag)tag).getAsFloat();
        }
        return 0.0f;
    }

    public String getString(int n) {
        if (n < 0 || n >= this.list.size()) {
            return "";
        }
        Tag tag = this.list.get(n);
        if (tag.getId() == 8) {
            return tag.getAsString();
        }
        return tag.toString();
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public Tag get(int n) {
        return this.list.get(n);
    }

    @Override
    public Tag set(int n, Tag tag) {
        Tag tag2 = this.get(n);
        if (!this.setTag(n, tag)) {
            throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", tag.getId(), this.type));
        }
        return tag2;
    }

    @Override
    public void add(int n, Tag tag) {
        if (!this.addTag(n, tag)) {
            throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", tag.getId(), this.type));
        }
    }

    @Override
    public boolean setTag(int n, Tag tag) {
        if (this.updateType(tag)) {
            this.list.set(n, tag);
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int n, Tag tag) {
        if (this.updateType(tag)) {
            this.list.add(n, tag);
            return true;
        }
        return false;
    }

    private boolean updateType(Tag tag) {
        if (tag.getId() == 0) {
            return false;
        }
        if (this.type == 0) {
            this.type = tag.getId();
            return true;
        }
        return this.type == tag.getId();
    }

    @Override
    public ListTag copy() {
        List<Tag> list = TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy);
        ArrayList arrayList = Lists.newArrayList(list);
        return new ListTag(arrayList, this.type);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ListTag && Objects.equals(this.list, ((ListTag)object).list);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitList(this);
    }

    @Override
    public byte getElementType() {
        return this.type;
    }

    @Override
    public void clear() {
        this.list.clear();
        this.type = 0;
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        switch (streamTagVisitor.visitList(TagTypes.getType(this.type), this.list.size())) {
            case HALT: {
                return StreamTagVisitor.ValueResult.HALT;
            }
            case BREAK: {
                return streamTagVisitor.visitContainerEnd();
            }
        }
        block13: for (int i = 0; i < this.list.size(); ++i) {
            Tag tag = this.list.get(i);
            switch (streamTagVisitor.visitElement(tag.getType(), i)) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case SKIP: {
                    continue block13;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
                default: {
                    switch (tag.accept(streamTagVisitor)) {
                        case HALT: {
                            return StreamTagVisitor.ValueResult.HALT;
                        }
                        case BREAK: {
                            return streamTagVisitor.visitContainerEnd();
                        }
                    }
                }
            }
        }
        return streamTagVisitor.visitContainerEnd();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }

    @Override
    public /* synthetic */ Object remove(int n) {
        return this.remove(n);
    }

    @Override
    public /* synthetic */ void add(int n, Object object) {
        this.add(n, (Tag)object);
    }

    @Override
    public /* synthetic */ Object set(int n, Object object) {
        return this.set(n, (Tag)object);
    }

    @Override
    public /* synthetic */ Object get(int n) {
        return this.get(n);
    }
}

