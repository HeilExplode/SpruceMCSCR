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
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class StatsRenameFix
extends DataFix {
    private final String name;
    private final Map<String, String> renames;

    public StatsRenameFix(Schema schema, String string, Map<String, String> map) {
        super(schema, false);
        this.name = string;
        this.renames = map;
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.createStatRule(), (TypeRewriteRule)this.createCriteriaRule());
    }

    private TypeRewriteRule createCriteriaRule() {
        Type type = this.getOutputSchema().getType(References.OBJECTIVE);
        Type type2 = this.getInputSchema().getType(References.OBJECTIVE);
        OpticFinder opticFinder = type2.findField("CriteriaType");
        TaggedChoice.TaggedChoiceType taggedChoiceType = (TaggedChoice.TaggedChoiceType)opticFinder.type().findChoiceType("type", -1).orElseThrow(() -> new IllegalStateException("Can't find choice type for criteria"));
        Type type3 = (Type)taggedChoiceType.types().get("minecraft:custom");
        if (type3 == null) {
            throw new IllegalStateException("Failed to find custom criterion type variant");
        }
        OpticFinder opticFinder2 = DSL.namedChoice((String)"minecraft:custom", (Type)type3);
        OpticFinder opticFinder3 = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        return this.fixTypeEverywhereTyped(this.name, type2, type, typed -> typed.updateTyped(opticFinder, typed2 -> typed2.updateTyped(opticFinder2, typed -> typed.update(opticFinder3, string -> this.renames.getOrDefault(string, (String)string)))));
    }

    private TypeRewriteRule createStatRule() {
        Type type = this.getOutputSchema().getType(References.STATS);
        Type type2 = this.getInputSchema().getType(References.STATS);
        OpticFinder opticFinder = type2.findField("stats");
        OpticFinder opticFinder2 = opticFinder.type().findField("minecraft:custom");
        OpticFinder opticFinder3 = NamespacedSchema.namespacedString().finder();
        return this.fixTypeEverywhereTyped(this.name, type2, type, typed -> typed.updateTyped(opticFinder, typed2 -> typed2.updateTyped(opticFinder2, typed -> typed.update(opticFinder3, string -> this.renames.getOrDefault(string, (String)string)))));
    }
}

