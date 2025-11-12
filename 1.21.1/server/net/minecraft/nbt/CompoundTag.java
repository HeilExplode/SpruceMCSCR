/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 */
package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TagVisitor;

public class CompoundTag
implements Tag {
    public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        Tag tag = (Tag)dynamic.convert((DynamicOps)NbtOps.INSTANCE).getValue();
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success((Object)(compoundTag == dynamic.getValue() ? compoundTag.copy() : compoundTag));
        }
        return DataResult.error(() -> "Not a compound tag: " + String.valueOf(tag));
    }, compoundTag -> new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.copy()));
    private static final int SELF_SIZE_IN_BYTES = 48;
    private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
    public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public CompoundTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                CompoundTag compoundTag = 1.loadCompound(dataInput, nbtAccounter);
                return compoundTag;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        private static CompoundTag loadCompound(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            byte by;
            nbtAccounter.accountBytes(48L);
            HashMap hashMap = Maps.newHashMap();
            while ((by = dataInput.readByte()) != 0) {
                Tag tag;
                String string = 1.readString(dataInput, nbtAccounter);
                if (hashMap.put(string, tag = CompoundTag.readNamedTagData(TagTypes.getType(by), string, dataInput, nbtAccounter)) != null) continue;
                nbtAccounter.accountBytes(36L);
            }
            return new CompoundTag(hashMap);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                StreamTagVisitor.ValueResult valueResult = 1.parseCompound(dataInput, streamTagVisitor, nbtAccounter);
                return valueResult;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        private static StreamTagVisitor.ValueResult parseCompound(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            byte by;
            nbtAccounter.accountBytes(48L);
            block13: while ((by = dataInput.readByte()) != 0) {
                TagType<?> tagType = TagTypes.getType(by);
                switch (streamTagVisitor.visitEntry(tagType)) {
                    case HALT: {
                        return StreamTagVisitor.ValueResult.HALT;
                    }
                    case BREAK: {
                        StringTag.skipString(dataInput);
                        tagType.skip(dataInput, nbtAccounter);
                        break block13;
                    }
                    case SKIP: {
                        StringTag.skipString(dataInput);
                        tagType.skip(dataInput, nbtAccounter);
                        continue block13;
                    }
                    default: {
                        String string = 1.readString(dataInput, nbtAccounter);
                        switch (streamTagVisitor.visitEntry(tagType, string)) {
                            case HALT: {
                                return StreamTagVisitor.ValueResult.HALT;
                            }
                            case BREAK: {
                                tagType.skip(dataInput, nbtAccounter);
                                break block13;
                            }
                            case SKIP: {
                                tagType.skip(dataInput, nbtAccounter);
                                continue block13;
                            }
                        }
                        nbtAccounter.accountBytes(36L);
                        switch (tagType.parse(dataInput, streamTagVisitor, nbtAccounter)) {
                            case HALT: {
                                return StreamTagVisitor.ValueResult.HALT;
                            }
                        }
                        continue block13;
                    }
                }
            }
            if (by != 0) {
                while ((by = dataInput.readByte()) != 0) {
                    StringTag.skipString(dataInput);
                    TagTypes.getType(by).skip(dataInput, nbtAccounter);
                }
            }
            return streamTagVisitor.visitContainerEnd();
        }

        private static String readString(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            String string = dataInput.readUTF();
            nbtAccounter.accountBytes(28L);
            nbtAccounter.accountBytes(2L, string.length());
            return string;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                byte by;
                while ((by = dataInput.readByte()) != 0) {
                    StringTag.skipString(dataInput);
                    TagTypes.getType(by).skip(dataInput, nbtAccounter);
                }
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        @Override
        public String getName() {
            return "COMPOUND";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Compound";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private final Map<String, Tag> tags;

    protected CompoundTag(Map<String, Tag> map) {
        this.tags = map;
    }

    public CompoundTag() {
        this(Maps.newHashMap());
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        for (String string : this.tags.keySet()) {
            Tag tag = this.tags.get(string);
            CompoundTag.writeNamedTag(string, tag, dataOutput);
        }
        dataOutput.writeByte(0);
    }

    @Override
    public int sizeInBytes() {
        int n = 48;
        for (Map.Entry<String, Tag> entry : this.tags.entrySet()) {
            n += 28 + 2 * entry.getKey().length();
            n += 36;
            n += entry.getValue().sizeInBytes();
        }
        return n;
    }

    public Set<String> getAllKeys() {
        return this.tags.keySet();
    }

    @Override
    public byte getId() {
        return 10;
    }

    public TagType<CompoundTag> getType() {
        return TYPE;
    }

    public int size() {
        return this.tags.size();
    }

    @Nullable
    public Tag put(String string, Tag tag) {
        return this.tags.put(string, tag);
    }

    public void putByte(String string, byte by) {
        this.tags.put(string, ByteTag.valueOf(by));
    }

    public void putShort(String string, short s) {
        this.tags.put(string, ShortTag.valueOf(s));
    }

    public void putInt(String string, int n) {
        this.tags.put(string, IntTag.valueOf(n));
    }

    public void putLong(String string, long l) {
        this.tags.put(string, LongTag.valueOf(l));
    }

    public void putUUID(String string, UUID uUID) {
        this.tags.put(string, NbtUtils.createUUID(uUID));
    }

    public UUID getUUID(String string) {
        return NbtUtils.loadUUID(this.get(string));
    }

    public boolean hasUUID(String string) {
        Tag tag = this.get(string);
        return tag != null && tag.getType() == IntArrayTag.TYPE && ((IntArrayTag)tag).getAsIntArray().length == 4;
    }

    public void putFloat(String string, float f) {
        this.tags.put(string, FloatTag.valueOf(f));
    }

    public void putDouble(String string, double d) {
        this.tags.put(string, DoubleTag.valueOf(d));
    }

    public void putString(String string, String string2) {
        this.tags.put(string, StringTag.valueOf(string2));
    }

    public void putByteArray(String string, byte[] byArray) {
        this.tags.put(string, new ByteArrayTag(byArray));
    }

    public void putByteArray(String string, List<Byte> list) {
        this.tags.put(string, new ByteArrayTag(list));
    }

    public void putIntArray(String string, int[] nArray) {
        this.tags.put(string, new IntArrayTag(nArray));
    }

    public void putIntArray(String string, List<Integer> list) {
        this.tags.put(string, new IntArrayTag(list));
    }

    public void putLongArray(String string, long[] lArray) {
        this.tags.put(string, new LongArrayTag(lArray));
    }

    public void putLongArray(String string, List<Long> list) {
        this.tags.put(string, new LongArrayTag(list));
    }

    public void putBoolean(String string, boolean bl) {
        this.tags.put(string, ByteTag.valueOf(bl));
    }

    @Nullable
    public Tag get(String string) {
        return this.tags.get(string);
    }

    public byte getTagType(String string) {
        Tag tag = this.tags.get(string);
        if (tag == null) {
            return 0;
        }
        return tag.getId();
    }

    public boolean contains(String string) {
        return this.tags.containsKey(string);
    }

    public boolean contains(String string, int n) {
        byte by = this.getTagType(string);
        if (by == n) {
            return true;
        }
        if (n == 99) {
            return by == 1 || by == 2 || by == 3 || by == 4 || by == 5 || by == 6;
        }
        return false;
    }

    public byte getByte(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsByte();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public short getShort(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsShort();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public int getInt(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsInt();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public long getLong(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsLong();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0L;
    }

    public float getFloat(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsFloat();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0.0f;
    }

    public double getDouble(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsDouble();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0.0;
    }

    public String getString(String string) {
        try {
            if (this.contains(string, 8)) {
                return this.tags.get(string).getAsString();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return "";
    }

    public byte[] getByteArray(String string) {
        try {
            if (this.contains(string, 7)) {
                return ((ByteArrayTag)this.tags.get(string)).getAsByteArray();
            }
        }
        catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, ByteArrayTag.TYPE, classCastException));
        }
        return new byte[0];
    }

    public int[] getIntArray(String string) {
        try {
            if (this.contains(string, 11)) {
                return ((IntArrayTag)this.tags.get(string)).getAsIntArray();
            }
        }
        catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, IntArrayTag.TYPE, classCastException));
        }
        return new int[0];
    }

    public long[] getLongArray(String string) {
        try {
            if (this.contains(string, 12)) {
                return ((LongArrayTag)this.tags.get(string)).getAsLongArray();
            }
        }
        catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, LongArrayTag.TYPE, classCastException));
        }
        return new long[0];
    }

    public CompoundTag getCompound(String string) {
        try {
            if (this.contains(string, 10)) {
                return (CompoundTag)this.tags.get(string);
            }
        }
        catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, TYPE, classCastException));
        }
        return new CompoundTag();
    }

    public ListTag getList(String string, int n) {
        try {
            if (this.getTagType(string) == 9) {
                ListTag listTag = (ListTag)this.tags.get(string);
                if (listTag.isEmpty() || listTag.getElementType() == n) {
                    return listTag;
                }
                return new ListTag();
            }
        }
        catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, ListTag.TYPE, classCastException));
        }
        return new ListTag();
    }

    public boolean getBoolean(String string) {
        return this.getByte(string) != 0;
    }

    public void remove(String string) {
        this.tags.remove(string);
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    private CrashReport createReport(String string, TagType<?> tagType, ClassCastException classCastException) {
        CrashReport crashReport = CrashReport.forThrowable(classCastException, "Reading NBT data");
        CrashReportCategory crashReportCategory = crashReport.addCategory("Corrupt NBT tag", 1);
        crashReportCategory.setDetail("Tag type found", () -> this.tags.get(string).getType().getName());
        crashReportCategory.setDetail("Tag type expected", tagType::getName);
        crashReportCategory.setDetail("Tag name", string);
        return crashReport;
    }

    protected CompoundTag shallowCopy() {
        return new CompoundTag(new HashMap<String, Tag>(this.tags));
    }

    @Override
    public CompoundTag copy() {
        HashMap hashMap = Maps.newHashMap((Map)Maps.transformValues(this.tags, Tag::copy));
        return new CompoundTag(hashMap);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)object).tags);
    }

    public int hashCode() {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String string, Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() == 0) {
            return;
        }
        dataOutput.writeUTF(string);
        tag.write(dataOutput);
    }

    static Tag readNamedTagData(TagType<?> tagType, String string, DataInput dataInput, NbtAccounter nbtAccounter) {
        try {
            return tagType.load(dataInput, nbtAccounter);
        }
        catch (IOException iOException) {
            CrashReport crashReport = CrashReport.forThrowable(iOException, "Loading NBT data");
            CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
            crashReportCategory.setDetail("Tag name", string);
            crashReportCategory.setDetail("Tag type", tagType.getName());
            throw new ReportedNbtException(crashReport);
        }
    }

    public CompoundTag merge(CompoundTag compoundTag) {
        for (String string : compoundTag.tags.keySet()) {
            Tag tag = compoundTag.tags.get(string);
            if (tag.getId() == 10) {
                if (this.contains(string, 10)) {
                    CompoundTag compoundTag2 = this.getCompound(string);
                    compoundTag2.merge((CompoundTag)tag);
                    continue;
                }
                this.put(string, tag.copy());
                continue;
            }
            this.put(string, tag.copy());
        }
        return this;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitCompound(this);
    }

    protected Set<Map.Entry<String, Tag>> entrySet() {
        return this.tags.entrySet();
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        block14: for (Map.Entry<String, Tag> entry : this.tags.entrySet()) {
            Tag tag = entry.getValue();
            TagType<?> tagType = tag.getType();
            StreamTagVisitor.EntryResult entryResult = streamTagVisitor.visitEntry(tagType);
            switch (entryResult) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
                case SKIP: {
                    continue block14;
                }
            }
            entryResult = streamTagVisitor.visitEntry(tagType, entry.getKey());
            switch (entryResult) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
                case SKIP: {
                    continue block14;
                }
            }
            StreamTagVisitor.ValueResult valueResult = tag.accept(streamTagVisitor);
            switch (valueResult) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
            }
        }
        return streamTagVisitor.visitContainerEnd();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

