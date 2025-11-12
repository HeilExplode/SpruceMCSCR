/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.util.debugchart;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.debugchart.RemoteDebugSampleType;

public class DebugSampleSubscriptionTracker {
    public static final int STOP_SENDING_AFTER_TICKS = 200;
    public static final int STOP_SENDING_AFTER_MS = 10000;
    private final PlayerList playerList;
    private final EnumMap<RemoteDebugSampleType, Map<ServerPlayer, SubscriptionStartedAt>> subscriptions;
    private final Queue<SubscriptionRequest> subscriptionRequestQueue = new LinkedList<SubscriptionRequest>();

    public DebugSampleSubscriptionTracker(PlayerList playerList) {
        this.playerList = playerList;
        this.subscriptions = new EnumMap(RemoteDebugSampleType.class);
        for (RemoteDebugSampleType remoteDebugSampleType : RemoteDebugSampleType.values()) {
            this.subscriptions.put(remoteDebugSampleType, Maps.newHashMap());
        }
    }

    public boolean shouldLogSamples(RemoteDebugSampleType remoteDebugSampleType) {
        return !this.subscriptions.get((Object)remoteDebugSampleType).isEmpty();
    }

    public void broadcast(ClientboundDebugSamplePacket clientboundDebugSamplePacket) {
        Set<ServerPlayer> set = this.subscriptions.get((Object)clientboundDebugSamplePacket.debugSampleType()).keySet();
        for (ServerPlayer serverPlayer : set) {
            serverPlayer.connection.send(clientboundDebugSamplePacket);
        }
    }

    public void subscribe(ServerPlayer serverPlayer, RemoteDebugSampleType remoteDebugSampleType) {
        if (this.playerList.isOp(serverPlayer.getGameProfile())) {
            this.subscriptionRequestQueue.add(new SubscriptionRequest(serverPlayer, remoteDebugSampleType));
        }
    }

    public void tick(int n) {
        long l = Util.getMillis();
        this.handleSubscriptions(l, n);
        this.handleUnsubscriptions(l, n);
    }

    private void handleSubscriptions(long l, int n) {
        for (SubscriptionRequest subscriptionRequest : this.subscriptionRequestQueue) {
            this.subscriptions.get((Object)subscriptionRequest.sampleType()).put(subscriptionRequest.player(), new SubscriptionStartedAt(l, n));
        }
    }

    private void handleUnsubscriptions(long l, int n) {
        for (Map<ServerPlayer, SubscriptionStartedAt> map : this.subscriptions.values()) {
            map.entrySet().removeIf(entry -> {
                boolean bl = !this.playerList.isOp(((ServerPlayer)entry.getKey()).getGameProfile());
                SubscriptionStartedAt subscriptionStartedAt = (SubscriptionStartedAt)entry.getValue();
                return bl || n > subscriptionStartedAt.tick() + 200 && l > subscriptionStartedAt.millis() + 10000L;
            });
        }
    }

    record SubscriptionRequest(ServerPlayer player, RemoteDebugSampleType sampleType) {
    }

    record SubscriptionStartedAt(long millis, int tick) {
    }
}

