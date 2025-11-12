/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 */
package net.minecraft.util.profiling.jfr.stats;

import com.google.common.base.MoreObjects;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public record ThreadAllocationStat(Instant timestamp, String threadName, long totalBytes) {
    private static final String UNKNOWN_THREAD = "unknown";

    public static ThreadAllocationStat from(RecordedEvent recordedEvent) {
        RecordedThread recordedThread = recordedEvent.getThread("thread");
        String string = recordedThread == null ? UNKNOWN_THREAD : (String)MoreObjects.firstNonNull((Object)recordedThread.getJavaName(), (Object)UNKNOWN_THREAD);
        return new ThreadAllocationStat(recordedEvent.getStartTime(), string, recordedEvent.getLong("allocated"));
    }

    public static Summary summary(List<ThreadAllocationStat> list2) {
        TreeMap<String, Double> treeMap = new TreeMap<String, Double>();
        Map<String, List<ThreadAllocationStat>> map = list2.stream().collect(Collectors.groupingBy(threadAllocationStat -> threadAllocationStat.threadName));
        map.forEach((string, list) -> {
            if (list.size() < 2) {
                return;
            }
            ThreadAllocationStat threadAllocationStat = (ThreadAllocationStat)list.get(0);
            ThreadAllocationStat threadAllocationStat2 = (ThreadAllocationStat)list.get(list.size() - 1);
            long l = Duration.between(threadAllocationStat.timestamp, threadAllocationStat2.timestamp).getSeconds();
            long l2 = threadAllocationStat2.totalBytes - threadAllocationStat.totalBytes;
            treeMap.put((String)string, (double)l2 / (double)l);
        });
        return new Summary(treeMap);
    }

    public record Summary(Map<String, Double> allocationsPerSecondByThread) {
    }
}

