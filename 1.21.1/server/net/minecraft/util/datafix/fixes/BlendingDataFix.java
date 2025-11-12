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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlendingDataFix
extends DataFix {
    private final String name;
    private static final Set<String> STATUSES_TO_SKIP_BLENDING = Set.of("minecraft:empty", "minecraft:structure_starts", "minecraft:structure_references", "minecraft:biomes");

    public BlendingDataFix(Schema schema) {
        super(schema, false);
        this.name = "Blending Data Fix v" + schema.getVersionKey();
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getOutputSchema().getType(References.CHUNK);
        return this.fixTypeEverywhereTyped(this.name, type, typed -> typed.update(DSL.remainderFinder(), dynamic -> BlendingDataFix.updateChunkTag(dynamic, dynamic.get("__context"))));
    }

    private static Dynamic<?> updateChunkTag(Dynamic<?> dynamic, OptionalDynamic<?> optionalDynamic) {
        dynamic = dynamic.remove("blending_data");
        boolean bl = "minecraft:overworld".equals(optionalDynamic.get("dimension").asString().result().orElse(""));
        Optional optional = dynamic.get("Status").result();
        if (bl && optional.isPresent()) {
            Dynamic dynamic2;
            String string;
            String string2 = NamespacedSchema.ensureNamespaced(((Dynamic)optional.get()).asString("empty"));
            Optional optional2 = dynamic.get("below_zero_retrogen").result();
            if (!STATUSES_TO_SKIP_BLENDING.contains(string2)) {
                dynamic = BlendingDataFix.updateBlendingData(dynamic, 384, -64);
            } else if (optional2.isPresent() && !STATUSES_TO_SKIP_BLENDING.contains(string = NamespacedSchema.ensureNamespaced((dynamic2 = (Dynamic)optional2.get()).get("target_status").asString("empty")))) {
                dynamic = BlendingDataFix.updateBlendingData(dynamic, 256, 0);
            }
        }
        return dynamic;
    }

    private static Dynamic<?> updateBlendingData(Dynamic<?> dynamic, int n, int n2) {
        return dynamic.set("blending_data", dynamic.createMap(Map.of(dynamic.createString("min_section"), dynamic.createInt(SectionPos.blockToSectionCoord(n2)), dynamic.createString("max_section"), dynamic.createInt(SectionPos.blockToSectionCoord(n2 + n)))));
    }
}

