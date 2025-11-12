/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;

public class DirectoryValidator {
    private final PathMatcher symlinkTargetAllowList;

    public DirectoryValidator(PathMatcher pathMatcher) {
        this.symlinkTargetAllowList = pathMatcher;
    }

    public void validateSymlink(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
        Path path2 = Files.readSymbolicLink(path);
        if (!this.symlinkTargetAllowList.matches(path2)) {
            list.add(new ForbiddenSymlinkInfo(path, path2));
        }
    }

    public List<ForbiddenSymlinkInfo> validateSymlink(Path path) throws IOException {
        ArrayList<ForbiddenSymlinkInfo> arrayList = new ArrayList<ForbiddenSymlinkInfo>();
        this.validateSymlink(path, arrayList);
        return arrayList;
    }

    public List<ForbiddenSymlinkInfo> validateDirectory(Path path, boolean bl) throws IOException {
        BasicFileAttributes basicFileAttributes;
        ArrayList<ForbiddenSymlinkInfo> arrayList = new ArrayList<ForbiddenSymlinkInfo>();
        try {
            basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }
        catch (NoSuchFileException noSuchFileException) {
            return arrayList;
        }
        if (basicFileAttributes.isRegularFile()) {
            throw new IOException("Path " + String.valueOf(path) + " is not a directory");
        }
        if (basicFileAttributes.isSymbolicLink()) {
            if (bl) {
                path = Files.readSymbolicLink(path);
            } else {
                this.validateSymlink(path, arrayList);
                return arrayList;
            }
        }
        this.validateKnownDirectory(path, arrayList);
        return arrayList;
    }

    public void validateKnownDirectory(Path path, final List<ForbiddenSymlinkInfo> list) throws IOException {
        Files.walkFileTree(path, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

            private void validateSymlink(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                if (basicFileAttributes.isSymbolicLink()) {
                    DirectoryValidator.this.validateSymlink(path, list);
                }
            }

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                this.validateSymlink(path, basicFileAttributes);
                return super.preVisitDirectory(path, basicFileAttributes);
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                this.validateSymlink(path, basicFileAttributes);
                return super.visitFile(path, basicFileAttributes);
            }

            @Override
            public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                return this.visitFile((Path)object, basicFileAttributes);
            }

            @Override
            public /* synthetic */ FileVisitResult preVisitDirectory(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                return this.preVisitDirectory((Path)object, basicFileAttributes);
            }
        });
    }
}

