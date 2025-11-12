/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemapChunkStatusFix
extends DataFix {
    private final String name;
    private final UnaryOperator<String> mapper;

    public RemapChunkStatusFix(Schema schema, String string, UnaryOperator<String> unaryOperator) {
        super(schema, false);
        this.name = string;
        this.mapper = unaryOperator;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.CHUNK), typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("Status", this::fixStatus).update("below_zero_retrogen", dynamic -> dynamic.update("target_status", this::fixStatus))));
    }

    private <T> Dynamic<T> fixStatus(Dynamic<T> dynamic) {
        Optional<Dynamic> optional = dynamic.asString().result().map(NamespacedSchema::ensureNamespaced).map(this.mapper).map(arg_0 -> dynamic.createString(arg_0));
        return (Dynamic)DataFixUtils.orElse(optional, dynamic);
    }
}

