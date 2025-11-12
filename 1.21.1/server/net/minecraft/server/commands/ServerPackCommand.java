/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;

public class ServerPackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("serverpack").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("push").then(((RequiredArgumentBuilder)Commands.argument("url", StringArgumentType.string()).then(((RequiredArgumentBuilder)Commands.argument("uuid", UuidArgument.uuid()).then(Commands.argument("hash", StringArgumentType.word()).executes(commandContext -> ServerPackCommand.pushPack((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"url"), Optional.of(UuidArgument.getUuid((CommandContext<CommandSourceStack>)commandContext, "uuid")), Optional.of(StringArgumentType.getString((CommandContext)commandContext, (String)"hash")))))).executes(commandContext -> ServerPackCommand.pushPack((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"url"), Optional.of(UuidArgument.getUuid((CommandContext<CommandSourceStack>)commandContext, "uuid")), Optional.empty())))).executes(commandContext -> ServerPackCommand.pushPack((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"url"), Optional.empty(), Optional.empty()))))).then(Commands.literal("pop").then(Commands.argument("uuid", UuidArgument.uuid()).executes(commandContext -> ServerPackCommand.popPack((CommandSourceStack)commandContext.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)commandContext, "uuid"))))));
    }

    private static void sendToAllConnections(CommandSourceStack commandSourceStack, Packet<?> packet) {
        commandSourceStack.getServer().getConnection().getConnections().forEach(connection -> connection.send(packet));
    }

    private static int pushPack(CommandSourceStack commandSourceStack, String string, Optional<UUID> optional, Optional<String> optional2) {
        UUID uUID = optional.orElseGet(() -> UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8)));
        String string2 = optional2.orElse("");
        ClientboundResourcePackPushPacket clientboundResourcePackPushPacket = new ClientboundResourcePackPushPacket(uUID, string, string2, false, null);
        ServerPackCommand.sendToAllConnections(commandSourceStack, clientboundResourcePackPushPacket);
        return 0;
    }

    private static int popPack(CommandSourceStack commandSourceStack, UUID uUID) {
        ClientboundResourcePackPopPacket clientboundResourcePackPopPacket = new ClientboundResourcePackPopPacket(Optional.of(uUID));
        ServerPackCommand.sendToAllConnections(commandSourceStack, clientboundResourcePackPopPacket);
        return 0;
    }
}

