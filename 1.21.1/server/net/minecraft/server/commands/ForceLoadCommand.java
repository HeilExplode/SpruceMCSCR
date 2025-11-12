/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package net.minecraft.server.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceLoadCommand {
    private static final int MAX_CHUNK_LIMIT = 256;
    private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.forceload.toobig", object, object2));
    private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.forceload.query.failure", object, object2));
    private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType((Message)Component.translatable("commands.forceload.added.failure"));
    private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType((Message)Component.translatable("commands.forceload.removed.failure"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("forceload").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("add").then(((RequiredArgumentBuilder)Commands.argument("from", ColumnPosArgument.columnPos()).executes(commandContext -> ForceLoadCommand.changeForceLoad((CommandSourceStack)commandContext.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "from"), true))).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes(commandContext -> ForceLoadCommand.changeForceLoad((CommandSourceStack)commandContext.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "to"), true)))))).then(((LiteralArgumentBuilder)Commands.literal("remove").then(((RequiredArgumentBuilder)Commands.argument("from", ColumnPosArgument.columnPos()).executes(commandContext -> ForceLoadCommand.changeForceLoad((CommandSourceStack)commandContext.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "from"), false))).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes(commandContext -> ForceLoadCommand.changeForceLoad((CommandSourceStack)commandContext.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "to"), false))))).then(Commands.literal("all").executes(commandContext -> ForceLoadCommand.removeAll((CommandSourceStack)commandContext.getSource()))))).then(((LiteralArgumentBuilder)Commands.literal("query").executes(commandContext -> ForceLoadCommand.listForceLoad((CommandSourceStack)commandContext.getSource()))).then(Commands.argument("pos", ColumnPosArgument.columnPos()).executes(commandContext -> ForceLoadCommand.queryForceLoad((CommandSourceStack)commandContext.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)commandContext, "pos"))))));
    }

    private static int queryForceLoad(CommandSourceStack commandSourceStack, ColumnPos columnPos) throws CommandSyntaxException {
        ChunkPos chunkPos = columnPos.toChunkPos();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ResourceKey<Level> resourceKey = serverLevel.dimension();
        boolean bl = serverLevel.getForcedChunks().contains(chunkPos.toLong());
        if (bl) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.forceload.query.success", Component.translationArg(chunkPos), Component.translationArg(resourceKey.location())), false);
            return 1;
        }
        throw ERROR_NOT_TICKING.create((Object)chunkPos, (Object)resourceKey.location());
    }

    private static int listForceLoad(CommandSourceStack commandSourceStack) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ResourceKey<Level> resourceKey = serverLevel.dimension();
        LongSet longSet = serverLevel.getForcedChunks();
        int n = longSet.size();
        if (n > 0) {
            String string = Joiner.on((String)", ").join(longSet.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
            if (n == 1) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.forceload.list.single", Component.translationArg(resourceKey.location()), string), false);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.forceload.list.multiple", n, Component.translationArg(resourceKey.location()), string), false);
            }
        } else {
            commandSourceStack.sendFailure(Component.translatable("commands.forceload.added.none", Component.translationArg(resourceKey.location())));
        }
        return n;
    }

    private static int removeAll(CommandSourceStack commandSourceStack) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ResourceKey<Level> resourceKey = serverLevel.dimension();
        LongSet longSet = serverLevel.getForcedChunks();
        longSet.forEach(l -> serverLevel.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false));
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.forceload.removed.all", Component.translationArg(resourceKey.location())), true);
        return 0;
    }

    private static int changeForceLoad(CommandSourceStack commandSourceStack, ColumnPos columnPos, ColumnPos columnPos2, boolean bl) throws CommandSyntaxException {
        int n;
        int n2;
        int n3 = Math.min(columnPos.x(), columnPos2.x());
        int n4 = Math.min(columnPos.z(), columnPos2.z());
        int n5 = Math.max(columnPos.x(), columnPos2.x());
        int n6 = Math.max(columnPos.z(), columnPos2.z());
        if (n3 < -30000000 || n4 < -30000000 || n5 >= 30000000 || n6 >= 30000000) {
            throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
        }
        int n7 = SectionPos.blockToSectionCoord(n3);
        int n8 = SectionPos.blockToSectionCoord(n4);
        int n9 = SectionPos.blockToSectionCoord(n5);
        long l = ((long)(n9 - n7) + 1L) * ((long)((n2 = SectionPos.blockToSectionCoord(n6)) - n8) + 1L);
        if (l > 256L) {
            throw ERROR_TOO_MANY_CHUNKS.create((Object)256, (Object)l);
        }
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ResourceKey<Level> resourceKey = serverLevel.dimension();
        ChunkPos chunkPos = null;
        int n10 = 0;
        for (int i = n7; i <= n9; ++i) {
            for (n = n8; n <= n2; ++n) {
                boolean bl2 = serverLevel.setChunkForced(i, n, bl);
                if (!bl2) continue;
                ++n10;
                if (chunkPos != null) continue;
                chunkPos = new ChunkPos(i, n);
            }
        }
        ChunkPos chunkPos2 = chunkPos;
        n = n10;
        if (n == 0) {
            throw (bl ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
        }
        if (n == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.forceload." + (bl ? "added" : "removed") + ".single", Component.translationArg(chunkPos2), Component.translationArg(resourceKey.location())), true);
        } else {
            ChunkPos chunkPos3 = new ChunkPos(n7, n8);
            ChunkPos chunkPos4 = new ChunkPos(n9, n2);
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.forceload." + (bl ? "added" : "removed") + ".multiple", n, Component.translationArg(resourceKey.location()), Component.translationArg(chunkPos3), Component.translationArg(chunkPos4)), true);
        }
        return n;
    }
}

