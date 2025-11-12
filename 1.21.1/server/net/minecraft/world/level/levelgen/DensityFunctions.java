/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.doubles.Double2DoubleFunction
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
    private static final Codec<DensityFunction> CODEC = BuiltInRegistries.DENSITY_FUNCTION_TYPE.byNameCodec().dispatch(densityFunction -> densityFunction.codec().codec(), Function.identity());
    protected static final double MAX_REASONABLE_NOISE_VALUE = 1000000.0;
    static final Codec<Double> NOISE_VALUE_CODEC = Codec.doubleRange((double)-1000000.0, (double)1000000.0);
    public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(NOISE_VALUE_CODEC, CODEC).xmap(either -> (DensityFunction)either.map(DensityFunctions::constant, Function.identity()), densityFunction -> {
        if (densityFunction instanceof Constant) {
            Constant constant = (Constant)densityFunction;
            return Either.left((Object)constant.value());
        }
        return Either.right((Object)densityFunction);
    });

    public static MapCodec<? extends DensityFunction> bootstrap(Registry<MapCodec<? extends DensityFunction>> registry) {
        DensityFunctions.register(registry, "blend_alpha", BlendAlpha.CODEC);
        DensityFunctions.register(registry, "blend_offset", BlendOffset.CODEC);
        DensityFunctions.register(registry, "beardifier", BeardifierMarker.CODEC);
        DensityFunctions.register(registry, "old_blended_noise", BlendedNoise.CODEC);
        for (Marker.Type enum_ : Marker.Type.values()) {
            DensityFunctions.register(registry, enum_.getSerializedName(), enum_.codec);
        }
        DensityFunctions.register(registry, "noise", Noise.CODEC);
        DensityFunctions.register(registry, "end_islands", EndIslandDensityFunction.CODEC);
        DensityFunctions.register(registry, "weird_scaled_sampler", WeirdScaledSampler.CODEC);
        DensityFunctions.register(registry, "shifted_noise", ShiftedNoise.CODEC);
        DensityFunctions.register(registry, "range_choice", RangeChoice.CODEC);
        DensityFunctions.register(registry, "shift_a", ShiftA.CODEC);
        DensityFunctions.register(registry, "shift_b", ShiftB.CODEC);
        DensityFunctions.register(registry, "shift", Shift.CODEC);
        DensityFunctions.register(registry, "blend_density", BlendDensity.CODEC);
        DensityFunctions.register(registry, "clamp", Clamp.CODEC);
        for (Enum enum_ : Mapped.Type.values()) {
            DensityFunctions.register(registry, ((Mapped.Type)enum_).getSerializedName(), ((Mapped.Type)enum_).codec);
        }
        for (Enum enum_ : TwoArgumentSimpleFunction.Type.values()) {
            DensityFunctions.register(registry, ((TwoArgumentSimpleFunction.Type)enum_).getSerializedName(), ((TwoArgumentSimpleFunction.Type)enum_).codec);
        }
        DensityFunctions.register(registry, "spline", Spline.CODEC);
        DensityFunctions.register(registry, "constant", Constant.CODEC);
        return DensityFunctions.register(registry, "y_clamped_gradient", YClampedGradient.CODEC);
    }

    private static MapCodec<? extends DensityFunction> register(Registry<MapCodec<? extends DensityFunction>> registry, String string, KeyDispatchDataCodec<? extends DensityFunction> keyDispatchDataCodec) {
        return Registry.register(registry, string, keyDispatchDataCodec.codec());
    }

    static <A, O> KeyDispatchDataCodec<O> singleArgumentCodec(Codec<A> codec, Function<A, O> function, Function<O, A> function2) {
        return KeyDispatchDataCodec.of(codec.fieldOf("argument").xmap(function, function2));
    }

    static <O> KeyDispatchDataCodec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> function, Function<O, DensityFunction> function2) {
        return DensityFunctions.singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, function, function2);
    }

    static <O> KeyDispatchDataCodec<O> doubleFunctionArgumentCodec(BiFunction<DensityFunction, DensityFunction, O> biFunction, Function<O, DensityFunction> function, Function<O, DensityFunction> function2) {
        return KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group((App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(function), (App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(function2)).apply((Applicative)instance, biFunction)));
    }

    static <O> KeyDispatchDataCodec<O> makeCodec(MapCodec<O> mapCodec) {
        return KeyDispatchDataCodec.of(mapCodec);
    }

    private DensityFunctions() {
    }

    public static DensityFunction interpolated(DensityFunction densityFunction) {
        return new Marker(Marker.Type.Interpolated, densityFunction);
    }

    public static DensityFunction flatCache(DensityFunction densityFunction) {
        return new Marker(Marker.Type.FlatCache, densityFunction);
    }

    public static DensityFunction cache2d(DensityFunction densityFunction) {
        return new Marker(Marker.Type.Cache2D, densityFunction);
    }

    public static DensityFunction cacheOnce(DensityFunction densityFunction) {
        return new Marker(Marker.Type.CacheOnce, densityFunction);
    }

    public static DensityFunction cacheAllInCell(DensityFunction densityFunction) {
        return new Marker(Marker.Type.CacheAllInCell, densityFunction);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, @Deprecated double d, double d2, double d3, double d4) {
        return DensityFunctions.mapFromUnitTo(new Noise(new DensityFunction.NoiseHolder(holder), d, d2), d3, d4);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, double d, double d2, double d3) {
        return DensityFunctions.mappedNoise(holder, 1.0, d, d2, d3);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, double d, double d2) {
        return DensityFunctions.mappedNoise(holder, 1.0, 1.0, d, d2);
    }

    public static DensityFunction shiftedNoise2d(DensityFunction densityFunction, DensityFunction densityFunction2, double d, Holder<NormalNoise.NoiseParameters> holder) {
        return new ShiftedNoise(densityFunction, DensityFunctions.zero(), densityFunction2, d, 0.0, new DensityFunction.NoiseHolder(holder));
    }

    public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder) {
        return DensityFunctions.noise(holder, 1.0, 1.0);
    }

    public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder, double d, double d2) {
        return new Noise(new DensityFunction.NoiseHolder(holder), d, d2);
    }

    public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder, double d) {
        return DensityFunctions.noise(holder, 1.0, d);
    }

    public static DensityFunction rangeChoice(DensityFunction densityFunction, double d, double d2, DensityFunction densityFunction2, DensityFunction densityFunction3) {
        return new RangeChoice(densityFunction, d, d2, densityFunction2, densityFunction3);
    }

    public static DensityFunction shiftA(Holder<NormalNoise.NoiseParameters> holder) {
        return new ShiftA(new DensityFunction.NoiseHolder(holder));
    }

    public static DensityFunction shiftB(Holder<NormalNoise.NoiseParameters> holder) {
        return new ShiftB(new DensityFunction.NoiseHolder(holder));
    }

    public static DensityFunction shift(Holder<NormalNoise.NoiseParameters> holder) {
        return new Shift(new DensityFunction.NoiseHolder(holder));
    }

    public static DensityFunction blendDensity(DensityFunction densityFunction) {
        return new BlendDensity(densityFunction);
    }

    public static DensityFunction endIslands(long l) {
        return new EndIslandDensityFunction(l);
    }

    public static DensityFunction weirdScaledSampler(DensityFunction densityFunction, Holder<NormalNoise.NoiseParameters> holder, WeirdScaledSampler.RarityValueMapper rarityValueMapper) {
        return new WeirdScaledSampler(densityFunction, new DensityFunction.NoiseHolder(holder), rarityValueMapper);
    }

    public static DensityFunction add(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.ADD, densityFunction, densityFunction2);
    }

    public static DensityFunction mul(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.MUL, densityFunction, densityFunction2);
    }

    public static DensityFunction min(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.MIN, densityFunction, densityFunction2);
    }

    public static DensityFunction max(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.MAX, densityFunction, densityFunction2);
    }

    public static DensityFunction spline(CubicSpline<Spline.Point, Spline.Coordinate> cubicSpline) {
        return new Spline(cubicSpline);
    }

    public static DensityFunction zero() {
        return Constant.ZERO;
    }

    public static DensityFunction constant(double d) {
        return new Constant(d);
    }

    public static DensityFunction yClampedGradient(int n, int n2, double d, double d2) {
        return new YClampedGradient(n, n2, d, d2);
    }

    public static DensityFunction map(DensityFunction densityFunction, Mapped.Type type) {
        return Mapped.create(type, densityFunction);
    }

    private static DensityFunction mapFromUnitTo(DensityFunction densityFunction, double d, double d2) {
        double d3 = (d + d2) * 0.5;
        double d4 = (d2 - d) * 0.5;
        return DensityFunctions.add(DensityFunctions.constant(d3), DensityFunctions.mul(DensityFunctions.constant(d4), densityFunction));
    }

    public static DensityFunction blendAlpha() {
        return BlendAlpha.INSTANCE;
    }

    public static DensityFunction blendOffset() {
        return BlendOffset.INSTANCE;
    }

    public static DensityFunction lerp(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3) {
        if (densityFunction2 instanceof Constant) {
            Constant constant = (Constant)densityFunction2;
            return DensityFunctions.lerp(densityFunction, constant.value, densityFunction3);
        }
        DensityFunction densityFunction4 = DensityFunctions.cacheOnce(densityFunction);
        DensityFunction densityFunction5 = DensityFunctions.add(DensityFunctions.mul(densityFunction4, DensityFunctions.constant(-1.0)), DensityFunctions.constant(1.0));
        return DensityFunctions.add(DensityFunctions.mul(densityFunction2, densityFunction5), DensityFunctions.mul(densityFunction3, densityFunction4));
    }

    public static DensityFunction lerp(DensityFunction densityFunction, double d, DensityFunction densityFunction2) {
        return DensityFunctions.add(DensityFunctions.mul(densityFunction, DensityFunctions.add(densityFunction2, DensityFunctions.constant(-d))), DensityFunctions.constant(d));
    }

    protected static enum BlendAlpha implements DensityFunction.SimpleFunction
    {
        INSTANCE;

        public static final KeyDispatchDataCodec<DensityFunction> CODEC;

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return 1.0;
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            Arrays.fill(dArray, 1.0);
        }

        @Override
        public double minValue() {
            return 1.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }

        static {
            CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)INSTANCE));
        }
    }

    protected static enum BlendOffset implements DensityFunction.SimpleFunction
    {
        INSTANCE;

        public static final KeyDispatchDataCodec<DensityFunction> CODEC;

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return 0.0;
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            Arrays.fill(dArray, 0.0);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }

        static {
            CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)INSTANCE));
        }
    }

    protected static enum BeardifierMarker implements BeardifierOrMarker
    {
        INSTANCE;


        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return 0.0;
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            Arrays.fill(dArray, 0.0);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }
    }

    protected record Marker(Type type, DensityFunction wrapped) implements MarkerOrMarked
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.wrapped.compute(functionContext);
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            this.wrapped.fillArray(dArray, contextProvider);
        }

        @Override
        public double minValue() {
            return this.wrapped.minValue();
        }

        @Override
        public double maxValue() {
            return this.wrapped.maxValue();
        }

        static enum Type implements StringRepresentable
        {
            Interpolated("interpolated"),
            FlatCache("flat_cache"),
            Cache2D("cache_2d"),
            CacheOnce("cache_once"),
            CacheAllInCell("cache_all_in_cell");

            private final String name;
            final KeyDispatchDataCodec<MarkerOrMarked> codec = DensityFunctions.singleFunctionArgumentCodec(densityFunction -> new Marker(this, (DensityFunction)densityFunction), MarkerOrMarked::wrapped);

            private Type(String string2) {
                this.name = string2;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    protected record Noise(DensityFunction.NoiseHolder noise, @Deprecated double xzScale, double yScale) implements DensityFunction
    {
        public static final MapCodec<Noise> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(Noise::noise), (App)Codec.DOUBLE.fieldOf("xz_scale").forGetter(Noise::xzScale), (App)Codec.DOUBLE.fieldOf("y_scale").forGetter(Noise::yScale)).apply((Applicative)instance, Noise::new));
        public static final KeyDispatchDataCodec<Noise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.noise.getValue((double)functionContext.blockX() * this.xzScale, (double)functionContext.blockY() * this.yScale, (double)functionContext.blockZ() * this.xzScale);
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new Noise(visitor.visitNoise(this.noise), this.xzScale, this.yScale));
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static final class EndIslandDensityFunction
    implements DensityFunction.SimpleFunction {
        public static final KeyDispatchDataCodec<EndIslandDensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)new EndIslandDensityFunction(0L)));
        private static final float ISLAND_THRESHOLD = -0.9f;
        private final SimplexNoise islandNoise;

        public EndIslandDensityFunction(long l) {
            LegacyRandomSource legacyRandomSource = new LegacyRandomSource(l);
            legacyRandomSource.consumeCount(17292);
            this.islandNoise = new SimplexNoise(legacyRandomSource);
        }

        private static float getHeightValue(SimplexNoise simplexNoise, int n, int n2) {
            int n3 = n / 2;
            int n4 = n2 / 2;
            int n5 = n % 2;
            int n6 = n2 % 2;
            float f = 100.0f - Mth.sqrt(n * n + n2 * n2) * 8.0f;
            f = Mth.clamp(f, -100.0f, 80.0f);
            for (int i = -12; i <= 12; ++i) {
                for (int j = -12; j <= 12; ++j) {
                    long l = n3 + i;
                    long l2 = n4 + j;
                    if (l * l + l2 * l2 <= 4096L || !(simplexNoise.getValue(l, l2) < (double)-0.9f)) continue;
                    float f2 = (Mth.abs(l) * 3439.0f + Mth.abs(l2) * 147.0f) % 13.0f + 9.0f;
                    float f3 = n5 - i * 2;
                    float f4 = n6 - j * 2;
                    float f5 = 100.0f - Mth.sqrt(f3 * f3 + f4 * f4) * f2;
                    f5 = Mth.clamp(f5, -100.0f, 80.0f);
                    f = Math.max(f, f5);
                }
            }
            return f;
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return ((double)EndIslandDensityFunction.getHeightValue(this.islandNoise, functionContext.blockX() / 8, functionContext.blockZ() / 8) - 8.0) / 128.0;
        }

        @Override
        public double minValue() {
            return -0.84375;
        }

        @Override
        public double maxValue() {
            return 0.5625;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected record WeirdScaledSampler(DensityFunction input, DensityFunction.NoiseHolder noise, RarityValueMapper rarityValueMapper) implements TransformerWithContext
    {
        private static final MapCodec<WeirdScaledSampler> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(WeirdScaledSampler::input), (App)DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(WeirdScaledSampler::noise), (App)RarityValueMapper.CODEC.fieldOf("rarity_value_mapper").forGetter(WeirdScaledSampler::rarityValueMapper)).apply((Applicative)instance, WeirdScaledSampler::new));
        public static final KeyDispatchDataCodec<WeirdScaledSampler> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double transform(DensityFunction.FunctionContext functionContext, double d) {
            double d2 = this.rarityValueMapper.mapper.get(d);
            return d2 * Math.abs(this.noise.getValue((double)functionContext.blockX() / d2, (double)functionContext.blockY() / d2, (double)functionContext.blockZ() / d2));
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new WeirdScaledSampler(this.input.mapAll(visitor), visitor.visitNoise(this.noise), this.rarityValueMapper));
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return this.rarityValueMapper.maxRarity * this.noise.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }

        public static enum RarityValueMapper implements StringRepresentable
        {
            TYPE1("type_1", NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0),
            TYPE2("type_2", NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0);

            public static final Codec<RarityValueMapper> CODEC;
            private final String name;
            final Double2DoubleFunction mapper;
            final double maxRarity;

            private RarityValueMapper(String string2, Double2DoubleFunction double2DoubleFunction, double d) {
                this.name = string2;
                this.mapper = double2DoubleFunction;
                this.maxRarity = d;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }

            static {
                CODEC = StringRepresentable.fromEnum(RarityValueMapper::values);
            }
        }
    }

    protected record ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.NoiseHolder noise) implements DensityFunction
    {
        private static final MapCodec<ShiftedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(ShiftedNoise::shiftX), (App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(ShiftedNoise::shiftY), (App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(ShiftedNoise::shiftZ), (App)Codec.DOUBLE.fieldOf("xz_scale").forGetter(ShiftedNoise::xzScale), (App)Codec.DOUBLE.fieldOf("y_scale").forGetter(ShiftedNoise::yScale), (App)DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(ShiftedNoise::noise)).apply((Applicative)instance, ShiftedNoise::new));
        public static final KeyDispatchDataCodec<ShiftedNoise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            double d = (double)functionContext.blockX() * this.xzScale + this.shiftX.compute(functionContext);
            double d2 = (double)functionContext.blockY() * this.yScale + this.shiftY.compute(functionContext);
            double d3 = (double)functionContext.blockZ() * this.xzScale + this.shiftZ.compute(functionContext);
            return this.noise.getValue(d, d2, d3);
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new ShiftedNoise(this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor), this.xzScale, this.yScale, visitor.visitNoise(this.noise)));
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) implements DensityFunction
    {
        public static final MapCodec<RangeChoice> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(RangeChoice::input), (App)NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(RangeChoice::minInclusive), (App)NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(RangeChoice::maxExclusive), (App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(RangeChoice::whenInRange), (App)DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(RangeChoice::whenOutOfRange)).apply((Applicative)instance, RangeChoice::new));
        public static final KeyDispatchDataCodec<RangeChoice> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            double d = this.input.compute(functionContext);
            if (d >= this.minInclusive && d < this.maxExclusive) {
                return this.whenInRange.compute(functionContext);
            }
            return this.whenOutOfRange.compute(functionContext);
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(dArray, contextProvider);
            for (int i = 0; i < dArray.length; ++i) {
                double d = dArray[i];
                dArray[i] = d >= this.minInclusive && d < this.maxExclusive ? this.whenInRange.compute(contextProvider.forIndex(i)) : this.whenOutOfRange.compute(contextProvider.forIndex(i));
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new RangeChoice(this.input.mapAll(visitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(visitor), this.whenOutOfRange.mapAll(visitor)));
        }

        @Override
        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        @Override
        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected record ShiftA(DensityFunction.NoiseHolder offsetNoise) implements ShiftNoise
    {
        static final KeyDispatchDataCodec<ShiftA> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.NoiseHolder.CODEC, ShiftA::new, ShiftA::offsetNoise);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockX(), 0.0, functionContext.blockZ());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new ShiftA(visitor.visitNoise(this.offsetNoise)));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected record ShiftB(DensityFunction.NoiseHolder offsetNoise) implements ShiftNoise
    {
        static final KeyDispatchDataCodec<ShiftB> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.NoiseHolder.CODEC, ShiftB::new, ShiftB::offsetNoise);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockZ(), functionContext.blockX(), 0.0);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new ShiftB(visitor.visitNoise(this.offsetNoise)));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected record Shift(DensityFunction.NoiseHolder offsetNoise) implements ShiftNoise
    {
        static final KeyDispatchDataCodec<Shift> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.NoiseHolder.CODEC, Shift::new, Shift::offsetNoise);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new Shift(visitor.visitNoise(this.offsetNoise)));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    record BlendDensity(DensityFunction input) implements TransformerWithContext
    {
        static final KeyDispatchDataCodec<BlendDensity> CODEC = DensityFunctions.singleFunctionArgumentCodec(BlendDensity::new, BlendDensity::input);

        @Override
        public double transform(DensityFunction.FunctionContext functionContext, double d) {
            return functionContext.getBlender().blendDensity(functionContext, d);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new BlendDensity(this.input.mapAll(visitor)));
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected record Clamp(DensityFunction input, double minValue, double maxValue) implements PureTransformer
    {
        private static final MapCodec<Clamp> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(Clamp::input), (App)NOISE_VALUE_CODEC.fieldOf("min").forGetter(Clamp::minValue), (App)NOISE_VALUE_CODEC.fieldOf("max").forGetter(Clamp::maxValue)).apply((Applicative)instance, Clamp::new));
        public static final KeyDispatchDataCodec<Clamp> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double transform(double d) {
            return Mth.clamp(d, this.minValue, this.maxValue);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return new Clamp(this.input.mapAll(visitor), this.minValue, this.maxValue);
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected record Mapped(Type type, DensityFunction input, double minValue, double maxValue) implements PureTransformer
    {
        public static Mapped create(Type type, DensityFunction densityFunction) {
            double d = densityFunction.minValue();
            double d2 = Mapped.transform(type, d);
            double d3 = Mapped.transform(type, densityFunction.maxValue());
            if (type == Type.ABS || type == Type.SQUARE) {
                return new Mapped(type, densityFunction, Math.max(0.0, d), Math.max(d2, d3));
            }
            return new Mapped(type, densityFunction, d2, d3);
        }

        private static double transform(Type type, double d) {
            return switch (type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Math.abs(d);
                case 1 -> d * d;
                case 2 -> d * d * d;
                case 3 -> {
                    if (d > 0.0) {
                        yield d;
                    }
                    yield d * 0.5;
                }
                case 4 -> {
                    if (d > 0.0) {
                        yield d;
                    }
                    yield d * 0.25;
                }
                case 5 -> {
                    double var3_2 = Mth.clamp(d, -1.0, 1.0);
                    yield var3_2 / 2.0 - var3_2 * var3_2 * var3_2 / 24.0;
                }
            };
        }

        @Override
        public double transform(double d) {
            return Mapped.transform(this.type, d);
        }

        @Override
        public Mapped mapAll(DensityFunction.Visitor visitor) {
            return Mapped.create(this.type, this.input.mapAll(visitor));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return this.type.codec;
        }

        @Override
        public /* synthetic */ DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return this.mapAll(visitor);
        }

        static enum Type implements StringRepresentable
        {
            ABS("abs"),
            SQUARE("square"),
            CUBE("cube"),
            HALF_NEGATIVE("half_negative"),
            QUARTER_NEGATIVE("quarter_negative"),
            SQUEEZE("squeeze");

            private final String name;
            final KeyDispatchDataCodec<Mapped> codec = DensityFunctions.singleFunctionArgumentCodec(densityFunction -> Mapped.create(this, densityFunction), Mapped::input);

            private Type(String string2) {
                this.name = string2;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    static interface TwoArgumentSimpleFunction
    extends DensityFunction {
        public static final Logger LOGGER = LogUtils.getLogger();

        public static TwoArgumentSimpleFunction create(Type type, DensityFunction densityFunction, DensityFunction densityFunction2) {
            double d;
            double d2 = densityFunction.minValue();
            double d3 = densityFunction2.minValue();
            double d4 = densityFunction.maxValue();
            double d5 = densityFunction2.maxValue();
            if (type == Type.MIN || type == Type.MAX) {
                boolean bl;
                boolean bl2 = d2 >= d5;
                boolean bl3 = bl = d3 >= d4;
                if (bl2 || bl) {
                    LOGGER.warn("Creating a " + String.valueOf(type) + " function between two non-overlapping inputs: " + String.valueOf(densityFunction) + " and " + String.valueOf(densityFunction2));
                }
            }
            double d6 = switch (type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> d2 + d3;
                case 3 -> Math.max(d2, d3);
                case 2 -> Math.min(d2, d3);
                case 1 -> d2 > 0.0 && d3 > 0.0 ? d2 * d3 : (d4 < 0.0 && d5 < 0.0 ? d4 * d5 : Math.min(d2 * d5, d4 * d3));
            };
            switch (type.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    double d7 = d4 + d5;
                    break;
                }
                case 3: {
                    double d7 = Math.max(d4, d5);
                    break;
                }
                case 2: {
                    double d7 = Math.min(d4, d5);
                    break;
                }
                case 1: {
                    double d7 = d2 > 0.0 && d3 > 0.0 ? d4 * d5 : (d = d4 < 0.0 && d5 < 0.0 ? d2 * d3 : Math.max(d2 * d3, d4 * d5));
                }
            }
            if (type == Type.MUL || type == Type.ADD) {
                if (densityFunction instanceof Constant) {
                    Constant constant = (Constant)densityFunction;
                    return new MulOrAdd(type == Type.ADD ? MulOrAdd.Type.ADD : MulOrAdd.Type.MUL, densityFunction2, d6, d, constant.value);
                }
                if (densityFunction2 instanceof Constant) {
                    Constant constant = (Constant)densityFunction2;
                    return new MulOrAdd(type == Type.ADD ? MulOrAdd.Type.ADD : MulOrAdd.Type.MUL, densityFunction, d6, d, constant.value);
                }
            }
            return new Ap2(type, densityFunction, densityFunction2, d6, d);
        }

        public Type type();

        public DensityFunction argument1();

        public DensityFunction argument2();

        @Override
        default public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return this.type().codec;
        }

        public static enum Type implements StringRepresentable
        {
            ADD("add"),
            MUL("mul"),
            MIN("min"),
            MAX("max");

            final KeyDispatchDataCodec<TwoArgumentSimpleFunction> codec = DensityFunctions.doubleFunctionArgumentCodec((densityFunction, densityFunction2) -> TwoArgumentSimpleFunction.create(this, densityFunction, densityFunction2), TwoArgumentSimpleFunction::argument1, TwoArgumentSimpleFunction::argument2);
            private final String name;

            private Type(String string2) {
                this.name = string2;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    public record Spline(CubicSpline<Point, Coordinate> spline) implements DensityFunction
    {
        private static final Codec<CubicSpline<Point, Coordinate>> SPLINE_CODEC = CubicSpline.codec(Coordinate.CODEC);
        private static final MapCodec<Spline> DATA_CODEC = SPLINE_CODEC.fieldOf("spline").xmap(Spline::new, Spline::spline);
        public static final KeyDispatchDataCodec<Spline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.spline.apply(new Point(functionContext));
        }

        @Override
        public double minValue() {
            return this.spline.minValue();
        }

        @Override
        public double maxValue() {
            return this.spline.maxValue();
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new Spline(this.spline.mapAll((I coordinate) -> coordinate.mapAll(visitor))));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }

        public record Point(DensityFunction.FunctionContext context) {
        }

        public record Coordinate(Holder<DensityFunction> function) implements ToFloatFunction<Point>
        {
            public static final Codec<Coordinate> CODEC = DensityFunction.CODEC.xmap(Coordinate::new, Coordinate::function);

            @Override
            public String toString() {
                Optional<ResourceKey<DensityFunction>> optional = this.function.unwrapKey();
                if (optional.isPresent()) {
                    ResourceKey<DensityFunction> resourceKey = optional.get();
                    if (resourceKey == NoiseRouterData.CONTINENTS) {
                        return "continents";
                    }
                    if (resourceKey == NoiseRouterData.EROSION) {
                        return "erosion";
                    }
                    if (resourceKey == NoiseRouterData.RIDGES) {
                        return "weirdness";
                    }
                    if (resourceKey == NoiseRouterData.RIDGES_FOLDED) {
                        return "ridges";
                    }
                }
                return "Coordinate[" + String.valueOf(this.function) + "]";
            }

            @Override
            public float apply(Point point) {
                return (float)this.function.value().compute(point.context());
            }

            @Override
            public float minValue() {
                return this.function.isBound() ? (float)this.function.value().minValue() : Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return this.function.isBound() ? (float)this.function.value().maxValue() : Float.POSITIVE_INFINITY;
            }

            public Coordinate mapAll(DensityFunction.Visitor visitor) {
                return new Coordinate(new Holder.Direct<DensityFunction>(this.function.value().mapAll(visitor)));
            }
        }
    }

    static final class Constant
    extends Record
    implements DensityFunction.SimpleFunction {
        final double value;
        static final KeyDispatchDataCodec<Constant> CODEC = DensityFunctions.singleArgumentCodec(NOISE_VALUE_CODEC, Constant::new, Constant::value);
        static final Constant ZERO = new Constant(0.0);

        Constant(double d) {
            this.value = d;
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.value;
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            Arrays.fill(dArray, this.value);
        }

        @Override
        public double minValue() {
            return this.value;
        }

        @Override
        public double maxValue() {
            return this.value;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Constant.class, "value", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Constant.class, "value", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Constant.class, "value", "value"}, this, object);
        }

        public double value() {
            return this.value;
        }
    }

    record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction
    {
        private static final MapCodec<YClampedGradient> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.intRange((int)(DimensionType.MIN_Y * 2), (int)(DimensionType.MAX_Y * 2)).fieldOf("from_y").forGetter(YClampedGradient::fromY), (App)Codec.intRange((int)(DimensionType.MIN_Y * 2), (int)(DimensionType.MAX_Y * 2)).fieldOf("to_y").forGetter(YClampedGradient::toY), (App)NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(YClampedGradient::fromValue), (App)NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(YClampedGradient::toValue)).apply((Applicative)instance, YClampedGradient::new));
        public static final KeyDispatchDataCodec<YClampedGradient> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return Mth.clampedMap((double)functionContext.blockY(), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
        }

        @Override
        public double minValue() {
            return Math.min(this.fromValue, this.toValue);
        }

        @Override
        public double maxValue() {
            return Math.max(this.fromValue, this.toValue);
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    record Ap2(TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue) implements TwoArgumentSimpleFunction
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            double d = this.argument1.compute(functionContext);
            return switch (this.type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> d + this.argument2.compute(functionContext);
                case 1 -> {
                    if (d == 0.0) {
                        yield 0.0;
                    }
                    yield d * this.argument2.compute(functionContext);
                }
                case 2 -> {
                    if (d < this.argument2.minValue()) {
                        yield d;
                    }
                    yield Math.min(d, this.argument2.compute(functionContext));
                }
                case 3 -> d > this.argument2.maxValue() ? d : Math.max(d, this.argument2.compute(functionContext));
            };
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            this.argument1.fillArray(dArray, contextProvider);
            switch (this.type.ordinal()) {
                case 0: {
                    double[] dArray2 = new double[dArray.length];
                    this.argument2.fillArray(dArray2, contextProvider);
                    for (int i = 0; i < dArray.length; ++i) {
                        dArray[i] = dArray[i] + dArray2[i];
                    }
                    break;
                }
                case 1: {
                    for (int i = 0; i < dArray.length; ++i) {
                        double d = dArray[i];
                        dArray[i] = d == 0.0 ? 0.0 : d * this.argument2.compute(contextProvider.forIndex(i));
                    }
                    break;
                }
                case 2: {
                    double d = this.argument2.minValue();
                    for (int i = 0; i < dArray.length; ++i) {
                        double d2 = dArray[i];
                        dArray[i] = d2 < d ? d2 : Math.min(d2, this.argument2.compute(contextProvider.forIndex(i)));
                    }
                    break;
                }
                case 3: {
                    double d = this.argument2.maxValue();
                    for (int i = 0; i < dArray.length; ++i) {
                        double d3 = dArray[i];
                        dArray[i] = d3 > d ? d3 : Math.max(d3, this.argument2.compute(contextProvider.forIndex(i)));
                    }
                    break;
                }
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(TwoArgumentSimpleFunction.create(this.type, this.argument1.mapAll(visitor), this.argument2.mapAll(visitor)));
        }
    }

    record MulOrAdd(Type specificType, DensityFunction input, double minValue, double maxValue, double argument) implements PureTransformer,
    TwoArgumentSimpleFunction
    {
        @Override
        public TwoArgumentSimpleFunction.Type type() {
            return this.specificType == Type.MUL ? TwoArgumentSimpleFunction.Type.MUL : TwoArgumentSimpleFunction.Type.ADD;
        }

        @Override
        public DensityFunction argument1() {
            return DensityFunctions.constant(this.argument);
        }

        @Override
        public DensityFunction argument2() {
            return this.input;
        }

        @Override
        public double transform(double d) {
            return switch (this.specificType.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> d * this.argument;
                case 1 -> d + this.argument;
            };
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            double d;
            double d2;
            DensityFunction densityFunction = this.input.mapAll(visitor);
            double d3 = densityFunction.minValue();
            double d4 = densityFunction.maxValue();
            if (this.specificType == Type.ADD) {
                d2 = d3 + this.argument;
                d = d4 + this.argument;
            } else if (this.argument >= 0.0) {
                d2 = d3 * this.argument;
                d = d4 * this.argument;
            } else {
                d2 = d4 * this.argument;
                d = d3 * this.argument;
            }
            return new MulOrAdd(this.specificType, densityFunction, d2, d, this.argument);
        }

        static enum Type {
            MUL,
            ADD;

        }
    }

    static interface ShiftNoise
    extends DensityFunction {
        public DensityFunction.NoiseHolder offsetNoise();

        @Override
        default public double minValue() {
            return -this.maxValue();
        }

        @Override
        default public double maxValue() {
            return this.offsetNoise().maxValue() * 4.0;
        }

        default public double compute(double d, double d2, double d3) {
            return this.offsetNoise().getValue(d * 0.25, d2 * 0.25, d3 * 0.25) * 4.0;
        }

        @Override
        default public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }
    }

    public static interface MarkerOrMarked
    extends DensityFunction {
        public Marker.Type type();

        public DensityFunction wrapped();

        @Override
        default public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return this.type().codec;
        }

        @Override
        default public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new Marker(this.type(), this.wrapped().mapAll(visitor)));
        }
    }

    @VisibleForDebug
    public record HolderHolder(Holder<DensityFunction> function) implements DensityFunction
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.function.value().compute(functionContext);
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            this.function.value().fillArray(dArray, contextProvider);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new HolderHolder(new Holder.Direct<DensityFunction>(this.function.value().mapAll(visitor))));
        }

        @Override
        public double minValue() {
            return this.function.isBound() ? this.function.value().minValue() : Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return this.function.isBound() ? this.function.value().maxValue() : Double.POSITIVE_INFINITY;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
        }
    }

    public static interface BeardifierOrMarker
    extends DensityFunction.SimpleFunction {
        public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)BeardifierMarker.INSTANCE));

        @Override
        default public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    static interface PureTransformer
    extends DensityFunction {
        public DensityFunction input();

        @Override
        default public double compute(DensityFunction.FunctionContext functionContext) {
            return this.transform(this.input().compute(functionContext));
        }

        @Override
        default public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            this.input().fillArray(dArray, contextProvider);
            for (int i = 0; i < dArray.length; ++i) {
                dArray[i] = this.transform(dArray[i]);
            }
        }

        public double transform(double var1);
    }

    static interface TransformerWithContext
    extends DensityFunction {
        public DensityFunction input();

        @Override
        default public double compute(DensityFunction.FunctionContext functionContext) {
            return this.transform(functionContext, this.input().compute(functionContext));
        }

        @Override
        default public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            this.input().fillArray(dArray, contextProvider);
            for (int i = 0; i < dArray.length; ++i) {
                dArray[i] = this.transform(contextProvider.forIndex(i), dArray[i]);
            }
        }

        public double transform(DensityFunction.FunctionContext var1, double var2);
    }
}

