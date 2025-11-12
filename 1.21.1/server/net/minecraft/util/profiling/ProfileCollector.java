/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.minecraft.util.profiling;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;

public interface ProfileCollector
extends ProfilerFiller {
    public ProfileResults getResults();

    @Nullable
    public ActiveProfiler.PathEntry getEntry(String var1);

    public Set<Pair<String, MetricCategory>> getChartedPaths();
}

