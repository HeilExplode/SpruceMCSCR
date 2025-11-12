/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FillCommand {
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.fill.toobig", object, object2));
    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fill").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("block", BlockStateArgument.block(commandBuildContext)).executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.REPLACE, null))).then(((LiteralArgumentBuilder)Commands.literal("replace").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.REPLACE, null))).then(Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandBuildContext)).executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.REPLACE, BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)commandContext, "filter")))))).then(Commands.literal("keep").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.REPLACE, blockInWorld -> blockInWorld.getLevel().isEmptyBlock(blockInWorld.getPos()))))).then(Commands.literal("outline").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.OUTLINE, null)))).then(Commands.literal("hollow").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.HOLLOW, null)))).then(Commands.literal("destroy").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.DESTROY, null)))))));
    }

    private static int fillBlocks(CommandSourceStack commandSourceStack, BoundingBox boundingBox, BlockInput blockInput, Mode mode, @Nullable Predicate<BlockInWorld> predicate) throws CommandSyntaxException {
        Object object;
        int n;
        int n2 = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (n2 > (n = commandSourceStack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            throw ERROR_AREA_TOO_LARGE.create((Object)n, (Object)n2);
        }
        ArrayList arrayList = Lists.newArrayList();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        int n3 = 0;
        for (BlockPos blockPos : BlockPos.betweenClosed(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ())) {
            if (predicate != null && !predicate.test(new BlockInWorld(serverLevel, blockPos, true)) || (object = mode.filter.filter(boundingBox, blockPos, blockInput, serverLevel)) == null) continue;
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            Clearable.tryClear(blockEntity);
            if (!((BlockInput)object).place(serverLevel, blockPos, 2)) continue;
            arrayList.add(blockPos.immutable());
            ++n3;
        }
        for (BlockPos blockPos : arrayList) {
            object = serverLevel.getBlockState(blockPos).getBlock();
            serverLevel.blockUpdated(blockPos, (Block)object);
        }
        if (n3 == 0) {
            throw ERROR_FAILED.create();
        }
        int n4 = n3;
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.fill.success", n4), true);
        return n3;
    }

    static enum Mode {
        REPLACE((boundingBox, blockPos, blockInput, serverLevel) -> blockInput),
        OUTLINE((boundingBox, blockPos, blockInput, serverLevel) -> {
            if (blockPos.getX() == boundingBox.minX() || blockPos.getX() == boundingBox.maxX() || blockPos.getY() == boundingBox.minY() || blockPos.getY() == boundingBox.maxY() || blockPos.getZ() == boundingBox.minZ() || blockPos.getZ() == boundingBox.maxZ()) {
                return blockInput;
            }
            return null;
        }),
        HOLLOW((boundingBox, blockPos, blockInput, serverLevel) -> {
            if (blockPos.getX() == boundingBox.minX() || blockPos.getX() == boundingBox.maxX() || blockPos.getY() == boundingBox.minY() || blockPos.getY() == boundingBox.maxY() || blockPos.getZ() == boundingBox.minZ() || blockPos.getZ() == boundingBox.maxZ()) {
                return blockInput;
            }
            return HOLLOW_CORE;
        }),
        DESTROY((boundingBox, blockPos, blockInput, serverLevel) -> {
            serverLevel.destroyBlock(blockPos, true);
            return blockInput;
        });

        public final SetBlockCommand.Filter filter;

        private Mode(SetBlockCommand.Filter filter) {
            this.filter = filter;
        }
    }
}

