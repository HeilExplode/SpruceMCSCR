/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMaps
 *  org.apache.commons.io.IOUtils
 *  org.apache.commons.lang3.ObjectUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.BufferedWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.ReportType;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerPathEntry;
import net.minecraft.util.profiling.ResultField;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class FilledProfileResults
implements ProfileResults {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ProfilerPathEntry EMPTY = new ProfilerPathEntry(){

        @Override
        public long getDuration() {
            return 0L;
        }

        @Override
        public long getMaxDuration() {
            return 0L;
        }

        @Override
        public long getCount() {
            return 0L;
        }

        @Override
        public Object2LongMap<String> getCounters() {
            return Object2LongMaps.emptyMap();
        }
    };
    private static final Splitter SPLITTER = Splitter.on((char)'\u001e');
    private static final Comparator<Map.Entry<String, CounterCollector>> COUNTER_ENTRY_COMPARATOR = Map.Entry.comparingByValue(Comparator.comparingLong(counterCollector -> counterCollector.totalValue)).reversed();
    private final Map<String, ? extends ProfilerPathEntry> entries;
    private final long startTimeNano;
    private final int startTimeTicks;
    private final long endTimeNano;
    private final int endTimeTicks;
    private final int tickDuration;

    public FilledProfileResults(Map<String, ? extends ProfilerPathEntry> map, long l, int n, long l2, int n2) {
        this.entries = map;
        this.startTimeNano = l;
        this.startTimeTicks = n;
        this.endTimeNano = l2;
        this.endTimeTicks = n2;
        this.tickDuration = n2 - n;
    }

    private ProfilerPathEntry getEntry(String string) {
        ProfilerPathEntry profilerPathEntry = this.entries.get(string);
        return profilerPathEntry != null ? profilerPathEntry : EMPTY;
    }

    @Override
    public List<ResultField> getTimes(String object) {
        Object string = object;
        ProfilerPathEntry profilerPathEntry = this.getEntry("root");
        long l = profilerPathEntry.getDuration();
        ProfilerPathEntry profilerPathEntry2 = this.getEntry((String)object);
        long l2 = profilerPathEntry2.getDuration();
        long l3 = profilerPathEntry2.getCount();
        ArrayList arrayList = Lists.newArrayList();
        if (!((String)object).isEmpty()) {
            object = (String)object + "\u001e";
        }
        long l4 = 0L;
        for (String object2 : this.entries.keySet()) {
            if (!FilledProfileResults.isDirectChild((String)object, object2)) continue;
            l4 += this.getEntry(object2).getDuration();
        }
        float f = l4;
        if (l4 < l2) {
            l4 = l2;
        }
        if (l < l4) {
            l = l4;
        }
        for (String string2 : this.entries.keySet()) {
            if (!FilledProfileResults.isDirectChild((String)object, string2)) continue;
            ProfilerPathEntry profilerPathEntry3 = this.getEntry(string2);
            long l5 = profilerPathEntry3.getDuration();
            double d = (double)l5 * 100.0 / (double)l4;
            double d2 = (double)l5 * 100.0 / (double)l;
            String string3 = string2.substring(((String)object).length());
            arrayList.add(new ResultField(string3, d, d2, profilerPathEntry3.getCount()));
        }
        if ((float)l4 > f) {
            arrayList.add(new ResultField("unspecified", (double)((float)l4 - f) * 100.0 / (double)l4, (double)((float)l4 - f) * 100.0 / (double)l, l3));
        }
        Collections.sort(arrayList);
        arrayList.add(0, new ResultField((String)string, 100.0, (double)l4 * 100.0 / (double)l, l3));
        return arrayList;
    }

    private static boolean isDirectChild(String string, String string2) {
        return string2.length() > string.length() && string2.startsWith(string) && string2.indexOf(30, string.length() + 1) < 0;
    }

    private Map<String, CounterCollector> getCounterValues() {
        TreeMap treeMap = Maps.newTreeMap();
        this.entries.forEach((string, profilerPathEntry) -> {
            Object2LongMap<String> object2LongMap = profilerPathEntry.getCounters();
            if (!object2LongMap.isEmpty()) {
                List list = SPLITTER.splitToList((CharSequence)string);
                object2LongMap.forEach((string2, l) -> treeMap.computeIfAbsent(string2, string -> new CounterCollector()).addValue(list.iterator(), (long)l));
            }
        });
        return treeMap;
    }

    @Override
    public long getStartTimeNano() {
        return this.startTimeNano;
    }

    @Override
    public int getStartTimeTicks() {
        return this.startTimeTicks;
    }

    @Override
    public long getEndTimeNano() {
        return this.endTimeNano;
    }

    @Override
    public int getEndTimeTicks() {
        return this.endTimeTicks;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean saveResults(Path path) {
        boolean bl;
        BufferedWriter bufferedWriter = null;
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);
            bufferedWriter.write(this.getProfilerResults(this.getNanoDuration(), this.getTickDuration()));
            bl = true;
        }
        catch (Throwable throwable) {
            boolean bl2;
            try {
                LOGGER.error("Could not save profiler results to {}", (Object)path, (Object)throwable);
                bl2 = false;
            }
            catch (Throwable throwable2) {
                IOUtils.closeQuietly(bufferedWriter);
                throw throwable2;
            }
            IOUtils.closeQuietly((Writer)bufferedWriter);
            return bl2;
        }
        IOUtils.closeQuietly((Writer)bufferedWriter);
        return bl;
    }

    protected String getProfilerResults(long l, int n) {
        StringBuilder stringBuilder = new StringBuilder();
        ReportType.PROFILE.appendHeader(stringBuilder, List.of());
        stringBuilder.append("Version: ").append(SharedConstants.getCurrentVersion().getId()).append('\n');
        stringBuilder.append("Time span: ").append(l / 1000000L).append(" ms\n");
        stringBuilder.append("Tick span: ").append(n).append(" ticks\n");
        stringBuilder.append("// This is approximately ").append(String.format(Locale.ROOT, "%.2f", Float.valueOf((float)n / ((float)l / 1.0E9f)))).append(" ticks per second. It should be ").append(20).append(" ticks per second\n\n");
        stringBuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
        this.appendProfilerResults(0, "root", stringBuilder);
        stringBuilder.append("--- END PROFILE DUMP ---\n\n");
        Map<String, CounterCollector> map = this.getCounterValues();
        if (!map.isEmpty()) {
            stringBuilder.append("--- BEGIN COUNTER DUMP ---\n\n");
            this.appendCounters(map, stringBuilder, n);
            stringBuilder.append("--- END COUNTER DUMP ---\n\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String getProfilerResults() {
        StringBuilder stringBuilder = new StringBuilder();
        this.appendProfilerResults(0, "root", stringBuilder);
        return stringBuilder.toString();
    }

    private static StringBuilder indentLine(StringBuilder stringBuilder, int n) {
        stringBuilder.append(String.format(Locale.ROOT, "[%02d] ", n));
        for (int i = 0; i < n; ++i) {
            stringBuilder.append("|   ");
        }
        return stringBuilder;
    }

    private void appendProfilerResults(int n, String string2, StringBuilder stringBuilder) {
        List<ResultField> list = this.getTimes(string2);
        Object2LongMap<String> object2LongMap = ((ProfilerPathEntry)ObjectUtils.firstNonNull((Object[])new ProfilerPathEntry[]{this.entries.get(string2), EMPTY})).getCounters();
        object2LongMap.forEach((string, l) -> FilledProfileResults.indentLine(stringBuilder, n).append('#').append((String)string).append(' ').append(l).append('/').append(l / (long)this.tickDuration).append('\n'));
        if (list.size() < 3) {
            return;
        }
        for (int i = 1; i < list.size(); ++i) {
            ResultField resultField = list.get(i);
            FilledProfileResults.indentLine(stringBuilder, n).append(resultField.name).append('(').append(resultField.count).append('/').append(String.format(Locale.ROOT, "%.0f", Float.valueOf((float)resultField.count / (float)this.tickDuration))).append(')').append(" - ").append(String.format(Locale.ROOT, "%.2f", resultField.percentage)).append("%/").append(String.format(Locale.ROOT, "%.2f", resultField.globalPercentage)).append("%\n");
            if ("unspecified".equals(resultField.name)) continue;
            try {
                this.appendProfilerResults(n + 1, string2 + "\u001e" + resultField.name, stringBuilder);
                continue;
            }
            catch (Exception exception) {
                stringBuilder.append("[[ EXCEPTION ").append(exception).append(" ]]");
            }
        }
    }

    private void appendCounterResults(int n, String string, CounterCollector counterCollector, int n2, StringBuilder stringBuilder) {
        FilledProfileResults.indentLine(stringBuilder, n).append(string).append(" total:").append(counterCollector.selfValue).append('/').append(counterCollector.totalValue).append(" average: ").append(counterCollector.selfValue / (long)n2).append('/').append(counterCollector.totalValue / (long)n2).append('\n');
        counterCollector.children.entrySet().stream().sorted(COUNTER_ENTRY_COMPARATOR).forEach(entry -> this.appendCounterResults(n + 1, (String)entry.getKey(), (CounterCollector)entry.getValue(), n2, stringBuilder));
    }

    private void appendCounters(Map<String, CounterCollector> map, StringBuilder stringBuilder, int n) {
        map.forEach((string, counterCollector) -> {
            stringBuilder.append("-- Counter: ").append((String)string).append(" --\n");
            this.appendCounterResults(0, "root", counterCollector.children.get("root"), n, stringBuilder);
            stringBuilder.append("\n\n");
        });
    }

    @Override
    public int getTickDuration() {
        return this.tickDuration;
    }

    static class CounterCollector {
        long selfValue;
        long totalValue;
        final Map<String, CounterCollector> children = Maps.newHashMap();

        CounterCollector() {
        }

        public void addValue(Iterator<String> iterator, long l) {
            this.totalValue += l;
            if (!iterator.hasNext()) {
                this.selfValue += l;
            } else {
                this.children.computeIfAbsent(iterator.next(), string -> new CounterCollector()).addValue(iterator, l);
            }
        }
    }
}

