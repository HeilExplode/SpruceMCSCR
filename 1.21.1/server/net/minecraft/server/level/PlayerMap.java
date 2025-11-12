/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerMap {
    private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap();

    public Set<ServerPlayer> getAllPlayers() {
        return this.players.keySet();
    }

    public void addPlayer(ServerPlayer serverPlayer, boolean bl) {
        this.players.put((Object)serverPlayer, bl);
    }

    public void removePlayer(ServerPlayer serverPlayer) {
        this.players.removeBoolean((Object)serverPlayer);
    }

    public void ignorePlayer(ServerPlayer serverPlayer) {
        this.players.replace((Object)serverPlayer, true);
    }

    public void unIgnorePlayer(ServerPlayer serverPlayer) {
        this.players.replace((Object)serverPlayer, false);
    }

    public boolean ignoredOrUnknown(ServerPlayer serverPlayer) {
        return this.players.getOrDefault((Object)serverPlayer, true);
    }

    public boolean ignored(ServerPlayer serverPlayer) {
        return this.players.getBoolean((Object)serverPlayer);
    }
}

