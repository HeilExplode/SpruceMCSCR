/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.functions.FunctionBuilder;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface CommandFunction<T> {
    public ResourceLocation id();

    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag var1, CommandDispatcher<T> var2) throws FunctionInstantiationException;

    private static boolean shouldConcatenateNextLine(CharSequence charSequence) {
        int n = charSequence.length();
        return n > 0 && charSequence.charAt(n - 1) == '\\';
    }

    public static <T extends ExecutionCommandSource<T>> CommandFunction<T> fromLines(ResourceLocation resourceLocation, CommandDispatcher<T> commandDispatcher, T t, List<String> list) {
        FunctionBuilder<T> functionBuilder = new FunctionBuilder<T>();
        for (int i = 0; i < list.size(); ++i) {
            String string;
            String string2;
            StringBuilder stringBuilder;
            int n = i + 1;
            String string3 = list.get(i).trim();
            if (CommandFunction.shouldConcatenateNextLine(string3)) {
                stringBuilder = new StringBuilder(string3);
                do {
                    if (++i == list.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    string2 = list.get(i).trim();
                    stringBuilder.append(string2);
                    CommandFunction.checkCommandLineLength(stringBuilder);
                } while (CommandFunction.shouldConcatenateNextLine(stringBuilder));
                string = stringBuilder.toString();
            } else {
                string = string3;
            }
            CommandFunction.checkCommandLineLength(string);
            stringBuilder = new StringReader(string);
            if (!stringBuilder.canRead() || stringBuilder.peek() == '#') continue;
            if (stringBuilder.peek() == '/') {
                stringBuilder.skip();
                if (stringBuilder.peek() == '/') {
                    throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + n + " (if you intended to make a comment, use '#' not '//')");
                }
                string2 = stringBuilder.readUnquotedString();
                throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + n + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)");
            }
            if (stringBuilder.peek() == '$') {
                functionBuilder.addMacro(string.substring(1), n, t);
                continue;
            }
            try {
                functionBuilder.addCommand(CommandFunction.parseCommand(commandDispatcher, t, (StringReader)stringBuilder));
                continue;
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException("Whilst parsing command on line " + n + ": " + commandSyntaxException.getMessage());
            }
        }
        return functionBuilder.build(resourceLocation);
    }

    public static void checkCommandLineLength(CharSequence charSequence) {
        if (charSequence.length() > 2000000) {
            CharSequence charSequence2 = charSequence.subSequence(0, Math.min(512, 2000000));
            throw new IllegalStateException("Command too long: " + charSequence.length() + " characters, contents: " + String.valueOf(charSequence2) + "...");
        }
    }

    public static <T extends ExecutionCommandSource<T>> UnboundEntryAction<T> parseCommand(CommandDispatcher<T> commandDispatcher, T t, StringReader stringReader) throws CommandSyntaxException {
        ParseResults parseResults = commandDispatcher.parse(stringReader, t);
        Commands.validateParseResults(parseResults);
        Optional optional = ContextChain.tryFlatten((CommandContext)parseResults.getContext().build(stringReader.getString()));
        if (optional.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
        }
        return new BuildContexts.Unbound(stringReader.getString(), (ContextChain)optional.get());
    }
}

