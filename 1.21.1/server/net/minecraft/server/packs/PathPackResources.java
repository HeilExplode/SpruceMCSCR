/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.slf4j.Logger;

public class PathPackResources
extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Joiner PATH_JOINER = Joiner.on((String)"/");
    private final Path root;

    public PathPackResources(PackLocationInfo packLocationInfo, Path path) {
        super(packLocationInfo);
        this.root = path;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String ... stringArray) {
        FileUtil.validatePath(stringArray);
        Path path = FileUtil.resolvePath(this.root, List.of(stringArray));
        if (Files.exists(path, new LinkOption[0])) {
            return IoSupplier.create(path);
        }
        return null;
    }

    public static boolean validatePath(Path path) {
        return true;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        Path path = this.root.resolve(packType.getDirectory()).resolve(resourceLocation.getNamespace());
        return PathPackResources.getResource(resourceLocation, path);
    }

    @Nullable
    public static IoSupplier<InputStream> getResource(ResourceLocation resourceLocation, Path path) {
        return (IoSupplier)FileUtil.decomposePath(resourceLocation.getPath()).mapOrElse(list -> {
            Path path2 = FileUtil.resolvePath(path, list);
            return PathPackResources.returnFileIfExists(path2);
        }, error -> {
            LOGGER.error("Invalid path {}: {}", (Object)resourceLocation, (Object)error.message());
            return null;
        });
    }

    @Nullable
    private static IoSupplier<InputStream> returnFileIfExists(Path path) {
        if (Files.exists(path, new LinkOption[0]) && PathPackResources.validatePath(path)) {
            return IoSupplier.create(path);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        FileUtil.decomposePath(string2).ifSuccess(list -> {
            Path path = this.root.resolve(packType.getDirectory()).resolve(string);
            PathPackResources.listPath(string, path, list, resourceOutput);
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)string2, (Object)error.message()));
    }

    public static void listPath(String string, Path path3, List<String> list, PackResources.ResourceOutput resourceOutput) {
        Path path4 = FileUtil.resolvePath(path3, list);
        try (Stream<Path> stream = Files.find(path4, Integer.MAX_VALUE, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile(), new FileVisitOption[0]);){
            stream.forEach(path2 -> {
                String string2 = PATH_JOINER.join((Iterable)path3.relativize((Path)path2));
                ResourceLocation resourceLocation = ResourceLocation.tryBuild(string, string2);
                if (resourceLocation == null) {
                    Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", string, string2));
                } else {
                    resourceOutput.accept(resourceLocation, IoSupplier.create(path2));
                }
            });
        }
        catch (NoSuchFileException | NotDirectoryException fileSystemException) {
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to list path {}", (Object)path4, (Object)iOException);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        HashSet hashSet = Sets.newHashSet();
        Path path = this.root.resolve(packType.getDirectory());
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);){
            for (Path path2 : directoryStream) {
                String string = path2.getFileName().toString();
                if (ResourceLocation.isValidNamespace(string)) {
                    hashSet.add(string);
                    continue;
                }
                LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", (Object)string, (Object)this.root);
            }
        }
        catch (NoSuchFileException | NotDirectoryException fileSystemException) {
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to list path {}", (Object)path, (Object)iOException);
        }
        return hashSet;
    }

    @Override
    public void close() {
    }

    public static class PathResourcesSupplier
    implements Pack.ResourcesSupplier {
        private final Path content;

        public PathResourcesSupplier(Path path) {
            this.content = path;
        }

        @Override
        public PackResources openPrimary(PackLocationInfo packLocationInfo) {
            return new PathPackResources(packLocationInfo, this.content);
        }

        @Override
        public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
            PackResources packResources = this.openPrimary(packLocationInfo);
            List<String> list = metadata.overlays();
            if (list.isEmpty()) {
                return packResources;
            }
            ArrayList<PackResources> arrayList = new ArrayList<PackResources>(list.size());
            for (String string : list) {
                Path path = this.content.resolve(string);
                arrayList.add(new PathPackResources(packLocationInfo, path));
            }
            return new CompositePackResources(packResources, arrayList);
        }
    }
}

