/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandExceptionType
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandSourceStack
implements ExecutionCommandSource<CommandSourceStack>,
SharedSuggestionProvider {
    public static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType((Message)Component.translatable("permissions.requires.player"));
    public static final SimpleCommandExceptionType ERROR_NOT_ENTITY = new SimpleCommandExceptionType((Message)Component.translatable("permissions.requires.entity"));
    private final CommandSource source;
    private final Vec3 worldPosition;
    private final ServerLevel level;
    private final int permissionLevel;
    private final String textName;
    private final Component displayName;
    private final MinecraftServer server;
    private final boolean silent;
    @Nullable
    private final Entity entity;
    private final CommandResultCallback resultCallback;
    private final EntityAnchorArgument.Anchor anchor;
    private final Vec2 rotation;
    private final CommandSigningContext signingContext;
    private final TaskChainer chatMessageChainer;

    public CommandSourceStack(CommandSource commandSource, Vec3 vec3, Vec2 vec2, ServerLevel serverLevel, int n, String string, Component component, MinecraftServer minecraftServer, @Nullable Entity entity) {
        this(commandSource, vec3, vec2, serverLevel, n, string, component, minecraftServer, entity, false, CommandResultCallback.EMPTY, EntityAnchorArgument.Anchor.FEET, CommandSigningContext.ANONYMOUS, TaskChainer.immediate(minecraftServer));
    }

    protected CommandSourceStack(CommandSource commandSource, Vec3 vec3, Vec2 vec2, ServerLevel serverLevel, int n, String string, Component component, MinecraftServer minecraftServer, @Nullable Entity entity, boolean bl, CommandResultCallback commandResultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext commandSigningContext, TaskChainer taskChainer) {
        this.source = commandSource;
        this.worldPosition = vec3;
        this.level = serverLevel;
        this.silent = bl;
        this.entity = entity;
        this.permissionLevel = n;
        this.textName = string;
        this.displayName = component;
        this.server = minecraftServer;
        this.resultCallback = commandResultCallback;
        this.anchor = anchor;
        this.rotation = vec2;
        this.signingContext = commandSigningContext;
        this.chatMessageChainer = taskChainer;
    }

    public CommandSourceStack withSource(CommandSource commandSource) {
        if (this.source == commandSource) {
            return this;
        }
        return new CommandSourceStack(commandSource, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withEntity(Entity entity) {
        if (this.entity == entity) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, entity.getName().getString(), entity.getDisplayName(), this.server, entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withPosition(Vec3 vec3) {
        if (this.worldPosition.equals(vec3)) {
            return this;
        }
        return new CommandSourceStack(this.source, vec3, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withRotation(Vec2 vec2) {
        if (this.rotation.equals(vec2)) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, vec2, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    @Override
    public CommandSourceStack withCallback(CommandResultCallback commandResultCallback) {
        if (Objects.equals(this.resultCallback, commandResultCallback)) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, commandResultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withCallback(CommandResultCallback commandResultCallback, BinaryOperator<CommandResultCallback> binaryOperator) {
        CommandResultCallback commandResultCallback2 = (CommandResultCallback)binaryOperator.apply(this.resultCallback, commandResultCallback);
        return this.withCallback(commandResultCallback2);
    }

    public CommandSourceStack withSuppressedOutput() {
        if (this.silent || this.source.alwaysAccepts()) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, true, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withPermission(int n) {
        if (n == this.permissionLevel) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, n, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withMaximumPermission(int n) {
        if (n <= this.permissionLevel) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, n, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withAnchor(EntityAnchorArgument.Anchor anchor) {
        if (anchor == this.anchor) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack withLevel(ServerLevel serverLevel) {
        if (serverLevel == this.level) {
            return this;
        }
        double d = DimensionType.getTeleportationScale(this.level.dimensionType(), serverLevel.dimensionType());
        Vec3 vec3 = new Vec3(this.worldPosition.x * d, this.worldPosition.y, this.worldPosition.z * d);
        return new CommandSourceStack(this.source, vec3, this.rotation, serverLevel, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
    }

    public CommandSourceStack facing(Entity entity, EntityAnchorArgument.Anchor anchor) {
        return this.facing(anchor.apply(entity));
    }

    public CommandSourceStack facing(Vec3 vec3) {
        Vec3 vec32 = this.anchor.apply(this);
        double d = vec3.x - vec32.x;
        double d2 = vec3.y - vec32.y;
        double d3 = vec3.z - vec32.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = Mth.wrapDegrees((float)(-(Mth.atan2(d2, d4) * 57.2957763671875)));
        float f2 = Mth.wrapDegrees((float)(Mth.atan2(d3, d) * 57.2957763671875) - 90.0f);
        return this.withRotation(new Vec2(f, f2));
    }

    public CommandSourceStack withSigningContext(CommandSigningContext commandSigningContext, TaskChainer taskChainer) {
        if (commandSigningContext == this.signingContext && taskChainer == this.chatMessageChainer) {
            return this;
        }
        return new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.resultCallback, this.anchor, commandSigningContext, taskChainer);
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public String getTextName() {
        return this.textName;
    }

    @Override
    public boolean hasPermission(int n) {
        return this.permissionLevel >= n;
    }

    public Vec3 getPosition() {
        return this.worldPosition;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    public Entity getEntityOrException() throws CommandSyntaxException {
        if (this.entity == null) {
            throw ERROR_NOT_ENTITY.create();
        }
        return this.entity;
    }

    public ServerPlayer getPlayerOrException() throws CommandSyntaxException {
        Entity entity = this.entity;
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            return serverPlayer;
        }
        throw ERROR_NOT_PLAYER.create();
    }

    @Nullable
    public ServerPlayer getPlayer() {
        ServerPlayer serverPlayer;
        Entity entity = this.entity;
        return entity instanceof ServerPlayer ? (serverPlayer = (ServerPlayer)entity) : null;
    }

    public boolean isPlayer() {
        return this.entity instanceof ServerPlayer;
    }

    public Vec2 getRotation() {
        return this.rotation;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public EntityAnchorArgument.Anchor getAnchor() {
        return this.anchor;
    }

    public CommandSigningContext getSigningContext() {
        return this.signingContext;
    }

    public TaskChainer getChatMessageChainer() {
        return this.chatMessageChainer;
    }

    public boolean shouldFilterMessageTo(ServerPlayer serverPlayer) {
        ServerPlayer serverPlayer2 = this.getPlayer();
        if (serverPlayer == serverPlayer2) {
            return false;
        }
        return serverPlayer2 != null && serverPlayer2.isTextFilteringEnabled() || serverPlayer.isTextFilteringEnabled();
    }

    public void sendChatMessage(OutgoingChatMessage outgoingChatMessage, boolean bl, ChatType.Bound bound) {
        if (this.silent) {
            return;
        }
        ServerPlayer serverPlayer = this.getPlayer();
        if (serverPlayer != null) {
            serverPlayer.sendChatMessage(outgoingChatMessage, bl, bound);
        } else {
            this.source.sendSystemMessage(bound.decorate(outgoingChatMessage.content()));
        }
    }

    public void sendSystemMessage(Component component) {
        if (this.silent) {
            return;
        }
        ServerPlayer serverPlayer = this.getPlayer();
        if (serverPlayer != null) {
            serverPlayer.sendSystemMessage(component);
        } else {
            this.source.sendSystemMessage(component);
        }
    }

    public void sendSuccess(Supplier<Component> supplier, boolean bl) {
        boolean bl2;
        boolean bl3 = this.source.acceptsSuccess() && !this.silent;
        boolean bl4 = bl2 = bl && this.source.shouldInformAdmins() && !this.silent;
        if (!bl3 && !bl2) {
            return;
        }
        Component component = supplier.get();
        if (bl3) {
            this.source.sendSystemMessage(component);
        }
        if (bl2) {
            this.broadcastToAdmins(component);
        }
    }

    private void broadcastToAdmins(Component component) {
        MutableComponent mutableComponent = Component.translatable("chat.type.admin", this.getDisplayName(), component).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        if (this.server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
                if (serverPlayer == this.source || !this.server.getPlayerList().isOp(serverPlayer.getGameProfile())) continue;
                serverPlayer.sendSystemMessage(mutableComponent);
            }
        }
        if (this.source != this.server && this.server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
            this.server.sendSystemMessage(mutableComponent);
        }
    }

    public void sendFailure(Component component) {
        if (this.source.acceptsFailure() && !this.silent) {
            this.source.sendSystemMessage(Component.empty().append(component).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public CommandResultCallback callback() {
        return this.resultCallback;
    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        return Lists.newArrayList((Object[])this.server.getPlayerNames());
    }

    @Override
    public Collection<String> getAllTeams() {
        return this.server.getScoreboard().getTeamNames();
    }

    @Override
    public Stream<ResourceLocation> getAvailableSounds() {
        return BuiltInRegistries.SOUND_EVENT.stream().map(SoundEvent::getLocation);
    }

    @Override
    public Stream<ResourceLocation> getRecipeNames() {
        return this.server.getRecipeManager().getRecipeIds();
    }

    @Override
    public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> commandContext) {
        return Suggestions.empty();
    }

    @Override
    public CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> resourceKey, SharedSuggestionProvider.ElementSuggestionType elementSuggestionType, SuggestionsBuilder suggestionsBuilder, CommandContext<?> commandContext) {
        return this.registryAccess().registry(resourceKey).map(registry -> {
            this.suggestRegistryElements((Registry<?>)registry, elementSuggestionType, suggestionsBuilder);
            return suggestionsBuilder.buildFuture();
        }).orElseGet(Suggestions::empty);
    }

    @Override
    public Set<ResourceKey<Level>> levels() {
        return this.server.levelKeys();
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.server.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.level.enabledFeatures();
    }

    @Override
    public CommandDispatcher<CommandSourceStack> dispatcher() {
        return this.getServer().getFunctions().getDispatcher();
    }

    @Override
    public void handleError(CommandExceptionType commandExceptionType, Message message, boolean bl, @Nullable TraceCallbacks traceCallbacks) {
        if (traceCallbacks != null) {
            traceCallbacks.onError(message.getString());
        }
        if (!bl) {
            this.sendFailure(ComponentUtils.fromMessage(message));
        }
    }

    @Override
    public boolean isSilent() {
        return this.silent;
    }

    @Override
    public /* synthetic */ ExecutionCommandSource withCallback(CommandResultCallback commandResultCallback) {
        return this.withCallback(commandResultCallback);
    }
}

