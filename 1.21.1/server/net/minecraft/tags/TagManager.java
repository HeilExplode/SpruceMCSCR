/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.tags;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;

public class TagManager
implements PreparableReloadListener {
    private final RegistryAccess registryAccess;
    private List<LoadResult<?>> results = List.of();

    public TagManager(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public List<LoadResult<?>> getResult() {
        return this.results;
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        List<CompletableFuture> list = this.registryAccess.registries().map(registryEntry -> this.createLoader(resourceManager, executor, (RegistryAccess.RegistryEntry)registryEntry)).toList();
        return ((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(void_ -> {
            this.results = list.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList());
        }, executor2);
    }

    private <T> CompletableFuture<LoadResult<T>> createLoader(ResourceManager resourceManager, Executor executor, RegistryAccess.RegistryEntry<T> registryEntry) {
        ResourceKey resourceKey = registryEntry.key();
        Registry<T> registry = registryEntry.value();
        TagLoader tagLoader = new TagLoader(registry::getHolder, Registries.tagsDirPath(resourceKey));
        return CompletableFuture.supplyAsync(() -> new LoadResult(resourceKey, tagLoader.loadAndBuild(resourceManager)), executor);
    }

    public record LoadResult<T>(ResourceKey<? extends Registry<T>> key, Map<ResourceLocation, Collection<Holder<T>>> tags) {
    }
}

