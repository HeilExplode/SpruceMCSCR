/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class JukeboxSongPlayer {
    public static final int PLAY_EVENT_INTERVAL_TICKS = 20;
    private long ticksSinceSongStarted;
    @Nullable
    private Holder<JukeboxSong> song;
    private final BlockPos blockPos;
    private final OnSongChanged onSongChanged;

    public JukeboxSongPlayer(OnSongChanged onSongChanged, BlockPos blockPos) {
        this.onSongChanged = onSongChanged;
        this.blockPos = blockPos;
    }

    public boolean isPlaying() {
        return this.song != null;
    }

    @Nullable
    public JukeboxSong getSong() {
        if (this.song == null) {
            return null;
        }
        return this.song.value();
    }

    public long getTicksSinceSongStarted() {
        return this.ticksSinceSongStarted;
    }

    public void setSongWithoutPlaying(Holder<JukeboxSong> holder, long l) {
        if (holder.value().hasFinished(l)) {
            return;
        }
        this.song = holder;
        this.ticksSinceSongStarted = l;
    }

    public void play(LevelAccessor levelAccessor, Holder<JukeboxSong> holder) {
        this.song = holder;
        this.ticksSinceSongStarted = 0L;
        int n = levelAccessor.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getId(this.song.value());
        levelAccessor.levelEvent(null, 1010, this.blockPos, n);
        this.onSongChanged.notifyChange();
    }

    public void stop(LevelAccessor levelAccessor, @Nullable BlockState blockState) {
        if (this.song == null) {
            return;
        }
        this.song = null;
        this.ticksSinceSongStarted = 0L;
        levelAccessor.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.blockPos, GameEvent.Context.of(blockState));
        levelAccessor.levelEvent(1011, this.blockPos, 0);
        this.onSongChanged.notifyChange();
    }

    public void tick(LevelAccessor levelAccessor, @Nullable BlockState blockState) {
        if (this.song == null) {
            return;
        }
        if (this.song.value().hasFinished(this.ticksSinceSongStarted)) {
            this.stop(levelAccessor, blockState);
            return;
        }
        if (this.shouldEmitJukeboxPlayingEvent()) {
            levelAccessor.gameEvent(GameEvent.JUKEBOX_PLAY, this.blockPos, GameEvent.Context.of(blockState));
            JukeboxSongPlayer.spawnMusicParticles(levelAccessor, this.blockPos);
        }
        ++this.ticksSinceSongStarted;
    }

    private boolean shouldEmitJukeboxPlayingEvent() {
        return this.ticksSinceSongStarted % 20L == 0L;
    }

    private static void spawnMusicParticles(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)levelAccessor;
            Vec3 vec3 = Vec3.atBottomCenterOf(blockPos).add(0.0, 1.2f, 0.0);
            float f = (float)levelAccessor.getRandom().nextInt(4) / 24.0f;
            serverLevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, f, 0.0, 0.0, 1.0);
        }
    }

    @FunctionalInterface
    public static interface OnSongChanged {
        public void notifyChange();
    }
}

