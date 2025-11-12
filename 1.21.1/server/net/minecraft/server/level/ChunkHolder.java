/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.shorts.ShortOpenHashSet
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ChunkHolder
extends GenerationChunkHolder {
    public static final ChunkResult<LevelChunk> UNLOADED_LEVEL_CHUNK = ChunkResult.error("Unloaded level chunk");
    private static final CompletableFuture<ChunkResult<LevelChunk>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_LEVEL_CHUNK);
    private final LevelHeightAccessor levelHeightAccessor;
    private volatile CompletableFuture<ChunkResult<LevelChunk>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<ChunkResult<LevelChunk>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<ChunkResult<LevelChunk>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    private boolean hasChangedSections;
    private final ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter = new BitSet();
    private final BitSet skyChangedLightSectionFilter = new BitSet();
    private final LevelLightEngine lightEngine;
    private final LevelChangeListener onLevelChange;
    private final PlayerProvider playerProvider;
    private boolean wasAccessibleSinceLastSave;
    private CompletableFuture<?> pendingFullStateConfirmation = CompletableFuture.completedFuture(null);
    private CompletableFuture<?> sendSync = CompletableFuture.completedFuture(null);
    private CompletableFuture<?> saveSync = CompletableFuture.completedFuture(null);

    public ChunkHolder(ChunkPos chunkPos, int n, LevelHeightAccessor levelHeightAccessor, LevelLightEngine levelLightEngine, LevelChangeListener levelChangeListener, PlayerProvider playerProvider) {
        super(chunkPos);
        this.levelHeightAccessor = levelHeightAccessor;
        this.lightEngine = levelLightEngine;
        this.onLevelChange = levelChangeListener;
        this.playerProvider = playerProvider;
        this.ticketLevel = this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
        this.queueLevel = this.oldTicketLevel;
        this.setTicketLevel(n);
        this.changedBlocksPerSection = new ShortSet[levelHeightAccessor.getSectionsCount()];
    }

    public CompletableFuture<ChunkResult<LevelChunk>> getTickingChunkFuture() {
        return this.tickingChunkFuture;
    }

    public CompletableFuture<ChunkResult<LevelChunk>> getEntityTickingChunkFuture() {
        return this.entityTickingChunkFuture;
    }

    public CompletableFuture<ChunkResult<LevelChunk>> getFullChunkFuture() {
        return this.fullChunkFuture;
    }

    @Nullable
    public LevelChunk getTickingChunk() {
        return this.getTickingChunkFuture().getNow(UNLOADED_LEVEL_CHUNK).orElse(null);
    }

    @Nullable
    public LevelChunk getChunkToSend() {
        if (!this.sendSync.isDone()) {
            return null;
        }
        return this.getTickingChunk();
    }

    public CompletableFuture<?> getSendSyncFuture() {
        return this.sendSync;
    }

    public void addSendDependency(CompletableFuture<?> completableFuture) {
        this.sendSync = this.sendSync.isDone() ? completableFuture : this.sendSync.thenCombine(completableFuture, (object, object2) -> null);
    }

    public CompletableFuture<?> getSaveSyncFuture() {
        return this.saveSync;
    }

    public boolean isReadyForSaving() {
        return this.getGenerationRefCount() == 0 && this.saveSync.isDone();
    }

    private void addSaveDependency(CompletableFuture<?> completableFuture) {
        this.saveSync = this.saveSync.isDone() ? completableFuture : this.saveSync.thenCombine(completableFuture, (object, object2) -> null);
    }

    public void blockChanged(BlockPos blockPos) {
        LevelChunk levelChunk = this.getTickingChunk();
        if (levelChunk == null) {
            return;
        }
        int n = this.levelHeightAccessor.getSectionIndex(blockPos.getY());
        if (this.changedBlocksPerSection[n] == null) {
            this.hasChangedSections = true;
            this.changedBlocksPerSection[n] = new ShortOpenHashSet();
        }
        this.changedBlocksPerSection[n].add(SectionPos.sectionRelativePos(blockPos));
    }

    public void sectionLightChanged(LightLayer lightLayer, int n) {
        ChunkAccess chunkAccess = this.getChunkIfPresent(ChunkStatus.INITIALIZE_LIGHT);
        if (chunkAccess == null) {
            return;
        }
        chunkAccess.setUnsaved(true);
        LevelChunk levelChunk = this.getTickingChunk();
        if (levelChunk == null) {
            return;
        }
        int n2 = this.lightEngine.getMinLightSection();
        int n3 = this.lightEngine.getMaxLightSection();
        if (n < n2 || n > n3) {
            return;
        }
        int n4 = n - n2;
        if (lightLayer == LightLayer.SKY) {
            this.skyChangedLightSectionFilter.set(n4);
        } else {
            this.blockChangedLightSectionFilter.set(n4);
        }
    }

    public void broadcastChanges(LevelChunk levelChunk) {
        List<ServerPlayer> list;
        if (!this.hasChangedSections && this.skyChangedLightSectionFilter.isEmpty() && this.blockChangedLightSectionFilter.isEmpty()) {
            return;
        }
        Level level = levelChunk.getLevel();
        if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            list = this.playerProvider.getPlayers(this.pos, true);
            if (!list.isEmpty()) {
                ClientboundLightUpdatePacket clientboundLightUpdatePacket = new ClientboundLightUpdatePacket(levelChunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter);
                this.broadcast(list, clientboundLightUpdatePacket);
            }
            this.skyChangedLightSectionFilter.clear();
            this.blockChangedLightSectionFilter.clear();
        }
        if (!this.hasChangedSections) {
            return;
        }
        list = this.playerProvider.getPlayers(this.pos, false);
        for (int i = 0; i < this.changedBlocksPerSection.length; ++i) {
            Object object;
            Object object2;
            ShortSet shortSet = this.changedBlocksPerSection[i];
            if (shortSet == null) continue;
            this.changedBlocksPerSection[i] = null;
            if (list.isEmpty()) continue;
            int n = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
            SectionPos sectionPos = SectionPos.of(levelChunk.getPos(), n);
            if (shortSet.size() == 1) {
                object2 = sectionPos.relativeToBlockPos(shortSet.iterator().nextShort());
                object = level.getBlockState((BlockPos)object2);
                this.broadcast(list, new ClientboundBlockUpdatePacket((BlockPos)object2, (BlockState)object));
                this.broadcastBlockEntityIfNeeded(list, level, (BlockPos)object2, (BlockState)object);
                continue;
            }
            object2 = levelChunk.getSection(i);
            object = new ClientboundSectionBlocksUpdatePacket(sectionPos, shortSet, (LevelChunkSection)object2);
            this.broadcast(list, (Packet<?>)object);
            ((ClientboundSectionBlocksUpdatePacket)object).runUpdates((blockPos, blockState) -> this.broadcastBlockEntityIfNeeded(list, level, (BlockPos)blockPos, (BlockState)blockState));
        }
        this.hasChangedSections = false;
    }

    private void broadcastBlockEntityIfNeeded(List<ServerPlayer> list, Level level, BlockPos blockPos, BlockState blockState) {
        if (blockState.hasBlockEntity()) {
            this.broadcastBlockEntity(list, level, blockPos);
        }
    }

    private void broadcastBlockEntity(List<ServerPlayer> list, Level level, BlockPos blockPos) {
        Packet<ClientGamePacketListener> packet;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null && (packet = blockEntity.getUpdatePacket()) != null) {
            this.broadcast(list, packet);
        }
    }

    private void broadcast(List<ServerPlayer> list, Packet<?> packet) {
        list.forEach(serverPlayer -> serverPlayer.connection.send(packet));
    }

    @Override
    public int getTicketLevel() {
        return this.ticketLevel;
    }

    @Override
    public int getQueueLevel() {
        return this.queueLevel;
    }

    private void setQueueLevel(int n) {
        this.queueLevel = n;
    }

    public void setTicketLevel(int n) {
        this.ticketLevel = n;
    }

    private void scheduleFullChunkPromotion(ChunkMap chunkMap, CompletableFuture<ChunkResult<LevelChunk>> completableFuture, Executor executor, FullChunkStatus fullChunkStatus) {
        this.pendingFullStateConfirmation.cancel(false);
        CompletableFuture completableFuture2 = new CompletableFuture();
        completableFuture2.thenRunAsync(() -> chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus), executor);
        this.pendingFullStateConfirmation = completableFuture2;
        completableFuture.thenAccept(chunkResult -> chunkResult.ifSuccess(levelChunk -> completableFuture2.complete(null)));
    }

    private void demoteFullChunk(ChunkMap chunkMap, FullChunkStatus fullChunkStatus) {
        this.pendingFullStateConfirmation.cancel(false);
        chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus);
    }

    protected void updateFutures(ChunkMap chunkMap, Executor executor) {
        FullChunkStatus fullChunkStatus = ChunkLevel.fullStatus(this.oldTicketLevel);
        FullChunkStatus fullChunkStatus2 = ChunkLevel.fullStatus(this.ticketLevel);
        boolean bl = fullChunkStatus.isOrAfter(FullChunkStatus.FULL);
        boolean bl2 = fullChunkStatus2.isOrAfter(FullChunkStatus.FULL);
        this.wasAccessibleSinceLastSave |= bl2;
        if (!bl && bl2) {
            this.fullChunkFuture = chunkMap.prepareAccessibleChunk(this);
            this.scheduleFullChunkPromotion(chunkMap, this.fullChunkFuture, executor, FullChunkStatus.FULL);
            this.addSaveDependency(this.fullChunkFuture);
        }
        if (bl && !bl2) {
            this.fullChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        boolean bl3 = fullChunkStatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        boolean bl4 = fullChunkStatus2.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        if (!bl3 && bl4) {
            this.tickingChunkFuture = chunkMap.prepareTickingChunk(this);
            this.scheduleFullChunkPromotion(chunkMap, this.tickingChunkFuture, executor, FullChunkStatus.BLOCK_TICKING);
            this.addSaveDependency(this.tickingChunkFuture);
        }
        if (bl3 && !bl4) {
            this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        boolean bl5 = fullChunkStatus.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        boolean bl6 = fullChunkStatus2.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        if (!bl5 && bl6) {
            if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw Util.pauseInIde(new IllegalStateException());
            }
            this.entityTickingChunkFuture = chunkMap.prepareEntityTickingChunk(this);
            this.scheduleFullChunkPromotion(chunkMap, this.entityTickingChunkFuture, executor, FullChunkStatus.ENTITY_TICKING);
            this.addSaveDependency(this.entityTickingChunkFuture);
        }
        if (bl5 && !bl6) {
            this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        if (!fullChunkStatus2.isOrAfter(fullChunkStatus)) {
            this.demoteFullChunk(chunkMap, fullChunkStatus2);
        }
        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public boolean wasAccessibleSinceLastSave() {
        return this.wasAccessibleSinceLastSave;
    }

    public void refreshAccessibility() {
        this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
    }

    @FunctionalInterface
    public static interface LevelChangeListener {
        public void onLevelChange(ChunkPos var1, IntSupplier var2, int var3, IntConsumer var4);
    }

    public static interface PlayerProvider {
        public List<ServerPlayer> getPlayers(ChunkPos var1, boolean var2);
    }
}

