/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class TippedArrowPotionToItemFix
extends NamedEntityWriteReadFix {
    public TippedArrowPotionToItemFix(Schema schema) {
        super(schema, false, "TippedArrowPotionToItemFix", References.ENTITY, "minecraft:arrow");
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> dynamic2) {
        Optional optional = dynamic2.get("Potion").result();
        Optional optional2 = dynamic2.get("custom_potion_effects").result();
        Optional optional3 = dynamic2.get("Color").result();
        if (optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty()) {
            return dynamic2;
        }
        return dynamic2.remove("Potion").remove("custom_potion_effects").remove("Color").update("item", dynamic -> {
            Dynamic dynamic2 = dynamic.get("tag").orElseEmptyMap();
            if (optional.isPresent()) {
                dynamic2 = dynamic2.set("Potion", (Dynamic)optional.get());
            }
            if (optional2.isPresent()) {
                dynamic2 = dynamic2.set("custom_potion_effects", (Dynamic)optional2.get());
            }
            if (optional3.isPresent()) {
                dynamic2 = dynamic2.set("CustomPotionColor", (Dynamic)optional3.get());
            }
            return dynamic.set("tag", dynamic2);
        });
    }
}

