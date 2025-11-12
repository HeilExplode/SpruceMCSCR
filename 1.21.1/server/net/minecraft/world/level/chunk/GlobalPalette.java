/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.MissingPaletteEntryException;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;

public class GlobalPalette<T>
implements Palette<T> {
    private final IdMap<T> registry;

    public GlobalPalette(IdMap<T> idMap) {
        this.registry = idMap;
    }

    public static <A> Palette<A> create(int n, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list) {
        return new GlobalPalette<A>(idMap);
    }

    @Override
    public int idFor(T t) {
        int n = this.registry.getId(t);
        return n == -1 ? 0 : n;
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        return true;
    }

    @Override
    public T valueFor(int n) {
        T t = this.registry.byId(n);
        if (t == null) {
            throw new MissingPaletteEntryException(n);
        }
        return t;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public int getSerializedSize() {
        return 0;
    }

    @Override
    public int getSize() {
        return this.registry.size();
    }

    @Override
    public Palette<T> copy() {
        return this;
    }
}

