/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2IntMap
 *  it.unimi.dsi.fastutil.longs.Long2IntMaps
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ObjectSet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public abstract class DistanceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    static final int PLAYER_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
    final Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk = new Long2ObjectOpenHashMap();
    final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap();
    private final ChunkTicketTracker ticketTracker = new ChunkTicketTracker();
    private final FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter = new FixedPlayerDistanceChunkTracker(8);
    private final TickingTracker tickingTicketsTracker = new TickingTracker();
    private final PlayerTicketTracker playerTicketManager = new PlayerTicketTracker(32);
    final Set<ChunkHolder> chunksToUpdateFutures = Sets.newHashSet();
    final ChunkTaskPriorityQueueSorter ticketThrottler;
    final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> ticketThrottlerInput;
    final ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> ticketThrottlerReleaser;
    final LongSet ticketsToRelease = new LongOpenHashSet();
    final Executor mainThreadExecutor;
    private long ticketTickCounter;
    private int simulationDistance = 10;

    protected DistanceManager(Executor executor, Executor executor2) {
        ChunkTaskPriorityQueueSorter chunkTaskPriorityQueueSorter;
        ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("player ticket throttler", executor2::execute);
        this.ticketThrottler = chunkTaskPriorityQueueSorter = new ChunkTaskPriorityQueueSorter((List<ProcessorHandle<?>>)ImmutableList.of(processorHandle), executor, 4);
        this.ticketThrottlerInput = chunkTaskPriorityQueueSorter.getProcessor(processorHandle, true);
        this.ticketThrottlerReleaser = chunkTaskPriorityQueueSorter.getReleaseProcessor(processorHandle);
        this.mainThreadExecutor = executor2;
    }

    protected void purgeStaleTickets() {
        ++this.ticketTickCounter;
        ObjectIterator objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            Iterator iterator = ((SortedArraySet)entry.getValue()).iterator();
            boolean bl = false;
            while (iterator.hasNext()) {
                Ticket ticket = (Ticket)iterator.next();
                if (!ticket.timedOut(this.ticketTickCounter)) continue;
                iterator.remove();
                bl = true;
                this.tickingTicketsTracker.removeTicket(entry.getLongKey(), ticket);
            }
            if (bl) {
                this.ticketTracker.update(entry.getLongKey(), DistanceManager.getTicketLevelAt((SortedArraySet)entry.getValue()), false);
            }
            if (!((SortedArraySet)entry.getValue()).isEmpty()) continue;
            objectIterator.remove();
        }
    }

    private static int getTicketLevelAt(SortedArraySet<Ticket<?>> sortedArraySet) {
        return !sortedArraySet.isEmpty() ? sortedArraySet.first().getTicketLevel() : ChunkLevel.MAX_LEVEL + 1;
    }

    protected abstract boolean isChunkToRemove(long var1);

    @Nullable
    protected abstract ChunkHolder getChunk(long var1);

    @Nullable
    protected abstract ChunkHolder updateChunkScheduling(long var1, int var3, @Nullable ChunkHolder var4, int var5);

    public boolean runAllUpdates(ChunkMap chunkMap) {
        boolean bl;
        this.naturalSpawnChunkCounter.runAllUpdates();
        this.tickingTicketsTracker.runAllUpdates();
        this.playerTicketManager.runAllUpdates();
        int n = Integer.MAX_VALUE - this.ticketTracker.runDistanceUpdates(Integer.MAX_VALUE);
        boolean bl2 = bl = n != 0;
        if (bl) {
            // empty if block
        }
        if (!this.chunksToUpdateFutures.isEmpty()) {
            this.chunksToUpdateFutures.forEach(chunkHolder -> chunkHolder.updateHighestAllowedStatus(chunkMap));
            this.chunksToUpdateFutures.forEach(chunkHolder -> chunkHolder.updateFutures(chunkMap, this.mainThreadExecutor));
            this.chunksToUpdateFutures.clear();
            return true;
        }
        if (!this.ticketsToRelease.isEmpty()) {
            LongIterator longIterator = this.ticketsToRelease.iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                if (!this.getTickets(l).stream().anyMatch(ticket -> ticket.getType() == TicketType.PLAYER)) continue;
                ChunkHolder chunkHolder2 = chunkMap.getUpdatingChunkIfPresent(l);
                if (chunkHolder2 == null) {
                    throw new IllegalStateException();
                }
                CompletableFuture<ChunkResult<LevelChunk>> completableFuture = chunkHolder2.getEntityTickingChunkFuture();
                completableFuture.thenAccept(chunkResult -> this.mainThreadExecutor.execute(() -> this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {}, l, false))));
            }
            this.ticketsToRelease.clear();
        }
        return bl;
    }

    void addTicket(long l, Ticket<?> ticket) {
        SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
        int n = DistanceManager.getTicketLevelAt(sortedArraySet);
        Ticket<?> ticket2 = sortedArraySet.addOrGet(ticket);
        ticket2.setCreatedTick(this.ticketTickCounter);
        if (ticket.getTicketLevel() < n) {
            this.ticketTracker.update(l, ticket.getTicketLevel(), true);
        }
    }

    void removeTicket(long l, Ticket<?> ticket) {
        SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
        if (sortedArraySet.remove(ticket)) {
            // empty if block
        }
        if (sortedArraySet.isEmpty()) {
            this.tickets.remove(l);
        }
        this.ticketTracker.update(l, DistanceManager.getTicketLevelAt(sortedArraySet), false);
    }

    public <T> void addTicket(TicketType<T> ticketType, ChunkPos chunkPos, int n, T t) {
        this.addTicket(chunkPos.toLong(), new Ticket<T>(ticketType, n, t));
    }

    public <T> void removeTicket(TicketType<T> ticketType, ChunkPos chunkPos, int n, T t) {
        Ticket<T> ticket = new Ticket<T>(ticketType, n, t);
        this.removeTicket(chunkPos.toLong(), ticket);
    }

    public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int n, T t) {
        Ticket<T> ticket = new Ticket<T>(ticketType, ChunkLevel.byStatus(FullChunkStatus.FULL) - n, t);
        long l = chunkPos.toLong();
        this.addTicket(l, ticket);
        this.tickingTicketsTracker.addTicket(l, ticket);
    }

    public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int n, T t) {
        Ticket<T> ticket = new Ticket<T>(ticketType, ChunkLevel.byStatus(FullChunkStatus.FULL) - n, t);
        long l = chunkPos.toLong();
        this.removeTicket(l, ticket);
        this.tickingTicketsTracker.removeTicket(l, ticket);
    }

    private SortedArraySet<Ticket<?>> getTickets(long l2) {
        return (SortedArraySet)this.tickets.computeIfAbsent(l2, l -> SortedArraySet.create(4));
    }

    protected void updateChunkForced(ChunkPos chunkPos, boolean bl) {
        Ticket<ChunkPos> ticket = new Ticket<ChunkPos>(TicketType.FORCED, ChunkMap.FORCED_TICKET_LEVEL, chunkPos);
        long l = chunkPos.toLong();
        if (bl) {
            this.addTicket(l, ticket);
            this.tickingTicketsTracker.addTicket(l, ticket);
        } else {
            this.removeTicket(l, ticket);
            this.tickingTicketsTracker.removeTicket(l, ticket);
        }
    }

    public void addPlayer(SectionPos sectionPos, ServerPlayer serverPlayer) {
        ChunkPos chunkPos = sectionPos.chunk();
        long l2 = chunkPos.toLong();
        ((ObjectSet)this.playersPerChunk.computeIfAbsent(l2, l -> new ObjectOpenHashSet())).add((Object)serverPlayer);
        this.naturalSpawnChunkCounter.update(l2, 0, true);
        this.playerTicketManager.update(l2, 0, true);
        this.tickingTicketsTracker.addTicket(TicketType.PLAYER, chunkPos, this.getPlayerTicketLevel(), chunkPos);
    }

    public void removePlayer(SectionPos sectionPos, ServerPlayer serverPlayer) {
        ChunkPos chunkPos = sectionPos.chunk();
        long l = chunkPos.toLong();
        ObjectSet objectSet = (ObjectSet)this.playersPerChunk.get(l);
        objectSet.remove((Object)serverPlayer);
        if (objectSet.isEmpty()) {
            this.playersPerChunk.remove(l);
            this.naturalSpawnChunkCounter.update(l, Integer.MAX_VALUE, false);
            this.playerTicketManager.update(l, Integer.MAX_VALUE, false);
            this.tickingTicketsTracker.removeTicket(TicketType.PLAYER, chunkPos, this.getPlayerTicketLevel(), chunkPos);
        }
    }

    private int getPlayerTicketLevel() {
        return Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - this.simulationDistance);
    }

    public boolean inEntityTickingRange(long l) {
        return ChunkLevel.isEntityTicking(this.tickingTicketsTracker.getLevel(l));
    }

    public boolean inBlockTickingRange(long l) {
        return ChunkLevel.isBlockTicking(this.tickingTicketsTracker.getLevel(l));
    }

    protected String getTicketDebugString(long l) {
        SortedArraySet sortedArraySet = (SortedArraySet)this.tickets.get(l);
        if (sortedArraySet == null || sortedArraySet.isEmpty()) {
            return "no_ticket";
        }
        return ((Ticket)sortedArraySet.first()).toString();
    }

    protected void updatePlayerTickets(int n) {
        this.playerTicketManager.updateViewDistance(n);
    }

    public void updateSimulationDistance(int n) {
        if (n != this.simulationDistance) {
            this.simulationDistance = n;
            this.tickingTicketsTracker.replacePlayerTicketsLevel(this.getPlayerTicketLevel());
        }
    }

    public int getNaturalSpawnChunkCount() {
        this.naturalSpawnChunkCounter.runAllUpdates();
        return this.naturalSpawnChunkCounter.chunks.size();
    }

    public boolean hasPlayersNearby(long l) {
        this.naturalSpawnChunkCounter.runAllUpdates();
        return this.naturalSpawnChunkCounter.chunks.containsKey(l);
    }

    public String getDebugStatus() {
        return this.ticketThrottler.getDebugStatus();
    }

    private void dumpTickets(String string) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(string));){
            for (Long2ObjectMap.Entry entry : this.tickets.long2ObjectEntrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
                for (Ticket ticket : (SortedArraySet)entry.getValue()) {
                    fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + String.valueOf(ticket.getType()) + "\t" + ticket.getTicketLevel() + "\t\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to dump tickets to {}", (Object)string, (Object)iOException);
        }
    }

    @VisibleForTesting
    TickingTracker tickingTracker() {
        return this.tickingTicketsTracker;
    }

    public void removeTicketsOnClosing() {
        ImmutableSet immutableSet = ImmutableSet.of(TicketType.UNKNOWN, TicketType.POST_TELEPORT);
        ObjectIterator objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            Iterator iterator = ((SortedArraySet)entry.getValue()).iterator();
            boolean bl = false;
            while (iterator.hasNext()) {
                Ticket ticket = (Ticket)iterator.next();
                if (immutableSet.contains(ticket.getType())) continue;
                iterator.remove();
                bl = true;
                this.tickingTicketsTracker.removeTicket(entry.getLongKey(), ticket);
            }
            if (bl) {
                this.ticketTracker.update(entry.getLongKey(), DistanceManager.getTicketLevelAt((SortedArraySet)entry.getValue()), false);
            }
            if (!((SortedArraySet)entry.getValue()).isEmpty()) continue;
            objectIterator.remove();
        }
    }

    public boolean hasTickets() {
        return !this.tickets.isEmpty();
    }

    class ChunkTicketTracker
    extends ChunkTracker {
        private static final int MAX_LEVEL = ChunkLevel.MAX_LEVEL + 1;

        public ChunkTicketTracker() {
            super(MAX_LEVEL + 1, 16, 256);
        }

        @Override
        protected int getLevelFromSource(long l) {
            SortedArraySet sortedArraySet = (SortedArraySet)DistanceManager.this.tickets.get(l);
            if (sortedArraySet == null) {
                return Integer.MAX_VALUE;
            }
            if (sortedArraySet.isEmpty()) {
                return Integer.MAX_VALUE;
            }
            return ((Ticket)sortedArraySet.first()).getTicketLevel();
        }

        @Override
        protected int getLevel(long l) {
            ChunkHolder chunkHolder;
            if (!DistanceManager.this.isChunkToRemove(l) && (chunkHolder = DistanceManager.this.getChunk(l)) != null) {
                return chunkHolder.getTicketLevel();
            }
            return MAX_LEVEL;
        }

        @Override
        protected void setLevel(long l, int n) {
            int n2;
            ChunkHolder chunkHolder = DistanceManager.this.getChunk(l);
            int n3 = n2 = chunkHolder == null ? MAX_LEVEL : chunkHolder.getTicketLevel();
            if (n2 == n) {
                return;
            }
            if ((chunkHolder = DistanceManager.this.updateChunkScheduling(l, n, chunkHolder, n2)) != null) {
                DistanceManager.this.chunksToUpdateFutures.add(chunkHolder);
            }
        }

        public int runDistanceUpdates(int n) {
            return this.runUpdates(n);
        }
    }

    class FixedPlayerDistanceChunkTracker
    extends ChunkTracker {
        protected final Long2ByteMap chunks;
        protected final int maxDistance;

        protected FixedPlayerDistanceChunkTracker(int n) {
            super(n + 2, 16, 256);
            this.chunks = new Long2ByteOpenHashMap();
            this.maxDistance = n;
            this.chunks.defaultReturnValue((byte)(n + 2));
        }

        @Override
        protected int getLevel(long l) {
            return this.chunks.get(l);
        }

        @Override
        protected void setLevel(long l, int n) {
            byte by = n > this.maxDistance ? this.chunks.remove(l) : this.chunks.put(l, (byte)n);
            this.onLevelChange(l, by, n);
        }

        protected void onLevelChange(long l, int n, int n2) {
        }

        @Override
        protected int getLevelFromSource(long l) {
            return this.havePlayer(l) ? 0 : Integer.MAX_VALUE;
        }

        private boolean havePlayer(long l) {
            ObjectSet objectSet = (ObjectSet)DistanceManager.this.playersPerChunk.get(l);
            return objectSet != null && !objectSet.isEmpty();
        }

        public void runAllUpdates() {
            this.runUpdates(Integer.MAX_VALUE);
        }

        private void dumpChunks(String string) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(new File(string));){
                for (Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
                    ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
                    String string2 = Byte.toString(entry.getByteValue());
                    fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + string2 + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to dump chunks to {}", (Object)string, (Object)iOException);
            }
        }
    }

    class PlayerTicketTracker
    extends FixedPlayerDistanceChunkTracker {
        private int viewDistance;
        private final Long2IntMap queueLevels;
        private final LongSet toUpdate;

        protected PlayerTicketTracker(int n) {
            super(n);
            this.queueLevels = Long2IntMaps.synchronize((Long2IntMap)new Long2IntOpenHashMap());
            this.toUpdate = new LongOpenHashSet();
            this.viewDistance = 0;
            this.queueLevels.defaultReturnValue(n + 2);
        }

        @Override
        protected void onLevelChange(long l, int n, int n2) {
            this.toUpdate.add(l);
        }

        public void updateViewDistance(int n) {
            for (Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
                byte by = entry.getByteValue();
                long l = entry.getLongKey();
                this.onLevelChange(l, by, this.haveTicketFor(by), by <= n);
            }
            this.viewDistance = n;
        }

        private void onLevelChange(long l, int n, boolean bl, boolean bl2) {
            if (bl != bl2) {
                Ticket<ChunkPos> ticket = new Ticket<ChunkPos>(TicketType.PLAYER, PLAYER_TICKET_LEVEL, new ChunkPos(l));
                if (bl2) {
                    DistanceManager.this.ticketThrottlerInput.tell(ChunkTaskPriorityQueueSorter.message(() -> DistanceManager.this.mainThreadExecutor.execute(() -> {
                        if (this.haveTicketFor(this.getLevel(l))) {
                            DistanceManager.this.addTicket(l, ticket);
                            DistanceManager.this.ticketsToRelease.add(l);
                        } else {
                            DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {}, l, false));
                        }
                    }), l, () -> n));
                } else {
                    DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> DistanceManager.this.mainThreadExecutor.execute(() -> DistanceManager.this.removeTicket(l, ticket)), l, true));
                }
            }
        }

        @Override
        public void runAllUpdates() {
            super.runAllUpdates();
            if (!this.toUpdate.isEmpty()) {
                LongIterator longIterator = this.toUpdate.iterator();
                while (longIterator.hasNext()) {
                    int n2;
                    long l = longIterator.nextLong();
                    int n3 = this.queueLevels.get(l);
                    if (n3 == (n2 = this.getLevel(l))) continue;
                    DistanceManager.this.ticketThrottler.onLevelChange(new ChunkPos(l), () -> this.queueLevels.get(l), n2, n -> {
                        if (n >= this.queueLevels.defaultReturnValue()) {
                            this.queueLevels.remove(l);
                        } else {
                            this.queueLevels.put(l, n);
                        }
                    });
                    this.onLevelChange(l, n2, this.haveTicketFor(n3), this.haveTicketFor(n2));
                }
                this.toUpdate.clear();
            }
        }

        private boolean haveTicketFor(int n) {
            return n <= this.viewDistance;
        }
    }
}

