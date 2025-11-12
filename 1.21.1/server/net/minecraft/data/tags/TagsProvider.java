/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;

public abstract class TagsProvider<T>
implements DataProvider {
    protected final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final CompletableFuture<Void> contentsDone = new CompletableFuture();
    private final CompletableFuture<TagLookup<T>> parentProvider;
    protected final ResourceKey<? extends Registry<T>> registryKey;
    private final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(PackOutput packOutput, ResourceKey<? extends Registry<T>> resourceKey, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this(packOutput, resourceKey, completableFuture, CompletableFuture.completedFuture(TagLookup.empty()));
    }

    protected TagsProvider(PackOutput packOutput, ResourceKey<? extends Registry<T>> resourceKey, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<T>> completableFuture2) {
        this.pathProvider = packOutput.createRegistryTagsPathProvider(resourceKey);
        this.registryKey = resourceKey;
        this.parentProvider = completableFuture2;
        this.lookupProvider = completableFuture;
    }

    @Override
    public final String getName() {
        return "Tags for " + String.valueOf(this.registryKey.location());
    }

    protected abstract void addTags(HolderLookup.Provider var1);

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        final class CombinedData<T>
        extends Record {
            final HolderLookup.Provider contents;
            final TagLookup<T> parent;

            CombinedData(HolderLookup.Provider provider, TagLookup<T> tagLookup) {
                this.contents = provider;
                this.parent = tagLookup;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this, object);
            }

            public HolderLookup.Provider contents() {
                return this.contents;
            }

            public TagLookup<T> parent() {
                return this.parent;
            }
        }
        return ((CompletableFuture)((CompletableFuture)this.createContentsProvider().thenApply(provider -> {
            this.contentsDone.complete(null);
            return provider;
        })).thenCombineAsync(this.parentProvider, (provider, tagLookup) -> new CombinedData((HolderLookup.Provider)provider, tagLookup), (Executor)Util.backgroundExecutor())).thenCompose(combinedData -> {
            HolderLookup.RegistryLookup registryLookup = combinedData.contents.lookupOrThrow(this.registryKey);
            Predicate<ResourceLocation> predicate = resourceLocation -> registryLookup.get(ResourceKey.create(this.registryKey, resourceLocation)).isPresent();
            Predicate<ResourceLocation> predicate2 = resourceLocation -> this.builders.containsKey(resourceLocation) || combinedData.parent.contains(TagKey.create(this.registryKey, resourceLocation));
            return CompletableFuture.allOf((CompletableFuture[])this.builders.entrySet().stream().map(entry -> {
                ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
                TagBuilder tagBuilder = (TagBuilder)entry.getValue();
                List<TagEntry> list = tagBuilder.build();
                List<TagEntry> list2 = list.stream().filter(tagEntry -> !tagEntry.verifyIfPresent(predicate, predicate2)).toList();
                if (!list2.isEmpty()) {
                    throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", resourceLocation, list2.stream().map(Objects::toString).collect(Collectors.joining(","))));
                }
                Path path = this.pathProvider.json(resourceLocation);
                return DataProvider.saveStable(cachedOutput, combinedData.contents, TagFile.CODEC, new TagFile(list, false), path);
            }).toArray(CompletableFuture[]::new));
        });
    }

    protected TagAppender<T> tag(TagKey<T> tagKey) {
        TagBuilder tagBuilder = this.getOrCreateRawBuilder(tagKey);
        return new TagAppender(tagBuilder);
    }

    protected TagBuilder getOrCreateRawBuilder(TagKey<T> tagKey) {
        return this.builders.computeIfAbsent(tagKey.location(), resourceLocation -> TagBuilder.create());
    }

    public CompletableFuture<TagLookup<T>> contentsGetter() {
        return this.contentsDone.thenApply(void_ -> tagKey -> Optional.ofNullable(this.builders.get(tagKey.location())));
    }

    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return this.lookupProvider.thenApply(provider -> {
            this.builders.clear();
            this.addTags((HolderLookup.Provider)provider);
            return provider;
        });
    }

    @FunctionalInterface
    public static interface TagLookup<T>
    extends Function<TagKey<T>, Optional<TagBuilder>> {
        public static <T> TagLookup<T> empty() {
            return tagKey -> Optional.empty();
        }

        default public boolean contains(TagKey<T> tagKey) {
            return ((Optional)this.apply(tagKey)).isPresent();
        }
    }

    protected static class TagAppender<T> {
        private final TagBuilder builder;

        protected TagAppender(TagBuilder tagBuilder) {
            this.builder = tagBuilder;
        }

        public final TagAppender<T> add(ResourceKey<T> resourceKey) {
            this.builder.addElement(resourceKey.location());
            return this;
        }

        @SafeVarargs
        public final TagAppender<T> add(ResourceKey<T> ... resourceKeyArray) {
            for (ResourceKey<T> resourceKey : resourceKeyArray) {
                this.builder.addElement(resourceKey.location());
            }
            return this;
        }

        public final TagAppender<T> addAll(List<ResourceKey<T>> list) {
            for (ResourceKey<T> resourceKey : list) {
                this.builder.addElement(resourceKey.location());
            }
            return this;
        }

        public TagAppender<T> addOptional(ResourceLocation resourceLocation) {
            this.builder.addOptionalElement(resourceLocation);
            return this;
        }

        public TagAppender<T> addTag(TagKey<T> tagKey) {
            this.builder.addTag(tagKey.location());
            return this;
        }

        public TagAppender<T> addOptionalTag(ResourceLocation resourceLocation) {
            this.builder.addOptionalTag(resourceLocation);
            return this;
        }
    }
}

