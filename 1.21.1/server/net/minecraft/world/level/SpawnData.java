/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.level.LightLayer;

public record SpawnData(CompoundTag entityToSpawn, Optional<CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment) {
    public static final String ENTITY_TAG = "entity";
    public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CompoundTag.CODEC.fieldOf(ENTITY_TAG).forGetter(spawnData -> spawnData.entityToSpawn), (App)CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(spawnData -> spawnData.customSpawnRules), (App)EquipmentTable.CODEC.optionalFieldOf("equipment").forGetter(spawnData -> spawnData.equipment)).apply((Applicative)instance, SpawnData::new));
    public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodecAllowingEmpty(CODEC);

    public SpawnData() {
        this(new CompoundTag(), Optional.empty(), Optional.empty());
    }

    public SpawnData {
        if (compoundTag.contains("id")) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("id"));
            if (resourceLocation != null) {
                compoundTag.putString("id", resourceLocation.toString());
            } else {
                compoundTag.remove("id");
            }
        }
    }

    public CompoundTag getEntityToSpawn() {
        return this.entityToSpawn;
    }

    public Optional<CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public Optional<EquipmentTable> getEquipment() {
        return this.equipment;
    }

    public record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
        private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange<Integer>(0, 15);
        public static final Codec<CustomSpawnRules> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CustomSpawnRules.lightLimit("block_light_limit").forGetter(customSpawnRules -> customSpawnRules.blockLightLimit), (App)CustomSpawnRules.lightLimit("sky_light_limit").forGetter(customSpawnRules -> customSpawnRules.skyLightLimit)).apply((Applicative)instance, CustomSpawnRules::new));

        private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> inclusiveRange) {
            if (!LIGHT_RANGE.contains(inclusiveRange)) {
                return DataResult.error(() -> "Light values must be withing range " + String.valueOf(LIGHT_RANGE));
            }
            return DataResult.success(inclusiveRange);
        }

        private static MapCodec<InclusiveRange<Integer>> lightLimit(String string) {
            return InclusiveRange.INT.lenientOptionalFieldOf(string, LIGHT_RANGE).validate(CustomSpawnRules::checkLightBoundaries);
        }

        public boolean isValidPosition(BlockPos blockPos, ServerLevel serverLevel) {
            return this.blockLightLimit.isValueInRange(serverLevel.getBrightness(LightLayer.BLOCK, blockPos)) && this.skyLightLimit.isValueInRange(serverLevel.getBrightness(LightLayer.SKY, blockPos));
        }
    }
}

