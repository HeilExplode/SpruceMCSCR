/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.RedirectModifier
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.SlotsArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;

public class ExecuteCommand {
    private static final int MAX_TEST_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.execute.blocks.toobig", object, object2));
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.execute.conditional.fail"));
    private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.execute.conditional.fail_count", object));
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.execute.function.instantiationFailure", object, object2));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREDICATE = (commandContext, suggestionsBuilder) -> {
        ReloadableServerRegistries.Holder holder = ((CommandSourceStack)commandContext.getSource()).getServer().reloadableRegistries();
        return SharedSuggestionProvider.suggestResource(holder.getKeys(Registries.PREDICATE), suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        LiteralCommandNode literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("execute").requires(commandSourceStack -> commandSourceStack.hasPermission(2)));
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("execute").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("run").redirect((CommandNode)commandDispatcher.getRoot()))).then(ExecuteCommand.addConditionals((CommandNode<CommandSourceStack>)literalCommandNode, Commands.literal("if"), true, commandBuildContext))).then(ExecuteCommand.addConditionals((CommandNode<CommandSourceStack>)literalCommandNode, Commands.literal("unless"), false, commandBuildContext))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)literalCommandNode, commandContext -> {
            ArrayList arrayList = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)commandContext, "targets")) {
                arrayList.add(((CommandSourceStack)commandContext.getSource()).withEntity(entity));
            }
            return arrayList;
        })))).then(Commands.literal("at").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)literalCommandNode, commandContext -> {
            ArrayList arrayList = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)commandContext, "targets")) {
                arrayList.add(((CommandSourceStack)commandContext.getSource()).withLevel((ServerLevel)entity.level()).withPosition(entity.position()).withRotation(entity.getRotationVector()));
            }
            return arrayList;
        })))).then(((LiteralArgumentBuilder)Commands.literal("store").then(ExecuteCommand.wrapStores((LiteralCommandNode<CommandSourceStack>)literalCommandNode, Commands.literal("result"), true))).then(ExecuteCommand.wrapStores((LiteralCommandNode<CommandSourceStack>)literalCommandNode, Commands.literal("success"), false)))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("positioned").then(Commands.argument("pos", Vec3Argument.vec3()).redirect((CommandNode)literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withPosition(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos")).withAnchor(EntityAnchorArgument.Anchor.FEET)))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)literalCommandNode, commandContext -> {
            ArrayList arrayList = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)commandContext, "targets")) {
                arrayList.add(((CommandSourceStack)commandContext.getSource()).withPosition(entity.position()));
            }
            return arrayList;
        })))).then(Commands.literal("over").then(Commands.argument("heightmap", HeightmapTypeArgument.heightmap()).redirect((CommandNode)literalCommandNode, commandContext -> {
            Vec3 vec3 = ((CommandSourceStack)commandContext.getSource()).getPosition();
            ServerLevel serverLevel = ((CommandSourceStack)commandContext.getSource()).getLevel();
            double d = vec3.x();
            double d2 = vec3.z();
            if (!serverLevel.hasChunk(SectionPos.blockToSectionCoord(d), SectionPos.blockToSectionCoord(d2))) {
                throw BlockPosArgument.ERROR_NOT_LOADED.create();
            }
            int n = serverLevel.getHeight(HeightmapTypeArgument.getHeightmap((CommandContext<CommandSourceStack>)commandContext, "heightmap"), Mth.floor(d), Mth.floor(d2));
            return ((CommandSourceStack)commandContext.getSource()).withPosition(new Vec3(d, n, d2));
        }))))).then(((LiteralArgumentBuilder)Commands.literal("rotated").then(Commands.argument("rot", RotationArgument.rotation()).redirect((CommandNode)literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withRotation(RotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rot").getRotation((CommandSourceStack)commandContext.getSource()))))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)literalCommandNode, commandContext -> {
            ArrayList arrayList = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)commandContext, "targets")) {
                arrayList.add(((CommandSourceStack)commandContext.getSource()).withRotation(entity.getRotationVector()));
            }
            return arrayList;
        }))))).then(((LiteralArgumentBuilder)Commands.literal("facing").then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("anchor", EntityAnchorArgument.anchor()).fork((CommandNode)literalCommandNode, commandContext -> {
            ArrayList arrayList = Lists.newArrayList();
            EntityAnchorArgument.Anchor anchor = EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)commandContext, "anchor");
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)commandContext, "targets")) {
                arrayList.add(((CommandSourceStack)commandContext.getSource()).facing(entity, anchor));
            }
            return arrayList;
        }))))).then(Commands.argument("pos", Vec3Argument.vec3()).redirect((CommandNode)literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).facing(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos")))))).then(Commands.literal("align").then(Commands.argument("axes", SwizzleArgument.swizzle()).redirect((CommandNode)literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withPosition(((CommandSourceStack)commandContext.getSource()).getPosition().align(SwizzleArgument.getSwizzle((CommandContext<CommandSourceStack>)commandContext, "axes"))))))).then(Commands.literal("anchored").then(Commands.argument("anchor", EntityAnchorArgument.anchor()).redirect((CommandNode)literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withAnchor(EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)commandContext, "anchor")))))).then(Commands.literal("in").then(Commands.argument("dimension", DimensionArgument.dimension()).redirect((CommandNode)literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withLevel(DimensionArgument.getDimension((CommandContext<CommandSourceStack>)commandContext, "dimension")))))).then(Commands.literal("summon").then(Commands.argument("entity", ResourceArgument.resource(commandBuildContext, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).redirect((CommandNode)literalCommandNode, commandContext -> ExecuteCommand.spawnEntityAndRedirect((CommandSourceStack)commandContext.getSource(), ResourceArgument.getSummonableEntityType((CommandContext<CommandSourceStack>)commandContext, "entity")))))).then(ExecuteCommand.createRelationOperations((CommandNode<CommandSourceStack>)literalCommandNode, Commands.literal("on"))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapStores(LiteralCommandNode<CommandSourceStack> literalCommandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, boolean bl) {
        literalArgumentBuilder.then(Commands.literal("score").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeValue((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), bl)))));
        literalArgumentBuilder.then(Commands.literal("bossbar").then(((RequiredArgumentBuilder)Commands.argument("id", ResourceLocationArgument.id()).suggests(BossBarCommands.SUGGEST_BOSS_BAR).then(Commands.literal("value").redirect(literalCommandNode, commandContext -> ExecuteCommand.storeValue((CommandSourceStack)commandContext.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)commandContext), true, bl)))).then(Commands.literal("max").redirect(literalCommandNode, commandContext -> ExecuteCommand.storeValue((CommandSourceStack)commandContext.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)commandContext), false, bl)))));
        for (DataCommands.DataProvider dataProvider : DataCommands.TARGET_PROVIDERS) {
            dataProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)literalArgumentBuilder, argumentBuilder -> argumentBuilder.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).then(Commands.literal("int").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"), n -> IntTag.valueOf((int)((double)n * DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale"))), bl))))).then(Commands.literal("float").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"), n -> FloatTag.valueOf((float)((double)n * DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale"))), bl))))).then(Commands.literal("short").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"), n -> ShortTag.valueOf((short)((double)n * DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale"))), bl))))).then(Commands.literal("long").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"), n -> LongTag.valueOf((long)((double)n * DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale"))), bl))))).then(Commands.literal("double").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"), n -> DoubleTag.valueOf((double)n * DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale")), bl))))).then(Commands.literal("byte").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"), n -> ByteTag.valueOf((byte)((double)n * DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale"))), bl))))));
        }
        return literalArgumentBuilder;
    }

    private static CommandSourceStack storeValue(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, boolean bl) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        return commandSourceStack.withCallback((bl2, n) -> {
            for (ScoreHolder scoreHolder : collection) {
                ScoreAccess scoreAccess = serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective);
                int n2 = bl ? n : (bl2 ? 1 : 0);
                scoreAccess.set(n2);
            }
        }, CommandResultCallback::chain);
    }

    private static CommandSourceStack storeValue(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, boolean bl, boolean bl2) {
        return commandSourceStack.withCallback((bl3, n) -> {
            int n2;
            int n3 = bl2 ? n : (n2 = bl3 ? 1 : 0);
            if (bl) {
                customBossEvent.setValue(n2);
            } else {
                customBossEvent.setMax(n2);
            }
        }, CommandResultCallback::chain);
    }

    private static CommandSourceStack storeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath, IntFunction<Tag> intFunction, boolean bl) {
        return commandSourceStack.withCallback((bl2, n) -> {
            try {
                CompoundTag compoundTag = dataAccessor.getData();
                int n2 = bl ? n : (bl2 ? 1 : 0);
                nbtPath.set(compoundTag, (Tag)intFunction.apply(n2));
                dataAccessor.setData(compoundTag);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }, CommandResultCallback::chain);
    }

    private static boolean isChunkLoaded(ServerLevel serverLevel, BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        LevelChunk levelChunk = serverLevel.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (levelChunk != null) {
            return levelChunk.getFullStatus() == FullChunkStatus.ENTITY_TICKING && serverLevel.areEntitiesLoaded(chunkPos.toLong());
        }
        return false;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(CommandNode<CommandSourceStack> commandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, boolean bl, CommandBuildContext commandBuildContext) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(ExecuteCommand.addConditional(commandNode, Commands.argument("block", BlockPredicateArgument.blockPredicate(commandBuildContext)), bl, commandContext -> BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)commandContext, "block").test(new BlockInWorld(((CommandSourceStack)commandContext.getSource()).getLevel(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), true))))))).then(Commands.literal("biome").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(ExecuteCommand.addConditional(commandNode, Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME)), bl, commandContext -> ResourceOrTagArgument.getResourceOrTag((CommandContext<CommandSourceStack>)commandContext, "biome", Registries.BIOME).test(((CommandSourceStack)commandContext.getSource()).getLevel().getBiome(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos")))))))).then(Commands.literal("loaded").then(ExecuteCommand.addConditional(commandNode, Commands.argument("pos", BlockPosArgument.blockPos()), bl, commandContext -> ExecuteCommand.isChunkLoaded(((CommandSourceStack)commandContext.getSource()).getLevel(), BlockPosArgument.getBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos")))))).then(Commands.literal("dimension").then(ExecuteCommand.addConditional(commandNode, Commands.argument("dimension", DimensionArgument.dimension()), bl, commandContext -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)commandContext, "dimension") == ((CommandSourceStack)commandContext.getSource()).getLevel())))).then(Commands.literal("score").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.literal("=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (int n, int n2) -> n == n2)))))).then(Commands.literal("<").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (int n, int n2) -> n < n2)))))).then(Commands.literal("<=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (int n, int n2) -> n <= n2)))))).then(Commands.literal(">").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (int n, int n2) -> n > n2)))))).then(Commands.literal(">=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (int n, int n2) -> n >= n2)))))).then(Commands.literal("matches").then(ExecuteCommand.addConditional(commandNode, Commands.argument("range", RangeArgument.intRange()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, RangeArgument.Ints.getRange((CommandContext<CommandSourceStack>)commandContext, "range"))))))))).then(Commands.literal("blocks").then(Commands.argument("start", BlockPosArgument.blockPos()).then(Commands.argument("end", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("destination", BlockPosArgument.blockPos()).then(ExecuteCommand.addIfBlocksConditional(commandNode, Commands.literal("all"), bl, false))).then(ExecuteCommand.addIfBlocksConditional(commandNode, Commands.literal("masked"), bl, true))))))).then(Commands.literal("entity").then(((RequiredArgumentBuilder)Commands.argument("entities", EntityArgument.entities()).fork(commandNode, commandContext -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)commandContext, bl, !EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)commandContext, "entities").isEmpty()))).executes(ExecuteCommand.createNumericConditionalHandler(bl, commandContext -> EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)commandContext, "entities").size()))))).then(Commands.literal("predicate").then(ExecuteCommand.addConditional(commandNode, Commands.argument("predicate", ResourceOrIdArgument.lootPredicate(commandBuildContext)).suggests(SUGGEST_PREDICATE), bl, commandContext -> ExecuteCommand.checkCustomPredicate((CommandSourceStack)commandContext.getSource(), ResourceOrIdArgument.getLootPredicate((CommandContext<CommandSourceStack>)commandContext, "predicate")))))).then(Commands.literal("function").then(Commands.argument("name", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).fork(commandNode, (RedirectModifier)new ExecuteIfFunctionCustomModifier(bl))))).then(((LiteralArgumentBuilder)Commands.literal("items").then(Commands.literal("entity").then(Commands.argument("entities", EntityArgument.entities()).then(Commands.argument("slots", SlotsArgument.slots()).then(((RequiredArgumentBuilder)Commands.argument("item_predicate", ItemPredicateArgument.itemPredicate(commandBuildContext)).fork(commandNode, commandContext -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)commandContext, bl, ExecuteCommand.countItems(EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "entities"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)commandContext, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)commandContext, "item_predicate")) > 0))).executes(ExecuteCommand.createNumericConditionalHandler(bl, commandContext -> ExecuteCommand.countItems(EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "entities"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)commandContext, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)commandContext, "item_predicate"))))))))).then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slots", SlotsArgument.slots()).then(((RequiredArgumentBuilder)Commands.argument("item_predicate", ItemPredicateArgument.itemPredicate(commandBuildContext)).fork(commandNode, commandContext -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)commandContext, bl, ExecuteCommand.countItems((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)commandContext, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)commandContext, "item_predicate")) > 0))).executes(ExecuteCommand.createNumericConditionalHandler(bl, commandContext -> ExecuteCommand.countItems((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)commandContext, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)commandContext, "item_predicate")))))))));
        for (DataCommands.DataProvider dataProvider : DataCommands.SOURCE_PROVIDERS) {
            literalArgumentBuilder.then(dataProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("data"), argumentBuilder -> argumentBuilder.then(((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).fork(commandNode, commandContext -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)commandContext, bl, ExecuteCommand.checkMatchingData(dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path")) > 0))).executes(ExecuteCommand.createNumericConditionalHandler(bl, commandContext -> ExecuteCommand.checkMatchingData(dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path")))))));
        }
        return literalArgumentBuilder;
    }

    private static int countItems(Iterable<? extends Entity> iterable, SlotRange slotRange, Predicate<ItemStack> predicate) {
        int n = 0;
        for (Entity entity : iterable) {
            IntList intList = slotRange.slots();
            for (int i = 0; i < intList.size(); ++i) {
                int n2 = intList.getInt(i);
                SlotAccess slotAccess = entity.getSlot(n2);
                ItemStack itemStack = slotAccess.get();
                if (!predicate.test(itemStack)) continue;
                n += itemStack.getCount();
            }
        }
        return n;
    }

    private static int countItems(CommandSourceStack commandSourceStack, BlockPos blockPos, SlotRange slotRange, Predicate<ItemStack> predicate) throws CommandSyntaxException {
        int n = 0;
        Container container = ItemCommands.getContainer(commandSourceStack, blockPos, ItemCommands.ERROR_SOURCE_NOT_A_CONTAINER);
        int n2 = container.getContainerSize();
        IntList intList = slotRange.slots();
        for (int i = 0; i < intList.size(); ++i) {
            ItemStack itemStack;
            int n3 = intList.getInt(i);
            if (n3 < 0 || n3 >= n2 || !predicate.test(itemStack = container.getItem(n3))) continue;
            n += itemStack.getCount();
        }
        return n;
    }

    private static Command<CommandSourceStack> createNumericConditionalHandler(boolean bl, CommandNumericPredicate commandNumericPredicate) {
        if (bl) {
            return commandContext -> {
                int n = commandNumericPredicate.test((CommandContext<CommandSourceStack>)commandContext);
                if (n > 0) {
                    ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", n), false);
                    return n;
                }
                throw ERROR_CONDITIONAL_FAILED.create();
            };
        }
        return commandContext -> {
            int n = commandNumericPredicate.test((CommandContext<CommandSourceStack>)commandContext);
            if (n == 0) {
                ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED_COUNT.create((Object)n);
        };
    }

    private static int checkMatchingData(DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
        return nbtPath.countMatching(dataAccessor.getData());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> commandContext, IntBiPredicate intBiPredicate) throws CommandSyntaxException {
        ScoreHolder scoreHolder = ScoreHolderArgument.getName(commandContext, "target");
        Objective objective = ObjectiveArgument.getObjective(commandContext, "targetObjective");
        ScoreHolder scoreHolder2 = ScoreHolderArgument.getName(commandContext, "source");
        Objective objective2 = ObjectiveArgument.getObjective(commandContext, "sourceObjective");
        ServerScoreboard serverScoreboard = ((CommandSourceStack)commandContext.getSource()).getServer().getScoreboard();
        ReadOnlyScoreInfo readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(scoreHolder, objective);
        ReadOnlyScoreInfo readOnlyScoreInfo2 = serverScoreboard.getPlayerScoreInfo(scoreHolder2, objective2);
        if (readOnlyScoreInfo == null || readOnlyScoreInfo2 == null) {
            return false;
        }
        return intBiPredicate.test(readOnlyScoreInfo.value(), readOnlyScoreInfo2.value());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> commandContext, MinMaxBounds.Ints ints) throws CommandSyntaxException {
        ScoreHolder scoreHolder = ScoreHolderArgument.getName(commandContext, "target");
        Objective objective = ObjectiveArgument.getObjective(commandContext, "targetObjective");
        ServerScoreboard serverScoreboard = ((CommandSourceStack)commandContext.getSource()).getServer().getScoreboard();
        ReadOnlyScoreInfo readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(scoreHolder, objective);
        if (readOnlyScoreInfo == null) {
            return false;
        }
        return ints.matches(readOnlyScoreInfo.value());
    }

    private static boolean checkCustomPredicate(CommandSourceStack commandSourceStack, Holder<LootItemCondition> holder) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity()).create(LootContextParamSets.COMMAND);
        LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
        lootContext.pushVisitedElement(LootContext.createVisitedEntry(holder.value()));
        return holder.value().test(lootContext);
    }

    private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> commandContext, boolean bl, boolean bl2) {
        if (bl2 == bl) {
            return Collections.singleton((CommandSourceStack)commandContext.getSource());
        }
        return Collections.emptyList();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(CommandNode<CommandSourceStack> commandNode, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, boolean bl, CommandPredicate commandPredicate) {
        return argumentBuilder.fork(commandNode, commandContext -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)commandContext, bl, commandPredicate.test((CommandContext<CommandSourceStack>)commandContext))).executes(commandContext -> {
            if (bl == commandPredicate.test((CommandContext<CommandSourceStack>)commandContext)) {
                ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED.create();
        });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addIfBlocksConditional(CommandNode<CommandSourceStack> commandNode, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, boolean bl, boolean bl2) {
        return argumentBuilder.fork(commandNode, commandContext -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)commandContext, bl, ExecuteCommand.checkRegions((CommandContext<CommandSourceStack>)commandContext, bl2).isPresent())).executes(bl ? commandContext -> ExecuteCommand.checkIfRegions((CommandContext<CommandSourceStack>)commandContext, bl2) : commandContext -> ExecuteCommand.checkUnlessRegions((CommandContext<CommandSourceStack>)commandContext, bl2));
    }

    private static int checkIfRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.checkRegions(commandContext, bl);
        if (optionalInt.isPresent()) {
            ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", optionalInt.getAsInt()), false);
            return optionalInt.getAsInt();
        }
        throw ERROR_CONDITIONAL_FAILED.create();
    }

    private static int checkUnlessRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.checkRegions(commandContext, bl);
        if (optionalInt.isPresent()) {
            throw ERROR_CONDITIONAL_FAILED_COUNT.create((Object)optionalInt.getAsInt());
        }
        ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
        return 1;
    }

    private static OptionalInt checkRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
        return ExecuteCommand.checkRegions(((CommandSourceStack)commandContext.getSource()).getLevel(), BlockPosArgument.getLoadedBlockPos(commandContext, "start"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), bl);
    }

    private static OptionalInt checkRegions(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, boolean bl) throws CommandSyntaxException {
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
        BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos3.offset(boundingBox.getLength()));
        BlockPos blockPos4 = new BlockPos(boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ());
        int n = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (n > 32768) {
            throw ERROR_AREA_TOO_LARGE.create((Object)32768, (Object)n);
        }
        RegistryAccess registryAccess = serverLevel.registryAccess();
        int n2 = 0;
        for (int i = boundingBox.minZ(); i <= boundingBox.maxZ(); ++i) {
            for (int j = boundingBox.minY(); j <= boundingBox.maxY(); ++j) {
                for (int k = boundingBox.minX(); k <= boundingBox.maxX(); ++k) {
                    BlockPos blockPos5 = new BlockPos(k, j, i);
                    BlockPos blockPos6 = blockPos5.offset(blockPos4);
                    BlockState blockState = serverLevel.getBlockState(blockPos5);
                    if (bl && blockState.is(Blocks.AIR)) continue;
                    if (blockState != serverLevel.getBlockState(blockPos6)) {
                        return OptionalInt.empty();
                    }
                    BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos5);
                    BlockEntity blockEntity2 = serverLevel.getBlockEntity(blockPos6);
                    if (blockEntity != null) {
                        CompoundTag compoundTag;
                        if (blockEntity2 == null) {
                            return OptionalInt.empty();
                        }
                        if (blockEntity2.getType() != blockEntity.getType()) {
                            return OptionalInt.empty();
                        }
                        if (!blockEntity.components().equals(blockEntity2.components())) {
                            return OptionalInt.empty();
                        }
                        CompoundTag compoundTag2 = blockEntity.saveCustomOnly(registryAccess);
                        if (!compoundTag2.equals(compoundTag = blockEntity2.saveCustomOnly(registryAccess))) {
                            return OptionalInt.empty();
                        }
                    }
                    ++n2;
                }
            }
        }
        return OptionalInt.of(n2);
    }

    private static RedirectModifier<CommandSourceStack> expandOneToOneEntityRelation(Function<Entity, Optional<Entity>> function) {
        return commandContext -> {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            Entity entity2 = commandSourceStack.getEntity();
            if (entity2 == null) {
                return List.of();
            }
            return ((Optional)function.apply(entity2)).filter(entity -> !entity.isRemoved()).map(entity -> List.of(commandSourceStack.withEntity((Entity)entity))).orElse(List.of());
        };
    }

    private static RedirectModifier<CommandSourceStack> expandOneToManyEntityRelation(Function<Entity, Stream<Entity>> function) {
        return commandContext -> {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            Entity entity2 = commandSourceStack.getEntity();
            if (entity2 == null) {
                return List.of();
            }
            return ((Stream)function.apply(entity2)).filter(entity -> !entity.isRemoved()).map(commandSourceStack::withEntity).toList();
        };
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRelationOperations(CommandNode<CommandSourceStack> commandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(Commands.literal("owner").fork(commandNode, ExecuteCommand.expandOneToOneEntityRelation(entity -> {
            Optional<Object> optional;
            if (entity instanceof OwnableEntity) {
                OwnableEntity ownableEntity = (OwnableEntity)((Object)entity);
                optional = Optional.ofNullable(ownableEntity.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("leasher").fork(commandNode, ExecuteCommand.expandOneToOneEntityRelation(entity -> {
            Optional<Object> optional;
            if (entity instanceof Leashable) {
                Leashable leashable = (Leashable)((Object)entity);
                optional = Optional.ofNullable(leashable.getLeashHolder());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("target").fork(commandNode, ExecuteCommand.expandOneToOneEntityRelation(entity -> {
            Optional<Object> optional;
            if (entity instanceof Targeting) {
                Targeting targeting = (Targeting)((Object)entity);
                optional = Optional.ofNullable(targeting.getTarget());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("attacker").fork(commandNode, ExecuteCommand.expandOneToOneEntityRelation(entity -> {
            Optional<Object> optional;
            if (entity instanceof Attackable) {
                Attackable attackable = (Attackable)((Object)entity);
                optional = Optional.ofNullable(attackable.getLastAttacker());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("vehicle").fork(commandNode, ExecuteCommand.expandOneToOneEntityRelation(entity -> Optional.ofNullable(entity.getVehicle()))))).then(Commands.literal("controller").fork(commandNode, ExecuteCommand.expandOneToOneEntityRelation(entity -> Optional.ofNullable(entity.getControllingPassenger()))))).then(Commands.literal("origin").fork(commandNode, ExecuteCommand.expandOneToOneEntityRelation(entity -> {
            Optional<Object> optional;
            if (entity instanceof TraceableEntity) {
                TraceableEntity traceableEntity = (TraceableEntity)((Object)entity);
                optional = Optional.ofNullable(traceableEntity.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("passengers").fork(commandNode, ExecuteCommand.expandOneToManyEntityRelation(entity -> entity.getPassengers().stream())));
    }

    private static CommandSourceStack spawnEntityAndRedirect(CommandSourceStack commandSourceStack, Holder.Reference<EntityType<?>> reference) throws CommandSyntaxException {
        Entity entity = SummonCommand.createEntity(commandSourceStack, reference, commandSourceStack.getPosition(), new CompoundTag(), true);
        return commandSourceStack.withEntity(entity);
    }

    /*
     * Exception decompiling
     */
    public static <T extends ExecutionCommandSource<T>> void scheduleFunctionConditionsAndTest(T var0, List<T> var1_1, Function<T, T> var2_2, IntPredicate var3_3, ContextChain<T> var4_4, @Nullable CompoundTag var5_5, ExecutionControl<T> var6_6, CommandGetter<T, Collection<CommandFunction<T>>> var7_7, ChainModifiers var8_8) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static /* synthetic */ void lambda$scheduleFunctionConditionsAndTest$89(List list, ExecutionCommandSource executionCommandSource, ExecutionControl executionControl) {
        for (InstantiatedFunction instantiatedFunction : list) {
            executionControl.queueNext(new CallFunction<ExecutionCommandSource>(instantiatedFunction, executionControl.currentFrame().returnValueConsumer(), true).bind(executionCommandSource));
        }
        executionControl.queueNext(FallthroughTask.instance());
    }

    private static /* synthetic */ void lambda$scheduleFunctionConditionsAndTest$88(IntPredicate intPredicate, List list, ExecutionCommandSource executionCommandSource, boolean bl, int n) {
        if (intPredicate.test(n)) {
            list.add(executionCommandSource);
        }
    }

    @FunctionalInterface
    static interface CommandPredicate {
        public boolean test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface CommandNumericPredicate {
        public int test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    static class ExecuteIfFunctionCustomModifier
    implements CustomModifierExecutor.ModifierAdapter<CommandSourceStack> {
        private final IntPredicate check;

        ExecuteIfFunctionCustomModifier(boolean bl) {
            this.check = bl ? n -> n != 0 : n -> n == 0;
        }

        @Override
        public void apply(CommandSourceStack commandSourceStack, List<CommandSourceStack> list, ContextChain<CommandSourceStack> contextChain, ChainModifiers chainModifiers, ExecutionControl<CommandSourceStack> executionControl) {
            ExecuteCommand.scheduleFunctionConditionsAndTest(commandSourceStack, list, FunctionCommand::modifySenderForExecution, this.check, contextChain, null, executionControl, commandContext -> FunctionArgument.getFunctions((CommandContext<CommandSourceStack>)commandContext, "name"), chainModifiers);
        }
    }

    @FunctionalInterface
    static interface IntBiPredicate {
        public boolean test(int var1, int var2);
    }

    @FunctionalInterface
    public static interface CommandGetter<T, R> {
        public R get(CommandContext<T> var1) throws CommandSyntaxException;
    }
}

