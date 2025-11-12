/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.storage.loot.LootTable;

public record EquipmentTable(ResourceKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
    public static final Codec<Map<EquipmentSlot, Float>> DROP_CHANCES_CODEC = Codec.either((Codec)Codec.FLOAT, (Codec)Codec.unboundedMap(EquipmentSlot.CODEC, (Codec)Codec.FLOAT)).xmap(either -> (Map)either.map(EquipmentTable::createForAllSlots, Function.identity()), map -> {
        boolean bl = map.values().stream().distinct().count() == 1L;
        boolean bl2 = map.keySet().containsAll(Arrays.asList(EquipmentSlot.values()));
        if (bl && bl2) {
            return Either.left((Object)map.values().stream().findFirst().orElse(Float.valueOf(0.0f)));
        }
        return Either.right((Object)map);
    });
    public static final Codec<EquipmentTable> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(EquipmentTable::lootTable), (App)DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(EquipmentTable::slotDropChances)).apply((Applicative)instance, EquipmentTable::new));

    private static Map<EquipmentSlot, Float> createForAllSlots(float f) {
        return EquipmentTable.createForAllSlots(List.of(EquipmentSlot.values()), f);
    }

    private static Map<EquipmentSlot, Float> createForAllSlots(List<EquipmentSlot> list, float f) {
        HashMap hashMap = Maps.newHashMap();
        for (EquipmentSlot equipmentSlot : list) {
            hashMap.put(equipmentSlot, Float.valueOf(f));
        }
        return hashMap;
    }
}

