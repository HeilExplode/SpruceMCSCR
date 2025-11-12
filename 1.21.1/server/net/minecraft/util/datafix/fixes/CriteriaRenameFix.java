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

public class CriteriaRenameFix
extends DataFix {
    private final String name;
    private final String advancementId;
    private final UnaryOperator<String> conversions;

    public CriteriaRenameFix(Schema schema, String string, String string2, UnaryOperator<String> unaryOperator) {
        super(schema, false);
        this.name = string;
        this.advancementId = string2;
        this.conversions = unaryOperator;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.ADVANCEMENTS), typed -> typed.update(DSL.remainderFinder(), this::fixAdvancements));
    }

    private Dynamic<?> fixAdvancements(Dynamic<?> dynamic) {
        return dynamic.update(this.advancementId, dynamic2 -> dynamic2.update("criteria", dynamic -> dynamic.updateMapValues(pair -> pair.mapFirst(dynamic -> (Dynamic)DataFixUtils.orElse((Optional)dynamic.asString().map(string -> dynamic.createString((String)this.conversions.apply((String)string))).result(), (Object)dynamic)))));
    }
}

