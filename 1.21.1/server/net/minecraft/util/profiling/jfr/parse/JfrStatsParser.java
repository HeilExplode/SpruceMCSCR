/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.util.profiling.jfr.parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.ChunkIdentification;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.IoSummary;
import net.minecraft.util.profiling.jfr.stats.PacketIdentification;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;

public class JfrStatsParser {
    private Instant recordingStarted = Instant.EPOCH;
    private Instant recordingEnded = Instant.EPOCH;
    private final List<ChunkGenStat> chunkGenStats = Lists.newArrayList();
    private final List<CpuLoadStat> cpuLoadStat = Lists.newArrayList();
    private final Map<PacketIdentification, MutableCountAndSize> receivedPackets = Maps.newHashMap();
    private final Map<PacketIdentification, MutableCountAndSize> sentPackets = Maps.newHashMap();
    private final Map<ChunkIdentification, MutableCountAndSize> readChunks = Maps.newHashMap();
    private final Map<ChunkIdentification, MutableCountAndSize> writtenChunks = Maps.newHashMap();
    private final List<FileIOStat> fileWrites = Lists.newArrayList();
    private final List<FileIOStat> fileReads = Lists.newArrayList();
    private int garbageCollections;
    private Duration gcTotalDuration = Duration.ZERO;
    private final List<GcHeapStat> gcHeapStats = Lists.newArrayList();
    private final List<ThreadAllocationStat> threadAllocationStats = Lists.newArrayList();
    private final List<TickTimeStat> tickTimes = Lists.newArrayList();
    @Nullable
    private Duration worldCreationDuration = null;

    private JfrStatsParser(Stream<RecordedEvent> stream) {
        this.capture(stream);
    }

    public static JfrStatsResult parse(Path path) {
        JfrStatsResult jfrStatsResult;
        final RecordingFile recordingFile = new RecordingFile(path);
        try {
            Iterator<RecordedEvent> iterator = new Iterator<RecordedEvent>(){

                @Override
                public boolean hasNext() {
                    return recordingFile.hasMoreEvents();
                }

                @Override
                public RecordedEvent next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return recordingFile.readEvent();
                    }
                    catch (IOException iOException) {
                        throw new UncheckedIOException(iOException);
                    }
                }

                @Override
                public /* synthetic */ Object next() {
                    return this.next();
                }
            };
            Stream<RecordedEvent> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1297), false);
            jfrStatsResult = new JfrStatsParser(stream).results();
        }
        catch (Throwable throwable) {
            try {
                try {
                    recordingFile.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                throw new UncheckedIOException(iOException);
            }
        }
        recordingFile.close();
        return jfrStatsResult;
    }

    private JfrStatsResult results() {
        Duration duration = Duration.between(this.recordingStarted, this.recordingEnded);
        return new JfrStatsResult(this.recordingStarted, this.recordingEnded, duration, this.worldCreationDuration, this.tickTimes, this.cpuLoadStat, GcHeapStat.summary(duration, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections), ThreadAllocationStat.summary(this.threadAllocationStats), JfrStatsParser.collectIoStats(duration, this.receivedPackets), JfrStatsParser.collectIoStats(duration, this.sentPackets), JfrStatsParser.collectIoStats(duration, this.writtenChunks), JfrStatsParser.collectIoStats(duration, this.readChunks), FileIOStat.summary(duration, this.fileWrites), FileIOStat.summary(duration, this.fileReads), this.chunkGenStats);
    }

    private void capture(Stream<RecordedEvent> stream) {
        stream.forEach(recordedEvent -> {
            if (recordedEvent.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
                this.recordingEnded = recordedEvent.getEndTime();
            }
            if (recordedEvent.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
                this.recordingStarted = recordedEvent.getStartTime();
            }
            switch (recordedEvent.getEventType().getName()) {
                case "minecraft.ChunkGeneration": {
                    this.chunkGenStats.add(ChunkGenStat.from(recordedEvent));
                    break;
                }
                case "minecraft.LoadWorld": {
                    this.worldCreationDuration = recordedEvent.getDuration();
                    break;
                }
                case "minecraft.ServerTickTime": {
                    this.tickTimes.add(TickTimeStat.from(recordedEvent));
                    break;
                }
                case "minecraft.PacketReceived": {
                    this.incrementPacket((RecordedEvent)recordedEvent, recordedEvent.getInt("bytes"), this.receivedPackets);
                    break;
                }
                case "minecraft.PacketSent": {
                    this.incrementPacket((RecordedEvent)recordedEvent, recordedEvent.getInt("bytes"), this.sentPackets);
                    break;
                }
                case "minecraft.ChunkRegionRead": {
                    this.incrementChunk((RecordedEvent)recordedEvent, recordedEvent.getInt("bytes"), this.readChunks);
                    break;
                }
                case "minecraft.ChunkRegionWrite": {
                    this.incrementChunk((RecordedEvent)recordedEvent, recordedEvent.getInt("bytes"), this.writtenChunks);
                    break;
                }
                case "jdk.ThreadAllocationStatistics": {
                    this.threadAllocationStats.add(ThreadAllocationStat.from(recordedEvent));
                    break;
                }
                case "jdk.GCHeapSummary": {
                    this.gcHeapStats.add(GcHeapStat.from(recordedEvent));
                    break;
                }
                case "jdk.CPULoad": {
                    this.cpuLoadStat.add(CpuLoadStat.from(recordedEvent));
                    break;
                }
                case "jdk.FileWrite": {
                    this.appendFileIO((RecordedEvent)recordedEvent, this.fileWrites, "bytesWritten");
                    break;
                }
                case "jdk.FileRead": {
                    this.appendFileIO((RecordedEvent)recordedEvent, this.fileReads, "bytesRead");
                    break;
                }
                case "jdk.GarbageCollection": {
                    ++this.garbageCollections;
                    this.gcTotalDuration = this.gcTotalDuration.plus(recordedEvent.getDuration());
                    break;
                }
            }
        });
    }

    private void incrementPacket(RecordedEvent recordedEvent, int n, Map<PacketIdentification, MutableCountAndSize> map) {
        map.computeIfAbsent(PacketIdentification.from(recordedEvent), packetIdentification -> new MutableCountAndSize()).increment(n);
    }

    private void incrementChunk(RecordedEvent recordedEvent, int n, Map<ChunkIdentification, MutableCountAndSize> map) {
        map.computeIfAbsent(ChunkIdentification.from(recordedEvent), chunkIdentification -> new MutableCountAndSize()).increment(n);
    }

    private void appendFileIO(RecordedEvent recordedEvent, List<FileIOStat> list, String string) {
        list.add(new FileIOStat(recordedEvent.getDuration(), recordedEvent.getString("path"), recordedEvent.getLong(string)));
    }

    private static <T> IoSummary<T> collectIoStats(Duration duration, Map<T, MutableCountAndSize> map) {
        List list = map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), (Object)((MutableCountAndSize)entry.getValue()).toCountAndSize())).toList();
        return new IoSummary(duration, list);
    }

    public static final class MutableCountAndSize {
        private long count;
        private long totalSize;

        public void increment(int n) {
            this.totalSize += (long)n;
            ++this.count;
        }

        public IoSummary.CountAndSize toCountAndSize() {
            return new IoSummary.CountAndSize(this.count, this.totalSize);
        }
    }
}

