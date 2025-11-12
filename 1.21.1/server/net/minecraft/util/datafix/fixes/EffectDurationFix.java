/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EffectDurationFix
extends DataFix {
    private static final Set<String> ITEM_TYPES = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

    public EffectDurationFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder opticFinder2 = type.findField("tag");
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("EffectDurationEntity", schema.getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), this::updateEntity)), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped("EffectDurationPlayer", schema.getType(References.PLAYER), typed -> typed.update(DSL.remainderFinder(), this::updateEntity)), this.fixTypeEverywhereTyped("EffectDurationItem", type, typed -> {
            Optional optional;
            Optional optional2 = typed.getOptional(opticFinder);
            if (optional2.filter(ITEM_TYPES::contains).isPresent() && (optional = typed.getOptionalTyped(opticFinder2)).isPresent()) {
                Dynamic dynamic = (Dynamic)((Typed)optional.get()).get(DSL.remainderFinder());
                Typed typed2 = ((Typed)optional.get()).set(DSL.remainderFinder(), (Object)dynamic.update("CustomPotionEffects", this::fix));
                return typed.set(opticFinder2, typed2);
            }
            return typed;
        })});
    }

    private Dynamic<?> fixEffect(Dynamic<?> dynamic) {
        return dynamic.update("FactorCalculationData", dynamic2 -> {
            int n = dynamic2.get("effect_changed_timestamp").asInt(-1);
            dynamic2 = dynamic2.remove("effect_changed_timestamp");
            int n2 = dynamic.get("Duration").asInt(-1);
            int n3 = n - n2;
            return dynamic2.set("ticks_active", dynamic2.createInt(n3));
        });
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.createList(dynamic.asStream().map(this::fixEffect));
    }

    private Dynamic<?> updateEntity(Dynamic<?> dynamic) {
        dynamic = dynamic.update("Effects", this::fix);
        dynamic = dynamic.update("ActiveEffects", this::fix);
        dynamic = dynamic.update("CustomPotionEffects", this::fix);
        return dynamic;
    }
}

