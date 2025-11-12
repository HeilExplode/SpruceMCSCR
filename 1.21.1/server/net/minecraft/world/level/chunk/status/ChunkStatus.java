/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.world.level.chunk.status;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.VisibleForTesting;

public class ChunkStatus {
    public static final int MAX_STRUCTURE_DISTANCE = 8;
    private static final EnumSet<Heightmap.Types> WORLDGEN_HEIGHTMAPS = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
    public static final EnumSet<Heightmap.Types> FINAL_HEIGHTMAPS = EnumSet.of(Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
    public static final ChunkStatus EMPTY = ChunkStatus.register("empty", null, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus STRUCTURE_STARTS = ChunkStatus.register("structure_starts", EMPTY, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus STRUCTURE_REFERENCES = ChunkStatus.register("structure_references", STRUCTURE_STARTS, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus BIOMES = ChunkStatus.register("biomes", STRUCTURE_REFERENCES, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus NOISE = ChunkStatus.register("noise", BIOMES, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus SURFACE = ChunkStatus.register("surface", NOISE, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus CARVERS = ChunkStatus.register("carvers", SURFACE, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus FEATURES = ChunkStatus.register("features", CARVERS, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus INITIALIZE_LIGHT = ChunkStatus.register("initialize_light", FEATURES, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus LIGHT = ChunkStatus.register("light", INITIALIZE_LIGHT, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus SPAWN = ChunkStatus.register("spawn", LIGHT, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus FULL = ChunkStatus.register("full", SPAWN, FINAL_HEIGHTMAPS, ChunkType.LEVELCHUNK);
    private final int index;
    private final ChunkStatus parent;
    private final ChunkType chunkType;
    private final EnumSet<Heightmap.Types> heightmapsAfter;

    private static ChunkStatus register(String string, @Nullable ChunkStatus chunkStatus, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType) {
        return Registry.register(BuiltInRegistries.CHUNK_STATUS, string, new ChunkStatus(chunkStatus, enumSet, chunkType));
    }

    public static List<ChunkStatus> getStatusList() {
        ChunkStatus chunkStatus;
        ArrayList arrayList = Lists.newArrayList();
        for (chunkStatus = FULL; chunkStatus.getParent() != chunkStatus; chunkStatus = chunkStatus.getParent()) {
            arrayList.add(chunkStatus);
        }
        arrayList.add(chunkStatus);
        Collections.reverse(arrayList);
        return arrayList;
    }

    @VisibleForTesting
    protected ChunkStatus(@Nullable ChunkStatus chunkStatus, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType) {
        this.parent = chunkStatus == null ? this : chunkStatus;
        this.chunkType = chunkType;
        this.heightmapsAfter = enumSet;
        this.index = chunkStatus == null ? 0 : chunkStatus.getIndex() + 1;
    }

    public int getIndex() {
        return this.index;
    }

    public ChunkStatus getParent() {
        return this.parent;
    }

    public ChunkType getChunkType() {
        return this.chunkType;
    }

    public static ChunkStatus byName(String string) {
        return BuiltInRegistries.CHUNK_STATUS.get(ResourceLocation.tryParse(string));
    }

    public EnumSet<Heightmap.Types> heightmapsAfter() {
        return this.heightmapsAfter;
    }

    public boolean isOrAfter(ChunkStatus chunkStatus) {
        return this.getIndex() >= chunkStatus.getIndex();
    }

    public boolean isAfter(ChunkStatus chunkStatus) {
        return this.getIndex() > chunkStatus.getIndex();
    }

    public boolean isOrBefore(ChunkStatus chunkStatus) {
        return this.getIndex() <= chunkStatus.getIndex();
    }

    public boolean isBefore(ChunkStatus chunkStatus) {
        return this.getIndex() < chunkStatus.getIndex();
    }

    public static ChunkStatus max(ChunkStatus chunkStatus, ChunkStatus chunkStatus2) {
        return chunkStatus.isAfter(chunkStatus2) ? chunkStatus : chunkStatus2;
    }

    public String toString() {
        return this.getName();
    }

    public String getName() {
        return BuiltInRegistries.CHUNK_STATUS.getKey(this).toString();
    }
}

