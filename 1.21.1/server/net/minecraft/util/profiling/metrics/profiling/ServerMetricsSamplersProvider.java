/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.base.Ticker
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.slf4j.Logger
 *  oshi.SystemInfo
 *  oshi.hardware.CentralProcessor
 */
package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import net.minecraft.SystemReport;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.profiling.ProfilerSamplerAdapter;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ServerMetricsSamplersProvider
implements MetricsSamplerProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Set<MetricSampler> samplers = new ObjectOpenHashSet();
    private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();

    public ServerMetricsSamplersProvider(LongSupplier longSupplier, boolean bl) {
        this.samplers.add(ServerMetricsSamplersProvider.tickTimeSampler(longSupplier));
        if (bl) {
            this.samplers.addAll(ServerMetricsSamplersProvider.runtimeIndependentSamplers());
        }
    }

    public static Set<MetricSampler> runtimeIndependentSamplers() {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        try {
            CpuStats cpuStats = new CpuStats();
            IntStream.range(0, cpuStats.nrOfCpus).mapToObj(n -> MetricSampler.create("cpu#" + n, MetricCategory.CPU, () -> cpuStats.loadForCpu(n))).forEach(arg_0 -> ((ImmutableSet.Builder)builder).add(arg_0));
        }
        catch (Throwable throwable) {
            LOGGER.warn("Failed to query cpu, no cpu stats will be recorded", throwable);
        }
        builder.add((Object)MetricSampler.create("heap MiB", MetricCategory.JVM, () -> SystemReport.sizeInMiB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
        builder.addAll(MetricsRegistry.INSTANCE.getRegisteredSamplers());
        return builder.build();
    }

    @Override
    public Set<MetricSampler> samplers(Supplier<ProfileCollector> supplier) {
        this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler(supplier));
        return this.samplers;
    }

    public static MetricSampler tickTimeSampler(final LongSupplier longSupplier) {
        Stopwatch stopwatch2 = Stopwatch.createUnstarted((Ticker)new Ticker(){

            public long read() {
                return longSupplier.getAsLong();
            }
        });
        ToDoubleFunction<Stopwatch> toDoubleFunction = stopwatch -> {
            if (stopwatch.isRunning()) {
                stopwatch.stop();
            }
            long l = stopwatch.elapsed(TimeUnit.NANOSECONDS);
            stopwatch.reset();
            return l;
        };
        MetricSampler.ValueIncreasedByPercentage valueIncreasedByPercentage = new MetricSampler.ValueIncreasedByPercentage(2.0f);
        return MetricSampler.builder("ticktime", MetricCategory.TICK_LOOP, toDoubleFunction, stopwatch2).withBeforeTick(Stopwatch::start).withThresholdAlert(valueIncreasedByPercentage).build();
    }

    static class CpuStats {
        private final SystemInfo systemInfo = new SystemInfo();
        private final CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
        public final int nrOfCpus = this.processor.getLogicalProcessorCount();
        private long[][] previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
        private double[] currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
        private long lastPollMs;

        CpuStats() {
        }

        public double loadForCpu(int n) {
            long l = System.currentTimeMillis();
            if (this.lastPollMs == 0L || this.lastPollMs + 501L < l) {
                this.currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
                this.previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
                this.lastPollMs = l;
            }
            return this.currentLoad[n] * 100.0;
        }
    }
}

