/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager
extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HolderLookup.Provider registries;
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.of();
    private Map<ResourceLocation, RecipeHolder<?>> byName = ImmutableMap.of();
    private boolean hasErrors;

    public RecipeManager(HolderLookup.Provider provider) {
        super(GSON, Registries.elementsDirPath(Registries.RECIPE));
        this.registries = provider;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.hasErrors = false;
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        ImmutableMap.Builder builder2 = ImmutableMap.builder();
        RegistryOps registryOps = this.registries.createSerializationContext(JsonOps.INSTANCE);
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            try {
                Recipe recipe = (Recipe)Recipe.CODEC.parse(registryOps, (Object)entry.getValue()).getOrThrow(JsonParseException::new);
                RecipeHolder<Recipe> recipeHolder = new RecipeHolder<Recipe>(resourceLocation, recipe);
                builder.put(recipe.getType(), recipeHolder);
                builder2.put((Object)resourceLocation, recipeHolder);
            }
            catch (JsonParseException | IllegalArgumentException throwable) {
                LOGGER.error("Parsing error loading recipe {}", (Object)resourceLocation, (Object)throwable);
            }
        }
        this.byType = builder.build();
        this.byName = builder2.build();
        LOGGER.info("Loaded {} recipes", (Object)this.byType.size());
    }

    public boolean hadErrorsLoading() {
        return this.hasErrors;
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, I i, Level level) {
        return this.getRecipeFor(recipeType, i, level, (RecipeHolder)null);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, I i, Level level, @Nullable ResourceLocation resourceLocation) {
        RecipeHolder<T> recipeHolder = resourceLocation != null ? this.byKeyTyped(recipeType, resourceLocation) : null;
        return this.getRecipeFor(recipeType, i, level, recipeHolder);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, I i, Level level, @Nullable RecipeHolder<T> recipeHolder2) {
        if (i.isEmpty()) {
            return Optional.empty();
        }
        if (recipeHolder2 != null && recipeHolder2.value().matches(i, level)) {
            return Optional.of(recipeHolder2);
        }
        return this.byType(recipeType).stream().filter(recipeHolder -> recipeHolder.value().matches((RecipeInput)i, level)).findFirst();
    }

    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getAllRecipesFor(RecipeType<T> recipeType) {
        return List.copyOf(this.byType(recipeType));
    }

    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getRecipesFor(RecipeType<T> recipeType, I i, Level level) {
        return this.byType(recipeType).stream().filter(recipeHolder -> recipeHolder.value().matches((RecipeInput)i, level)).sorted(Comparator.comparing(recipeHolder -> recipeHolder.value().getResultItem(level.registryAccess()).getDescriptionId())).collect(Collectors.toList());
    }

    private <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> recipeType) {
        return this.byType.get(recipeType);
    }

    public <I extends RecipeInput, T extends Recipe<I>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> recipeType, I i, Level level) {
        Optional<RecipeHolder<T>> optional = this.getRecipeFor(recipeType, i, level);
        if (optional.isPresent()) {
            return optional.get().value().getRemainingItems(i);
        }
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(i.size(), ItemStack.EMPTY);
        for (int j = 0; j < nonNullList.size(); ++j) {
            nonNullList.set(j, i.getItem(j));
        }
        return nonNullList;
    }

    public Optional<RecipeHolder<?>> byKey(ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.byName.get(resourceLocation));
    }

    @Nullable
    private <T extends Recipe<?>> RecipeHolder<T> byKeyTyped(RecipeType<T> recipeType, ResourceLocation resourceLocation) {
        RecipeHolder<?> recipeHolder = this.byName.get(resourceLocation);
        if (recipeHolder != null && recipeHolder.value().getType().equals(recipeType)) {
            return recipeHolder;
        }
        return null;
    }

    public Collection<RecipeHolder<?>> getOrderedRecipes() {
        return this.byType.values();
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        return this.byName.values();
    }

    public Stream<ResourceLocation> getRecipeIds() {
        return this.byName.keySet().stream();
    }

    @VisibleForTesting
    protected static RecipeHolder<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject, HolderLookup.Provider provider) {
        Recipe recipe = (Recipe)Recipe.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), (Object)jsonObject).getOrThrow(JsonParseException::new);
        return new RecipeHolder<Recipe>(resourceLocation, recipe);
    }

    public void replaceRecipes(Iterable<RecipeHolder<?>> iterable) {
        this.hasErrors = false;
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        ImmutableMap.Builder builder2 = ImmutableMap.builder();
        for (RecipeHolder<?> recipeHolder : iterable) {
            RecipeType<?> recipeType = recipeHolder.value().getType();
            builder.put(recipeType, recipeHolder);
            builder2.put((Object)recipeHolder.id(), recipeHolder);
        }
        this.byType = builder.build();
        this.byName = builder2.build();
    }

    public static <I extends RecipeInput, T extends Recipe<I>> CachedCheck<I, T> createCheck(final RecipeType<T> recipeType) {
        return new CachedCheck<I, T>(){
            @Nullable
            private ResourceLocation lastRecipe;

            @Override
            public Optional<RecipeHolder<T>> getRecipeFor(I i, Level level) {
                RecipeManager recipeManager = level.getRecipeManager();
                Optional optional = recipeManager.getRecipeFor(recipeType, i, level, this.lastRecipe);
                if (optional.isPresent()) {
                    RecipeHolder recipeHolder = optional.get();
                    this.lastRecipe = recipeHolder.id();
                    return Optional.of(recipeHolder);
                }
                return Optional.empty();
            }
        };
    }

    public static interface CachedCheck<I extends RecipeInput, T extends Recipe<I>> {
        public Optional<RecipeHolder<T>> getRecipeFor(I var1, Level var2);
    }
}

