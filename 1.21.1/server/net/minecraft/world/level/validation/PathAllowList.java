/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.validation;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class PathAllowList
implements PathMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMMENT_PREFIX = "#";
    private final List<ConfigEntry> entries;
    private final Map<String, PathMatcher> compiledPaths = new ConcurrentHashMap<String, PathMatcher>();

    public PathAllowList(List<ConfigEntry> list) {
        this.entries = list;
    }

    public PathMatcher getForFileSystem(FileSystem fileSystem) {
        return this.compiledPaths.computeIfAbsent(fileSystem.provider().getScheme(), string -> {
            List<PathMatcher> list;
            try {
                list = this.entries.stream().map(configEntry -> configEntry.compile(fileSystem)).toList();
            }
            catch (Exception exception) {
                LOGGER.error("Failed to compile file pattern list", (Throwable)exception);
                return path -> false;
            }
            return switch (list.size()) {
                case 0 -> path -> false;
                case 1 -> list.get(0);
                default -> path -> {
                    for (PathMatcher pathMatcher : list) {
                        if (!pathMatcher.matches(path)) continue;
                        return true;
                    }
                    return false;
                };
            };
        });
    }

    @Override
    public boolean matches(Path path) {
        return this.getForFileSystem(path.getFileSystem()).matches(path);
    }

    public static PathAllowList readPlain(BufferedReader bufferedReader) {
        return new PathAllowList(bufferedReader.lines().flatMap(string -> ConfigEntry.parse(string).stream()).toList());
    }

    public record ConfigEntry(EntryType type, String pattern) {
        public PathMatcher compile(FileSystem fileSystem) {
            return this.type().compile(fileSystem, this.pattern);
        }

        static Optional<ConfigEntry> parse(String string) {
            if (string.isBlank() || string.startsWith(PathAllowList.COMMENT_PREFIX)) {
                return Optional.empty();
            }
            if (!string.startsWith("[")) {
                return Optional.of(new ConfigEntry(EntryType.PREFIX, string));
            }
            int n = string.indexOf(93, 1);
            if (n == -1) {
                throw new IllegalArgumentException("Unterminated type in line '" + string + "'");
            }
            String string2 = string.substring(1, n);
            String string3 = string.substring(n + 1);
            return switch (string2) {
                case "glob", "regex" -> Optional.of(new ConfigEntry(EntryType.FILESYSTEM, string2 + ":" + string3));
                case "prefix" -> Optional.of(new ConfigEntry(EntryType.PREFIX, string3));
                default -> throw new IllegalArgumentException("Unsupported definition type in line '" + string + "'");
            };
        }

        static ConfigEntry glob(String string) {
            return new ConfigEntry(EntryType.FILESYSTEM, "glob:" + string);
        }

        static ConfigEntry regex(String string) {
            return new ConfigEntry(EntryType.FILESYSTEM, "regex:" + string);
        }

        static ConfigEntry prefix(String string) {
            return new ConfigEntry(EntryType.PREFIX, string);
        }
    }

    @FunctionalInterface
    public static interface EntryType {
        public static final EntryType FILESYSTEM = FileSystem::getPathMatcher;
        public static final EntryType PREFIX = (fileSystem, string) -> path -> path.toString().startsWith(string);

        public PathMatcher compile(FileSystem var1, String var2);
    }
}

