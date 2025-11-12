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
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RenameEnchantmentsFix
extends DataFix {
    final String name;
    final Map<String, String> renames;

    public RenameEnchantmentsFix(Schema schema, String string, Map<String, String> map) {
        super(schema, false);
        this.name = string;
        this.renames = map;
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped(this.name, type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::fixTag)));
    }

    private Dynamic<?> fixTag(Dynamic<?> dynamic) {
        dynamic = this.fixEnchantmentList(dynamic, "Enchantments");
        dynamic = this.fixEnchantmentList(dynamic, "StoredEnchantments");
        return dynamic;
    }

    private Dynamic<?> fixEnchantmentList(Dynamic<?> dynamic2, String string) {
        return dynamic2.update(string, dynamic -> (Dynamic)dynamic.asStreamOpt().map(stream -> stream.map(dynamic -> dynamic.update("id", dynamic2 -> (Dynamic)dynamic2.asString().map(string -> dynamic.createString(this.renames.getOrDefault(NamespacedSchema.ensureNamespaced(string), (String)string))).mapOrElse(Function.identity(), error -> dynamic2)))).map(arg_0 -> ((Dynamic)dynamic).createList(arg_0)).mapOrElse(Function.identity(), error -> dynamic));
    }
}

