/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.stats.TimedStat;

public record TimedStatSummary<T extends TimedStat>(T fastest, T slowest, @Nullable T secondSlowest, int count, Map<Integer, Double> percentilesNanos, Duration totalDuration) {
    public static <T extends TimedStat> TimedStatSummary<T> summary(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("No values");
        }
        List<TimedStat> list2 = list.stream().sorted(Comparator.comparing(TimedStat::duration)).toList();
        Duration duration = list2.stream().map(TimedStat::duration).reduce(Duration::plus).orElse(Duration.ZERO);
        TimedStat timedStat2 = list2.get(0);
        TimedStat timedStat3 = list2.get(list2.size() - 1);
        TimedStat timedStat4 = list2.size() > 1 ? list2.get(list2.size() - 2) : null;
        int n = list2.size();
        Map<Integer, Double> map = Percentiles.evaluate(list2.stream().mapToLong(timedStat -> timedStat.duration().toNanos()).toArray());
        return new TimedStatSummary<TimedStat>(timedStat2, timedStat3, timedStat4, n, map, duration);
    }
}

