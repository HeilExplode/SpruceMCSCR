/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Keyable
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;

public interface StringRepresentable {
    public static final int PRE_BUILT_MAP_THRESHOLD = 16;

    public String getSerializedName();

    public static <E extends Enum<E>> EnumCodec<E> fromEnum(Supplier<E[]> supplier) {
        return StringRepresentable.fromEnumWithMapping(supplier, string -> string);
    }

    public static <E extends Enum<E>> EnumCodec<E> fromEnumWithMapping(Supplier<E[]> supplier, Function<String, String> function) {
        Enum[] enumArray = (Enum[])supplier.get();
        Function function2 = StringRepresentable.createNameLookup((StringRepresentable[])enumArray, function);
        return new EnumCodec(enumArray, function2);
    }

    public static <T extends StringRepresentable> Codec<T> fromValues(Supplier<T[]> supplier) {
        StringRepresentable[] stringRepresentableArray = (StringRepresentable[])supplier.get();
        Function function = StringRepresentable.createNameLookup((StringRepresentable[])stringRepresentableArray, (T string) -> string);
        ToIntFunction<StringRepresentable> toIntFunction = Util.createIndexLookup(Arrays.asList(stringRepresentableArray));
        return new StringRepresentableCodec(stringRepresentableArray, function, toIntFunction);
    }

    public static <T extends StringRepresentable> Function<String, T> createNameLookup(T[] TArray, Function<String, String> function) {
        if (TArray.length > 16) {
            Map<String, StringRepresentable> map = Arrays.stream(TArray).collect(Collectors.toMap(stringRepresentable -> (String)function.apply(stringRepresentable.getSerializedName()), stringRepresentable -> stringRepresentable));
            return string -> string == null ? null : (StringRepresentable)map.get(string);
        }
        return string -> {
            for (StringRepresentable stringRepresentable : TArray) {
                if (!((String)function.apply(stringRepresentable.getSerializedName())).equals(string)) continue;
                return stringRepresentable;
            }
            return null;
        };
    }

    public static Keyable keys(final StringRepresentable[] stringRepresentableArray) {
        return new Keyable(){

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Arrays.stream(stringRepresentableArray).map(StringRepresentable::getSerializedName).map(arg_0 -> dynamicOps.createString(arg_0));
            }
        };
    }

    @Deprecated
    public static class EnumCodec<E extends Enum<E>>
    extends StringRepresentableCodec<E> {
        private final Function<String, E> resolver;

        public EnumCodec(E[] EArray, Function<String, E> function) {
            super(EArray, function, object -> ((Enum)object).ordinal());
            this.resolver = function;
        }

        @Nullable
        public E byName(@Nullable String string) {
            return (E)((Enum)this.resolver.apply(string));
        }

        public E byName(@Nullable String string, E e) {
            return (E)((Enum)Objects.requireNonNullElse(this.byName(string), e));
        }
    }

    public static class StringRepresentableCodec<S extends StringRepresentable>
    implements Codec<S> {
        private final Codec<S> codec;

        public StringRepresentableCodec(S[] SArray, Function<String, S> function, ToIntFunction<S> toIntFunction) {
            this.codec = ExtraCodecs.orCompressed(Codec.stringResolver(StringRepresentable::getSerializedName, function), ExtraCodecs.idResolverCodec(toIntFunction, n -> n >= 0 && n < SArray.length ? SArray[n] : null, -1));
        }

        public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> dynamicOps, T t) {
            return this.codec.decode(dynamicOps, t);
        }

        public <T> DataResult<T> encode(S s, DynamicOps<T> dynamicOps, T t) {
            return this.codec.encode(s, dynamicOps, t);
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((S)((StringRepresentable)object), (DynamicOps<T>)dynamicOps, (T)object2);
        }
    }
}

