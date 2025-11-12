/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityGoatMissingStateFix
extends NamedEntityFix {
    public EntityGoatMissingStateFix(Schema schema) {
        super(schema, false, "EntityGoatMissingStateFix", References.ENTITY, "minecraft:goat");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.set("HasLeftHorn", dynamic.createBoolean(true)).set("HasRightHorn", dynamic.createBoolean(true)));
    }
}

