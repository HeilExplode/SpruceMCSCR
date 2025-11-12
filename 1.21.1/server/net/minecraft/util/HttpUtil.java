/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Funnels
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.HashFunction
 *  com.google.common.hash.Hasher
 *  com.google.common.hash.PrimitiveSink
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.PrimitiveSink;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;
import java.util.OptionalLong;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class HttpUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private HttpUtil() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    public static Path downloadFile(Path path, URL uRL, Map<String, String> map, HashFunction hashFunction, @Nullable HashCode hashCode, int n, Proxy proxy, DownloadProgressListener downloadProgressListener) {
        InputStream inputStream;
        HttpURLConnection httpURLConnection;
        block21: {
            Path path2;
            httpURLConnection = null;
            inputStream = null;
            downloadProgressListener.requestStart();
            if (hashCode != null) {
                path2 = HttpUtil.cachedFilePath(path, hashCode);
                try {
                    if (HttpUtil.checkExistingFile(path2, hashFunction, hashCode)) {
                        LOGGER.info("Returning cached file since actual hash matches requested");
                        downloadProgressListener.requestFinished(true);
                        HttpUtil.updateModificationTime(path2);
                        return path2;
                    }
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to check cached file {}", (Object)path2, (Object)iOException);
                }
                try {
                    LOGGER.warn("Existing file {} not found or had mismatched hash", (Object)path2);
                    Files.deleteIfExists(path2);
                }
                catch (IOException iOException) {
                    downloadProgressListener.requestFinished(false);
                    throw new UncheckedIOException("Failed to remove existing file " + String.valueOf(path2), iOException);
                }
            }
            path2 = null;
            httpURLConnection = (HttpURLConnection)uRL.openConnection(proxy);
            httpURLConnection.setInstanceFollowRedirects(true);
            map.forEach(httpURLConnection::setRequestProperty);
            inputStream = httpURLConnection.getInputStream();
            long l = httpURLConnection.getContentLengthLong();
            OptionalLong optionalLong = l != -1L ? OptionalLong.of(l) : OptionalLong.empty();
            FileUtil.createDirectoriesSafe(path);
            downloadProgressListener.downloadStart(optionalLong);
            if (optionalLong.isPresent() && optionalLong.getAsLong() > (long)n) {
                throw new IOException("Filesize is bigger than maximum allowed (file is " + String.valueOf(optionalLong) + ", limit is " + n + ")");
            }
            if (path2 == null) break block21;
            HashCode hashCode2 = HttpUtil.downloadAndHash(hashFunction, n, downloadProgressListener, inputStream, path2);
            if (!hashCode2.equals((Object)hashCode)) {
                throw new IOException("Hash of downloaded file (" + String.valueOf(hashCode2) + ") did not match requested (" + String.valueOf(hashCode) + ")");
            }
            downloadProgressListener.requestFinished(true);
            Path path3 = path2;
            IOUtils.closeQuietly((InputStream)inputStream);
            return path3;
        }
        Path path4 = Files.createTempFile(path, "download", ".tmp", new FileAttribute[0]);
        HashCode hashCode3 = HttpUtil.downloadAndHash(hashFunction, n, downloadProgressListener, inputStream, path4);
        Path path5 = HttpUtil.cachedFilePath(path, hashCode3);
        if (!HttpUtil.checkExistingFile(path5, hashFunction, hashCode3)) {
            Files.move(path4, path5, StandardCopyOption.REPLACE_EXISTING);
        } else {
            HttpUtil.updateModificationTime(path5);
        }
        downloadProgressListener.requestFinished(true);
        Path path6 = path5;
        Files.deleteIfExists(path4);
        IOUtils.closeQuietly((InputStream)inputStream);
        return path6;
        {
            catch (Throwable throwable) {
                try {
                    try {
                        Files.deleteIfExists(path4);
                        throw throwable;
                    }
                    catch (Throwable throwable2) {
                        InputStream inputStream2;
                        if (httpURLConnection != null && (inputStream2 = httpURLConnection.getErrorStream()) != null) {
                            try {
                                LOGGER.error("HTTP response error: {}", (Object)IOUtils.toString((InputStream)inputStream2, (Charset)StandardCharsets.UTF_8));
                            }
                            catch (Exception exception) {
                                LOGGER.error("Failed to read response from server");
                            }
                        }
                        downloadProgressListener.requestFinished(false);
                        throw new IllegalStateException("Failed to download file " + String.valueOf(uRL), throwable2);
                    }
                }
                catch (Throwable throwable3) {
                    IOUtils.closeQuietly(inputStream);
                    throw throwable3;
                }
            }
        }
    }

    private static void updateModificationTime(Path path) {
        try {
            Files.setLastModifiedTime(path, FileTime.from(Instant.now()));
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to update modification time of {}", (Object)path, (Object)iOException);
        }
    }

    private static HashCode hashFile(Path path, HashFunction hashFunction) throws IOException {
        Hasher hasher = hashFunction.newHasher();
        try (OutputStream outputStream = Funnels.asOutputStream((PrimitiveSink)hasher);
             InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
            inputStream.transferTo(outputStream);
        }
        return hasher.hash();
    }

    private static boolean checkExistingFile(Path path, HashFunction hashFunction, HashCode hashCode) throws IOException {
        if (Files.exists(path, new LinkOption[0])) {
            HashCode hashCode2 = HttpUtil.hashFile(path, hashFunction);
            if (hashCode2.equals((Object)hashCode)) {
                return true;
            }
            LOGGER.warn("Mismatched hash of file {}, expected {} but found {}", new Object[]{path, hashCode, hashCode2});
        }
        return false;
    }

    private static Path cachedFilePath(Path path, HashCode hashCode) {
        return path.resolve(hashCode.toString());
    }

    private static HashCode downloadAndHash(HashFunction hashFunction, int n, DownloadProgressListener downloadProgressListener, InputStream inputStream, Path path) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);){
            int n2;
            Hasher hasher = hashFunction.newHasher();
            byte[] byArray = new byte[8196];
            long l = 0L;
            while ((n2 = inputStream.read(byArray)) >= 0) {
                downloadProgressListener.downloadedBytes(l += (long)n2);
                if (l > (long)n) {
                    throw new IOException("Filesize was bigger than maximum allowed (got >= " + l + ", limit was " + n + ")");
                }
                if (Thread.interrupted()) {
                    LOGGER.error("INTERRUPTED");
                    throw new IOException("Download interrupted");
                }
                outputStream.write(byArray, 0, n2);
                hasher.putBytes(byArray, 0, n2);
            }
            HashCode hashCode = hasher.hash();
            return hashCode;
        }
    }

    public static int getAvailablePort() {
        int n;
        ServerSocket serverSocket = new ServerSocket(0);
        try {
            n = serverSocket.getLocalPort();
        }
        catch (Throwable throwable) {
            try {
                try {
                    serverSocket.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                return 25564;
            }
        }
        serverSocket.close();
        return n;
    }

    public static boolean isPortAvailable(int n) {
        boolean bl;
        if (n < 0 || n > 65535) {
            return false;
        }
        ServerSocket serverSocket = new ServerSocket(n);
        try {
            bl = serverSocket.getLocalPort() == n;
        }
        catch (Throwable throwable) {
            try {
                try {
                    serverSocket.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                return false;
            }
        }
        serverSocket.close();
        return bl;
    }

    public static interface DownloadProgressListener {
        public void requestStart();

        public void downloadStart(OptionalLong var1);

        public void downloadedBytes(long var1);

        public void requestFinished(boolean var1);
    }
}

