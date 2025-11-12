/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestBatchListener;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureBlockPosFinder;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFinder;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.gametest.framework.TestFunctionArgument;
import net.minecraft.gametest.framework.TestFunctionFinder;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class TestCommand {
    public static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
    public static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_CLEAR_RADIUS = 200;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int SHOW_POS_DURATION_MS = 10000;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;
    private static final String STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND = "Structure block entity could not be found";
    private static final TestFinder.Builder<Runner> testFinder = new TestFinder.Builder<Runner>(Runner::new);

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Function<CommandContext<CommandSourceStack>, Runner> function, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function2) {
        return argumentBuilder.executes(commandContext -> ((Runner)function.apply(commandContext)).run()).then(((RequiredArgumentBuilder)Commands.argument("numberOfTimes", IntegerArgumentType.integer((int)0)).executes(commandContext -> ((Runner)function.apply(commandContext)).run(new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), false)))).then(function2.apply(Commands.argument("untilFailed", BoolArgumentType.bool()).executes(commandContext -> ((Runner)function.apply(commandContext)).run(new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"untilFailed")))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2, Function<CommandContext<CommandSourceStack>, Runner> function) {
        return TestCommand.runWithRetryOptions(argumentBuilder2, function, argumentBuilder -> argumentBuilder);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2, Function<CommandContext<CommandSourceStack>, Runner> function) {
        return TestCommand.runWithRetryOptions(argumentBuilder2, function, argumentBuilder -> argumentBuilder.then(((RequiredArgumentBuilder)Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes(commandContext -> ((Runner)function.apply(commandContext)).run(new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"rotationSteps")))).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes(commandContext -> ((Runner)function.apply(commandContext)).run(new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"rotationSteps"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"testsPerRow"))))));
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        ArgumentBuilder<CommandSourceStack, ?> argumentBuilder = TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("onlyRequiredTests", BoolArgumentType.bool()), commandContext -> testFinder.failedTests((CommandContext<CommandSourceStack>)commandContext, BoolArgumentType.getBool((CommandContext)commandContext, (String)"onlyRequiredTests")));
        ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2 = TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("testClassName", TestClassNameArgument.testClassName()), commandContext -> testFinder.allTestsInClass((CommandContext<CommandSourceStack>)commandContext, TestClassNameArgument.getTestClassName((CommandContext<CommandSourceStack>)commandContext, "testClassName")));
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("test").then(Commands.literal("run").then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("testName", TestFunctionArgument.testFunctionArgument()), commandContext -> testFinder.byArgument((CommandContext<CommandSourceStack>)commandContext, "testName"))))).then(Commands.literal("runmultiple").then(((RequiredArgumentBuilder)Commands.argument("testName", TestFunctionArgument.testFunctionArgument()).executes(commandContext -> testFinder.byArgument((CommandContext<CommandSourceStack>)commandContext, "testName").run())).then(Commands.argument("amount", IntegerArgumentType.integer()).executes(commandContext -> testFinder.createMultipleCopies(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount")).byArgument((CommandContext<CommandSourceStack>)commandContext, "testName").run()))))).then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.literal("runall").then(argumentBuilder2), testFinder::allTests))).then(TestCommand.runWithRetryOptions(Commands.literal("runthese"), testFinder::allNearby))).then(TestCommand.runWithRetryOptions(Commands.literal("runclosest"), testFinder::nearest))).then(TestCommand.runWithRetryOptions(Commands.literal("runthat"), testFinder::lookedAt))).then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then(argumentBuilder), testFinder::failedTests))).then(Commands.literal("verify").then(Commands.argument("testName", TestFunctionArgument.testFunctionArgument()).executes(commandContext -> testFinder.byArgument((CommandContext<CommandSourceStack>)commandContext, "testName").verify())))).then(Commands.literal("verifyclass").then(Commands.argument("testClassName", TestClassNameArgument.testClassName()).executes(commandContext -> testFinder.allTestsInClass((CommandContext<CommandSourceStack>)commandContext, TestClassNameArgument.getTestClassName((CommandContext<CommandSourceStack>)commandContext, "testClassName")).verify())))).then(Commands.literal("locate").then(Commands.argument("testName", TestFunctionArgument.testFunctionArgument()).executes(commandContext -> testFinder.locateByName((CommandContext<CommandSourceStack>)commandContext, "minecraft:" + TestFunctionArgument.getTestFunction((CommandContext<CommandSourceStack>)commandContext, "testName").structureName()).locate())))).then(Commands.literal("resetclosest").executes(commandContext -> testFinder.nearest((CommandContext<CommandSourceStack>)commandContext).reset()))).then(Commands.literal("resetthese").executes(commandContext -> testFinder.allNearby((CommandContext<CommandSourceStack>)commandContext).reset()))).then(Commands.literal("resetthat").executes(commandContext -> testFinder.lookedAt((CommandContext<CommandSourceStack>)commandContext).reset()))).then(Commands.literal("export").then(Commands.argument("testName", StringArgumentType.word()).executes(commandContext -> TestCommand.exportTestStructure((CommandSourceStack)commandContext.getSource(), "minecraft:" + StringArgumentType.getString((CommandContext)commandContext, (String)"testName")))))).then(Commands.literal("exportclosest").executes(commandContext -> testFinder.nearest((CommandContext<CommandSourceStack>)commandContext).export()))).then(Commands.literal("exportthese").executes(commandContext -> testFinder.allNearby((CommandContext<CommandSourceStack>)commandContext).export()))).then(Commands.literal("exportthat").executes(commandContext -> testFinder.lookedAt((CommandContext<CommandSourceStack>)commandContext).export()))).then(Commands.literal("clearthat").executes(commandContext -> testFinder.lookedAt((CommandContext<CommandSourceStack>)commandContext).clear()))).then(Commands.literal("clearthese").executes(commandContext -> testFinder.allNearby((CommandContext<CommandSourceStack>)commandContext).clear()))).then(((LiteralArgumentBuilder)Commands.literal("clearall").executes(commandContext -> testFinder.radius((CommandContext<CommandSourceStack>)commandContext, 200).clear())).then(Commands.argument("radius", IntegerArgumentType.integer()).executes(commandContext -> testFinder.radius((CommandContext<CommandSourceStack>)commandContext, Mth.clamp(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"radius"), 0, 1024)).clear())))).then(Commands.literal("import").then(Commands.argument("testName", StringArgumentType.word()).executes(commandContext -> TestCommand.importTestStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"testName")))))).then(Commands.literal("stop").executes(commandContext -> TestCommand.stopTests()))).then(((LiteralArgumentBuilder)Commands.literal("pos").executes(commandContext -> TestCommand.showPos((CommandSourceStack)commandContext.getSource(), "pos"))).then(Commands.argument("var", StringArgumentType.word()).executes(commandContext -> TestCommand.showPos((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"var")))))).then(Commands.literal("create").then(((RequiredArgumentBuilder)Commands.argument("testName", StringArgumentType.word()).suggests(TestFunctionArgument::suggestTestFunction).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"testName"), 5, 5, 5))).then(((RequiredArgumentBuilder)Commands.argument("width", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"testName"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width")))).then(Commands.argument("height", IntegerArgumentType.integer()).then(Commands.argument("depth", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"testName"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"height"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"depth")))))))));
    }

    private static int resetGameTestInfo(GameTestInfo gameTestInfo) {
        gameTestInfo.getLevel().getEntities(null, gameTestInfo.getStructureBounds()).stream().forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        gameTestInfo.getStructureBlockEntity().placeStructure(gameTestInfo.getLevel());
        StructureUtils.removeBarriers(gameTestInfo.getStructureBounds(), gameTestInfo.getLevel());
        TestCommand.say(gameTestInfo.getLevel(), "Reset succeded for: " + gameTestInfo.getTestName(), ChatFormatting.GREEN);
        return 1;
    }

    static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack commandSourceStack, RetryOptions retryOptions, StructureBlockPosFinder structureBlockPosFinder) {
        return structureBlockPosFinder.findStructureBlockPos().map(blockPos -> TestCommand.createGameTestInfo(blockPos, commandSourceStack.getLevel(), retryOptions)).flatMap(Optional::stream);
    }

    static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack commandSourceStack, RetryOptions retryOptions, TestFunctionFinder testFunctionFinder, int n) {
        return testFunctionFinder.findTestFunctions().filter(testFunction -> TestCommand.verifyStructureExists(commandSourceStack.getLevel(), testFunction.structureName())).map(testFunction -> new GameTestInfo((TestFunction)testFunction, StructureUtils.getRotationForRotationSteps(n), commandSourceStack.getLevel(), retryOptions));
    }

    private static Optional<GameTestInfo> createGameTestInfo(BlockPos blockPos, ServerLevel serverLevel, RetryOptions retryOptions) {
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
        if (structureBlockEntity == null) {
            TestCommand.say(serverLevel, STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND, ChatFormatting.RED);
            return Optional.empty();
        }
        String string = structureBlockEntity.getMetaData();
        Optional<TestFunction> optional = GameTestRegistry.findTestFunction(string);
        if (optional.isEmpty()) {
            TestCommand.say(serverLevel, "Test function for test " + string + " could not be found", ChatFormatting.RED);
            return Optional.empty();
        }
        TestFunction testFunction = optional.get();
        GameTestInfo gameTestInfo = new GameTestInfo(testFunction, structureBlockEntity.getRotation(), serverLevel, retryOptions);
        gameTestInfo.setStructureBlockPos(blockPos);
        if (!TestCommand.verifyStructureExists(serverLevel, gameTestInfo.getStructureName())) {
            return Optional.empty();
        }
        return Optional.of(gameTestInfo);
    }

    private static int createNewStructure(CommandSourceStack commandSourceStack, String string, int n, int n2, int n3) {
        if (n > 48 || n2 > 48 || n3 > 48) {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
        ServerLevel serverLevel = commandSourceStack.getLevel();
        BlockPos blockPos2 = TestCommand.createTestPositionAround(commandSourceStack).below();
        StructureUtils.createNewEmptyStructureBlock(string.toLowerCase(), blockPos2, new Vec3i(n, n2, n3), Rotation.NONE, serverLevel);
        BlockPos blockPos3 = blockPos2.above();
        BlockPos blockPos4 = blockPos3.offset(n - 1, 0, n3 - 1);
        BlockPos.betweenClosedStream(blockPos3, blockPos4).forEach(blockPos -> serverLevel.setBlockAndUpdate((BlockPos)blockPos, Blocks.BEDROCK.defaultBlockState()));
        StructureUtils.addCommandBlockAndButtonToStartTest(blockPos2, new BlockPos(1, 0, -1), Rotation.NONE, serverLevel);
        return 0;
    }

    private static int showPos(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
        ServerLevel serverLevel;
        BlockHitResult blockHitResult = (BlockHitResult)commandSourceStack.getPlayerOrException().pick(10.0, 1.0f, false);
        BlockPos blockPos = blockHitResult.getBlockPos();
        Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockPos, 15, serverLevel = commandSourceStack.getLevel());
        if (optional.isEmpty()) {
            optional = StructureUtils.findStructureBlockContainingPos(blockPos, 200, serverLevel);
        }
        if (optional.isEmpty()) {
            commandSourceStack.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + String.valueOf(blockPos)));
            return 0;
        }
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(optional.get());
        if (structureBlockEntity == null) {
            TestCommand.say(serverLevel, STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND, ChatFormatting.RED);
            return 0;
        }
        BlockPos blockPos2 = blockPos.subtract(optional.get());
        String string2 = blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ();
        String string3 = structureBlockEntity.getMetaData();
        MutableComponent mutableComponent = Component.literal(string2).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + string + " = new BlockPos(" + string2 + ");")));
        commandSourceStack.sendSuccess(() -> Component.literal("Position relative to " + string3 + ": ").append(mutableComponent), false);
        DebugPackets.sendGameTestAddMarker(serverLevel, new BlockPos(blockPos), string2, -2147418368, 10000);
        return 1;
    }

    static int stopTests() {
        GameTestTicker.SINGLETON.clear();
        return 1;
    }

    static int trackAndStartRunner(CommandSourceStack commandSourceStack, ServerLevel serverLevel, GameTestRunner gameTestRunner) {
        gameTestRunner.addListener(new TestBatchSummaryDisplayer(commandSourceStack));
        MultipleTestTracker multipleTestTracker = new MultipleTestTracker(gameTestRunner.getTestInfos());
        multipleTestTracker.addListener(new TestSummaryDisplayer(serverLevel, multipleTestTracker));
        multipleTestTracker.addFailureListener(gameTestInfo -> GameTestRegistry.rememberFailedTest(gameTestInfo.getTestFunction()));
        gameTestRunner.start();
        return 1;
    }

    static int saveAndExportTestStructure(CommandSourceStack commandSourceStack, StructureBlockEntity structureBlockEntity) {
        String string = structureBlockEntity.getStructureName();
        if (!structureBlockEntity.saveStructure(true)) {
            TestCommand.say(commandSourceStack, "Failed to save structure " + string);
        }
        return TestCommand.exportTestStructure(commandSourceStack, string);
    }

    private static int exportTestStructure(CommandSourceStack commandSourceStack, String string) {
        Path path = Paths.get(StructureUtils.testStructuresDir, new String[0]);
        ResourceLocation resourceLocation = ResourceLocation.parse(string);
        Path path2 = commandSourceStack.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(resourceLocation, ".nbt");
        Path path3 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path2, resourceLocation.getPath(), path);
        if (path3 == null) {
            TestCommand.say(commandSourceStack, "Failed to export " + String.valueOf(path2));
            return 1;
        }
        try {
            FileUtil.createDirectoriesSafe(path3.getParent());
        }
        catch (IOException iOException) {
            TestCommand.say(commandSourceStack, "Could not create folder " + String.valueOf(path3.getParent()));
            LOGGER.error("Could not create export folder", (Throwable)iOException);
            return 1;
        }
        TestCommand.say(commandSourceStack, "Exported " + string + " to " + String.valueOf(path3.toAbsolutePath()));
        return 0;
    }

    private static boolean verifyStructureExists(ServerLevel serverLevel, String string) {
        if (serverLevel.getStructureManager().get(ResourceLocation.parse(string)).isEmpty()) {
            TestCommand.say(serverLevel, "Test structure " + string + " could not be found", ChatFormatting.RED);
            return false;
        }
        return true;
    }

    static BlockPos createTestPositionAround(CommandSourceStack commandSourceStack) {
        BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
        int n = commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
        return new BlockPos(blockPos.getX(), n + 1, blockPos.getZ() + 3);
    }

    static void say(CommandSourceStack commandSourceStack, String string) {
        commandSourceStack.sendSuccess(() -> Component.literal(string), false);
    }

    private static int importTestStructure(CommandSourceStack commandSourceStack, String string) {
        Path path = Paths.get(StructureUtils.testStructuresDir, string + ".snbt");
        ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace(string);
        Path path2 = commandSourceStack.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(resourceLocation, ".nbt");
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            String string2 = IOUtils.toString((Reader)bufferedReader);
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            try (OutputStream outputStream = Files.newOutputStream(path2, new OpenOption[0]);){
                NbtIo.writeCompressed(NbtUtils.snbtToStructure(string2), outputStream);
            }
            commandSourceStack.getLevel().getStructureManager().remove(resourceLocation);
            TestCommand.say(commandSourceStack, "Imported to " + String.valueOf(path2.toAbsolutePath()));
            return 0;
        }
        catch (CommandSyntaxException | IOException throwable) {
            LOGGER.error("Failed to load structure {}", (Object)string, (Object)throwable);
            return 1;
        }
    }

    static void say(ServerLevel serverLevel, String string, ChatFormatting chatFormatting) {
        serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(string).withStyle(chatFormatting)));
    }

    record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener
    {
        @Override
        public void testBatchStarting(GameTestBatch gameTestBatch) {
            TestCommand.say(this.source, "Starting batch: " + gameTestBatch.name());
        }

        @Override
        public void testBatchFinished(GameTestBatch gameTestBatch) {
        }
    }

    public record TestSummaryDisplayer(ServerLevel level, MultipleTestTracker tracker) implements GameTestListener
    {
        @Override
        public void testStructureLoaded(GameTestInfo gameTestInfo) {
        }

        @Override
        public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
            TestSummaryDisplayer.showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
            TestSummaryDisplayer.showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
            this.tracker.addTestToTrack(gameTestInfo2);
        }

        private static void showTestSummaryIfAllDone(ServerLevel serverLevel, MultipleTestTracker multipleTestTracker) {
            if (multipleTestTracker.isDone()) {
                TestCommand.say(serverLevel, "GameTest done! " + multipleTestTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
                if (multipleTestTracker.hasFailedRequired()) {
                    TestCommand.say(serverLevel, multipleTestTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
                } else {
                    TestCommand.say(serverLevel, "All required tests passed :)", ChatFormatting.GREEN);
                }
                if (multipleTestTracker.hasFailedOptional()) {
                    TestCommand.say(serverLevel, multipleTestTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
                }
            }
        }
    }

    public static class Runner {
        private final TestFinder<Runner> finder;

        public Runner(TestFinder<Runner> testFinder) {
            this.finder = testFinder;
        }

        public int reset() {
            TestCommand.stopTests();
            return TestCommand.toGameTestInfos(this.finder.source(), RetryOptions.noRetries(), this.finder).map(TestCommand::resetGameTestInfo).toList().isEmpty() ? 0 : 1;
        }

        private <T> void logAndRun(Stream<T> stream, ToIntFunction<T> toIntFunction, Runnable runnable, Consumer<Integer> consumer) {
            int n = stream.mapToInt(toIntFunction).sum();
            if (n == 0) {
                runnable.run();
            } else {
                consumer.accept(n);
            }
        }

        public int clear() {
            TestCommand.stopTests();
            CommandSourceStack commandSourceStack = this.finder.source();
            ServerLevel serverLevel = commandSourceStack.getLevel();
            GameTestRunner.clearMarkers(serverLevel);
            this.logAndRun(this.finder.findStructureBlockPos(), blockPos -> {
                StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity((BlockPos)blockPos);
                if (structureBlockEntity == null) {
                    return 0;
                }
                BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(structureBlockEntity);
                StructureUtils.clearSpaceForStructure(boundingBox, serverLevel);
                return 1;
            }, () -> TestCommand.say(serverLevel, "Could not find any structures to clear", ChatFormatting.RED), n -> TestCommand.say(commandSourceStack, "Cleared " + n + " structures"));
            return 1;
        }

        public int export() {
            MutableBoolean mutableBoolean = new MutableBoolean(true);
            CommandSourceStack commandSourceStack = this.finder.source();
            ServerLevel serverLevel = commandSourceStack.getLevel();
            this.logAndRun(this.finder.findStructureBlockPos(), blockPos -> {
                StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity((BlockPos)blockPos);
                if (structureBlockEntity == null) {
                    TestCommand.say(serverLevel, TestCommand.STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND, ChatFormatting.RED);
                    mutableBoolean.setFalse();
                    return 0;
                }
                if (TestCommand.saveAndExportTestStructure(commandSourceStack, structureBlockEntity) != 0) {
                    mutableBoolean.setFalse();
                }
                return 1;
            }, () -> TestCommand.say(serverLevel, "Could not find any structures to export", ChatFormatting.RED), n -> TestCommand.say(commandSourceStack, "Exported " + n + " structures"));
            return mutableBoolean.getValue() != false ? 0 : 1;
        }

        int verify() {
            TestCommand.stopTests();
            CommandSourceStack commandSourceStack = this.finder.source();
            ServerLevel serverLevel = commandSourceStack.getLevel();
            BlockPos blockPos = TestCommand.createTestPositionAround(commandSourceStack);
            List<GameTestInfo> list = Stream.concat(TestCommand.toGameTestInfos(commandSourceStack, RetryOptions.noRetries(), this.finder), TestCommand.toGameTestInfo(commandSourceStack, RetryOptions.noRetries(), this.finder, 0)).toList();
            int n = 10;
            GameTestRunner.clearMarkers(serverLevel);
            GameTestRegistry.forgetFailedTests();
            ArrayList<GameTestBatch> arrayList = new ArrayList<GameTestBatch>();
            for (GameTestInfo object2 : list) {
                for (Rotation rotation : Rotation.values()) {
                    ArrayList<GameTestInfo> arrayList2 = new ArrayList<GameTestInfo>();
                    for (int i = 0; i < 100; ++i) {
                        GameTestInfo gameTestInfo = new GameTestInfo(object2.getTestFunction(), rotation, serverLevel, new RetryOptions(1, true));
                        arrayList2.add(gameTestInfo);
                    }
                    GameTestBatch i = GameTestBatchFactory.toGameTestBatch(arrayList2, object2.getTestFunction().batchName(), rotation.ordinal());
                    arrayList.add(i);
                }
            }
            StructureGridSpawner structureGridSpawner = new StructureGridSpawner(blockPos, 10, true);
            GameTestRunner gameTestRunner = GameTestRunner.Builder.fromBatches(arrayList, serverLevel).batcher(GameTestBatchFactory.fromGameTestInfo(100)).newStructureSpawner(structureGridSpawner).existingStructureSpawner(structureGridSpawner).haltOnError(true).build();
            return TestCommand.trackAndStartRunner(commandSourceStack, serverLevel, gameTestRunner);
        }

        public int run(RetryOptions retryOptions, int n, int n2) {
            TestCommand.stopTests();
            CommandSourceStack commandSourceStack = this.finder.source();
            ServerLevel serverLevel = commandSourceStack.getLevel();
            BlockPos blockPos = TestCommand.createTestPositionAround(commandSourceStack);
            List<GameTestInfo> list = Stream.concat(TestCommand.toGameTestInfos(commandSourceStack, retryOptions, this.finder), TestCommand.toGameTestInfo(commandSourceStack, retryOptions, this.finder, n)).toList();
            if (list.isEmpty()) {
                TestCommand.say(commandSourceStack, "No tests found");
                return 0;
            }
            GameTestRunner.clearMarkers(serverLevel);
            GameTestRegistry.forgetFailedTests();
            TestCommand.say(commandSourceStack, "Running " + list.size() + " tests...");
            GameTestRunner gameTestRunner = GameTestRunner.Builder.fromInfo(list, serverLevel).newStructureSpawner(new StructureGridSpawner(blockPos, n2, false)).build();
            return TestCommand.trackAndStartRunner(commandSourceStack, serverLevel, gameTestRunner);
        }

        public int run(int n, int n2) {
            return this.run(RetryOptions.noRetries(), n, n2);
        }

        public int run(int n) {
            return this.run(RetryOptions.noRetries(), n, 8);
        }

        public int run(RetryOptions retryOptions, int n) {
            return this.run(retryOptions, n, 8);
        }

        public int run(RetryOptions retryOptions) {
            return this.run(retryOptions, 0, 8);
        }

        public int run() {
            return this.run(RetryOptions.noRetries());
        }

        public int locate() {
            TestCommand.say(this.finder.source(), "Started locating test structures, this might take a while..");
            MutableInt mutableInt = new MutableInt(0);
            BlockPos blockPos = BlockPos.containing(this.finder.source().getPosition());
            this.finder.findStructureBlockPos().forEach(blockPos2 -> {
                StructureBlockEntity structureBlockEntity = (StructureBlockEntity)this.finder.source().getLevel().getBlockEntity((BlockPos)blockPos2);
                if (structureBlockEntity == null) {
                    return;
                }
                Direction direction = structureBlockEntity.getRotation().rotate(Direction.NORTH);
                BlockPos blockPos3 = structureBlockEntity.getBlockPos().relative(direction, 2);
                int n = (int)direction.getOpposite().toYRot();
                String string = String.format("/tp @s %d %d %d %d 0", blockPos3.getX(), blockPos3.getY(), blockPos3.getZ(), n);
                int n2 = blockPos.getX() - blockPos2.getX();
                int n3 = blockPos.getZ() - blockPos2.getZ();
                int n4 = Mth.floor(Mth.sqrt(n2 * n2 + n3 * n3));
                MutableComponent mutableComponent = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockPos2.getX(), blockPos2.getY(), blockPos2.getZ())).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, string)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
                MutableComponent mutableComponent2 = Component.literal("Found structure at: ").append(mutableComponent).append(" (distance: " + n4 + ")");
                this.finder.source().sendSuccess(() -> mutableComponent2, false);
                mutableInt.increment();
            });
            int n = mutableInt.intValue();
            if (n == 0) {
                TestCommand.say(this.finder.source().getLevel(), "No such test structure found", ChatFormatting.RED);
                return 0;
            }
            TestCommand.say(this.finder.source().getLevel(), "Finished locating, found " + n + " structure(s)", ChatFormatting.GREEN);
            return 1;
        }
    }
}

