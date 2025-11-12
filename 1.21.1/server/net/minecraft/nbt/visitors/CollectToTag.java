/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;

public class CollectToTag
implements StreamTagVisitor {
    private String lastId = "";
    @Nullable
    private Tag rootTag;
    private final Deque<Consumer<Tag>> consumerStack = new ArrayDeque<Consumer<Tag>>();

    @Nullable
    public Tag getResult() {
        return this.rootTag;
    }

    protected int depth() {
        return this.consumerStack.size();
    }

    private void appendEntry(Tag tag) {
        this.consumerStack.getLast().accept(tag);
    }

    @Override
    public StreamTagVisitor.ValueResult visitEnd() {
        this.appendEntry(EndTag.INSTANCE);
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(String string) {
        this.appendEntry(StringTag.valueOf(string));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte by) {
        this.appendEntry(ByteTag.valueOf(by));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(short s) {
        this.appendEntry(ShortTag.valueOf(s));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int n) {
        this.appendEntry(IntTag.valueOf(n));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long l) {
        this.appendEntry(LongTag.valueOf(l));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(float f) {
        this.appendEntry(FloatTag.valueOf(f));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(double d) {
        this.appendEntry(DoubleTag.valueOf(d));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte[] byArray) {
        this.appendEntry(new ByteArrayTag(byArray));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int[] nArray) {
        this.appendEntry(new IntArrayTag(nArray));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long[] lArray) {
        this.appendEntry(new LongArrayTag(lArray));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitList(TagType<?> tagType, int n) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.EntryResult visitElement(TagType<?> tagType, int n) {
        this.enterContainerIfNeeded(tagType);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
        this.lastId = string;
        this.enterContainerIfNeeded(tagType);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    private void enterContainerIfNeeded(TagType<?> tagType) {
        if (tagType == ListTag.TYPE) {
            ListTag listTag = new ListTag();
            this.appendEntry(listTag);
            this.consumerStack.addLast(listTag::add);
        } else if (tagType == CompoundTag.TYPE) {
            CompoundTag compoundTag = new CompoundTag();
            this.appendEntry(compoundTag);
            this.consumerStack.addLast(tag -> compoundTag.put(this.lastId, (Tag)tag));
        }
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        this.consumerStack.removeLast();
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
        if (tagType == ListTag.TYPE) {
            ListTag listTag = new ListTag();
            this.rootTag = listTag;
            this.consumerStack.addLast(listTag::add);
        } else if (tagType == CompoundTag.TYPE) {
            CompoundTag compoundTag = new CompoundTag();
            this.rootTag = compoundTag;
            this.consumerStack.addLast(tag -> compoundTag.put(this.lastId, (Tag)tag));
        } else {
            this.consumerStack.addLast(tag -> {
                this.rootTag = tag;
            });
        }
        return StreamTagVisitor.ValueResult.CONTINUE;
    }
}

