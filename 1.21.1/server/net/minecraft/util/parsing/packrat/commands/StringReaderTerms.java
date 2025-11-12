/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {
    public static Term<StringReader> word(String string) {
        return new TerminalWord(string);
    }

    public static Term<StringReader> character(char c) {
        return new TerminalCharacter(c);
    }

    public record TerminalWord(String value) implements Term<StringReader>
    {
        @Override
        public boolean parse(ParseState<StringReader> parseState2, Scope scope, Control control) {
            parseState2.input().skipWhitespace();
            int n = parseState2.mark();
            String string = parseState2.input().readUnquotedString();
            if (!string.equals(this.value)) {
                parseState2.errorCollector().store(n, parseState -> Stream.of(this.value), (Object)CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create((Object)this.value));
                return false;
            }
            return true;
        }
    }

    public record TerminalCharacter(char value) implements Term<StringReader>
    {
        @Override
        public boolean parse(ParseState<StringReader> parseState2, Scope scope, Control control) {
            parseState2.input().skipWhitespace();
            int n = parseState2.mark();
            if (!parseState2.input().canRead() || parseState2.input().read() != this.value) {
                parseState2.errorCollector().store(n, parseState -> Stream.of(String.valueOf(this.value)), (Object)CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create((Object)Character.valueOf(this.value)));
                return false;
            }
            return true;
        }
    }
}

