/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.StructureBlockPosFinder;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.gametest.framework.TestFunctionArgument;
import net.minecraft.gametest.framework.TestFunctionFinder;

public class TestFinder<T>
implements StructureBlockPosFinder,
TestFunctionFinder {
    static final TestFunctionFinder NO_FUNCTIONS = Stream::empty;
    static final StructureBlockPosFinder NO_STRUCTURES = Stream::empty;
    private final TestFunctionFinder testFunctionFinder;
    private final StructureBlockPosFinder structureBlockPosFinder;
    private final CommandSourceStack source;
    private final Function<TestFinder<T>, T> contextProvider;

    @Override
    public Stream<BlockPos> findStructureBlockPos() {
        return this.structureBlockPosFinder.findStructureBlockPos();
    }

    TestFinder(CommandSourceStack commandSourceStack, Function<TestFinder<T>, T> function, TestFunctionFinder testFunctionFinder, StructureBlockPosFinder structureBlockPosFinder) {
        this.source = commandSourceStack;
        this.contextProvider = function;
        this.testFunctionFinder = testFunctionFinder;
        this.structureBlockPosFinder = structureBlockPosFinder;
    }

    T get() {
        return this.contextProvider.apply(this);
    }

    public CommandSourceStack source() {
        return this.source;
    }

    @Override
    public Stream<TestFunction> findTestFunctions() {
        return this.testFunctionFinder.findTestFunctions();
    }

    public static class Builder<T> {
        private final Function<TestFinder<T>, T> contextProvider;
        private final UnaryOperator<Supplier<Stream<TestFunction>>> testFunctionFinderWrapper;
        private final UnaryOperator<Supplier<Stream<BlockPos>>> structureBlockPosFinderWrapper;

        public Builder(Function<TestFinder<T>, T> function) {
            this.contextProvider = function;
            this.testFunctionFinderWrapper = supplier -> supplier;
            this.structureBlockPosFinderWrapper = supplier -> supplier;
        }

        private Builder(Function<TestFinder<T>, T> function, UnaryOperator<Supplier<Stream<TestFunction>>> unaryOperator, UnaryOperator<Supplier<Stream<BlockPos>>> unaryOperator2) {
            this.contextProvider = function;
            this.testFunctionFinderWrapper = unaryOperator;
            this.structureBlockPosFinderWrapper = unaryOperator2;
        }

        public Builder<T> createMultipleCopies(int n) {
            return new Builder<T>(this.contextProvider, Builder.createCopies(n), Builder.createCopies(n));
        }

        private static <Q> UnaryOperator<Supplier<Stream<Q>>> createCopies(int n) {
            return supplier -> {
                LinkedList linkedList = new LinkedList();
                List list = ((Stream)supplier.get()).toList();
                for (int i = 0; i < n; ++i) {
                    linkedList.addAll(list);
                }
                return linkedList::stream;
            };
        }

        private T build(CommandSourceStack commandSourceStack, TestFunctionFinder testFunctionFinder, StructureBlockPosFinder structureBlockPosFinder) {
            return new TestFinder<T>(commandSourceStack, this.contextProvider, ((Supplier)((Supplier)this.testFunctionFinderWrapper.apply(testFunctionFinder::findTestFunctions)))::get, ((Supplier)((Supplier)this.structureBlockPosFinderWrapper.apply(structureBlockPosFinder::findStructureBlockPos)))::get).get();
        }

        public T radius(CommandContext<CommandSourceStack> commandContext, int n) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.findStructureBlocks(blockPos, n, commandSourceStack.getLevel()));
        }

        public T nearest(CommandContext<CommandSourceStack> commandContext) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.findNearestStructureBlock(blockPos, 15, commandSourceStack.getLevel()).stream());
        }

        public T allNearby(CommandContext<CommandSourceStack> commandContext) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.findStructureBlocks(blockPos, 200, commandSourceStack.getLevel()));
        }

        public T lookedAt(CommandContext<CommandSourceStack> commandContext) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.lookedAtStructureBlockPos(BlockPos.containing(commandSourceStack.getPosition()), commandSourceStack.getPlayer().getCamera(), commandSourceStack.getLevel()));
        }

        public T allTests(CommandContext<CommandSourceStack> commandContext) {
            return this.build((CommandSourceStack)commandContext.getSource(), () -> GameTestRegistry.getAllTestFunctions().stream().filter(testFunction -> !testFunction.manualOnly()), NO_STRUCTURES);
        }

        public T allTestsInClass(CommandContext<CommandSourceStack> commandContext, String string) {
            return this.build((CommandSourceStack)commandContext.getSource(), () -> GameTestRegistry.getTestFunctionsForClassName(string).filter(testFunction -> !testFunction.manualOnly()), NO_STRUCTURES);
        }

        public T failedTests(CommandContext<CommandSourceStack> commandContext, boolean bl) {
            return this.build((CommandSourceStack)commandContext.getSource(), () -> GameTestRegistry.getLastFailedTests().filter(testFunction -> !bl || testFunction.required()), NO_STRUCTURES);
        }

        public T byArgument(CommandContext<CommandSourceStack> commandContext, String string) {
            return this.build((CommandSourceStack)commandContext.getSource(), () -> Stream.of(TestFunctionArgument.getTestFunction(commandContext, string)), NO_STRUCTURES);
        }

        public T locateByName(CommandContext<CommandSourceStack> commandContext, String string) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.findStructureByTestFunction(blockPos, 1024, commandSourceStack.getLevel(), string));
        }

        public T failedTests(CommandContext<CommandSourceStack> commandContext) {
            return this.failedTests(commandContext, false);
        }
    }
}

