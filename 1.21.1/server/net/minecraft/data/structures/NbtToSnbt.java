/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 *  com.google.common.hash.HashingOutputStream
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.data.structures;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.FastBufferedInputStream;
import org.slf4j.Logger;

public class NbtToSnbt
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Iterable<Path> inputFolders;
    private final PackOutput output;

    public NbtToSnbt(PackOutput packOutput, Collection<Path> collection) {
        this.inputFolders = collection;
        this.output = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path path = this.output.getOutputFolder();
        ArrayList<CompletionStage> arrayList = new ArrayList<CompletionStage>();
        for (Path path2 : this.inputFolders) {
            arrayList.add(CompletableFuture.supplyAsync(() -> {
                CompletableFuture<Void> completableFuture;
                block8: {
                    Stream<Path> stream = Files.walk(path2, new FileVisitOption[0]);
                    try {
                        completableFuture = CompletableFuture.allOf((CompletableFuture[])stream.filter(path -> path.toString().endsWith(".nbt")).map(path3 -> CompletableFuture.runAsync(() -> NbtToSnbt.convertStructure(cachedOutput, path3, NbtToSnbt.getName(path2, path3), path), Util.ioPool())).toArray(CompletableFuture[]::new));
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
                        catch (IOException iOException) {
                            LOGGER.error("Failed to read structure input directory", (Throwable)iOException);
                            return CompletableFuture.completedFuture(null);
                        }
                    }
                    stream.close();
                }
                return completableFuture;
            }, Util.backgroundExecutor()).thenCompose(completableFuture -> completableFuture));
        }
        return CompletableFuture.allOf((CompletableFuture[])arrayList.toArray(CompletableFuture[]::new));
    }

    @Override
    public final String getName() {
        return "NBT -> SNBT";
    }

    private static String getName(Path path, Path path2) {
        String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
        return string.substring(0, string.length() - ".nbt".length());
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static Path convertStructure(CachedOutput cachedOutput, Path path, String string, Path path2) {
        try (InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
            Path path3;
            try (FastBufferedInputStream fastBufferedInputStream = new FastBufferedInputStream(inputStream);){
                Path path4 = path2.resolve(string + ".snbt");
                NbtToSnbt.writeSnbt(cachedOutput, path4, NbtUtils.structureToSnbt(NbtIo.readCompressed(fastBufferedInputStream, NbtAccounter.unlimitedHeap())));
                LOGGER.info("Converted {} from NBT to SNBT", (Object)string);
                path3 = path4;
            }
            return path3;
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", new Object[]{string, path, iOException});
            return null;
        }
    }

    public static void writeSnbt(CachedOutput cachedOutput, Path path, String string) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), (OutputStream)byteArrayOutputStream);
        hashingOutputStream.write(string.getBytes(StandardCharsets.UTF_8));
        hashingOutputStream.write(10);
        cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
    }
}

