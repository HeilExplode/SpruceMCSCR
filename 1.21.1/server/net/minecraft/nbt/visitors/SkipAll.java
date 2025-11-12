/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.visitors;

import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public interface SkipAll
extends StreamTagVisitor {
    public static final SkipAll INSTANCE = new SkipAll(){};

    @Override
    default public StreamTagVisitor.ValueResult visitEnd() {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(String string) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(byte by) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(short s) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(int n) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(long l) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(float f) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(double d) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(byte[] byArray) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(int[] nArray) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(long[] lArray) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visitList(TagType<?> tagType, int n) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.EntryResult visitElement(TagType<?> tagType, int n) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default public StreamTagVisitor.ValueResult visitContainerEnd() {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }
}

