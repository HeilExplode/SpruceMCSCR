/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.HashFunction
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.packs.DownloadCacheCleaner;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ProcessorMailbox;
import org.slf4j.Logger;

public class DownloadQueue
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_KEPT_PACKS = 20;
    private final Path cacheDir;
    private final JsonEventLog<LogEntry> eventLog;
    private final ProcessorMailbox<Runnable> tasks = ProcessorMailbox.create(Util.nonCriticalIoPool(), "download-queue");

    public DownloadQueue(Path path) throws IOException {
        this.cacheDir = path;
        FileUtil.createDirectoriesSafe(path);
        this.eventLog = JsonEventLog.open(LogEntry.CODEC, path.resolve("log.json"));
        DownloadCacheCleaner.vacuumCacheDir(path, 20);
    }

    private BatchResult runDownload(BatchConfig batchConfig, Map<UUID, DownloadRequest> map) {
        BatchResult batchResult = new BatchResult();
        map.forEach((uUID, downloadRequest) -> {
            Path path = this.cacheDir.resolve(uUID.toString());
            Path path2 = null;
            try {
                path2 = HttpUtil.downloadFile(path, downloadRequest.url, batchConfig.headers, batchConfig.hashFunction, downloadRequest.hash, batchConfig.maxSize, batchConfig.proxy, batchConfig.listener);
                batchResult.downloaded.put((UUID)uUID, path2);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to download {}", (Object)downloadRequest.url, (Object)exception);
                batchResult.failed.add((UUID)uUID);
            }
            try {
                this.eventLog.write(new LogEntry((UUID)uUID, downloadRequest.url.toString(), Instant.now(), Optional.ofNullable(downloadRequest.hash).map(HashCode::toString), path2 != null ? this.getFileInfo(path2) : Either.left((Object)"download_failed")));
            }
            catch (Exception exception) {
                LOGGER.error("Failed to log download of {}", (Object)downloadRequest.url, (Object)exception);
            }
        });
        return batchResult;
    }

    private Either<String, FileInfoEntry> getFileInfo(Path path) {
        try {
            long l = Files.size(path);
            Path path2 = this.cacheDir.relativize(path);
            return Either.right((Object)new FileInfoEntry(path2.toString(), l));
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to get file size of {}", (Object)path, (Object)iOException);
            return Either.left((Object)"no_access");
        }
    }

    public CompletableFuture<BatchResult> downloadBatch(BatchConfig batchConfig, Map<UUID, DownloadRequest> map) {
        return CompletableFuture.supplyAsync(() -> this.runDownload(batchConfig, map), this.tasks::tell);
    }

    @Override
    public void close() throws IOException {
        this.tasks.close();
        this.eventLog.close();
    }

    record LogEntry(UUID id, String url, Instant time, Optional<String> hash, Either<String, FileInfoEntry> errorOrFileInfo) {
        public static final Codec<LogEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(LogEntry::id), (App)Codec.STRING.fieldOf("url").forGetter(LogEntry::url), (App)ExtraCodecs.INSTANT_ISO8601.fieldOf("time").forGetter(LogEntry::time), (App)Codec.STRING.optionalFieldOf("hash").forGetter(LogEntry::hash), (App)Codec.mapEither((MapCodec)Codec.STRING.fieldOf("error"), (MapCodec)FileInfoEntry.CODEC.fieldOf("file")).forGetter(LogEntry::errorOrFileInfo)).apply((Applicative)instance, LogEntry::new));
    }

    public static final class BatchResult
    extends Record {
        final Map<UUID, Path> downloaded;
        final Set<UUID> failed;

        public BatchResult() {
            this(new HashMap<UUID, Path>(), new HashSet<UUID>());
        }

        public BatchResult(Map<UUID, Path> map, Set<UUID> set) {
            this.downloaded = map;
            this.failed = set;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BatchResult.class, "downloaded;failed", "downloaded", "failed"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BatchResult.class, "downloaded;failed", "downloaded", "failed"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BatchResult.class, "downloaded;failed", "downloaded", "failed"}, this, object);
        }

        public Map<UUID, Path> downloaded() {
            return this.downloaded;
        }

        public Set<UUID> failed() {
            return this.failed;
        }
    }

    public static final class BatchConfig
    extends Record {
        final HashFunction hashFunction;
        final int maxSize;
        final Map<String, String> headers;
        final Proxy proxy;
        final HttpUtil.DownloadProgressListener listener;

        public BatchConfig(HashFunction hashFunction, int n, Map<String, String> map, Proxy proxy, HttpUtil.DownloadProgressListener downloadProgressListener) {
            this.hashFunction = hashFunction;
            this.maxSize = n;
            this.headers = map;
            this.proxy = proxy;
            this.listener = downloadProgressListener;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BatchConfig.class, "hashFunction;maxSize;headers;proxy;listener", "hashFunction", "maxSize", "headers", "proxy", "listener"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BatchConfig.class, "hashFunction;maxSize;headers;proxy;listener", "hashFunction", "maxSize", "headers", "proxy", "listener"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BatchConfig.class, "hashFunction;maxSize;headers;proxy;listener", "hashFunction", "maxSize", "headers", "proxy", "listener"}, this, object);
        }

        public HashFunction hashFunction() {
            return this.hashFunction;
        }

        public int maxSize() {
            return this.maxSize;
        }

        public Map<String, String> headers() {
            return this.headers;
        }

        public Proxy proxy() {
            return this.proxy;
        }

        public HttpUtil.DownloadProgressListener listener() {
            return this.listener;
        }
    }

    record FileInfoEntry(String name, long size) {
        public static final Codec<FileInfoEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(FileInfoEntry::name), (App)Codec.LONG.fieldOf("size").forGetter(FileInfoEntry::size)).apply((Applicative)instance, FileInfoEntry::new));
    }

    public static final class DownloadRequest
    extends Record {
        final URL url;
        @Nullable
        final HashCode hash;

        public DownloadRequest(URL uRL, @Nullable HashCode hashCode) {
            this.url = uRL;
            this.hash = hashCode;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DownloadRequest.class, "url;hash", "url", "hash"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DownloadRequest.class, "url;hash", "url", "hash"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DownloadRequest.class, "url;hash", "url", "hash"}, this, object);
        }

        public URL url() {
            return this.url;
        }

        @Nullable
        public HashCode hash() {
            return this.hash;
        }
    }
}

