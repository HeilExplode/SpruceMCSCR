/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class ChestedHorsesInventoryZeroIndexingFix
extends DataFix {
    public ChestedHorsesInventoryZeroIndexingFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.typeFinder((Type)this.getInputSchema().getType(References.ITEM_STACK));
        Type type = this.getInputSchema().getType(References.ENTITY);
        return TypeRewriteRule.seq((TypeRewriteRule)this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:llama"), (TypeRewriteRule[])new TypeRewriteRule[]{this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:trader_llama"), this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:mule"), this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:donkey")});
    }

    private TypeRewriteRule horseLikeInventoryIndexingFixer(OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticFinder, Type<?> type, String string) {
        Type type2 = this.getInputSchema().getChoiceType(References.ENTITY, string);
        OpticFinder opticFinder2 = DSL.namedChoice((String)string, (Type)type2);
        OpticFinder opticFinder3 = type2.findField("Items");
        return this.fixTypeEverywhereTyped("Fix non-zero indexing in chest horse type " + string, type, typed -> typed.updateTyped(opticFinder2, typed2 -> typed2.updateTyped(opticFinder3, typed -> typed.update(opticFinder, pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> pair.mapSecond(dynamic2 -> dynamic2.update("Slot", dynamic -> dynamic.createByte((byte)(dynamic.asInt(2) - 2))))))))));
    }
}

