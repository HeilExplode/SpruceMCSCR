/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  javax.annotation.Nullable
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 *  joptsimple.ValueConverter
 *  joptsimple.util.PathConverter
 *  joptsimple.util.PathProperties
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import joptsimple.ValueConverter;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] stringArray) {
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpecBuilder = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpecBuilder2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpecBuilder3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpecBuilder4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpecBuilder5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpecBuilder6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpecBuilder7 = optionParser.accepts("recreateRegionFiles");
        OptionSpecBuilder optionSpecBuilder8 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec abstractOptionSpec = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec = optionParser.accepts("universe").withRequiredArg().defaultsTo((Object)".", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec2 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec3 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo((Object)-1, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec4 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpecBuilder9 = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec5 = optionParser.accepts("pidFile").withRequiredArg().withValuesConvertedBy((ValueConverter)new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec nonOptionArgumentSpec = optionParser.nonOptions();
        try {
            WorldStem worldStem;
            Object object;
            Object object2;
            Object object3;
            Dynamic<?> dynamic;
            OptionSet optionSet = optionParser.parse(stringArray);
            if (optionSet.has((OptionSpec)abstractOptionSpec)) {
                optionParser.printHelpOn((OutputStream)System.err);
                return;
            }
            Path path = (Path)optionSet.valueOf((OptionSpec)argumentAcceptingOptionSpec5);
            if (path != null) {
                Main.writePidFile(path);
            }
            CrashReport.preload();
            if (optionSet.has((OptionSpec)optionSpecBuilder9)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path path2 = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path2);
            dedicatedServerSettings.forceSave();
            RegionFileVersion.configure(dedicatedServerSettings.getProperties().regionFileComression);
            Path path3 = Paths.get("eula.txt", new String[0]);
            Eula eula = new Eula(path3);
            if (optionSet.has((OptionSpec)optionSpecBuilder2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path2.toAbsolutePath(), (Object)path3.toAbsolutePath());
                return;
            }
            if (!eula.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File((String)optionSet.valueOf((OptionSpec)argumentAcceptingOptionSpec));
            Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String string = Optional.ofNullable((String)optionSet.valueOf((OptionSpec)argumentAcceptingOptionSpec2)).orElse(dedicatedServerSettings.getProperties().levelName);
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
            LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.validateAndCreateAccess(string);
            if (levelStorageAccess.hasWorldData()) {
                try {
                    dynamic = levelStorageAccess.getDataTag();
                    object3 = levelStorageAccess.getSummary(dynamic);
                }
                catch (IOException | NbtException | ReportedNbtException exception) {
                    object2 = levelStorageAccess.getLevelDirectory();
                    LOGGER.warn("Failed to load world data from {}", (Object)((LevelStorageSource.LevelDirectory)object2).dataFile(), (Object)exception);
                    LOGGER.info("Attempting to use fallback");
                    try {
                        dynamic = levelStorageAccess.getDataTagFallback();
                        object3 = levelStorageAccess.getSummary(dynamic);
                    }
                    catch (IOException | NbtException | ReportedNbtException exception2) {
                        LOGGER.error("Failed to load world data from {}", (Object)((LevelStorageSource.LevelDirectory)object2).oldDataFile(), (Object)exception2);
                        LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", (Object)((LevelStorageSource.LevelDirectory)object2).dataFile(), (Object)((LevelStorageSource.LevelDirectory)object2).oldDataFile());
                        return;
                    }
                    levelStorageAccess.restoreLevelDataFromOld();
                }
                if (((LevelSummary)object3).requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!((LevelSummary)object3).isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                dynamic = null;
            }
            object3 = dynamic;
            boolean bl = optionSet.has((OptionSpec)optionSpecBuilder8);
            if (bl) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            object2 = ServerPacksSource.createPackRepository(levelStorageAccess);
            try {
                object = Main.loadOrCreateConfig(dedicatedServerSettings.getProperties(), object3, bl, (PackRepository)object2);
                worldStem = (WorldStem)Util.blockUntilDone(arg_0 -> Main.lambda$main$1((WorldLoader.InitConfig)object, (Dynamic)object3, optionSet, (OptionSpec)optionSpecBuilder3, dedicatedServerSettings, (OptionSpec)optionSpecBuilder4, arg_0)).get();
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)exception);
                return;
            }
            object = worldStem.registries().compositeAccess();
            boolean bl2 = optionSet.has((OptionSpec)optionSpecBuilder7);
            if (optionSet.has((OptionSpec)optionSpecBuilder5) || bl2) {
                Main.forceUpgrade(levelStorageAccess, DataFixers.getDataFixer(), optionSet.has((OptionSpec)optionSpecBuilder6), () -> true, (RegistryAccess)object, bl2);
            }
            WorldData worldData = worldStem.worldData();
            levelStorageAccess.saveDataTag((RegistryAccess)object, worldData);
            final DedicatedServer dedicatedServer = MinecraftServer.spin(arg_0 -> Main.lambda$main$3(levelStorageAccess, (PackRepository)object2, worldStem, dedicatedServerSettings, services, optionSet, (OptionSpec)argumentAcceptingOptionSpec3, (OptionSpec)optionSpecBuilder3, (OptionSpec)argumentAcceptingOptionSpec4, (OptionSpec)optionSpecBuilder, (OptionSpec)nonOptionArgumentSpec, arg_0));
            Thread thread = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    dedicatedServer.halt(true);
                }
            };
            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        }
        catch (Exception exception) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)exception);
        }
    }

    private static void writePidFile(Path path) {
        try {
            long l = ProcessHandle.current().pid();
            Files.writeString(path, (CharSequence)Long.toString(l), new OpenOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    private static WorldLoader.InitConfig loadOrCreateConfig(DedicatedServerProperties dedicatedServerProperties, @Nullable Dynamic<?> dynamic, boolean bl, PackRepository packRepository) {
        WorldDataConfiguration worldDataConfiguration;
        boolean bl2;
        Record record;
        if (dynamic != null) {
            record = LevelStorageSource.readDataConfig(dynamic);
            bl2 = false;
            worldDataConfiguration = record;
        } else {
            bl2 = true;
            worldDataConfiguration = new WorldDataConfiguration(dedicatedServerProperties.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
        }
        record = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, bl, bl2);
        return new WorldLoader.InitConfig((WorldLoader.PackConfig)record, Commands.CommandSelection.DEDICATED, dedicatedServerProperties.functionPermissionLevel);
    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, boolean bl, BooleanSupplier booleanSupplier, RegistryAccess registryAccess, boolean bl2) {
        LOGGER.info("Forcing world upgrade!");
        WorldUpgrader worldUpgrader = new WorldUpgrader(levelStorageAccess, dataFixer, registryAccess, bl, bl2);
        Component component = null;
        while (!worldUpgrader.isFinished()) {
            int n;
            Component component2 = worldUpgrader.getStatus();
            if (component != component2) {
                component = component2;
                LOGGER.info(worldUpgrader.getStatus().getString());
            }
            if ((n = worldUpgrader.getTotalChunks()) > 0) {
                int n2 = worldUpgrader.getConverted() + worldUpgrader.getSkipped();
                LOGGER.info("{}% completed ({} / {} chunks)...", new Object[]{Mth.floor((float)n2 / (float)n * 100.0f), n2, n});
            }
            if (!booleanSupplier.getAsBoolean()) {
                worldUpgrader.cancel();
                continue;
            }
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    private static /* synthetic */ DedicatedServer lambda$main$3(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, DedicatedServerSettings dedicatedServerSettings, Services services, OptionSet optionSet, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, Thread thread) {
        boolean bl;
        DedicatedServer dedicatedServer = new DedicatedServer(thread, levelStorageAccess, packRepository, worldStem, dedicatedServerSettings, DataFixers.getDataFixer(), services, LoggerChunkProgressListener::createFromGameruleRadius);
        dedicatedServer.setPort((Integer)optionSet.valueOf(optionSpec));
        dedicatedServer.setDemo(optionSet.has(optionSpec2));
        dedicatedServer.setId((String)optionSet.valueOf(optionSpec3));
        boolean bl2 = bl = !optionSet.has(optionSpec4) && !optionSet.valuesOf(optionSpec5).contains("nogui");
        if (bl && !GraphicsEnvironment.isHeadless()) {
            dedicatedServer.showGui();
        }
        return dedicatedServer;
    }

    private static /* synthetic */ CompletableFuture lambda$main$1(WorldLoader.InitConfig initConfig, Dynamic dynamic, OptionSet optionSet, OptionSpec optionSpec, DedicatedServerSettings dedicatedServerSettings, OptionSpec optionSpec2, Executor executor) {
        return WorldLoader.load(initConfig, dataLoadContext -> {
            Object object;
            WorldDimensions worldDimensions;
            WorldOptions worldOptions;
            LevelSettings levelSettings;
            Registry<LevelStem> registry = dataLoadContext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
            if (dynamic != null) {
                LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen());
                return new WorldLoader.DataLoadOutput<WorldData>(levelDataAndDimensions.worldData(), levelDataAndDimensions.dimensions().dimensionsRegistryAccess());
            }
            LOGGER.info("No existing world data, creating new world");
            if (optionSet.has(optionSpec)) {
                levelSettings = MinecraftServer.DEMO_SETTINGS;
                worldOptions = WorldOptions.DEMO_OPTIONS;
                worldDimensions = WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen());
            } else {
                object = dedicatedServerSettings.getProperties();
                levelSettings = new LevelSettings(((DedicatedServerProperties)object).levelName, ((DedicatedServerProperties)object).gamemode, ((DedicatedServerProperties)object).hardcore, ((DedicatedServerProperties)object).difficulty, false, new GameRules(), dataLoadContext.dataConfiguration());
                worldOptions = optionSet.has(optionSpec2) ? ((DedicatedServerProperties)object).worldOptions.withBonusChest(true) : ((DedicatedServerProperties)object).worldOptions;
                worldDimensions = ((DedicatedServerProperties)object).createDimensions(dataLoadContext.datapackWorldgen());
            }
            object = worldDimensions.bake(registry);
            Lifecycle lifecycle = ((WorldDimensions.Complete)object).lifecycle().add(dataLoadContext.datapackWorldgen().allRegistriesLifecycle());
            return new WorldLoader.DataLoadOutput<PrimaryLevelData>(new PrimaryLevelData(levelSettings, worldOptions, ((WorldDimensions.Complete)object).specialWorldProperty(), lifecycle), ((WorldDimensions.Complete)object).dimensionsRegistryAccess());
        }, WorldStem::new, Util.backgroundExecutor(), executor);
    }
}

