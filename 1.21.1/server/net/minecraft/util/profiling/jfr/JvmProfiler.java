/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.net.SocketAddress;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.slf4j.Logger;

public interface JvmProfiler {
    public static final JvmProfiler INSTANCE = Runtime.class.getModule().getLayer().findModule("jdk.jfr").isPresent() ? JfrProfiler.getInstance() : new NoOpProfiler();

    public boolean start(Environment var1);

    public Path stop();

    public boolean isRunning();

    public boolean isAvailable();

    public void onServerTick(float var1);

    public void onPacketReceived(ConnectionProtocol var1, PacketType<?> var2, SocketAddress var3, int var4);

    public void onPacketSent(ConnectionProtocol var1, PacketType<?> var2, SocketAddress var3, int var4);

    public void onRegionFileRead(RegionStorageInfo var1, ChunkPos var2, RegionFileVersion var3, int var4);

    public void onRegionFileWrite(RegionStorageInfo var1, ChunkPos var2, RegionFileVersion var3, int var4);

    @Nullable
    public ProfiledDuration onWorldLoadedStarted();

    @Nullable
    public ProfiledDuration onChunkGenerate(ChunkPos var1, ResourceKey<Level> var2, String var3);

    public static class NoOpProfiler
    implements JvmProfiler {
        private static final Logger LOGGER = LogUtils.getLogger();
        static final ProfiledDuration noOpCommit = () -> {};

        @Override
        public boolean start(Environment environment) {
            LOGGER.warn("Attempted to start Flight Recorder, but it's not supported on this JVM");
            return false;
        }

        @Override
        public Path stop() {
            throw new IllegalStateException("Attempted to stop Flight Recorder, but it's not supported on this JVM");
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void onPacketReceived(ConnectionProtocol connectionProtocol, PacketType<?> packetType, SocketAddress socketAddress, int n) {
        }

        @Override
        public void onPacketSent(ConnectionProtocol connectionProtocol, PacketType<?> packetType, SocketAddress socketAddress, int n) {
        }

        @Override
        public void onRegionFileRead(RegionStorageInfo regionStorageInfo, ChunkPos chunkPos, RegionFileVersion regionFileVersion, int n) {
        }

        @Override
        public void onRegionFileWrite(RegionStorageInfo regionStorageInfo, ChunkPos chunkPos, RegionFileVersion regionFileVersion, int n) {
        }

        @Override
        public void onServerTick(float f) {
        }

        @Override
        public ProfiledDuration onWorldLoadedStarted() {
            return noOpCommit;
        }

        @Override
        @Nullable
        public ProfiledDuration onChunkGenerate(ChunkPos chunkPos, ResourceKey<Level> resourceKey, String string) {
            return null;
        }
    }
}

