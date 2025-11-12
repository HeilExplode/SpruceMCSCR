/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.players;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class SleepStatus {
    private int activePlayers;
    private int sleepingPlayers;

    public boolean areEnoughSleeping(int n) {
        return this.sleepingPlayers >= this.sleepersNeeded(n);
    }

    public boolean areEnoughDeepSleeping(int n, List<ServerPlayer> list) {
        int n2 = (int)list.stream().filter(Player::isSleepingLongEnough).count();
        return n2 >= this.sleepersNeeded(n);
    }

    public int sleepersNeeded(int n) {
        return Math.max(1, Mth.ceil((float)(this.activePlayers * n) / 100.0f));
    }

    public void removeAllSleepers() {
        this.sleepingPlayers = 0;
    }

    public int amountSleeping() {
        return this.sleepingPlayers;
    }

    public boolean update(List<ServerPlayer> list) {
        int n = this.activePlayers;
        int n2 = this.sleepingPlayers;
        this.activePlayers = 0;
        this.sleepingPlayers = 0;
        for (ServerPlayer serverPlayer : list) {
            if (serverPlayer.isSpectator()) continue;
            ++this.activePlayers;
            if (!serverPlayer.isSleeping()) continue;
            ++this.sleepingPlayers;
        }
        return !(n2 <= 0 && this.sleepingPlayers <= 0 || n == this.activePlayers && n2 == this.sleepingPlayers);
    }
}

