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
import net.minecraft.util.datafix.fixes.ItemStackComponentRemainderFix;

public class LodestoneCompassComponentFix
extends ItemStackComponentRemainderFix {
    public LodestoneCompassComponentFix(Schema schema) {
        super(schema, "LodestoneCompassComponentFix", "minecraft:lodestone_target", "minecraft:lodestone_tracker");
    }

    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        Optional optional = dynamic.get("pos").result();
        Optional optional2 = dynamic.get("dimension").result();
        dynamic = dynamic.remove("pos").remove("dimension");
        if (optional.isPresent() && optional2.isPresent()) {
            dynamic = dynamic.set("target", dynamic.emptyMap().set("pos", (Dynamic)optional.get()).set("dimension", (Dynamic)optional2.get()));
        }
        return dynamic;
    }
}

