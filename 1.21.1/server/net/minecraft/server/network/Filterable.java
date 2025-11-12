/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.server.network;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.FilteredText;

public record Filterable<T>(T raw, Optional<T> filtered) {
    public static <T> Codec<Filterable<T>> codec(Codec<T> codec) {
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("raw").forGetter(Filterable::raw), (App)codec.optionalFieldOf("filtered").forGetter(Filterable::filtered)).apply((Applicative)instance, Filterable::new));
        Codec codec3 = codec.xmap(Filterable::passThrough, Filterable::raw);
        return Codec.withAlternative((Codec)codec2, (Codec)codec3);
    }

    public static <B extends ByteBuf, T> StreamCodec<B, Filterable<T>> streamCodec(StreamCodec<B, T> streamCodec) {
        return StreamCodec.composite(streamCodec, Filterable::raw, streamCodec.apply(ByteBufCodecs::optional), Filterable::filtered, Filterable::new);
    }

    public static <T> Filterable<T> passThrough(T t) {
        return new Filterable<T>(t, Optional.empty());
    }

    public static Filterable<String> from(FilteredText filteredText) {
        return new Filterable<String>(filteredText.raw(), filteredText.isFiltered() ? Optional.of(filteredText.filteredOrEmpty()) : Optional.empty());
    }

    public T get(boolean bl) {
        if (bl) {
            return this.filtered.orElse(this.raw);
        }
        return this.raw;
    }

    public <U> Filterable<U> map(Function<T, U> function) {
        return new Filterable<U>(function.apply(this.raw), this.filtered.map(function));
    }

    public <U> Optional<Filterable<U>> resolve(Function<T, Optional<U>> function) {
        Optional<U> optional = function.apply(this.raw);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        if (this.filtered.isPresent()) {
            Optional<U> optional2 = function.apply(this.filtered.get());
            if (optional2.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Filterable<U>(optional.get(), optional2));
        }
        return Optional.of(new Filterable<U>(optional.get(), Optional.empty()));
    }
}

