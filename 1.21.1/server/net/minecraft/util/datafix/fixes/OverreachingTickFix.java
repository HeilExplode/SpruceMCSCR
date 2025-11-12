/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class OverreachingTickFix
extends DataFix {
    public OverreachingTickFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder opticFinder = type.findField("block_ticks");
        return this.fixTypeEverywhereTyped("Handle ticks saved in the wrong chunk", type, typed -> {
            Optional optional = typed.getOptionalTyped(opticFinder);
            Optional optional2 = optional.isPresent() ? ((Typed)optional.get()).write().result() : Optional.empty();
            return typed.update(DSL.remainderFinder(), dynamic -> {
                int n = dynamic.get("xPos").asInt(0);
                int n2 = dynamic.get("zPos").asInt(0);
                Optional optional2 = dynamic.get("fluid_ticks").get().result();
                dynamic = OverreachingTickFix.extractOverreachingTicks(dynamic, n, n2, optional2, "neighbor_block_ticks");
                dynamic = OverreachingTickFix.extractOverreachingTicks(dynamic, n, n2, optional2, "neighbor_fluid_ticks");
                return dynamic;
            });
        });
    }

    private static Dynamic<?> extractOverreachingTicks(Dynamic<?> dynamic2, int n, int n2, Optional<? extends Dynamic<?>> optional, String string) {
        List<Dynamic> list;
        if (optional.isPresent() && !(list = optional.get().asStream().filter(dynamic -> {
            int n3 = dynamic.get("x").asInt(0);
            int n4 = dynamic.get("z").asInt(0);
            int n5 = Math.abs(n - (n3 >> 4));
            int n6 = Math.abs(n2 - (n4 >> 4));
            return (n5 != 0 || n6 != 0) && n5 <= 1 && n6 <= 1;
        }).toList()).isEmpty()) {
            dynamic2 = dynamic2.set("UpgradeData", dynamic2.get("UpgradeData").orElseEmptyMap().set(string, dynamic2.createList(list.stream())));
        }
        return dynamic2;
    }
}

