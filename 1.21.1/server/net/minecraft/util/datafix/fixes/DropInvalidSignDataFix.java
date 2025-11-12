/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class DropInvalidSignDataFix
extends NamedEntityFix {
    private static final String[] FIELDS_TO_DROP = new String[]{"Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText"};

    public DropInvalidSignDataFix(Schema schema, String string, String string2) {
        super(schema, false, string, References.BLOCK_ENTITY, string2);
    }

    private static <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        dynamic = dynamic.update("front_text", DropInvalidSignDataFix::fixText);
        dynamic = dynamic.update("back_text", DropInvalidSignDataFix::fixText);
        for (String string : FIELDS_TO_DROP) {
            dynamic = dynamic.remove(string);
        }
        return dynamic;
    }

    private static <T> Dynamic<T> fixText(Dynamic<T> dynamic) {
        boolean bl = dynamic.get("_filtered_correct").asBoolean(false);
        if (bl) {
            return dynamic.remove("_filtered_correct");
        }
        Optional optional = dynamic.get("filtered_messages").asStreamOpt().result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        Dynamic dynamic3 = ComponentDataFixUtils.createEmptyComponent(dynamic.getOps());
        List<Dynamic> list = dynamic.get("messages").asStreamOpt().result().orElse(Stream.of(new Dynamic[0])).toList();
        List list2 = Streams.mapWithIndex((Stream)((Stream)optional.get()), (dynamic2, l) -> {
            Dynamic dynamic3 = l < (long)list.size() ? (Dynamic)list.get((int)l) : dynamic3;
            return dynamic2.equals((Object)dynamic3) ? dynamic3 : dynamic2;
        }).toList();
        if (list2.stream().allMatch(dynamic2 -> dynamic2.equals((Object)dynamic3))) {
            return dynamic.remove("filtered_messages");
        }
        return dynamic.set("filtered_messages", dynamic.createList(list2.stream()));
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), DropInvalidSignDataFix::fix);
    }
}

