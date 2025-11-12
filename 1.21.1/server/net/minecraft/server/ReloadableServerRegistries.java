/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class ReloadableServerRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

    public static CompletableFuture<LayeredRegistryAccess<RegistryLayer>> reload(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, ResourceManager resourceManager, Executor executor) {
        RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(RegistryLayer.RELOADABLE);
        RegistryOps registryOps = new EmptyTagLookupWrapper(frozen).createSerializationContext(JsonOps.INSTANCE);
        List<CompletableFuture> list2 = LootDataType.values().map(lootDataType -> ReloadableServerRegistries.scheduleElementParse(lootDataType, registryOps, resourceManager, executor)).toList();
        CompletableFuture completableFuture = Util.sequence(list2);
        return completableFuture.thenApplyAsync(list -> ReloadableServerRegistries.apply(layeredRegistryAccess, list), executor);
    }

    private static <T> CompletableFuture<WritableRegistry<?>> scheduleElementParse(LootDataType<T> lootDataType, RegistryOps<JsonElement> registryOps, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            MappedRegistry mappedRegistry = new MappedRegistry(lootDataType.registryKey(), Lifecycle.experimental());
            HashMap<ResourceLocation, JsonElement> hashMap = new HashMap<ResourceLocation, JsonElement>();
            String string = Registries.elementsDirPath(lootDataType.registryKey());
            SimpleJsonResourceReloadListener.scanDirectory(resourceManager, string, GSON, hashMap);
            hashMap.forEach((resourceLocation, jsonElement) -> lootDataType.deserialize((ResourceLocation)resourceLocation, registryOps, jsonElement).ifPresent(object -> mappedRegistry.register(ResourceKey.create(lootDataType.registryKey(), resourceLocation), object, DEFAULT_REGISTRATION_INFO)));
            return mappedRegistry;
        }, executor);
    }

    private static LayeredRegistryAccess<RegistryLayer> apply(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<WritableRegistry<?>> list) {
        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = ReloadableServerRegistries.createUpdatedRegistries(layeredRegistryAccess, list);
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        RegistryAccess.Frozen frozen = layeredRegistryAccess2.compositeAccess();
        ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, frozen.asGetterLookup());
        LootDataType.values().forEach(lootDataType -> ReloadableServerRegistries.validateRegistry(validationContext, lootDataType, frozen));
        collector.get().forEach((string, string2) -> LOGGER.warn("Found loot table element validation problem in {}: {}", string, string2));
        return layeredRegistryAccess2;
    }

    private static LayeredRegistryAccess<RegistryLayer> createUpdatedRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<WritableRegistry<?>> list) {
        RegistryAccess.ImmutableRegistryAccess immutableRegistryAccess = new RegistryAccess.ImmutableRegistryAccess(list);
        ((WritableRegistry)immutableRegistryAccess.registryOrThrow(Registries.LOOT_TABLE)).register(BuiltInLootTables.EMPTY, LootTable.EMPTY, DEFAULT_REGISTRATION_INFO);
        return layeredRegistryAccess.replaceFrom(RegistryLayer.RELOADABLE, immutableRegistryAccess.freeze());
    }

    private static <T> void validateRegistry(ValidationContext validationContext, LootDataType<T> lootDataType, RegistryAccess registryAccess) {
        Registry<T> registry = registryAccess.registryOrThrow(lootDataType.registryKey());
        registry.holders().forEach(reference -> lootDataType.runValidation(validationContext, reference.key(), reference.value()));
    }

    static class EmptyTagLookupWrapper
    implements HolderLookup.Provider {
        private final RegistryAccess registryAccess;

        EmptyTagLookupWrapper(RegistryAccess registryAccess) {
            this.registryAccess = registryAccess;
        }

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
            return this.registryAccess.listRegistries();
        }

        @Override
        public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
            return this.registryAccess.registry(resourceKey).map(Registry::asTagAddingLookup);
        }
    }

    public static class Holder {
        private final RegistryAccess.Frozen registries;

        public Holder(RegistryAccess.Frozen frozen) {
            this.registries = frozen;
        }

        public RegistryAccess.Frozen get() {
            return this.registries;
        }

        public HolderGetter.Provider lookup() {
            return this.registries.asGetterLookup();
        }

        public Collection<ResourceLocation> getKeys(ResourceKey<? extends Registry<?>> resourceKey) {
            return this.registries.registry(resourceKey).stream().flatMap(registry -> registry.holders().map(reference -> reference.key().location())).toList();
        }

        public LootTable getLootTable(ResourceKey<LootTable> resourceKey) {
            return this.registries.lookup(Registries.LOOT_TABLE).flatMap(registryLookup -> registryLookup.get(resourceKey)).map(net.minecraft.core.Holder::value).orElse(LootTable.EMPTY);
        }
    }
}

