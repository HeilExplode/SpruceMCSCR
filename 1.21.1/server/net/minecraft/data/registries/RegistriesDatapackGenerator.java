/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data.registries;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

public class RegistriesDatapackGenerator
implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public RegistriesDatapackGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.registries = completableFuture;
        this.output = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.registries.thenCompose(provider -> {
            RegistryOps registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
            return CompletableFuture.allOf((CompletableFuture[])RegistryDataLoader.WORLDGEN_REGISTRIES.stream().flatMap(registryData -> this.dumpRegistryCap(cachedOutput, (HolderLookup.Provider)provider, registryOps, (RegistryDataLoader.RegistryData)registryData).stream()).toArray(CompletableFuture[]::new));
        });
    }

    private <T> Optional<CompletableFuture<?>> dumpRegistryCap(CachedOutput cachedOutput, HolderLookup.Provider provider, DynamicOps<JsonElement> dynamicOps, RegistryDataLoader.RegistryData<T> registryData) {
        ResourceKey resourceKey = registryData.key();
        return provider.lookup(resourceKey).map(registryLookup -> {
            PackOutput.PathProvider pathProvider = this.output.createRegistryElementsPathProvider(resourceKey);
            return CompletableFuture.allOf((CompletableFuture[])registryLookup.listElements().map(reference -> RegistriesDatapackGenerator.dumpValue(pathProvider.json(reference.key().location()), cachedOutput, dynamicOps, registryData.elementCodec(), reference.value())).toArray(CompletableFuture[]::new));
        });
    }

    private static <E> CompletableFuture<?> dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E e) {
        return (CompletableFuture)encoder.encodeStart(dynamicOps, e).mapOrElse(jsonElement -> DataProvider.saveStable(cachedOutput, jsonElement, path), error -> CompletableFuture.failedFuture(new IllegalStateException("Couldn't generate file '" + String.valueOf(path) + "': " + error.message())));
    }

    @Override
    public final String getName() {
        return "Registries";
    }
}

