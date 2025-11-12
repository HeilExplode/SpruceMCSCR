/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.ScoreAccess;

public class OperationArgument
implements ArgumentType<Operation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
    private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType((Message)Component.translatable("arguments.operation.invalid"));
    private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType((Message)Component.translatable("arguments.operation.div0"));

    public static OperationArgument operation() {
        return new OperationArgument();
    }

    public static Operation getOperation(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Operation)commandContext.getArgument(string, Operation.class);
    }

    public Operation parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead()) {
            int n = stringReader.getCursor();
            while (stringReader.canRead() && stringReader.peek() != ' ') {
                stringReader.skip();
            }
            return OperationArgument.getOperation(stringReader.getString().substring(n, stringReader.getCursor()));
        }
        throw ERROR_INVALID_OPERATION.createWithContext((ImmutableStringReader)stringReader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, suggestionsBuilder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static Operation getOperation(String string) throws CommandSyntaxException {
        if (string.equals("><")) {
            return (scoreAccess, scoreAccess2) -> {
                int n = scoreAccess.get();
                scoreAccess.set(scoreAccess2.get());
                scoreAccess2.set(n);
            };
        }
        return OperationArgument.getSimpleOperation(string);
    }

    private static SimpleOperation getSimpleOperation(String string) throws CommandSyntaxException {
        return switch (string) {
            case "=" -> (n, n2) -> n2;
            case "+=" -> Integer::sum;
            case "-=" -> (n, n2) -> n - n2;
            case "*=" -> (n, n2) -> n * n2;
            case "/=" -> (n, n2) -> {
                if (n2 == 0) {
                    throw ERROR_DIVIDE_BY_ZERO.create();
                }
                return Mth.floorDiv(n, n2);
            };
            case "%=" -> (n, n2) -> {
                if (n2 == 0) {
                    throw ERROR_DIVIDE_BY_ZERO.create();
                }
                return Mth.positiveModulo(n, n2);
            };
            case "<" -> Math::min;
            case ">" -> Math::max;
            default -> throw ERROR_INVALID_OPERATION.create();
        };
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    @FunctionalInterface
    public static interface Operation {
        public void apply(ScoreAccess var1, ScoreAccess var2) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface SimpleOperation
    extends Operation {
        public int apply(int var1, int var2) throws CommandSyntaxException;

        @Override
        default public void apply(ScoreAccess scoreAccess, ScoreAccess scoreAccess2) throws CommandSyntaxException {
            scoreAccess.set(this.apply(scoreAccess.get(), scoreAccess2.get()));
        }
    }
}

