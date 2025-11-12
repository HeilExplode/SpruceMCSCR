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
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class PrimedTntBlockStateFixer
extends NamedEntityWriteReadFix {
    public PrimedTntBlockStateFixer(Schema schema) {
        super(schema, true, "PrimedTnt BlockState fixer", References.ENTITY, "minecraft:tnt");
    }

    private static <T> Dynamic<T> renameFuse(Dynamic<T> dynamic) {
        Optional optional = dynamic.get("Fuse").get().result();
        if (optional.isPresent()) {
            return dynamic.set("fuse", (Dynamic)optional.get());
        }
        return dynamic;
    }

    private static <T> Dynamic<T> insertBlockState(Dynamic<T> dynamic) {
        return dynamic.set("block_state", dynamic.createMap(Map.of(dynamic.createString("Name"), dynamic.createString("minecraft:tnt"))));
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        return PrimedTntBlockStateFixer.renameFuse(PrimedTntBlockStateFixer.insertBlockState(dynamic));
    }
}

