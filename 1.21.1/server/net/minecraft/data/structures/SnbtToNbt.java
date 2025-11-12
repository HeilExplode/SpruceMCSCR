/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.Hashing
 *  com.google.common.hash.HashingOutputStream
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class SnbtToNbt
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput output;
    private final Iterable<Path> inputFolders;
    private final List<Filter> filters = Lists.newArrayList();

    public SnbtToNbt(PackOutput packOutput, Iterable<Path> iterable) {
        this.output = packOutput;
        this.inputFolders = iterable;
    }

    public SnbtToNbt addFilter(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    private CompoundTag applyFilters(String string, CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag;
        for (Filter filter : this.filters) {
            compoundTag2 = filter.apply(string, compoundTag2);
        }
        return compoundTag2;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path path = this.output.getOutputFolder();
        ArrayList arrayList = Lists.newArrayList();
        for (Path path2 : this.inputFolders) {
            arrayList.add(CompletableFuture.supplyAsync(() -> {
                CompletableFuture<Void> completableFuture;
                block8: {
                    Stream<Path> stream = Files.walk(path2, new FileVisitOption[0]);
                    try {
                        completableFuture = CompletableFuture.allOf((CompletableFuture[])stream.filter(path -> path.toString().endsWith(".snbt")).map(path3 -> CompletableFuture.runAsync(() -> {
                            TaskResult taskResult = this.readStructure((Path)path3, this.getName(path2, (Path)path3));
                            this.storeStructureIfChanged(cachedOutput, taskResult, path);
                        }, Util.backgroundExecutor())).toArray(CompletableFuture[]::new));
                        if (stream == null) break block8;
                    }
                    catch (Throwable throwable) {
                        try {
                            if (stream != null) {
                                try {
                                    stream.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        catch (Exception exception) {
                            throw new RuntimeException("Failed to read structure input directory, aborting", exception);
                        }
                    }
                    stream.close();
                }
                return completableFuture;
            }, Util.backgroundExecutor()).thenCompose(completableFuture -> completableFuture));
        }
        return Util.sequenceFailFast(arrayList);
    }

    @Override
    public final String getName() {
        return "SNBT -> NBT";
    }

    private String getName(Path path, Path path2) {
        String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
        return string.substring(0, string.length() - ".snbt".length());
    }

    private TaskResult readStructure(Path path, String string) {
        TaskResult taskResult;
        block8: {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            try {
                String string2 = IOUtils.toString((Reader)bufferedReader);
                CompoundTag compoundTag = this.applyFilters(string, NbtUtils.snbtToStructure(string2));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), (OutputStream)byteArrayOutputStream);
                NbtIo.writeCompressed(compoundTag, (OutputStream)hashingOutputStream);
                byte[] byArray = byteArrayOutputStream.toByteArray();
                HashCode hashCode = hashingOutputStream.hash();
                taskResult = new TaskResult(string, byArray, hashCode);
                if (bufferedReader == null) break block8;
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
                catch (Throwable throwable3) {
                    throw new StructureConversionException(path, throwable3);
                }
            }
            bufferedReader.close();
        }
        return taskResult;
    }

    private void storeStructureIfChanged(CachedOutput cachedOutput, TaskResult taskResult, Path path) {
        Path path2 = path.resolve(taskResult.name + ".nbt");
        try {
            cachedOutput.writeIfNeeded(path2, taskResult.payload, taskResult.hash);
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't write structure {} at {}", new Object[]{taskResult.name, path2, iOException});
        }
    }

    @FunctionalInterface
    public static interface Filter {
        public CompoundTag apply(String var1, CompoundTag var2);
    }

    static final class TaskResult
    extends Record {
        final String name;
        final byte[] payload;
        final HashCode hash;

        TaskResult(String string, byte[] byArray, HashCode hashCode) {
            this.name = string;
            this.payload = byArray;
            this.hash = hashCode;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TaskResult.class, "name;payload;hash", "name", "payload", "hash"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TaskResult.class, "name;payload;hash", "name", "payload", "hash"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TaskResult.class, "name;payload;hash", "name", "payload", "hash"}, this, object);
        }

        public String name() {
            return this.name;
        }

        public byte[] payload() {
            return this.payload;
        }

        public HashCode hash() {
            return this.hash;
        }
    }

    static class StructureConversionException
    extends RuntimeException {
        public StructureConversionException(Path path, Throwable throwable) {
            super(path.toAbsolutePath().toString(), throwable);
        }
    }
}

