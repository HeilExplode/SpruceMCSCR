/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.slf4j.Logger;

public class RateKickingConnection
extends Connection {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component EXCEED_REASON = Component.translatable("disconnect.exceeded_packet_rate");
    private final int rateLimitPacketsPerSecond;

    public RateKickingConnection(int n) {
        super(PacketFlow.SERVERBOUND);
        this.rateLimitPacketsPerSecond = n;
    }

    @Override
    protected void tickSecond() {
        super.tickSecond();
        float f = this.getAverageReceivedPackets();
        if (f > (float)this.rateLimitPacketsPerSecond) {
            LOGGER.warn("Player exceeded rate-limit (sent {} packets per second)", (Object)Float.valueOf(f));
            this.send(new ClientboundDisconnectPacket(EXCEED_REASON), PacketSendListener.thenRun(() -> this.disconnect(EXCEED_REASON)));
            this.setReadOnly();
        }
    }
}

