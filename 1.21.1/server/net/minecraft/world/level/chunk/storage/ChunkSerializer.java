/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.slf4j.Logger;

public class ChunkSerializer {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState());
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    public static ProtoChunk read(ServerLevel serverLevel, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos, CompoundTag compoundTag) {
        int n;
        ListTag listTag;
        ChunkAccess chunkAccess;
        Object object2;
        Object object3;
        EnumSet<Heightmap.Types> enumSet;
        ChunkPos chunkPos2 = new ChunkPos(compoundTag.getInt(X_POS_TAG), compoundTag.getInt(Z_POS_TAG));
        if (!Objects.equals(chunkPos, chunkPos2)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{chunkPos, chunkPos, chunkPos2});
            serverLevel.getServer().reportMisplacedChunk(chunkPos2, chunkPos, regionStorageInfo);
        }
        UpgradeData upgradeData = compoundTag.contains(TAG_UPGRADE_DATA, 10) ? new UpgradeData(compoundTag.getCompound(TAG_UPGRADE_DATA), serverLevel) : UpgradeData.EMPTY;
        boolean bl = compoundTag.getBoolean(IS_LIGHT_ON_TAG);
        ListTag listTag2 = compoundTag.getList(SECTIONS_TAG, 10);
        int n2 = serverLevel.getSectionsCount();
        LevelChunkSection[] levelChunkSectionArray = new LevelChunkSection[n2];
        boolean bl2 = serverLevel.dimensionType().hasSkyLight();
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        LevelLightEngine levelLightEngine = ((ChunkSource)serverChunkCache).getLightEngine();
        Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> codec = ChunkSerializer.makeBiomeCodec(registry);
        boolean bl3 = false;
        for (int i = 0; i < listTag2.size(); ++i) {
            boolean bl4;
            CompoundTag compoundTag2 = listTag2.getCompound(i);
            byte by = compoundTag2.getByte("Y");
            int n3 = serverLevel.getSectionIndexFromSectionY(by);
            if (n3 >= 0 && n3 < levelChunkSectionArray.length) {
                PalettedContainer palettedContainer = compoundTag2.contains("block_states", 10) ? (PalettedContainer)BLOCK_STATE_CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag2.getCompound("block_states")).promotePartial(string -> ChunkSerializer.logErrors(chunkPos, by, string)).getOrThrow(ChunkReadException::new) : new PalettedContainer(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                PalettedContainerRO<Holder.Reference<Biome>> palettedContainerRO = compoundTag2.contains("biomes", 10) ? (PalettedContainerRO)codec.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag2.getCompound("biomes")).promotePartial(string -> ChunkSerializer.logErrors(chunkPos, by, string)).getOrThrow(ChunkReadException::new) : new PalettedContainer<Holder.Reference<Biome>>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                enumSet = new LevelChunkSection(palettedContainer, palettedContainerRO);
                levelChunkSectionArray[n3] = enumSet;
                object3 = SectionPos.of(chunkPos, by);
                poiManager.checkConsistencyWithBlocks((SectionPos)object3, (LevelChunkSection)((Object)enumSet));
            }
            boolean bl5 = compoundTag2.contains(BLOCK_LIGHT_TAG, 7);
            boolean bl6 = bl4 = bl2 && compoundTag2.contains(SKY_LIGHT_TAG, 7);
            if (!bl5 && !bl4) continue;
            if (!bl3) {
                levelLightEngine.retainData(chunkPos, true);
                bl3 = true;
            }
            if (bl5) {
                levelLightEngine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, by), new DataLayer(compoundTag2.getByteArray(BLOCK_LIGHT_TAG)));
            }
            if (!bl4) continue;
            levelLightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, by), new DataLayer(compoundTag2.getByteArray(SKY_LIGHT_TAG)));
        }
        long l = compoundTag.getLong("InhabitedTime");
        ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
        BlendingData blendingData = compoundTag.contains("blending_data", 10) ? (BlendingData)BlendingData.CODEC.parse(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getCompound("blending_data"))).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElse(null) : null;
        if (chunkType == ChunkType.LEVELCHUNK) {
            object2 = LevelChunkTicks.load(compoundTag.getList(BLOCK_TICKS_TAG, 10), string -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            enumSet = LevelChunkTicks.load(compoundTag.getList(FLUID_TICKS_TAG, 10), string -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            chunkAccess = new LevelChunk(serverLevel.getLevel(), chunkPos, upgradeData, (LevelChunkTicks<Block>)object2, (LevelChunkTicks<Fluid>)((Object)enumSet), l, levelChunkSectionArray, ChunkSerializer.postLoadChunk(serverLevel, compoundTag), blendingData);
        } else {
            object2 = ProtoChunkTicks.load(compoundTag.getList(BLOCK_TICKS_TAG, 10), string -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            enumSet = ProtoChunkTicks.load(compoundTag.getList(FLUID_TICKS_TAG, 10), string -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            object3 = new ProtoChunk(chunkPos, upgradeData, levelChunkSectionArray, (ProtoChunkTicks<Block>)object2, (ProtoChunkTicks<Fluid>)((Object)enumSet), serverLevel, registry, blendingData);
            chunkAccess = object3;
            chunkAccess.setInhabitedTime(l);
            if (compoundTag.contains("below_zero_retrogen", 10)) {
                BelowZeroRetrogen.CODEC.parse(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getCompound("below_zero_retrogen"))).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(arg_0 -> object3.setBelowZeroRetrogen(arg_0));
            }
            ChunkStatus object4 = ChunkStatus.byName(compoundTag.getString("Status"));
            ((ProtoChunk)object3).setPersistedStatus(object4);
            if (object4.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                ((ProtoChunk)object3).setLightEngine(levelLightEngine);
            }
        }
        chunkAccess.setLightCorrect(bl);
        object2 = compoundTag.getCompound(HEIGHTMAPS_TAG);
        enumSet = EnumSet.noneOf(Heightmap.Types.class);
        for (Heightmap.Types types : chunkAccess.getPersistedStatus().heightmapsAfter()) {
            String string2 = types.getSerializationKey();
            if (((CompoundTag)object2).contains(string2, 12)) {
                chunkAccess.setHeightmap(types, ((CompoundTag)object2).getLongArray(string2));
                continue;
            }
            enumSet.add(types);
        }
        Heightmap.primeHeightmaps(chunkAccess, enumSet);
        object3 = compoundTag.getCompound("structures");
        chunkAccess.setAllStarts(ChunkSerializer.unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverLevel), (CompoundTag)object3, serverLevel.getSeed()));
        chunkAccess.setAllReferences(ChunkSerializer.unpackStructureReferences(serverLevel.registryAccess(), chunkPos, (CompoundTag)object3));
        if (compoundTag.getBoolean("shouldSave")) {
            chunkAccess.setUnsaved(true);
        }
        ListTag listTag3 = compoundTag.getList("PostProcessing", 9);
        for (int i = 0; i < listTag3.size(); ++i) {
            listTag = listTag3.getList(i);
            for (n = 0; n < listTag.size(); ++n) {
                chunkAccess.addPackedPostProcess(listTag.getShort(n), i);
            }
        }
        if (chunkType == ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)chunkAccess, false);
        }
        ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
        listTag = compoundTag.getList("entities", 10);
        for (n = 0; n < listTag.size(); ++n) {
            protoChunk.addEntity(listTag.getCompound(n));
        }
        ListTag listTag4 = compoundTag.getList("block_entities", 10);
        for (int i = 0; i < listTag4.size(); ++i) {
            CompoundTag compoundTag2 = listTag4.getCompound(i);
            chunkAccess.setBlockEntityNbt(compoundTag2);
        }
        CompoundTag compoundTag3 = compoundTag.getCompound("CarvingMasks");
        for (String string3 : compoundTag3.getAllKeys()) {
            GenerationStep.Carving carving = GenerationStep.Carving.valueOf(string3);
            protoChunk.setCarvingMask(carving, new CarvingMask(compoundTag3.getLongArray(string3), chunkAccess.getMinBuildHeight()));
        }
        return protoChunk;
    }

    private static void logErrors(ChunkPos chunkPos, int n, String string) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[]{chunkPos.x, n, chunkPos.z, string});
    }

    private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> registry) {
        return PalettedContainer.codecRO(registry.asHolderIdMap(), registry.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, registry.getHolderOrThrow(Biomes.PLAINS));
    }

    public static CompoundTag write(ServerLevel serverLevel, ChunkAccess chunkAccess) {
        Object object;
        UpgradeData upgradeData;
        BelowZeroRetrogen belowZeroRetrogen;
        ChunkPos chunkPos = chunkAccess.getPos();
        CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundTag.putInt(X_POS_TAG, chunkPos.x);
        compoundTag.putInt("yPos", chunkAccess.getMinSection());
        compoundTag.putInt(Z_POS_TAG, chunkPos.z);
        compoundTag.putLong("LastUpdate", serverLevel.getGameTime());
        compoundTag.putLong("InhabitedTime", chunkAccess.getInhabitedTime());
        compoundTag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(chunkAccess.getPersistedStatus()).toString());
        BlendingData blendingData = chunkAccess.getBlendingData();
        if (blendingData != null) {
            BlendingData.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)blendingData).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("blending_data", (Tag)tag));
        }
        if ((belowZeroRetrogen = chunkAccess.getBelowZeroRetrogen()) != null) {
            BelowZeroRetrogen.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)belowZeroRetrogen).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("below_zero_retrogen", (Tag)tag));
        }
        if (!(upgradeData = chunkAccess.getUpgradeData()).isEmpty()) {
            compoundTag.put(TAG_UPGRADE_DATA, upgradeData.write());
        }
        LevelChunkSection[] levelChunkSectionArray = chunkAccess.getSections();
        ListTag listTag = new ListTag();
        ThreadedLevelLightEngine threadedLevelLightEngine = serverLevel.getChunkSource().getLightEngine();
        Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> codec = ChunkSerializer.makeBiomeCodec(registry);
        boolean bl = chunkAccess.isLightCorrect();
        for (int i = threadedLevelLightEngine.getMinLightSection(); i < threadedLevelLightEngine.getMaxLightSection(); ++i) {
            int n = chunkAccess.getSectionIndexFromSectionY(i);
            boolean bl2 = n >= 0 && n < levelChunkSectionArray.length;
            DataLayer object22 = threadedLevelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, i));
            GenerationStep.Carving[] carvingArray = threadedLevelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, i));
            if (!bl2 && object22 == null && carvingArray == null) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            if (bl2) {
                LevelChunkSection levelChunkSection = levelChunkSectionArray[n];
                compoundTag2.put("block_states", (Tag)BLOCK_STATE_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, levelChunkSection.getStates()).getOrThrow());
                compoundTag2.put("biomes", (Tag)codec.encodeStart((DynamicOps)NbtOps.INSTANCE, levelChunkSection.getBiomes()).getOrThrow());
            }
            if (object22 != null && !object22.isEmpty()) {
                compoundTag2.putByteArray(BLOCK_LIGHT_TAG, object22.getData());
            }
            if (carvingArray != null && !carvingArray.isEmpty()) {
                compoundTag2.putByteArray(SKY_LIGHT_TAG, carvingArray.getData());
            }
            if (compoundTag2.isEmpty()) continue;
            compoundTag2.putByte("Y", (byte)i);
            listTag.add(compoundTag2);
        }
        compoundTag.put(SECTIONS_TAG, listTag);
        if (bl) {
            compoundTag.putBoolean(IS_LIGHT_ON_TAG, true);
        }
        ListTag listTag2 = new ListTag();
        for (BlockPos blockPos : chunkAccess.getBlockEntitiesPos()) {
            CompoundTag compoundTag3 = chunkAccess.getBlockEntityNbtForSaving(blockPos, serverLevel.registryAccess());
            if (compoundTag3 == null) continue;
            listTag2.add(compoundTag3);
        }
        compoundTag.put("block_entities", listTag2);
        if (chunkAccess.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            object = (ProtoChunk)chunkAccess;
            ListTag listTag3 = new ListTag();
            listTag3.addAll(((ProtoChunk)object).getEntities());
            compoundTag.put("entities", listTag3);
            CompoundTag compoundTag4 = new CompoundTag();
            for (GenerationStep.Carving carving : GenerationStep.Carving.values()) {
                CarvingMask carvingMask = ((ProtoChunk)object).getCarvingMask(carving);
                if (carvingMask == null) continue;
                compoundTag4.putLongArray(carving.toString(), carvingMask.toArray());
            }
            compoundTag.put("CarvingMasks", compoundTag4);
        }
        ChunkSerializer.saveTicks(serverLevel, compoundTag, chunkAccess.getTicksForSerialization());
        compoundTag.put("PostProcessing", ChunkSerializer.packOffsets(chunkAccess.getPostProcessing()));
        object = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
            if (!chunkAccess.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) continue;
            ((CompoundTag)object).put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        }
        compoundTag.put(HEIGHTMAPS_TAG, (Tag)object);
        compoundTag.put("structures", ChunkSerializer.packStructureData(StructurePieceSerializationContext.fromLevel(serverLevel), chunkPos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences()));
        return compoundTag;
    }

    private static void saveTicks(ServerLevel serverLevel, CompoundTag compoundTag, ChunkAccess.TicksToSave ticksToSave) {
        long l = serverLevel.getLevelData().getGameTime();
        compoundTag.put(BLOCK_TICKS_TAG, ticksToSave.blocks().save(l, block -> BuiltInRegistries.BLOCK.getKey((Block)block).toString()));
        compoundTag.put(FLUID_TICKS_TAG, ticksToSave.fluids().save(l, fluid -> BuiltInRegistries.FLUID.getKey((Fluid)fluid).toString()));
    }

    public static ChunkType getChunkTypeFromTag(@Nullable CompoundTag compoundTag) {
        if (compoundTag != null) {
            return ChunkStatus.byName(compoundTag.getString("Status")).getChunkType();
        }
        return ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel serverLevel, CompoundTag compoundTag) {
        ListTag listTag = ChunkSerializer.getListOfCompoundsOrNull(compoundTag, "entities");
        ListTag listTag2 = ChunkSerializer.getListOfCompoundsOrNull(compoundTag, "block_entities");
        if (listTag == null && listTag2 == null) {
            return null;
        }
        return levelChunk -> {
            if (listTag != null) {
                serverLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(listTag, serverLevel));
            }
            if (listTag2 != null) {
                for (int i = 0; i < listTag2.size(); ++i) {
                    CompoundTag compoundTag = listTag2.getCompound(i);
                    boolean bl = compoundTag.getBoolean("keepPacked");
                    if (bl) {
                        levelChunk.setBlockEntityNbt(compoundTag);
                        continue;
                    }
                    BlockPos blockPos = BlockEntity.getPosFromTag(compoundTag);
                    BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, levelChunk.getBlockState(blockPos), compoundTag, serverLevel.registryAccess());
                    if (blockEntity == null) continue;
                    levelChunk.setBlockEntity(blockEntity);
                }
            }
        };
    }

    @Nullable
    private static ListTag getListOfCompoundsOrNull(CompoundTag compoundTag, String string) {
        ListTag listTag = compoundTag.getList(string, 10);
        return listTag.isEmpty() ? null : listTag;
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos, Map<Structure, StructureStart> map, Map<Structure, LongSet> map2) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> object : map.entrySet()) {
            ResourceLocation resourceLocation = registry.getKey(object.getKey());
            compoundTag2.put(resourceLocation.toString(), object.getValue().createTag(structurePieceSerializationContext, chunkPos));
        }
        compoundTag.put("starts", compoundTag2);
        CompoundTag compoundTag3 = new CompoundTag();
        for (Map.Entry<Structure, LongSet> entry : map2.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            ResourceLocation resourceLocation = registry.getKey(entry.getKey());
            compoundTag3.put(resourceLocation.toString(), new LongArrayTag(entry.getValue()));
        }
        compoundTag.put("References", compoundTag3);
        return compoundTag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
        HashMap hashMap = Maps.newHashMap();
        Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompound("starts");
        for (String string : compoundTag2.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            Structure structure = registry.get(resourceLocation);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", (Object)resourceLocation);
                continue;
            }
            StructureStart structureStart = StructureStart.loadStaticStart(structurePieceSerializationContext, compoundTag2.getCompound(string), l);
            if (structureStart == null) continue;
            hashMap.put(structure, structureStart);
        }
        return hashMap;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos chunkPos, CompoundTag compoundTag) {
        HashMap hashMap = Maps.newHashMap();
        Registry<Structure> registry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompound("References");
        for (String string : compoundTag2.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            Structure structure = registry.get(resourceLocation);
            if (structure == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)resourceLocation, (Object)chunkPos);
                continue;
            }
            long[] lArray = compoundTag2.getLongArray(string);
            if (lArray.length == 0) continue;
            hashMap.put(structure, new LongOpenHashSet(Arrays.stream(lArray).filter(l -> {
                ChunkPos chunkPos2 = new ChunkPos(l);
                if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
                    LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{resourceLocation, chunkPos2, chunkPos});
                    return false;
                }
                return true;
            }).toArray()));
        }
        return hashMap;
    }

    public static ListTag packOffsets(ShortList[] shortListArray) {
        ListTag listTag = new ListTag();
        for (ShortList shortList : shortListArray) {
            ListTag listTag2 = new ListTag();
            if (shortList != null) {
                for (Short s : shortList) {
                    listTag2.add(ShortTag.valueOf(s));
                }
            }
            listTag.add(listTag2);
        }
        return listTag;
    }

    public static class ChunkReadException
    extends NbtException {
        public ChunkReadException(String string) {
            super(string);
        }
    }
}

