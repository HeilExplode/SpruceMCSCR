/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class HorseBodyArmorItemFix
extends NamedEntityWriteReadFix {
    private final String previousBodyArmorTag;
    private final boolean clearArmorItems;

    public HorseBodyArmorItemFix(Schema schema, String string, String string2, boolean bl) {
        super(schema, true, "Horse armor fix for " + string, References.ENTITY, string);
        this.previousBodyArmorTag = string2;
        this.clearArmorItems = bl;
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional optional = dynamic.get(this.previousBodyArmorTag).result();
        if (optional.isPresent()) {
            Dynamic dynamic3 = (Dynamic)optional.get();
            Dynamic dynamic4 = dynamic.remove(this.previousBodyArmorTag);
            if (this.clearArmorItems) {
                dynamic4 = dynamic4.update("ArmorItems", dynamic2 -> dynamic2.createList(Streams.mapWithIndex((Stream)dynamic2.asStream(), (dynamic, l) -> l == 2L ? dynamic.emptyMap() : dynamic)));
                dynamic4 = dynamic4.update("ArmorDropChances", dynamic2 -> dynamic2.createList(Streams.mapWithIndex((Stream)dynamic2.asStream(), (dynamic, l) -> l == 2L ? dynamic.createFloat(0.085f) : dynamic)));
            }
            dynamic4 = dynamic4.set("body_armor_item", dynamic3);
            dynamic4 = dynamic4.set("body_armor_drop_chance", dynamic.createFloat(2.0f));
            return dynamic4;
        }
        return dynamic;
    }
}

