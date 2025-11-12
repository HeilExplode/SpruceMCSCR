/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.slf4j.Logger;

public class FallbackResourceManager
implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<PackEntry> fallbacks = Lists.newArrayList();
    private final PackType type;
    private final String namespace;

    public FallbackResourceManager(PackType packType, String string) {
        this.type = packType;
        this.namespace = string;
    }

    public void push(PackResources packResources) {
        this.pushInternal(packResources.packId(), packResources, null);
    }

    public void push(PackResources packResources, Predicate<ResourceLocation> predicate) {
        this.pushInternal(packResources.packId(), packResources, predicate);
    }

    public void pushFilterOnly(String string, Predicate<ResourceLocation> predicate) {
        this.pushInternal(string, null, predicate);
    }

    private void pushInternal(String string, @Nullable PackResources packResources, @Nullable Predicate<ResourceLocation> predicate) {
        this.fallbacks.add(new PackEntry(string, packResources, predicate));
    }

    @Override
    public Set<String> getNamespaces() {
        return ImmutableSet.of((Object)this.namespace);
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation resourceLocation) {
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            IoSupplier<InputStream> ioSupplier;
            PackEntry packEntry = this.fallbacks.get(i);
            PackResources packResources = packEntry.resources;
            if (packResources != null && (ioSupplier = packResources.getResource(this.type, resourceLocation)) != null) {
                IoSupplier<ResourceMetadata> ioSupplier2 = this.createStackMetadataFinder(resourceLocation, i);
                return Optional.of(FallbackResourceManager.createResource(packResources, resourceLocation, ioSupplier, ioSupplier2));
            }
            if (!packEntry.isFiltered(resourceLocation)) continue;
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)resourceLocation, (Object)packEntry.name);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Resource createResource(PackResources packResources, ResourceLocation resourceLocation, IoSupplier<InputStream> ioSupplier, IoSupplier<ResourceMetadata> ioSupplier2) {
        return new Resource(packResources, FallbackResourceManager.wrapForDebug(resourceLocation, packResources, ioSupplier), ioSupplier2);
    }

    private static IoSupplier<InputStream> wrapForDebug(ResourceLocation resourceLocation, PackResources packResources, IoSupplier<InputStream> ioSupplier) {
        if (LOGGER.isDebugEnabled()) {
            return () -> new LeakedResourceWarningInputStream((InputStream)ioSupplier.get(), resourceLocation, packResources.packId());
        }
        return ioSupplier;
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
        ArrayList<Resource> arrayList = new ArrayList<Resource>();
        boolean bl = false;
        String string = null;
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            IoSupplier<InputStream> ioSupplier;
            PackEntry packEntry = this.fallbacks.get(i);
            PackResources packResources = packEntry.resources;
            if (packResources != null && (ioSupplier = packResources.getResource(this.type, resourceLocation)) != null) {
                IoSupplier<ResourceMetadata> ioSupplier2 = bl ? ResourceMetadata.EMPTY_SUPPLIER : () -> {
                    IoSupplier<InputStream> ioSupplier = packResources.getResource(this.type, resourceLocation2);
                    return ioSupplier != null ? FallbackResourceManager.parseMetadata(ioSupplier) : ResourceMetadata.EMPTY;
                };
                arrayList.add(new Resource(packResources, ioSupplier, ioSupplier2));
            }
            if (packEntry.isFiltered(resourceLocation)) {
                string = packEntry.name;
                break;
            }
            if (!packEntry.isFiltered(resourceLocation2)) continue;
            bl = true;
        }
        if (arrayList.isEmpty() && string != null) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)resourceLocation, string);
        }
        return Lists.reverse(arrayList);
    }

    private static boolean isMetadata(ResourceLocation resourceLocation) {
        return resourceLocation.getPath().endsWith(".mcmeta");
    }

    private static ResourceLocation getResourceLocationFromMetadata(ResourceLocation resourceLocation) {
        String string = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - ".mcmeta".length());
        return resourceLocation.withPath(string);
    }

    static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
        return resourceLocation.withPath(resourceLocation.getPath() + ".mcmeta");
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
        final class ResourceWithSourceAndIndex
        extends Record {
            final PackResources packResources;
            final IoSupplier<InputStream> resource;
            final int packIndex;

            ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> ioSupplier, int n) {
                this.packResources = packResources;
                this.resource = ioSupplier;
                this.packIndex = n;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this, object);
            }

            public PackResources packResources() {
                return this.packResources;
            }

            public IoSupplier<InputStream> resource() {
                return this.resource;
            }

            public int packIndex() {
                return this.packIndex;
            }
        }
        HashMap<ResourceLocation, ResourceWithSourceAndIndex> hashMap = new HashMap<ResourceLocation, ResourceWithSourceAndIndex>();
        HashMap hashMap2 = new HashMap();
        int n = this.fallbacks.size();
        for (int i = 0; i < n; ++i) {
            PackEntry packEntry = this.fallbacks.get(i);
            packEntry.filterAll(hashMap.keySet());
            packEntry.filterAll(hashMap2.keySet());
            PackResources packResources = packEntry.resources;
            if (packResources == null) continue;
            int n2 = i;
            packResources.listResources(this.type, this.namespace, string, (resourceLocation, ioSupplier) -> {
                if (FallbackResourceManager.isMetadata(resourceLocation)) {
                    if (predicate.test(FallbackResourceManager.getResourceLocationFromMetadata(resourceLocation))) {
                        hashMap2.put(resourceLocation, new ResourceWithSourceAndIndex(packResources, (IoSupplier<InputStream>)ioSupplier, n2));
                    }
                } else if (predicate.test((ResourceLocation)resourceLocation)) {
                    hashMap.put((ResourceLocation)resourceLocation, new ResourceWithSourceAndIndex(packResources, (IoSupplier<InputStream>)ioSupplier, n2));
                }
            });
        }
        TreeMap treeMap = Maps.newTreeMap();
        hashMap.forEach((resourceLocation, resourceWithSourceAndIndex) -> {
            ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
            ResourceWithSourceAndIndex resourceWithSourceAndIndex2 = (ResourceWithSourceAndIndex)hashMap2.get(resourceLocation2);
            IoSupplier<ResourceMetadata> ioSupplier = resourceWithSourceAndIndex2 != null && resourceWithSourceAndIndex2.packIndex >= resourceWithSourceAndIndex.packIndex ? FallbackResourceManager.convertToMetadata(resourceWithSourceAndIndex2.resource) : ResourceMetadata.EMPTY_SUPPLIER;
            treeMap.put(resourceLocation, FallbackResourceManager.createResource(resourceWithSourceAndIndex.packResources, resourceLocation, resourceWithSourceAndIndex.resource, ioSupplier));
        });
        return treeMap;
    }

    private IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation resourceLocation, int n) {
        return () -> {
            ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
            for (int i = this.fallbacks.size() - 1; i >= n; --i) {
                IoSupplier<InputStream> ioSupplier;
                PackEntry packEntry = this.fallbacks.get(i);
                PackResources packResources = packEntry.resources;
                if (packResources != null && (ioSupplier = packResources.getResource(this.type, resourceLocation2)) != null) {
                    return FallbackResourceManager.parseMetadata(ioSupplier);
                }
                if (packEntry.isFiltered(resourceLocation2)) break;
            }
            return ResourceMetadata.EMPTY;
        };
    }

    private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> ioSupplier) {
        return () -> FallbackResourceManager.parseMetadata(ioSupplier);
    }

    private static ResourceMetadata parseMetadata(IoSupplier<InputStream> ioSupplier) throws IOException {
        try (InputStream inputStream = ioSupplier.get();){
            ResourceMetadata resourceMetadata = ResourceMetadata.fromJsonStream(inputStream);
            return resourceMetadata;
        }
    }

    private static void applyPackFiltersToExistingResources(PackEntry packEntry, Map<ResourceLocation, EntryStack> map) {
        for (EntryStack entryStack : map.values()) {
            if (packEntry.isFiltered(entryStack.fileLocation)) {
                entryStack.fileSources.clear();
                continue;
            }
            if (!packEntry.isFiltered(entryStack.metadataLocation())) continue;
            entryStack.metaSources.clear();
        }
    }

    private void listPackResources(PackEntry packEntry, String string, Predicate<ResourceLocation> predicate, Map<ResourceLocation, EntryStack> map) {
        PackResources packResources = packEntry.resources;
        if (packResources == null) {
            return;
        }
        packResources.listResources(this.type, this.namespace, string, (resourceLocation, ioSupplier) -> {
            if (FallbackResourceManager.isMetadata(resourceLocation)) {
                ResourceLocation resourceLocation2 = FallbackResourceManager.getResourceLocationFromMetadata(resourceLocation);
                if (!predicate.test(resourceLocation2)) {
                    return;
                }
                map.computeIfAbsent(resourceLocation2, (Function<ResourceLocation, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.ResourceLocation ), (Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).metaSources.put(packResources, (IoSupplier<InputStream>)ioSupplier);
            } else {
                if (!predicate.test((ResourceLocation)resourceLocation)) {
                    return;
                }
                map.computeIfAbsent(resourceLocation, (Function<ResourceLocation, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.ResourceLocation ), (Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).fileSources.add(new ResourceWithSource(packResources, (IoSupplier<InputStream>)ioSupplier));
            }
        });
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
        HashMap hashMap = Maps.newHashMap();
        for (PackEntry object : this.fallbacks) {
            FallbackResourceManager.applyPackFiltersToExistingResources(object, hashMap);
            this.listPackResources(object, string, predicate, hashMap);
        }
        TreeMap treeMap = Maps.newTreeMap();
        for (EntryStack entryStack : hashMap.values()) {
            if (entryStack.fileSources.isEmpty()) continue;
            ArrayList<Resource> arrayList = new ArrayList<Resource>();
            for (ResourceWithSource resourceWithSource : entryStack.fileSources) {
                PackResources packResources = resourceWithSource.source;
                IoSupplier<InputStream> ioSupplier = entryStack.metaSources.get(packResources);
                IoSupplier<ResourceMetadata> ioSupplier2 = ioSupplier != null ? FallbackResourceManager.convertToMetadata(ioSupplier) : ResourceMetadata.EMPTY_SUPPLIER;
                arrayList.add(FallbackResourceManager.createResource(packResources, entryStack.fileLocation, resourceWithSource.resource, ioSupplier2));
            }
            treeMap.put(entryStack.fileLocation, arrayList);
        }
        return treeMap;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.fallbacks.stream().map(packEntry -> packEntry.resources).filter(Objects::nonNull);
    }

    static final class PackEntry
    extends Record {
        final String name;
        @Nullable
        final PackResources resources;
        @Nullable
        private final Predicate<ResourceLocation> filter;

        PackEntry(String string, @Nullable PackResources packResources, @Nullable Predicate<ResourceLocation> predicate) {
            this.name = string;
            this.resources = packResources;
            this.filter = predicate;
        }

        public void filterAll(Collection<ResourceLocation> collection) {
            if (this.filter != null) {
                collection.removeIf(this.filter);
            }
        }

        public boolean isFiltered(ResourceLocation resourceLocation) {
            return this.filter != null && this.filter.test(resourceLocation);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this, object);
        }

        public String name() {
            return this.name;
        }

        @Nullable
        public PackResources resources() {
            return this.resources;
        }

        @Nullable
        public Predicate<ResourceLocation> filter() {
            return this.filter;
        }
    }

    static final class EntryStack
    extends Record {
        final ResourceLocation fileLocation;
        private final ResourceLocation metadataLocation;
        final List<ResourceWithSource> fileSources;
        final Map<PackResources, IoSupplier<InputStream>> metaSources;

        EntryStack(ResourceLocation resourceLocation) {
            this(resourceLocation, FallbackResourceManager.getMetadataLocation(resourceLocation), new ArrayList<ResourceWithSource>(), (Map<PackResources, IoSupplier<InputStream>>)new Object2ObjectArrayMap());
        }

        private EntryStack(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, List<ResourceWithSource> list, Map<PackResources, IoSupplier<InputStream>> map) {
            this.fileLocation = resourceLocation;
            this.metadataLocation = resourceLocation2;
            this.fileSources = list;
            this.metaSources = map;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this, object);
        }

        public ResourceLocation fileLocation() {
            return this.fileLocation;
        }

        public ResourceLocation metadataLocation() {
            return this.metadataLocation;
        }

        public List<ResourceWithSource> fileSources() {
            return this.fileSources;
        }

        public Map<PackResources, IoSupplier<InputStream>> metaSources() {
            return this.metaSources;
        }
    }

    static final class ResourceWithSource
    extends Record {
        final PackResources source;
        final IoSupplier<InputStream> resource;

        ResourceWithSource(PackResources packResources, IoSupplier<InputStream> ioSupplier) {
            this.source = packResources;
            this.resource = ioSupplier;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this, object);
        }

        public PackResources source() {
            return this.source;
        }

        public IoSupplier<InputStream> resource() {
            return this.resource;
        }
    }

    static class LeakedResourceWarningInputStream
    extends FilterInputStream {
        private final Supplier<String> message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream inputStream, ResourceLocation resourceLocation, String string) {
            super(inputStream);
            Exception exception = new Exception("Stacktrace");
            this.message = () -> {
                StringWriter stringWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(stringWriter));
                return "Leaked resource: '" + String.valueOf(resourceLocation) + "' loaded from pack: '" + string + "'\n" + String.valueOf(stringWriter);
            };
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn("{}", (Object)this.message.get());
            }
            super.finalize();
        }
    }
}

