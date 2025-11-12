/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketUtils;

public interface PacketListener {
    public PacketFlow flow();

    public ConnectionProtocol protocol();

    public void onDisconnect(DisconnectionDetails var1);

    default public void onPacketError(Packet packet, Exception exception) throws ReportedException {
        throw PacketUtils.makeReportedException(exception, packet, this);
    }

    default public DisconnectionDetails createDisconnectionInfo(Component component, Throwable throwable) {
        return new DisconnectionDetails(component);
    }

    public boolean isAcceptingMessages();

    default public boolean shouldHandleMessage(Packet<?> packet) {
        return this.isAcceptingMessages();
    }

    default public void fillCrashReport(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Connection");
        crashReportCategory.setDetail("Protocol", () -> this.protocol().id());
        crashReportCategory.setDetail("Flow", () -> this.flow().toString());
        this.fillListenerSpecificCrashDetails(crashReport, crashReportCategory);
    }

    default public void fillListenerSpecificCrashDetails(CrashReport crashReport, CrashReportCategory crashReportCategory) {
    }
}

