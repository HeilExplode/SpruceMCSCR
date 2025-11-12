/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport
implements DataProvider {
    private final PackOutput output;

    public RegistryDumpReport(PackOutput packOutput) {
        this.output = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject jsonObject = new JsonObject();
        BuiltInRegistries.REGISTRY.holders().forEach(reference -> jsonObject.add(reference.key().location().toString(), RegistryDumpReport.dumpRegistry((Registry)reference.value())));
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("registries.json");
        return DataProvider.saveStable(cachedOutput, (JsonElement)jsonObject, path);
    }

    private static <T> JsonElement dumpRegistry(Registry<T> registry) {
        JsonObject jsonObject = new JsonObject();
        if (registry instanceof DefaultedRegistry) {
            ResourceLocation resourceLocation = ((DefaultedRegistry)registry).getDefaultKey();
            jsonObject.addProperty("default", resourceLocation.toString());
        }
        int n = BuiltInRegistries.REGISTRY.getId(registry);
        jsonObject.addProperty("protocol_id", (Number)n);
        JsonObject jsonObject2 = new JsonObject();
        registry.holders().forEach(reference -> {
            Object t = reference.value();
            int n = registry.getId(t);
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("protocol_id", (Number)n);
            jsonObject2.add(reference.key().location().toString(), (JsonElement)jsonObject2);
        });
        jsonObject.add("entries", (JsonElement)jsonObject2);
        return jsonObject;
    }

    @Override
    public final String getName() {
        return "Registry Dump";
    }
}

