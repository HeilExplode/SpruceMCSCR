/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRules {
    public static final ConditionSource ON_FLOOR = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
    public static final ConditionSource UNDER_FLOOR = SurfaceRules.stoneDepthCheck(0, true, CaveSurface.FLOOR);
    public static final ConditionSource DEEP_UNDER_FLOOR = SurfaceRules.stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
    public static final ConditionSource VERY_DEEP_UNDER_FLOOR = SurfaceRules.stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
    public static final ConditionSource ON_CEILING = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.CEILING);
    public static final ConditionSource UNDER_CEILING = SurfaceRules.stoneDepthCheck(0, true, CaveSurface.CEILING);

    public static ConditionSource stoneDepthCheck(int n, boolean bl, CaveSurface caveSurface) {
        return new StoneDepthCheck(n, bl, 0, caveSurface);
    }

    public static ConditionSource stoneDepthCheck(int n, boolean bl, int n2, CaveSurface caveSurface) {
        return new StoneDepthCheck(n, bl, n2, caveSurface);
    }

    public static ConditionSource not(ConditionSource conditionSource) {
        return new NotConditionSource(conditionSource);
    }

    public static ConditionSource yBlockCheck(VerticalAnchor verticalAnchor, int n) {
        return new YConditionSource(verticalAnchor, n, false);
    }

    public static ConditionSource yStartCheck(VerticalAnchor verticalAnchor, int n) {
        return new YConditionSource(verticalAnchor, n, true);
    }

    public static ConditionSource waterBlockCheck(int n, int n2) {
        return new WaterConditionSource(n, n2, false);
    }

    public static ConditionSource waterStartCheck(int n, int n2) {
        return new WaterConditionSource(n, n2, true);
    }

    @SafeVarargs
    public static ConditionSource isBiome(ResourceKey<Biome> ... resourceKeyArray) {
        return SurfaceRules.isBiome(List.of(resourceKeyArray));
    }

    private static BiomeConditionSource isBiome(List<ResourceKey<Biome>> list) {
        return new BiomeConditionSource(list);
    }

    public static ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d) {
        return SurfaceRules.noiseCondition(resourceKey, d, Double.MAX_VALUE);
    }

    public static ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d, double d2) {
        return new NoiseThresholdConditionSource(resourceKey, d, d2);
    }

    public static ConditionSource verticalGradient(String string, VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        return new VerticalGradientConditionSource(ResourceLocation.parse(string), verticalAnchor, verticalAnchor2);
    }

    public static ConditionSource steep() {
        return Steep.INSTANCE;
    }

    public static ConditionSource hole() {
        return Hole.INSTANCE;
    }

    public static ConditionSource abovePreliminarySurface() {
        return AbovePreliminarySurface.INSTANCE;
    }

    public static ConditionSource temperature() {
        return Temperature.INSTANCE;
    }

    public static RuleSource ifTrue(ConditionSource conditionSource, RuleSource ruleSource) {
        return new TestRuleSource(conditionSource, ruleSource);
    }

    public static RuleSource sequence(RuleSource ... ruleSourceArray) {
        if (ruleSourceArray.length == 0) {
            throw new IllegalArgumentException("Need at least 1 rule for a sequence");
        }
        return new SequenceRuleSource(Arrays.asList(ruleSourceArray));
    }

    public static RuleSource state(BlockState blockState) {
        return new BlockRuleSource(blockState);
    }

    public static RuleSource bandlands() {
        return Bandlands.INSTANCE;
    }

    static <A> MapCodec<? extends A> register(Registry<MapCodec<? extends A>> registry, String string, KeyDispatchDataCodec<? extends A> keyDispatchDataCodec) {
        return Registry.register(registry, string, keyDispatchDataCodec.codec());
    }

    static final class StoneDepthCheck
    extends Record
    implements ConditionSource {
        final int offset;
        final boolean addSurfaceDepth;
        final int secondaryDepthRange;
        private final CaveSurface surfaceType;
        static final KeyDispatchDataCodec<StoneDepthCheck> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("offset").forGetter(StoneDepthCheck::offset), (App)Codec.BOOL.fieldOf("add_surface_depth").forGetter(StoneDepthCheck::addSurfaceDepth), (App)Codec.INT.fieldOf("secondary_depth_range").forGetter(StoneDepthCheck::secondaryDepthRange), (App)CaveSurface.CODEC.fieldOf("surface_type").forGetter(StoneDepthCheck::surfaceType)).apply((Applicative)instance, StoneDepthCheck::new)));

        StoneDepthCheck(int n, boolean bl, int n2, CaveSurface caveSurface) {
            this.offset = n;
            this.addSurfaceDepth = bl;
            this.secondaryDepthRange = n2;
            this.surfaceType = caveSurface;
        }

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            final boolean bl = this.surfaceType == CaveSurface.CEILING;
            class StoneDepthCondition
            extends LazyYCondition {
                StoneDepthCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    int n = bl ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
                    int n2 = StoneDepthCheck.this.addSurfaceDepth ? this.context.surfaceDepth : 0;
                    int n3 = StoneDepthCheck.this.secondaryDepthRange == 0 ? 0 : (int)Mth.map(this.context.getSurfaceSecondary(), -1.0, 1.0, 0.0, (double)StoneDepthCheck.this.secondaryDepthRange);
                    return n <= 1 + StoneDepthCheck.this.offset + n2 + n3;
                }
            }
            return new StoneDepthCondition();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{StoneDepthCheck.class, "offset;addSurfaceDepth;secondaryDepthRange;surfaceType", "offset", "addSurfaceDepth", "secondaryDepthRange", "surfaceType"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StoneDepthCheck.class, "offset;addSurfaceDepth;secondaryDepthRange;surfaceType", "offset", "addSurfaceDepth", "secondaryDepthRange", "surfaceType"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StoneDepthCheck.class, "offset;addSurfaceDepth;secondaryDepthRange;surfaceType", "offset", "addSurfaceDepth", "secondaryDepthRange", "surfaceType"}, this, object);
        }

        public int offset() {
            return this.offset;
        }

        public boolean addSurfaceDepth() {
            return this.addSurfaceDepth;
        }

        public int secondaryDepthRange() {
            return this.secondaryDepthRange;
        }

        public CaveSurface surfaceType() {
            return this.surfaceType;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record NotConditionSource(ConditionSource target) implements ConditionSource
    {
        static final KeyDispatchDataCodec<NotConditionSource> CODEC = KeyDispatchDataCodec.of(ConditionSource.CODEC.xmap(NotConditionSource::new, NotConditionSource::target).fieldOf("invert"));

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return new NotCondition((Condition)this.target.apply(context));
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    public static interface ConditionSource
    extends Function<Context, Condition> {
        public static final Codec<ConditionSource> CODEC = BuiltInRegistries.MATERIAL_CONDITION.byNameCodec().dispatch(conditionSource -> conditionSource.codec().codec(), Function.identity());

        public static MapCodec<? extends ConditionSource> bootstrap(Registry<MapCodec<? extends ConditionSource>> registry) {
            SurfaceRules.register(registry, "biome", BiomeConditionSource.CODEC);
            SurfaceRules.register(registry, "noise_threshold", NoiseThresholdConditionSource.CODEC);
            SurfaceRules.register(registry, "vertical_gradient", VerticalGradientConditionSource.CODEC);
            SurfaceRules.register(registry, "y_above", YConditionSource.CODEC);
            SurfaceRules.register(registry, "water", WaterConditionSource.CODEC);
            SurfaceRules.register(registry, "temperature", Temperature.CODEC);
            SurfaceRules.register(registry, "steep", Steep.CODEC);
            SurfaceRules.register(registry, "not", NotConditionSource.CODEC);
            SurfaceRules.register(registry, "hole", Hole.CODEC);
            SurfaceRules.register(registry, "above_preliminary_surface", AbovePreliminarySurface.CODEC);
            return SurfaceRules.register(registry, "stone_depth", StoneDepthCheck.CODEC);
        }

        public KeyDispatchDataCodec<? extends ConditionSource> codec();
    }

    static final class YConditionSource
    extends Record
    implements ConditionSource {
        final VerticalAnchor anchor;
        final int surfaceDepthMultiplier;
        final boolean addStoneDepth;
        static final KeyDispatchDataCodec<YConditionSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group((App)VerticalAnchor.CODEC.fieldOf("anchor").forGetter(YConditionSource::anchor), (App)Codec.intRange((int)-20, (int)20).fieldOf("surface_depth_multiplier").forGetter(YConditionSource::surfaceDepthMultiplier), (App)Codec.BOOL.fieldOf("add_stone_depth").forGetter(YConditionSource::addStoneDepth)).apply((Applicative)instance, YConditionSource::new)));

        YConditionSource(VerticalAnchor verticalAnchor, int n, boolean bl) {
            this.anchor = verticalAnchor;
            this.surfaceDepthMultiplier = n;
            this.addStoneDepth = bl;
        }

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            class YCondition
            extends LazyYCondition {
                YCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    return this.context.blockY + (YConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= YConditionSource.this.anchor.resolveY(this.context.context) + this.context.surfaceDepth * YConditionSource.this.surfaceDepthMultiplier;
                }
            }
            return new YCondition();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{YConditionSource.class, "anchor;surfaceDepthMultiplier;addStoneDepth", "anchor", "surfaceDepthMultiplier", "addStoneDepth"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{YConditionSource.class, "anchor;surfaceDepthMultiplier;addStoneDepth", "anchor", "surfaceDepthMultiplier", "addStoneDepth"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{YConditionSource.class, "anchor;surfaceDepthMultiplier;addStoneDepth", "anchor", "surfaceDepthMultiplier", "addStoneDepth"}, this, object);
        }

        public VerticalAnchor anchor() {
            return this.anchor;
        }

        public int surfaceDepthMultiplier() {
            return this.surfaceDepthMultiplier;
        }

        public boolean addStoneDepth() {
            return this.addStoneDepth;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    static final class WaterConditionSource
    extends Record
    implements ConditionSource {
        final int offset;
        final int surfaceDepthMultiplier;
        final boolean addStoneDepth;
        static final KeyDispatchDataCodec<WaterConditionSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("offset").forGetter(WaterConditionSource::offset), (App)Codec.intRange((int)-20, (int)20).fieldOf("surface_depth_multiplier").forGetter(WaterConditionSource::surfaceDepthMultiplier), (App)Codec.BOOL.fieldOf("add_stone_depth").forGetter(WaterConditionSource::addStoneDepth)).apply((Applicative)instance, WaterConditionSource::new)));

        WaterConditionSource(int n, int n2, boolean bl) {
            this.offset = n;
            this.surfaceDepthMultiplier = n2;
            this.addStoneDepth = bl;
        }

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            class WaterCondition
            extends LazyYCondition {
                WaterCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    return this.context.waterHeight == Integer.MIN_VALUE || this.context.blockY + (WaterConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= this.context.waterHeight + WaterConditionSource.this.offset + this.context.surfaceDepth * WaterConditionSource.this.surfaceDepthMultiplier;
                }
            }
            return new WaterCondition();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{WaterConditionSource.class, "offset;surfaceDepthMultiplier;addStoneDepth", "offset", "surfaceDepthMultiplier", "addStoneDepth"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{WaterConditionSource.class, "offset;surfaceDepthMultiplier;addStoneDepth", "offset", "surfaceDepthMultiplier", "addStoneDepth"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{WaterConditionSource.class, "offset;surfaceDepthMultiplier;addStoneDepth", "offset", "surfaceDepthMultiplier", "addStoneDepth"}, this, object);
        }

        public int offset() {
            return this.offset;
        }

        public int surfaceDepthMultiplier() {
            return this.surfaceDepthMultiplier;
        }

        public boolean addStoneDepth() {
            return this.addStoneDepth;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    static final class BiomeConditionSource
    implements ConditionSource {
        static final KeyDispatchDataCodec<BiomeConditionSource> CODEC = KeyDispatchDataCodec.of(ResourceKey.codec(Registries.BIOME).listOf().fieldOf("biome_is").xmap(SurfaceRules::isBiome, biomeConditionSource -> biomeConditionSource.biomes));
        private final List<ResourceKey<Biome>> biomes;
        final Predicate<ResourceKey<Biome>> biomeNameTest;

        BiomeConditionSource(List<ResourceKey<Biome>> list) {
            this.biomes = list;
            this.biomeNameTest = Set.copyOf(list)::contains;
        }

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            class BiomeCondition
            extends LazyYCondition {
                BiomeCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    return this.context.biome.get().is(BiomeConditionSource.this.biomeNameTest);
                }
            }
            return new BiomeCondition();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof BiomeConditionSource) {
                BiomeConditionSource biomeConditionSource = (BiomeConditionSource)object;
                return this.biomes.equals(biomeConditionSource.biomes);
            }
            return false;
        }

        public int hashCode() {
            return this.biomes.hashCode();
        }

        public String toString() {
            return "BiomeConditionSource[biomes=" + String.valueOf(this.biomes) + "]";
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    static final class NoiseThresholdConditionSource
    extends Record
    implements ConditionSource {
        private final ResourceKey<NormalNoise.NoiseParameters> noise;
        final double minThreshold;
        final double maxThreshold;
        static final KeyDispatchDataCodec<NoiseThresholdConditionSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(NoiseThresholdConditionSource::noise), (App)Codec.DOUBLE.fieldOf("min_threshold").forGetter(NoiseThresholdConditionSource::minThreshold), (App)Codec.DOUBLE.fieldOf("max_threshold").forGetter(NoiseThresholdConditionSource::maxThreshold)).apply((Applicative)instance, NoiseThresholdConditionSource::new)));

        NoiseThresholdConditionSource(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d, double d2) {
            this.noise = resourceKey;
            this.minThreshold = d;
            this.maxThreshold = d2;
        }

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            final NormalNoise normalNoise = context.randomState.getOrCreateNoise(this.noise);
            class NoiseThresholdCondition
            extends LazyXZCondition {
                NoiseThresholdCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    double d = normalNoise.getValue(this.context.blockX, 0.0, this.context.blockZ);
                    return d >= NoiseThresholdConditionSource.this.minThreshold && d <= NoiseThresholdConditionSource.this.maxThreshold;
                }
            }
            return new NoiseThresholdCondition();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NoiseThresholdConditionSource.class, "noise;minThreshold;maxThreshold", "noise", "minThreshold", "maxThreshold"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NoiseThresholdConditionSource.class, "noise;minThreshold;maxThreshold", "noise", "minThreshold", "maxThreshold"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NoiseThresholdConditionSource.class, "noise;minThreshold;maxThreshold", "noise", "minThreshold", "maxThreshold"}, this, object);
        }

        public ResourceKey<NormalNoise.NoiseParameters> noise() {
            return this.noise;
        }

        public double minThreshold() {
            return this.minThreshold;
        }

        public double maxThreshold() {
            return this.maxThreshold;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record VerticalGradientConditionSource(ResourceLocation randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove) implements ConditionSource
    {
        static final KeyDispatchDataCodec<VerticalGradientConditionSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("random_name").forGetter(VerticalGradientConditionSource::randomName), (App)VerticalAnchor.CODEC.fieldOf("true_at_and_below").forGetter(VerticalGradientConditionSource::trueAtAndBelow), (App)VerticalAnchor.CODEC.fieldOf("false_at_and_above").forGetter(VerticalGradientConditionSource::falseAtAndAbove)).apply((Applicative)instance, VerticalGradientConditionSource::new)));

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            final int n = this.trueAtAndBelow().resolveY(context.context);
            final int n2 = this.falseAtAndAbove().resolveY(context.context);
            final PositionalRandomFactory positionalRandomFactory = context.randomState.getOrCreateRandomFactory(this.randomName());
            class VerticalGradientCondition
            extends LazyYCondition {
                VerticalGradientCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    int n3 = this.context.blockY;
                    if (n3 <= n) {
                        return true;
                    }
                    if (n3 >= n2) {
                        return false;
                    }
                    double d = Mth.map((double)n3, (double)n, (double)n2, 1.0, 0.0);
                    RandomSource randomSource = positionalRandomFactory.at(this.context.blockX, n3, this.context.blockZ);
                    return (double)randomSource.nextFloat() < d;
                }
            }
            return new VerticalGradientCondition();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    static enum Steep implements ConditionSource
    {
        INSTANCE;

        static final KeyDispatchDataCodec<Steep> CODEC;

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.steep;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)INSTANCE));
        }
    }

    static enum Hole implements ConditionSource
    {
        INSTANCE;

        static final KeyDispatchDataCodec<Hole> CODEC;

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.hole;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)INSTANCE));
        }
    }

    static enum AbovePreliminarySurface implements ConditionSource
    {
        INSTANCE;

        static final KeyDispatchDataCodec<AbovePreliminarySurface> CODEC;

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.abovePreliminarySurface;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)INSTANCE));
        }
    }

    static enum Temperature implements ConditionSource
    {
        INSTANCE;

        static final KeyDispatchDataCodec<Temperature> CODEC;

        @Override
        public KeyDispatchDataCodec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.temperature;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)INSTANCE));
        }
    }

    record TestRuleSource(ConditionSource ifTrue, RuleSource thenRun) implements RuleSource
    {
        static final KeyDispatchDataCodec<TestRuleSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group((App)ConditionSource.CODEC.fieldOf("if_true").forGetter(TestRuleSource::ifTrue), (App)RuleSource.CODEC.fieldOf("then_run").forGetter(TestRuleSource::thenRun)).apply((Applicative)instance, TestRuleSource::new)));

        @Override
        public KeyDispatchDataCodec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return new TestRule((Condition)this.ifTrue.apply(context), (SurfaceRule)this.thenRun.apply(context));
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    public static interface RuleSource
    extends Function<Context, SurfaceRule> {
        public static final Codec<RuleSource> CODEC = BuiltInRegistries.MATERIAL_RULE.byNameCodec().dispatch(ruleSource -> ruleSource.codec().codec(), Function.identity());

        public static MapCodec<? extends RuleSource> bootstrap(Registry<MapCodec<? extends RuleSource>> registry) {
            SurfaceRules.register(registry, "bandlands", Bandlands.CODEC);
            SurfaceRules.register(registry, "block", BlockRuleSource.CODEC);
            SurfaceRules.register(registry, "sequence", SequenceRuleSource.CODEC);
            return SurfaceRules.register(registry, "condition", TestRuleSource.CODEC);
        }

        public KeyDispatchDataCodec<? extends RuleSource> codec();
    }

    record SequenceRuleSource(List<RuleSource> sequence) implements RuleSource
    {
        static final KeyDispatchDataCodec<SequenceRuleSource> CODEC = KeyDispatchDataCodec.of(RuleSource.CODEC.listOf().xmap(SequenceRuleSource::new, SequenceRuleSource::sequence).fieldOf("sequence"));

        @Override
        public KeyDispatchDataCodec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            if (this.sequence.size() == 1) {
                return (SurfaceRule)this.sequence.get(0).apply(context);
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            for (RuleSource ruleSource : this.sequence) {
                builder.add((Object)((SurfaceRule)ruleSource.apply(context)));
            }
            return new SequenceRule((List<SurfaceRule>)builder.build());
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record BlockRuleSource(BlockState resultState, StateRule rule) implements RuleSource
    {
        static final KeyDispatchDataCodec<BlockRuleSource> CODEC = KeyDispatchDataCodec.of(BlockState.CODEC.xmap(BlockRuleSource::new, BlockRuleSource::resultState).fieldOf("result_state"));

        BlockRuleSource(BlockState blockState) {
            this(blockState, new StateRule(blockState));
        }

        @Override
        public KeyDispatchDataCodec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return this.rule;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    static enum Bandlands implements RuleSource
    {
        INSTANCE;

        static final KeyDispatchDataCodec<Bandlands> CODEC;

        @Override
        public KeyDispatchDataCodec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return context.system::getBand;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = KeyDispatchDataCodec.of(MapCodec.unit((Object)INSTANCE));
        }
    }

    record SequenceRule(List<SurfaceRule> rules) implements SurfaceRule
    {
        @Override
        @Nullable
        public BlockState tryApply(int n, int n2, int n3) {
            for (SurfaceRule surfaceRule : this.rules) {
                BlockState blockState = surfaceRule.tryApply(n, n2, n3);
                if (blockState == null) continue;
                return blockState;
            }
            return null;
        }
    }

    record TestRule(Condition condition, SurfaceRule followup) implements SurfaceRule
    {
        @Override
        @Nullable
        public BlockState tryApply(int n, int n2, int n3) {
            if (!this.condition.test()) {
                return null;
            }
            return this.followup.tryApply(n, n2, n3);
        }
    }

    record StateRule(BlockState state) implements SurfaceRule
    {
        @Override
        public BlockState tryApply(int n, int n2, int n3) {
            return this.state;
        }
    }

    protected static interface SurfaceRule {
        @Nullable
        public BlockState tryApply(int var1, int var2, int var3);
    }

    record NotCondition(Condition target) implements Condition
    {
        @Override
        public boolean test() {
            return !this.target.test();
        }
    }

    static abstract class LazyYCondition
    extends LazyCondition {
        protected LazyYCondition(Context context) {
            super(context);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateY;
        }
    }

    static abstract class LazyXZCondition
    extends LazyCondition {
        protected LazyXZCondition(Context context) {
            super(context);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateXZ;
        }
    }

    static abstract class LazyCondition
    implements Condition {
        protected final Context context;
        private long lastUpdate;
        @Nullable
        Boolean result;

        protected LazyCondition(Context context) {
            this.context = context;
            this.lastUpdate = this.getContextLastUpdate() - 1L;
        }

        @Override
        public boolean test() {
            long l = this.getContextLastUpdate();
            if (l == this.lastUpdate) {
                if (this.result == null) {
                    throw new IllegalStateException("Update triggered but the result is null");
                }
                return this.result;
            }
            this.lastUpdate = l;
            this.result = this.compute();
            return this.result;
        }

        protected abstract long getContextLastUpdate();

        protected abstract boolean compute();
    }

    static interface Condition {
        public boolean test();
    }

    protected static final class Context {
        private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
        private static final int SURFACE_CELL_BITS = 4;
        private static final int SURFACE_CELL_SIZE = 16;
        private static final int SURFACE_CELL_MASK = 15;
        final SurfaceSystem system;
        final Condition temperature = new TemperatureHelperCondition(this);
        final Condition steep = new SteepMaterialCondition(this);
        final Condition hole = new HoleCondition(this);
        final Condition abovePreliminarySurface = new AbovePreliminarySurfaceCondition();
        final RandomState randomState;
        final ChunkAccess chunk;
        private final NoiseChunk noiseChunk;
        private final Function<BlockPos, Holder<Biome>> biomeGetter;
        final WorldGenerationContext context;
        private long lastPreliminarySurfaceCellOrigin = Long.MAX_VALUE;
        private final int[] preliminarySurfaceCache = new int[4];
        long lastUpdateXZ = -9223372036854775807L;
        int blockX;
        int blockZ;
        int surfaceDepth;
        private long lastSurfaceDepth2Update = this.lastUpdateXZ - 1L;
        private double surfaceSecondary;
        private long lastMinSurfaceLevelUpdate = this.lastUpdateXZ - 1L;
        private int minSurfaceLevel;
        long lastUpdateY = -9223372036854775807L;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Supplier<Holder<Biome>> biome;
        int blockY;
        int waterHeight;
        int stoneDepthBelow;
        int stoneDepthAbove;

        protected Context(SurfaceSystem surfaceSystem, RandomState randomState, ChunkAccess chunkAccess, NoiseChunk noiseChunk, Function<BlockPos, Holder<Biome>> function, Registry<Biome> registry, WorldGenerationContext worldGenerationContext) {
            this.system = surfaceSystem;
            this.randomState = randomState;
            this.chunk = chunkAccess;
            this.noiseChunk = noiseChunk;
            this.biomeGetter = function;
            this.context = worldGenerationContext;
        }

        protected void updateXZ(int n, int n2) {
            ++this.lastUpdateXZ;
            ++this.lastUpdateY;
            this.blockX = n;
            this.blockZ = n2;
            this.surfaceDepth = this.system.getSurfaceDepth(n, n2);
        }

        protected void updateY(int n, int n2, int n3, int n4, int n5, int n6) {
            ++this.lastUpdateY;
            this.biome = Suppliers.memoize(() -> this.biomeGetter.apply(this.pos.set(n4, n5, n6)));
            this.blockY = n5;
            this.waterHeight = n3;
            this.stoneDepthBelow = n2;
            this.stoneDepthAbove = n;
        }

        protected double getSurfaceSecondary() {
            if (this.lastSurfaceDepth2Update != this.lastUpdateXZ) {
                this.lastSurfaceDepth2Update = this.lastUpdateXZ;
                this.surfaceSecondary = this.system.getSurfaceSecondary(this.blockX, this.blockZ);
            }
            return this.surfaceSecondary;
        }

        private static int blockCoordToSurfaceCell(int n) {
            return n >> 4;
        }

        private static int surfaceCellToBlockCoord(int n) {
            return n << 4;
        }

        protected int getMinSurfaceLevel() {
            if (this.lastMinSurfaceLevelUpdate != this.lastUpdateXZ) {
                int n;
                this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ;
                int n2 = Context.blockCoordToSurfaceCell(this.blockX);
                long l = ChunkPos.asLong(n2, n = Context.blockCoordToSurfaceCell(this.blockZ));
                if (this.lastPreliminarySurfaceCellOrigin != l) {
                    this.lastPreliminarySurfaceCellOrigin = l;
                    this.preliminarySurfaceCache[0] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(n2), Context.surfaceCellToBlockCoord(n));
                    this.preliminarySurfaceCache[1] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(n2 + 1), Context.surfaceCellToBlockCoord(n));
                    this.preliminarySurfaceCache[2] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(n2), Context.surfaceCellToBlockCoord(n + 1));
                    this.preliminarySurfaceCache[3] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(n2 + 1), Context.surfaceCellToBlockCoord(n + 1));
                }
                int n3 = Mth.floor(Mth.lerp2((float)(this.blockX & 0xF) / 16.0f, (float)(this.blockZ & 0xF) / 16.0f, this.preliminarySurfaceCache[0], this.preliminarySurfaceCache[1], this.preliminarySurfaceCache[2], this.preliminarySurfaceCache[3]));
                this.minSurfaceLevel = n3 + this.surfaceDepth - 8;
            }
            return this.minSurfaceLevel;
        }

        static class TemperatureHelperCondition
        extends LazyYCondition {
            TemperatureHelperCondition(Context context) {
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.context.biome.get().value().coldEnoughToSnow(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ));
            }
        }

        static class SteepMaterialCondition
        extends LazyXZCondition {
            SteepMaterialCondition(Context context) {
                super(context);
            }

            @Override
            protected boolean compute() {
                int n;
                int n2 = this.context.blockX & 0xF;
                int n3 = this.context.blockZ & 0xF;
                int n4 = Math.max(n3 - 1, 0);
                int n5 = Math.min(n3 + 1, 15);
                ChunkAccess chunkAccess = this.context.chunk;
                int n6 = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, n2, n4);
                int n7 = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, n2, n5);
                if (n7 >= n6 + 4) {
                    return true;
                }
                int n8 = Math.max(n2 - 1, 0);
                int n9 = Math.min(n2 + 1, 15);
                int n10 = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, n8, n3);
                return n10 >= (n = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, n9, n3)) + 4;
            }
        }

        static final class HoleCondition
        extends LazyXZCondition {
            HoleCondition(Context context) {
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.context.surfaceDepth <= 0;
            }
        }

        final class AbovePreliminarySurfaceCondition
        implements Condition {
            AbovePreliminarySurfaceCondition() {
            }

            @Override
            public boolean test() {
                return Context.this.blockY >= Context.this.getMinSurfaceLevel();
            }
        }
    }
}

