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
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class MapBannerBlockPosFormatFix
extends DataFix {
    public MapBannerBlockPosFormatFix(Schema schema) {
        super(schema, false);
    }

    private static <T> Dynamic<T> fixMapSavedData(Dynamic<T> dynamic) {
        return dynamic.update("banners", dynamic2 -> dynamic2.createList(dynamic2.asStream().map(dynamic -> dynamic.update("Pos", ExtraDataFixUtils::fixBlockPos))));
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("MapBannerBlockPosFormatFix", this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("data", MapBannerBlockPosFormatFix::fixMapSavedData)));
    }
}

