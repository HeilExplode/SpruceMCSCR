/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.util.parsing.packrat;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import org.apache.commons.lang3.mutable.MutableBoolean;

public interface Term<S> {
    public boolean parse(ParseState<S> var1, Scope var2, Control var3);

    public static <S> Term<S> named(Atom<?> atom) {
        return new Reference(atom);
    }

    public static <S, T> Term<S> marker(Atom<T> atom, T t) {
        return new Marker(atom, t);
    }

    @SafeVarargs
    public static <S> Term<S> sequence(Term<S> ... termArray) {
        return new Sequence<S>(List.of(termArray));
    }

    @SafeVarargs
    public static <S> Term<S> alternative(Term<S> ... termArray) {
        return new Alternative<S>(List.of(termArray));
    }

    public static <S> Term<S> optional(Term<S> term) {
        return new Maybe<S>(term);
    }

    public static <S> Term<S> cut() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                control.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    public static <S> Term<S> empty() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    public record Reference<S, T>(Atom<T> name) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            Optional<T> optional = parseState.parse(this.name);
            if (optional.isEmpty()) {
                return false;
            }
            scope.put(this.name, optional.get());
            return true;
        }
    }

    public record Marker<S, T>(Atom<T> name, T value) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            scope.put(this.name, this.value);
            return true;
        }
    }

    public record Sequence<S>(List<Term<S>> elements) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int n = parseState.mark();
            for (Term<S> term : this.elements) {
                if (term.parse(parseState, scope, control)) continue;
                parseState.restore(n);
                return false;
            }
            return true;
        }
    }

    public record Alternative<S>(List<Term<S>> elements) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            MutableBoolean mutableBoolean = new MutableBoolean();
            Control control2 = () -> ((MutableBoolean)mutableBoolean).setTrue();
            int n = parseState.mark();
            for (Term<S> term : this.elements) {
                if (mutableBoolean.isTrue()) break;
                Scope scope2 = new Scope();
                if (term.parse(parseState, scope2, control2)) {
                    scope.putAll(scope2);
                    return true;
                }
                parseState.restore(n);
            }
            return false;
        }
    }

    public record Maybe<S>(Term<S> term) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int n = parseState.mark();
            if (!this.term.parse(parseState, scope, control)) {
                parseState.restore(n);
            }
            return true;
        }
    }
}

