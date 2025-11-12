/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class AreaEffectCloudPotionFix
extends NamedEntityFix {
    public AreaEffectCloudPotionFix(Schema schema) {
        super(schema, false, "AreaEffectCloudPotionFix", References.ENTITY, "minecraft:area_effect_cloud");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional optional = dynamic.get("Color").result();
        Optional optional2 = dynamic.get("effects").result();
        Optional optional3 = dynamic.get("Potion").result();
        dynamic = dynamic.remove("Color").remove("effects").remove("Potion");
        if (optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty()) {
            return dynamic;
        }
        Dynamic dynamic2 = dynamic.emptyMap();
        if (optional.isPresent()) {
            dynamic2 = dynamic2.set("custom_color", (Dynamic)optional.get());
        }
        if (optional2.isPresent()) {
            dynamic2 = dynamic2.set("custom_effects", (Dynamic)optional2.get());
        }
        if (optional3.isPresent()) {
            dynamic2 = dynamic2.set("potion", (Dynamic)optional3.get());
        }
        return dynamic.set("potion_contents", dynamic2);
    }
}

