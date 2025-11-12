/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.timers;

import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class FunctionTagCallback
implements TimerCallback<MinecraftServer> {
    final ResourceLocation tagId;

    public FunctionTagCallback(ResourceLocation resourceLocation) {
        this.tagId = resourceLocation;
    }

    @Override
    public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long l) {
        ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
        Collection<CommandFunction<CommandSourceStack>> collection = serverFunctionManager.getTag(this.tagId);
        for (CommandFunction<CommandSourceStack> commandFunction : collection) {
            serverFunctionManager.execute(commandFunction, serverFunctionManager.getGameLoopSender());
        }
    }

    @Override
    public /* synthetic */ void handle(Object object, TimerQueue timerQueue, long l) {
        this.handle((MinecraftServer)object, (TimerQueue<MinecraftServer>)timerQueue, l);
    }

    public static class Serializer
    extends TimerCallback.Serializer<MinecraftServer, FunctionTagCallback> {
        public Serializer() {
            super(ResourceLocation.withDefaultNamespace("function_tag"), FunctionTagCallback.class);
        }

        @Override
        public void serialize(CompoundTag compoundTag, FunctionTagCallback functionTagCallback) {
            compoundTag.putString("Name", functionTagCallback.tagId.toString());
        }

        @Override
        public FunctionTagCallback deserialize(CompoundTag compoundTag) {
            ResourceLocation resourceLocation = ResourceLocation.parse(compoundTag.getString("Name"));
            return new FunctionTagCallback(resourceLocation);
        }

        @Override
        public /* synthetic */ TimerCallback deserialize(CompoundTag compoundTag) {
            return this.deserialize(compoundTag);
        }
    }
}

