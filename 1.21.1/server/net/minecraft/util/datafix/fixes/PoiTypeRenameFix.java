/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.AbstractPoiSectionFix;

public class PoiTypeRenameFix
extends AbstractPoiSectionFix {
    private final Function<String, String> renamer;

    public PoiTypeRenameFix(Schema schema, String string, Function<String, String> function) {
        super(schema, string);
        this.renamer = function;
    }

    @Override
    protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> stream) {
        return stream.map(dynamic2 -> dynamic2.update("type", dynamic -> (Dynamic)DataFixUtils.orElse((Optional)dynamic.asString().map(this.renamer).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)).result(), (Object)dynamic)));
    }
}

