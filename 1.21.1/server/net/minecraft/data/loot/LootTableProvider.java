/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.data.loot;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput.PathProvider pathProvider;
    private final Set<ResourceKey<LootTable>> requiredTables;
    private final List<SubProviderEntry> subProviders;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public LootTableProvider(PackOutput packOutput, Set<ResourceKey<LootTable>> set, List<SubProviderEntry> list, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.pathProvider = packOutput.createRegistryElementsPathProvider(Registries.LOOT_TABLE);
        this.subProviders = list;
        this.requiredTables = set;
        this.registries = completableFuture;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.registries.thenCompose(provider -> this.run(cachedOutput, (HolderLookup.Provider)provider));
    }

    private CompletableFuture<?> run(CachedOutput cachedOutput, HolderLookup.Provider provider) {
        MappedRegistry<LootTable> mappedRegistry = new MappedRegistry<LootTable>(Registries.LOOT_TABLE, Lifecycle.experimental());
        Object2ObjectOpenHashMap object2ObjectOpenHashMap = new Object2ObjectOpenHashMap();
        this.subProviders.forEach(arg_0 -> LootTableProvider.lambda$run$2(provider, (Map)object2ObjectOpenHashMap, mappedRegistry, arg_0));
        mappedRegistry.freeze();
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        HolderGetter.Provider provider2 = new RegistryAccess.ImmutableRegistryAccess(List.of(mappedRegistry)).freeze().asGetterLookup();
        ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, provider2);
        Sets.SetView setView = Sets.difference(this.requiredTables, mappedRegistry.registryKeySet());
        for (ResourceKey resourceKey : setView) {
            collector.report("Missing built-in table: " + String.valueOf(resourceKey.location()));
        }
        mappedRegistry.holders().forEach(reference -> ((LootTable)reference.value()).validate(validationContext.setParams(((LootTable)reference.value()).getParamSet()).enterElement("{" + String.valueOf(reference.key().location()) + "}", reference.key())));
        Iterator iterator = collector.get();
        if (!iterator.isEmpty()) {
            iterator.forEach((string, string2) -> LOGGER.warn("Found validation problem in {}: {}", string, string2));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }
        return CompletableFuture.allOf((CompletableFuture[])mappedRegistry.entrySet().stream().map(entry -> {
            ResourceKey resourceKey = (ResourceKey)entry.getKey();
            LootTable lootTable = (LootTable)entry.getValue();
            Path path = this.pathProvider.json(resourceKey.location());
            return DataProvider.saveStable(cachedOutput, provider, LootTable.DIRECT_CODEC, lootTable, path);
        }).toArray(CompletableFuture[]::new));
    }

    private static ResourceLocation sequenceIdForLootTable(ResourceKey<LootTable> resourceKey) {
        return resourceKey.location();
    }

    @Override
    public final String getName() {
        return "Loot Tables";
    }

    private static /* synthetic */ void lambda$run$2(HolderLookup.Provider provider, Map map, WritableRegistry writableRegistry, SubProviderEntry subProviderEntry) {
        subProviderEntry.provider().apply(provider).generate((resourceKey, builder) -> {
            ResourceLocation resourceLocation = LootTableProvider.sequenceIdForLootTable(resourceKey);
            ResourceLocation resourceLocation2 = map.put(RandomSequence.seedForKey(resourceLocation), resourceLocation);
            if (resourceLocation2 != null) {
                Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + String.valueOf(resourceLocation2) + " and " + String.valueOf(resourceKey.location()));
            }
            builder.setRandomSequence(resourceLocation);
            LootTable lootTable = builder.setParamSet(subProviderEntry.paramSet).build();
            writableRegistry.register(resourceKey, lootTable, RegistrationInfo.BUILT_IN);
        });
    }

    public static final class SubProviderEntry
    extends Record {
        private final Function<HolderLookup.Provider, LootTableSubProvider> provider;
        final LootContextParamSet paramSet;

        public SubProviderEntry(Function<HolderLookup.Provider, LootTableSubProvider> function, LootContextParamSet lootContextParamSet) {
            this.provider = function;
            this.paramSet = lootContextParamSet;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this, object);
        }

        public Function<HolderLookup.Provider, LootTableSubProvider> provider() {
            return this.provider;
        }

        public LootContextParamSet paramSet() {
            return this.paramSet;
        }
    }
}

