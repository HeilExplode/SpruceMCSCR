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
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.util.Either
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType((Message)Component.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.fillbiome.toobig", object, object2));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fillbiome").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("biome", ResourceArgument.resource(commandBuildContext, Registries.BIOME)).executes(commandContext -> FillBiomeCommand.fill((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to"), ResourceArgument.getResource((CommandContext<CommandSourceStack>)commandContext, "biome", Registries.BIOME), holder -> true))).then(Commands.literal("replace").then(Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME)).executes(commandContext -> FillBiomeCommand.fill((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to"), ResourceArgument.getResource((CommandContext<CommandSourceStack>)commandContext, "biome", Registries.BIOME), ResourceOrTagArgument.getResourceOrTag((CommandContext<CommandSourceStack>)commandContext, "filter", Registries.BIOME)::test))))))));
    }

    private static int quantize(int n) {
        return QuartPos.toBlock(QuartPos.fromBlock(n));
    }

    private static BlockPos quantize(BlockPos blockPos) {
        return new BlockPos(FillBiomeCommand.quantize(blockPos.getX()), FillBiomeCommand.quantize(blockPos.getY()), FillBiomeCommand.quantize(blockPos.getZ()));
    }

    private static BiomeResolver makeResolver(MutableInt mutableInt, ChunkAccess chunkAccess, BoundingBox boundingBox, Holder<Biome> holder, Predicate<Holder<Biome>> predicate) {
        return (n, n2, n3, sampler) -> {
            int n4 = QuartPos.toBlock(n);
            int n5 = QuartPos.toBlock(n2);
            int n6 = QuartPos.toBlock(n3);
            Holder<Biome> holder2 = chunkAccess.getNoiseBiome(n, n2, n3);
            if (boundingBox.isInside(n4, n5, n6) && predicate.test(holder2)) {
                mutableInt.increment();
                return holder;
            }
            return holder2;
        };
    }

    public static Either<Integer, CommandSyntaxException> fill(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, Holder<Biome> holder2) {
        return FillBiomeCommand.fill(serverLevel, blockPos, blockPos2, holder2, holder -> true, supplier -> {});
    }

    public static Either<Integer, CommandSyntaxException> fill(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, Holder<Biome> holder, Predicate<Holder<Biome>> predicate, Consumer<Supplier<Component>> consumer) {
        int n;
        BlockPos blockPos3;
        BlockPos blockPos4 = FillBiomeCommand.quantize(blockPos);
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos4, blockPos3 = FillBiomeCommand.quantize(blockPos2));
        int n2 = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (n2 > (n = serverLevel.getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            return Either.right((Object)((Object)ERROR_VOLUME_TOO_LARGE.create((Object)n, (Object)n2)));
        }
        ArrayList<ChunkAccess> arrayList = new ArrayList<ChunkAccess>();
        for (int i = SectionPos.blockToSectionCoord(boundingBox.minZ()); i <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); ++i) {
            for (int j = SectionPos.blockToSectionCoord(boundingBox.minX()); j <= SectionPos.blockToSectionCoord(boundingBox.maxX()); ++j) {
                ChunkAccess chunkAccess = serverLevel.getChunk(j, i, ChunkStatus.FULL, false);
                if (chunkAccess == null) {
                    return Either.right((Object)((Object)ERROR_NOT_LOADED.create()));
                }
                arrayList.add(chunkAccess);
            }
        }
        MutableInt mutableInt = new MutableInt(0);
        for (ChunkAccess chunkAccess : arrayList) {
            chunkAccess.fillBiomesFromNoise(FillBiomeCommand.makeResolver(mutableInt, chunkAccess, boundingBox, holder, predicate), serverLevel.getChunkSource().randomState().sampler());
            chunkAccess.setUnsaved(true);
        }
        serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(arrayList);
        consumer.accept(() -> Component.translatable("commands.fillbiome.success.count", mutableInt.getValue(), boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()));
        return Either.left((Object)mutableInt.getValue());
    }

    private static int fill(CommandSourceStack commandSourceStack, BlockPos blockPos, BlockPos blockPos2, Holder.Reference<Biome> reference, Predicate<Holder<Biome>> predicate) throws CommandSyntaxException {
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fill(commandSourceStack.getLevel(), blockPos, blockPos2, reference, predicate, supplier -> commandSourceStack.sendSuccess((Supplier<Component>)supplier, true));
        Optional optional = either.right();
        if (optional.isPresent()) {
            throw (CommandSyntaxException)((Object)optional.get());
        }
        return (Integer)either.left().get();
    }
}

