/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class VariantRenameFix
extends NamedEntityFix {
    private final Map<String, String> renames;

    public VariantRenameFix(Schema schema, String string, DSL.TypeReference typeReference, String string2, Map<String, String> map) {
        super(schema, false, string, typeReference, string2);
        this.renames = map;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("variant", dynamic -> (Dynamic)DataFixUtils.orElse((Optional)dynamic.asString().map(string -> dynamic.createString(this.renames.getOrDefault(string, (String)string))).result(), (Object)dynamic)));
    }
}

