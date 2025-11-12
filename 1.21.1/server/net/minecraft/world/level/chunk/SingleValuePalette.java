/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import org.apache.commons.lang3.Validate;

public class SingleValuePalette<T>
implements Palette<T> {
    private final IdMap<T> registry;
    @Nullable
    private T value;
    private final PaletteResize<T> resizeHandler;

    public SingleValuePalette(IdMap<T> idMap, PaletteResize<T> paletteResize, List<T> list) {
        this.registry = idMap;
        this.resizeHandler = paletteResize;
        if (list.size() > 0) {
            Validate.isTrue((list.size() <= 1 ? 1 : 0) != 0, (String)"Can't initialize SingleValuePalette with %d values.", (long)list.size());
            this.value = list.get(0);
        }
    }

    public static <A> Palette<A> create(int n, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list) {
        return new SingleValuePalette<A>(idMap, paletteResize, list);
    }

    @Override
    public int idFor(T t) {
        if (this.value == null || this.value == t) {
            this.value = t;
            return 0;
        }
        return this.resizeHandler.onResize(1, t);
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return predicate.test(this.value);
    }

    @Override
    public T valueFor(int n) {
        if (this.value == null || n != 0) {
            throw new IllegalStateException("Missing Palette entry for id " + n + ".");
        }
        return this.value;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.value = this.registry.byIdOrThrow(friendlyByteBuf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        friendlyByteBuf.writeVarInt(this.registry.getId(this.value));
    }

    @Override
    public int getSerializedSize() {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return VarInt.getByteSize(this.registry.getId(this.value));
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public Palette<T> copy() {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return this;
    }
}

