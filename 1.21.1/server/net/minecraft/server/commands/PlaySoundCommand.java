/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PlaySoundCommand {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType((Message)Component.translatable("commands.playsound.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        RequiredArgumentBuilder requiredArgumentBuilder = (RequiredArgumentBuilder)Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), PlaySoundCommand.getCallingPlayerAsCollection(((CommandSourceStack)commandContext.getSource()).getPlayer()), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound"), SoundSource.MASTER, ((CommandSourceStack)commandContext.getSource()).getPosition(), 1.0f, 1.0f, 0.0f));
        for (SoundSource soundSource : SoundSource.values()) {
            requiredArgumentBuilder.then(PlaySoundCommand.source(soundSource));
        }
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("playsound").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then((ArgumentBuilder)requiredArgumentBuilder));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource soundSource) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(soundSource.getName()).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), PlaySoundCommand.getCallingPlayerAsCollection(((CommandSourceStack)commandContext.getSource()).getPlayer()), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound"), soundSource, ((CommandSourceStack)commandContext.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound"), soundSource, ((CommandSourceStack)commandContext.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3()).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound"), soundSource, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos"), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("volume", FloatArgumentType.floatArg((float)0.0f)).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound"), soundSource, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos"), ((Float)commandContext.getArgument("volume", Float.class)).floatValue(), 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("pitch", FloatArgumentType.floatArg((float)0.0f, (float)2.0f)).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound"), soundSource, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos"), ((Float)commandContext.getArgument("volume", Float.class)).floatValue(), ((Float)commandContext.getArgument("pitch", Float.class)).floatValue(), 0.0f))).then(Commands.argument("minVolume", FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound"), soundSource, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos"), ((Float)commandContext.getArgument("volume", Float.class)).floatValue(), ((Float)commandContext.getArgument("pitch", Float.class)).floatValue(), ((Float)commandContext.getArgument("minVolume", Float.class)).floatValue())))))));
    }

    private static Collection<ServerPlayer> getCallingPlayerAsCollection(@Nullable ServerPlayer serverPlayer) {
        return serverPlayer != null ? List.of(serverPlayer) : List.of();
    }

    private static int playSound(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, ResourceLocation resourceLocation, SoundSource soundSource, Vec3 vec3, float f, float f2, float f3) throws CommandSyntaxException {
        Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(resourceLocation));
        double d = Mth.square(holder.value().getRange(f));
        int n = 0;
        long l = commandSourceStack.getLevel().getRandom().nextLong();
        for (ServerPlayer serverPlayer : collection) {
            double d2 = vec3.x - serverPlayer.getX();
            double d3 = vec3.y - serverPlayer.getY();
            double d4 = vec3.z - serverPlayer.getZ();
            double d5 = d2 * d2 + d3 * d3 + d4 * d4;
            Vec3 vec32 = vec3;
            float f4 = f;
            if (d5 > d) {
                if (f3 <= 0.0f) continue;
                double d6 = Math.sqrt(d5);
                vec32 = new Vec3(serverPlayer.getX() + d2 / d6 * 2.0, serverPlayer.getY() + d3 / d6 * 2.0, serverPlayer.getZ() + d4 / d6 * 2.0);
                f4 = f3;
            }
            serverPlayer.connection.send(new ClientboundSoundPacket(holder, soundSource, vec32.x(), vec32.y(), vec32.z(), f4, f2, l));
            ++n;
        }
        if (n == 0) {
            throw ERROR_TOO_FAR.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.playsound.success.single", Component.translationArg(resourceLocation), ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.playsound.success.multiple", Component.translationArg(resourceLocation), collection.size()), true);
        }
        return n;
    }
}

