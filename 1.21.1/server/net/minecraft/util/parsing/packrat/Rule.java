/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

import java.util.Optional;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;

public interface Rule<S, T> {
    public Optional<T> parse(ParseState<S> var1);

    public static <S, T> Rule<S, T> fromTerm(Term<S> term, RuleAction<S, T> ruleAction) {
        return new WrappedTerm<S, T>(ruleAction, term);
    }

    public static <S, T> Rule<S, T> fromTerm(Term<S> term, SimpleRuleAction<T> simpleRuleAction) {
        return new WrappedTerm((parseState, scope) -> Optional.of(simpleRuleAction.run(scope)), term);
    }

    public record WrappedTerm<S, T>(RuleAction<S, T> action, Term<S> child) implements Rule<S, T>
    {
        @Override
        public Optional<T> parse(ParseState<S> parseState) {
            Scope scope = new Scope();
            if (this.child.parse(parseState, scope, Control.UNBOUND)) {
                return this.action.run(parseState, scope);
            }
            return Optional.empty();
        }
    }

    @FunctionalInterface
    public static interface RuleAction<S, T> {
        public Optional<T> run(ParseState<S> var1, Scope var2);
    }

    @FunctionalInterface
    public static interface SimpleRuleAction<T> {
        public T run(Scope var1);
    }
}

