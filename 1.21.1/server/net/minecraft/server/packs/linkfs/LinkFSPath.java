/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  javax.annotation.Nullable
 */
package net.minecraft.server.packs.linkfs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.server.packs.linkfs.DummyFileAttributes;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.server.packs.linkfs.PathContents;

class LinkFSPath
implements Path {
    private static final BasicFileAttributes DIRECTORY_ATTRIBUTES = new DummyFileAttributes(){

        @Override
        public boolean isRegularFile() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }
    };
    private static final BasicFileAttributes FILE_ATTRIBUTES = new DummyFileAttributes(){

        @Override
        public boolean isRegularFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }
    };
    private static final Comparator<LinkFSPath> PATH_COMPARATOR = Comparator.comparing(LinkFSPath::pathToString);
    private final String name;
    private final LinkFileSystem fileSystem;
    @Nullable
    private final LinkFSPath parent;
    @Nullable
    private List<String> pathToRoot;
    @Nullable
    private String pathString;
    private final PathContents pathContents;

    public LinkFSPath(LinkFileSystem linkFileSystem, String string, @Nullable LinkFSPath linkFSPath, PathContents pathContents) {
        this.fileSystem = linkFileSystem;
        this.name = string;
        this.parent = linkFSPath;
        this.pathContents = pathContents;
    }

    private LinkFSPath createRelativePath(@Nullable LinkFSPath linkFSPath, String string) {
        return new LinkFSPath(this.fileSystem, string, linkFSPath, PathContents.RELATIVE);
    }

    @Override
    public LinkFileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return this.pathContents != PathContents.RELATIVE;
    }

    @Override
    public File toFile() {
        PathContents pathContents = this.pathContents;
        if (pathContents instanceof PathContents.FileContents) {
            PathContents.FileContents fileContents = (PathContents.FileContents)pathContents;
            return fileContents.contents().toFile();
        }
        throw new UnsupportedOperationException("Path " + this.pathToString() + " does not represent file");
    }

    @Override
    @Nullable
    public LinkFSPath getRoot() {
        if (this.isAbsolute()) {
            return this.fileSystem.rootPath();
        }
        return null;
    }

    @Override
    public LinkFSPath getFileName() {
        return this.createRelativePath(null, this.name);
    }

    @Override
    @Nullable
    public LinkFSPath getParent() {
        return this.parent;
    }

    @Override
    public int getNameCount() {
        return this.pathToRoot().size();
    }

    private List<String> pathToRoot() {
        if (this.name.isEmpty()) {
            return List.of();
        }
        if (this.pathToRoot == null) {
            ImmutableList.Builder builder = ImmutableList.builder();
            if (this.parent != null) {
                builder.addAll(this.parent.pathToRoot());
            }
            builder.add((Object)this.name);
            this.pathToRoot = builder.build();
        }
        return this.pathToRoot;
    }

    @Override
    public LinkFSPath getName(int n) {
        List<String> list = this.pathToRoot();
        if (n < 0 || n >= list.size()) {
            throw new IllegalArgumentException("Invalid index: " + n);
        }
        return this.createRelativePath(null, list.get(n));
    }

    @Override
    public LinkFSPath subpath(int n, int n2) {
        List<String> list = this.pathToRoot();
        if (n < 0 || n2 > list.size() || n >= n2) {
            throw new IllegalArgumentException();
        }
        LinkFSPath linkFSPath = null;
        for (int i = n; i < n2; ++i) {
            linkFSPath = this.createRelativePath(linkFSPath, list.get(i));
        }
        return linkFSPath;
    }

    @Override
    public boolean startsWith(Path path) {
        if (path.isAbsolute() != this.isAbsolute()) {
            return false;
        }
        if (path instanceof LinkFSPath) {
            LinkFSPath linkFSPath = (LinkFSPath)path;
            if (linkFSPath.fileSystem != this.fileSystem) {
                return false;
            }
            List<String> list = this.pathToRoot();
            List<String> list2 = linkFSPath.pathToRoot();
            int n = list2.size();
            if (n > list.size()) {
                return false;
            }
            for (int i = 0; i < n; ++i) {
                if (list2.get(i).equals(list.get(i))) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean endsWith(Path path) {
        if (path.isAbsolute() && !this.isAbsolute()) {
            return false;
        }
        if (path instanceof LinkFSPath) {
            LinkFSPath linkFSPath = (LinkFSPath)path;
            if (linkFSPath.fileSystem != this.fileSystem) {
                return false;
            }
            List<String> list = this.pathToRoot();
            List<String> list2 = linkFSPath.pathToRoot();
            int n = list2.size();
            int n2 = list.size() - n;
            if (n2 < 0) {
                return false;
            }
            for (int i = n - 1; i >= 0; --i) {
                if (list2.get(i).equals(list.get(n2 + i))) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public LinkFSPath normalize() {
        return this;
    }

    @Override
    public LinkFSPath resolve(Path path) {
        LinkFSPath linkFSPath = this.toLinkPath(path);
        if (path.isAbsolute()) {
            return linkFSPath;
        }
        return this.resolve(linkFSPath.pathToRoot());
    }

    private LinkFSPath resolve(List<String> list) {
        LinkFSPath linkFSPath = this;
        for (String string : list) {
            linkFSPath = linkFSPath.resolveName(string);
        }
        return linkFSPath;
    }

    LinkFSPath resolveName(String string) {
        if (LinkFSPath.isRelativeOrMissing(this.pathContents)) {
            return new LinkFSPath(this.fileSystem, string, this, this.pathContents);
        }
        Object object = this.pathContents;
        if (object instanceof PathContents.DirectoryContents) {
            PathContents.DirectoryContents directoryContents = (PathContents.DirectoryContents)object;
            return (object = directoryContents.children().get(string)) != null ? object : new LinkFSPath(this.fileSystem, string, this, PathContents.MISSING);
        }
        if (this.pathContents instanceof PathContents.FileContents) {
            return new LinkFSPath(this.fileSystem, string, this, PathContents.MISSING);
        }
        throw new AssertionError((Object)"All content types should be already handled");
    }

    private static boolean isRelativeOrMissing(PathContents pathContents) {
        return pathContents == PathContents.MISSING || pathContents == PathContents.RELATIVE;
    }

    @Override
    public LinkFSPath relativize(Path path) {
        LinkFSPath linkFSPath = this.toLinkPath(path);
        if (this.isAbsolute() != linkFSPath.isAbsolute()) {
            throw new IllegalArgumentException("absolute mismatch");
        }
        List<String> list = this.pathToRoot();
        List<String> list2 = linkFSPath.pathToRoot();
        if (list.size() >= list2.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).equals(list2.get(i))) continue;
            throw new IllegalArgumentException();
        }
        return linkFSPath.subpath(list.size(), list2.size());
    }

    @Override
    public URI toUri() {
        try {
            return new URI("x-mc-link", this.fileSystem.store().name(), this.pathToString(), null);
        }
        catch (URISyntaxException uRISyntaxException) {
            throw new AssertionError("Failed to create URI", uRISyntaxException);
        }
    }

    @Override
    public LinkFSPath toAbsolutePath() {
        if (this.isAbsolute()) {
            return this;
        }
        return this.fileSystem.rootPath().resolve(this);
    }

    @Override
    public LinkFSPath toRealPath(LinkOption ... linkOptionArray) {
        return this.toAbsolutePath();
    }

    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kindArray, WatchEvent.Modifier ... modifierArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path path) {
        LinkFSPath linkFSPath = this.toLinkPath(path);
        return PATH_COMPARATOR.compare(this, linkFSPath);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof LinkFSPath) {
            LinkFSPath linkFSPath = (LinkFSPath)object;
            if (this.fileSystem != linkFSPath.fileSystem) {
                return false;
            }
            boolean bl = this.hasRealContents();
            if (bl != linkFSPath.hasRealContents()) {
                return false;
            }
            if (bl) {
                return this.pathContents == linkFSPath.pathContents;
            }
            return Objects.equals(this.parent, linkFSPath.parent) && Objects.equals(this.name, linkFSPath.name);
        }
        return false;
    }

    private boolean hasRealContents() {
        return !LinkFSPath.isRelativeOrMissing(this.pathContents);
    }

    @Override
    public int hashCode() {
        return this.hasRealContents() ? this.pathContents.hashCode() : this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.pathToString();
    }

    private String pathToString() {
        if (this.pathString == null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (this.isAbsolute()) {
                stringBuilder.append("/");
            }
            Joiner.on((String)"/").appendTo(stringBuilder, this.pathToRoot());
            this.pathString = stringBuilder.toString();
        }
        return this.pathString;
    }

    private LinkFSPath toLinkPath(@Nullable Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (path instanceof LinkFSPath) {
            LinkFSPath linkFSPath = (LinkFSPath)path;
            if (linkFSPath.fileSystem == this.fileSystem) {
                return linkFSPath;
            }
        }
        throw new ProviderMismatchException();
    }

    public boolean exists() {
        return this.hasRealContents();
    }

    @Nullable
    public Path getTargetPath() {
        Path path;
        PathContents pathContents = this.pathContents;
        if (pathContents instanceof PathContents.FileContents) {
            PathContents.FileContents fileContents = (PathContents.FileContents)pathContents;
            path = fileContents.contents();
        } else {
            path = null;
        }
        return path;
    }

    @Nullable
    public PathContents.DirectoryContents getDirectoryContents() {
        PathContents.DirectoryContents directoryContents;
        PathContents pathContents = this.pathContents;
        return pathContents instanceof PathContents.DirectoryContents ? (directoryContents = (PathContents.DirectoryContents)pathContents) : null;
    }

    public BasicFileAttributeView getBasicAttributeView() {
        return new BasicFileAttributeView(){

            @Override
            public String name() {
                return "basic";
            }

            @Override
            public BasicFileAttributes readAttributes() throws IOException {
                return LinkFSPath.this.getBasicAttributes();
            }

            @Override
            public void setTimes(FileTime fileTime, FileTime fileTime2, FileTime fileTime3) {
                throw new ReadOnlyFileSystemException();
            }
        };
    }

    public BasicFileAttributes getBasicAttributes() throws IOException {
        if (this.pathContents instanceof PathContents.DirectoryContents) {
            return DIRECTORY_ATTRIBUTES;
        }
        if (this.pathContents instanceof PathContents.FileContents) {
            return FILE_ATTRIBUTES;
        }
        throw new NoSuchFileException(this.pathToString());
    }

    @Override
    public /* synthetic */ Path toRealPath(LinkOption[] linkOptionArray) throws IOException {
        return this.toRealPath(linkOptionArray);
    }

    @Override
    public /* synthetic */ Path toAbsolutePath() {
        return this.toAbsolutePath();
    }

    @Override
    public /* synthetic */ Path relativize(Path path) {
        return this.relativize(path);
    }

    @Override
    public /* synthetic */ Path resolve(Path path) {
        return this.resolve(path);
    }

    @Override
    public /* synthetic */ Path normalize() {
        return this.normalize();
    }

    @Override
    public /* synthetic */ Path subpath(int n, int n2) {
        return this.subpath(n, n2);
    }

    @Override
    public /* synthetic */ Path getName(int n) {
        return this.getName(n);
    }

    @Override
    @Nullable
    public /* synthetic */ Path getParent() {
        return this.getParent();
    }

    @Override
    public /* synthetic */ Path getFileName() {
        return this.getFileName();
    }

    @Override
    @Nullable
    public /* synthetic */ Path getRoot() {
        return this.getRoot();
    }

    @Override
    public /* synthetic */ FileSystem getFileSystem() {
        return this.getFileSystem();
    }
}

