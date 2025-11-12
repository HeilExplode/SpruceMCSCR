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
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType((Message)Component.translatable("commands.teleport.invalidPosition"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralCommandNode literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("teleport").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("location", Vec3Argument.vec3()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), Collections.singleton(((CommandSourceStack)commandContext.getSource()).getEntityOrException()), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)commandContext, "location"), WorldCoordinates.current(), null)))).then(Commands.argument("destination", EntityArgument.entity()).executes(commandContext -> TeleportCommand.teleportToEntity((CommandSourceStack)commandContext.getSource(), Collections.singleton(((CommandSourceStack)commandContext.getSource()).getEntityOrException()), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "destination"))))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("location", Vec3Argument.vec3()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)commandContext, "location"), null, null))).then(Commands.argument("rotation", RotationArgument.rotation()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)commandContext, "location"), RotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rotation"), null)))).then(((LiteralArgumentBuilder)Commands.literal("facing").then(Commands.literal("entity").then(((RequiredArgumentBuilder)Commands.argument("facingEntity", EntityArgument.entity()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)commandContext, "location"), null, new LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "facingEntity"), EntityAnchorArgument.Anchor.FEET)))).then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)commandContext, "location"), null, new LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "facingEntity"), EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)commandContext, "facingAnchor")))))))).then(Commands.argument("facingLocation", Vec3Argument.vec3()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)commandContext, "location"), null, new LookAtPosition(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "facingLocation")))))))).then(Commands.argument("destination", EntityArgument.entity()).executes(commandContext -> TeleportCommand.teleportToEntity((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "destination"))))));
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tp").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).redirect((CommandNode)literalCommandNode));
    }

    private static int teleportToEntity(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Entity entity) throws CommandSyntaxException {
        for (Entity entity2 : collection) {
            TeleportCommand.performTeleport(commandSourceStack, entity2, (ServerLevel)entity.level(), entity.getX(), entity.getY(), entity.getZ(), EnumSet.noneOf(RelativeMovement.class), entity.getYRot(), entity.getXRot(), null);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.single", ((Entity)collection.iterator().next()).getDisplayName(), entity.getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", collection.size(), entity.getDisplayName()), true);
        }
        return collection.size();
    }

    private static int teleportToPos(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, ServerLevel serverLevel, Coordinates coordinates, @Nullable Coordinates coordinates2, @Nullable LookAt lookAt) throws CommandSyntaxException {
        Vec3 vec3 = coordinates.getPosition(commandSourceStack);
        Vec2 vec2 = coordinates2 == null ? null : coordinates2.getRotation(commandSourceStack);
        EnumSet<RelativeMovement> enumSet = EnumSet.noneOf(RelativeMovement.class);
        if (coordinates.isXRelative()) {
            enumSet.add(RelativeMovement.X);
        }
        if (coordinates.isYRelative()) {
            enumSet.add(RelativeMovement.Y);
        }
        if (coordinates.isZRelative()) {
            enumSet.add(RelativeMovement.Z);
        }
        if (coordinates2 == null) {
            enumSet.add(RelativeMovement.X_ROT);
            enumSet.add(RelativeMovement.Y_ROT);
        } else {
            if (coordinates2.isXRelative()) {
                enumSet.add(RelativeMovement.X_ROT);
            }
            if (coordinates2.isYRelative()) {
                enumSet.add(RelativeMovement.Y_ROT);
            }
        }
        for (Entity entity : collection) {
            if (coordinates2 == null) {
                TeleportCommand.performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, enumSet, entity.getYRot(), entity.getXRot(), lookAt);
                continue;
            }
            TeleportCommand.performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, enumSet, vec2.y, vec2.x, lookAt);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single", ((Entity)collection.iterator().next()).getDisplayName(), TeleportCommand.formatDouble(vec3.x), TeleportCommand.formatDouble(vec3.y), TeleportCommand.formatDouble(vec3.z)), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple", collection.size(), TeleportCommand.formatDouble(vec3.x), TeleportCommand.formatDouble(vec3.y), TeleportCommand.formatDouble(vec3.z)), true);
        }
        return collection.size();
    }

    private static String formatDouble(double d) {
        return String.format(Locale.ROOT, "%f", d);
    }

    private static void performTeleport(CommandSourceStack commandSourceStack, Entity entity, ServerLevel serverLevel, double d, double d2, double d3, Set<RelativeMovement> set, float f, float f2, @Nullable LookAt lookAt) throws CommandSyntaxException {
        LivingEntity livingEntity;
        float f3;
        BlockPos blockPos = BlockPos.containing(d, d2, d3);
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw INVALID_POSITION.create();
        }
        float f4 = Mth.wrapDegrees(f);
        if (!entity.teleportTo(serverLevel, d, d2, d3, set, f4, f3 = Mth.wrapDegrees(f2))) {
            return;
        }
        if (lookAt != null) {
            lookAt.perform(commandSourceStack, entity);
        }
        if (!(entity instanceof LivingEntity) || !(livingEntity = (LivingEntity)entity).isFallFlying()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            entity.setOnGround(true);
        }
        if (entity instanceof PathfinderMob) {
            livingEntity = (PathfinderMob)entity;
            ((Mob)livingEntity).getNavigation().stop();
        }
    }

    @FunctionalInterface
    static interface LookAt {
        public void perform(CommandSourceStack var1, Entity var2);
    }

    record LookAtPosition(Vec3 position) implements LookAt
    {
        @Override
        public void perform(CommandSourceStack commandSourceStack, Entity entity) {
            entity.lookAt(commandSourceStack.getAnchor(), this.position);
        }
    }

    record LookAtEntity(Entity entity, EntityAnchorArgument.Anchor anchor) implements LookAt
    {
        @Override
        public void perform(CommandSourceStack commandSourceStack, Entity entity) {
            if (entity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)entity;
                serverPlayer.lookAt(commandSourceStack.getAnchor(), this.entity, this.anchor);
            } else {
                entity.lookAt(commandSourceStack.getAnchor(), this.anchor.apply(this.entity));
            }
        }
    }
}

