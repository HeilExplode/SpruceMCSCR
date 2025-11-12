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
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class ProjectileStoredWeaponFix
extends DataFix {
    public ProjectileStoredWeaponFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ENTITY);
        Type type2 = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("Fix Arrow stored weapon", type, type2, ExtraDataFixUtils.chainAllFilters(this.fixChoice("minecraft:arrow"), this.fixChoice("minecraft:spectral_arrow")));
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String string) {
        Type type = this.getInputSchema().getChoiceType(References.ENTITY, string);
        Type type2 = this.getOutputSchema().getChoiceType(References.ENTITY, string);
        return ProjectileStoredWeaponFix.fixChoiceCap(string, type, type2);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String string, Type<?> type, Type<T> type2) {
        OpticFinder opticFinder = DSL.namedChoice((String)string, type);
        return typed2 -> typed2.updateTyped(opticFinder, type2, typed -> Util.writeAndReadTypedOrThrow(typed, type2, UnaryOperator.identity()));
    }
}

