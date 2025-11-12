/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.util.parsing.packrat.Atom;

public final class Scope {
    private final Object2ObjectMap<Atom<?>, Object> values = new Object2ObjectArrayMap();

    public <T> void put(Atom<T> atom, @Nullable T t) {
        this.values.put(atom, t);
    }

    @Nullable
    public <T> T get(Atom<T> atom) {
        return (T)this.values.get(atom);
    }

    public <T> T getOrThrow(Atom<T> atom) {
        return Objects.requireNonNull(this.get(atom));
    }

    public <T> T getOrDefault(Atom<T> atom, T t) {
        return Objects.requireNonNullElse(this.get(atom), t);
    }

    @Nullable
    @SafeVarargs
    public final <T> T getAny(Atom<T> ... atomArray) {
        for (Atom<T> atom : atomArray) {
            T t = this.get(atom);
            if (t == null) continue;
            return t;
        }
        return null;
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Atom<T> ... atomArray) {
        return Objects.requireNonNull(this.getAny(atomArray));
    }

    public String toString() {
        return this.values.toString();
    }

    public void putAll(Scope scope) {
        this.values.putAll(scope.values);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Scope) {
            Scope scope = (Scope)object;
            return this.values.equals(scope.values);
        }
        return false;
    }

    public int hashCode() {
        return this.values.hashCode();
    }
}

