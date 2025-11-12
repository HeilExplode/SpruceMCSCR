/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class OptionsMenuBlurrinessFix
extends DataFix {
    public OptionsMenuBlurrinessFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsMenuBlurrinessFix", this.getInputSchema().getType(References.OPTIONS), typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("menuBackgroundBlurriness", dynamic -> dynamic.createInt(this.convertToIntRange(dynamic.asString("0.5"))))));
    }

    private int convertToIntRange(String string) {
        try {
            return Math.round(Float.parseFloat(string) * 10.0f);
        }
        catch (NumberFormatException numberFormatException) {
            return 5;
        }
    }
}

