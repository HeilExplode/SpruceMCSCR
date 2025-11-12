/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import org.slf4j.Logger;

public class FileZipper
implements Closeable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path outputFile;
    private final Path tempFile;
    private final FileSystem fs;

    public FileZipper(Path path) {
        this.outputFile = path;
        this.tempFile = path.resolveSibling(path.getFileName().toString() + "_tmp");
        try {
            this.fs = Util.ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(this.tempFile, (Map<String, ?>)ImmutableMap.of((Object)"create", (Object)"true"));
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    public void add(Path path, String string) {
        try {
            Path path2 = this.fs.getPath(File.separator, new String[0]);
            Path path3 = path2.resolve(path.toString());
            Files.createDirectories(path3.getParent(), new FileAttribute[0]);
            Files.write(path3, string.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    public void add(Path path, File file) {
        try {
            Path path2 = this.fs.getPath(File.separator, new String[0]);
            Path path3 = path2.resolve(path.toString());
            Files.createDirectories(path3.getParent(), new FileAttribute[0]);
            Files.copy(file.toPath(), path3, new CopyOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    public void add(Path path2) {
        try {
            Path path3 = this.fs.getPath(File.separator, new String[0]);
            if (Files.isRegularFile(path2, new LinkOption[0])) {
                Path path4 = path3.resolve(path2.getParent().relativize(path2).toString());
                Files.copy(path4, path2, new CopyOption[0]);
                return;
            }
            try (Stream<Path> stream = Files.find(path2, Integer.MAX_VALUE, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile(), new FileVisitOption[0]);){
                for (Path path5 : stream.collect(Collectors.toList())) {
                    Path path6 = path3.resolve(path2.relativize(path5).toString());
                    Files.createDirectories(path6.getParent(), new FileAttribute[0]);
                    Files.copy(path5, path6, new CopyOption[0]);
                }
            }
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    @Override
    public void close() {
        try {
            this.fs.close();
            Files.move(this.tempFile, this.outputFile, new CopyOption[0]);
            LOGGER.info("Compressed to {}", (Object)this.outputFile);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }
}

