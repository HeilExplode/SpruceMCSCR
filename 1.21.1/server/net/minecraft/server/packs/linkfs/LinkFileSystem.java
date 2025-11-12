/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.server.packs.linkfs;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.server.packs.linkfs.LinkFSFileStore;
import net.minecraft.server.packs.linkfs.LinkFSPath;
import net.minecraft.server.packs.linkfs.LinkFSProvider;
import net.minecraft.server.packs.linkfs.PathContents;

public class LinkFileSystem
extends FileSystem {
    private static final Set<String> VIEWS = Set.of("basic");
    public static final String PATH_SEPARATOR = "/";
    private static final Splitter PATH_SPLITTER = Splitter.on((char)'/');
    private final FileStore store;
    private final FileSystemProvider provider = new LinkFSProvider();
    private final LinkFSPath root;

    LinkFileSystem(String string, DirectoryEntry directoryEntry) {
        this.store = new LinkFSFileStore(string);
        this.root = LinkFileSystem.buildPath(directoryEntry, this, "", null);
    }

    private static LinkFSPath buildPath(DirectoryEntry directoryEntry2, LinkFileSystem linkFileSystem, String string2, @Nullable LinkFSPath linkFSPath) {
        Object2ObjectOpenHashMap object2ObjectOpenHashMap = new Object2ObjectOpenHashMap();
        LinkFSPath linkFSPath2 = new LinkFSPath(linkFileSystem, string2, linkFSPath, new PathContents.DirectoryContents((Map<String, LinkFSPath>)object2ObjectOpenHashMap));
        directoryEntry2.files.forEach((string, path) -> object2ObjectOpenHashMap.put(string, (Object)new LinkFSPath(linkFileSystem, (String)string, linkFSPath2, new PathContents.FileContents((Path)path))));
        directoryEntry2.children.forEach((string, directoryEntry) -> object2ObjectOpenHashMap.put(string, (Object)LinkFileSystem.buildPath(directoryEntry, linkFileSystem, string, linkFSPath2)));
        object2ObjectOpenHashMap.trim();
        return linkFSPath2;
    }

    @Override
    public FileSystemProvider provider() {
        return this.provider;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getSeparator() {
        return PATH_SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return List.of(this.root);
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return List.of(this.store);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return VIEWS;
    }

    @Override
    public Path getPath(String string, String ... stringArray) {
        String string2;
        Stream<String> stream = Stream.of(string);
        if (stringArray.length > 0) {
            stream = Stream.concat(stream, Stream.of(stringArray));
        }
        if ((string2 = stream.collect(Collectors.joining(PATH_SEPARATOR))).equals(PATH_SEPARATOR)) {
            return this.root;
        }
        if (string2.startsWith(PATH_SEPARATOR)) {
            LinkFSPath linkFSPath = this.root;
            for (String string3 : PATH_SPLITTER.split((CharSequence)string2.substring(1))) {
                if (string3.isEmpty()) {
                    throw new IllegalArgumentException("Empty paths not allowed");
                }
                linkFSPath = linkFSPath.resolveName(string3);
            }
            return linkFSPath;
        }
        LinkFSPath linkFSPath = null;
        for (String string4 : PATH_SPLITTER.split((CharSequence)string2)) {
            if (string4.isEmpty()) {
                throw new IllegalArgumentException("Empty paths not allowed");
            }
            linkFSPath = new LinkFSPath(this, string4, linkFSPath, PathContents.RELATIVE);
        }
        if (linkFSPath == null) {
            throw new IllegalArgumentException("Empty paths not allowed");
        }
        return linkFSPath;
    }

    @Override
    public PathMatcher getPathMatcher(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException();
    }

    public FileStore store() {
        return this.store;
    }

    public LinkFSPath rootPath() {
        return this.root;
    }

    public static Builder builder() {
        return new Builder();
    }

    static final class DirectoryEntry
    extends Record {
        final Map<String, DirectoryEntry> children;
        final Map<String, Path> files;

        public DirectoryEntry() {
            this(new HashMap<String, DirectoryEntry>(), new HashMap<String, Path>());
        }

        private DirectoryEntry(Map<String, DirectoryEntry> map, Map<String, Path> map2) {
            this.children = map;
            this.files = map2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DirectoryEntry.class, "children;files", "children", "files"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DirectoryEntry.class, "children;files", "children", "files"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DirectoryEntry.class, "children;files", "children", "files"}, this, object);
        }

        public Map<String, DirectoryEntry> children() {
            return this.children;
        }

        public Map<String, Path> files() {
            return this.files;
        }
    }

    public static class Builder {
        private final DirectoryEntry root = new DirectoryEntry();

        public Builder put(List<String> list, String string2, Path path) {
            DirectoryEntry directoryEntry = this.root;
            for (String string3 : list) {
                directoryEntry = directoryEntry.children.computeIfAbsent(string3, string -> new DirectoryEntry());
            }
            directoryEntry.files.put(string2, path);
            return this;
        }

        public Builder put(List<String> list, Path path) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Path can't be empty");
            }
            int n = list.size() - 1;
            return this.put(list.subList(0, n), list.get(n), path);
        }

        public FileSystem build(String string) {
            return new LinkFileSystem(string, this.root);
        }
    }
}

