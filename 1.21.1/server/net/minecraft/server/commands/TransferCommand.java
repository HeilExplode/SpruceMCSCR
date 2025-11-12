/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.server.level.ServerPlayer;

public class TransferCommand {
    private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType((Message)Component.translatable("commands.transfer.error.no_players"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("transfer").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(((RequiredArgumentBuilder)Commands.argument("hostname", StringArgumentType.string()).executes(commandContext -> TransferCommand.transfer((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"hostname"), 25565, List.of(((CommandSourceStack)commandContext.getSource()).getPlayerOrException())))).then(((RequiredArgumentBuilder)Commands.argument("port", IntegerArgumentType.integer((int)1, (int)65535)).executes(commandContext -> TransferCommand.transfer((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"hostname"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"port"), List.of(((CommandSourceStack)commandContext.getSource()).getPlayerOrException())))).then(Commands.argument("players", EntityArgument.players()).executes(commandContext -> TransferCommand.transfer((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"hostname"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"port"), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "players")))))));
    }

    private static int transfer(CommandSourceStack commandSourceStack, String string, int n, Collection<ServerPlayer> collection) throws CommandSyntaxException {
        if (collection.isEmpty()) {
            throw ERROR_NO_PLAYERS.create();
        }
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.connection.send(new ClientboundTransferPacket(string, n));
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.transfer.success.single", ((ServerPlayer)collection.iterator().next()).getDisplayName(), string, n), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.transfer.success.multiple", collection.size(), string, n), true);
        }
        return collection.size();
    }
}

