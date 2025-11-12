/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.Hashing
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.slf4j.Logger
 */
package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.WorldVersion;
import net.minecraft.data.CachedOutput;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class HashCache {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String HEADER_MARKER = "// ";
    private final Path rootDir;
    private final Path cacheDir;
    private final String versionId;
    private final Map<String, ProviderCache> caches;
    private final Set<String> cachesToWrite = new HashSet<String>();
    final Set<Path> cachePaths = new HashSet<Path>();
    private final int initialCount;
    private int writes;

    private Path getProviderCachePath(String string) {
        return this.cacheDir.resolve(Hashing.sha1().hashString((CharSequence)string, StandardCharsets.UTF_8).toString());
    }

    public HashCache(Path path, Collection<String> collection, WorldVersion worldVersion) throws IOException {
        this.versionId = worldVersion.getName();
        this.rootDir = path;
        this.cacheDir = path.resolve(".cache");
        Files.createDirectories(this.cacheDir, new FileAttribute[0]);
        HashMap<String, ProviderCache> hashMap = new HashMap<String, ProviderCache>();
        int n = 0;
        for (String string : collection) {
            Path path2 = this.getProviderCachePath(string);
            this.cachePaths.add(path2);
            ProviderCache providerCache = HashCache.readCache(path, path2);
            hashMap.put(string, providerCache);
            n += providerCache.count();
        }
        this.caches = hashMap;
        this.initialCount = n;
    }

    private static ProviderCache readCache(Path path, Path path2) {
        if (Files.isReadable(path2)) {
            try {
                return ProviderCache.load(path, path2);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to parse cache {}, discarding", (Object)path2, (Object)exception);
            }
        }
        return new ProviderCache("unknown", (ImmutableMap<Path, HashCode>)ImmutableMap.of());
    }

    public boolean shouldRunInThisVersion(String string) {
        ProviderCache providerCache = this.caches.get(string);
        return providerCache == null || !providerCache.version.equals(this.versionId);
    }

    public CompletableFuture<UpdateResult> generateUpdate(String string, UpdateFunction updateFunction) {
        ProviderCache providerCache = this.caches.get(string);
        if (providerCache == null) {
            throw new IllegalStateException("Provider not registered: " + string);
        }
        CacheUpdater cacheUpdater = new CacheUpdater(this, string, this.versionId, providerCache);
        return updateFunction.update(cacheUpdater).thenApply(object -> cacheUpdater.close());
    }

    public void applyUpdate(UpdateResult updateResult) {
        this.caches.put(updateResult.providerId(), updateResult.cache());
        this.cachesToWrite.add(updateResult.providerId());
        this.writes += updateResult.writes();
    }

    public void purgeStaleAndWrite() throws IOException {
        final HashSet<Path> hashSet = new HashSet<Path>();
        this.caches.forEach((string, providerCache) -> {
            if (this.cachesToWrite.contains(string)) {
                Path path = this.getProviderCachePath((String)string);
                providerCache.save(this.rootDir, path, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + string);
            }
            hashSet.addAll((Collection<Path>)providerCache.data().keySet());
        });
        hashSet.add(this.rootDir.resolve("version.json"));
        final MutableInt mutableInt = new MutableInt();
        final MutableInt mutableInt2 = new MutableInt();
        Files.walkFileTree(this.rootDir, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
                if (HashCache.this.cachePaths.contains(path)) {
                    return FileVisitResult.CONTINUE;
                }
                mutableInt.increment();
                if (hashSet.contains(path)) {
                    return FileVisitResult.CONTINUE;
                }
                try {
                    Files.delete(path);
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete file {}", (Object)path, (Object)iOException);
                }
                mutableInt2.increment();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                return this.visitFile((Path)object, basicFileAttributes);
            }
        });
        LOGGER.info("Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", new Object[]{mutableInt, this.initialCount, hashSet.size(), mutableInt2, this.writes});
    }

    static final class ProviderCache
    extends Record {
        final String version;
        private final ImmutableMap<Path, HashCode> data;

        ProviderCache(String string, ImmutableMap<Path, HashCode> immutableMap) {
            this.version = string;
            this.data = immutableMap;
        }

        @Nullable
        public HashCode get(Path path) {
            return (HashCode)this.data.get((Object)path);
        }

        public int count() {
            return this.data.size();
        }

        public static ProviderCache load(Path path, Path path2) throws IOException {
            try (BufferedReader bufferedReader = Files.newBufferedReader(path2, StandardCharsets.UTF_8);){
                String string2 = bufferedReader.readLine();
                if (!string2.startsWith(HashCache.HEADER_MARKER)) {
                    throw new IllegalStateException("Missing cache file header");
                }
                String[] stringArray = string2.substring(HashCache.HEADER_MARKER.length()).split("\t", 2);
                String string3 = stringArray[0];
                ImmutableMap.Builder builder = ImmutableMap.builder();
                bufferedReader.lines().forEach(string -> {
                    int n = string.indexOf(32);
                    builder.put((Object)path.resolve(string.substring(n + 1)), (Object)HashCode.fromString((String)string.substring(0, n)));
                });
                ProviderCache providerCache = new ProviderCache(string3, (ImmutableMap<Path, HashCode>)builder.build());
                return providerCache;
            }
        }

        public void save(Path path, Path path2, String string) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, new OpenOption[0]);){
                bufferedWriter.write(HashCache.HEADER_MARKER);
                bufferedWriter.write(this.version);
                bufferedWriter.write(9);
                bufferedWriter.write(string);
                bufferedWriter.newLine();
                for (Map.Entry entry : this.data.entrySet()) {
                    bufferedWriter.write(((HashCode)entry.getValue()).toString());
                    bufferedWriter.write(32);
                    bufferedWriter.write(path.relativize((Path)entry.getKey()).toString());
                    bufferedWriter.newLine();
                }
            }
            catch (IOException iOException) {
                LOGGER.warn("Unable write cachefile {}: {}", (Object)path2, (Object)iOException);
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ProviderCache.class, "version;data", "version", "data"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ProviderCache.class, "version;data", "version", "data"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ProviderCache.class, "version;data", "version", "data"}, this, object);
        }

        public String version() {
            return this.version;
        }

        public ImmutableMap<Path, HashCode> data() {
            return this.data;
        }
    }

    class CacheUpdater
    implements CachedOutput {
        private final String provider;
        private final ProviderCache oldCache;
        private final ProviderCacheBuilder newCache;
        private final AtomicInteger writes = new AtomicInteger();
        private volatile boolean closed;

        CacheUpdater(HashCache hashCache, String string, String string2, ProviderCache providerCache) {
            this.provider = string;
            this.oldCache = providerCache;
            this.newCache = new ProviderCacheBuilder(string2);
        }

        private boolean shouldWrite(Path path, HashCode hashCode) {
            return !Objects.equals(this.oldCache.get(path), hashCode) || !Files.exists(path, new LinkOption[0]);
        }

        @Override
        public void writeIfNeeded(Path path, byte[] byArray, HashCode hashCode) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("Cannot write to cache as it has already been closed");
            }
            if (this.shouldWrite(path, hashCode)) {
                this.writes.incrementAndGet();
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
                Files.write(path, byArray, new OpenOption[0]);
            }
            this.newCache.put(path, hashCode);
        }

        public UpdateResult close() {
            this.closed = true;
            return new UpdateResult(this.provider, this.newCache.build(), this.writes.get());
        }
    }

    @FunctionalInterface
    public static interface UpdateFunction {
        public CompletableFuture<?> update(CachedOutput var1);
    }

    public record UpdateResult(String providerId, ProviderCache cache, int writes) {
    }

    record ProviderCacheBuilder(String version, ConcurrentMap<Path, HashCode> data) {
        ProviderCacheBuilder(String string) {
            this(string, new ConcurrentHashMap<Path, HashCode>());
        }

        public void put(Path path, HashCode hashCode) {
            this.data.put(path, hashCode);
        }

        public ProviderCache build() {
            return new ProviderCache(this.version, (ImmutableMap<Path, HashCode>)ImmutableMap.copyOf(this.data));
        }
    }
}

