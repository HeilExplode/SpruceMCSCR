/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockPosFormatAndRenamesFix
extends DataFix {
    private static final List<String> PATROLLING_MOBS = List.of("minecraft:witch", "minecraft:ravager", "minecraft:pillager", "minecraft:illusioner", "minecraft:evoker", "minecraft:vindicator");

    public BlockPosFormatAndRenamesFix(Schema schema) {
        super(schema, false);
    }

    private Typed<?> fixFields(Typed<?> typed, Map<String, String> map) {
        return typed.update(DSL.remainderFinder(), dynamic -> {
            for (Map.Entry entry : map.entrySet()) {
                dynamic = dynamic.renameAndFixField((String)entry.getKey(), (String)entry.getValue(), ExtraDataFixUtils::fixBlockPos);
            }
            return dynamic;
        });
    }

    private <T> Dynamic<T> fixMapSavedData(Dynamic<T> dynamic) {
        return dynamic.update("frames", dynamic2 -> dynamic2.createList(dynamic2.asStream().map(dynamic -> {
            dynamic = dynamic.renameAndFixField("Pos", "pos", ExtraDataFixUtils::fixBlockPos);
            dynamic = dynamic.renameField("Rotation", "rotation");
            dynamic = dynamic.renameField("EntityId", "entity_id");
            return dynamic;
        }))).update("banners", dynamic2 -> dynamic2.createList(dynamic2.asStream().map(dynamic -> {
            dynamic = dynamic.renameField("Pos", "pos");
            dynamic = dynamic.renameField("Color", "color");
            dynamic = dynamic.renameField("Name", "name");
            return dynamic;
        })));
    }

    public TypeRewriteRule makeRule() {
        ArrayList<TypeRewriteRule> arrayList = new ArrayList<TypeRewriteRule>();
        this.addEntityRules(arrayList);
        this.addBlockEntityRules(arrayList);
        arrayList.add(this.fixTypeEverywhereTyped("BlockPos format for map frames", this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("data", this::fixMapSavedData))));
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        arrayList.add(this.fixTypeEverywhereTyped("BlockPos format for compass target", type, ItemStackTagFix.createFixer(type, "minecraft:compass"::equals, dynamic -> dynamic.update("LodestonePos", ExtraDataFixUtils::fixBlockPos))));
        return TypeRewriteRule.seq(arrayList);
    }

    private void addEntityRules(List<TypeRewriteRule> list) {
        list.add(this.createEntityFixer(References.ENTITY, "minecraft:bee", Map.of("HivePos", "hive_pos", "FlowerPos", "flower_pos")));
        list.add(this.createEntityFixer(References.ENTITY, "minecraft:end_crystal", Map.of("BeamTarget", "beam_target")));
        list.add(this.createEntityFixer(References.ENTITY, "minecraft:wandering_trader", Map.of("WanderTarget", "wander_target")));
        for (String string : PATROLLING_MOBS) {
            list.add(this.createEntityFixer(References.ENTITY, string, Map.of("PatrolTarget", "patrol_target")));
        }
        list.add(this.fixTypeEverywhereTyped("BlockPos format in Leash for mobs", this.getInputSchema().getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.renameAndFixField("Leash", "leash", ExtraDataFixUtils::fixBlockPos))));
    }

    private void addBlockEntityRules(List<TypeRewriteRule> list) {
        list.add(this.createEntityFixer(References.BLOCK_ENTITY, "minecraft:beehive", Map.of("FlowerPos", "flower_pos")));
        list.add(this.createEntityFixer(References.BLOCK_ENTITY, "minecraft:end_gateway", Map.of("ExitPortal", "exit_portal")));
    }

    private TypeRewriteRule createEntityFixer(DSL.TypeReference typeReference, String string, Map<String, String> map) {
        String string2 = "BlockPos format in " + String.valueOf(map.keySet()) + " for " + string + " (" + typeReference.typeName() + ")";
        OpticFinder opticFinder = DSL.namedChoice((String)string, (Type)this.getInputSchema().getChoiceType(typeReference, string));
        return this.fixTypeEverywhereTyped(string2, this.getInputSchema().getType(typeReference), typed2 -> typed2.updateTyped(opticFinder, typed -> this.fixFields((Typed<?>)typed, map)));
    }
}

