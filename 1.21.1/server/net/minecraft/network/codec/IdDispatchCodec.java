/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

public class IdDispatchCodec<B extends ByteBuf, V, T>
implements StreamCodec<B, V> {
    private static final int UNKNOWN_TYPE = -1;
    private final Function<V, ? extends T> typeGetter;
    private final List<Entry<B, V, T>> byId;
    private final Object2IntMap<T> toId;

    IdDispatchCodec(Function<V, ? extends T> function, List<Entry<B, V, T>> list, Object2IntMap<T> object2IntMap) {
        this.typeGetter = function;
        this.byId = list;
        this.toId = object2IntMap;
    }

    @Override
    public V decode(B b) {
        int n = VarInt.read(b);
        if (n < 0 || n >= this.byId.size()) {
            throw new DecoderException("Received unknown packet id " + n);
        }
        Entry<B, V, T> entry = this.byId.get(n);
        try {
            return (V)entry.serializer.decode(b);
        }
        catch (Exception exception) {
            throw new DecoderException("Failed to decode packet '" + String.valueOf(entry.type) + "'", (Throwable)exception);
        }
    }

    @Override
    public void encode(B b, V v) {
        T t = this.typeGetter.apply(v);
        int n = this.toId.getOrDefault(t, -1);
        if (n == -1) {
            throw new EncoderException("Sending unknown packet '" + String.valueOf(t) + "'");
        }
        VarInt.write(b, n);
        Entry<B, V, T> entry = this.byId.get(n);
        try {
            StreamCodec streamCodec = entry.serializer;
            streamCodec.encode(b, v);
        }
        catch (Exception exception) {
            throw new EncoderException("Failed to encode packet '" + String.valueOf(t) + "'", (Throwable)exception);
        }
    }

    public static <B extends ByteBuf, V, T> Builder<B, V, T> builder(Function<V, ? extends T> function) {
        return new Builder(function);
    }

    @Override
    public /* synthetic */ void encode(Object object, Object object2) {
        this.encode((B)((ByteBuf)object), (V)object2);
    }

    @Override
    public /* synthetic */ Object decode(Object object) {
        return this.decode((B)((ByteBuf)object));
    }

    static final class Entry<B, V, T>
    extends Record {
        final StreamCodec<? super B, ? extends V> serializer;
        final T type;

        Entry(StreamCodec<? super B, ? extends V> streamCodec, T t) {
            this.serializer = streamCodec;
            this.type = t;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "serializer;type", "serializer", "type"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "serializer;type", "serializer", "type"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "serializer;type", "serializer", "type"}, this, object);
        }

        public StreamCodec<? super B, ? extends V> serializer() {
            return this.serializer;
        }

        public T type() {
            return this.type;
        }
    }

    public static class Builder<B extends ByteBuf, V, T> {
        private final List<Entry<B, V, T>> entries = new ArrayList<Entry<B, V, T>>();
        private final Function<V, ? extends T> typeGetter;

        Builder(Function<V, ? extends T> function) {
            this.typeGetter = function;
        }

        public Builder<B, V, T> add(T t, StreamCodec<? super B, ? extends V> streamCodec) {
            this.entries.add(new Entry<B, V, T>(streamCodec, t));
            return this;
        }

        public IdDispatchCodec<B, V, T> build() {
            Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
            object2IntOpenHashMap.defaultReturnValue(-2);
            for (Entry<B, V, T> entry : this.entries) {
                int n = object2IntOpenHashMap.size();
                int n2 = object2IntOpenHashMap.putIfAbsent(entry.type, n);
                if (n2 == -2) continue;
                throw new IllegalStateException("Duplicate registration for type " + String.valueOf(entry.type));
            }
            return new IdDispatchCodec<B, V, T>(this.typeGetter, List.copyOf(this.entries), object2IntOpenHashMap);
        }
    }
}

