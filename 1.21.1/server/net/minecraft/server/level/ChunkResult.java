/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.server.level;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface ChunkResult<T> {
    public static <T> ChunkResult<T> of(T t) {
        return new Success<T>(t);
    }

    public static <T> ChunkResult<T> error(String string) {
        return ChunkResult.error(() -> string);
    }

    public static <T> ChunkResult<T> error(Supplier<String> supplier) {
        return new Fail(supplier);
    }

    public boolean isSuccess();

    @Nullable
    public T orElse(@Nullable T var1);

    @Nullable
    public static <R> R orElse(ChunkResult<? extends R> chunkResult, @Nullable R r) {
        R r2 = chunkResult.orElse(null);
        return r2 != null ? r2 : (R)r;
    }

    @Nullable
    public String getError();

    public ChunkResult<T> ifSuccess(Consumer<T> var1);

    public <R> ChunkResult<R> map(Function<T, R> var1);

    public <E extends Throwable> T orElseThrow(Supplier<E> var1) throws E;

    public record Success<T>(T value) implements ChunkResult<T>
    {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T orElse(@Nullable T t) {
            return this.value;
        }

        @Override
        @Nullable
        public String getError() {
            return null;
        }

        @Override
        public ChunkResult<T> ifSuccess(Consumer<T> consumer) {
            consumer.accept(this.value);
            return this;
        }

        @Override
        public <R> ChunkResult<R> map(Function<T, R> function) {
            return new Success<R>(function.apply(this.value));
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E {
            return this.value;
        }
    }

    public record Fail<T>(Supplier<String> error) implements ChunkResult<T>
    {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        @Nullable
        public T orElse(@Nullable T t) {
            return t;
        }

        @Override
        public String getError() {
            return this.error.get();
        }

        @Override
        public ChunkResult<T> ifSuccess(Consumer<T> consumer) {
            return this;
        }

        @Override
        public <R> ChunkResult<R> map(Function<T, R> function) {
            return new Fail<T>(this.error);
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E {
            throw (Throwable)supplier.get();
        }
    }
}

