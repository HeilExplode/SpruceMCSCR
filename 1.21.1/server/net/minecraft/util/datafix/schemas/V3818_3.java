/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V3818_3
extends NamespacedSchema {
    public V3818_3(int n, Schema schema) {
        super(n, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"minecraft:bees", (Object)DSL.list((TypeTemplate)DSL.optionalFields((String)"entity_data", (TypeTemplate)References.ENTITY_TREE.in(schema)))), Pair.of((Object)"minecraft:block_entity_data", (Object)References.BLOCK_ENTITY.in(schema)), Pair.of((Object)"minecraft:bundle_contents", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"minecraft:can_break", (Object)DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))))))), Pair.of((Object)"minecraft:can_place_on", (Object)DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))))))), Pair.of((Object)"minecraft:charged_projectiles", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"minecraft:container", (Object)DSL.list((TypeTemplate)DSL.optionalFields((String)"item", (TypeTemplate)References.ITEM_STACK.in(schema)))), Pair.of((Object)"minecraft:entity_data", (Object)References.ENTITY_TREE.in(schema)), Pair.of((Object)"minecraft:pot_decorations", (Object)DSL.list((TypeTemplate)References.ITEM_NAME.in(schema))), Pair.of((Object)"minecraft:food", (Object)DSL.optionalFields((String)"using_converts_to", (TypeTemplate)References.ITEM_STACK.in(schema)))}));
    }
}

