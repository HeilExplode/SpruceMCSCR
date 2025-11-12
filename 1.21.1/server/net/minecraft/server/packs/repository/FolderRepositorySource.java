/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackDetector;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.slf4j.Logger;

public class FolderRepositorySource
implements RepositorySource {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final PackSelectionConfig DISCOVERED_PACK_SELECTION_CONFIG = new PackSelectionConfig(false, Pack.Position.TOP, false);
    private final Path folder;
    private final PackType packType;
    private final PackSource packSource;
    private final DirectoryValidator validator;

    public FolderRepositorySource(Path path, PackType packType, PackSource packSource, DirectoryValidator directoryValidator) {
        this.folder = path;
        this.packType = packType;
        this.packSource = packSource;
        this.validator = directoryValidator;
    }

    private static String nameFromPath(Path path) {
        return path.getFileName().toString();
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        try {
            FileUtil.createDirectoriesSafe(this.folder);
            FolderRepositorySource.discoverPacks(this.folder, this.validator, (path, resourcesSupplier) -> {
                PackLocationInfo packLocationInfo = this.createDiscoveredFilePackInfo((Path)path);
                Pack pack = Pack.readMetaAndCreate(packLocationInfo, resourcesSupplier, this.packType, DISCOVERED_PACK_SELECTION_CONFIG);
                if (pack != null) {
                    consumer.accept(pack);
                }
            });
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to list packs in {}", (Object)this.folder, (Object)iOException);
        }
    }

    private PackLocationInfo createDiscoveredFilePackInfo(Path path) {
        String string = FolderRepositorySource.nameFromPath(path);
        return new PackLocationInfo("file/" + string, Component.literal(string), this.packSource, Optional.empty());
    }

    public static void discoverPacks(Path path, DirectoryValidator directoryValidator, BiConsumer<Path, Pack.ResourcesSupplier> biConsumer) throws IOException {
        FolderPackDetector folderPackDetector = new FolderPackDetector(directoryValidator);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);){
            for (Path path2 : directoryStream) {
                try {
                    ArrayList<ForbiddenSymlinkInfo> arrayList = new ArrayList<ForbiddenSymlinkInfo>();
                    Pack.ResourcesSupplier resourcesSupplier = (Pack.ResourcesSupplier)folderPackDetector.detectPackResources(path2, arrayList);
                    if (!arrayList.isEmpty()) {
                        LOGGER.warn("Ignoring potential pack entry: {}", (Object)ContentValidationException.getMessage(path2, arrayList));
                        continue;
                    }
                    if (resourcesSupplier != null) {
                        biConsumer.accept(path2, resourcesSupplier);
                        continue;
                    }
                    LOGGER.info("Found non-pack entry '{}', ignoring", (Object)path2);
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to read properties of '{}', ignoring", (Object)path2, (Object)iOException);
                }
            }
        }
    }

    static class FolderPackDetector
    extends PackDetector<Pack.ResourcesSupplier> {
        protected FolderPackDetector(DirectoryValidator directoryValidator) {
            super(directoryValidator);
        }

        @Override
        @Nullable
        protected Pack.ResourcesSupplier createZipPack(Path path) {
            FileSystem fileSystem = path.getFileSystem();
            if (fileSystem == FileSystems.getDefault() || fileSystem instanceof LinkFileSystem) {
                return new FilePackResources.FileResourcesSupplier(path);
            }
            LOGGER.info("Can't open pack archive at {}", (Object)path);
            return null;
        }

        @Override
        protected Pack.ResourcesSupplier createDirectoryPack(Path path) {
            return new PathPackResources.PathResourcesSupplier(path);
        }

        @Override
        protected /* synthetic */ Object createDirectoryPack(Path path) throws IOException {
            return this.createDirectoryPack(path);
        }

        @Override
        @Nullable
        protected /* synthetic */ Object createZipPack(Path path) throws IOException {
            return this.createZipPack(path);
        }
    }
}

