/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.fixes.References;

public class BlendingDataRemoveFromNetherEndFix
extends DataFix {
    public BlendingDataRemoveFromNetherEndFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getOutputSchema().getType(References.CHUNK);
        return this.fixTypeEverywhereTyped("BlendingDataRemoveFromNetherEndFix", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> BlendingDataRemoveFromNetherEndFix.updateChunkTag(dynamic, dynamic.get("__context"))));
    }

    private static Dynamic<?> updateChunkTag(Dynamic<?> dynamic, OptionalDynamic<?> optionalDynamic) {
        boolean bl = "minecraft:overworld".equals(optionalDynamic.get("dimension").asString().result().orElse(""));
        return bl ? dynamic : dynamic.remove("blending_data");
    }
}

