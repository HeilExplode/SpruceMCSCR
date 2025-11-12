/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;

public class ContentValidationException
extends Exception {
    private final Path directory;
    private final List<ForbiddenSymlinkInfo> entries;

    public ContentValidationException(Path path, List<ForbiddenSymlinkInfo> list) {
        this.directory = path;
        this.entries = list;
    }

    @Override
    public String getMessage() {
        return ContentValidationException.getMessage(this.directory, this.entries);
    }

    public static String getMessage(Path path, List<ForbiddenSymlinkInfo> list) {
        return "Failed to validate '" + String.valueOf(path) + "'. Found forbidden symlinks: " + list.stream().map(forbiddenSymlinkInfo -> String.valueOf(forbiddenSymlinkInfo.link()) + "->" + String.valueOf(forbiddenSymlinkInfo.target())).collect(Collectors.joining(", "));
    }
}

