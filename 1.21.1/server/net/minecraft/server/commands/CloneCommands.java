/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelTicks;

public class CloneCommands {
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType((Message)Component.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.clone.toobig", object, object2));
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = blockInWorld -> !blockInWorld.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clone").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(CloneCommands.beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> ((CommandSourceStack)commandContext.getSource()).getLevel()))).then(Commands.literal("from").then(Commands.argument("sourceDimension", DimensionArgument.dimension()).then(CloneCommands.beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)commandContext, "sourceDimension"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(CommandBuildContext commandBuildContext, CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction) {
        return Commands.argument("begin", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("end", BlockPosArgument.blockPos()).then(CloneCommands.destinationAndModeSuffix(commandBuildContext, commandFunction, commandContext -> ((CommandSourceStack)commandContext.getSource()).getLevel()))).then(Commands.literal("to").then(Commands.argument("targetDimension", DimensionArgument.dimension()).then(CloneCommands.destinationAndModeSuffix(commandBuildContext, commandFunction, commandContext -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)commandContext, "targetDimension"))))));
    }

    private static DimensionAndPosition getLoadedDimensionAndPosition(CommandContext<CommandSourceStack> commandContext, ServerLevel serverLevel, String string) throws CommandSyntaxException {
        BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(commandContext, serverLevel, string);
        return new DimensionAndPosition(serverLevel, blockPos);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destinationAndModeSuffix(CommandBuildContext commandBuildContext, CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction, CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction2) {
        CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction3 = commandContext -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)commandContext, (ServerLevel)commandFunction.apply((CommandContext<CommandSourceStack>)commandContext), "begin");
        CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction4 = commandContext -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)commandContext, (ServerLevel)commandFunction.apply((CommandContext<CommandSourceStack>)commandContext), "end");
        CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction5 = commandContext -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)commandContext, (ServerLevel)commandFunction2.apply((CommandContext<CommandSourceStack>)commandContext), "destination");
        return ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("destination", BlockPosArgument.blockPos()).executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), blockInWorld -> true, Mode.NORMAL))).then(CloneCommands.wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> blockInWorld -> true, Commands.literal("replace").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), blockInWorld -> true, Mode.NORMAL))))).then(CloneCommands.wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> FILTER_AIR, Commands.literal("masked").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), FILTER_AIR, Mode.NORMAL))))).then(Commands.literal("filtered").then(CloneCommands.wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)commandContext, "filter"), Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandBuildContext)).executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)commandContext, "filter"), Mode.NORMAL)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction, CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction2, CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction3, CommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> commandFunction4, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder) {
        return argumentBuilder.then(Commands.literal("force").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction.apply(commandContext), (DimensionAndPosition)commandFunction2.apply(commandContext), (DimensionAndPosition)commandFunction3.apply(commandContext), (Predicate)commandFunction4.apply(commandContext), Mode.FORCE))).then(Commands.literal("move").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction.apply(commandContext), (DimensionAndPosition)commandFunction2.apply(commandContext), (DimensionAndPosition)commandFunction3.apply(commandContext), (Predicate)commandFunction4.apply(commandContext), Mode.MOVE))).then(Commands.literal("normal").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction.apply(commandContext), (DimensionAndPosition)commandFunction2.apply(commandContext), (DimensionAndPosition)commandFunction3.apply(commandContext), (Predicate)commandFunction4.apply(commandContext), Mode.NORMAL)));
    }

    private static int clone(CommandSourceStack commandSourceStack, DimensionAndPosition dimensionAndPosition, DimensionAndPosition dimensionAndPosition2, DimensionAndPosition dimensionAndPosition3, Predicate<BlockInWorld> predicate, Mode mode) throws CommandSyntaxException {
        Object object;
        Object object22;
        int n;
        BlockPos blockPos = dimensionAndPosition.position();
        BlockPos blockPos2 = dimensionAndPosition2.position();
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
        BlockPos blockPos3 = dimensionAndPosition3.position();
        BlockPos blockPos4 = blockPos3.offset(boundingBox.getLength());
        BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos4);
        ServerLevel serverLevel = dimensionAndPosition.dimension();
        ServerLevel serverLevel2 = dimensionAndPosition3.dimension();
        if (!mode.canOverlap() && serverLevel == serverLevel2 && boundingBox2.intersects(boundingBox)) {
            throw ERROR_OVERLAP.create();
        }
        int n2 = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (n2 > (n = commandSourceStack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            throw ERROR_AREA_TOO_LARGE.create((Object)n, (Object)n2);
        }
        if (!serverLevel.hasChunksAt(blockPos, blockPos2) || !serverLevel2.hasChunksAt(blockPos3, blockPos4)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
        ArrayList arrayList = Lists.newArrayList();
        ArrayList arrayList2 = Lists.newArrayList();
        ArrayList arrayList3 = Lists.newArrayList();
        LinkedList linkedList = Lists.newLinkedList();
        BlockPos blockPos5 = new BlockPos(boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ());
        for (int i = boundingBox.minZ(); i <= boundingBox.maxZ(); ++i) {
            for (int j = boundingBox.minY(); j <= boundingBox.maxY(); ++j) {
                for (int k = boundingBox.minX(); k <= boundingBox.maxX(); ++k) {
                    Iterator iterator = new BlockPos(k, j, i);
                    object22 = ((BlockPos)((Object)iterator)).offset(blockPos5);
                    object = new BlockInWorld(serverLevel, (BlockPos)((Object)iterator), false);
                    BlockState blockState = ((BlockInWorld)object).getState();
                    if (!predicate.test((BlockInWorld)object)) continue;
                    BlockEntity blockEntity = serverLevel.getBlockEntity((BlockPos)((Object)iterator));
                    if (blockEntity != null) {
                        CloneBlockEntityInfo cloneBlockEntityInfo = new CloneBlockEntityInfo(blockEntity.saveCustomOnly(commandSourceStack.registryAccess()), blockEntity.components());
                        arrayList2.add(new CloneBlockInfo((BlockPos)object22, blockState, cloneBlockEntityInfo));
                        linkedList.addLast(iterator);
                        continue;
                    }
                    if (blockState.isSolidRender(serverLevel, (BlockPos)((Object)iterator)) || blockState.isCollisionShapeFullBlock(serverLevel, (BlockPos)((Object)iterator))) {
                        arrayList.add(new CloneBlockInfo((BlockPos)object22, blockState, null));
                        linkedList.addLast(iterator);
                        continue;
                    }
                    arrayList3.add(new CloneBlockInfo((BlockPos)object22, blockState, null));
                    linkedList.addFirst(iterator);
                }
            }
        }
        if (mode == Mode.MOVE) {
            for (BlockPos blockPos6 : linkedList) {
                BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos6);
                Clearable.tryClear(blockEntity);
                serverLevel.setBlock(blockPos6, Blocks.BARRIER.defaultBlockState(), 2);
            }
            for (BlockPos blockPos7 : linkedList) {
                serverLevel.setBlock(blockPos7, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        ArrayList arrayList4 = Lists.newArrayList();
        arrayList4.addAll(arrayList);
        arrayList4.addAll(arrayList2);
        arrayList4.addAll(arrayList3);
        List list = Lists.reverse((List)arrayList4);
        for (Iterator iterator : list) {
            object22 = serverLevel2.getBlockEntity(((CloneBlockInfo)((Object)iterator)).pos);
            Clearable.tryClear(object22);
            serverLevel2.setBlock(((CloneBlockInfo)((Object)iterator)).pos, Blocks.BARRIER.defaultBlockState(), 2);
        }
        int n3 = 0;
        for (Object object22 : arrayList4) {
            if (!serverLevel2.setBlock(((CloneBlockInfo)object22).pos, ((CloneBlockInfo)object22).state, 2)) continue;
            ++n3;
        }
        for (Object object22 : arrayList2) {
            object = serverLevel2.getBlockEntity(((CloneBlockInfo)object22).pos);
            if (((CloneBlockInfo)object22).blockEntityInfo != null && object != null) {
                ((BlockEntity)object).loadCustomOnly(((CloneBlockInfo)object22).blockEntityInfo.tag, serverLevel2.registryAccess());
                ((BlockEntity)object).setComponents(((CloneBlockInfo)object22).blockEntityInfo.components);
                ((BlockEntity)object).setChanged();
            }
            serverLevel2.setBlock(((CloneBlockInfo)object22).pos, ((CloneBlockInfo)object22).state, 2);
        }
        for (Object object22 : list) {
            serverLevel2.blockUpdated(((CloneBlockInfo)object22).pos, ((CloneBlockInfo)object22).state.getBlock());
        }
        ((LevelTicks)serverLevel2.getBlockTicks()).copyAreaFrom(serverLevel.getBlockTicks(), boundingBox, blockPos5);
        if (n3 == 0) {
            throw ERROR_FAILED.create();
        }
        int n4 = n3;
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.clone.success", n4), true);
        return n3;
    }

    @FunctionalInterface
    static interface CommandFunction<T, R> {
        public R apply(T var1) throws CommandSyntaxException;
    }

    record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean canOverlap;

        private Mode(boolean bl) {
            this.canOverlap = bl;
        }

        public boolean canOverlap() {
            return this.canOverlap;
        }
    }

    static final class CloneBlockEntityInfo
    extends Record {
        final CompoundTag tag;
        final DataComponentMap components;

        CloneBlockEntityInfo(CompoundTag compoundTag, DataComponentMap dataComponentMap) {
            this.tag = compoundTag;
            this.components = dataComponentMap;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this, object);
        }

        public CompoundTag tag() {
            return this.tag;
        }

        public DataComponentMap components() {
            return this.components;
        }
    }

    static final class CloneBlockInfo
    extends Record {
        final BlockPos pos;
        final BlockState state;
        @Nullable
        final CloneBlockEntityInfo blockEntityInfo;

        CloneBlockInfo(BlockPos blockPos, BlockState blockState, @Nullable CloneBlockEntityInfo cloneBlockEntityInfo) {
            this.pos = blockPos;
            this.state = blockState;
            this.blockEntityInfo = cloneBlockEntityInfo;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo", "pos", "state", "blockEntityInfo"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo", "pos", "state", "blockEntityInfo"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo", "pos", "state", "blockEntityInfo"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }

        @Nullable
        public CloneBlockEntityInfo blockEntityInfo() {
            return this.blockEntityInfo;
        }
    }
}

