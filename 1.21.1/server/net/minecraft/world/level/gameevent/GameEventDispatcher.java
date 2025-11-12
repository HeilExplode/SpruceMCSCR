/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.gameevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.phys.Vec3;

public class GameEventDispatcher {
    private final ServerLevel level;

    public GameEventDispatcher(ServerLevel serverLevel) {
        this.level = serverLevel;
    }

    public void post(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
        int n = holder.value().notificationRadius();
        BlockPos blockPos = BlockPos.containing(vec3);
        int n2 = SectionPos.blockToSectionCoord(blockPos.getX() - n);
        int n3 = SectionPos.blockToSectionCoord(blockPos.getY() - n);
        int n4 = SectionPos.blockToSectionCoord(blockPos.getZ() - n);
        int n5 = SectionPos.blockToSectionCoord(blockPos.getX() + n);
        int n6 = SectionPos.blockToSectionCoord(blockPos.getY() + n);
        int n7 = SectionPos.blockToSectionCoord(blockPos.getZ() + n);
        ArrayList<GameEvent.ListenerInfo> arrayList = new ArrayList<GameEvent.ListenerInfo>();
        GameEventListenerRegistry.ListenerVisitor listenerVisitor = (gameEventListener, vec32) -> {
            if (gameEventListener.getDeliveryMode() == GameEventListener.DeliveryMode.BY_DISTANCE) {
                arrayList.add(new GameEvent.ListenerInfo(holder, vec3, context, gameEventListener, vec32));
            } else {
                gameEventListener.handleGameEvent(this.level, holder, context, vec3);
            }
        };
        boolean bl = false;
        for (int i = n2; i <= n5; ++i) {
            for (int j = n4; j <= n7; ++j) {
                LevelChunk levelChunk = this.level.getChunkSource().getChunkNow(i, j);
                if (levelChunk == null) continue;
                for (int k = n3; k <= n6; ++k) {
                    bl |= ((ChunkAccess)levelChunk).getListenerRegistry(k).visitInRangeListeners(holder, vec3, context, listenerVisitor);
                }
            }
        }
        if (!arrayList.isEmpty()) {
            this.handleGameEventMessagesInQueue(arrayList);
        }
        if (bl) {
            DebugPackets.sendGameEventInfo(this.level, holder, vec3);
        }
    }

    private void handleGameEventMessagesInQueue(List<GameEvent.ListenerInfo> list) {
        Collections.sort(list);
        for (GameEvent.ListenerInfo listenerInfo : list) {
            GameEventListener gameEventListener = listenerInfo.recipient();
            gameEventListener.handleGameEvent(this.level, listenerInfo.gameEvent(), listenerInfo.context(), listenerInfo.source());
        }
    }
}

