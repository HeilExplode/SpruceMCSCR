/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.slf4j.Logger;

public class ReloadableServerResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final ReloadableServerRegistries.Holder fullRegistryHolder;
    private final ConfigurableRegistryLookup registryLookup;
    private final Commands commands;
    private final RecipeManager recipes;
    private final TagManager tagManager;
    private final ServerAdvancementManager advancements;
    private final ServerFunctionLibrary functionLibrary;

    private ReloadableServerResources(RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int n) {
        this.fullRegistryHolder = new ReloadableServerRegistries.Holder(frozen);
        this.registryLookup = new ConfigurableRegistryLookup(frozen);
        this.registryLookup.missingTagAccessPolicy(MissingTagAccessPolicy.CREATE_NEW);
        this.recipes = new RecipeManager(this.registryLookup);
        this.tagManager = new TagManager(frozen);
        this.commands = new Commands(commandSelection, CommandBuildContext.simple(this.registryLookup, featureFlagSet));
        this.advancements = new ServerAdvancementManager(this.registryLookup);
        this.functionLibrary = new ServerFunctionLibrary(n, this.commands.getDispatcher());
    }

    public ServerFunctionLibrary getFunctionLibrary() {
        return this.functionLibrary;
    }

    public ReloadableServerRegistries.Holder fullRegistries() {
        return this.fullRegistryHolder;
    }

    public RecipeManager getRecipeManager() {
        return this.recipes;
    }

    public Commands getCommands() {
        return this.commands;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.advancements;
    }

    public List<PreparableReloadListener> listeners() {
        return List.of(this.tagManager, this.recipes, this.functionLibrary, this.advancements);
    }

    public static CompletableFuture<ReloadableServerResources> loadResources(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int n, Executor executor, Executor executor2) {
        return ReloadableServerRegistries.reload(layeredRegistryAccess2, resourceManager, executor).thenCompose(layeredRegistryAccess -> {
            ReloadableServerResources reloadableServerResources = new ReloadableServerResources(layeredRegistryAccess.compositeAccess(), featureFlagSet, commandSelection, n);
            return ((CompletableFuture)SimpleReloadInstance.create(resourceManager, reloadableServerResources.listeners(), executor, executor2, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()).done().whenComplete((object, throwable) -> reloadableServerResources.registryLookup.missingTagAccessPolicy(MissingTagAccessPolicy.FAIL))).thenApply(object -> reloadableServerResources);
        });
    }

    public void updateRegistryTags() {
        this.tagManager.getResult().forEach(loadResult -> ReloadableServerResources.updateRegistryTags(this.fullRegistryHolder.get(), loadResult));
        AbstractFurnaceBlockEntity.invalidateCache();
        Blocks.rebuildCache();
    }

    private static <T> void updateRegistryTags(RegistryAccess registryAccess, TagManager.LoadResult<T> loadResult) {
        ResourceKey resourceKey = loadResult.key();
        Map map = loadResult.tags().entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> TagKey.create(resourceKey, (ResourceLocation)entry.getKey()), entry -> List.copyOf((Collection)entry.getValue())));
        registryAccess.registryOrThrow(resourceKey).bindTags(map);
    }

    static class ConfigurableRegistryLookup
    implements HolderLookup.Provider {
        private final RegistryAccess registryAccess;
        MissingTagAccessPolicy missingTagAccessPolicy = MissingTagAccessPolicy.FAIL;

        ConfigurableRegistryLookup(RegistryAccess registryAccess) {
            this.registryAccess = registryAccess;
        }

        public void missingTagAccessPolicy(MissingTagAccessPolicy missingTagAccessPolicy) {
            this.missingTagAccessPolicy = missingTagAccessPolicy;
        }

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
            return this.registryAccess.listRegistries();
        }

        @Override
        public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
            return this.registryAccess.registry(resourceKey).map(registry -> this.createDispatchedLookup(registry.asLookup(), registry.asTagAddingLookup()));
        }

        private <T> HolderLookup.RegistryLookup<T> createDispatchedLookup(final HolderLookup.RegistryLookup<T> registryLookup, final HolderLookup.RegistryLookup<T> registryLookup2) {
            return new HolderLookup.RegistryLookup.Delegate<T>(){

                @Override
                public HolderLookup.RegistryLookup<T> parent() {
                    return switch (missingTagAccessPolicy.ordinal()) {
                        default -> throw new MatchException(null, null);
                        case 1 -> registryLookup;
                        case 0 -> registryLookup2;
                    };
                }
            };
        }
    }

    static enum MissingTagAccessPolicy {
        CREATE_NEW,
        FAIL;

    }
}

