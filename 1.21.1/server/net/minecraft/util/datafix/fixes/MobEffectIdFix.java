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
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MobEffectIdFix
extends DataFix {
    private static final Int2ObjectMap<String> ID_MAP = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(1, (Object)"minecraft:speed");
        int2ObjectOpenHashMap.put(2, (Object)"minecraft:slowness");
        int2ObjectOpenHashMap.put(3, (Object)"minecraft:haste");
        int2ObjectOpenHashMap.put(4, (Object)"minecraft:mining_fatigue");
        int2ObjectOpenHashMap.put(5, (Object)"minecraft:strength");
        int2ObjectOpenHashMap.put(6, (Object)"minecraft:instant_health");
        int2ObjectOpenHashMap.put(7, (Object)"minecraft:instant_damage");
        int2ObjectOpenHashMap.put(8, (Object)"minecraft:jump_boost");
        int2ObjectOpenHashMap.put(9, (Object)"minecraft:nausea");
        int2ObjectOpenHashMap.put(10, (Object)"minecraft:regeneration");
        int2ObjectOpenHashMap.put(11, (Object)"minecraft:resistance");
        int2ObjectOpenHashMap.put(12, (Object)"minecraft:fire_resistance");
        int2ObjectOpenHashMap.put(13, (Object)"minecraft:water_breathing");
        int2ObjectOpenHashMap.put(14, (Object)"minecraft:invisibility");
        int2ObjectOpenHashMap.put(15, (Object)"minecraft:blindness");
        int2ObjectOpenHashMap.put(16, (Object)"minecraft:night_vision");
        int2ObjectOpenHashMap.put(17, (Object)"minecraft:hunger");
        int2ObjectOpenHashMap.put(18, (Object)"minecraft:weakness");
        int2ObjectOpenHashMap.put(19, (Object)"minecraft:poison");
        int2ObjectOpenHashMap.put(20, (Object)"minecraft:wither");
        int2ObjectOpenHashMap.put(21, (Object)"minecraft:health_boost");
        int2ObjectOpenHashMap.put(22, (Object)"minecraft:absorption");
        int2ObjectOpenHashMap.put(23, (Object)"minecraft:saturation");
        int2ObjectOpenHashMap.put(24, (Object)"minecraft:glowing");
        int2ObjectOpenHashMap.put(25, (Object)"minecraft:levitation");
        int2ObjectOpenHashMap.put(26, (Object)"minecraft:luck");
        int2ObjectOpenHashMap.put(27, (Object)"minecraft:unluck");
        int2ObjectOpenHashMap.put(28, (Object)"minecraft:slow_falling");
        int2ObjectOpenHashMap.put(29, (Object)"minecraft:conduit_power");
        int2ObjectOpenHashMap.put(30, (Object)"minecraft:dolphins_grace");
        int2ObjectOpenHashMap.put(31, (Object)"minecraft:bad_omen");
        int2ObjectOpenHashMap.put(32, (Object)"minecraft:hero_of_the_village");
        int2ObjectOpenHashMap.put(33, (Object)"minecraft:darkness");
    });
    private static final Set<String> MOB_EFFECT_INSTANCE_CARRIER_ITEMS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

    public MobEffectIdFix(Schema schema) {
        super(schema, false);
    }

    private static <T> Optional<Dynamic<T>> getAndConvertMobEffectId(Dynamic<T> dynamic, String string) {
        return dynamic.get(string).asNumber().result().map(number -> (String)ID_MAP.get(number.intValue())).map(arg_0 -> dynamic.createString(arg_0));
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> dynamic, String string, Dynamic<T> dynamic2, String string2) {
        Optional<Dynamic<T>> optional = MobEffectIdFix.getAndConvertMobEffectId(dynamic, string);
        return dynamic2.replaceField(string, string2, optional);
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> dynamic, String string, String string2) {
        return MobEffectIdFix.updateMobEffectIdField(dynamic, string, dynamic, string2);
    }

    private static <T> Dynamic<T> updateMobEffectInstance(Dynamic<T> dynamic) {
        dynamic = MobEffectIdFix.updateMobEffectIdField(dynamic, "Id", "id");
        dynamic = dynamic.renameField("Ambient", "ambient");
        dynamic = dynamic.renameField("Amplifier", "amplifier");
        dynamic = dynamic.renameField("Duration", "duration");
        dynamic = dynamic.renameField("ShowParticles", "show_particles");
        dynamic = dynamic.renameField("ShowIcon", "show_icon");
        Optional<Dynamic> optional = dynamic.get("HiddenEffect").result().map(MobEffectIdFix::updateMobEffectInstance);
        return dynamic.replaceField("HiddenEffect", "hidden_effect", optional);
    }

    private static <T> Dynamic<T> updateMobEffectInstanceList(Dynamic<T> dynamic, String string, String string2) {
        Optional<Dynamic> optional = dynamic.get(string).asStreamOpt().result().map(stream -> dynamic.createList(stream.map(MobEffectIdFix::updateMobEffectInstance)));
        return dynamic.replaceField(string, string2, optional);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> dynamic, Dynamic<T> dynamic2) {
        dynamic2 = MobEffectIdFix.updateMobEffectIdField(dynamic, "EffectId", dynamic2, "id");
        Optional optional = dynamic.get("EffectDuration").result();
        return dynamic2.replaceField("EffectDuration", "duration", optional);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> dynamic) {
        return MobEffectIdFix.updateSuspiciousStewEntry(dynamic, dynamic);
    }

    private Typed<?> updateNamedChoice(Typed<?> typed2, DSL.TypeReference typeReference, String string, Function<Dynamic<?>, Dynamic<?>> function) {
        Type type = this.getInputSchema().getChoiceType(typeReference, string);
        Type type2 = this.getOutputSchema().getChoiceType(typeReference, string);
        return typed2.updateTyped(DSL.namedChoice((String)string, (Type)type), type2, typed -> typed.update(DSL.remainderFinder(), function));
    }

    private TypeRewriteRule blockEntityFixer() {
        Type type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped("BlockEntityMobEffectIdFix", type, typed -> {
            typed = this.updateNamedChoice((Typed<?>)typed, References.BLOCK_ENTITY, "minecraft:beacon", dynamic -> {
                dynamic = MobEffectIdFix.updateMobEffectIdField(dynamic, "Primary", "primary_effect");
                return MobEffectIdFix.updateMobEffectIdField(dynamic, "Secondary", "secondary_effect");
            });
            return typed;
        });
    }

    private static <T> Dynamic<T> fixMooshroomTag(Dynamic<T> dynamic) {
        Dynamic dynamic2 = dynamic.emptyMap();
        Dynamic<T> dynamic3 = MobEffectIdFix.updateSuspiciousStewEntry(dynamic, dynamic2);
        if (!dynamic3.equals((Object)dynamic2)) {
            dynamic = dynamic.set("stew_effects", dynamic.createList(Stream.of(dynamic3)));
        }
        return dynamic.remove("EffectId").remove("EffectDuration");
    }

    private static <T> Dynamic<T> fixArrowTag(Dynamic<T> dynamic) {
        return MobEffectIdFix.updateMobEffectInstanceList(dynamic, "CustomPotionEffects", "custom_potion_effects");
    }

    private static <T> Dynamic<T> fixAreaEffectCloudTag(Dynamic<T> dynamic) {
        return MobEffectIdFix.updateMobEffectInstanceList(dynamic, "Effects", "effects");
    }

    private static Dynamic<?> updateLivingEntityTag(Dynamic<?> dynamic) {
        return MobEffectIdFix.updateMobEffectInstanceList(dynamic, "ActiveEffects", "active_effects");
    }

    private TypeRewriteRule entityFixer() {
        Type type = this.getInputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("EntityMobEffectIdFix", type, typed -> {
            typed = this.updateNamedChoice((Typed<?>)typed, References.ENTITY, "minecraft:mooshroom", MobEffectIdFix::fixMooshroomTag);
            typed = this.updateNamedChoice((Typed<?>)typed, References.ENTITY, "minecraft:arrow", MobEffectIdFix::fixArrowTag);
            typed = this.updateNamedChoice((Typed<?>)typed, References.ENTITY, "minecraft:area_effect_cloud", MobEffectIdFix::fixAreaEffectCloudTag);
            typed = typed.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag);
            return typed;
        });
    }

    private TypeRewriteRule playerFixer() {
        Type type = this.getInputSchema().getType(References.PLAYER);
        return this.fixTypeEverywhereTyped("PlayerMobEffectIdFix", type, typed -> typed.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag));
    }

    private static <T> Dynamic<T> fixSuspiciousStewTag(Dynamic<T> dynamic) {
        Optional<Dynamic> optional = dynamic.get("Effects").asStreamOpt().result().map(stream -> dynamic.createList(stream.map(MobEffectIdFix::updateSuspiciousStewEntry)));
        return dynamic.replaceField("Effects", "effects", optional);
    }

    private TypeRewriteRule itemStackFixer() {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder2 = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemStackMobEffectIdFix", type, typed2 -> {
            Optional optional = typed2.getOptional(opticFinder);
            if (optional.isPresent()) {
                String string = (String)((Pair)optional.get()).getSecond();
                if (string.equals("minecraft:suspicious_stew")) {
                    return typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), MobEffectIdFix::fixSuspiciousStewTag));
                }
                if (MOB_EFFECT_INSTANCE_CARRIER_ITEMS.contains(string)) {
                    return typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), dynamic -> MobEffectIdFix.updateMobEffectInstanceList(dynamic, "CustomPotionEffects", "custom_potion_effects")));
                }
            }
            return typed2;
        });
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.blockEntityFixer(), (TypeRewriteRule[])new TypeRewriteRule[]{this.entityFixer(), this.playerFixer(), this.itemStackFixer()});
    }
}

