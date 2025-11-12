/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.chunk.MissingPaletteEntryException;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;

public class HashMapPalette<T>
implements Palette<T> {
    private final IdMap<T> registry;
    private final CrudeIncrementalIntIdentityHashBiMap<T> values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;

    public HashMapPalette(IdMap<T> idMap, int n, PaletteResize<T> paletteResize, List<T> list) {
        this(idMap, n, paletteResize);
        list.forEach(this.values::add);
    }

    public HashMapPalette(IdMap<T> idMap, int n, PaletteResize<T> paletteResize) {
        this(idMap, n, paletteResize, CrudeIncrementalIntIdentityHashBiMap.create(1 << n));
    }

    private HashMapPalette(IdMap<T> idMap, int n, PaletteResize<T> paletteResize, CrudeIncrementalIntIdentityHashBiMap<T> crudeIncrementalIntIdentityHashBiMap) {
        this.registry = idMap;
        this.bits = n;
        this.resizeHandler = paletteResize;
        this.values = crudeIncrementalIntIdentityHashBiMap;
    }

    public static <A> Palette<A> create(int n, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list) {
        return new HashMapPalette<A>(idMap, n, paletteResize, list);
    }

    @Override
    public int idFor(T t) {
        int n = this.values.getId(t);
        if (n == -1 && (n = this.values.add(t)) >= 1 << this.bits) {
            n = this.resizeHandler.onResize(this.bits + 1, t);
        }
        return n;
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < this.getSize(); ++i) {
            if (!predicate.test(this.values.byId(i))) continue;
            return true;
        }
        return false;
    }

    @Override
    public T valueFor(int n) {
        T t = this.values.byId(n);
        if (t == null) {
            throw new MissingPaletteEntryException(n);
        }
        return t;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.values.clear();
        int n = friendlyByteBuf.readVarInt();
        for (int i = 0; i < n; ++i) {
            this.values.add(this.registry.byIdOrThrow(friendlyByteBuf.readVarInt()));
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        int n = this.getSize();
        friendlyByteBuf.writeVarInt(n);
        for (int i = 0; i < n; ++i) {
            friendlyByteBuf.writeVarInt(this.registry.getId(this.values.byId(i)));
        }
    }

    @Override
    public int getSerializedSize() {
        int n = VarInt.getByteSize(this.getSize());
        for (int i = 0; i < this.getSize(); ++i) {
            n += VarInt.getByteSize(this.registry.getId(this.values.byId(i)));
        }
        return n;
    }

    public List<T> getEntries() {
        ArrayList arrayList = new ArrayList();
        this.values.iterator().forEachRemaining(arrayList::add);
        return arrayList;
    }

    @Override
    public int getSize() {
        return this.values.size();
    }

    @Override
    public Palette<T> copy() {
        return new HashMapPalette<T>(this.registry, this.bits, this.resizeHandler, this.values.copy());
    }
}

