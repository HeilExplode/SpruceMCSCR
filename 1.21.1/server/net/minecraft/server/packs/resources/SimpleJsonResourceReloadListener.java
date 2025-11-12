/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener
extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson gson;
    private final String directory;

    public SimpleJsonResourceReloadListener(Gson gson, String string) {
        this.gson = gson;
        this.directory = string;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        HashMap<ResourceLocation, JsonElement> hashMap = new HashMap<ResourceLocation, JsonElement>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, this.directory, this.gson, hashMap);
        return hashMap;
    }

    public static void scanDirectory(ResourceManager resourceManager, String string, Gson gson, Map<ResourceLocation, JsonElement> map) {
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(string);
        for (Map.Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            ResourceLocation resourceLocation2 = fileToIdConverter.fileToId(resourceLocation);
            try {
                BufferedReader bufferedReader = entry.getValue().openAsReader();
                try {
                    JsonElement jsonElement = GsonHelper.fromJson(gson, (Reader)bufferedReader, JsonElement.class);
                    JsonElement jsonElement2 = map.put(resourceLocation2, jsonElement);
                    if (jsonElement2 == null) continue;
                    throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf(resourceLocation2));
                }
                finally {
                    if (bufferedReader == null) continue;
                    ((Reader)bufferedReader).close();
                }
            }
            catch (JsonParseException | IOException | IllegalArgumentException throwable) {
                LOGGER.error("Couldn't parse data file {} from {}", new Object[]{resourceLocation2, resourceLocation, throwable});
            }
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

