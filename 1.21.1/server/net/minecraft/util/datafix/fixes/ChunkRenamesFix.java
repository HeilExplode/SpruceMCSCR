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
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class ChunkRenamesFix
extends DataFix {
    public ChunkRenamesFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder opticFinder = type.findField("Level");
        OpticFinder opticFinder2 = opticFinder.type().findField("Structures");
        Type type2 = this.getOutputSchema().getType(References.CHUNK);
        Type type3 = type2.findFieldType("structures");
        return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", type, type2, typed2 -> {
            Typed typed3 = typed2.getTyped(opticFinder);
            Typed<?> typed4 = ChunkRenamesFix.appendChunkName(typed3);
            typed4 = typed4.set(DSL.remainderFinder(), ChunkRenamesFix.mergeRemainders(typed2, (Dynamic)typed3.get(DSL.remainderFinder())));
            typed4 = ChunkRenamesFix.renameField(typed4, "TileEntities", "block_entities");
            typed4 = ChunkRenamesFix.renameField(typed4, "TileTicks", "block_ticks");
            typed4 = ChunkRenamesFix.renameField(typed4, "Entities", "entities");
            typed4 = ChunkRenamesFix.renameField(typed4, "Sections", "sections");
            typed4 = typed4.updateTyped(opticFinder2, type3, typed -> ChunkRenamesFix.renameField(typed, "Starts", "starts"));
            typed4 = ChunkRenamesFix.renameField(typed4, "Structures", "structures");
            return typed4.update(DSL.remainderFinder(), dynamic -> dynamic.remove("Level"));
        });
    }

    private static Typed<?> renameField(Typed<?> typed, String string, String string2) {
        return ChunkRenamesFix.renameFieldHelper(typed, string, string2, typed.getType().findFieldType(string)).update(DSL.remainderFinder(), dynamic -> dynamic.remove(string));
    }

    private static <A> Typed<?> renameFieldHelper(Typed<?> typed, String string, String string2, Type<A> type) {
        Type type2 = DSL.optional((Type)DSL.field((String)string, type));
        Type type3 = DSL.optional((Type)DSL.field((String)string2, type));
        return typed.update(type2.finder(), type3, Function.identity());
    }

    private static <A> Typed<Pair<String, A>> appendChunkName(Typed<A> typed) {
        return new Typed(DSL.named((String)"chunk", (Type)typed.getType()), typed.getOps(), (Object)Pair.of((Object)"chunk", (Object)typed.getValue()));
    }

    private static <T> Dynamic<T> mergeRemainders(Typed<?> typed, Dynamic<T> dynamic) {
        DynamicOps dynamicOps = dynamic.getOps();
        Dynamic dynamic2 = ((Dynamic)typed.get(DSL.remainderFinder())).convert(dynamicOps);
        DataResult dataResult = dynamicOps.getMap(dynamic.getValue()).flatMap(mapLike -> dynamicOps.mergeToMap(dynamic2.getValue(), mapLike));
        return dataResult.result().map(object -> new Dynamic(dynamicOps, object)).orElse(dynamic);
    }
}

