/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class FixWolfHealth
extends NamedEntityFix {
    private static final String WOLF_ID = "minecraft:wolf";
    private static final String WOLF_HEALTH = "minecraft:generic.max_health";

    public FixWolfHealth(Schema schema) {
        super(schema, false, "FixWolfHealth", References.ENTITY, WOLF_ID);
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> {
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            dynamic2 = dynamic2.update("Attributes", dynamic -> dynamic.createList(dynamic.asStream().map(dynamic2 -> {
                if (WOLF_HEALTH.equals(NamespacedSchema.ensureNamespaced(dynamic2.get("Name").asString("")))) {
                    return dynamic2.update("Base", dynamic -> {
                        if (dynamic.asDouble(0.0) == 20.0) {
                            mutableBoolean.setTrue();
                            return dynamic.createDouble(40.0);
                        }
                        return dynamic;
                    });
                }
                return dynamic2;
            })));
            if (mutableBoolean.isTrue()) {
                dynamic2 = dynamic2.update("Health", dynamic -> dynamic.createFloat(dynamic.asFloat(0.0f) * 2.0f));
            }
            return dynamic2;
        });
    }
}

