/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2LongMap
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class ChunkMap
extends ChunkStorage
implements ChunkHolder.PlayerProvider,
GeneratingChunkMap {
    private static final ChunkResult<List<ChunkAccess>> UNLOADED_CHUNK_LIST_RESULT = ChunkResult.error("Unloaded chunks found in range");
    private static final CompletableFuture<ChunkResult<List<ChunkAccess>>> UNLOADED_CHUNK_LIST_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK_LIST_RESULT);
    private static final byte CHUNK_TYPE_REPLACEABLE = -1;
    private static final byte CHUNK_TYPE_UNKNOWN = 0;
    private static final byte CHUNK_TYPE_FULL = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHUNK_SAVED_PER_TICK = 200;
    private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
    private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
    public static final int MIN_VIEW_DISTANCE = 2;
    public static final int MAX_VIEW_DISTANCE = 32;
    public static final int FORCED_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap();
    private final List<ChunkGenerationTask> pendingGenerationTasks = new ArrayList<ChunkGenerationTask>();
    final ServerLevel level;
    private final ThreadedLevelLightEngine lightEngine;
    private final BlockableEventLoop<Runnable> mainThreadExecutor;
    private final RandomState randomState;
    private final ChunkGeneratorStructureState chunkGeneratorState;
    private final Supplier<DimensionDataStorage> overworldDataStorage;
    private final PoiManager poiManager;
    final LongSet toDrop = new LongOpenHashSet();
    private boolean modified;
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    private final ChunkProgressListener progressListener;
    private final ChunkStatusUpdateListener chunkStatusListener;
    private final DistanceManager distanceManager;
    private final AtomicInteger tickingGenerated = new AtomicInteger();
    private final String storageName;
    private final PlayerMap playerMap = new PlayerMap();
    private final Int2ObjectMap<TrackedEntity> entityMap = new Int2ObjectOpenHashMap();
    private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkSaveCooldowns = new Long2LongOpenHashMap();
    private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
    private int serverViewDistance;
    private final WorldGenContext worldGenContext;

    public ChunkMap(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, BlockableEventLoop<Runnable> blockableEventLoop, LightChunkGetter lightChunkGetter, ChunkGenerator chunkGenerator, ChunkProgressListener chunkProgressListener, ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier<DimensionDataStorage> supplier, int n, boolean bl) {
        super(new RegionStorageInfo(levelStorageAccess.getLevelId(), serverLevel.dimension(), "chunk"), levelStorageAccess.getDimensionPath(serverLevel.dimension()).resolve("region"), dataFixer, bl);
        Object object;
        Path path = levelStorageAccess.getDimensionPath(serverLevel.dimension());
        this.storageName = path.getFileName().toString();
        this.level = serverLevel;
        RegistryAccess registryAccess = serverLevel.registryAccess();
        long l = serverLevel.getSeed();
        if (chunkGenerator instanceof NoiseBasedChunkGenerator) {
            object = (NoiseBasedChunkGenerator)chunkGenerator;
            this.randomState = RandomState.create(((NoiseBasedChunkGenerator)object).generatorSettings().value(), registryAccess.lookupOrThrow(Registries.NOISE), l);
        } else {
            this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), registryAccess.lookupOrThrow(Registries.NOISE), l);
        }
        this.chunkGeneratorState = chunkGenerator.createState(registryAccess.lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, l);
        this.mainThreadExecutor = blockableEventLoop;
        object = ProcessorMailbox.create(executor, "worldgen");
        ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("main", blockableEventLoop::tell);
        this.progressListener = chunkProgressListener;
        this.chunkStatusListener = chunkStatusUpdateListener;
        ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(executor, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter((List<ProcessorHandle<?>>)ImmutableList.of((Object)object, processorHandle, processorMailbox), executor, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(object, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(processorHandle, false);
        this.lightEngine = new ThreadedLevelLightEngine(lightChunkGetter, this, this.level.dimensionType().hasSkyLight(), processorMailbox, this.queueSorter.getProcessor(processorMailbox, false));
        this.distanceManager = new DistanceManager(executor, blockableEventLoop);
        this.overworldDataStorage = supplier;
        this.poiManager = new PoiManager(new RegionStorageInfo(levelStorageAccess.getLevelId(), serverLevel.dimension(), "poi"), path.resolve("poi"), dataFixer, bl, registryAccess, serverLevel.getServer(), serverLevel);
        this.setServerViewDistance(n);
        this.worldGenContext = new WorldGenContext(serverLevel, chunkGenerator, structureTemplateManager, this.lightEngine, this.mainThreadMailbox);
    }

    protected ChunkGenerator generator() {
        return this.worldGenContext.generator();
    }

    protected ChunkGeneratorStructureState generatorState() {
        return this.chunkGeneratorState;
    }

    protected RandomState randomState() {
        return this.randomState;
    }

    private static double euclideanDistanceSquared(ChunkPos chunkPos, Entity entity) {
        double d = SectionPos.sectionToBlockCoord(chunkPos.x, 8);
        double d2 = SectionPos.sectionToBlockCoord(chunkPos.z, 8);
        double d3 = d - entity.getX();
        double d4 = d2 - entity.getZ();
        return d3 * d3 + d4 * d4;
    }

    boolean isChunkTracked(ServerPlayer serverPlayer, int n, int n2) {
        return serverPlayer.getChunkTrackingView().contains(n, n2) && !serverPlayer.connection.chunkSender.isPending(ChunkPos.asLong(n, n2));
    }

    private boolean isChunkOnTrackedBorder(ServerPlayer serverPlayer, int n, int n2) {
        if (!this.isChunkTracked(serverPlayer, n, n2)) {
            return false;
        }
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0 || this.isChunkTracked(serverPlayer, n + i, n2 + j)) continue;
                return true;
            }
        }
        return false;
    }

    protected ThreadedLevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    protected ChunkHolder getUpdatingChunkIfPresent(long l) {
        return (ChunkHolder)this.updatingChunkMap.get(l);
    }

    @Nullable
    protected ChunkHolder getVisibleChunkIfPresent(long l) {
        return (ChunkHolder)this.visibleChunkMap.get(l);
    }

    protected IntSupplier getChunkQueueLevel(long l) {
        return () -> {
            ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
            if (chunkHolder == null) {
                return ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1;
            }
            return Math.min(chunkHolder.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
        };
    }

    public String getChunkDebugData(ChunkPos chunkPos) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(chunkPos.toLong());
        if (chunkHolder == null) {
            return "null";
        }
        String string = chunkHolder.getTicketLevel() + "\n";
        ChunkStatus chunkStatus = chunkHolder.getLatestStatus();
        ChunkAccess chunkAccess = chunkHolder.getLatestChunk();
        if (chunkStatus != null) {
            string = string + "St: \u00a7" + chunkStatus.getIndex() + String.valueOf(chunkStatus) + "\u00a7r\n";
        }
        if (chunkAccess != null) {
            string = string + "Ch: \u00a7" + chunkAccess.getPersistedStatus().getIndex() + String.valueOf(chunkAccess.getPersistedStatus()) + "\u00a7r\n";
        }
        FullChunkStatus fullChunkStatus = chunkHolder.getFullStatus();
        string = string + String.valueOf('\u00a7') + fullChunkStatus.ordinal() + String.valueOf((Object)fullChunkStatus);
        return string + "\u00a7r";
    }

    private CompletableFuture<ChunkResult<List<ChunkAccess>>> getChunkRangeFuture(ChunkHolder chunkHolder, int n, IntFunction<ChunkStatus> intFunction) {
        if (n == 0) {
            ChunkStatus chunkStatus = intFunction.apply(0);
            return chunkHolder.scheduleChunkGenerationTask(chunkStatus, this).thenApply(chunkResult -> chunkResult.map(List::of));
        }
        ArrayList<CompletableFuture<ChunkResult<ChunkAccess>>> arrayList = new ArrayList<CompletableFuture<ChunkResult<ChunkAccess>>>();
        ChunkPos chunkPos = chunkHolder.getPos();
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                int n2 = Math.max(Math.abs(j), Math.abs(i));
                long l = ChunkPos.asLong(chunkPos.x + j, chunkPos.z + i);
                ChunkHolder chunkHolder2 = this.getUpdatingChunkIfPresent(l);
                if (chunkHolder2 == null) {
                    return UNLOADED_CHUNK_LIST_FUTURE;
                }
                ChunkStatus chunkStatus = intFunction.apply(n2);
                arrayList.add(chunkHolder2.scheduleChunkGenerationTask(chunkStatus, this));
            }
        }
        return Util.sequence(arrayList).thenApply(list -> {
            ArrayList arrayList = Lists.newArrayList();
            for (ChunkResult chunkResult : list) {
                if (chunkResult == null) {
                    throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }
                ChunkAccess chunkAccess = chunkResult.orElse(null);
                if (chunkAccess == null) {
                    return UNLOADED_CHUNK_LIST_RESULT;
                }
                arrayList.add(chunkAccess);
            }
            return ChunkResult.of(arrayList);
        });
    }

    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException illegalStateException, String string) {
        StringBuilder stringBuilder = new StringBuilder();
        Consumer<ChunkHolder> consumer = chunkHolder -> chunkHolder.getAllFutures().forEach(pair -> {
            ChunkStatus chunkStatus = (ChunkStatus)pair.getFirst();
            CompletableFuture completableFuture = (CompletableFuture)pair.getSecond();
            if (completableFuture != null && completableFuture.isDone() && completableFuture.join() == null) {
                stringBuilder.append(chunkHolder.getPos()).append(" - status: ").append(chunkStatus).append(" future: ").append(completableFuture).append(System.lineSeparator());
            }
        });
        stringBuilder.append("Updating:").append(System.lineSeparator());
        this.updatingChunkMap.values().forEach(consumer);
        stringBuilder.append("Visible:").append(System.lineSeparator());
        this.visibleChunkMap.values().forEach(consumer);
        CrashReport crashReport = CrashReport.forThrowable(illegalStateException, "Chunk loading");
        CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk loading");
        crashReportCategory.setDetail("Details", string);
        crashReportCategory.setDetail("Futures", stringBuilder);
        return new ReportedException(crashReport);
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareEntityTickingChunk(ChunkHolder chunkHolder) {
        return this.getChunkRangeFuture(chunkHolder, 2, n -> ChunkStatus.FULL).thenApplyAsync(chunkResult -> chunkResult.map(list -> (LevelChunk)list.get(list.size() / 2)), (Executor)this.mainThreadExecutor);
    }

    @Nullable
    ChunkHolder updateChunkScheduling(long l, int n, @Nullable ChunkHolder chunkHolder, int n2) {
        if (!ChunkLevel.isLoaded(n2) && !ChunkLevel.isLoaded(n)) {
            return chunkHolder;
        }
        if (chunkHolder != null) {
            chunkHolder.setTicketLevel(n);
        }
        if (chunkHolder != null) {
            if (!ChunkLevel.isLoaded(n)) {
                this.toDrop.add(l);
            } else {
                this.toDrop.remove(l);
            }
        }
        if (ChunkLevel.isLoaded(n) && chunkHolder == null) {
            chunkHolder = (ChunkHolder)this.pendingUnloads.remove(l);
            if (chunkHolder != null) {
                chunkHolder.setTicketLevel(n);
            } else {
                chunkHolder = new ChunkHolder(new ChunkPos(l), n, this.level, this.lightEngine, this.queueSorter, this);
            }
            this.updatingChunkMap.put(l, (Object)chunkHolder);
            this.modified = true;
        }
        return chunkHolder;
    }

    @Override
    public void close() throws IOException {
        try {
            this.queueSorter.close();
            this.poiManager.close();
        }
        finally {
            super.close();
        }
    }

    protected void saveAllChunks(boolean bl) {
        if (bl) {
            List<ChunkHolder> list = this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).peek(ChunkHolder::refreshAccessibility).toList();
            MutableBoolean mutableBoolean = new MutableBoolean();
            do {
                mutableBoolean.setFalse();
                list.stream().map(chunkHolder -> {
                    this.mainThreadExecutor.managedBlock(chunkHolder::isReadyForSaving);
                    return chunkHolder.getLatestChunk();
                }).filter(chunkAccess -> chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk).filter(this::save).forEach(chunkAccess -> mutableBoolean.setTrue());
            } while (mutableBoolean.isTrue());
            this.processUnloads(() -> true);
            this.flushWorker();
        } else {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }
    }

    protected void tick(BooleanSupplier booleanSupplier) {
        ProfilerFiller profilerFiller = this.level.getProfiler();
        profilerFiller.push("poi");
        this.poiManager.tick(booleanSupplier);
        profilerFiller.popPush("chunk_unload");
        if (!this.level.noSave()) {
            this.processUnloads(booleanSupplier);
        }
        profilerFiller.pop();
    }

    public boolean hasWork() {
        return this.lightEngine.hasLightWork() || !this.pendingUnloads.isEmpty() || !this.updatingChunkMap.isEmpty() || this.poiManager.hasWork() || !this.toDrop.isEmpty() || !this.unloadQueue.isEmpty() || this.queueSorter.hasWork() || this.distanceManager.hasTickets();
    }

    private void processUnloads(BooleanSupplier booleanSupplier) {
        Runnable runnable;
        LongIterator longIterator = this.toDrop.iterator();
        int n = 0;
        while (longIterator.hasNext() && (booleanSupplier.getAsBoolean() || n < 200 || this.toDrop.size() > 2000)) {
            long l = longIterator.nextLong();
            ChunkHolder chunkHolder = (ChunkHolder)this.updatingChunkMap.get(l);
            if (chunkHolder != null) {
                if (chunkHolder.getGenerationRefCount() != 0) continue;
                this.updatingChunkMap.remove(l);
                this.pendingUnloads.put(l, (Object)chunkHolder);
                this.modified = true;
                ++n;
                this.scheduleUnload(l, chunkHolder);
            }
            longIterator.remove();
        }
        for (int i = Math.max(0, this.unloadQueue.size() - 2000); (booleanSupplier.getAsBoolean() || i > 0) && (runnable = this.unloadQueue.poll()) != null; --i) {
            runnable.run();
        }
        int n2 = 0;
        ObjectIterator objectIterator = this.visibleChunkMap.values().iterator();
        while (n2 < 20 && booleanSupplier.getAsBoolean() && objectIterator.hasNext()) {
            if (!this.saveChunkIfNeeded((ChunkHolder)objectIterator.next())) continue;
            ++n2;
        }
    }

    private void scheduleUnload(long l, ChunkHolder chunkHolder) {
        ((CompletableFuture)chunkHolder.getSaveSyncFuture().thenRunAsync(() -> {
            if (!chunkHolder.isReadyForSaving()) {
                this.scheduleUnload(l, chunkHolder);
                return;
            }
            ChunkAccess chunkAccess = chunkHolder.getLatestChunk();
            if (this.pendingUnloads.remove(l, (Object)chunkHolder) && chunkAccess != null) {
                LevelChunk levelChunk;
                if (chunkAccess instanceof LevelChunk) {
                    levelChunk = (LevelChunk)chunkAccess;
                    levelChunk.setLoaded(false);
                }
                this.save(chunkAccess);
                if (chunkAccess instanceof LevelChunk) {
                    levelChunk = (LevelChunk)chunkAccess;
                    this.level.unload(levelChunk);
                }
                this.lightEngine.updateChunkStatus(chunkAccess.getPos());
                this.lightEngine.tryScheduleUpdate();
                this.progressListener.onStatusChange(chunkAccess.getPos(), null);
                this.chunkSaveCooldowns.remove(chunkAccess.getPos().toLong());
            }
        }, this.unloadQueue::add)).whenComplete((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to save chunk {}", (Object)chunkHolder.getPos(), throwable);
            }
        });
    }

    protected boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        }
        this.visibleChunkMap = this.updatingChunkMap.clone();
        this.modified = false;
        return true;
    }

    private CompletableFuture<ChunkAccess> scheduleChunkLoad(ChunkPos chunkPos) {
        return ((CompletableFuture)((CompletableFuture)this.readChunk(chunkPos).thenApply(optional -> optional.filter(compoundTag -> {
            boolean bl = ChunkMap.isChunkDataValid(compoundTag);
            if (!bl) {
                LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)chunkPos);
            }
            return bl;
        }))).thenApplyAsync(optional -> {
            this.level.getProfiler().incrementCounter("chunkLoad");
            if (optional.isPresent()) {
                ProtoChunk protoChunk = ChunkSerializer.read(this.level, this.poiManager, this.storageInfo(), chunkPos, (CompoundTag)optional.get());
                this.markPosition(chunkPos, ((ChunkAccess)protoChunk).getPersistedStatus().getChunkType());
                return protoChunk;
            }
            return this.createEmptyChunk(chunkPos);
        }, (Executor)this.mainThreadExecutor)).exceptionallyAsync(throwable -> this.handleChunkLoadFailure((Throwable)throwable, chunkPos), (Executor)this.mainThreadExecutor);
    }

    private static boolean isChunkDataValid(CompoundTag compoundTag) {
        return compoundTag.contains("Status", 8);
    }

    private ChunkAccess handleChunkLoadFailure(Throwable throwable, ChunkPos chunkPos) {
        boolean bl;
        Throwable throwable2;
        Throwable throwable3;
        Throwable throwable4;
        if (throwable instanceof CompletionException) {
            throwable4 = (CompletionException)throwable;
            v0 = throwable4.getCause();
        } else {
            v0 = throwable3 = throwable;
        }
        if (throwable3 instanceof ReportedException) {
            ReportedException reportedException = (ReportedException)throwable3;
            throwable2 = reportedException.getCause();
        } else {
            throwable2 = throwable3;
        }
        throwable4 = throwable2;
        boolean bl2 = throwable4 instanceof Error;
        boolean bl3 = bl = throwable4 instanceof IOException || throwable4 instanceof NbtException;
        if (!bl2) {
            if (!bl) {
                // empty if block
            }
        } else {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception loading chunk");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk being loaded");
            crashReportCategory.setDetail("pos", chunkPos);
            this.markPositionReplaceable(chunkPos);
            throw new ReportedException(crashReport);
        }
        this.level.getServer().reportChunkLoadFailure(throwable4, this.storageInfo(), chunkPos);
        return this.createEmptyChunk(chunkPos);
    }

    private ChunkAccess createEmptyChunk(ChunkPos chunkPos) {
        this.markPositionReplaceable(chunkPos);
        return new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registries.BIOME), null);
    }

    private void markPositionReplaceable(ChunkPos chunkPos) {
        this.chunkTypeCache.put(chunkPos.toLong(), (byte)-1);
    }

    private byte markPosition(ChunkPos chunkPos, ChunkType chunkType) {
        return this.chunkTypeCache.put(chunkPos.toLong(), chunkType == ChunkType.PROTOCHUNK ? (byte)-1 : 1);
    }

    @Override
    public GenerationChunkHolder acquireGeneration(long l) {
        ChunkHolder chunkHolder = (ChunkHolder)this.updatingChunkMap.get(l);
        chunkHolder.increaseGenerationRefCount();
        return chunkHolder;
    }

    @Override
    public void releaseGeneration(GenerationChunkHolder generationChunkHolder) {
        generationChunkHolder.decreaseGenerationRefCount();
    }

    @Override
    public CompletableFuture<ChunkAccess> applyStep(GenerationChunkHolder generationChunkHolder, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D) {
        ChunkPos chunkPos = generationChunkHolder.getPos();
        if (chunkStep.targetStatus() == ChunkStatus.EMPTY) {
            return this.scheduleChunkLoad(chunkPos);
        }
        try {
            GenerationChunkHolder generationChunkHolder2 = staticCache2D.get(chunkPos.x, chunkPos.z);
            ChunkAccess chunkAccess = generationChunkHolder2.getChunkIfPresentUnchecked(chunkStep.targetStatus().getParent());
            if (chunkAccess == null) {
                throw new IllegalStateException("Parent chunk missing");
            }
            CompletableFuture<ChunkAccess> completableFuture = chunkStep.apply(this.worldGenContext, staticCache2D, chunkAccess);
            this.progressListener.onStatusChange(chunkPos, chunkStep.targetStatus());
            return completableFuture;
        }
        catch (Exception exception) {
            exception.getStackTrace();
            CrashReport crashReport = CrashReport.forThrowable(exception, "Exception generating new chunk");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk to be generated");
            crashReportCategory.setDetail("Status being generated", () -> chunkStep.targetStatus().getName());
            crashReportCategory.setDetail("Location", String.format(Locale.ROOT, "%d,%d", chunkPos.x, chunkPos.z));
            crashReportCategory.setDetail("Position hash", ChunkPos.asLong(chunkPos.x, chunkPos.z));
            crashReportCategory.setDetail("Generator", this.generator());
            this.mainThreadExecutor.execute(() -> {
                throw new ReportedException(crashReport);
            });
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public ChunkGenerationTask scheduleGenerationTask(ChunkStatus chunkStatus, ChunkPos chunkPos) {
        ChunkGenerationTask chunkGenerationTask = ChunkGenerationTask.create(this, chunkStatus, chunkPos);
        this.pendingGenerationTasks.add(chunkGenerationTask);
        return chunkGenerationTask;
    }

    private void runGenerationTask(ChunkGenerationTask chunkGenerationTask) {
        this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkGenerationTask.getCenter(), () -> {
            CompletableFuture<?> completableFuture = chunkGenerationTask.runUntilWait();
            if (completableFuture == null) {
                return;
            }
            completableFuture.thenRun(() -> this.runGenerationTask(chunkGenerationTask));
        }));
    }

    @Override
    public void runGenerationTasks() {
        this.pendingGenerationTasks.forEach(this::runGenerationTask);
        this.pendingGenerationTasks.clear();
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareTickingChunk(ChunkHolder chunkHolder) {
        CompletableFuture<ChunkResult<List<ChunkAccess>>> completableFuture = this.getChunkRangeFuture(chunkHolder, 1, n -> ChunkStatus.FULL);
        CompletionStage completionStage = ((CompletableFuture)completableFuture.thenApplyAsync(chunkResult -> chunkResult.map(list -> (LevelChunk)list.get(list.size() / 2)), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message((GenerationChunkHolder)chunkHolder, runnable)))).thenApplyAsync(chunkResult -> chunkResult.ifSuccess(levelChunk -> {
            levelChunk.postProcessGeneration();
            this.level.startTickingChunk((LevelChunk)levelChunk);
            CompletableFuture<?> completableFuture = chunkHolder.getSendSyncFuture();
            if (completableFuture.isDone()) {
                this.onChunkReadyToSend((LevelChunk)levelChunk);
            } else {
                completableFuture.thenAcceptAsync(object -> this.onChunkReadyToSend((LevelChunk)levelChunk), (Executor)this.mainThreadExecutor);
            }
        }), (Executor)this.mainThreadExecutor);
        ((CompletableFuture)completionStage).handle((chunkResult, throwable) -> {
            this.tickingGenerated.getAndIncrement();
            return null;
        });
        return completionStage;
    }

    private void onChunkReadyToSend(LevelChunk levelChunk) {
        ChunkPos chunkPos = levelChunk.getPos();
        for (ServerPlayer serverPlayer : this.playerMap.getAllPlayers()) {
            if (!serverPlayer.getChunkTrackingView().contains(chunkPos)) continue;
            ChunkMap.markChunkPendingToSend(serverPlayer, levelChunk);
        }
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareAccessibleChunk(ChunkHolder chunkHolder) {
        return this.getChunkRangeFuture(chunkHolder, 1, ChunkLevel::getStatusAroundFullChunk).thenApplyAsync(chunkResult -> chunkResult.map(list -> (LevelChunk)list.get(list.size() / 2)), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message((GenerationChunkHolder)chunkHolder, runnable)));
    }

    public int getTickingGenerated() {
        return this.tickingGenerated.get();
    }

    private boolean saveChunkIfNeeded(ChunkHolder chunkHolder) {
        if (!chunkHolder.wasAccessibleSinceLastSave() || !chunkHolder.isReadyForSaving()) {
            return false;
        }
        ChunkAccess chunkAccess = chunkHolder.getLatestChunk();
        if (chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk) {
            long l = chunkAccess.getPos().toLong();
            long l2 = this.chunkSaveCooldowns.getOrDefault(l, -1L);
            long l3 = System.currentTimeMillis();
            if (l3 < l2) {
                return false;
            }
            boolean bl = this.save(chunkAccess);
            chunkHolder.refreshAccessibility();
            if (bl) {
                this.chunkSaveCooldowns.put(l, l3 + 10000L);
            }
            return bl;
        }
        return false;
    }

    private boolean save(ChunkAccess chunkAccess) {
        this.poiManager.flush(chunkAccess.getPos());
        if (!chunkAccess.isUnsaved()) {
            return false;
        }
        chunkAccess.setUnsaved(false);
        ChunkPos chunkPos = chunkAccess.getPos();
        try {
            ChunkStatus chunkStatus = chunkAccess.getPersistedStatus();
            if (chunkStatus.getChunkType() != ChunkType.LEVELCHUNK) {
                if (this.isExistingChunkFull(chunkPos)) {
                    return false;
                }
                if (chunkStatus == ChunkStatus.EMPTY && chunkAccess.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                    return false;
                }
            }
            this.level.getProfiler().incrementCounter("chunkSave");
            CompoundTag compoundTag = ChunkSerializer.write(this.level, chunkAccess);
            this.write(chunkPos, compoundTag).exceptionally(throwable -> {
                this.level.getServer().reportChunkSaveFailure((Throwable)throwable, this.storageInfo(), chunkPos);
                return null;
            });
            this.markPosition(chunkPos, chunkStatus.getChunkType());
            return true;
        }
        catch (Exception exception) {
            this.level.getServer().reportChunkSaveFailure(exception, this.storageInfo(), chunkPos);
            return false;
        }
    }

    private boolean isExistingChunkFull(ChunkPos chunkPos) {
        CompoundTag compoundTag;
        byte by = this.chunkTypeCache.get(chunkPos.toLong());
        if (by != 0) {
            return by == 1;
        }
        try {
            compoundTag = this.readChunk(chunkPos).join().orElse(null);
            if (compoundTag == null) {
                this.markPositionReplaceable(chunkPos);
                return false;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Failed to read chunk {}", (Object)chunkPos, (Object)exception);
            this.markPositionReplaceable(chunkPos);
            return false;
        }
        ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
        return this.markPosition(chunkPos, chunkType) == 1;
    }

    protected void setServerViewDistance(int n) {
        int n2 = Mth.clamp(n, 2, 32);
        if (n2 != this.serverViewDistance) {
            this.serverViewDistance = n2;
            this.distanceManager.updatePlayerTickets(this.serverViewDistance);
            for (ServerPlayer serverPlayer : this.playerMap.getAllPlayers()) {
                this.updateChunkTracking(serverPlayer);
            }
        }
    }

    int getPlayerViewDistance(ServerPlayer serverPlayer) {
        return Mth.clamp(serverPlayer.requestedViewDistance(), 2, this.serverViewDistance);
    }

    private void markChunkPendingToSend(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        LevelChunk levelChunk = this.getChunkToSend(chunkPos.toLong());
        if (levelChunk != null) {
            ChunkMap.markChunkPendingToSend(serverPlayer, levelChunk);
        }
    }

    private static void markChunkPendingToSend(ServerPlayer serverPlayer, LevelChunk levelChunk) {
        serverPlayer.connection.chunkSender.markChunkPendingToSend(levelChunk);
    }

    private static void dropChunk(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        serverPlayer.connection.chunkSender.dropChunk(serverPlayer, chunkPos);
    }

    @Nullable
    public LevelChunk getChunkToSend(long l) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (chunkHolder == null) {
            return null;
        }
        return chunkHolder.getChunkToSend();
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    public net.minecraft.server.level.DistanceManager getDistanceManager() {
        return this.distanceManager;
    }

    protected Iterable<ChunkHolder> getChunks() {
        return Iterables.unmodifiableIterable((Iterable)this.visibleChunkMap.values());
    }

    void dumpChunks(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").build(writer);
        TickingTracker tickingTracker = this.distanceManager.tickingTracker();
        for (Long2ObjectMap.Entry entry : this.visibleChunkMap.long2ObjectEntrySet()) {
            long l = entry.getLongKey();
            ChunkPos chunkPos = new ChunkPos(l);
            ChunkHolder chunkHolder = (ChunkHolder)entry.getValue();
            Optional<ChunkAccess> optional = Optional.ofNullable(chunkHolder.getLatestChunk());
            Optional<Object> optional2 = optional.flatMap(chunkAccess -> chunkAccess instanceof LevelChunk ? Optional.of((LevelChunk)chunkAccess) : Optional.empty());
            csvOutput.writeRow(chunkPos.x, chunkPos.z, chunkHolder.getTicketLevel(), optional.isPresent(), optional.map(ChunkAccess::getPersistedStatus).orElse(null), optional2.map(LevelChunk::getFullStatus).orElse(null), ChunkMap.printFuture(chunkHolder.getFullChunkFuture()), ChunkMap.printFuture(chunkHolder.getTickingChunkFuture()), ChunkMap.printFuture(chunkHolder.getEntityTickingChunkFuture()), this.distanceManager.getTicketDebugString(l), this.anyPlayerCloseEnoughForSpawning(chunkPos), optional2.map(levelChunk -> levelChunk.getBlockEntities().size()).orElse(0), tickingTracker.getTicketDebugString(l), tickingTracker.getLevel(l), optional2.map(levelChunk -> levelChunk.getBlockTicks().count()).orElse(0), optional2.map(levelChunk -> levelChunk.getFluidTicks().count()).orElse(0));
        }
    }

    private static String printFuture(CompletableFuture<ChunkResult<LevelChunk>> completableFuture) {
        try {
            ChunkResult chunkResult = completableFuture.getNow(null);
            if (chunkResult != null) {
                return chunkResult.isSuccess() ? "done" : "unloaded";
            }
            return "not completed";
        }
        catch (CompletionException completionException) {
            return "failed " + completionException.getCause().getMessage();
        }
        catch (CancellationException cancellationException) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos chunkPos) {
        return this.read(chunkPos).thenApplyAsync(optional -> optional.map(this::upgradeChunkTag), (Executor)Util.backgroundExecutor());
    }

    private CompoundTag upgradeChunkTag(CompoundTag compoundTag) {
        return this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, compoundTag, this.generator().getTypeNameForDataFixer());
    }

    boolean anyPlayerCloseEnoughForSpawning(ChunkPos chunkPos) {
        if (!this.distanceManager.hasPlayersNearby(chunkPos.toLong())) {
            return false;
        }
        for (ServerPlayer serverPlayer : this.playerMap.getAllPlayers()) {
            if (!this.playerIsCloseEnoughForSpawning(serverPlayer, chunkPos)) continue;
            return true;
        }
        return false;
    }

    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        if (!this.distanceManager.hasPlayersNearby(l)) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayer serverPlayer : this.playerMap.getAllPlayers()) {
            if (!this.playerIsCloseEnoughForSpawning(serverPlayer, chunkPos)) continue;
            builder.add((Object)serverPlayer);
        }
        return builder.build();
    }

    private boolean playerIsCloseEnoughForSpawning(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        if (serverPlayer.isSpectator()) {
            return false;
        }
        double d = ChunkMap.euclideanDistanceSquared(chunkPos, serverPlayer);
        return d < 16384.0;
    }

    private boolean skipPlayer(ServerPlayer serverPlayer) {
        return serverPlayer.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
    }

    void updatePlayerStatus(ServerPlayer serverPlayer, boolean bl) {
        boolean bl2 = this.skipPlayer(serverPlayer);
        boolean bl3 = this.playerMap.ignoredOrUnknown(serverPlayer);
        if (bl) {
            this.playerMap.addPlayer(serverPlayer, bl2);
            this.updatePlayerPos(serverPlayer);
            if (!bl2) {
                this.distanceManager.addPlayer(SectionPos.of(serverPlayer), serverPlayer);
            }
            serverPlayer.setChunkTrackingView(ChunkTrackingView.EMPTY);
            this.updateChunkTracking(serverPlayer);
        } else {
            SectionPos sectionPos = serverPlayer.getLastSectionPos();
            this.playerMap.removePlayer(serverPlayer);
            if (!bl3) {
                this.distanceManager.removePlayer(sectionPos, serverPlayer);
            }
            this.applyChunkTrackingView(serverPlayer, ChunkTrackingView.EMPTY);
        }
    }

    private void updatePlayerPos(ServerPlayer serverPlayer) {
        SectionPos sectionPos = SectionPos.of(serverPlayer);
        serverPlayer.setLastSectionPos(sectionPos);
    }

    public void move(ServerPlayer serverPlayer) {
        boolean bl;
        Object object2;
        for (Object object2 : this.entityMap.values()) {
            if (((TrackedEntity)object2).entity == serverPlayer) {
                ((TrackedEntity)object2).updatePlayers(this.level.players());
                continue;
            }
            ((TrackedEntity)object2).updatePlayer(serverPlayer);
        }
        SectionPos sectionPos = serverPlayer.getLastSectionPos();
        object2 = SectionPos.of(serverPlayer);
        boolean bl2 = this.playerMap.ignored(serverPlayer);
        boolean bl3 = this.skipPlayer(serverPlayer);
        boolean bl4 = bl = sectionPos.asLong() != ((SectionPos)object2).asLong();
        if (bl || bl2 != bl3) {
            this.updatePlayerPos(serverPlayer);
            if (!bl2) {
                this.distanceManager.removePlayer(sectionPos, serverPlayer);
            }
            if (!bl3) {
                this.distanceManager.addPlayer((SectionPos)object2, serverPlayer);
            }
            if (!bl2 && bl3) {
                this.playerMap.ignorePlayer(serverPlayer);
            }
            if (bl2 && !bl3) {
                this.playerMap.unIgnorePlayer(serverPlayer);
            }
            this.updateChunkTracking(serverPlayer);
        }
    }

    private void updateChunkTracking(ServerPlayer serverPlayer) {
        ChunkTrackingView.Positioned positioned;
        ChunkPos chunkPos = serverPlayer.chunkPosition();
        int n = this.getPlayerViewDistance(serverPlayer);
        ChunkTrackingView chunkTrackingView = serverPlayer.getChunkTrackingView();
        if (chunkTrackingView instanceof ChunkTrackingView.Positioned && (positioned = (ChunkTrackingView.Positioned)chunkTrackingView).center().equals(chunkPos) && positioned.viewDistance() == n) {
            return;
        }
        this.applyChunkTrackingView(serverPlayer, ChunkTrackingView.of(chunkPos, n));
    }

    private void applyChunkTrackingView(ServerPlayer serverPlayer, ChunkTrackingView chunkTrackingView) {
        if (serverPlayer.level() != this.level) {
            return;
        }
        ChunkTrackingView chunkTrackingView2 = serverPlayer.getChunkTrackingView();
        if (chunkTrackingView instanceof ChunkTrackingView.Positioned) {
            ChunkTrackingView.Positioned positioned;
            ChunkTrackingView.Positioned positioned2 = (ChunkTrackingView.Positioned)chunkTrackingView;
            if (!(chunkTrackingView2 instanceof ChunkTrackingView.Positioned) || !(positioned = (ChunkTrackingView.Positioned)chunkTrackingView2).center().equals(positioned2.center())) {
                serverPlayer.connection.send(new ClientboundSetChunkCacheCenterPacket(positioned2.center().x, positioned2.center().z));
            }
        }
        ChunkTrackingView.difference(chunkTrackingView2, chunkTrackingView, chunkPos -> this.markChunkPendingToSend(serverPlayer, (ChunkPos)chunkPos), chunkPos -> ChunkMap.dropChunk(serverPlayer, chunkPos));
        serverPlayer.setChunkTrackingView(chunkTrackingView);
    }

    @Override
    public List<ServerPlayer> getPlayers(ChunkPos chunkPos, boolean bl) {
        Set<ServerPlayer> set = this.playerMap.getAllPlayers();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayer serverPlayer : set) {
            if ((!bl || !this.isChunkOnTrackedBorder(serverPlayer, chunkPos.x, chunkPos.z)) && (bl || !this.isChunkTracked(serverPlayer, chunkPos.x, chunkPos.z))) continue;
            builder.add((Object)serverPlayer);
        }
        return builder.build();
    }

    protected void addEntity(Entity entity) {
        if (entity instanceof EnderDragonPart) {
            return;
        }
        EntityType<?> entityType = entity.getType();
        int n = entityType.clientTrackingRange() * 16;
        if (n == 0) {
            return;
        }
        int n2 = entityType.updateInterval();
        if (this.entityMap.containsKey(entity.getId())) {
            throw Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
        }
        TrackedEntity trackedEntity = new TrackedEntity(entity, n, n2, entityType.trackDeltas());
        this.entityMap.put(entity.getId(), (Object)trackedEntity);
        trackedEntity.updatePlayers(this.level.players());
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            this.updatePlayerStatus(serverPlayer, true);
            for (TrackedEntity trackedEntity2 : this.entityMap.values()) {
                if (trackedEntity2.entity == serverPlayer) continue;
                trackedEntity2.updatePlayer(serverPlayer);
            }
        }
    }

    protected void removeEntity(Entity entity) {
        Object object;
        if (entity instanceof ServerPlayer) {
            object = (ServerPlayer)entity;
            this.updatePlayerStatus((ServerPlayer)object, false);
            for (TrackedEntity trackedEntity : this.entityMap.values()) {
                trackedEntity.removePlayer((ServerPlayer)object);
            }
        }
        if ((object = (TrackedEntity)this.entityMap.remove(entity.getId())) != null) {
            ((TrackedEntity)object).broadcastRemoved();
        }
    }

    protected void tick() {
        for (ServerPlayer object2 : this.playerMap.getAllPlayers()) {
            this.updateChunkTracking(object2);
        }
        ArrayList arrayList = Lists.newArrayList();
        List<ServerPlayer> list = this.level.players();
        for (TrackedEntity trackedEntity : this.entityMap.values()) {
            boolean bl;
            SectionPos sectionPos = trackedEntity.lastSectionPos;
            SectionPos sectionPos2 = SectionPos.of(trackedEntity.entity);
            boolean bl2 = bl = !Objects.equals(sectionPos, sectionPos2);
            if (bl) {
                trackedEntity.updatePlayers(list);
                Entity entity = trackedEntity.entity;
                if (entity instanceof ServerPlayer) {
                    arrayList.add((ServerPlayer)entity);
                }
                trackedEntity.lastSectionPos = sectionPos2;
            }
            if (!bl && !this.distanceManager.inEntityTickingRange(sectionPos2.chunk().toLong())) continue;
            trackedEntity.serverEntity.sendChanges();
        }
        if (!arrayList.isEmpty()) {
            for (TrackedEntity trackedEntity : this.entityMap.values()) {
                trackedEntity.updatePlayers(arrayList);
            }
        }
    }

    public void broadcast(Entity entity, Packet<?> packet) {
        TrackedEntity trackedEntity = (TrackedEntity)this.entityMap.get(entity.getId());
        if (trackedEntity != null) {
            trackedEntity.broadcast(packet);
        }
    }

    protected void broadcastAndSend(Entity entity, Packet<?> packet) {
        TrackedEntity trackedEntity = (TrackedEntity)this.entityMap.get(entity.getId());
        if (trackedEntity != null) {
            trackedEntity.broadcastAndSend(packet);
        }
    }

    public void resendBiomesForChunks(List<ChunkAccess> list2) {
        HashMap<ServerPlayer, List> hashMap = new HashMap<ServerPlayer, List>();
        for (ChunkAccess chunkAccess : list2) {
            LevelChunk levelChunk;
            ChunkPos chunkPos = chunkAccess.getPos();
            if (chunkAccess instanceof LevelChunk) {
                LevelChunk levelChunk2 = (LevelChunk)chunkAccess;
                levelChunk = levelChunk2;
            } else {
                levelChunk = this.level.getChunk(chunkPos.x, chunkPos.z);
            }
            for (ServerPlayer serverPlayer2 : this.getPlayers(chunkPos, false)) {
                hashMap.computeIfAbsent(serverPlayer2, serverPlayer -> new ArrayList()).add(levelChunk);
            }
        }
        hashMap.forEach((serverPlayer, list) -> serverPlayer.connection.send(ClientboundChunksBiomesPacket.forChunks(list)));
    }

    protected PoiManager getPoiManager() {
        return this.poiManager;
    }

    public String getStorageName() {
        return this.storageName;
    }

    void onFullChunkStatusChange(ChunkPos chunkPos, FullChunkStatus fullChunkStatus) {
        this.chunkStatusListener.onChunkStatusChange(chunkPos, fullChunkStatus);
    }

    public void waitForLightBeforeSending(ChunkPos chunkPos2, int n) {
        int n2 = n + 1;
        ChunkPos.rangeClosed(chunkPos2, n2).forEach(chunkPos -> {
            ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(chunkPos.toLong());
            if (chunkHolder != null) {
                chunkHolder.addSendDependency(this.lightEngine.waitForPendingTasks(chunkPos.x, chunkPos.z));
            }
        });
    }

    class DistanceManager
    extends net.minecraft.server.level.DistanceManager {
        protected DistanceManager(Executor executor, Executor executor2) {
            super(executor, executor2);
        }

        @Override
        protected boolean isChunkToRemove(long l) {
            return ChunkMap.this.toDrop.contains(l);
        }

        @Override
        @Nullable
        protected ChunkHolder getChunk(long l) {
            return ChunkMap.this.getUpdatingChunkIfPresent(l);
        }

        @Override
        @Nullable
        protected ChunkHolder updateChunkScheduling(long l, int n, @Nullable ChunkHolder chunkHolder, int n2) {
            return ChunkMap.this.updateChunkScheduling(l, n, chunkHolder, n2);
        }
    }

    class TrackedEntity {
        final ServerEntity serverEntity;
        final Entity entity;
        private final int range;
        SectionPos lastSectionPos;
        private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

        public TrackedEntity(Entity entity, int n, int n2, boolean bl) {
            this.serverEntity = new ServerEntity(ChunkMap.this.level, entity, n2, bl, this::broadcast);
            this.entity = entity;
            this.range = n;
            this.lastSectionPos = SectionPos.of(entity);
        }

        public boolean equals(Object object) {
            if (object instanceof TrackedEntity) {
                return ((TrackedEntity)object).entity.getId() == this.entity.getId();
            }
            return false;
        }

        public int hashCode() {
            return this.entity.getId();
        }

        public void broadcast(Packet<?> packet) {
            for (ServerPlayerConnection serverPlayerConnection : this.seenBy) {
                serverPlayerConnection.send(packet);
            }
        }

        public void broadcastAndSend(Packet<?> packet) {
            this.broadcast(packet);
            if (this.entity instanceof ServerPlayer) {
                ((ServerPlayer)this.entity).connection.send(packet);
            }
        }

        public void broadcastRemoved() {
            for (ServerPlayerConnection serverPlayerConnection : this.seenBy) {
                this.serverEntity.removePairing(serverPlayerConnection.getPlayer());
            }
        }

        public void removePlayer(ServerPlayer serverPlayer) {
            if (this.seenBy.remove(serverPlayer.connection)) {
                this.serverEntity.removePairing(serverPlayer);
            }
        }

        public void updatePlayer(ServerPlayer serverPlayer) {
            boolean bl;
            if (serverPlayer == this.entity) {
                return;
            }
            Vec3 vec3 = serverPlayer.position().subtract(this.entity.position());
            int n = ChunkMap.this.getPlayerViewDistance(serverPlayer);
            double d = vec3.x * vec3.x + vec3.z * vec3.z;
            double d2 = Math.min(this.getEffectiveRange(), n * 16);
            double d3 = d2 * d2;
            boolean bl2 = bl = d <= d3 && this.entity.broadcastToPlayer(serverPlayer) && ChunkMap.this.isChunkTracked(serverPlayer, this.entity.chunkPosition().x, this.entity.chunkPosition().z);
            if (bl) {
                if (this.seenBy.add(serverPlayer.connection)) {
                    this.serverEntity.addPairing(serverPlayer);
                }
            } else if (this.seenBy.remove(serverPlayer.connection)) {
                this.serverEntity.removePairing(serverPlayer);
            }
        }

        private int scaledRange(int n) {
            return ChunkMap.this.level.getServer().getScaledTrackingDistance(n);
        }

        private int getEffectiveRange() {
            int n = this.range;
            for (Entity entity : this.entity.getIndirectPassengers()) {
                int n2 = entity.getType().clientTrackingRange() * 16;
                if (n2 <= n) continue;
                n = n2;
            }
            return this.scaledRange(n);
        }

        public void updatePlayers(List<ServerPlayer> list) {
            for (ServerPlayer serverPlayer : list) {
                this.updatePlayer(serverPlayer);
            }
        }
    }
}

