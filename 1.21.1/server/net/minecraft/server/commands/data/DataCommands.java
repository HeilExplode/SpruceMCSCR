/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterables
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.commands.data.StorageDataAccessor;
import net.minecraft.util.Mth;

public class DataCommands {
    private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType((Message)Component.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.data.get.invalid", object));
    private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.data.get.unknown", object));
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType((Message)Component.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.data.modify.expected_object", object));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.data.modify.expected_value", object));
    private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.data.modify.invalid_substring", object, object2));
    public static final List<Function<String, DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);
    public static final List<DataProvider> TARGET_PROVIDERS = (List)ALL_PROVIDERS.stream().map(function -> (DataProvider)function.apply("target")).collect(ImmutableList.toImmutableList());
    public static final List<DataProvider> SOURCE_PROVIDERS = (List)ALL_PROVIDERS.stream().map(function -> (DataProvider)function.apply("source")).collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)Commands.literal("data").requires(commandSourceStack -> commandSourceStack.hasPermission(2));
        for (DataProvider dataProvider : TARGET_PROVIDERS) {
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(dataProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("merge"), argumentBuilder -> argumentBuilder.then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(commandContext -> DataCommands.mergeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), CompoundTagArgument.getCompoundTag(commandContext, "nbt"))))))).then(dataProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("get"), argumentBuilder -> argumentBuilder.executes(commandContext -> DataCommands.getData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext))).then(((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.getData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path")))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> DataCommands.getNumeric((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale")))))))).then(dataProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("remove"), argumentBuilder -> argumentBuilder.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.removeData((CommandSourceStack)commandContext.getSource(), dataProvider.access((CommandContext<CommandSourceStack>)commandContext), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)commandContext, "path"))))))).then(DataCommands.decorateModification((argumentBuilder, dataManipulatorDecorator) -> argumentBuilder.then(Commands.literal("insert").then(Commands.argument("index", IntegerArgumentType.integer()).then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.insert(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"index"), compoundTag, list))))).then(Commands.literal("prepend").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.insert(0, compoundTag, list)))).then(Commands.literal("append").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.insert(-1, compoundTag, list)))).then(Commands.literal("set").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.set(compoundTag, (Tag)Iterables.getLast((Iterable)list))))).then(Commands.literal("merge").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> {
                Object object;
                CompoundTag compoundTag2 = new CompoundTag();
                for (Tag tag : list) {
                    if (NbtPathArgument.NbtPath.isTooDeep(tag, 0)) {
                        throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                    }
                    if (tag instanceof CompoundTag) {
                        object = (CompoundTag)tag;
                        compoundTag2.merge((CompoundTag)object);
                        continue;
                    }
                    throw ERROR_EXPECTED_OBJECT.create((Object)tag);
                }
                List<Tag> list2 = nbtPath.getOrCreate(compoundTag, CompoundTag::new);
                int n = 0;
                object = list2.iterator();
                while (object.hasNext()) {
                    Tag tag = (Tag)object.next();
                    if (!(tag instanceof CompoundTag)) {
                        throw ERROR_EXPECTED_OBJECT.create((Object)tag);
                    }
                    CompoundTag compoundTag3 = (CompoundTag)tag;
                    CompoundTag compoundTag4 = compoundTag3.copy();
                    compoundTag3.merge(compoundTag2);
                    n += compoundTag4.equals(compoundTag3) ? 0 : 1;
                }
                return n;
            })))));
        }
        commandDispatcher.register(literalArgumentBuilder);
    }

    private static String getAsText(Tag tag) throws CommandSyntaxException {
        if (tag.getType().isValue()) {
            return tag.getAsString();
        }
        throw ERROR_EXPECTED_VALUE.create((Object)tag);
    }

    private static List<Tag> stringifyTagList(List<Tag> list, StringProcessor stringProcessor) throws CommandSyntaxException {
        ArrayList<Tag> arrayList = new ArrayList<Tag>(list.size());
        for (Tag tag : list) {
            String string = DataCommands.getAsText(tag);
            arrayList.add(StringTag.valueOf(stringProcessor.process(string)));
        }
        return arrayList;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataManipulatorDecorator> biConsumer) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("modify");
        for (DataProvider dataProvider : TARGET_PROVIDERS) {
            dataProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)literalArgumentBuilder, argumentBuilder -> {
                RequiredArgumentBuilder<CommandSourceStack, NbtPathArgument.NbtPath> requiredArgumentBuilder = Commands.argument("targetPath", NbtPathArgument.nbtPath());
                for (DataProvider dataProvider2 : SOURCE_PROVIDERS) {
                    biConsumer.accept((ArgumentBuilder<CommandSourceStack, ?>)requiredArgumentBuilder, dataManipulator -> dataProvider2.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("from"), argumentBuilder -> argumentBuilder.executes(commandContext -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)commandContext, dataProvider, dataManipulator, DataCommands.getSingletonSource((CommandContext<CommandSourceStack>)commandContext, dataProvider2))).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)commandContext, dataProvider, dataManipulator, DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)commandContext, dataProvider2))))));
                    biConsumer.accept((ArgumentBuilder<CommandSourceStack, ?>)requiredArgumentBuilder, dataManipulator -> dataProvider2.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("string"), argumentBuilder -> argumentBuilder.executes(commandContext -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.getSingletonSource((CommandContext<CommandSourceStack>)commandContext, dataProvider2), string -> string))).then(((RequiredArgumentBuilder)Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)commandContext, dataProvider2), string -> string)))).then(((RequiredArgumentBuilder)Commands.argument("start", IntegerArgumentType.integer()).executes(commandContext -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)commandContext, dataProvider2), string -> DataCommands.substring(string, IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"start")))))).then(Commands.argument("end", IntegerArgumentType.integer()).executes(commandContext -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)commandContext, dataProvider2), string -> DataCommands.substring(string, IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"start"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"end"))))))))));
                }
                biConsumer.accept((ArgumentBuilder<CommandSourceStack, ?>)requiredArgumentBuilder, dataManipulator -> Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag()).executes(commandContext -> {
                    List<Tag> list = Collections.singletonList(NbtTagArgument.getNbtTag(commandContext, "value"));
                    return DataCommands.manipulateData((CommandContext<CommandSourceStack>)commandContext, dataProvider, dataManipulator, list);
                })));
                return argumentBuilder.then(requiredArgumentBuilder);
            });
        }
        return literalArgumentBuilder;
    }

    private static String validatedSubstring(String string, int n, int n2) throws CommandSyntaxException {
        if (n < 0 || n2 > string.length() || n > n2) {
            throw ERROR_INVALID_SUBSTRING.create((Object)n, (Object)n2);
        }
        return string.substring(n, n2);
    }

    private static String substring(String string, int n, int n2) throws CommandSyntaxException {
        int n3 = string.length();
        int n4 = DataCommands.getOffset(n, n3);
        int n5 = DataCommands.getOffset(n2, n3);
        return DataCommands.validatedSubstring(string, n4, n5);
    }

    private static String substring(String string, int n) throws CommandSyntaxException {
        int n2 = string.length();
        return DataCommands.validatedSubstring(string, DataCommands.getOffset(n, n2), n2);
    }

    private static int getOffset(int n, int n2) {
        return n >= 0 ? n : n2 + n;
    }

    private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> commandContext, DataProvider dataProvider) throws CommandSyntaxException {
        DataAccessor dataAccessor = dataProvider.access(commandContext);
        return Collections.singletonList(dataAccessor.getData());
    }

    private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> commandContext, DataProvider dataProvider) throws CommandSyntaxException {
        DataAccessor dataAccessor = dataProvider.access(commandContext);
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(commandContext, "sourcePath");
        return nbtPath.get(dataAccessor.getData());
    }

    private static int manipulateData(CommandContext<CommandSourceStack> commandContext, DataProvider dataProvider, DataManipulator dataManipulator, List<Tag> list) throws CommandSyntaxException {
        DataAccessor dataAccessor = dataProvider.access(commandContext);
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(commandContext, "targetPath");
        CompoundTag compoundTag = dataAccessor.getData();
        int n = dataManipulator.modify(commandContext, compoundTag, nbtPath, list);
        if (n == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        dataAccessor.setData(compoundTag);
        ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> dataAccessor.getModifiedSuccess(), true);
        return n;
    }

    private static int removeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
        CompoundTag compoundTag = dataAccessor.getData();
        int n = nbtPath.remove(compoundTag);
        if (n == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        dataAccessor.setData(compoundTag);
        commandSourceStack.sendSuccess(() -> dataAccessor.getModifiedSuccess(), true);
        return n;
    }

    public static Tag getSingleTag(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor) throws CommandSyntaxException {
        List<Tag> list = nbtPath.get(dataAccessor.getData());
        Iterator iterator = list.iterator();
        Tag tag = (Tag)iterator.next();
        if (iterator.hasNext()) {
            throw ERROR_MULTIPLE_TAGS.create();
        }
        return tag;
    }

    private static int getData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
        int n;
        Tag tag = DataCommands.getSingleTag(nbtPath, dataAccessor);
        if (tag instanceof NumericTag) {
            n = Mth.floor(((NumericTag)tag).getAsDouble());
        } else if (tag instanceof CollectionTag) {
            n = ((CollectionTag)tag).size();
        } else if (tag instanceof CompoundTag) {
            n = ((CompoundTag)tag).size();
        } else if (tag instanceof StringTag) {
            n = tag.getAsString().length();
        } else {
            throw ERROR_GET_NON_EXISTENT.create((Object)nbtPath.toString());
        }
        commandSourceStack.sendSuccess(() -> dataAccessor.getPrintSuccess(tag), false);
        return n;
    }

    private static int getNumeric(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath, double d) throws CommandSyntaxException {
        Tag tag = DataCommands.getSingleTag(nbtPath, dataAccessor);
        if (!(tag instanceof NumericTag)) {
            throw ERROR_GET_NOT_NUMBER.create((Object)nbtPath.toString());
        }
        int n = Mth.floor(((NumericTag)tag).getAsDouble() * d);
        commandSourceStack.sendSuccess(() -> dataAccessor.getPrintSuccess(nbtPath, d, n), false);
        return n;
    }

    private static int getData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor) throws CommandSyntaxException {
        CompoundTag compoundTag = dataAccessor.getData();
        commandSourceStack.sendSuccess(() -> dataAccessor.getPrintSuccess(compoundTag), false);
        return 1;
    }

    private static int mergeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, CompoundTag compoundTag) throws CommandSyntaxException {
        CompoundTag compoundTag2 = dataAccessor.getData();
        if (NbtPathArgument.NbtPath.isTooDeep(compoundTag, 0)) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
        }
        CompoundTag compoundTag3 = compoundTag2.copy().merge(compoundTag);
        if (compoundTag2.equals(compoundTag3)) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        dataAccessor.setData(compoundTag3);
        commandSourceStack.sendSuccess(() -> dataAccessor.getModifiedSuccess(), true);
        return 1;
    }

    public static interface DataProvider {
        public DataAccessor access(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> var1, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> var2);
    }

    @FunctionalInterface
    static interface StringProcessor {
        public String process(String var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface DataManipulator {
        public int modify(CommandContext<CommandSourceStack> var1, CompoundTag var2, NbtPathArgument.NbtPath var3, List<Tag> var4) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface DataManipulatorDecorator {
        public ArgumentBuilder<CommandSourceStack, ?> create(DataManipulator var1);
    }
}

