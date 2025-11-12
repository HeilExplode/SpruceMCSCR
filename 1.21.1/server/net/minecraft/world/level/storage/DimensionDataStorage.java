/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class DimensionDataStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, SavedData> cache = Maps.newHashMap();
    private final DataFixer fixerUpper;
    private final HolderLookup.Provider registries;
    private final File dataFolder;

    public DimensionDataStorage(File file, DataFixer dataFixer, HolderLookup.Provider provider) {
        this.fixerUpper = dataFixer;
        this.dataFolder = file;
        this.registries = provider;
    }

    private File getDataFile(String string) {
        return new File(this.dataFolder, string + ".dat");
    }

    public <T extends SavedData> T computeIfAbsent(SavedData.Factory<T> factory, String string) {
        T t = this.get(factory, string);
        if (t != null) {
            return t;
        }
        SavedData savedData = (SavedData)factory.constructor().get();
        this.set(string, savedData);
        return (T)savedData;
    }

    @Nullable
    public <T extends SavedData> T get(SavedData.Factory<T> factory, String string) {
        SavedData savedData = this.cache.get(string);
        if (savedData == null && !this.cache.containsKey(string)) {
            savedData = this.readSavedData(factory.deserializer(), factory.type(), string);
            this.cache.put(string, savedData);
        }
        return (T)savedData;
    }

    @Nullable
    private <T extends SavedData> T readSavedData(BiFunction<CompoundTag, HolderLookup.Provider, T> biFunction, DataFixTypes dataFixTypes, String string) {
        try {
            File file = this.getDataFile(string);
            if (file.exists()) {
                CompoundTag compoundTag = this.readTagFromDisk(string, dataFixTypes, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                return (T)((SavedData)biFunction.apply(compoundTag.getCompound("data"), this.registries));
            }
        }
        catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", (Object)string, (Object)exception);
        }
        return null;
    }

    public void set(String string, SavedData savedData) {
        this.cache.put(string, savedData);
    }

    public CompoundTag readTagFromDisk(String string, DataFixTypes dataFixTypes, int n) throws IOException {
        File file = this.getDataFile(string);
        try (FileInputStream fileInputStream = new FileInputStream(file);){
            CompoundTag compoundTag;
            try (PushbackInputStream pushbackInputStream = new PushbackInputStream(new FastBufferedInputStream(fileInputStream), 2);){
                CompoundTag compoundTag2;
                if (this.isGzip(pushbackInputStream)) {
                    compoundTag2 = NbtIo.readCompressed(pushbackInputStream, NbtAccounter.unlimitedHeap());
                } else {
                    try (DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);){
                        compoundTag2 = NbtIo.read(dataInputStream);
                    }
                }
                int n2 = NbtUtils.getDataVersion(compoundTag2, 1343);
                compoundTag = dataFixTypes.update(this.fixerUpper, compoundTag2, n2, n);
            }
            return compoundTag;
        }
    }

    private boolean isGzip(PushbackInputStream pushbackInputStream) throws IOException {
        int n;
        byte[] byArray = new byte[2];
        boolean bl = false;
        int n2 = pushbackInputStream.read(byArray, 0, 2);
        if (n2 == 2 && (n = (byArray[1] & 0xFF) << 8 | byArray[0] & 0xFF) == 35615) {
            bl = true;
        }
        if (n2 != 0) {
            pushbackInputStream.unread(byArray, 0, n2);
        }
        return bl;
    }

    public void save() {
        this.cache.forEach((string, savedData) -> {
            if (savedData != null) {
                savedData.save(this.getDataFile((String)string), this.registries);
            }
        });
    }
}

