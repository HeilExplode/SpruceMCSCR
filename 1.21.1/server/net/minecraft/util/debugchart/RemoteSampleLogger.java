/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.debugchart;

import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.util.debugchart.AbstractSampleLogger;
import net.minecraft.util.debugchart.DebugSampleSubscriptionTracker;
import net.minecraft.util.debugchart.RemoteDebugSampleType;

public class RemoteSampleLogger
extends AbstractSampleLogger {
    private final DebugSampleSubscriptionTracker subscriptionTracker;
    private final RemoteDebugSampleType sampleType;

    public RemoteSampleLogger(int n, DebugSampleSubscriptionTracker debugSampleSubscriptionTracker, RemoteDebugSampleType remoteDebugSampleType) {
        this(n, debugSampleSubscriptionTracker, remoteDebugSampleType, new long[n]);
    }

    public RemoteSampleLogger(int n, DebugSampleSubscriptionTracker debugSampleSubscriptionTracker, RemoteDebugSampleType remoteDebugSampleType, long[] lArray) {
        super(n, lArray);
        this.subscriptionTracker = debugSampleSubscriptionTracker;
        this.sampleType = remoteDebugSampleType;
    }

    @Override
    protected void useSample() {
        this.subscriptionTracker.broadcast(new ClientboundDebugSamplePacket((long[])this.sample.clone(), this.sampleType));
    }
}

