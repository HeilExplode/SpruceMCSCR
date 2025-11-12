/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PlaceCommand {
    private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.feature.failed"));
    private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.jigsaw.failed"));
    private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.structure.failed"));
    private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.place.template.invalid", object));
    private static final SimpleCommandExceptionType ERROR_TEMPLATE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.template.failed"));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEMPLATES = (commandContext, suggestionsBuilder) -> {
        StructureTemplateManager structureTemplateManager = ((CommandSourceStack)commandContext.getSource()).getLevel().getStructureManager();
        return SharedSuggestionProvider.suggestResource(structureTemplateManager.listTemplates(), suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("place").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("feature").then(((RequiredArgumentBuilder)Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE)).executes(commandContext -> PlaceCommand.placeFeature((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getConfiguredFeature((CommandContext<CommandSourceStack>)commandContext, "feature"), BlockPos.containing(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> PlaceCommand.placeFeature((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getConfiguredFeature((CommandContext<CommandSourceStack>)commandContext, "feature"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"))))))).then(Commands.literal("jigsaw").then(Commands.argument("pool", ResourceKeyArgument.key(Registries.TEMPLATE_POOL)).then(Commands.argument("target", ResourceLocationArgument.id()).then(((RequiredArgumentBuilder)Commands.argument("max_depth", IntegerArgumentType.integer((int)1, (int)20)).executes(commandContext -> PlaceCommand.placeJigsaw((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructureTemplatePool((CommandContext<CommandSourceStack>)commandContext, "pool"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"max_depth"), BlockPos.containing(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("position", BlockPosArgument.blockPos()).executes(commandContext -> PlaceCommand.placeJigsaw((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructureTemplatePool((CommandContext<CommandSourceStack>)commandContext, "pool"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"max_depth"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "position"))))))))).then(Commands.literal("structure").then(((RequiredArgumentBuilder)Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE)).executes(commandContext -> PlaceCommand.placeStructure((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructure((CommandContext<CommandSourceStack>)commandContext, "structure"), BlockPos.containing(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> PlaceCommand.placeStructure((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructure((CommandContext<CommandSourceStack>)commandContext, "structure"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"))))))).then(Commands.literal("template").then(((RequiredArgumentBuilder)Commands.argument("template", ResourceLocationArgument.id()).suggests(SUGGEST_TEMPLATES).executes(commandContext -> PlaceCommand.placeTemplate((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "template"), BlockPos.containing(((CommandSourceStack)commandContext.getSource()).getPosition()), Rotation.NONE, Mirror.NONE, 1.0f, 0))).then(((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> PlaceCommand.placeTemplate((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), Rotation.NONE, Mirror.NONE, 1.0f, 0))).then(((RequiredArgumentBuilder)Commands.argument("rotation", TemplateRotationArgument.templateRotation()).executes(commandContext -> PlaceCommand.placeTemplate((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rotation"), Mirror.NONE, 1.0f, 0))).then(((RequiredArgumentBuilder)Commands.argument("mirror", TemplateMirrorArgument.templateMirror()).executes(commandContext -> PlaceCommand.placeTemplate((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rotation"), TemplateMirrorArgument.getMirror((CommandContext<CommandSourceStack>)commandContext, "mirror"), 1.0f, 0))).then(((RequiredArgumentBuilder)Commands.argument("integrity", FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes(commandContext -> PlaceCommand.placeTemplate((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rotation"), TemplateMirrorArgument.getMirror((CommandContext<CommandSourceStack>)commandContext, "mirror"), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"integrity"), 0))).then(Commands.argument("seed", IntegerArgumentType.integer()).executes(commandContext -> PlaceCommand.placeTemplate((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rotation"), TemplateMirrorArgument.getMirror((CommandContext<CommandSourceStack>)commandContext, "mirror"), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"integrity"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seed")))))))))));
    }

    public static int placeFeature(CommandSourceStack commandSourceStack, Holder.Reference<ConfiguredFeature<?, ?>> reference, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ConfiguredFeature<?, ?> configuredFeature = reference.value();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        PlaceCommand.checkLoaded(serverLevel, new ChunkPos(chunkPos.x - 1, chunkPos.z - 1), new ChunkPos(chunkPos.x + 1, chunkPos.z + 1));
        if (!configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos)) {
            throw ERROR_FEATURE_FAILED.create();
        }
        String string = reference.key().location().toString();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.place.feature.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    public static int placeJigsaw(CommandSourceStack commandSourceStack, Holder<StructureTemplatePool> holder, ResourceLocation resourceLocation, int n, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        PlaceCommand.checkLoaded(serverLevel, chunkPos, chunkPos);
        if (!JigsawPlacement.generateJigsaw(serverLevel, holder, resourceLocation, n, blockPos, false)) {
            throw ERROR_JIGSAW_FAILED.create();
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.place.jigsaw.success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    public static int placeStructure(CommandSourceStack commandSourceStack, Holder.Reference<Structure> reference, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        Structure structure = reference.value();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        StructureStart structureStart = structure.generate(commandSourceStack.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), serverLevel.getChunkSource().randomState(), serverLevel.getStructureManager(), serverLevel.getSeed(), new ChunkPos(blockPos), 0, serverLevel, holder -> true);
        if (!structureStart.isValid()) {
            throw ERROR_STRUCTURE_FAILED.create();
        }
        BoundingBox boundingBox = structureStart.getBoundingBox();
        ChunkPos chunkPos2 = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
        ChunkPos chunkPos3 = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
        PlaceCommand.checkLoaded(serverLevel, chunkPos2, chunkPos3);
        ChunkPos.rangeClosed(chunkPos2, chunkPos3).forEach(chunkPos -> structureStart.placeInChunk(serverLevel, serverLevel.structureManager(), chunkGenerator, serverLevel.getRandom(), new BoundingBox(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight(), chunkPos.getMaxBlockZ()), (ChunkPos)chunkPos));
        String string = reference.key().location().toString();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.place.structure.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    public static int placeTemplate(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, Mirror mirror, float f, int n) throws CommandSyntaxException {
        boolean bl;
        Optional<StructureTemplate> optional;
        ServerLevel serverLevel = commandSourceStack.getLevel();
        StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
        try {
            optional = structureTemplateManager.get(resourceLocation);
        }
        catch (ResourceLocationException resourceLocationException) {
            throw ERROR_TEMPLATE_INVALID.create((Object)resourceLocation);
        }
        if (optional.isEmpty()) {
            throw ERROR_TEMPLATE_INVALID.create((Object)resourceLocation);
        }
        StructureTemplate structureTemplate = optional.get();
        PlaceCommand.checkLoaded(serverLevel, new ChunkPos(blockPos), new ChunkPos(blockPos.offset(structureTemplate.getSize())));
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setMirror(mirror).setRotation(rotation);
        if (f < 1.0f) {
            structurePlaceSettings.clearProcessors().addProcessor(new BlockRotProcessor(f)).setRandom(StructureBlockEntity.createRandom(n));
        }
        if (!(bl = structureTemplate.placeInWorld(serverLevel, blockPos, blockPos, structurePlaceSettings, StructureBlockEntity.createRandom(n), 2))) {
            throw ERROR_TEMPLATE_FAILED.create();
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.place.template.success", Component.translationArg(resourceLocation), blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    private static void checkLoaded(ServerLevel serverLevel, ChunkPos chunkPos2, ChunkPos chunkPos3) throws CommandSyntaxException {
        if (ChunkPos.rangeClosed(chunkPos2, chunkPos3).filter(chunkPos -> !serverLevel.isLoaded(chunkPos.getWorldPosition())).findAny().isPresent()) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
    }
}

