/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String STRUCTURE_RESOURCE_DIRECTORY_NAME = "structure";
    private static final String STRUCTURE_GENERATED_DIRECTORY_NAME = "structures";
    private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
    private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
    private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.newConcurrentMap();
    private final DataFixer fixerUpper;
    private ResourceManager resourceManager;
    private final Path generatedDir;
    private final List<Source> sources;
    private final HolderGetter<Block> blockLookup;
    private static final FileToIdConverter RESOURCE_LISTER = new FileToIdConverter("structure", ".nbt");

    public StructureTemplateManager(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, HolderGetter<Block> holderGetter) {
        this.resourceManager = resourceManager;
        this.fixerUpper = dataFixer;
        this.generatedDir = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
        this.blockLookup = holderGetter;
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add((Object)new Source(this::loadFromGenerated, this::listGenerated));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            builder.add((Object)new Source(this::loadFromTestStructures, this::listTestStructures));
        }
        builder.add((Object)new Source(this::loadFromResource, this::listResources));
        this.sources = builder.build();
    }

    public StructureTemplate getOrCreate(ResourceLocation resourceLocation) {
        Optional<StructureTemplate> optional = this.get(resourceLocation);
        if (optional.isPresent()) {
            return optional.get();
        }
        StructureTemplate structureTemplate = new StructureTemplate();
        this.structureRepository.put(resourceLocation, Optional.of(structureTemplate));
        return structureTemplate;
    }

    public Optional<StructureTemplate> get(ResourceLocation resourceLocation) {
        return this.structureRepository.computeIfAbsent(resourceLocation, this::tryLoad);
    }

    public Stream<ResourceLocation> listTemplates() {
        return this.sources.stream().flatMap(source -> source.lister().get()).distinct();
    }

    private Optional<StructureTemplate> tryLoad(ResourceLocation resourceLocation) {
        for (Source source : this.sources) {
            try {
                Optional<StructureTemplate> optional = source.loader().apply(resourceLocation);
                if (!optional.isPresent()) continue;
                return optional;
            }
            catch (Exception exception) {
            }
        }
        return Optional.empty();
    }

    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.structureRepository.clear();
    }

    private Optional<StructureTemplate> loadFromResource(ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = RESOURCE_LISTER.idToFile(resourceLocation);
        return this.load(() -> this.resourceManager.open(resourceLocation2), throwable -> LOGGER.error("Couldn't load structure {}", (Object)resourceLocation, throwable));
    }

    private Stream<ResourceLocation> listResources() {
        return RESOURCE_LISTER.listMatchingResources(this.resourceManager).keySet().stream().map(RESOURCE_LISTER::fileToId);
    }

    private Optional<StructureTemplate> loadFromTestStructures(ResourceLocation resourceLocation) {
        return this.loadFromSnbt(resourceLocation, Paths.get(StructureUtils.testStructuresDir, new String[0]));
    }

    private Stream<ResourceLocation> listTestStructures() {
        Path path = Paths.get(StructureUtils.testStructuresDir, new String[0]);
        if (!Files.isDirectory(path, new LinkOption[0])) {
            return Stream.empty();
        }
        ArrayList arrayList = new ArrayList();
        this.listFolderContents(path, "minecraft", STRUCTURE_TEXT_FILE_EXTENSION, arrayList::add);
        return arrayList.stream();
    }

    private Optional<StructureTemplate> loadFromGenerated(ResourceLocation resourceLocation) {
        if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
            return Optional.empty();
        }
        Path path = this.createAndValidatePathToGeneratedStructure(resourceLocation, STRUCTURE_FILE_EXTENSION);
        return this.load(() -> new FileInputStream(path.toFile()), throwable -> LOGGER.error("Couldn't load structure from {}", (Object)path, throwable));
    }

    private Stream<ResourceLocation> listGenerated() {
        if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
            return Stream.empty();
        }
        try {
            ArrayList arrayList = new ArrayList();
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.generatedDir, path -> Files.isDirectory(path, new LinkOption[0]));){
                for (Path path2 : directoryStream) {
                    String string = path2.getFileName().toString();
                    Path path3 = path2.resolve(STRUCTURE_GENERATED_DIRECTORY_NAME);
                    this.listFolderContents(path3, string, STRUCTURE_FILE_EXTENSION, arrayList::add);
                }
            }
            return arrayList.stream();
        }
        catch (IOException iOException) {
            return Stream.empty();
        }
    }

    private void listFolderContents(Path path3, String string2, String string3, Consumer<ResourceLocation> consumer) {
        int n = string3.length();
        Function<String, String> function = string -> string.substring(0, string.length() - n);
        try (Stream<Path> stream = Files.find(path3, Integer.MAX_VALUE, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile() && path.toString().endsWith(string3), new FileVisitOption[0]);){
            stream.forEach(path2 -> {
                try {
                    consumer.accept(ResourceLocation.fromNamespaceAndPath(string2, (String)function.apply(this.relativize(path3, (Path)path2))));
                }
                catch (ResourceLocationException resourceLocationException) {
                    LOGGER.error("Invalid location while listing folder {} contents", (Object)path3, (Object)resourceLocationException);
                }
            });
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to list folder {} contents", (Object)path3, (Object)iOException);
        }
    }

    private String relativize(Path path, Path path2) {
        return path.relativize(path2).toString().replace(File.separator, "/");
    }

    private Optional<StructureTemplate> loadFromSnbt(ResourceLocation resourceLocation, Path path) {
        Optional<StructureTemplate> optional;
        block10: {
            if (!Files.isDirectory(path, new LinkOption[0])) {
                return Optional.empty();
            }
            Path path2 = FileUtil.createPathToResource(path, resourceLocation.getPath(), STRUCTURE_TEXT_FILE_EXTENSION);
            BufferedReader bufferedReader = Files.newBufferedReader(path2);
            try {
                String string = IOUtils.toString((Reader)bufferedReader);
                optional = Optional.of(this.readStructure(NbtUtils.snbtToStructure(string)));
                if (bufferedReader == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (NoSuchFileException noSuchFileException) {
                    return Optional.empty();
                }
                catch (CommandSyntaxException | IOException throwable3) {
                    LOGGER.error("Couldn't load structure from {}", (Object)path2, (Object)throwable3);
                    return Optional.empty();
                }
            }
            bufferedReader.close();
        }
        return optional;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private Optional<StructureTemplate> load(InputStreamOpener inputStreamOpener, Consumer<Throwable> consumer) {
        try (InputStream inputStream = inputStreamOpener.open();){
            Optional<StructureTemplate> optional;
            try (FastBufferedInputStream fastBufferedInputStream = new FastBufferedInputStream(inputStream);){
                optional = Optional.of(this.readStructure(fastBufferedInputStream));
            }
            return optional;
        }
        catch (FileNotFoundException fileNotFoundException) {
            return Optional.empty();
        }
        catch (Throwable throwable) {
            consumer.accept(throwable);
            return Optional.empty();
        }
    }

    private StructureTemplate readStructure(InputStream inputStream) throws IOException {
        CompoundTag compoundTag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
        return this.readStructure(compoundTag);
    }

    public StructureTemplate readStructure(CompoundTag compoundTag) {
        StructureTemplate structureTemplate = new StructureTemplate();
        int n = NbtUtils.getDataVersion(compoundTag, 500);
        structureTemplate.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, compoundTag, n));
        return structureTemplate;
    }

    public boolean save(ResourceLocation resourceLocation) {
        Optional<StructureTemplate> optional = this.structureRepository.get(resourceLocation);
        if (optional.isEmpty()) {
            return false;
        }
        StructureTemplate structureTemplate = optional.get();
        Path path = this.createAndValidatePathToGeneratedStructure(resourceLocation, STRUCTURE_FILE_EXTENSION);
        Path path2 = path.getParent();
        if (path2 == null) {
            return false;
        }
        try {
            Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath(new LinkOption[0]) : path2, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to create parent directory: {}", (Object)path2);
            return false;
        }
        CompoundTag compoundTag = structureTemplate.save(new CompoundTag());
        try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());){
            NbtIo.writeCompressed(compoundTag, fileOutputStream);
        }
        catch (Throwable throwable) {
            return false;
        }
        return true;
    }

    public Path createAndValidatePathToGeneratedStructure(ResourceLocation resourceLocation, String string) {
        if (resourceLocation.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + String.valueOf(resourceLocation));
        }
        try {
            Path path = this.generatedDir.resolve(resourceLocation.getNamespace());
            Path path2 = path.resolve(STRUCTURE_GENERATED_DIRECTORY_NAME);
            Path path3 = FileUtil.createPathToResource(path2, resourceLocation.getPath(), string);
            if (!(path3.startsWith(this.generatedDir) && FileUtil.isPathNormalized(path3) && FileUtil.isPathPortable(path3))) {
                throw new ResourceLocationException("Invalid resource path: " + String.valueOf(path3));
            }
            return path3;
        }
        catch (InvalidPathException invalidPathException) {
            throw new ResourceLocationException("Invalid resource path: " + String.valueOf(resourceLocation), invalidPathException);
        }
    }

    public void remove(ResourceLocation resourceLocation) {
        this.structureRepository.remove(resourceLocation);
    }

    record Source(Function<ResourceLocation, Optional<StructureTemplate>> loader, Supplier<Stream<ResourceLocation>> lister) {
    }

    @FunctionalInterface
    static interface InputStreamOpener {
        public InputStream open() throws IOException;
    }
}

