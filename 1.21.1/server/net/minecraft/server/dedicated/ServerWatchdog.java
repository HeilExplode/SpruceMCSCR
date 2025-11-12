/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportType;
import net.minecraft.Util;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class ServerWatchdog
implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long MAX_SHUTDOWN_TIME = 10000L;
    private static final int SHUTDOWN_STATUS = 1;
    private final DedicatedServer server;
    private final long maxTickTimeNanos;

    public ServerWatchdog(DedicatedServer dedicatedServer) {
        this.server = dedicatedServer;
        this.maxTickTimeNanos = dedicatedServer.getMaxTickLength() * TimeUtil.NANOSECONDS_PER_MILLISECOND;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            long l = this.server.getNextTickTime();
            long l2 = Util.getNanos();
            long l3 = l2 - l;
            if (l3 > this.maxTickTimeNanos) {
                LOGGER.error(LogUtils.FATAL_MARKER, "A single server tick took {} seconds (should be max {})", (Object)String.format(Locale.ROOT, "%.2f", Float.valueOf((float)l3 / (float)TimeUtil.NANOSECONDS_PER_SECOND)), (Object)String.format(Locale.ROOT, "%.2f", Float.valueOf(this.server.tickRateManager().millisecondsPerTick() / (float)TimeUtil.MILLISECONDS_PER_SECOND)));
                LOGGER.error(LogUtils.FATAL_MARKER, "Considering it to be crashed, server will forcibly shutdown.");
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                ThreadInfo[] threadInfoArray = threadMXBean.dumpAllThreads(true, true);
                StringBuilder stringBuilder = new StringBuilder();
                Error error = new Error("Watchdog");
                for (ThreadInfo threadInfo : threadInfoArray) {
                    if (threadInfo.getThreadId() == this.server.getRunningThread().getId()) {
                        error.setStackTrace(threadInfo.getStackTrace());
                    }
                    stringBuilder.append(threadInfo);
                    stringBuilder.append("\n");
                }
                CrashReport crashReport = new CrashReport("Watching Server", error);
                this.server.fillSystemReport(crashReport.getSystemReport());
                CrashReportCategory crashReportCategory = crashReport.addCategory("Thread Dump");
                crashReportCategory.setDetail("Threads", stringBuilder);
                CrashReportCategory crashReportCategory2 = crashReport.addCategory("Performance stats");
                crashReportCategory2.setDetail("Random tick rate", () -> this.server.getWorldData().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).toString());
                crashReportCategory2.setDetail("Level stats", () -> Streams.stream(this.server.getAllLevels()).map(serverLevel -> String.valueOf(serverLevel.dimension()) + ": " + serverLevel.getWatchdogStats()).collect(Collectors.joining(",\n")));
                Bootstrap.realStdoutPrintln("Crash report:\n" + crashReport.getFriendlyReport(ReportType.CRASH));
                Path object2 = this.server.getServerDirectory().resolve("crash-reports").resolve("crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
                if (crashReport.saveToFile(object2, ReportType.CRASH)) {
                    LOGGER.error("This crash report has been saved to: {}", (Object)object2.toAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }
                this.exit();
            }
            try {
                Thread.sleep((l + this.maxTickTimeNanos - l2) / TimeUtil.NANOSECONDS_PER_MILLISECOND);
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    private void exit() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask(this){

                @Override
                public void run() {
                    Runtime.getRuntime().halt(1);
                }
            }, 10000L);
            System.exit(1);
        }
        catch (Throwable throwable) {
            Runtime.getRuntime().halt(1);
        }
    }
}

