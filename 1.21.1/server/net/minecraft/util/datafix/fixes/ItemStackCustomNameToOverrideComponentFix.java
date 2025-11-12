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
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackCustomNameToOverrideComponentFix
extends DataFix {
    private static final Set<String> MAP_NAMES = Set.of("filled_map.buried_treasure", "filled_map.explorer_jungle", "filled_map.explorer_swamp", "filled_map.mansion", "filled_map.monument", "filled_map.trial_chambers", "filled_map.village_desert", "filled_map.village_plains", "filled_map.village_savanna", "filled_map.village_snowy", "filled_map.village_taiga");

    public ItemStackCustomNameToOverrideComponentFix(Schema schema) {
        super(schema, false);
    }

    public final TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder opticFinder2 = type.findField("components");
        return this.fixTypeEverywhereTyped("ItemStack custom_name to item_name component fix", type, typed2 -> {
            Optional optional = typed2.getOptional(opticFinder);
            Optional<String> optional2 = optional.map(Pair::getSecond);
            if (optional2.filter(string -> string.equals("minecraft:white_banner")).isPresent()) {
                return typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), ItemStackCustomNameToOverrideComponentFix::fixBanner));
            }
            if (optional2.filter(string -> string.equals("minecraft:filled_map")).isPresent()) {
                return typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), ItemStackCustomNameToOverrideComponentFix::fixMap));
            }
            return typed2;
        });
    }

    private static <T> Dynamic<T> fixMap(Dynamic<T> dynamic) {
        return ItemStackCustomNameToOverrideComponentFix.fixCustomName(dynamic, MAP_NAMES::contains);
    }

    private static <T> Dynamic<T> fixBanner(Dynamic<T> dynamic) {
        return ItemStackCustomNameToOverrideComponentFix.fixCustomName(dynamic, string -> string.equals("block.minecraft.ominous_banner"));
    }

    private static <T> Dynamic<T> fixCustomName(Dynamic<T> dynamic, Predicate<String> predicate) {
        OptionalDynamic optionalDynamic = dynamic.get("minecraft:custom_name");
        Optional<String> optional = optionalDynamic.asString().result().flatMap(ComponentDataFixUtils::extractTranslationString).filter(predicate);
        if (optional.isPresent()) {
            return dynamic.renameField("minecraft:custom_name", "minecraft:item_name");
        }
        return dynamic;
    }
}

