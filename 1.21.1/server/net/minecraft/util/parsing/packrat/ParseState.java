/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class ParseState<S> {
    private final Map<CacheKey<?>, CacheEntry<?>> ruleCache = new HashMap();
    private final Dictionary<S> dictionary;
    private final ErrorCollector<S> errorCollector;

    protected ParseState(Dictionary<S> dictionary, ErrorCollector<S> errorCollector) {
        this.dictionary = dictionary;
        this.errorCollector = errorCollector;
    }

    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    public <T> Optional<T> parseTopRule(Atom<T> atom) {
        Optional<T> optional = this.parse(atom);
        if (optional.isPresent()) {
            this.errorCollector.finish(this.mark());
        }
        return optional;
    }

    public <T> Optional<T> parse(Atom<T> atom) {
        CacheKey<T> cacheKey = new CacheKey<T>(atom, this.mark());
        CacheEntry<T> cacheEntry = this.lookupInCache(cacheKey);
        if (cacheEntry != null) {
            this.restore(cacheEntry.mark());
            return cacheEntry.value;
        }
        Rule<S, T> rule = this.dictionary.get(atom);
        if (rule == null) {
            throw new IllegalStateException("No symbol " + String.valueOf(atom));
        }
        Optional<T> optional = rule.parse(this);
        this.storeInCache(cacheKey, optional);
        return optional;
    }

    @Nullable
    private <T> CacheEntry<T> lookupInCache(CacheKey<T> cacheKey) {
        return this.ruleCache.get(cacheKey);
    }

    private <T> void storeInCache(CacheKey<T> cacheKey, Optional<T> optional) {
        this.ruleCache.put(cacheKey, new CacheEntry<T>(optional, this.mark()));
    }

    public abstract S input();

    public abstract int mark();

    public abstract void restore(int var1);

    record CacheKey<T>(Atom<T> name, int mark) {
    }

    static final class CacheEntry<T>
    extends Record {
        final Optional<T> value;
        private final int mark;

        CacheEntry(Optional<T> optional, int n) {
            this.value = optional;
            this.mark = n;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheEntry.class, "value;mark", "value", "mark"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheEntry.class, "value;mark", "value", "mark"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheEntry.class, "value;mark", "value", "mark"}, this, object);
        }

        public Optional<T> value() {
            return this.value;
        }

        public int mark() {
            return this.mark;
        }
    }
}

