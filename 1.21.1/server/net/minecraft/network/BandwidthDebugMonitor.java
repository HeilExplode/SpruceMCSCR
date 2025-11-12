/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.debugchart.LocalSampleLogger;

public class BandwidthDebugMonitor {
    private final AtomicInteger bytesReceived = new AtomicInteger();
    private final LocalSampleLogger bandwidthLogger;

    public BandwidthDebugMonitor(LocalSampleLogger localSampleLogger) {
        this.bandwidthLogger = localSampleLogger;
    }

    public void onReceive(int n) {
        this.bytesReceived.getAndAdd(n);
    }

    public void tick() {
        this.bandwidthLogger.logSample(this.bytesReceived.getAndSet(0));
    }
}

