/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class RideCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_RIDING = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.ride.not_riding", object));
    private static final Dynamic2CommandExceptionType ERROR_ALREADY_RIDING = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.ride.already_riding", object, object2));
    private static final Dynamic2CommandExceptionType ERROR_MOUNT_FAILED = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.ride.mount.failure.generic", object, object2));
    private static final SimpleCommandExceptionType ERROR_MOUNTING_PLAYER = new SimpleCommandExceptionType((Message)Component.translatable("commands.ride.mount.failure.cant_ride_players"));
    private static final SimpleCommandExceptionType ERROR_MOUNTING_LOOP = new SimpleCommandExceptionType((Message)Component.translatable("commands.ride.mount.failure.loop"));
    private static final SimpleCommandExceptionType ERROR_WRONG_DIMENSION = new SimpleCommandExceptionType((Message)Component.translatable("commands.ride.mount.failure.wrong_dimension"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ride").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity()).then(Commands.literal("mount").then(Commands.argument("vehicle", EntityArgument.entity()).executes(commandContext -> RideCommand.mount((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "vehicle")))))).then(Commands.literal("dismount").executes(commandContext -> RideCommand.dismount((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"))))));
    }

    private static int mount(CommandSourceStack commandSourceStack, Entity entity, Entity entity3) throws CommandSyntaxException {
        Entity entity4 = entity.getVehicle();
        if (entity4 != null) {
            throw ERROR_ALREADY_RIDING.create((Object)entity.getDisplayName(), (Object)entity4.getDisplayName());
        }
        if (entity3.getType() == EntityType.PLAYER) {
            throw ERROR_MOUNTING_PLAYER.create();
        }
        if (entity.getSelfAndPassengers().anyMatch(entity2 -> entity2 == entity3)) {
            throw ERROR_MOUNTING_LOOP.create();
        }
        if (entity.level() != entity3.level()) {
            throw ERROR_WRONG_DIMENSION.create();
        }
        if (!entity.startRiding(entity3, true)) {
            throw ERROR_MOUNT_FAILED.create((Object)entity.getDisplayName(), (Object)entity3.getDisplayName());
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.ride.mount.success", entity.getDisplayName(), entity3.getDisplayName()), true);
        return 1;
    }

    private static int dismount(CommandSourceStack commandSourceStack, Entity entity) throws CommandSyntaxException {
        Entity entity2 = entity.getVehicle();
        if (entity2 == null) {
            throw ERROR_NOT_RIDING.create((Object)entity.getDisplayName());
        }
        entity.stopRiding();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.ride.dismount.success", entity.getDisplayName(), entity2.getDisplayName()), true);
        return 1;
    }
}

