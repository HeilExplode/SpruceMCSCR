/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.util.Function3
 *  com.mojang.datafixers.util.Function4
 *  com.mojang.datafixers.util.Function5
 *  com.mojang.datafixers.util.Function6
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.codec.StreamMemberEncoder;

public interface StreamCodec<B, V>
extends StreamDecoder<B, V>,
StreamEncoder<B, V> {
    public static <B, V> StreamCodec<B, V> of(final StreamEncoder<B, V> streamEncoder, final StreamDecoder<B, V> streamDecoder) {
        return new StreamCodec<B, V>(){

            @Override
            public V decode(B b) {
                return streamDecoder.decode(b);
            }

            @Override
            public void encode(B b, V v) {
                streamEncoder.encode(b, v);
            }
        };
    }

    public static <B, V> StreamCodec<B, V> ofMember(final StreamMemberEncoder<B, V> streamMemberEncoder, final StreamDecoder<B, V> streamDecoder) {
        return new StreamCodec<B, V>(){

            @Override
            public V decode(B b) {
                return streamDecoder.decode(b);
            }

            @Override
            public void encode(B b, V v) {
                streamMemberEncoder.encode(v, b);
            }
        };
    }

    public static <B, V> StreamCodec<B, V> unit(final V v) {
        return new StreamCodec<B, V>(){

            @Override
            public V decode(B b) {
                return v;
            }

            @Override
            public void encode(B b, V v2) {
                if (!v2.equals(v)) {
                    throw new IllegalStateException("Can't encode '" + String.valueOf(v2) + "', expected '" + String.valueOf(v) + "'");
                }
            }
        };
    }

    default public <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> codecOperation) {
        return codecOperation.apply(this);
    }

    default public <O> StreamCodec<B, O> map(final Function<? super V, ? extends O> function, final Function<? super O, ? extends V> function2) {
        return new StreamCodec<B, O>(){

            @Override
            public O decode(B b) {
                return function.apply(StreamCodec.this.decode(b));
            }

            @Override
            public void encode(B b, O o) {
                StreamCodec.this.encode(b, function2.apply(o));
            }
        };
    }

    default public <O extends ByteBuf> StreamCodec<O, V> mapStream(final Function<O, ? extends B> function) {
        return new StreamCodec<O, V>(){

            @Override
            public V decode(O o) {
                Object r = function.apply(o);
                return StreamCodec.this.decode(r);
            }

            @Override
            public void encode(O o, V v) {
                Object r = function.apply(o);
                StreamCodec.this.encode(r, v);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((O)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((O)((ByteBuf)object));
            }
        };
    }

    default public <U> StreamCodec<B, U> dispatch(final Function<? super U, ? extends V> function, final Function<? super V, ? extends StreamCodec<? super B, ? extends U>> function2) {
        return new StreamCodec<B, U>(){

            @Override
            public U decode(B b) {
                Object t = StreamCodec.this.decode(b);
                StreamCodec streamCodec = (StreamCodec)function2.apply(t);
                return streamCodec.decode(b);
            }

            @Override
            public void encode(B b, U u) {
                Object r = function.apply(u);
                StreamCodec streamCodec = (StreamCodec)function2.apply(r);
                StreamCodec.this.encode(b, r);
                streamCodec.encode(b, u);
            }
        };
    }

    public static <B, C, T1> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final Function<T1, C> function2) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B b) {
                Object t = streamCodec.decode(b);
                return function2.apply(t);
            }

            @Override
            public void encode(B b, C c) {
                streamCodec.encode(b, function.apply(c));
            }
        };
    }

    public static <B, C, T1, T2> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final BiFunction<T1, T2, C> biFunction) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B b) {
                Object t = streamCodec.decode(b);
                Object t2 = streamCodec2.decode(b);
                return biFunction.apply(t, t2);
            }

            @Override
            public void encode(B b, C c) {
                streamCodec.encode(b, function.apply(c));
                streamCodec2.encode(b, function2.apply(c));
            }
        };
    }

    public static <B, C, T1, T2, T3> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final Function3<T1, T2, T3, C> function32) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B b) {
                Object t = streamCodec.decode(b);
                Object t2 = streamCodec2.decode(b);
                Object t3 = streamCodec3.decode(b);
                return function32.apply(t, t2, t3);
            }

            @Override
            public void encode(B b, C c) {
                streamCodec.encode(b, function.apply(c));
                streamCodec2.encode(b, function2.apply(c));
                streamCodec3.encode(b, function3.apply(c));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final Function4<T1, T2, T3, T4, C> function42) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B b) {
                Object t = streamCodec.decode(b);
                Object t2 = streamCodec2.decode(b);
                Object t3 = streamCodec3.decode(b);
                Object t4 = streamCodec4.decode(b);
                return function42.apply(t, t2, t3, t4);
            }

            @Override
            public void encode(B b, C c) {
                streamCodec.encode(b, function.apply(c));
                streamCodec2.encode(b, function2.apply(c));
                streamCodec3.encode(b, function3.apply(c));
                streamCodec4.encode(b, function4.apply(c));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final Function5<T1, T2, T3, T4, T5, C> function52) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B b) {
                Object t = streamCodec.decode(b);
                Object t2 = streamCodec2.decode(b);
                Object t3 = streamCodec3.decode(b);
                Object t4 = streamCodec4.decode(b);
                Object t5 = streamCodec5.decode(b);
                return function52.apply(t, t2, t3, t4, t5);
            }

            @Override
            public void encode(B b, C c) {
                streamCodec.encode(b, function.apply(c));
                streamCodec2.encode(b, function2.apply(c));
                streamCodec3.encode(b, function3.apply(c));
                streamCodec4.encode(b, function4.apply(c));
                streamCodec5.encode(b, function5.apply(c));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final Function6<T1, T2, T3, T4, T5, T6, C> function62) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B b) {
                Object t = streamCodec.decode(b);
                Object t2 = streamCodec2.decode(b);
                Object t3 = streamCodec3.decode(b);
                Object t4 = streamCodec4.decode(b);
                Object t5 = streamCodec5.decode(b);
                Object t6 = streamCodec6.decode(b);
                return function62.apply(t, t2, t3, t4, t5, t6);
            }

            @Override
            public void encode(B b, C c) {
                streamCodec.encode(b, function.apply(c));
                streamCodec2.encode(b, function2.apply(c));
                streamCodec3.encode(b, function3.apply(c));
                streamCodec4.encode(b, function4.apply(c));
                streamCodec5.encode(b, function5.apply(c));
                streamCodec6.encode(b, function6.apply(c));
            }
        };
    }

    public static <B, T> StreamCodec<B, T> recursive(final UnaryOperator<StreamCodec<B, T>> unaryOperator) {
        return new StreamCodec<B, T>(){
            private final Supplier<StreamCodec<B, T>> inner = Suppliers.memoize(() -> (StreamCodec)unaryOperator.apply(this));

            @Override
            public T decode(B b) {
                return this.inner.get().decode(b);
            }

            @Override
            public void encode(B b, T t) {
                this.inner.get().encode(b, t);
            }
        };
    }

    default public <S extends B> StreamCodec<S, V> cast() {
        return this;
    }

    @FunctionalInterface
    public static interface CodecOperation<B, S, T> {
        public StreamCodec<B, T> apply(StreamCodec<B, S> var1);
    }
}

