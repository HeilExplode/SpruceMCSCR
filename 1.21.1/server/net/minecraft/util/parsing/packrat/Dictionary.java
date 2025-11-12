/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Rule;
import net.minecraft.util.parsing.packrat.Term;

public class Dictionary<S> {
    private final Map<Atom<?>, Rule<S, ?>> terms = new HashMap();

    public <T> void put(Atom<T> atom, Rule<S, T> rule) {
        Rule<S, T> rule2 = this.terms.putIfAbsent(atom, rule);
        if (rule2 != null) {
            throw new IllegalArgumentException("Trying to override rule: " + String.valueOf(atom));
        }
    }

    public <T> void put(Atom<T> atom, Term<S> term, Rule.RuleAction<S, T> ruleAction) {
        this.put(atom, Rule.fromTerm(term, ruleAction));
    }

    public <T> void put(Atom<T> atom, Term<S> term, Rule.SimpleRuleAction<T> simpleRuleAction) {
        this.put(atom, Rule.fromTerm(term, simpleRuleAction));
    }

    @Nullable
    public <T> Rule<S, T> get(Atom<T> atom) {
        return this.terms.get(atom);
    }
}

