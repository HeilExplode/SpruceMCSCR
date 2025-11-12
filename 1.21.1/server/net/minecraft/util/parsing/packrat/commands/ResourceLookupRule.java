/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;
import net.minecraft.util.parsing.packrat.commands.ResourceSuggestion;

public abstract class ResourceLookupRule<C, V>
implements Rule<StringReader, V>,
ResourceSuggestion {
    private final Atom<ResourceLocation> idParser;
    protected final C context;

    protected ResourceLookupRule(Atom<ResourceLocation> atom, C c) {
        this.idParser = atom;
        this.context = c;
    }

    @Override
    public Optional<V> parse(ParseState<StringReader> parseState) {
        parseState.input().skipWhitespace();
        int n = parseState.mark();
        Optional<ResourceLocation> optional = parseState.parse(this.idParser);
        if (optional.isPresent()) {
            try {
                return Optional.of(this.validateElement((ImmutableStringReader)parseState.input(), optional.get()));
            }
            catch (Exception exception) {
                parseState.errorCollector().store(n, this, exception);
                return Optional.empty();
            }
        }
        parseState.errorCollector().store(n, this, (Object)ResourceLocation.ERROR_INVALID.createWithContext((ImmutableStringReader)parseState.input()));
        return Optional.empty();
    }

    protected abstract V validateElement(ImmutableStringReader var1, ResourceLocation var2) throws Exception;
}

