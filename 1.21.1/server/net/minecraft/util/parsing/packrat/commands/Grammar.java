/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import net.minecraft.util.parsing.packrat.commands.ResourceSuggestion;
import net.minecraft.util.parsing.packrat.commands.StringReaderParserState;

public record Grammar<T>(Dictionary<StringReader> rules, Atom<T> top) {
    public Optional<T> parse(ParseState<StringReader> parseState) {
        return parseState.parseTopRule(this.top);
    }

    public T parseForCommands(StringReader stringReader) throws CommandSyntaxException {
        Exception exception2;
        ErrorCollector.LongestOnly<StringReader> longestOnly = new ErrorCollector.LongestOnly<StringReader>();
        StringReaderParserState stringReaderParserState = new StringReaderParserState(this.rules(), longestOnly, stringReader);
        Optional<T> optional = this.parse(stringReaderParserState);
        if (optional.isPresent()) {
            return optional.get();
        }
        List list = longestOnly.entries().stream().mapMulti((errorEntry, consumer) -> {
            Object object = errorEntry.reason();
            if (object instanceof Exception) {
                Exception exception = (Exception)object;
                consumer.accept(exception);
            }
        }).toList();
        for (Exception exception2 : list) {
            if (!(exception2 instanceof CommandSyntaxException)) continue;
            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)((Object)exception2);
            throw commandSyntaxException;
        }
        if (list.size() == 1 && (exception2 = list.get(0)) instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException)exception2;
            throw runtimeException;
        }
        throw new IllegalStateException("Failed to parse: " + longestOnly.entries().stream().map(ErrorEntry::toString).collect(Collectors.joining(", ")));
    }

    public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsBuilder) {
        StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
        stringReader.setCursor(suggestionsBuilder.getStart());
        ErrorCollector.LongestOnly<StringReader> longestOnly = new ErrorCollector.LongestOnly<StringReader>();
        StringReaderParserState stringReaderParserState = new StringReaderParserState(this.rules(), longestOnly, stringReader);
        this.parse(stringReaderParserState);
        List<ErrorEntry<StringReader>> list = longestOnly.entries();
        if (list.isEmpty()) {
            return suggestionsBuilder.buildFuture();
        }
        SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(longestOnly.cursor());
        for (ErrorEntry<StringReader> errorEntry : list) {
            SuggestionSupplier<StringReader> suggestionSupplier = errorEntry.suggestions();
            if (suggestionSupplier instanceof ResourceSuggestion) {
                ResourceSuggestion resourceSuggestion = (ResourceSuggestion)suggestionSupplier;
                SharedSuggestionProvider.suggestResource(resourceSuggestion.possibleResources(), suggestionsBuilder2);
                continue;
            }
            SharedSuggestionProvider.suggest(errorEntry.suggestions().possibleValues(stringReaderParserState), suggestionsBuilder2);
        }
        return suggestionsBuilder2.buildFuture();
    }
}

