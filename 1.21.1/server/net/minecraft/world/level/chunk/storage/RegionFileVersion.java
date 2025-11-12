/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  net.jpountz.lz4.LZ4BlockInputStream
 *  net.jpountz.lz4.LZ4BlockOutputStream
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.minecraft.util.FastBufferedInputStream;
import org.slf4j.Logger;

public class RegionFileVersion {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Int2ObjectMap<RegionFileVersion> VERSIONS = new Int2ObjectOpenHashMap();
    private static final Object2ObjectMap<String, RegionFileVersion> VERSIONS_BY_NAME = new Object2ObjectOpenHashMap();
    public static final RegionFileVersion VERSION_GZIP = RegionFileVersion.register(new RegionFileVersion(1, null, inputStream -> new FastBufferedInputStream(new GZIPInputStream((InputStream)inputStream)), outputStream -> new BufferedOutputStream(new GZIPOutputStream((OutputStream)outputStream))));
    public static final RegionFileVersion VERSION_DEFLATE = RegionFileVersion.register(new RegionFileVersion(2, "deflate", inputStream -> new FastBufferedInputStream(new InflaterInputStream((InputStream)inputStream)), outputStream -> new BufferedOutputStream(new DeflaterOutputStream((OutputStream)outputStream))));
    public static final RegionFileVersion VERSION_NONE = RegionFileVersion.register(new RegionFileVersion(3, "none", FastBufferedInputStream::new, BufferedOutputStream::new));
    public static final RegionFileVersion VERSION_LZ4 = RegionFileVersion.register(new RegionFileVersion(4, "lz4", inputStream -> new FastBufferedInputStream((InputStream)new LZ4BlockInputStream(inputStream)), outputStream -> new BufferedOutputStream((OutputStream)new LZ4BlockOutputStream(outputStream))));
    public static final RegionFileVersion VERSION_CUSTOM = RegionFileVersion.register(new RegionFileVersion(127, null, inputStream -> {
        throw new UnsupportedOperationException();
    }, outputStream -> {
        throw new UnsupportedOperationException();
    }));
    public static final RegionFileVersion DEFAULT;
    private static volatile RegionFileVersion selected;
    private final int id;
    @Nullable
    private final String optionName;
    private final StreamWrapper<InputStream> inputWrapper;
    private final StreamWrapper<OutputStream> outputWrapper;

    private RegionFileVersion(int n, @Nullable String string, StreamWrapper<InputStream> streamWrapper, StreamWrapper<OutputStream> streamWrapper2) {
        this.id = n;
        this.optionName = string;
        this.inputWrapper = streamWrapper;
        this.outputWrapper = streamWrapper2;
    }

    private static RegionFileVersion register(RegionFileVersion regionFileVersion) {
        VERSIONS.put(regionFileVersion.id, (Object)regionFileVersion);
        if (regionFileVersion.optionName != null) {
            VERSIONS_BY_NAME.put((Object)regionFileVersion.optionName, (Object)regionFileVersion);
        }
        return regionFileVersion;
    }

    @Nullable
    public static RegionFileVersion fromId(int n) {
        return (RegionFileVersion)VERSIONS.get(n);
    }

    public static void configure(String string) {
        RegionFileVersion regionFileVersion = (RegionFileVersion)VERSIONS_BY_NAME.get((Object)string);
        if (regionFileVersion != null) {
            selected = regionFileVersion;
        } else {
            LOGGER.error("Invalid `region-file-compression` value `{}` in server.properties. Please use one of: {}", (Object)string, (Object)String.join((CharSequence)", ", (Iterable<? extends CharSequence>)VERSIONS_BY_NAME.keySet()));
        }
    }

    public static RegionFileVersion getSelected() {
        return selected;
    }

    public static boolean isValidVersion(int n) {
        return VERSIONS.containsKey(n);
    }

    public int getId() {
        return this.id;
    }

    public OutputStream wrap(OutputStream outputStream) throws IOException {
        return this.outputWrapper.wrap(outputStream);
    }

    public InputStream wrap(InputStream inputStream) throws IOException {
        return this.inputWrapper.wrap(inputStream);
    }

    static {
        selected = DEFAULT = VERSION_DEFLATE;
    }

    @FunctionalInterface
    static interface StreamWrapper<O> {
        public O wrap(O var1) throws IOException;
    }
}

