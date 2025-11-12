/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.primitives.UnsignedBytes
 *  com.google.gson.JsonElement
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Codec$ResultFunction
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.BaseMapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.apache.commons.lang3.StringEscapeUtils
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.joml.AxisAngle4f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ExtraCodecs {
    public static final Codec<JsonElement> JSON = ExtraCodecs.converter(JsonOps.INSTANCE);
    public static final Codec<Object> JAVA = ExtraCodecs.converter(JavaOps.INSTANCE);
    public static final Codec<Vector3f> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 3).map(list -> new Vector3f(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue())), vector3f -> List.of(Float.valueOf(vector3f.x()), Float.valueOf(vector3f.y()), Float.valueOf(vector3f.z())));
    public static final Codec<Vector4f> VECTOR4F = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 4).map(list -> new Vector4f(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue(), ((Float)list.get(3)).floatValue())), vector4f -> List.of(Float.valueOf(vector4f.x()), Float.valueOf(vector4f.y()), Float.valueOf(vector4f.z()), Float.valueOf(vector4f.w())));
    public static final Codec<Quaternionf> QUATERNIONF_COMPONENTS = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 4).map(list -> new Quaternionf(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue(), ((Float)list.get(3)).floatValue()).normalize()), quaternionf -> List.of(Float.valueOf(quaternionf.x), Float.valueOf(quaternionf.y), Float.valueOf(quaternionf.z), Float.valueOf(quaternionf.w)));
    public static final Codec<AxisAngle4f> AXISANGLE4F = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("angle").forGetter(axisAngle4f -> Float.valueOf(axisAngle4f.angle)), (App)VECTOR3F.fieldOf("axis").forGetter(axisAngle4f -> new Vector3f(axisAngle4f.x, axisAngle4f.y, axisAngle4f.z))).apply((Applicative)instance, AxisAngle4f::new));
    public static final Codec<Quaternionf> QUATERNIONF = Codec.withAlternative(QUATERNIONF_COMPONENTS, (Codec)AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new));
    public static Codec<Matrix4f> MATRIX4F = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 16).map(list -> {
        Matrix4f matrix4f = new Matrix4f();
        for (int i = 0; i < list.size(); ++i) {
            matrix4f.setRowColumn(i >> 2, i & 3, ((Float)list.get(i)).floatValue());
        }
        return matrix4f.determineProperties();
    }), matrix4f -> {
        FloatArrayList floatArrayList = new FloatArrayList(16);
        for (int i = 0; i < 16; ++i) {
            floatArrayList.add(matrix4f.getRowColumn(i >> 2, i & 3));
        }
        return floatArrayList;
    });
    public static final Codec<Integer> ARGB_COLOR_CODEC = Codec.withAlternative((Codec)Codec.INT, VECTOR4F, vector4f -> FastColor.ARGB32.colorFromFloat(vector4f.w(), vector4f.x(), vector4f.y(), vector4f.z()));
    public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE.flatComapMap(UnsignedBytes::toInt, n -> {
        if (n > 255) {
            return DataResult.error(() -> "Unsigned byte was too large: " + n + " > 255");
        }
        return DataResult.success((Object)n.byteValue());
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = ExtraCodecs.intRangeWithMessage(0, Integer.MAX_VALUE, n -> "Value must be non-negative: " + n);
    public static final Codec<Integer> POSITIVE_INT = ExtraCodecs.intRangeWithMessage(1, Integer.MAX_VALUE, n -> "Value must be positive: " + n);
    public static final Codec<Float> POSITIVE_FLOAT = ExtraCodecs.floatRangeMinExclusiveWithMessage(0.0f, Float.MAX_VALUE, f -> "Value must be positive: " + f);
    public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)Pattern.compile(string));
        }
        catch (PatternSyntaxException patternSyntaxException) {
            return DataResult.error(() -> "Invalid regex pattern '" + string + "': " + patternSyntaxException.getMessage());
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT_ISO8601 = ExtraCodecs.temporalCodec(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
    public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)Base64.getDecoder().decode((String)string));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error(() -> "Malformed base64 string");
        }
    }, byArray -> Base64.getEncoder().encodeToString((byte[])byArray));
    public static final Codec<String> ESCAPED_STRING = Codec.STRING.comapFlatMap(string -> DataResult.success((Object)StringEscapeUtils.unescapeJava((String)string)), StringEscapeUtils::escapeJava);
    public static final Codec<TagOrElementLocation> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap(string -> string.startsWith("#") ? ResourceLocation.read(string.substring(1)).map(resourceLocation -> new TagOrElementLocation((ResourceLocation)resourceLocation, true)) : ResourceLocation.read(string).map(resourceLocation -> new TagOrElementLocation((ResourceLocation)resourceLocation, false)), TagOrElementLocation::decoratedId);
    public static final Function<Optional<Long>, OptionalLong> toOptionalLong = optional -> optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = optionalLong -> optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty();
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap(longStream -> BitSet.valueOf(longStream.toArray()), bitSet -> Arrays.stream(bitSet.toLongArray()));
    private static final Codec<Property> PROPERTY = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(Property::name), (App)Codec.STRING.fieldOf("value").forGetter(Property::value), (App)Codec.STRING.lenientOptionalFieldOf("signature").forGetter(property -> Optional.ofNullable(property.signature()))).apply((Applicative)instance, (string, string2, optional) -> new Property(string, string2, (String)optional.orElse(null))));
    public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either((Codec)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING.listOf()), (Codec)PROPERTY.listOf()).xmap(either -> {
        PropertyMap propertyMap = new PropertyMap();
        either.ifLeft(map -> map.forEach((string, list) -> {
            for (String string2 : list) {
                propertyMap.put(string, (Object)new Property(string, string2));
            }
        })).ifRight(list -> {
            for (Property property : list) {
                propertyMap.put((Object)property.name(), (Object)property);
            }
        });
        return propertyMap;
    }, propertyMap -> Either.right(propertyMap.values().stream().toList()));
    public static final Codec<String> PLAYER_NAME = Codec.string((int)0, (int)16).validate(string -> {
        if (StringUtil.isValidPlayerName(string)) {
            return DataResult.success((Object)string);
        }
        return DataResult.error(() -> "Player name contained disallowed characters: '" + string + "'");
    });
    private static final MapCodec<GameProfile> GAME_PROFILE_WITHOUT_PROPERTIES = RecordCodecBuilder.mapCodec(instance -> instance.group((App)UUIDUtil.AUTHLIB_CODEC.fieldOf("id").forGetter(GameProfile::getId), (App)PLAYER_NAME.fieldOf("name").forGetter(GameProfile::getName)).apply((Applicative)instance, GameProfile::new));
    public static final Codec<GameProfile> GAME_PROFILE = RecordCodecBuilder.create(instance -> instance.group((App)GAME_PROFILE_WITHOUT_PROPERTIES.forGetter(Function.identity()), (App)PROPERTY_MAP.lenientOptionalFieldOf("properties", (Object)new PropertyMap()).forGetter(GameProfile::getProperties)).apply((Applicative)instance, (gameProfile, propertyMap) -> {
        propertyMap.forEach((string, property) -> gameProfile.getProperties().put(string, property));
        return gameProfile;
    }));
    public static final Codec<String> NON_EMPTY_STRING = Codec.STRING.validate(string -> string.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success((Object)string));
    public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap(string -> {
        int[] nArray = string.codePoints().toArray();
        if (nArray.length != 1) {
            return DataResult.error(() -> "Expected one codepoint, got: " + string);
        }
        return DataResult.success((Object)nArray[0]);
    }, Character::toString);
    public static Codec<String> RESOURCE_PATH_CODEC = Codec.STRING.validate(string -> {
        if (!ResourceLocation.isValidPath(string)) {
            return DataResult.error(() -> "Invalid string to use as a resource path element: " + string);
        }
        return DataResult.success((Object)string);
    });

    public static <T> Codec<T> converter(DynamicOps<T> dynamicOps) {
        return Codec.PASSTHROUGH.xmap(dynamic -> dynamic.convert(dynamicOps).getValue(), object -> new Dynamic(dynamicOps, object));
    }

    public static <P, I> Codec<I> intervalCodec(Codec<P> codec, String string, String string2, BiFunction<P, P, DataResult<I>> biFunction, Function<I, P> function, Function<I, P> function2) {
        Codec codec2 = Codec.list(codec).comapFlatMap(list2 -> Util.fixedSize(list2, 2).flatMap(list -> {
            Object e = list.get(0);
            Object e2 = list.get(1);
            return (DataResult)biFunction.apply(e, e2);
        }), object -> ImmutableList.of(function.apply(object), function2.apply(object)));
        Codec codec3 = RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf(string).forGetter(Pair::getFirst), (App)codec.fieldOf(string2).forGetter(Pair::getSecond)).apply((Applicative)instance, Pair::of)).comapFlatMap(pair -> (DataResult)biFunction.apply(pair.getFirst(), pair.getSecond()), object -> Pair.of(function.apply(object), function2.apply(object)));
        Codec codec4 = Codec.withAlternative((Codec)codec2, (Codec)codec3);
        return Codec.either(codec, (Codec)codec4).comapFlatMap(either -> (DataResult)either.map(object -> (DataResult)biFunction.apply(object, object), DataResult::success), object -> {
            Object r;
            Object r2 = function.apply(object);
            if (Objects.equals(r2, r = function2.apply(object))) {
                return Either.left(r2);
            }
            return Either.right((Object)object);
        });
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A a) {
        return new Codec.ResultFunction<A>(){

            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> dynamicOps, T t, DataResult<Pair<A, T>> dataResult) {
                MutableObject mutableObject = new MutableObject();
                Optional optional = dataResult.resultOrPartial(arg_0 -> ((MutableObject)mutableObject).setValue(arg_0));
                if (optional.isPresent()) {
                    return dataResult;
                }
                return DataResult.error(() -> "(" + (String)mutableObject.getValue() + " -> using default)", (Object)Pair.of((Object)a, t));
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, A a2, DataResult<T> dataResult) {
                return dataResult;
            }

            public String toString() {
                return "OrElsePartial[" + String.valueOf(a) + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> toIntFunction, IntFunction<E> intFunction, int n2) {
        return Codec.INT.flatXmap(n -> Optional.ofNullable(intFunction.apply((int)n)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element id: " + n)), object -> {
            int n2 = toIntFunction.applyAsInt(object);
            return n2 == n2 ? DataResult.error(() -> "Element with unknown id: " + String.valueOf(object)) : DataResult.success((Object)n2);
        });
    }

    public static <E> Codec<E> orCompressed(final Codec<E> codec, final Codec<E> codec2) {
        return new Codec<E>(){

            public <T> DataResult<T> encode(E e, DynamicOps<T> dynamicOps, T t) {
                if (dynamicOps.compressMaps()) {
                    return codec2.encode(e, dynamicOps, t);
                }
                return codec.encode(e, dynamicOps, t);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T t) {
                if (dynamicOps.compressMaps()) {
                    return codec2.decode(dynamicOps, t);
                }
                return codec.decode(dynamicOps, t);
            }

            public String toString() {
                return String.valueOf(codec) + " orCompressed " + String.valueOf(codec2);
            }
        };
    }

    public static <E> MapCodec<E> orCompressed(final MapCodec<E> mapCodec, final MapCodec<E> mapCodec2) {
        return new MapCodec<E>(){

            public <T> RecordBuilder<T> encode(E e, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                if (dynamicOps.compressMaps()) {
                    return mapCodec2.encode(e, dynamicOps, recordBuilder);
                }
                return mapCodec.encode(e, dynamicOps, recordBuilder);
            }

            public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                if (dynamicOps.compressMaps()) {
                    return mapCodec2.decode(dynamicOps, mapLike);
                }
                return mapCodec.decode(dynamicOps, mapLike);
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return mapCodec2.keys(dynamicOps);
            }

            public String toString() {
                return String.valueOf(mapCodec) + " orCompressed " + String.valueOf(mapCodec2);
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, final Function<E, Lifecycle> function, final Function<E, Lifecycle> function2) {
        return codec.mapResult(new Codec.ResultFunction<E>(){

            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> dynamicOps, T t, DataResult<Pair<E, T>> dataResult) {
                return dataResult.result().map(pair -> dataResult.setLifecycle((Lifecycle)function.apply(pair.getFirst()))).orElse(dataResult);
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, E e, DataResult<T> dataResult) {
                return dataResult.setLifecycle((Lifecycle)function2.apply(e));
            }

            public String toString() {
                return "WithLifecycle[" + String.valueOf(function) + " " + String.valueOf(function2) + "]";
            }
        });
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, Function<E, Lifecycle> function) {
        return ExtraCodecs.overrideLifecycle(codec, function, function);
    }

    public static <K, V> StrictUnboundedMapCodec<K, V> strictUnboundedMap(Codec<K> codec, Codec<V> codec2) {
        return new StrictUnboundedMapCodec<K, V>(codec, codec2);
    }

    private static Codec<Integer> intRangeWithMessage(int n, int n2, Function<Integer, String> function) {
        return Codec.INT.validate(n3 -> {
            if (n3.compareTo(n) >= 0 && n3.compareTo(n2) <= 0) {
                return DataResult.success((Object)n3);
            }
            return DataResult.error(() -> (String)function.apply((Integer)n3));
        });
    }

    public static Codec<Integer> intRange(int n, int n2) {
        return ExtraCodecs.intRangeWithMessage(n, n2, n3 -> "Value must be within range [" + n + ";" + n2 + "]: " + n3);
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float f2, Function<Float, String> function) {
        return Codec.FLOAT.validate(f3 -> {
            if (f3.compareTo(Float.valueOf(f)) > 0 && f3.compareTo(Float.valueOf(f2)) <= 0) {
                return DataResult.success((Object)f3);
            }
            return DataResult.error(() -> (String)function.apply((Float)f3));
        });
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
        return codec.validate(list -> list.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success((Object)list));
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> codec) {
        return codec.validate(holderSet -> {
            if (holderSet.unwrap().right().filter(List::isEmpty).isPresent()) {
                return DataResult.error(() -> "List must have contents");
            }
            return DataResult.success((Object)holderSet);
        });
    }

    public static <E> MapCodec<E> retrieveContext(Function<DynamicOps<?>, DataResult<E>> function) {
        class ContextRetrievalCodec
        extends MapCodec<E> {
            final /* synthetic */ Function val$getter;

            ContextRetrievalCodec(Function function) {
                this.val$getter = function;
            }

            public <T> RecordBuilder<T> encode(E e, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                return recordBuilder;
            }

            public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                return (DataResult)this.val$getter.apply(dynamicOps);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + String.valueOf(this.val$getter) + "]";
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Stream.empty();
            }
        }
        return new ContextRetrievalCodec(function);
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> function) {
        return collection -> {
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                Object r = function.apply(iterator.next());
                while (iterator.hasNext()) {
                    Object e = iterator.next();
                    Object r2 = function.apply(e);
                    if (r2 == r) continue;
                    return DataResult.error(() -> "Mixed type list: element " + String.valueOf(e) + " had type " + String.valueOf(r2) + ", but list is of type " + String.valueOf(r));
                }
            }
            return DataResult.success((Object)collection, (Lifecycle)Lifecycle.stable());
        };
    }

    public static <A> Codec<A> catchDecoderException(final Codec<A> codec) {
        return Codec.of(codec, (Decoder)new Decoder<A>(){

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T t) {
                try {
                    return codec.decode(dynamicOps, t);
                }
                catch (Exception exception) {
                    return DataResult.error(() -> "Caught exception decoding " + String.valueOf(t) + ": " + exception.getMessage());
                }
            }
        });
    }

    public static Codec<TemporalAccessor> temporalCodec(DateTimeFormatter dateTimeFormatter) {
        return Codec.STRING.comapFlatMap(string -> {
            try {
                return DataResult.success((Object)dateTimeFormatter.parse((CharSequence)string));
            }
            catch (Exception exception) {
                return DataResult.error(exception::getMessage);
            }
        }, dateTimeFormatter::format);
    }

    public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> mapCodec) {
        return mapCodec.xmap(toOptionalLong, fromOptionalLong);
    }

    public static <K, V> Codec<Map<K, V>> sizeLimitedMap(Codec<Map<K, V>> codec, int n) {
        return codec.validate(map -> {
            if (map.size() > n) {
                return DataResult.error(() -> "Map is too long: " + map.size() + ", expected range [0-" + n + "]");
            }
            return DataResult.success((Object)map);
        });
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> codec) {
        return Codec.unboundedMap(codec, (Codec)Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    @Deprecated
    public static <K, V> MapCodec<V> dispatchOptionalValue(final String string, final String string2, final Codec<K> codec, final Function<? super V, ? extends K> function, final Function<? super K, ? extends Codec<? extends V>> function2) {
        return new MapCodec<V>(){

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Stream.of(dynamicOps.createString(string), dynamicOps.createString(string2));
            }

            public <T> DataResult<V> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                Object object = mapLike.get(string);
                if (object == null) {
                    return DataResult.error(() -> "Missing \"" + string + "\" in: " + String.valueOf(mapLike));
                }
                return codec.decode(dynamicOps, object).flatMap(pair -> {
                    Object object = Objects.requireNonNullElseGet(mapLike.get(string2), () -> ((DynamicOps)dynamicOps).emptyMap());
                    return ((Codec)function2.apply(pair.getFirst())).decode(dynamicOps, object).map(Pair::getFirst);
                });
            }

            public <T> RecordBuilder<T> encode(V v, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                Object r = function.apply(v);
                recordBuilder.add(string, codec.encodeStart(dynamicOps, r));
                DataResult<T> dataResult = this.encode((Codec)function2.apply(r), v, dynamicOps);
                if (dataResult.result().isEmpty() || !Objects.equals(dataResult.result().get(), dynamicOps.emptyMap())) {
                    recordBuilder.add(string2, dataResult);
                }
                return recordBuilder;
            }

            private <T, V2 extends V> DataResult<T> encode(Codec<V2> codec2, V v, DynamicOps<T> dynamicOps) {
                return codec2.encodeStart(dynamicOps, v);
            }
        };
    }

    public static <A> Codec<Optional<A>> optionalEmptyMap(final Codec<A> codec) {
        return new Codec<Optional<A>>(){

            public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> dynamicOps, T t) {
                if (7.isEmptyMap(dynamicOps, t)) {
                    return DataResult.success((Object)Pair.of(Optional.empty(), t));
                }
                return codec.decode(dynamicOps, t).map(pair -> pair.mapFirst(Optional::of));
            }

            private static <T> boolean isEmptyMap(DynamicOps<T> dynamicOps, T t) {
                Optional optional = dynamicOps.getMap(t).result();
                return optional.isPresent() && ((MapLike)optional.get()).entries().findAny().isEmpty();
            }

            public <T> DataResult<T> encode(Optional<A> optional, DynamicOps<T> dynamicOps, T t) {
                if (optional.isEmpty()) {
                    return DataResult.success((Object)dynamicOps.emptyMap());
                }
                return codec.encode(optional.get(), dynamicOps, t);
            }

            public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                return this.encode((Optional)object, dynamicOps, object2);
            }
        };
    }

    public record StrictUnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements Codec<Map<K, V>>,
    BaseMapCodec<K, V>
    {
        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Pair pair : mapLike.entries().toList()) {
                String string;
                DataResult dataResult;
                DataResult dataResult2 = this.keyCodec().parse(dynamicOps, pair.getFirst());
                DataResult dataResult3 = dataResult2.apply2stable(Pair::of, dataResult = this.elementCodec().parse(dynamicOps, pair.getSecond()));
                Optional optional = dataResult3.error();
                if (optional.isPresent()) {
                    string = ((DataResult.Error)optional.get()).message();
                    return DataResult.error(() -> {
                        if (dataResult2.result().isPresent()) {
                            return "Map entry '" + String.valueOf(dataResult2.result().get()) + "' : " + string;
                        }
                        return string;
                    });
                }
                if (dataResult3.result().isPresent()) {
                    string = (Pair)dataResult3.result().get();
                    builder.put(string.getFirst(), string.getSecond());
                    continue;
                }
                return DataResult.error(() -> "Empty or invalid map contents are not allowed");
            }
            ImmutableMap immutableMap = builder.build();
            return DataResult.success((Object)immutableMap);
        }

        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> dynamicOps, T t) {
            return dynamicOps.getMap(t).setLifecycle(Lifecycle.stable()).flatMap(mapLike -> this.decode(dynamicOps, (Object)mapLike)).map(map -> Pair.of((Object)map, (Object)t));
        }

        public <T> DataResult<T> encode(Map<K, V> map, DynamicOps<T> dynamicOps, T t) {
            return this.encode(map, dynamicOps, dynamicOps.mapBuilder()).build(t);
        }

        @Override
        public String toString() {
            return "StrictUnboundedMapCodec[" + String.valueOf(this.keyCodec) + " -> " + String.valueOf(this.elementCodec) + "]";
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Map)object, dynamicOps, object2);
        }
    }

    public record TagOrElementLocation(ResourceLocation id, boolean tag) {
        @Override
        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? "#" + String.valueOf(this.id) : this.id.toString();
        }
    }
}

