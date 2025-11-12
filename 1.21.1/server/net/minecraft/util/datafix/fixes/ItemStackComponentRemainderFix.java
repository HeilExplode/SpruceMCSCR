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
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public abstract class ItemStackComponentRemainderFix
extends DataFix {
    private final String name;
    private final String componentId;
    private final String newComponentId;

    public ItemStackComponentRemainderFix(Schema schema, String string, String string2) {
        this(schema, string, string2, string2);
    }

    public ItemStackComponentRemainderFix(Schema schema, String string, String string2, String string3) {
        super(schema, false);
        this.name = string;
        this.componentId = string2;
        this.newComponentId = string3;
    }

    public final TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder = type.findField("components");
        return this.fixTypeEverywhereTyped(this.name, type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.renameAndFixField(this.componentId, this.newComponentId, this::fixComponent))));
    }

    protected abstract <T> Dynamic<T> fixComponent(Dynamic<T> var1);
}

