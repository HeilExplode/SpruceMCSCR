/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerFunctionLibrary
implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<Registry<CommandFunction<CommandSourceStack>>> TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("function"));
    private static final FileToIdConverter LISTER = new FileToIdConverter(Registries.elementsDirPath(TYPE_KEY), ".mcfunction");
    private volatile Map<ResourceLocation, CommandFunction<CommandSourceStack>> functions = ImmutableMap.of();
    private final TagLoader<CommandFunction<CommandSourceStack>> tagsLoader = new TagLoader<CommandFunction<CommandSourceStack>>(this::getFunction, Registries.tagsDirPath(TYPE_KEY));
    private volatile Map<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> tags = Map.of();
    private final int functionCompilationLevel;
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    public Optional<CommandFunction<CommandSourceStack>> getFunction(ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.functions.get(resourceLocation));
    }

    public Map<ResourceLocation, CommandFunction<CommandSourceStack>> getFunctions() {
        return this.functions;
    }

    public Collection<CommandFunction<CommandSourceStack>> getTag(ResourceLocation resourceLocation) {
        return this.tags.getOrDefault(resourceLocation, List.of());
    }

    public Iterable<ResourceLocation> getAvailableTags() {
        return this.tags.keySet();
    }

    public ServerFunctionLibrary(int n, CommandDispatcher<CommandSourceStack> commandDispatcher) {
        this.functionCompilationLevel = n;
        this.dispatcher = commandDispatcher;
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        CompletableFuture<Map> completableFuture = CompletableFuture.supplyAsync(() -> this.tagsLoader.load(resourceManager), executor);
        CompletionStage completionStage = CompletableFuture.supplyAsync(() -> LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            HashMap hashMap = Maps.newHashMap();
            CommandSourceStack commandSourceStack = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.functionCompilationLevel, "", CommonComponents.EMPTY, null, null);
            for (Map.Entry entry : map.entrySet()) {
                ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
                ResourceLocation resourceLocation2 = LISTER.fileToId(resourceLocation);
                hashMap.put(resourceLocation2, CompletableFuture.supplyAsync(() -> {
                    List<String> list = ServerFunctionLibrary.readLines((Resource)entry.getValue());
                    return CommandFunction.fromLines(resourceLocation2, this.dispatcher, commandSourceStack, list);
                }, executor));
            }
            CompletableFuture[] completableFutureArray = hashMap.values().toArray(new CompletableFuture[0]);
            return CompletableFuture.allOf(completableFutureArray).handle((void_, throwable) -> hashMap);
        });
        return ((CompletableFuture)((CompletableFuture)completableFuture.thenCombine(completionStage, Pair::of)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(pair -> {
            Map map = (Map)pair.getSecond();
            ImmutableMap.Builder builder = ImmutableMap.builder();
            map.forEach((resourceLocation, completableFuture) -> ((CompletableFuture)completableFuture.handle((commandFunction, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Failed to load function {}", resourceLocation, throwable);
                } else {
                    builder.put(resourceLocation, commandFunction);
                }
                return null;
            })).join());
            this.functions = builder.build();
            this.tags = this.tagsLoader.build((Map)pair.getFirst());
        }, executor2);
    }

    private static List<String> readLines(Resource resource) {
        List<String> list;
        block8: {
            BufferedReader bufferedReader = resource.openAsReader();
            try {
                list = bufferedReader.lines().toList();
                if (bufferedReader == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    throw new CompletionException(iOException);
                }
            }
            bufferedReader.close();
        }
        return list;
    }
}

