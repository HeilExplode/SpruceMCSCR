/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.server.packs.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;

public abstract class PackDetector<T> {
    private final DirectoryValidator validator;

    protected PackDetector(DirectoryValidator directoryValidator) {
        this.validator = directoryValidator;
    }

    @Nullable
    public T detectPackResources(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
        BasicFileAttributes basicFileAttributes;
        Path path2 = path;
        try {
            basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }
        catch (NoSuchFileException noSuchFileException) {
            return null;
        }
        if (basicFileAttributes.isSymbolicLink()) {
            this.validator.validateSymlink(path, list);
            if (!list.isEmpty()) {
                return null;
            }
            path2 = Files.readSymbolicLink(path);
            basicFileAttributes = Files.readAttributes(path2, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }
        if (basicFileAttributes.isDirectory()) {
            this.validator.validateKnownDirectory(path2, list);
            if (!list.isEmpty()) {
                return null;
            }
            if (!Files.isRegularFile(path2.resolve("pack.mcmeta"), new LinkOption[0])) {
                return null;
            }
            return this.createDirectoryPack(path2);
        }
        if (basicFileAttributes.isRegularFile() && path2.getFileName().toString().endsWith(".zip")) {
            return this.createZipPack(path2);
        }
        return null;
    }

    @Nullable
    protected abstract T createZipPack(Path var1) throws IOException;

    @Nullable
    protected abstract T createDirectoryPack(Path var1) throws IOException;
}

