/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  org.apache.commons.lang3.exception.ExceptionUtils
 */
package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.ExhaustedAttemptsException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener
implements GameTestListener {
    private int attempts = 0;
    private int successes = 0;

    @Override
    public void testStructureLoaded(GameTestInfo gameTestInfo) {
        ReportGameListener.spawnBeacon(gameTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
        ++this.attempts;
    }

    private void handleRetry(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner, boolean bl) {
        RetryOptions retryOptions = gameTestInfo.retryOptions();
        Object object = String.format("[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);
        if (!retryOptions.unlimitedTries()) {
            object = (String)object + String.format(", Left: %4d", retryOptions.numberOfTries() - this.attempts);
        }
        object = (String)object + "]";
        String string = gameTestInfo.getTestName() + " " + (bl ? "passed" : "failed") + "! " + gameTestInfo.getRunTime() + "ms";
        String string2 = String.format("%-53s%s", object, string);
        if (bl) {
            ReportGameListener.reportPassed(gameTestInfo, string2);
        } else {
            ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.RED, string2);
        }
        if (retryOptions.hasTriesLeft(this.attempts, this.successes)) {
            gameTestRunner.rerunTest(gameTestInfo);
        }
    }

    @Override
    public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
        ++this.successes;
        if (gameTestInfo.retryOptions().hasRetries()) {
            this.handleRetry(gameTestInfo, gameTestRunner, true);
            return;
        }
        if (!gameTestInfo.isFlaky()) {
            ReportGameListener.reportPassed(gameTestInfo, gameTestInfo.getTestName() + " passed! (" + gameTestInfo.getRunTime() + "ms)");
            return;
        }
        if (this.successes >= gameTestInfo.requiredSuccesses()) {
            ReportGameListener.reportPassed(gameTestInfo, String.valueOf(gameTestInfo) + " passed " + this.successes + " times of " + this.attempts + " attempts.");
        } else {
            ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.GREEN, "Flaky test " + String.valueOf(gameTestInfo) + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
            gameTestRunner.rerunTest(gameTestInfo);
        }
    }

    @Override
    public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
        if (!gameTestInfo.isFlaky()) {
            ReportGameListener.reportFailure(gameTestInfo, gameTestInfo.getError());
            if (gameTestInfo.retryOptions().hasRetries()) {
                this.handleRetry(gameTestInfo, gameTestRunner, false);
            }
            return;
        }
        TestFunction testFunction = gameTestInfo.getTestFunction();
        String string = "Flaky test " + String.valueOf(gameTestInfo) + " failed, attempt: " + this.attempts + "/" + testFunction.maxAttempts();
        if (testFunction.requiredSuccesses() > 1) {
            string = string + ", successes: " + this.successes + " (" + testFunction.requiredSuccesses() + " required)";
        }
        ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.YELLOW, string);
        if (gameTestInfo.maxAttempts() - this.attempts + this.successes >= gameTestInfo.requiredSuccesses()) {
            gameTestRunner.rerunTest(gameTestInfo);
        } else {
            ReportGameListener.reportFailure(gameTestInfo, new ExhaustedAttemptsException(this.attempts, this.successes, gameTestInfo));
        }
    }

    @Override
    public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
        gameTestInfo2.addListener(this);
    }

    public static void reportPassed(GameTestInfo gameTestInfo, String string) {
        ReportGameListener.updateBeaconGlass(gameTestInfo, Blocks.LIME_STAINED_GLASS);
        ReportGameListener.visualizePassedTest(gameTestInfo, string);
    }

    private static void visualizePassedTest(GameTestInfo gameTestInfo, String string) {
        ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.GREEN, string);
        GlobalTestReporter.onTestSuccess(gameTestInfo);
    }

    protected static void reportFailure(GameTestInfo gameTestInfo, Throwable throwable) {
        ReportGameListener.updateBeaconGlass(gameTestInfo, gameTestInfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
        ReportGameListener.spawnLectern(gameTestInfo, Util.describeError(throwable));
        ReportGameListener.visualizeFailedTest(gameTestInfo, throwable);
    }

    protected static void visualizeFailedTest(GameTestInfo gameTestInfo, Throwable throwable) {
        String string = throwable.getMessage() + (String)(throwable.getCause() == null ? "" : " cause: " + Util.describeError(throwable.getCause()));
        String string2 = (gameTestInfo.isRequired() ? "" : "(optional) ") + gameTestInfo.getTestName() + " failed! " + string;
        ReportGameListener.say(gameTestInfo.getLevel(), gameTestInfo.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, string2);
        Throwable throwable2 = (Throwable)MoreObjects.firstNonNull((Object)ExceptionUtils.getRootCause((Throwable)throwable), (Object)throwable);
        if (throwable2 instanceof GameTestAssertPosException) {
            GameTestAssertPosException gameTestAssertPosException = (GameTestAssertPosException)throwable2;
            ReportGameListener.showRedBox(gameTestInfo.getLevel(), gameTestAssertPosException.getAbsolutePos(), gameTestAssertPosException.getMessageToShowAtBlock());
        }
        GlobalTestReporter.onTestFailed(gameTestInfo);
    }

    protected static void spawnBeacon(GameTestInfo gameTestInfo, Block block) {
        ServerLevel serverLevel = gameTestInfo.getLevel();
        BlockPos blockPos = ReportGameListener.getBeaconPos(gameTestInfo);
        serverLevel.setBlockAndUpdate(blockPos, Blocks.BEACON.defaultBlockState().rotate(gameTestInfo.getRotation()));
        ReportGameListener.updateBeaconGlass(gameTestInfo, block);
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                BlockPos blockPos2 = blockPos.offset(i, -1, j);
                serverLevel.setBlockAndUpdate(blockPos2, Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
    }

    private static BlockPos getBeaconPos(GameTestInfo gameTestInfo) {
        BlockPos blockPos = gameTestInfo.getStructureBlockPos();
        BlockPos blockPos2 = new BlockPos(-1, -2, -1);
        return StructureTemplate.transform(blockPos.offset(blockPos2), Mirror.NONE, gameTestInfo.getRotation(), blockPos);
    }

    private static void updateBeaconGlass(GameTestInfo gameTestInfo, Block block) {
        BlockPos blockPos;
        ServerLevel serverLevel = gameTestInfo.getLevel();
        if (serverLevel.getBlockState(blockPos = ReportGameListener.getBeaconPos(gameTestInfo)).is(Blocks.BEACON)) {
            BlockPos blockPos2 = blockPos.offset(0, 1, 0);
            serverLevel.setBlockAndUpdate(blockPos2, block.defaultBlockState());
        }
    }

    private static void spawnLectern(GameTestInfo gameTestInfo, String string) {
        ServerLevel serverLevel = gameTestInfo.getLevel();
        BlockPos blockPos = gameTestInfo.getStructureBlockPos();
        BlockPos blockPos2 = new BlockPos(-1, 0, -1);
        BlockPos blockPos3 = StructureTemplate.transform(blockPos.offset(blockPos2), Mirror.NONE, gameTestInfo.getRotation(), blockPos);
        serverLevel.setBlockAndUpdate(blockPos3, Blocks.LECTERN.defaultBlockState().rotate(gameTestInfo.getRotation()));
        BlockState blockState = serverLevel.getBlockState(blockPos3);
        ItemStack itemStack = ReportGameListener.createBook(gameTestInfo.getTestName(), gameTestInfo.isRequired(), string);
        LecternBlock.tryPlaceBook(null, serverLevel, blockPos3, blockState, itemStack);
    }

    private static ItemStack createBook(String string2, boolean bl, String string3) {
        StringBuffer stringBuffer = new StringBuffer();
        Arrays.stream(string2.split("\\.")).forEach(string -> stringBuffer.append((String)string).append('\n'));
        if (!bl) {
            stringBuffer.append("(optional)\n");
        }
        stringBuffer.append("-------------------\n");
        ItemStack itemStack = new ItemStack(Items.WRITABLE_BOOK);
        itemStack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(List.of(Filterable.passThrough(String.valueOf(stringBuffer) + string3))));
        return itemStack;
    }

    protected static void say(ServerLevel serverLevel, ChatFormatting chatFormatting, String string) {
        serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(string).withStyle(chatFormatting)));
    }

    private static void showRedBox(ServerLevel serverLevel, BlockPos blockPos, String string) {
        DebugPackets.sendGameTestAddMarker(serverLevel, blockPos, string, -2130771968, Integer.MAX_VALUE);
    }
}

