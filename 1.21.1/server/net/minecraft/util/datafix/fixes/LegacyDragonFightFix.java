/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class LegacyDragonFightFix
extends DataFix {
    public LegacyDragonFightFix(Schema schema) {
        super(schema, false);
    }

    private static <T> Dynamic<T> fixDragonFight(Dynamic<T> dynamic) {
        return dynamic.update("ExitPortalLocation", ExtraDataFixUtils::fixBlockPos);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LegacyDragonFightFix", this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            OptionalDynamic optionalDynamic = dynamic.get("DragonFight");
            if (optionalDynamic.result().isPresent()) {
                return dynamic;
            }
            Dynamic dynamic2 = dynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap();
            return dynamic.set("DragonFight", LegacyDragonFightFix.fixDragonFight(dynamic2));
        }));
    }
}

