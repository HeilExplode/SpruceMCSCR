/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.FieldTree;

public class SkipFields
extends CollectToTag {
    private final Deque<FieldTree> stack = new ArrayDeque<FieldTree>();

    public SkipFields(FieldSelector ... fieldSelectorArray) {
        FieldTree fieldTree = FieldTree.createRoot();
        for (FieldSelector fieldSelector : fieldSelectorArray) {
            fieldTree.addEntry(fieldSelector);
        }
        this.stack.push(fieldTree);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
        FieldTree fieldTree;
        FieldTree fieldTree2 = this.stack.element();
        if (fieldTree2.isSelected(tagType, string)) {
            return StreamTagVisitor.EntryResult.SKIP;
        }
        if (tagType == CompoundTag.TYPE && (fieldTree = fieldTree2.fieldsToRecurse().get(string)) != null) {
            this.stack.push(fieldTree);
        }
        return super.visitEntry(tagType, string);
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }
        return super.visitContainerEnd();
    }
}

