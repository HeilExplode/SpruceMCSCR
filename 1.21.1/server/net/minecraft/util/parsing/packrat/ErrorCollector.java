/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;

public interface ErrorCollector<S> {
    public void store(int var1, SuggestionSupplier<S> var2, Object var3);

    default public void store(int n, Object object) {
        this.store(n, SuggestionSupplier.empty(), object);
    }

    public void finish(int var1);

    public static class LongestOnly<S>
    implements ErrorCollector<S> {
        private final List<ErrorEntry<S>> entries = new ArrayList<ErrorEntry<S>>();
        private int lastCursor = -1;

        private void discardErrorsFromShorterParse(int n) {
            if (n > this.lastCursor) {
                this.lastCursor = n;
                this.entries.clear();
            }
        }

        @Override
        public void finish(int n) {
            this.discardErrorsFromShorterParse(n);
        }

        @Override
        public void store(int n, SuggestionSupplier<S> suggestionSupplier, Object object) {
            this.discardErrorsFromShorterParse(n);
            if (n == this.lastCursor) {
                this.entries.add(new ErrorEntry<S>(n, suggestionSupplier, object));
            }
        }

        public List<ErrorEntry<S>> entries() {
            return this.entries;
        }

        public int cursor() {
            return this.lastCursor;
        }
    }
}

