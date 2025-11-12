/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;

public class ItemListReport
implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public ItemListReport(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.output = packOutput;
        this.registries = completableFuture;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("items.json");
        return this.registries.thenCompose(provider -> {
            JsonObject jsonObject = new JsonObject();
            RegistryOps registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
            provider.lookupOrThrow(Registries.ITEM).listElements().forEach(reference -> {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.add("components", (JsonElement)DataComponentMap.CODEC.encodeStart((DynamicOps)registryOps, (Object)((Item)reference.value()).components()).getOrThrow(string -> new IllegalStateException("Failed to encode components: " + string)));
                jsonObject.add(reference.getRegisteredName(), (JsonElement)jsonObject2);
            });
            return DataProvider.saveStable(cachedOutput, (JsonElement)jsonObject, path);
        });
    }

    @Override
    public final String getName() {
        return "Item List";
    }
}

