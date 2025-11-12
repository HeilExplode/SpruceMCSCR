/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.Multimap
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public interface ProblemReporter {
    public ProblemReporter forChild(String var1);

    public void report(String var1);

    public static class Collector
    implements ProblemReporter {
        private final Multimap<String, String> problems;
        private final Supplier<String> path;
        @Nullable
        private String pathCache;

        public Collector() {
            this((Multimap<String, String>)HashMultimap.create(), () -> "");
        }

        private Collector(Multimap<String, String> multimap, Supplier<String> supplier) {
            this.problems = multimap;
            this.path = supplier;
        }

        private String getPath() {
            if (this.pathCache == null) {
                this.pathCache = this.path.get();
            }
            return this.pathCache;
        }

        @Override
        public ProblemReporter forChild(String string) {
            return new Collector(this.problems, () -> this.getPath() + string);
        }

        @Override
        public void report(String string) {
            this.problems.put((Object)this.getPath(), (Object)string);
        }

        public Multimap<String, String> get() {
            return ImmutableMultimap.copyOf(this.problems);
        }

        public Optional<String> getReport() {
            Multimap<String, String> multimap = this.get();
            if (!multimap.isEmpty()) {
                String string = multimap.asMap().entrySet().stream().map(entry -> " at " + (String)entry.getKey() + ": " + String.join((CharSequence)"; ", (Iterable)entry.getValue())).collect(Collectors.joining("\n"));
                return Optional.of(string);
            }
            return Optional.empty();
        }
    }
}

