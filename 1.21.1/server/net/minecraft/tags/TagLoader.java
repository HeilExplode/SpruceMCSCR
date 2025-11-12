/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.util.DependencySorter;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Function<ResourceLocation, Optional<? extends T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<? extends T>> function, String string) {
        this.idToValue = function;
        this.directory = string;
    }

    public Map<ResourceLocation, List<EntryWithSource>> load(ResourceManager resourceManager) {
        HashMap hashMap = Maps.newHashMap();
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(this.directory);
        for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation resourceLocation2 = entry.getKey();
            ResourceLocation resourceLocation3 = fileToIdConverter.fileToId(resourceLocation2);
            for (Resource resource : entry.getValue()) {
                try {
                    BufferedReader bufferedReader = resource.openAsReader();
                    try {
                        JsonElement jsonElement = JsonParser.parseReader((Reader)bufferedReader);
                        List list = hashMap.computeIfAbsent(resourceLocation3, resourceLocation -> new ArrayList());
                        TagFile tagFile = (TagFile)TagFile.CODEC.parse(new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement)).getOrThrow();
                        if (tagFile.replace()) {
                            list.clear();
                        }
                        String string = resource.sourcePackId();
                        tagFile.entries().forEach(tagEntry -> list.add(new EntryWithSource((TagEntry)tagEntry, string)));
                    }
                    finally {
                        if (bufferedReader == null) continue;
                        ((Reader)bufferedReader).close();
                    }
                }
                catch (Exception exception) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{resourceLocation3, resourceLocation2, resource.sourcePackId(), exception});
                }
            }
        }
        return hashMap;
    }

    private Either<Collection<EntryWithSource>, Collection<T>> build(TagEntry.Lookup<T> lookup, List<EntryWithSource> list) {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        ArrayList<EntryWithSource> arrayList = new ArrayList<EntryWithSource>();
        for (EntryWithSource entryWithSource : list) {
            if (entryWithSource.entry().build(lookup, arg_0 -> ((ImmutableSet.Builder)builder).add(arg_0))) continue;
            arrayList.add(entryWithSource);
        }
        return arrayList.isEmpty() ? Either.right((Object)builder.build()) : Either.left(arrayList);
    }

    public Map<ResourceLocation, Collection<T>> build(Map<ResourceLocation, List<EntryWithSource>> map) {
        final HashMap hashMap = Maps.newHashMap();
        TagEntry.Lookup lookup = new TagEntry.Lookup<T>(){

            @Override
            @Nullable
            public T element(ResourceLocation resourceLocation) {
                return TagLoader.this.idToValue.apply(resourceLocation).orElse(null);
            }

            @Override
            @Nullable
            public Collection<T> tag(ResourceLocation resourceLocation) {
                return (Collection)hashMap.get(resourceLocation);
            }
        };
        DependencySorter<ResourceLocation, SortingEntry> dependencySorter = new DependencySorter<ResourceLocation, SortingEntry>();
        map.forEach((resourceLocation, list) -> dependencySorter.addEntry((ResourceLocation)resourceLocation, new SortingEntry((List<EntryWithSource>)list)));
        dependencySorter.orderByDependencies((resourceLocation, sortingEntry) -> this.build(lookup, sortingEntry.entries).ifLeft(collection -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", resourceLocation, (Object)collection.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight(collection -> hashMap.put(resourceLocation, collection)));
        return hashMap;
    }

    public Map<ResourceLocation, Collection<T>> loadAndBuild(ResourceManager resourceManager) {
        return this.build(this.load(resourceManager));
    }

    public static final class EntryWithSource
    extends Record {
        final TagEntry entry;
        private final String source;

        public EntryWithSource(TagEntry tagEntry, String string) {
            this.entry = tagEntry;
            this.source = string;
        }

        @Override
        public String toString() {
            return String.valueOf(this.entry) + " (from " + this.source + ")";
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryWithSource.class, "entry;source", "entry", "source"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryWithSource.class, "entry;source", "entry", "source"}, this, object);
        }

        public TagEntry entry() {
            return this.entry;
        }

        public String source() {
            return this.source;
        }
    }

    static final class SortingEntry
    extends Record
    implements DependencySorter.Entry<ResourceLocation> {
        final List<EntryWithSource> entries;

        SortingEntry(List<EntryWithSource> list) {
            this.entries = list;
        }

        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
            this.entries.forEach(entryWithSource -> entryWithSource.entry.visitRequiredDependencies(consumer));
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
            this.entries.forEach(entryWithSource -> entryWithSource.entry.visitOptionalDependencies(consumer));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this, object);
        }

        public List<EntryWithSource> entries() {
            return this.entries;
        }
    }
}

