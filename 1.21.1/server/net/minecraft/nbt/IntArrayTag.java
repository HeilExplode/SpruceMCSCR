/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class IntArrayTag
extends CollectionTag<IntTag> {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<IntArrayTag> TYPE = new TagType.VariableSize<IntArrayTag>(){

        @Override
        public IntArrayTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return new IntArrayTag(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static int[] readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(24L);
            int n = dataInput.readInt();
            nbtAccounter.accountBytes(4L, n);
            int[] nArray = new int[n];
            for (int i = 0; i < n; ++i) {
                nArray[i] = dataInput.readInt();
            }
            return nArray;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 4);
        }

        @Override
        public String getName() {
            return "INT[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private int[] data;

    public IntArrayTag(int[] nArray) {
        this.data = nArray;
    }

    public IntArrayTag(List<Integer> list) {
        this(IntArrayTag.toArray(list));
    }

    private static int[] toArray(List<Integer> list) {
        int[] nArray = new int[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Integer n = list.get(i);
            nArray[i] = n == null ? 0 : n;
        }
        return nArray;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        for (int n : this.data) {
            dataOutput.writeInt(n);
        }
    }

    @Override
    public int sizeInBytes() {
        return 24 + 4 * this.data.length;
    }

    @Override
    public byte getId() {
        return 11;
    }

    public TagType<IntArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    @Override
    public IntArrayTag copy() {
        int[] nArray = new int[this.data.length];
        System.arraycopy(this.data, 0, nArray, 0, this.data.length);
        return new IntArrayTag(nArray);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)object).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public int[] getAsIntArray() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitIntArray(this);
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public IntTag get(int n) {
        return IntTag.valueOf(this.data[n]);
    }

    @Override
    public IntTag set(int n, IntTag intTag) {
        int n2 = this.data[n];
        this.data[n] = intTag.getAsInt();
        return IntTag.valueOf(n2);
    }

    @Override
    public void add(int n, IntTag intTag) {
        this.data = ArrayUtils.add((int[])this.data, (int)n, (int)intTag.getAsInt());
    }

    @Override
    public boolean setTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data[n] = ((NumericTag)tag).getAsInt();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data = ArrayUtils.add((int[])this.data, (int)n, (int)((NumericTag)tag).getAsInt());
            return true;
        }
        return false;
    }

    @Override
    public IntTag remove(int n) {
        int n2 = this.data[n];
        this.data = ArrayUtils.remove((int[])this.data, (int)n);
        return IntTag.valueOf(n2);
    }

    @Override
    public byte getElementType() {
        return 3;
    }

    @Override
    public void clear() {
        this.data = new int[0];
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.data);
    }

    @Override
    public /* synthetic */ Tag remove(int n) {
        return this.remove(n);
    }

    @Override
    public /* synthetic */ void add(int n, Tag tag) {
        this.add(n, (IntTag)tag);
    }

    @Override
    public /* synthetic */ Tag set(int n, Tag tag) {
        return this.set(n, (IntTag)tag);
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
        this.add(n, (IntTag)object);
    }

    @Override
    public /* synthetic */ Object set(int n, Object object) {
        return this.set(n, (IntTag)object);
    }

    @Override
    public /* synthetic */ Object get(int n) {
        return this.get(n);
    }
}

