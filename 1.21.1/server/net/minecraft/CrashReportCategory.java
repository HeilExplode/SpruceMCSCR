/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CrashReportCategory {
    private final String title;
    private final List<Entry> entries = Lists.newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportCategory(String string) {
        this.title = string;
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, double d, double d2, double d3) {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", d, d2, d3, CrashReportCategory.formatLocation(levelHeightAccessor, BlockPos.containing(d, d2, d3)));
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, BlockPos blockPos) {
        return CrashReportCategory.formatLocation(levelHeightAccessor, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, int n, int n2, int n3) {
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10;
        int n11;
        int n12;
        int n13;
        int n14;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(String.format(Locale.ROOT, "World: (%d,%d,%d)", n, n2, n3));
        }
        catch (Throwable throwable) {
            stringBuilder.append("(Error finding world loc)");
        }
        stringBuilder.append(", ");
        try {
            int n15 = SectionPos.blockToSectionCoord(n);
            n14 = SectionPos.blockToSectionCoord(n2);
            n13 = SectionPos.blockToSectionCoord(n3);
            n12 = n & 0xF;
            n11 = n2 & 0xF;
            n10 = n3 & 0xF;
            n9 = SectionPos.sectionToBlockCoord(n15);
            n8 = levelHeightAccessor.getMinBuildHeight();
            n7 = SectionPos.sectionToBlockCoord(n13);
            n6 = SectionPos.sectionToBlockCoord(n15 + 1) - 1;
            n5 = levelHeightAccessor.getMaxBuildHeight() - 1;
            n4 = SectionPos.sectionToBlockCoord(n13 + 1) - 1;
            stringBuilder.append(String.format(Locale.ROOT, "Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)", n12, n11, n10, n15, n14, n13, n9, n8, n7, n6, n5, n4));
        }
        catch (Throwable throwable) {
            stringBuilder.append("(Error finding chunk loc)");
        }
        stringBuilder.append(", ");
        try {
            int n16 = n >> 9;
            n14 = n3 >> 9;
            n13 = n16 << 5;
            n12 = n14 << 5;
            n11 = (n16 + 1 << 5) - 1;
            n10 = (n14 + 1 << 5) - 1;
            n9 = n16 << 9;
            n8 = levelHeightAccessor.getMinBuildHeight();
            n7 = n14 << 9;
            n6 = (n16 + 1 << 9) - 1;
            n5 = levelHeightAccessor.getMaxBuildHeight() - 1;
            n4 = (n14 + 1 << 9) - 1;
            stringBuilder.append(String.format(Locale.ROOT, "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)", n16, n14, n13, n12, n11, n10, n9, n8, n7, n6, n5, n4));
        }
        catch (Throwable throwable) {
            stringBuilder.append("(Error finding world loc)");
        }
        return stringBuilder.toString();
    }

    public CrashReportCategory setDetail(String string, CrashReportDetail<String> crashReportDetail) {
        try {
            this.setDetail(string, crashReportDetail.call());
        }
        catch (Throwable throwable) {
            this.setDetailError(string, throwable);
        }
        return this;
    }

    public CrashReportCategory setDetail(String string, Object object) {
        this.entries.add(new Entry(string, object));
        return this;
    }

    public void setDetailError(String string, Throwable throwable) {
        this.setDetail(string, throwable);
    }

    public int fillInStackTrace(int n) {
        StackTraceElement[] stackTraceElementArray = Thread.currentThread().getStackTrace();
        if (stackTraceElementArray.length <= 0) {
            return 0;
        }
        this.stackTrace = new StackTraceElement[stackTraceElementArray.length - 3 - n];
        System.arraycopy(stackTraceElementArray, 3 + n, this.stackTrace, 0, this.stackTrace.length);
        return this.stackTrace.length;
    }

    public boolean validateStackTrace(StackTraceElement stackTraceElement, StackTraceElement stackTraceElement2) {
        if (this.stackTrace.length == 0 || stackTraceElement == null) {
            return false;
        }
        StackTraceElement stackTraceElement3 = this.stackTrace[0];
        if (!(stackTraceElement3.isNativeMethod() == stackTraceElement.isNativeMethod() && stackTraceElement3.getClassName().equals(stackTraceElement.getClassName()) && stackTraceElement3.getFileName().equals(stackTraceElement.getFileName()) && stackTraceElement3.getMethodName().equals(stackTraceElement.getMethodName()))) {
            return false;
        }
        if (stackTraceElement2 != null != this.stackTrace.length > 1) {
            return false;
        }
        if (stackTraceElement2 != null && !this.stackTrace[1].equals(stackTraceElement2)) {
            return false;
        }
        this.stackTrace[0] = stackTraceElement;
        return true;
    }

    public void trimStacktrace(int n) {
        StackTraceElement[] stackTraceElementArray = new StackTraceElement[this.stackTrace.length - n];
        System.arraycopy(this.stackTrace, 0, stackTraceElementArray, 0, stackTraceElementArray.length);
        this.stackTrace = stackTraceElementArray;
    }

    public void getDetails(StringBuilder stringBuilder) {
        stringBuilder.append("-- ").append(this.title).append(" --\n");
        stringBuilder.append("Details:");
        for (Entry entry : this.entries) {
            stringBuilder.append("\n\t");
            stringBuilder.append(entry.getKey());
            stringBuilder.append(": ");
            stringBuilder.append(entry.getValue());
        }
        if (this.stackTrace != null && this.stackTrace.length > 0) {
            stringBuilder.append("\nStacktrace:");
            for (StackTraceElement stackTraceElement : this.stackTrace) {
                stringBuilder.append("\n\tat ");
                stringBuilder.append(stackTraceElement);
            }
        }
    }

    public StackTraceElement[] getStacktrace() {
        return this.stackTrace;
    }

    public static void populateBlockDetails(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor, BlockPos blockPos, @Nullable BlockState blockState) {
        if (blockState != null) {
            crashReportCategory.setDetail("Block", blockState::toString);
        }
        crashReportCategory.setDetail("Block location", () -> CrashReportCategory.formatLocation(levelHeightAccessor, blockPos));
    }

    static class Entry {
        private final String key;
        private final String value;

        public Entry(String string, @Nullable Object object) {
            this.key = string;
            if (object == null) {
                this.value = "~~NULL~~";
            } else if (object instanceof Throwable) {
                Throwable throwable = (Throwable)object;
                this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            } else {
                this.value = object.toString();
            }
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}

