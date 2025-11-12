/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class MsgCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralCommandNode literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("msg").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes(commandContext -> {
            Collection<ServerPlayer> collection = EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets");
            if (!collection.isEmpty()) {
                MessageArgument.resolveChatMessage((CommandContext<CommandSourceStack>)commandContext, "message", playerChatMessage -> MsgCommand.sendMessage((CommandSourceStack)commandContext.getSource(), collection, playerChatMessage));
            }
            return collection.size();
        }))));
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("tell").redirect((CommandNode)literalCommandNode));
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("w").redirect((CommandNode)literalCommandNode));
    }

    private static void sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, PlayerChatMessage playerChatMessage) {
        ChatType.Bound bound = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, commandSourceStack);
        OutgoingChatMessage outgoingChatMessage = OutgoingChatMessage.create(playerChatMessage);
        boolean bl = false;
        for (ServerPlayer serverPlayer : collection) {
            ChatType.Bound bound2 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, commandSourceStack).withTargetName(serverPlayer.getDisplayName());
            commandSourceStack.sendChatMessage(outgoingChatMessage, false, bound2);
            boolean bl2 = commandSourceStack.shouldFilterMessageTo(serverPlayer);
            serverPlayer.sendChatMessage(outgoingChatMessage, bl2, bound);
            bl |= bl2 && playerChatMessage.isFullyFiltered();
        }
        if (bl) {
            commandSourceStack.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }
    }
}

