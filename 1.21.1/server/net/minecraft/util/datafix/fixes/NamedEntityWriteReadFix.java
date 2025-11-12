/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.RewriteResult
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.View
 *  com.mojang.datafixers.functions.PointFreeRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.BitSet;
import net.minecraft.Util;

public abstract class NamedEntityWriteReadFix
extends DataFix {
    private final String name;
    private final String entityName;
    private final DSL.TypeReference type;

    public NamedEntityWriteReadFix(Schema schema, boolean bl, String string, DSL.TypeReference typeReference, String string2) {
        super(schema, bl);
        this.name = string;
        this.type = typeReference;
        this.entityName = string2;
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(this.type);
        Type type2 = this.getInputSchema().getChoiceType(this.type, this.entityName);
        Type type3 = this.getOutputSchema().getType(this.type);
        Type type4 = this.getOutputSchema().getChoiceType(this.type, this.entityName);
        OpticFinder opticFinder = DSL.namedChoice((String)this.entityName, (Type)type2);
        Type type5 = type2.all(NamedEntityWriteReadFix.typePatcher(type, type3), true, false).view().newType();
        return this.fix(type, type3, opticFinder, type4, type5);
    }

    private <S, T, A, B> TypeRewriteRule fix(Type<S> type, Type<T> type2, OpticFinder<A> opticFinder, Type<B> type3, Type<?> type4) {
        return this.fixTypeEverywhere(this.name, type, type2, dynamicOps -> object2 -> {
            Typed typed = new Typed(type, dynamicOps, object2);
            return typed.update(opticFinder, type3, object -> {
                Typed typed = new Typed(type4, dynamicOps, object);
                return Util.writeAndReadTypedOrThrow(typed, type3, this::fix).getValue();
            }).getValue();
        });
    }

    private static <A, B> TypeRewriteRule typePatcher(Type<A> type, Type<B> type2) {
        RewriteResult rewriteResult = RewriteResult.create((View)View.create((String)"Patcher", type, type2, dynamicOps -> object -> {
            throw new UnsupportedOperationException();
        }), (BitSet)new BitSet());
        return TypeRewriteRule.everywhere((TypeRewriteRule)TypeRewriteRule.ifSame(type, (RewriteResult)rewriteResult), (PointFreeRule)PointFreeRule.nop(), (boolean)true, (boolean)true);
    }

    protected abstract <T> Dynamic<T> fix(Dynamic<T> var1);
}

