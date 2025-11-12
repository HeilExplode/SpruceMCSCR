/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;

public class FilteredBooksFix
extends ItemStackTagFix {
    public FilteredBooksFix(Schema schema) {
        super(schema, "Remove filtered text from books", string -> string.equals("minecraft:writable_book") || string.equals("minecraft:written_book"));
    }

    @Override
    protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic) {
        return dynamic.remove("filtered_title").remove("filtered_pages");
    }
}

