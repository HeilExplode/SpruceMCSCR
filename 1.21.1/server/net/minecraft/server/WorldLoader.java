/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <D, R> CompletableFuture<R> load(InitConfig initConfig, WorldDataSupplier<D> worldDataSupplier, ResultFactory<D, R> resultFactory, Executor executor, Executor executor2) {
        try {
            Pair<WorldDataConfiguration, CloseableResourceManager> pair = initConfig.packConfig.createResourceManager();
            CloseableResourceManager closeableResourceManager = (CloseableResourceManager)pair.getSecond();
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = RegistryLayer.createRegistryAccess();
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = WorldLoader.loadAndReplaceLayer(closeableResourceManager, layeredRegistryAccess, RegistryLayer.WORLDGEN, RegistryDataLoader.WORLDGEN_REGISTRIES);
            RegistryAccess.Frozen frozen = layeredRegistryAccess2.getAccessForLoading(RegistryLayer.DIMENSIONS);
            RegistryAccess.Frozen frozen2 = RegistryDataLoader.load(closeableResourceManager, (RegistryAccess)frozen, RegistryDataLoader.DIMENSION_REGISTRIES);
            WorldDataConfiguration worldDataConfiguration = (WorldDataConfiguration)pair.getFirst();
            DataLoadOutput<D> dataLoadOutput = worldDataSupplier.get(new DataLoadContext(closeableResourceManager, worldDataConfiguration, frozen, frozen2));
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess3 = layeredRegistryAccess2.replaceFrom(RegistryLayer.DIMENSIONS, dataLoadOutput.finalDimensions);
            return ((CompletableFuture)ReloadableServerResources.loadResources(closeableResourceManager, layeredRegistryAccess3, worldDataConfiguration.enabledFeatures(), initConfig.commandSelection(), initConfig.functionCompilationLevel(), executor, executor2).whenComplete((reloadableServerResources, throwable) -> {
                if (throwable != null) {
                    closeableResourceManager.close();
                }
            })).thenApplyAsync(reloadableServerResources -> {
                reloadableServerResources.updateRegistryTags();
                return resultFactory.create(closeableResourceManager, (ReloadableServerResources)reloadableServerResources, layeredRegistryAccess3, dataLoadOutput.cookie);
            }, executor2);
        }
        catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private static RegistryAccess.Frozen loadLayer(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, RegistryLayer registryLayer, List<RegistryDataLoader.RegistryData<?>> list) {
        RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(registryLayer);
        return RegistryDataLoader.load(resourceManager, (RegistryAccess)frozen, list);
    }

    private static LayeredRegistryAccess<RegistryLayer> loadAndReplaceLayer(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, RegistryLayer registryLayer, List<RegistryDataLoader.RegistryData<?>> list) {
        RegistryAccess.Frozen frozen = WorldLoader.loadLayer(resourceManager, layeredRegistryAccess, registryLayer, list);
        return layeredRegistryAccess.replaceFrom(registryLayer, frozen);
    }

    public static final class InitConfig
    extends Record {
        final PackConfig packConfig;
        private final Commands.CommandSelection commandSelection;
        private final int functionCompilationLevel;

        public InitConfig(PackConfig packConfig, Commands.CommandSelection commandSelection, int n) {
            this.packConfig = packConfig;
            this.commandSelection = commandSelection;
            this.functionCompilationLevel = n;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationLevel", "packConfig", "commandSelection", "functionCompilationLevel"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationLevel", "packConfig", "commandSelection", "functionCompilationLevel"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationLevel", "packConfig", "commandSelection", "functionCompilationLevel"}, this, object);
        }

        public PackConfig packConfig() {
            return this.packConfig;
        }

        public Commands.CommandSelection commandSelection() {
            return this.commandSelection;
        }

        public int functionCompilationLevel() {
            return this.functionCompilationLevel;
        }
    }

    public record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
        public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
            WorldDataConfiguration worldDataConfiguration = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataConfig, this.initMode, this.safeMode);
            List<PackResources> list = this.packRepository.openAllSelected();
            MultiPackResourceManager multiPackResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
            return Pair.of((Object)worldDataConfiguration, (Object)multiPackResourceManager);
        }
    }

    public record DataLoadContext(ResourceManager resources, WorldDataConfiguration dataConfiguration, RegistryAccess.Frozen datapackWorldgen, RegistryAccess.Frozen datapackDimensions) {
    }

    @FunctionalInterface
    public static interface WorldDataSupplier<D> {
        public DataLoadOutput<D> get(DataLoadContext var1);
    }

    public static final class DataLoadOutput<D>
    extends Record {
        final D cookie;
        final RegistryAccess.Frozen finalDimensions;

        public DataLoadOutput(D d, RegistryAccess.Frozen frozen) {
            this.cookie = d;
            this.finalDimensions = frozen;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this, object);
        }

        public D cookie() {
            return this.cookie;
        }

        public RegistryAccess.Frozen finalDimensions() {
            return this.finalDimensions;
        }
    }

    @FunctionalInterface
    public static interface ResultFactory<D, R> {
        public R create(CloseableResourceManager var1, ReloadableServerResources var2, LayeredRegistryAccess<RegistryLayer> var3, D var4);
    }
}

