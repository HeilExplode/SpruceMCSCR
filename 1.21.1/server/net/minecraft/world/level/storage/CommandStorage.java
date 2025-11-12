/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class CommandStorage {
    private static final String ID_PREFIX = "command_storage_";
    private final Map<String, Container> namespaces = Maps.newHashMap();
    private final DimensionDataStorage storage;

    public CommandStorage(DimensionDataStorage dimensionDataStorage) {
        this.storage = dimensionDataStorage;
    }

    private Container newStorage(String string) {
        Container container = new Container();
        this.namespaces.put(string, container);
        return container;
    }

    private SavedData.Factory<Container> factory(String string) {
        return new SavedData.Factory<Container>(() -> this.newStorage(string), (compoundTag, provider) -> this.newStorage(string).load((CompoundTag)compoundTag), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public CompoundTag get(ResourceLocation resourceLocation) {
        String string = resourceLocation.getNamespace();
        Container container = this.storage.get(this.factory(string), CommandStorage.createId(string));
        return container != null ? container.get(resourceLocation.getPath()) : new CompoundTag();
    }

    public void set(ResourceLocation resourceLocation, CompoundTag compoundTag) {
        String string = resourceLocation.getNamespace();
        this.storage.computeIfAbsent(this.factory(string), CommandStorage.createId(string)).put(resourceLocation.getPath(), compoundTag);
    }

    public Stream<ResourceLocation> keys() {
        return this.namespaces.entrySet().stream().flatMap(entry -> ((Container)entry.getValue()).getKeys((String)entry.getKey()));
    }

    private static String createId(String string) {
        return ID_PREFIX + string;
    }

    static class Container
    extends SavedData {
        private static final String TAG_CONTENTS = "contents";
        private final Map<String, CompoundTag> storage = Maps.newHashMap();

        Container() {
        }

        Container load(CompoundTag compoundTag) {
            CompoundTag compoundTag2 = compoundTag.getCompound(TAG_CONTENTS);
            for (String string : compoundTag2.getAllKeys()) {
                this.storage.put(string, compoundTag2.getCompound(string));
            }
            return this;
        }

        @Override
        public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
            CompoundTag compoundTag3 = new CompoundTag();
            this.storage.forEach((string, compoundTag2) -> compoundTag3.put((String)string, compoundTag2.copy()));
            compoundTag.put(TAG_CONTENTS, compoundTag3);
            return compoundTag;
        }

        public CompoundTag get(String string) {
            CompoundTag compoundTag = this.storage.get(string);
            return compoundTag != null ? compoundTag : new CompoundTag();
        }

        public void put(String string, CompoundTag compoundTag) {
            if (compoundTag.isEmpty()) {
                this.storage.remove(string);
            } else {
                this.storage.put(string, compoundTag);
            }
            this.setDirty();
        }

        public Stream<ResourceLocation> getKeys(String string) {
            return this.storage.keySet().stream().map(string2 -> ResourceLocation.fromNamespaceAndPath(string, string2));
        }
    }
}

