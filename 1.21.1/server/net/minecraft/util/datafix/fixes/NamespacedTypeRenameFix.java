/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NamespacedTypeRenameFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference type;
    private final UnaryOperator<String> renamer;

    public NamespacedTypeRenameFix(Schema schema, String string, DSL.TypeReference typeReference, UnaryOperator<String> unaryOperator) {
        super(schema, false);
        this.name = string;
        this.type = typeReference;
        this.renamer = unaryOperator;
    }

    protected TypeRewriteRule makeRule() {
        Type type = DSL.named((String)this.type.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(type, this.getInputSchema().getType(this.type))) {
            throw new IllegalStateException("\"" + this.type.typeName() + "\" is not what was expected.");
        }
        return this.fixTypeEverywhere(this.name, type, dynamicOps -> pair -> pair.mapSecond(this.renamer));
    }
}

