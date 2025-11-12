/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class LevelLegacyWorldGenSettingsFix
extends DataFix {
    private static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
    private static final List<String> OLD_SETTINGS_KEYS = List.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");

    public LevelLegacyWorldGenSettingsFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelLegacyWorldGenSettingsFix", this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Dynamic dynamic2 = dynamic.get(WORLD_GEN_SETTINGS).orElseEmptyMap();
            for (String string : OLD_SETTINGS_KEYS) {
                Optional optional = dynamic.get(string).result();
                if (!optional.isPresent()) continue;
                dynamic = dynamic.remove(string);
                dynamic2 = dynamic2.set(string, (Dynamic)optional.get());
            }
            return dynamic.set(WORLD_GEN_SETTINGS, dynamic2);
        }));
    }
}

