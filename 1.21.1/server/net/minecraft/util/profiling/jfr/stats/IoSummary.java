/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public final class IoSummary<T> {
    private final CountAndSize totalCountAndSize;
    private final List<Pair<T, CountAndSize>> largestSizeContributors;
    private final Duration recordingDuration;

    public IoSummary(Duration duration, List<Pair<T, CountAndSize>> list) {
        this.recordingDuration = duration;
        this.totalCountAndSize = list.stream().map(Pair::getSecond).reduce(new CountAndSize(0L, 0L), CountAndSize::add);
        this.largestSizeContributors = list.stream().sorted(Comparator.comparing(Pair::getSecond, CountAndSize.SIZE_THEN_COUNT)).limit(10L).toList();
    }

    public double getCountsPerSecond() {
        return (double)this.totalCountAndSize.totalCount / (double)this.recordingDuration.getSeconds();
    }

    public double getSizePerSecond() {
        return (double)this.totalCountAndSize.totalSize / (double)this.recordingDuration.getSeconds();
    }

    public long getTotalCount() {
        return this.totalCountAndSize.totalCount;
    }

    public long getTotalSize() {
        return this.totalCountAndSize.totalSize;
    }

    public List<Pair<T, CountAndSize>> largestSizeContributors() {
        return this.largestSizeContributors;
    }

    public static final class CountAndSize
    extends Record {
        final long totalCount;
        final long totalSize;
        static final Comparator<CountAndSize> SIZE_THEN_COUNT = Comparator.comparing(CountAndSize::totalSize).thenComparing(CountAndSize::totalCount).reversed();

        public CountAndSize(long l, long l2) {
            this.totalCount = l;
            this.totalSize = l2;
        }

        CountAndSize add(CountAndSize countAndSize) {
            return new CountAndSize(this.totalCount + countAndSize.totalCount, this.totalSize + countAndSize.totalSize);
        }

        public float averageSize() {
            return (float)this.totalSize / (float)this.totalCount;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CountAndSize.class, "totalCount;totalSize", "totalCount", "totalSize"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CountAndSize.class, "totalCount;totalSize", "totalCount", "totalSize"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CountAndSize.class, "totalCount;totalSize", "totalCount", "totalSize"}, this, object);
        }

        public long totalCount() {
            return this.totalCount;
        }

        public long totalSize() {
            return this.totalSize;
        }
    }
}

