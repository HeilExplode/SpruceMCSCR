/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
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

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class FixProjectileStoredItem
extends DataFix {
    private static final String EMPTY_POTION = "minecraft:empty";

    public FixProjectileStoredItem(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ENTITY);
        Type type2 = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("Fix AbstractArrow item type", type, type2, ExtraDataFixUtils.chainAllFilters(this.fixChoice("minecraft:trident", FixProjectileStoredItem::castUnchecked), this.fixChoice("minecraft:arrow", FixProjectileStoredItem::fixArrow), this.fixChoice("minecraft:spectral_arrow", FixProjectileStoredItem::fixSpectralArrow)));
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String string, SubFixer<?> subFixer) {
        Type type = this.getInputSchema().getChoiceType(References.ENTITY, string);
        Type type2 = this.getOutputSchema().getChoiceType(References.ENTITY, string);
        return FixProjectileStoredItem.fixChoiceCap(string, subFixer, type, type2);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String string, SubFixer<?> subFixer, Type<?> type, Type<T> type2) {
        OpticFinder opticFinder = DSL.namedChoice((String)string, type);
        SubFixer<?> subFixer2 = subFixer;
        return typed2 -> typed2.updateTyped(opticFinder, type2, typed -> subFixer2.fix((Typed<?>)typed, type2));
    }

    private static <T> Typed<T> fixArrow(Typed<?> typed, Type<T> type) {
        return Util.writeAndReadTypedOrThrow(typed, type, dynamic -> dynamic.set("item", FixProjectileStoredItem.createItemStack(dynamic, FixProjectileStoredItem.getArrowType(dynamic))));
    }

    private static String getArrowType(Dynamic<?> dynamic) {
        return dynamic.get("Potion").asString(EMPTY_POTION).equals(EMPTY_POTION) ? "minecraft:arrow" : "minecraft:tipped_arrow";
    }

    private static <T> Typed<T> fixSpectralArrow(Typed<?> typed, Type<T> type) {
        return Util.writeAndReadTypedOrThrow(typed, type, dynamic -> dynamic.set("item", FixProjectileStoredItem.createItemStack(dynamic, "minecraft:spectral_arrow")));
    }

    private static Dynamic<?> createItemStack(Dynamic<?> dynamic, String string) {
        return dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("id"), (Object)dynamic.createString(string), (Object)dynamic.createString("Count"), (Object)dynamic.createInt(1)));
    }

    private static <T> Typed<T> castUnchecked(Typed<?> typed, Type<T> type) {
        return new Typed(type, typed.getOps(), typed.getValue());
    }

    static interface SubFixer<F> {
        public Typed<F> fix(Typed<?> var1, Type<F> var2);
    }
}

