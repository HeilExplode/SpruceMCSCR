/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class AbstractArrowPickupFix
extends DataFix {
    public AbstractArrowPickupFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        return this.fixTypeEverywhereTyped("AbstractArrowPickupFix", schema.getType(References.ENTITY), this::updateProjectiles);
    }

    private Typed<?> updateProjectiles(Typed<?> typed) {
        typed = this.updateEntity(typed, "minecraft:arrow", AbstractArrowPickupFix::updatePickup);
        typed = this.updateEntity(typed, "minecraft:spectral_arrow", AbstractArrowPickupFix::updatePickup);
        typed = this.updateEntity(typed, "minecraft:trident", AbstractArrowPickupFix::updatePickup);
        return typed;
    }

    private static Dynamic<?> updatePickup(Dynamic<?> dynamic) {
        if (dynamic.get("pickup").result().isPresent()) {
            return dynamic;
        }
        boolean bl = dynamic.get("player").asBoolean(true);
        return dynamic.set("pickup", dynamic.createByte((byte)(bl ? 1 : 0))).remove("player");
    }

    private Typed<?> updateEntity(Typed<?> typed2, String string, Function<Dynamic<?>, Dynamic<?>> function) {
        Type type = this.getInputSchema().getChoiceType(References.ENTITY, string);
        Type type2 = this.getOutputSchema().getChoiceType(References.ENTITY, string);
        return typed2.updateTyped(DSL.namedChoice((String)string, (Type)type), type2, typed -> typed.update(DSL.remainderFinder(), function));
    }
}

