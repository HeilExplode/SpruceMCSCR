/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapId;

public class MapIndex
extends SavedData {
    public static final String FILE_NAME = "idcounts";
    private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap();

    public static SavedData.Factory<MapIndex> factory() {
        return new SavedData.Factory<MapIndex>(MapIndex::new, MapIndex::load, DataFixTypes.SAVED_DATA_MAP_INDEX);
    }

    public MapIndex() {
        this.usedAuxIds.defaultReturnValue(-1);
    }

    public static MapIndex load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        MapIndex mapIndex = new MapIndex();
        for (String string : compoundTag.getAllKeys()) {
            if (!compoundTag.contains(string, 99)) continue;
            mapIndex.usedAuxIds.put((Object)string, compoundTag.getInt(string));
        }
        return mapIndex;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        for (Object2IntMap.Entry entry : this.usedAuxIds.object2IntEntrySet()) {
            compoundTag.putInt((String)entry.getKey(), entry.getIntValue());
        }
        return compoundTag;
    }

    public MapId getFreeAuxValueForMap() {
        int n = this.usedAuxIds.getInt((Object)"map") + 1;
        this.usedAuxIds.put((Object)"map", n);
        this.setDirty();
        return new MapId(n);
    }
}

