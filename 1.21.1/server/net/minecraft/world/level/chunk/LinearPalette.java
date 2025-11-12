/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.world.level.chunk.MissingPaletteEntryException;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import org.apache.commons.lang3.Validate;

public class LinearPalette<T>
implements Palette<T> {
    private final IdMap<T> registry;
    private final T[] values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;
    private int size;

    private LinearPalette(IdMap<T> idMap, int n, PaletteResize<T> paletteResize, List<T> list) {
        this.registry = idMap;
        this.values = new Object[1 << n];
        this.bits = n;
        this.resizeHandler = paletteResize;
        Validate.isTrue((list.size() <= this.values.length ? 1 : 0) != 0, (String)"Can't initialize LinearPalette of size %d with %d entries", (Object[])new Object[]{this.values.length, list.size()});
        for (int i = 0; i < list.size(); ++i) {
            this.values[i] = list.get(i);
        }
        this.size = list.size();
    }

    private LinearPalette(IdMap<T> idMap, T[] TArray, PaletteResize<T> paletteResize, int n, int n2) {
        this.registry = idMap;
        this.values = TArray;
        this.resizeHandler = paletteResize;
        this.bits = n;
        this.size = n2;
    }

    public static <A> Palette<A> create(int n, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list) {
        return new LinearPalette<A>(idMap, n, paletteResize, list);
    }

    @Override
    public int idFor(T t) {
        int n;
        for (n = 0; n < this.size; ++n) {
            if (this.values[n] != t) continue;
            return n;
        }
        if ((n = this.size++) < this.values.length) {
            this.values[n] = t;
            return n;
        }
        return this.resizeHandler.onResize(this.bits + 1, t);
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < this.size; ++i) {
            if (!predicate.test(this.values[i])) continue;
            return true;
        }
        return false;
    }

    @Override
    public T valueFor(int n) {
        if (n >= 0 && n < this.size) {
            return this.values[n];
        }
        throw new MissingPaletteEntryException(n);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.size = friendlyByteBuf.readVarInt();
        for (int i = 0; i < this.size; ++i) {
            this.values[i] = this.registry.byIdOrThrow(friendlyByteBuf.readVarInt());
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.size);
        for (int i = 0; i < this.size; ++i) {
            friendlyByteBuf.writeVarInt(this.registry.getId(this.values[i]));
        }
    }

    @Override
    public int getSerializedSize() {
        int n = VarInt.getByteSize(this.getSize());
        for (int i = 0; i < this.getSize(); ++i) {
            n += VarInt.getByteSize(this.registry.getId(this.values[i]));
        }
        return n;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public Palette<T> copy() {
        return new LinearPalette<Object>(this.registry, (Object[])this.values.clone(), this.resizeHandler, this.bits, this.size);
    }
}

