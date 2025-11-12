/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.BuiltInExceptionProvider
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public interface MinMaxBounds<T extends Number> {
    public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType((Message)Component.translatable("argument.range.empty"));
    public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType((Message)Component.translatable("argument.range.swapped"));

    public Optional<T> min();

    public Optional<T> max();

    default public boolean isAny() {
        return this.min().isEmpty() && this.max().isEmpty();
    }

    default public Optional<T> unwrapPoint() {
        Optional<T> optional;
        Optional<T> optional2 = this.min();
        return optional2.equals(optional = this.max()) ? optional2 : Optional.empty();
    }

    public static <T extends Number, R extends MinMaxBounds<T>> Codec<R> createCodec(Codec<T> codec, BoundsFactory<T, R> boundsFactory) {
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group((App)codec.optionalFieldOf("min").forGetter(MinMaxBounds::min), (App)codec.optionalFieldOf("max").forGetter(MinMaxBounds::max)).apply((Applicative)instance, boundsFactory::create));
        return Codec.either((Codec)codec2, codec).xmap(either -> (MinMaxBounds)either.map(minMaxBounds -> minMaxBounds, number -> boundsFactory.create(Optional.of(number), Optional.of(number))), minMaxBounds -> {
            Optional optional = minMaxBounds.unwrapPoint();
            return optional.isPresent() ? Either.right((Object)((Number)optional.get())) : Either.left((Object)minMaxBounds);
        });
    }

    public static <T extends Number, R extends MinMaxBounds<T>> R fromReader(StringReader stringReader, BoundsFromReaderFactory<T, R> boundsFromReaderFactory, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier, Function<T, T> function2) throws CommandSyntaxException {
        if (!stringReader.canRead()) {
            throw ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
        }
        int n = stringReader.getCursor();
        try {
            Optional<T> optional;
            Optional<T> optional2 = MinMaxBounds.readNumber(stringReader, function, supplier).map(function2);
            if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
                stringReader.skip();
                stringReader.skip();
                optional = MinMaxBounds.readNumber(stringReader, function, supplier).map(function2);
                if (optional2.isEmpty() && optional.isEmpty()) {
                    throw ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
                }
            } else {
                optional = optional2;
            }
            if (optional2.isEmpty() && optional.isEmpty()) {
                throw ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
            }
            return boundsFromReaderFactory.create(stringReader, optional2, optional);
        }
        catch (CommandSyntaxException commandSyntaxException) {
            stringReader.setCursor(n);
            throw new CommandSyntaxException(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), commandSyntaxException.getInput(), n);
        }
    }

    private static <T extends Number> Optional<T> readNumber(StringReader stringReader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
        int n = stringReader.getCursor();
        while (stringReader.canRead() && MinMaxBounds.isAllowedInputChat(stringReader)) {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(n, stringReader.getCursor());
        if (string.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of((Number)function.apply(string));
        }
        catch (NumberFormatException numberFormatException) {
            throw supplier.get().createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
    }

    private static boolean isAllowedInputChat(StringReader stringReader) {
        char c = stringReader.peek();
        if (c >= '0' && c <= '9' || c == '-') {
            return true;
        }
        if (c == '.') {
            return !stringReader.canRead(2) || stringReader.peek(1) != '.';
        }
        return false;
    }

    @FunctionalInterface
    public static interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
        public R create(Optional<T> var1, Optional<T> var2);
    }

    @FunctionalInterface
    public static interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
        public R create(StringReader var1, Optional<T> var2, Optional<T> var3) throws CommandSyntaxException;
    }

    public record Doubles(Optional<Double> min, Optional<Double> max, Optional<Double> minSq, Optional<Double> maxSq) implements MinMaxBounds<Double>
    {
        public static final Doubles ANY = new Doubles(Optional.empty(), Optional.empty());
        public static final Codec<Doubles> CODEC = MinMaxBounds.createCodec(Codec.DOUBLE, Doubles::new);

        private Doubles(Optional<Double> optional, Optional<Double> optional2) {
            this(optional, optional2, Doubles.squareOpt(optional), Doubles.squareOpt(optional2));
        }

        private static Doubles create(StringReader stringReader, Optional<Double> optional, Optional<Double> optional2) throws CommandSyntaxException {
            if (optional.isPresent() && optional2.isPresent() && optional.get() > optional2.get()) {
                throw ERROR_SWAPPED.createWithContext((ImmutableStringReader)stringReader);
            }
            return new Doubles(optional, optional2);
        }

        private static Optional<Double> squareOpt(Optional<Double> optional) {
            return optional.map(d -> d * d);
        }

        public static Doubles exactly(double d) {
            return new Doubles(Optional.of(d), Optional.of(d));
        }

        public static Doubles between(double d, double d2) {
            return new Doubles(Optional.of(d), Optional.of(d2));
        }

        public static Doubles atLeast(double d) {
            return new Doubles(Optional.of(d), Optional.empty());
        }

        public static Doubles atMost(double d) {
            return new Doubles(Optional.empty(), Optional.of(d));
        }

        public boolean matches(double d) {
            if (this.min.isPresent() && this.min.get() > d) {
                return false;
            }
            return this.max.isEmpty() || !(this.max.get() < d);
        }

        public boolean matchesSqr(double d) {
            if (this.minSq.isPresent() && this.minSq.get() > d) {
                return false;
            }
            return this.maxSq.isEmpty() || !(this.maxSq.get() < d);
        }

        public static Doubles fromReader(StringReader stringReader) throws CommandSyntaxException {
            return Doubles.fromReader(stringReader, d -> d);
        }

        public static Doubles fromReader(StringReader stringReader, Function<Double, Double> function) throws CommandSyntaxException {
            return MinMaxBounds.fromReader(stringReader, Doubles::create, Double::parseDouble, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidDouble(), function);
        }
    }

    public record Ints(Optional<Integer> min, Optional<Integer> max, Optional<Long> minSq, Optional<Long> maxSq) implements MinMaxBounds<Integer>
    {
        public static final Ints ANY = new Ints(Optional.empty(), Optional.empty());
        public static final Codec<Ints> CODEC = MinMaxBounds.createCodec(Codec.INT, Ints::new);

        private Ints(Optional<Integer> optional, Optional<Integer> optional2) {
            this(optional, optional2, optional.map(n -> n.longValue() * n.longValue()), Ints.squareOpt(optional2));
        }

        private static Ints create(StringReader stringReader, Optional<Integer> optional, Optional<Integer> optional2) throws CommandSyntaxException {
            if (optional.isPresent() && optional2.isPresent() && optional.get() > optional2.get()) {
                throw ERROR_SWAPPED.createWithContext((ImmutableStringReader)stringReader);
            }
            return new Ints(optional, optional2);
        }

        private static Optional<Long> squareOpt(Optional<Integer> optional) {
            return optional.map(n -> n.longValue() * n.longValue());
        }

        public static Ints exactly(int n) {
            return new Ints(Optional.of(n), Optional.of(n));
        }

        public static Ints between(int n, int n2) {
            return new Ints(Optional.of(n), Optional.of(n2));
        }

        public static Ints atLeast(int n) {
            return new Ints(Optional.of(n), Optional.empty());
        }

        public static Ints atMost(int n) {
            return new Ints(Optional.empty(), Optional.of(n));
        }

        public boolean matches(int n) {
            if (this.min.isPresent() && this.min.get() > n) {
                return false;
            }
            return this.max.isEmpty() || this.max.get() >= n;
        }

        public boolean matchesSqr(long l) {
            if (this.minSq.isPresent() && this.minSq.get() > l) {
                return false;
            }
            return this.maxSq.isEmpty() || this.maxSq.get() >= l;
        }

        public static Ints fromReader(StringReader stringReader) throws CommandSyntaxException {
            return Ints.fromReader(stringReader, n -> n);
        }

        public static Ints fromReader(StringReader stringReader, Function<Integer, Integer> function) throws CommandSyntaxException {
            return MinMaxBounds.fromReader(stringReader, Ints::create, Integer::parseInt, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidInt(), function);
        }
    }
}

