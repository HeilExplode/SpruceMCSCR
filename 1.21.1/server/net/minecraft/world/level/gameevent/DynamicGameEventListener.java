/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;

public class DynamicGameEventListener<T extends GameEventListener> {
    private final T listener;
    @Nullable
    private SectionPos lastSection;

    public DynamicGameEventListener(T t) {
        this.listener = t;
    }

    public void add(ServerLevel serverLevel) {
        this.move(serverLevel);
    }

    public T getListener() {
        return this.listener;
    }

    public void remove(ServerLevel serverLevel) {
        DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister((GameEventListener)this.listener));
    }

    public void move(ServerLevel serverLevel) {
        this.listener.getListenerSource().getPosition(serverLevel).map(SectionPos::of).ifPresent(sectionPos -> {
            if (this.lastSection == null || !this.lastSection.equals(sectionPos)) {
                DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister((GameEventListener)this.listener));
                this.lastSection = sectionPos;
                DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.register((GameEventListener)this.listener));
            }
        });
    }

    private static void ifChunkExists(LevelReader levelReader, @Nullable SectionPos sectionPos, Consumer<GameEventListenerRegistry> consumer) {
        if (sectionPos == null) {
            return;
        }
        ChunkAccess chunkAccess = levelReader.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.FULL, false);
        if (chunkAccess != null) {
            consumer.accept(chunkAccess.getListenerRegistry(sectionPos.y()));
        }
    }
}

