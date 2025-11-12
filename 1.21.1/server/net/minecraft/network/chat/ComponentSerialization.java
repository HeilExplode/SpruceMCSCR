/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonParser
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapDecoder
 *  com.mojang.serialization.MapEncoder
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;

public class ComponentSerialization {
    public static final Codec<Component> CODEC = Codec.recursive((String)"Component", ComponentSerialization::createCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Component>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Component>> TRUSTED_OPTIONAL_STREAM_CODEC = TRUSTED_STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<ByteBuf, Component> TRUSTED_CONTEXT_FREE_STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(CODEC);
    public static final Codec<Component> FLAT_CODEC = ComponentSerialization.flatCodec(Integer.MAX_VALUE);

    public static Codec<Component> flatCodec(int n) {
        final Codec codec = Codec.string((int)0, (int)n);
        return new Codec<Component>(){

            public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> dynamicOps, T t) {
                DynamicOps<JsonElement> dynamicOps2 = 1.asJsonOps(dynamicOps);
                return codec.decode(dynamicOps, t).flatMap(pair -> {
                    try {
                        JsonElement jsonElement = JsonParser.parseString((String)((String)pair.getFirst()));
                        return CODEC.parse(dynamicOps2, (Object)jsonElement).map(component -> Pair.of((Object)component, (Object)pair.getSecond()));
                    }
                    catch (JsonParseException jsonParseException) {
                        return DataResult.error(jsonParseException::getMessage);
                    }
                });
            }

            public <T> DataResult<T> encode(Component component, DynamicOps<T> dynamicOps, T t) {
                DynamicOps<JsonElement> dynamicOps2 = 1.asJsonOps(dynamicOps);
                return CODEC.encodeStart(dynamicOps2, (Object)component).flatMap(jsonElement -> {
                    try {
                        return codec.encodeStart(dynamicOps, (Object)GsonHelper.toStableString(jsonElement));
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        return DataResult.error(illegalArgumentException::getMessage);
                    }
                });
            }

            private static <T> DynamicOps<JsonElement> asJsonOps(DynamicOps<T> dynamicOps) {
                if (dynamicOps instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)dynamicOps;
                    return registryOps.withParent(JsonOps.INSTANCE);
                }
                return JsonOps.INSTANCE;
            }

            public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                return this.encode((Component)object, dynamicOps, object2);
            }
        };
    }

    private static MutableComponent createFromList(List<Component> list) {
        MutableComponent mutableComponent = list.get(0).copy();
        for (int i = 1; i < list.size(); ++i) {
            mutableComponent.append(list.get(i));
        }
        return mutableComponent;
    }

    public static <T extends StringRepresentable, E> MapCodec<E> createLegacyComponentMatcher(T[] TArray, Function<T, MapCodec<? extends E>> function, Function<E, T> function2, String string) {
        FuzzyCodec<Object> fuzzyCodec = new FuzzyCodec<Object>(Stream.of(TArray).map(function).toList(), object -> (MapEncoder)function.apply((StringRepresentable)function2.apply(object)));
        Codec codec = StringRepresentable.fromValues(() -> TArray);
        MapCodec mapCodec = codec.dispatchMap(string, function2, function);
        StrictEither<Object> strictEither = new StrictEither<Object>(string, mapCodec, fuzzyCodec);
        return ExtraCodecs.orCompressed(strictEither, mapCodec);
    }

    private static Codec<Component> createCodec(Codec<Component> codec) {
        StringRepresentable[] stringRepresentableArray = new ComponentContents.Type[]{PlainTextContents.TYPE, TranslatableContents.TYPE, KeybindContents.TYPE, ScoreContents.TYPE, SelectorContents.TYPE, NbtContents.TYPE};
        MapCodec mapCodec = ComponentSerialization.createLegacyComponentMatcher((StringRepresentable[])stringRepresentableArray, ComponentContents.Type::codec, ComponentContents::type, (String)"type");
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group((App)mapCodec.forGetter(Component::getContents), (App)ExtraCodecs.nonEmptyList(codec.listOf()).optionalFieldOf("extra", List.of()).forGetter(Component::getSiblings), (App)Style.Serializer.MAP_CODEC.forGetter(Component::getStyle)).apply((Applicative)instance, MutableComponent::new));
        return Codec.either((Codec)Codec.either((Codec)Codec.STRING, ExtraCodecs.nonEmptyList(codec.listOf())), (Codec)codec2).xmap(either2 -> (Component)either2.map(either -> (Component)either.map(Component::literal, ComponentSerialization::createFromList), component -> component), component -> {
            String string = component.tryCollapseToString();
            return string != null ? Either.left((Object)Either.left((Object)string)) : Either.right((Object)component);
        });
    }

    static class FuzzyCodec<T>
    extends MapCodec<T> {
        private final List<MapCodec<? extends T>> codecs;
        private final Function<T, MapEncoder<? extends T>> encoderGetter;

        public FuzzyCodec(List<MapCodec<? extends T>> list, Function<T, MapEncoder<? extends T>> function) {
            this.codecs = list;
            this.encoderGetter = function;
        }

        public <S> DataResult<T> decode(DynamicOps<S> dynamicOps, MapLike<S> mapLike) {
            for (MapDecoder mapDecoder : this.codecs) {
                DataResult dataResult = mapDecoder.decode(dynamicOps, mapLike);
                if (!dataResult.result().isPresent()) continue;
                return dataResult;
            }
            return DataResult.error(() -> "No matching codec found");
        }

        public <S> RecordBuilder<S> encode(T t, DynamicOps<S> dynamicOps, RecordBuilder<S> recordBuilder) {
            MapEncoder<? extends T> mapEncoder = this.encoderGetter.apply(t);
            return mapEncoder.encode(t, dynamicOps, recordBuilder);
        }

        public <S> Stream<S> keys(DynamicOps<S> dynamicOps) {
            return this.codecs.stream().flatMap(mapCodec -> mapCodec.keys(dynamicOps)).distinct();
        }

        public String toString() {
            return "FuzzyCodec[" + String.valueOf(this.codecs) + "]";
        }
    }

    static class StrictEither<T>
    extends MapCodec<T> {
        private final String typeFieldName;
        private final MapCodec<T> typed;
        private final MapCodec<T> fuzzy;

        public StrictEither(String string, MapCodec<T> mapCodec, MapCodec<T> mapCodec2) {
            this.typeFieldName = string;
            this.typed = mapCodec;
            this.fuzzy = mapCodec2;
        }

        public <O> DataResult<T> decode(DynamicOps<O> dynamicOps, MapLike<O> mapLike) {
            if (mapLike.get(this.typeFieldName) != null) {
                return this.typed.decode(dynamicOps, mapLike);
            }
            return this.fuzzy.decode(dynamicOps, mapLike);
        }

        public <O> RecordBuilder<O> encode(T t, DynamicOps<O> dynamicOps, RecordBuilder<O> recordBuilder) {
            return this.fuzzy.encode(t, dynamicOps, recordBuilder);
        }

        public <T1> Stream<T1> keys(DynamicOps<T1> dynamicOps) {
            return Stream.concat(this.typed.keys(dynamicOps), this.fuzzy.keys(dynamicOps)).distinct();
        }
    }
}

