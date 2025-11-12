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
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class BannerEntityCustomNameToOverrideComponentFix
extends DataFix {
    public BannerEntityCustomNameToOverrideComponentFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder opticFinder = type.findField("components");
        return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", type, typed -> {
            Object object = ((Pair)typed.get(taggedChoiceType.finder())).getFirst();
            return object.equals("minecraft:banner") ? this.fix((Typed<?>)typed, (OpticFinder<?>)opticFinder) : typed;
        });
    }

    private Typed<?> fix(Typed<?> typed, OpticFinder<?> opticFinder) {
        Dynamic dynamic2 = (Dynamic)typed.getOptional(DSL.remainderFinder()).orElseThrow();
        OptionalDynamic optionalDynamic = dynamic2.get("CustomName");
        boolean bl = optionalDynamic.asString().result().flatMap(ComponentDataFixUtils::extractTranslationString).filter(string -> string.equals("block.minecraft.ominous_banner")).isPresent();
        if (bl) {
            Typed typed2 = typed.getOrCreateTyped(opticFinder).update(DSL.remainderFinder(), dynamic -> dynamic.set("minecraft:item_name", (Dynamic)optionalDynamic.result().get()).set("minecraft:hide_additional_tooltip", dynamic.createMap(Map.of())));
            return typed.set(opticFinder, typed2).set(DSL.remainderFinder(), (Object)dynamic2.remove("CustomName"));
        }
        return typed;
    }
}

