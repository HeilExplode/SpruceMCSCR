/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public record FileIOStat(Duration duration, @Nullable String path, long bytes) {
    public static Summary summary(Duration duration, List<FileIOStat> list) {
        long l = list.stream().mapToLong(fileIOStat -> fileIOStat.bytes).sum();
        return new Summary(l, (double)l / (double)duration.getSeconds(), list.size(), (double)list.size() / (double)duration.getSeconds(), list.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus), list.stream().filter(fileIOStat -> fileIOStat.path != null).collect(Collectors.groupingBy(fileIOStat -> fileIOStat.path, Collectors.summingLong(fileIOStat -> fileIOStat.bytes))).entrySet().stream().sorted(Map.Entry.comparingByValue().reversed()).map(entry -> Pair.of((Object)((String)entry.getKey()), (Object)((Long)entry.getValue()))).limit(10L).toList());
    }

    public record Summary(long totalBytes, double bytesPerSecond, long counts, double countsPerSecond, Duration timeSpentInIO, List<Pair<String, Long>> topTenContributorsByTotalBytes) {
    }
}

