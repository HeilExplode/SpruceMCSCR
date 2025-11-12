/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t, ServerLevel serverLevel) throws RunningOnDifferentThreadException {
        PacketUtils.ensureRunningOnSameThread(packet, t, serverLevel.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t, BlockableEventLoop<?> blockableEventLoop) throws RunningOnDifferentThreadException {
        if (!blockableEventLoop.isSameThread()) {
            blockableEventLoop.executeIfPossible(() -> {
                if (t.shouldHandleMessage(packet)) {
                    try {
                        packet.handle(t);
                    }
                    catch (Exception exception) {
                        ReportedException reportedException;
                        if (exception instanceof ReportedException && (reportedException = (ReportedException)exception).getCause() instanceof OutOfMemoryError) {
                            throw PacketUtils.makeReportedException(exception, packet, t);
                        }
                        t.onPacketError(packet, exception);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)packet);
                }
            });
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }

    public static <T extends PacketListener> ReportedException makeReportedException(Exception exception, Packet<T> packet, T t) {
        if (exception instanceof ReportedException) {
            ReportedException reportedException = (ReportedException)exception;
            PacketUtils.fillCrashReport(reportedException.getReport(), t, packet);
            return reportedException;
        }
        CrashReport crashReport = CrashReport.forThrowable(exception, "Main thread packet handler");
        PacketUtils.fillCrashReport(crashReport, t, packet);
        return new ReportedException(crashReport);
    }

    public static <T extends PacketListener> void fillCrashReport(CrashReport crashReport, T t, @Nullable Packet<T> packet) {
        if (packet != null) {
            CrashReportCategory crashReportCategory = crashReport.addCategory("Incoming Packet");
            crashReportCategory.setDetail("Type", () -> packet.type().toString());
            crashReportCategory.setDetail("Is Terminal", () -> Boolean.toString(packet.isTerminal()));
            crashReportCategory.setDetail("Is Skippable", () -> Boolean.toString(packet.isSkippable()));
        }
        t.fillCrashReport(crashReport);
    }
}

