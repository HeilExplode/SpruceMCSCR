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
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemoveEmptyItemInBrushableBlockFix
extends NamedEntityWriteReadFix {
    public RemoveEmptyItemInBrushableBlockFix(Schema schema) {
        super(schema, false, "RemoveEmptyItemInSuspiciousBlockFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional optional = dynamic.get("item").result();
        if (optional.isPresent() && RemoveEmptyItemInBrushableBlockFix.isEmptyStack((Dynamic)optional.get())) {
            return dynamic.remove("item");
        }
        return dynamic;
    }

    private static boolean isEmptyStack(Dynamic<?> dynamic) {
        String string = NamespacedSchema.ensureNamespaced(dynamic.get("id").asString("minecraft:air"));
        int n = dynamic.get("count").asInt(0);
        return string.equals("minecraft:air") || n == 0;
    }
}

