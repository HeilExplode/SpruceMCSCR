/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.base.Splitter
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.GameProfileRepository
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.runtime.ObjectMethods;
import java.net.Proxy;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.ServerInfo;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.Services;
import net.minecraft.server.TickTask;
import net.minecraft.server.WorldStem;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class MinecraftServer
extends ReentrantBlockableEventLoop<TickTask>
implements ServerInfo,
ChunkIOErrorReporter,
CommandSource,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA_BRAND = "vanilla";
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8f;
    private static final int TICK_STATS_SPAN = 100;
    private static final long OVERLOADED_THRESHOLD_NANOS = 20L * TimeUtil.NANOSECONDS_PER_SECOND / 20L;
    private static final int OVERLOADED_TICKS_THRESHOLD = 20;
    private static final long OVERLOADED_WARNING_INTERVAL_NANOS = 10L * TimeUtil.NANOSECONDS_PER_SECOND;
    private static final int OVERLOADED_TICKS_WARNING_INTERVAL = 100;
    private static final long STATUS_EXPIRE_TIME_NANOS = 5L * TimeUtil.NANOSECONDS_PER_SECOND;
    private static final long PREPARE_LEVELS_DEFAULT_DELAY_NANOS = 10L * TimeUtil.NANOSECONDS_PER_MILLISECOND;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    private static final int SPAWN_POSITION_SEARCH_RADIUS = 5;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MIMINUM_AUTOSAVE_TICKS = 100;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT);
    public static final GameProfile ANONYMOUS_PLAYER_PROFILE = new GameProfile(Util.NIL_UUID, "Anonymous Player");
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final List<Runnable> tickables = Lists.newArrayList();
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private ProfilerFiller profiler = this.metricsRecorder.getProfiler();
    private Consumer<ProfileResults> onMetricsRecordingStopped = profileResults -> this.stopRecordingMetrics();
    private Consumer<Path> onMetricsRecordingFinished = path -> {};
    private boolean willStartRecordingMetrics;
    @Nullable
    private TimeProfiler debugCommandProfiler;
    private boolean debugCommandProfilerDelayStart;
    private final ServerConnectionListener connection;
    private final ChunkProgressListenerFactory progressListenerFactory;
    @Nullable
    private ServerStatus status;
    @Nullable
    private ServerStatus.Favicon statusIcon;
    private final RandomSource random = RandomSource.create();
    private final DataFixer fixerUpper;
    private String localIp;
    private int port = -1;
    private final LayeredRegistryAccess<RegistryLayer> registries;
    private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
    private PlayerList playerList;
    private volatile boolean running = true;
    private boolean stopped;
    private int tickCount;
    private int ticksUntilAutosave = 6000;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvp;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    private final long[] tickTimesNanos = new long[100];
    private long aggregatedTickTimesNanos = 0L;
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private GameProfile singleplayerProfile;
    private boolean isDemo;
    private volatile boolean isReady;
    private long lastOverloadWarningNanos;
    protected final Services services;
    private long lastServerStatus;
    private final Thread serverThread;
    private long lastTickNanos = Util.getNanos();
    private long taskExecutionStartNanos = Util.getNanos();
    private long idleTimeNanos;
    private long nextTickTimeNanos = Util.getNanos();
    private long delayedTasksMaxNextTickTimeNanos;
    private boolean mayHaveDelayedTasks;
    private final PackRepository packRepository;
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents();
    private final ServerFunctionManager functionManager;
    private boolean enforceWhitelist;
    private float smoothedTickTimeMillis;
    private final Executor executor;
    @Nullable
    private String serverId;
    private ReloadableResources resources;
    private final StructureTemplateManager structureTemplateManager;
    private final ServerTickRateManager tickRateManager;
    protected final WorldData worldData;
    private final PotionBrewing potionBrewing;
    private volatile boolean isSaving;
    private static final AtomicReference<RuntimeException> fatalException = new AtomicReference();

    public static <S extends MinecraftServer> S spin(Function<Thread, S> function) {
        AtomicReference<MinecraftServer> atomicReference = new AtomicReference<MinecraftServer>();
        Thread thread2 = new Thread(() -> ((MinecraftServer)atomicReference.get()).runServer(), "Server thread");
        thread2.setUncaughtExceptionHandler((thread, throwable) -> LOGGER.error("Uncaught exception in server thread", throwable));
        if (Runtime.getRuntime().availableProcessors() > 4) {
            thread2.setPriority(8);
        }
        MinecraftServer minecraftServer = (MinecraftServer)function.apply(thread2);
        atomicReference.set(minecraftServer);
        thread2.start();
        return (S)minecraftServer;
    }

    public MinecraftServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super("Server");
        this.registries = worldStem.registries();
        this.worldData = worldStem.worldData();
        if (!this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)) {
            throw new IllegalStateException("Missing Overworld dimension data");
        }
        this.proxy = proxy;
        this.packRepository = packRepository;
        this.resources = new ReloadableResources(worldStem.resourceManager(), worldStem.dataPackResources());
        this.services = services;
        if (services.profileCache() != null) {
            services.profileCache().setExecutor(this);
        }
        this.connection = new ServerConnectionListener(this);
        this.tickRateManager = new ServerTickRateManager(this);
        this.progressListenerFactory = chunkProgressListenerFactory;
        this.storageSource = levelStorageAccess;
        this.playerDataStorage = levelStorageAccess.createPlayerStorage();
        this.fixerUpper = dataFixer;
        this.functionManager = new ServerFunctionManager(this, this.resources.managers.getFunctionLibrary());
        HolderLookup.RegistryLookup<Block> registryLookup = this.registries.compositeAccess().registryOrThrow(Registries.BLOCK).asLookup().filterFeatures(this.worldData.enabledFeatures());
        this.structureTemplateManager = new StructureTemplateManager(worldStem.resourceManager(), levelStorageAccess, dataFixer, registryLookup);
        this.serverThread = thread;
        this.executor = Util.backgroundExecutor();
        this.potionBrewing = PotionBrewing.bootstrap(this.worldData.enabledFeatures());
    }

    private void readScoreboard(DimensionDataStorage dimensionDataStorage) {
        dimensionDataStorage.computeIfAbsent(this.getScoreboard().dataFactory(), "scoreboard");
    }

    protected abstract boolean initServer() throws IOException;

    protected void loadLevel() {
        if (!JvmProfiler.INSTANCE.isRunning()) {
            // empty if block
        }
        boolean bl = false;
        ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onWorldLoadedStarted();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
        ChunkProgressListener chunkProgressListener = this.progressListenerFactory.create(this.worldData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));
        this.createLevels(chunkProgressListener);
        this.forceDifficulty();
        this.prepareLevels(chunkProgressListener);
        if (profiledDuration != null) {
            profiledDuration.finish();
        }
        if (bl) {
            try {
                JvmProfiler.INSTANCE.stop();
            }
            catch (Throwable throwable) {
                LOGGER.warn("Failed to stop JFR profiling", throwable);
            }
        }
    }

    protected void forceDifficulty() {
    }

    protected void createLevels(ChunkProgressListener chunkProgressListener) {
        ServerLevelData serverLevelData = this.worldData.overworldData();
        boolean bl = this.worldData.isDebugWorld();
        Registry<LevelStem> registry = this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
        WorldOptions worldOptions = this.worldData.worldGenOptions();
        long l = worldOptions.seed();
        long l2 = BiomeManager.obfuscateSeed(l);
        ImmutableList immutableList = ImmutableList.of((Object)new PhantomSpawner(), (Object)new PatrolSpawner(), (Object)new CatSpawner(), (Object)new VillageSiege(), (Object)new WanderingTraderSpawner(serverLevelData));
        LevelStem levelStem = registry.get(LevelStem.OVERWORLD);
        ServerLevel serverLevel = new ServerLevel(this, this.executor, this.storageSource, serverLevelData, Level.OVERWORLD, levelStem, chunkProgressListener, bl, l2, (List<CustomSpawner>)immutableList, true, null);
        this.levels.put(Level.OVERWORLD, serverLevel);
        DimensionDataStorage dimensionDataStorage = serverLevel.getDataStorage();
        this.readScoreboard(dimensionDataStorage);
        this.commandStorage = new CommandStorage(dimensionDataStorage);
        WorldBorder worldBorder = serverLevel.getWorldBorder();
        if (!serverLevelData.isInitialized()) {
            try {
                MinecraftServer.setInitialSpawn(serverLevel, serverLevelData, worldOptions.generateBonusChest(), bl);
                serverLevelData.setInitialized(true);
                if (bl) {
                    this.setupDebugLevel(this.worldData);
                }
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverLevel.fillReportDetails(crashReport);
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashReport);
            }
            serverLevelData.setInitialized(true);
        }
        this.getPlayerList().addWorldborderListener(serverLevel);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents(), this.registryAccess());
        }
        RandomSequences randomSequences = serverLevel.getRandomSequences();
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == LevelStem.OVERWORLD) continue;
            ResourceKey<Level> resourceKey2 = ResourceKey.create(Registries.DIMENSION, resourceKey.location());
            DerivedLevelData derivedLevelData = new DerivedLevelData(this.worldData, serverLevelData);
            ServerLevel serverLevel2 = new ServerLevel(this, this.executor, this.storageSource, derivedLevelData, resourceKey2, entry.getValue(), chunkProgressListener, bl, l2, (List<CustomSpawner>)ImmutableList.of(), false, randomSequences);
            worldBorder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverLevel2.getWorldBorder()));
            this.levels.put(resourceKey2, serverLevel2);
        }
        worldBorder.applySettings(serverLevelData.getWorldBorder());
    }

    private static void setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
        if (bl2) {
            serverLevelData.setSpawn(BlockPos.ZERO.above(80), 0.0f);
            return;
        }
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        ChunkPos chunkPos = new ChunkPos(serverChunkCache.randomState().sampler().findSpawnPosition());
        int n = serverChunkCache.getGenerator().getSpawnHeight(serverLevel);
        if (n < serverLevel.getMinBuildHeight()) {
            BlockPos blockPos = chunkPos.getWorldPosition();
            n = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX() + 8, blockPos.getZ() + 8);
        }
        serverLevelData.setSpawn(chunkPos.getWorldPosition().offset(8, n, 8), 0.0f);
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        int n5 = -1;
        for (int i = 0; i < Mth.square(11); ++i) {
            BlockPos blockPos;
            if (n2 >= -5 && n2 <= 5 && n3 >= -5 && n3 <= 5 && (blockPos = PlayerRespawnLogic.getSpawnPosInChunk(serverLevel, new ChunkPos(chunkPos.x + n2, chunkPos.z + n3))) != null) {
                serverLevelData.setSpawn(blockPos, 0.0f);
                break;
            }
            if (n2 == n3 || n2 < 0 && n2 == -n3 || n2 > 0 && n2 == 1 - n3) {
                int n6 = n4;
                n4 = -n5;
                n5 = n6;
            }
            n2 += n4;
            n3 += n5;
        }
        if (bl) {
            serverLevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap(registry -> registry.getHolder(MiscOverworldFeatures.BONUS_CHEST)).ifPresent(reference -> ((ConfiguredFeature)reference.value()).place(serverLevel, serverChunkCache.getGenerator(), serverLevel.random, serverLevelData.getSpawnPos()));
        }
    }

    private void setupDebugLevel(WorldData worldData) {
        worldData.setDifficulty(Difficulty.PEACEFUL);
        worldData.setDifficultyLocked(true);
        ServerLevelData serverLevelData = worldData.overworldData();
        serverLevelData.setRaining(false);
        serverLevelData.setThundering(false);
        serverLevelData.setClearWeatherTime(1000000000);
        serverLevelData.setDayTime(6000L);
        serverLevelData.setGameType(GameType.SPECTATOR);
    }

    private void prepareLevels(ChunkProgressListener chunkProgressListener) {
        int n;
        ServerLevel serverLevel = this.overworld();
        LOGGER.info("Preparing start region for dimension {}", (Object)serverLevel.dimension().location());
        BlockPos blockPos = serverLevel.getSharedSpawnPos();
        chunkProgressListener.updateSpawnPos(new ChunkPos(blockPos));
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        this.nextTickTimeNanos = Util.getNanos();
        serverLevel.setDefaultSpawnPos(blockPos, serverLevel.getSharedSpawnAngle());
        int n2 = this.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS);
        int n3 = n = n2 > 0 ? Mth.square(ChunkProgressListener.calculateDiameter(n2)) : 0;
        while (serverChunkCache.getTickingGenerated() < n) {
            this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
            this.waitUntilNextTick();
        }
        this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
        this.waitUntilNextTick();
        for (ServerLevel serverLevel2 : this.levels.values()) {
            ForcedChunksSavedData forcedChunksSavedData = serverLevel2.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
            if (forcedChunksSavedData == null) continue;
            LongIterator longIterator = forcedChunksSavedData.getChunks().iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                ChunkPos chunkPos = new ChunkPos(l);
                serverLevel2.getChunkSource().updateChunkForced(chunkPos, true);
            }
        }
        this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
        this.waitUntilNextTick();
        chunkProgressListener.stop();
        this.updateMobSpawningFlags();
    }

    public GameType getDefaultGameType() {
        return this.worldData.getGameType();
    }

    public boolean isHardcore() {
        return this.worldData.isHardcore();
    }

    public abstract int getOperatorUserPermissionLevel();

    public abstract int getFunctionCompilationLevel();

    public abstract boolean shouldRconBroadcast();

    public boolean saveAllChunks(boolean bl, boolean bl2, boolean bl3) {
        boolean bl4 = false;
        for (ServerLevel object2 : this.getAllLevels()) {
            if (!bl) {
                LOGGER.info("Saving chunks for level '{}'/{}", (Object)object2, (Object)object2.dimension().location());
            }
            object2.save(null, bl2, object2.noSave && !bl3);
            bl4 = true;
        }
        ServerLevel serverLevel = this.overworld();
        ServerLevelData serverLevelData = this.worldData.overworldData();
        serverLevelData.setWorldBorder(serverLevel.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save(this.registryAccess()));
        this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
        if (bl2) {
            for (ServerLevel serverLevel2 : this.getAllLevels()) {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", (Object)serverLevel2.getChunkSource().chunkMap.getStorageName());
            }
            LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }
        return bl4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean saveEverything(boolean bl, boolean bl2, boolean bl3) {
        try {
            this.isSaving = true;
            this.getPlayerList().saveAll();
            boolean bl4 = this.saveAllChunks(bl, bl2, bl3);
            return bl4;
        }
        finally {
            this.isSaving = false;
        }
    }

    @Override
    public void close() {
        this.stopServer();
    }

    public void stopServer() {
        if (this.metricsRecorder.isRecording()) {
            this.cancelRecordingMetrics();
        }
        LOGGER.info("Stopping server");
        this.getConnection().stop();
        this.isSaving = true;
        if (this.playerList != null) {
            LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }
        LOGGER.info("Saving worlds");
        for (ServerLevel serverLevel2 : this.getAllLevels()) {
            if (serverLevel2 == null) continue;
            serverLevel2.noSave = false;
        }
        while (this.levels.values().stream().anyMatch(serverLevel -> serverLevel.getChunkSource().chunkMap.hasWork())) {
            this.nextTickTimeNanos = Util.getNanos() + TimeUtil.NANOSECONDS_PER_MILLISECOND;
            for (ServerLevel serverLevel2 : this.getAllLevels()) {
                serverLevel2.getChunkSource().removeTicketsOnClosing();
                serverLevel2.getChunkSource().tick(() -> true, false);
            }
            this.waitUntilNextTick();
        }
        this.saveAllChunks(false, true, false);
        for (ServerLevel serverLevel2 : this.getAllLevels()) {
            if (serverLevel2 == null) continue;
            try {
                serverLevel2.close();
            }
            catch (IOException iOException) {
                LOGGER.error("Exception closing the level", (Throwable)iOException);
            }
        }
        this.isSaving = false;
        this.resources.close();
        try {
            this.storageSource.close();
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to unlock level {}", (Object)this.storageSource.getLevelId(), (Object)iOException);
        }
    }

    public String getLocalIp() {
        return this.localIp;
    }

    public void setLocalIp(String string) {
        this.localIp = string;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void halt(boolean bl) {
        this.running = false;
        if (bl) {
            try {
                this.serverThread.join();
            }
            catch (InterruptedException interruptedException) {
                LOGGER.error("Error while shutting down", (Throwable)interruptedException);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void runServer() {
        block28: {
            try {
                if (this.initServer()) {
                    this.nextTickTimeNanos = Util.getNanos();
                    this.statusIcon = this.loadStatusIcon().orElse(null);
                    this.status = this.buildServerStatus();
                    while (this.running) {
                        boolean bl;
                        long l;
                        if (!this.isPaused() && this.tickRateManager.isSprinting() && this.tickRateManager.checkShouldSprintThisTick()) {
                            l = 0L;
                            this.lastOverloadWarningNanos = this.nextTickTimeNanos = Util.getNanos();
                        } else {
                            l = this.tickRateManager.nanosecondsPerTick();
                            long l2 = Util.getNanos() - this.nextTickTimeNanos;
                            if (l2 > OVERLOADED_THRESHOLD_NANOS + 20L * l && this.nextTickTimeNanos - this.lastOverloadWarningNanos >= OVERLOADED_WARNING_INTERVAL_NANOS + 100L * l) {
                                long l3 = l2 / l;
                                LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", (Object)(l2 / TimeUtil.NANOSECONDS_PER_MILLISECOND), (Object)l3);
                                this.nextTickTimeNanos += l3 * l;
                                this.lastOverloadWarningNanos = this.nextTickTimeNanos;
                            }
                        }
                        boolean bl2 = bl = l == 0L;
                        if (this.debugCommandProfilerDelayStart) {
                            this.debugCommandProfilerDelayStart = false;
                            this.debugCommandProfiler = new TimeProfiler(Util.getNanos(), this.tickCount);
                        }
                        this.nextTickTimeNanos += l;
                        this.startMetricsRecordingTick();
                        this.profiler.push("tick");
                        this.tickServer(bl ? () -> false : this::haveTime);
                        this.profiler.popPush("nextTickWait");
                        this.mayHaveDelayedTasks = true;
                        this.delayedTasksMaxNextTickTimeNanos = Math.max(Util.getNanos() + l, this.nextTickTimeNanos);
                        this.startMeasuringTaskExecutionTime();
                        this.waitUntilNextTick();
                        this.finishMeasuringTaskExecutionTime();
                        if (bl) {
                            this.tickRateManager.endTickWork();
                        }
                        this.profiler.pop();
                        this.logFullTickTime();
                        this.endMetricsRecordingTick();
                        this.isReady = true;
                        JvmProfiler.INSTANCE.onServerTick(this.smoothedTickTimeMillis);
                    }
                    break block28;
                }
                throw new IllegalStateException("Failed to initialize server");
            }
            catch (Throwable throwable) {
                LOGGER.error("Encountered an unexpected exception", throwable);
                CrashReport crashReport = MinecraftServer.constructOrExtractCrashReport(throwable);
                this.fillSystemReport(crashReport.getSystemReport());
                Path path = this.getServerDirectory().resolve("crash-reports").resolve("crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
                if (crashReport.saveToFile(path, ReportType.CRASH)) {
                    LOGGER.error("This crash report has been saved to: {}", (Object)path.toAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }
                this.onServerCrash(crashReport);
            }
            finally {
                try {
                    this.stopped = true;
                    this.stopServer();
                }
                catch (Throwable throwable) {
                    LOGGER.error("Exception stopping the server", throwable);
                }
                finally {
                    if (this.services.profileCache() != null) {
                        this.services.profileCache().clearExecutor();
                    }
                    this.onServerExit();
                }
            }
        }
    }

    private void logFullTickTime() {
        long l = Util.getNanos();
        if (this.isTickTimeLoggingEnabled()) {
            this.getTickTimeLogger().logSample(l - this.lastTickNanos);
        }
        this.lastTickNanos = l;
    }

    private void startMeasuringTaskExecutionTime() {
        if (this.isTickTimeLoggingEnabled()) {
            this.taskExecutionStartNanos = Util.getNanos();
            this.idleTimeNanos = 0L;
        }
    }

    private void finishMeasuringTaskExecutionTime() {
        if (this.isTickTimeLoggingEnabled()) {
            SampleLogger sampleLogger = this.getTickTimeLogger();
            sampleLogger.logPartialSample(Util.getNanos() - this.taskExecutionStartNanos - this.idleTimeNanos, TpsDebugDimensions.SCHEDULED_TASKS.ordinal());
            sampleLogger.logPartialSample(this.idleTimeNanos, TpsDebugDimensions.IDLE.ordinal());
        }
    }

    private static CrashReport constructOrExtractCrashReport(Throwable throwable) {
        Object object;
        Object object2 = null;
        for (Throwable throwable2 = throwable; throwable2 != null; throwable2 = throwable2.getCause()) {
            if (!(throwable2 instanceof ReportedException)) continue;
            object2 = object = (ReportedException)throwable2;
        }
        if (object2 != null) {
            object = ((ReportedException)object2).getReport();
            if (object2 != throwable) {
                ((CrashReport)object).addCategory("Wrapped in").setDetailError("Wrapping exception", throwable);
            }
        } else {
            object = new CrashReport("Exception in server tick loop", throwable);
        }
        return object;
    }

    private boolean haveTime() {
        return this.runningTask() || Util.getNanos() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTimeNanos : this.nextTickTimeNanos);
    }

    public static boolean throwIfFatalException() {
        RuntimeException runtimeException = fatalException.get();
        if (runtimeException != null) {
            throw runtimeException;
        }
        return true;
    }

    public static void setFatalException(RuntimeException runtimeException) {
        fatalException.compareAndSet(null, runtimeException);
    }

    @Override
    public void managedBlock(BooleanSupplier booleanSupplier) {
        super.managedBlock(() -> MinecraftServer.throwIfFatalException() && booleanSupplier.getAsBoolean());
    }

    protected void waitUntilNextTick() {
        this.runAllTasks();
        this.managedBlock(() -> !this.haveTime());
    }

    @Override
    public void waitForTasks() {
        boolean bl = this.isTickTimeLoggingEnabled();
        long l = bl ? Util.getNanos() : 0L;
        super.waitForTasks();
        if (bl) {
            this.idleTimeNanos += Util.getNanos() - l;
        }
    }

    @Override
    protected TickTask wrapRunnable(Runnable runnable) {
        return new TickTask(this.tickCount, runnable);
    }

    @Override
    protected boolean shouldRun(TickTask tickTask) {
        return tickTask.getTick() + 3 < this.tickCount || this.haveTime();
    }

    @Override
    public boolean pollTask() {
        boolean bl;
        this.mayHaveDelayedTasks = bl = this.pollTaskInternal();
        return bl;
    }

    private boolean pollTaskInternal() {
        if (super.pollTask()) {
            return true;
        }
        if (this.tickRateManager.isSprinting() || this.haveTime()) {
            for (ServerLevel serverLevel : this.getAllLevels()) {
                if (!serverLevel.getChunkSource().pollTask()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doRunTask(TickTask tickTask) {
        this.getProfiler().incrementCounter("runTask");
        super.doRunTask(tickTask);
    }

    private Optional<ServerStatus.Favicon> loadStatusIcon() {
        Optional<Path> optional = Optional.of(this.getFile("server-icon.png")).filter(path -> Files.isRegularFile(path, new LinkOption[0])).or(() -> this.storageSource.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])));
        return optional.flatMap(path -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());
                Preconditions.checkState((bufferedImage.getWidth() == 64 ? 1 : 0) != 0, (Object)"Must be 64 pixels wide");
                Preconditions.checkState((bufferedImage.getHeight() == 64 ? 1 : 0) != 0, (Object)"Must be 64 pixels high");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write((RenderedImage)bufferedImage, "PNG", byteArrayOutputStream);
                return Optional.of(new ServerStatus.Favicon(byteArrayOutputStream.toByteArray()));
            }
            catch (Exception exception) {
                LOGGER.error("Couldn't load server icon", (Throwable)exception);
                return Optional.empty();
            }
        });
    }

    public Optional<Path> getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public Path getServerDirectory() {
        return Path.of("", new String[0]);
    }

    public void onServerCrash(CrashReport crashReport) {
    }

    public void onServerExit() {
    }

    public boolean isPaused() {
        return false;
    }

    public void tickServer(BooleanSupplier booleanSupplier) {
        long l = Util.getNanos();
        ++this.tickCount;
        this.tickRateManager.tick();
        this.tickChildren(booleanSupplier);
        if (l - this.lastServerStatus >= STATUS_EXPIRE_TIME_NANOS) {
            this.lastServerStatus = l;
            this.status = this.buildServerStatus();
        }
        --this.ticksUntilAutosave;
        if (this.ticksUntilAutosave <= 0) {
            this.ticksUntilAutosave = this.computeNextAutosaveInterval();
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.saveEverything(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }
        this.profiler.push("tallying");
        long l2 = Util.getNanos() - l;
        int n = this.tickCount % 100;
        this.aggregatedTickTimesNanos -= this.tickTimesNanos[n];
        this.aggregatedTickTimesNanos += l2;
        this.tickTimesNanos[n] = l2;
        this.smoothedTickTimeMillis = this.smoothedTickTimeMillis * 0.8f + (float)l2 / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND * 0.19999999f;
        this.logTickMethodTime(l);
        this.profiler.pop();
    }

    private void logTickMethodTime(long l) {
        if (this.isTickTimeLoggingEnabled()) {
            this.getTickTimeLogger().logPartialSample(Util.getNanos() - l, TpsDebugDimensions.TICK_SERVER_METHOD.ordinal());
        }
    }

    private int computeNextAutosaveInterval() {
        float f;
        if (this.tickRateManager.isSprinting()) {
            long l = this.getAverageTickTimeNanos() + 1L;
            f = (float)TimeUtil.NANOSECONDS_PER_SECOND / (float)l;
        } else {
            f = this.tickRateManager.tickrate();
        }
        int n = 300;
        return Math.max(100, (int)(f * 300.0f));
    }

    public void onTickRateChanged() {
        int n = this.computeNextAutosaveInterval();
        if (n < this.ticksUntilAutosave) {
            this.ticksUntilAutosave = n;
        }
    }

    protected abstract SampleLogger getTickTimeLogger();

    public abstract boolean isTickTimeLoggingEnabled();

    private ServerStatus buildServerStatus() {
        ServerStatus.Players players = this.buildPlayerStatus();
        return new ServerStatus(Component.nullToEmpty(this.motd), Optional.of(players), Optional.of(ServerStatus.Version.current()), Optional.ofNullable(this.statusIcon), this.enforceSecureProfile());
    }

    private ServerStatus.Players buildPlayerStatus() {
        List<ServerPlayer> list = this.playerList.getPlayers();
        int n = this.getMaxPlayers();
        if (this.hidesOnlinePlayers()) {
            return new ServerStatus.Players(n, list.size(), List.of());
        }
        int n2 = Math.min(list.size(), 12);
        ObjectArrayList objectArrayList = new ObjectArrayList(n2);
        int n3 = Mth.nextInt(this.random, 0, list.size() - n2);
        for (int i = 0; i < n2; ++i) {
            ServerPlayer serverPlayer = list.get(n3 + i);
            objectArrayList.add((Object)(serverPlayer.allowsListing() ? serverPlayer.getGameProfile() : ANONYMOUS_PLAYER_PROFILE));
        }
        Util.shuffle(objectArrayList, this.random);
        return new ServerStatus.Players(n, list.size(), (List<GameProfile>)objectArrayList);
    }

    public void tickChildren(BooleanSupplier booleanSupplier) {
        this.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.connection.suspendFlushing());
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");
        for (ServerLevel object : this.getAllLevels()) {
            this.profiler.push(() -> String.valueOf(object) + " " + String.valueOf(object.dimension().location()));
            if (this.tickCount % 20 == 0) {
                this.profiler.push("timeSync");
                this.synchronizeTime(object);
                this.profiler.pop();
            }
            this.profiler.push("tick");
            try {
                object.tick(booleanSupplier);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception ticking world");
                object.fillReportDetails(crashReport);
                throw new ReportedException(crashReport);
            }
            this.profiler.pop();
            this.profiler.pop();
        }
        this.profiler.popPush("connection");
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();
        if (SharedConstants.IS_RUNNING_IN_IDE && this.tickRateManager.runsNormally()) {
            GameTestTicker.SINGLETON.tick();
        }
        this.profiler.popPush("server gui refresh");
        for (int i = 0; i < this.tickables.size(); ++i) {
            this.tickables.get(i).run();
        }
        this.profiler.popPush("send chunks");
        for (ServerPlayer serverPlayer2 : this.playerList.getPlayers()) {
            serverPlayer2.connection.chunkSender.sendNextChunks(serverPlayer2);
            serverPlayer2.connection.resumeFlushing();
        }
        this.profiler.pop();
    }

    private void synchronizeTime(ServerLevel serverLevel) {
        this.playerList.broadcastAll(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), serverLevel.dimension());
    }

    public void forceTimeSynchronization() {
        this.profiler.push("timeSync");
        for (ServerLevel serverLevel : this.getAllLevels()) {
            this.synchronizeTime(serverLevel);
        }
        this.profiler.pop();
    }

    public boolean isLevelEnabled(Level level) {
        return true;
    }

    public void addTickable(Runnable runnable) {
        this.tickables.add(runnable);
    }

    protected void setId(String string) {
        this.serverId = string;
    }

    public boolean isShutdown() {
        return !this.serverThread.isAlive();
    }

    public Path getFile(String string) {
        return this.getServerDirectory().resolve(string);
    }

    public final ServerLevel overworld() {
        return this.levels.get(Level.OVERWORLD);
    }

    @Nullable
    public ServerLevel getLevel(ResourceKey<Level> resourceKey) {
        return this.levels.get(resourceKey);
    }

    public Set<ResourceKey<Level>> levelKeys() {
        return this.levels.keySet();
    }

    public Iterable<ServerLevel> getAllLevels() {
        return this.levels.values();
    }

    @Override
    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    @Override
    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayerNames() {
        return this.playerList.getPlayerNamesArray();
    }

    @DontObfuscate
    public String getServerModName() {
        return VANILLA_BRAND;
    }

    public SystemReport fillSystemReport(SystemReport systemReport) {
        systemReport.setDetail("Server Running", () -> Boolean.toString(this.running));
        if (this.playerList != null) {
            systemReport.setDetail("Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + String.valueOf(this.playerList.getPlayers()));
        }
        systemReport.setDetail("Active Data Packs", () -> PackRepository.displayPackList(this.packRepository.getSelectedPacks()));
        systemReport.setDetail("Available Data Packs", () -> PackRepository.displayPackList(this.packRepository.getAvailablePacks()));
        systemReport.setDetail("Enabled Feature Flags", () -> FeatureFlags.REGISTRY.toNames(this.worldData.enabledFeatures()).stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
        systemReport.setDetail("World Generation", () -> this.worldData.worldGenSettingsLifecycle().toString());
        systemReport.setDetail("World Seed", () -> String.valueOf(this.worldData.worldGenOptions().seed()));
        if (this.serverId != null) {
            systemReport.setDetail("Server Id", () -> this.serverId);
        }
        return this.fillServerSystemReport(systemReport);
    }

    public abstract SystemReport fillServerSystemReport(SystemReport var1);

    public ModCheck getModdedStatus() {
        return ModCheck.identify(VANILLA_BRAND, this::getServerModName, "Server", MinecraftServer.class);
    }

    @Override
    public void sendSystemMessage(Component component) {
        LOGGER.info(component.getString());
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int n) {
        this.port = n;
    }

    @Nullable
    public GameProfile getSingleplayerProfile() {
        return this.singleplayerProfile;
    }

    public void setSingleplayerProfile(@Nullable GameProfile gameProfile) {
        this.singleplayerProfile = gameProfile;
    }

    public boolean isSingleplayer() {
        return this.singleplayerProfile != null;
    }

    protected void initializeKeyPair() {
        LOGGER.info("Generating keypair");
        try {
            this.keyPair = Crypt.generateKeyPair();
        }
        catch (CryptException cryptException) {
            throw new IllegalStateException("Failed to generate key pair", cryptException);
        }
    }

    public void setDifficulty(Difficulty difficulty, boolean bl) {
        if (!bl && this.worldData.isDifficultyLocked()) {
            return;
        }
        this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : difficulty);
        this.updateMobSpawningFlags();
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    public int getScaledTrackingDistance(int n) {
        return n;
    }

    private void updateMobSpawningFlags() {
        for (ServerLevel serverLevel : this.getAllLevels()) {
            serverLevel.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        }
    }

    public void setDifficultyLocked(boolean bl) {
        this.worldData.setDifficultyLocked(bl);
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    private void sendDifficultyUpdate(ServerPlayer serverPlayer) {
        LevelData levelData = serverPlayer.level().getLevelData();
        serverPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
    }

    public boolean isSpawningMonsters() {
        return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean bl) {
        this.isDemo = bl;
    }

    public Optional<ServerResourcePackInfo> getServerResourcePack() {
        return Optional.empty();
    }

    public boolean isResourcePackRequired() {
        return this.getServerResourcePack().filter(ServerResourcePackInfo::isRequired).isPresent();
    }

    public abstract boolean isDedicatedServer();

    public abstract int getRateLimitPacketsPerSecond();

    public boolean usesAuthentication() {
        return this.onlineMode;
    }

    public void setUsesAuthentication(boolean bl) {
        this.onlineMode = bl;
    }

    public boolean getPreventProxyConnections() {
        return this.preventProxyConnections;
    }

    public void setPreventProxyConnections(boolean bl) {
        this.preventProxyConnections = bl;
    }

    public boolean isSpawningAnimals() {
        return true;
    }

    public boolean areNpcsEnabled() {
        return true;
    }

    public abstract boolean isEpollEnabled();

    public boolean isPvpAllowed() {
        return this.pvp;
    }

    public void setPvpAllowed(boolean bl) {
        this.pvp = bl;
    }

    public boolean isFlightAllowed() {
        return this.allowFlight;
    }

    public void setFlightAllowed(boolean bl) {
        this.allowFlight = bl;
    }

    public abstract boolean isCommandBlockEnabled();

    @Override
    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String string) {
        this.motd = string;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void setPlayerList(PlayerList playerList) {
        this.playerList = playerList;
    }

    public abstract boolean isPublished();

    public void setDefaultGameType(GameType gameType) {
        this.worldData.setGameType(gameType);
    }

    public ServerConnectionListener getConnection() {
        return this.connection;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean hasGui() {
        return false;
    }

    public boolean publishServer(@Nullable GameType gameType, boolean bl, int n) {
        return false;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public int getSpawnProtectionRadius() {
        return 16;
    }

    public boolean isUnderSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player) {
        return false;
    }

    public boolean repliesToStatus() {
        return true;
    }

    public boolean hidesOnlinePlayers() {
        return false;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public int getPlayerIdleTimeout() {
        return this.playerIdleTimeout;
    }

    public void setPlayerIdleTimeout(int n) {
        this.playerIdleTimeout = n;
    }

    public MinecraftSessionService getSessionService() {
        return this.services.sessionService();
    }

    @Nullable
    public SignatureValidator getProfileKeySignatureValidator() {
        return this.services.profileKeySignatureValidator();
    }

    public GameProfileRepository getProfileRepository() {
        return this.services.profileRepository();
    }

    @Nullable
    public GameProfileCache getProfileCache() {
        return this.services.profileCache();
    }

    @Nullable
    public ServerStatus getStatus() {
        return this.status;
    }

    public void invalidateStatus() {
        this.lastServerStatus = 0L;
    }

    public int getAbsoluteMaxWorldSize() {
        return 29999984;
    }

    @Override
    public boolean scheduleExecutables() {
        return super.scheduleExecutables() && !this.isStopped();
    }

    @Override
    public void executeIfPossible(Runnable runnable) {
        if (this.isStopped()) {
            throw new RejectedExecutionException("Server already shutting down");
        }
        super.executeIfPossible(runnable);
    }

    @Override
    public Thread getRunningThread() {
        return this.serverThread;
    }

    public int getCompressionThreshold() {
        return 256;
    }

    public boolean enforceSecureProfile() {
        return false;
    }

    public long getNextTickTime() {
        return this.nextTickTimeNanos;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public int getSpawnRadius(@Nullable ServerLevel serverLevel) {
        if (serverLevel != null) {
            return serverLevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS);
        }
        return 10;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.resources.managers.getAdvancements();
    }

    public ServerFunctionManager getFunctions() {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> collection) {
        CompletionStage completionStage = ((CompletableFuture)CompletableFuture.supplyAsync(() -> (ImmutableList)collection.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList()), this).thenCompose(immutableList -> {
            MultiPackResourceManager multiPackResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, (List<PackResources>)immutableList);
            return ((CompletableFuture)ReloadableServerResources.loadResources(multiPackResourceManager, this.registries, this.worldData.enabledFeatures(), this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED, this.getFunctionCompilationLevel(), this.executor, this).whenComplete((reloadableServerResources, throwable) -> {
                if (throwable != null) {
                    multiPackResourceManager.close();
                }
            })).thenApply(reloadableServerResources -> new ReloadableResources(multiPackResourceManager, (ReloadableServerResources)reloadableServerResources));
        })).thenAcceptAsync(reloadableResources -> {
            this.resources.close();
            this.resources = reloadableResources;
            this.packRepository.setSelected(collection);
            WorldDataConfiguration worldDataConfiguration = new WorldDataConfiguration(MinecraftServer.getSelectedPacks(this.packRepository, true), this.worldData.enabledFeatures());
            this.worldData.setDataConfiguration(worldDataConfiguration);
            this.resources.managers.updateRegistryTags();
            this.getPlayerList().saveAll();
            this.getPlayerList().reloadResources();
            this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
            this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
        }, (Executor)this);
        if (this.isSameThread()) {
            this.managedBlock(((CompletableFuture)completionStage)::isDone);
        }
        return completionStage;
    }

    public static WorldDataConfiguration configurePackRepository(PackRepository packRepository, WorldDataConfiguration worldDataConfiguration, boolean bl, boolean bl2) {
        DataPackConfig dataPackConfig = worldDataConfiguration.dataPacks();
        FeatureFlagSet featureFlagSet = bl ? FeatureFlagSet.of() : worldDataConfiguration.enabledFeatures();
        FeatureFlagSet featureFlagSet2 = bl ? FeatureFlags.REGISTRY.allFlags() : worldDataConfiguration.enabledFeatures();
        packRepository.reload();
        if (bl2) {
            return MinecraftServer.configureRepositoryWithSelection(packRepository, List.of(VANILLA_BRAND), featureFlagSet, false);
        }
        LinkedHashSet linkedHashSet = Sets.newLinkedHashSet();
        for (String object : dataPackConfig.getEnabled()) {
            if (packRepository.isAvailable(object)) {
                linkedHashSet.add(object);
                continue;
            }
            LOGGER.warn("Missing data pack {}", (Object)object);
        }
        for (Pack pack : packRepository.getAvailablePacks()) {
            String string = pack.getId();
            if (dataPackConfig.getDisabled().contains(string)) continue;
            FeatureFlagSet featureFlagSet3 = pack.getRequestedFeatures();
            boolean bl3 = linkedHashSet.contains(string);
            if (!bl3 && pack.getPackSource().shouldAddAutomatically()) {
                if (featureFlagSet3.isSubsetOf(featureFlagSet2)) {
                    LOGGER.info("Found new data pack {}, loading it automatically", (Object)string);
                    linkedHashSet.add(string);
                } else {
                    LOGGER.info("Found new data pack {}, but can't load it due to missing features {}", (Object)string, (Object)FeatureFlags.printMissingFlags(featureFlagSet2, featureFlagSet3));
                }
            }
            if (!bl3 || featureFlagSet3.isSubsetOf(featureFlagSet2)) continue;
            LOGGER.warn("Pack {} requires features {} that are not enabled for this world, disabling pack.", (Object)string, (Object)FeatureFlags.printMissingFlags(featureFlagSet2, featureFlagSet3));
            linkedHashSet.remove(string);
        }
        if (linkedHashSet.isEmpty()) {
            LOGGER.info("No datapacks selected, forcing vanilla");
            linkedHashSet.add(VANILLA_BRAND);
        }
        return MinecraftServer.configureRepositoryWithSelection(packRepository, linkedHashSet, featureFlagSet, true);
    }

    private static WorldDataConfiguration configureRepositoryWithSelection(PackRepository packRepository, Collection<String> collection, FeatureFlagSet featureFlagSet, boolean bl) {
        packRepository.setSelected(collection);
        MinecraftServer.enableForcedFeaturePacks(packRepository, featureFlagSet);
        DataPackConfig dataPackConfig = MinecraftServer.getSelectedPacks(packRepository, bl);
        FeatureFlagSet featureFlagSet2 = packRepository.getRequestedFeatureFlags().join(featureFlagSet);
        return new WorldDataConfiguration(dataPackConfig, featureFlagSet2);
    }

    private static void enableForcedFeaturePacks(PackRepository packRepository, FeatureFlagSet featureFlagSet) {
        FeatureFlagSet featureFlagSet2 = packRepository.getRequestedFeatureFlags();
        FeatureFlagSet featureFlagSet3 = featureFlagSet.subtract(featureFlagSet2);
        if (featureFlagSet3.isEmpty()) {
            return;
        }
        ObjectArraySet objectArraySet = new ObjectArraySet(packRepository.getSelectedIds());
        for (Pack pack : packRepository.getAvailablePacks()) {
            if (featureFlagSet3.isEmpty()) break;
            if (pack.getPackSource() != PackSource.FEATURE) continue;
            String string = pack.getId();
            FeatureFlagSet featureFlagSet4 = pack.getRequestedFeatures();
            if (featureFlagSet4.isEmpty() || !featureFlagSet4.intersects(featureFlagSet3) || !featureFlagSet4.isSubsetOf(featureFlagSet)) continue;
            if (!objectArraySet.add(string)) {
                throw new IllegalStateException("Tried to force '" + string + "', but it was already enabled");
            }
            LOGGER.info("Found feature pack ('{}') for requested feature, forcing to enabled", (Object)string);
            featureFlagSet3 = featureFlagSet3.subtract(featureFlagSet4);
        }
        packRepository.setSelected((Collection<String>)objectArraySet);
    }

    private static DataPackConfig getSelectedPacks(PackRepository packRepository, boolean bl) {
        Collection<String> collection = packRepository.getSelectedIds();
        ImmutableList immutableList = ImmutableList.copyOf(collection);
        List<String> list = bl ? packRepository.getAvailableIds().stream().filter(string -> !collection.contains(string)).toList() : List.of();
        return new DataPackConfig((List<String>)immutableList, list);
    }

    public void kickUnlistedPlayers(CommandSourceStack commandSourceStack) {
        if (!this.isEnforceWhitelist()) {
            return;
        }
        PlayerList playerList = commandSourceStack.getServer().getPlayerList();
        UserWhiteList userWhiteList = playerList.getWhiteList();
        ArrayList arrayList = Lists.newArrayList(playerList.getPlayers());
        for (ServerPlayer serverPlayer : arrayList) {
            if (userWhiteList.isWhiteListed(serverPlayer.getGameProfile())) continue;
            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
        }
    }

    public PackRepository getPackRepository() {
        return this.packRepository;
    }

    public Commands getCommands() {
        return this.resources.managers.getCommands();
    }

    public CommandSourceStack createCommandSourceStack() {
        ServerLevel serverLevel = this.overworld();
        return new CommandSourceStack(this, serverLevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()), Vec2.ZERO, serverLevel, 4, "Server", Component.literal("Server"), this, null);
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public abstract boolean shouldInformAdmins();

    public RecipeManager getRecipeManager() {
        return this.resources.managers.getRecipeManager();
    }

    public ServerScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public CommandStorage getCommandStorage() {
        if (this.commandStorage == null) {
            throw new NullPointerException("Called before server init");
        }
        return this.commandStorage;
    }

    public GameRules getGameRules() {
        return this.overworld().getGameRules();
    }

    public CustomBossEvents getCustomBossEvents() {
        return this.customBossEvents;
    }

    public boolean isEnforceWhitelist() {
        return this.enforceWhitelist;
    }

    public void setEnforceWhitelist(boolean bl) {
        this.enforceWhitelist = bl;
    }

    public float getCurrentSmoothedTickTime() {
        return this.smoothedTickTimeMillis;
    }

    public ServerTickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    public long getAverageTickTimeNanos() {
        return this.aggregatedTickTimesNanos / (long)Math.min(100, Math.max(this.tickCount, 1));
    }

    public long[] getTickTimesNanos() {
        return this.tickTimesNanos;
    }

    public int getProfilePermissions(GameProfile gameProfile) {
        if (this.getPlayerList().isOp(gameProfile)) {
            ServerOpListEntry serverOpListEntry = (ServerOpListEntry)this.getPlayerList().getOps().get(gameProfile);
            if (serverOpListEntry != null) {
                return serverOpListEntry.getLevel();
            }
            if (this.isSingleplayerOwner(gameProfile)) {
                return 4;
            }
            if (this.isSingleplayer()) {
                return this.getPlayerList().isAllowCommandsForAllPlayers() ? 4 : 0;
            }
            return this.getOperatorUserPermissionLevel();
        }
        return 0;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public abstract boolean isSingleplayerOwner(GameProfile var1);

    public void dumpServerProperties(Path path) throws IOException {
    }

    private void saveDebugReport(Path path) {
        Path path2 = path.resolve("levels");
        try {
            for (Map.Entry<ResourceKey<Level>, ServerLevel> entry : this.levels.entrySet()) {
                ResourceLocation resourceLocation = entry.getKey().location();
                Path path3 = path2.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath());
                Files.createDirectories(path3, new FileAttribute[0]);
                entry.getValue().saveDebugReport(path3);
            }
            this.dumpGameRules(path.resolve("gamerules.txt"));
            this.dumpClasspath(path.resolve("classpath.txt"));
            this.dumpMiscStats(path.resolve("stats.txt"));
            this.dumpThreads(path.resolve("threads.txt"));
            this.dumpServerProperties(path.resolve("server.properties.txt"));
            this.dumpNativeModules(path.resolve("modules.txt"));
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to save debug report", (Throwable)iOException);
        }
    }

    private void dumpMiscStats(Path path) throws IOException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
            bufferedWriter.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
            bufferedWriter.write(String.format(Locale.ROOT, "average_tick_time: %f\n", Float.valueOf(this.getCurrentSmoothedTickTime())));
            bufferedWriter.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimesNanos)));
            bufferedWriter.write(String.format(Locale.ROOT, "queue: %s\n", Util.backgroundExecutor()));
        }
    }

    private void dumpGameRules(Path path) throws IOException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
            final ArrayList arrayList = Lists.newArrayList();
            final GameRules gameRules = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor(){

                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                    arrayList.add(String.format(Locale.ROOT, "%s=%s\n", key.getId(), gameRules.getRule(key)));
                }
            });
            for (String string : arrayList) {
                bufferedWriter.write(string);
            }
        }
    }

    private void dumpClasspath(Path path) throws IOException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
            String string = System.getProperty("java.class.path");
            String string2 = System.getProperty("path.separator");
            for (String string3 : Splitter.on((String)string2).split((CharSequence)string)) {
                bufferedWriter.write(string3);
                bufferedWriter.write("\n");
            }
        }
    }

    private void dumpThreads(Path path) throws IOException {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfoArray = threadMXBean.dumpAllThreads(true, true);
        Arrays.sort(threadInfoArray, Comparator.comparing(ThreadInfo::getThreadName));
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
            for (ThreadInfo threadInfo : threadInfoArray) {
                bufferedWriter.write(threadInfo.toString());
                ((Writer)bufferedWriter).write(10);
            }
        }
    }

    private void dumpNativeModules(Path path) throws IOException {
        BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);
        try {
            ArrayList arrayList;
            try {
                arrayList = Lists.newArrayList(NativeModuleLister.listModules());
            }
            catch (Throwable throwable) {
                LOGGER.warn("Failed to list native modules", throwable);
                if (bufferedWriter != null) {
                    ((Writer)bufferedWriter).close();
                }
                return;
            }
            arrayList.sort(Comparator.comparing(nativeModuleInfo -> nativeModuleInfo.name));
            for (NativeModuleLister.NativeModuleInfo nativeModuleInfo2 : arrayList) {
                bufferedWriter.write(nativeModuleInfo2.toString());
                ((Writer)bufferedWriter).write(10);
            }
        }
        finally {
            if (bufferedWriter != null) {
                try {
                    ((Writer)bufferedWriter).close();
                }
                catch (Throwable throwable) {
                    Throwable throwable2;
                    throwable2.addSuppressed(throwable);
                }
            }
        }
    }

    private void startMetricsRecordingTick() {
        if (this.willStartRecordingMetrics) {
            this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()), Util.timeSource, Util.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, path -> {
                this.executeBlocking(() -> this.saveDebugReport(path.resolve("server")));
                this.onMetricsRecordingFinished.accept((Path)path);
            });
            this.willStartRecordingMetrics = false;
        }
        this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
        this.metricsRecorder.startTick();
        this.profiler.startTick();
    }

    public void endMetricsRecordingTick() {
        this.profiler.endTick();
        this.metricsRecorder.endTick();
    }

    public boolean isRecordingMetrics() {
        return this.metricsRecorder.isRecording();
    }

    public void startRecordingMetrics(Consumer<ProfileResults> consumer, Consumer<Path> consumer2) {
        this.onMetricsRecordingStopped = profileResults -> {
            this.stopRecordingMetrics();
            consumer.accept((ProfileResults)profileResults);
        };
        this.onMetricsRecordingFinished = consumer2;
        this.willStartRecordingMetrics = true;
    }

    public void stopRecordingMetrics() {
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    }

    public void finishRecordingMetrics() {
        this.metricsRecorder.end();
    }

    public void cancelRecordingMetrics() {
        this.metricsRecorder.cancel();
        this.profiler = this.metricsRecorder.getProfiler();
    }

    public Path getWorldPath(LevelResource levelResource) {
        return this.storageSource.getLevelPath(levelResource);
    }

    public boolean forceSynchronousWrites() {
        return true;
    }

    public StructureTemplateManager getStructureManager() {
        return this.structureTemplateManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public RegistryAccess.Frozen registryAccess() {
        return this.registries.compositeAccess();
    }

    public LayeredRegistryAccess<RegistryLayer> registries() {
        return this.registries;
    }

    public ReloadableServerRegistries.Holder reloadableRegistries() {
        return this.resources.managers.fullRegistries();
    }

    public TextFilter createTextFilterForPlayer(ServerPlayer serverPlayer) {
        return TextFilter.DUMMY;
    }

    public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer serverPlayer) {
        return this.isDemo() ? new DemoMode(serverPlayer) : new ServerPlayerGameMode(serverPlayer);
    }

    @Nullable
    public GameType getForcedGameType() {
        return null;
    }

    public ResourceManager getResourceManager() {
        return this.resources.resourceManager;
    }

    public boolean isCurrentlySaving() {
        return this.isSaving;
    }

    public boolean isTimeProfilerRunning() {
        return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
    }

    public void startTimeProfiler() {
        this.debugCommandProfilerDelayStart = true;
    }

    public ProfileResults stopTimeProfiler() {
        if (this.debugCommandProfiler == null) {
            return EmptyProfileResults.EMPTY;
        }
        ProfileResults profileResults = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
        this.debugCommandProfiler = null;
        return profileResults;
    }

    public int getMaxChainedNeighborUpdates() {
        return 1000000;
    }

    public void logChatMessage(Component component, ChatType.Bound bound, @Nullable String string) {
        String string2 = bound.decorate(component).getString();
        if (string != null) {
            LOGGER.info("[{}] {}", (Object)string, (Object)string2);
        } else {
            LOGGER.info("{}", (Object)string2);
        }
    }

    public ChatDecorator getChatDecorator() {
        return ChatDecorator.PLAIN;
    }

    public boolean logIPs() {
        return true;
    }

    public void subscribeToDebugSample(ServerPlayer serverPlayer, RemoteDebugSampleType remoteDebugSampleType) {
    }

    public boolean acceptsTransfers() {
        return false;
    }

    private void storeChunkIoError(CrashReport crashReport, ChunkPos chunkPos, RegionStorageInfo regionStorageInfo) {
        Util.ioPool().execute(() -> {
            try {
                Path path = this.getFile("debug");
                FileUtil.createDirectoriesSafe(path);
                String string = FileUtil.sanitizeName(regionStorageInfo.level());
                Path path2 = path.resolve("chunk-" + string + "-" + Util.getFilenameFormattedDateTime() + "-server.txt");
                FileStore fileStore = Files.getFileStore(path);
                long l = fileStore.getUsableSpace();
                if (l < 8192L) {
                    LOGGER.warn("Not storing chunk IO report due to low space on drive {}", (Object)fileStore.name());
                    return;
                }
                CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk Info");
                crashReportCategory.setDetail("Level", regionStorageInfo::level);
                crashReportCategory.setDetail("Dimension", () -> regionStorageInfo.dimension().location().toString());
                crashReportCategory.setDetail("Storage", regionStorageInfo::type);
                crashReportCategory.setDetail("Position", chunkPos::toString);
                crashReport.saveToFile(path2, ReportType.CHUNK_IO_ERROR);
                LOGGER.info("Saved details to {}", (Object)crashReport.getSaveFile());
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to store chunk IO exception", (Throwable)exception);
            }
        });
    }

    @Override
    public void reportChunkLoadFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos) {
        LOGGER.error("Failed to load chunk {},{}", new Object[]{chunkPos.x, chunkPos.z, throwable});
        this.storeChunkIoError(CrashReport.forThrowable(throwable, "Chunk load failure"), chunkPos, regionStorageInfo);
    }

    @Override
    public void reportChunkSaveFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos) {
        LOGGER.error("Failed to save chunk {},{}", new Object[]{chunkPos.x, chunkPos.z, throwable});
        this.storeChunkIoError(CrashReport.forThrowable(throwable, "Chunk save failure"), chunkPos, regionStorageInfo);
    }

    public PotionBrewing potionBrewing() {
        return this.potionBrewing;
    }

    public ServerLinks serverLinks() {
        return ServerLinks.EMPTY;
    }

    @Override
    public /* synthetic */ void doRunTask(Runnable runnable) {
        this.doRunTask((TickTask)runnable);
    }

    @Override
    public /* synthetic */ boolean shouldRun(Runnable runnable) {
        return this.shouldRun((TickTask)runnable);
    }

    @Override
    public /* synthetic */ Runnable wrapRunnable(Runnable runnable) {
        return this.wrapRunnable(runnable);
    }

    static final class ReloadableResources
    extends Record
    implements AutoCloseable {
        final CloseableResourceManager resourceManager;
        final ReloadableServerResources managers;

        ReloadableResources(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources) {
            this.resourceManager = closeableResourceManager;
            this.managers = reloadableServerResources;
        }

        @Override
        public void close() {
            this.resourceManager.close();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ReloadableResources.class, "resourceManager;managers", "resourceManager", "managers"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ReloadableResources.class, "resourceManager;managers", "resourceManager", "managers"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ReloadableResources.class, "resourceManager;managers", "resourceManager", "managers"}, this, object);
        }

        public CloseableResourceManager resourceManager() {
            return this.resourceManager;
        }

        public ReloadableServerResources managers() {
            return this.managers;
        }
    }

    static class TimeProfiler {
        final long startNanos;
        final int startTick;

        TimeProfiler(long l, int n) {
            this.startNanos = l;
            this.startTick = n;
        }

        ProfileResults stop(final long l, final int n) {
            return new ProfileResults(){

                @Override
                public List<ResultField> getTimes(String string) {
                    return Collections.emptyList();
                }

                @Override
                public boolean saveResults(Path path) {
                    return false;
                }

                @Override
                public long getStartTimeNano() {
                    return startNanos;
                }

                @Override
                public int getStartTimeTicks() {
                    return startTick;
                }

                @Override
                public long getEndTimeNano() {
                    return l;
                }

                @Override
                public int getEndTimeTicks() {
                    return n;
                }

                @Override
                public String getProfilerResults() {
                    return "";
                }
            };
        }
    }

    public record ServerResourcePackInfo(UUID id, String url, String hash, boolean isRequired, @Nullable Component prompt) {
    }
}

