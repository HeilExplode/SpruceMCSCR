/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P5
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public abstract class StructurePlacement {
    public static final Codec<StructurePlacement> CODEC = BuiltInRegistries.STRUCTURE_PLACEMENT.byNameCodec().dispatch(StructurePlacement::type, StructurePlacementType::codec);
    private static final int HIGHLY_ARBITRARY_RANDOM_SALT = 10387320;
    private final Vec3i locateOffset;
    private final FrequencyReductionMethod frequencyReductionMethod;
    private final float frequency;
    private final int salt;
    private final Optional<ExclusionZone> exclusionZone;

    protected static <S extends StructurePlacement> Products.P5<RecordCodecBuilder.Mu<S>, Vec3i, FrequencyReductionMethod, Float, Integer, Optional<ExclusionZone>> placementCodec(RecordCodecBuilder.Instance<S> instance) {
        return instance.group((App)Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", (Object)Vec3i.ZERO).forGetter(StructurePlacement::locateOffset), (App)FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", (Object)FrequencyReductionMethod.DEFAULT).forGetter(StructurePlacement::frequencyReductionMethod), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("frequency", (Object)Float.valueOf(1.0f)).forGetter(StructurePlacement::frequency), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(StructurePlacement::salt), (App)ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StructurePlacement::exclusionZone));
    }

    protected StructurePlacement(Vec3i vec3i, FrequencyReductionMethod frequencyReductionMethod, float f, int n, Optional<ExclusionZone> optional) {
        this.locateOffset = vec3i;
        this.frequencyReductionMethod = frequencyReductionMethod;
        this.frequency = f;
        this.salt = n;
        this.exclusionZone = optional;
    }

    protected Vec3i locateOffset() {
        return this.locateOffset;
    }

    protected FrequencyReductionMethod frequencyReductionMethod() {
        return this.frequencyReductionMethod;
    }

    protected float frequency() {
        return this.frequency;
    }

    protected int salt() {
        return this.salt;
    }

    protected Optional<ExclusionZone> exclusionZone() {
        return this.exclusionZone;
    }

    public boolean isStructureChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int n, int n2) {
        return this.isPlacementChunk(chunkGeneratorStructureState, n, n2) && this.applyAdditionalChunkRestrictions(n, n2, chunkGeneratorStructureState.getLevelSeed()) && this.applyInteractionsWithOtherStructures(chunkGeneratorStructureState, n, n2);
    }

    public boolean applyAdditionalChunkRestrictions(int n, int n2, long l) {
        return !(this.frequency < 1.0f) || this.frequencyReductionMethod.shouldGenerate(l, this.salt, n, n2, this.frequency);
    }

    public boolean applyInteractionsWithOtherStructures(ChunkGeneratorStructureState chunkGeneratorStructureState, int n, int n2) {
        return !this.exclusionZone.isPresent() || !this.exclusionZone.get().isPlacementForbidden(chunkGeneratorStructureState, n, n2);
    }

    protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState var1, int var2, int var3);

    public BlockPos getLocatePos(ChunkPos chunkPos) {
        return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()).offset(this.locateOffset());
    }

    public abstract StructurePlacementType<?> type();

    private static boolean probabilityReducer(long l, int n, int n2, int n3, float f) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(l, n, n2, n3);
        return worldgenRandom.nextFloat() < f;
    }

    private static boolean legacyProbabilityReducerWithDouble(long l, int n, int n2, int n3, float f) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(l, n2, n3);
        return worldgenRandom.nextDouble() < (double)f;
    }

    private static boolean legacyArbitrarySaltProbabilityReducer(long l, int n, int n2, int n3, float f) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(l, n2, n3, 10387320);
        return worldgenRandom.nextFloat() < f;
    }

    private static boolean legacyPillagerOutpostReducer(long l, int n, int n2, int n3, float f) {
        int n4 = n2 >> 4;
        int n5 = n3 >> 4;
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setSeed((long)(n4 ^ n5 << 4) ^ l);
        worldgenRandom.nextInt();
        return worldgenRandom.nextInt((int)(1.0f / f)) == 0;
    }

    public static enum FrequencyReductionMethod implements StringRepresentable
    {
        DEFAULT("default", StructurePlacement::probabilityReducer),
        LEGACY_TYPE_1("legacy_type_1", StructurePlacement::legacyPillagerOutpostReducer),
        LEGACY_TYPE_2("legacy_type_2", StructurePlacement::legacyArbitrarySaltProbabilityReducer),
        LEGACY_TYPE_3("legacy_type_3", StructurePlacement::legacyProbabilityReducerWithDouble);

        public static final Codec<FrequencyReductionMethod> CODEC;
        private final String name;
        private final FrequencyReducer reducer;

        private FrequencyReductionMethod(String string2, FrequencyReducer frequencyReducer) {
            this.name = string2;
            this.reducer = frequencyReducer;
        }

        public boolean shouldGenerate(long l, int n, int n2, int n3, float f) {
            return this.reducer.shouldGenerate(l, n, n2, n3, f);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(FrequencyReductionMethod::values);
        }
    }

    @Deprecated
    public record ExclusionZone(Holder<StructureSet> otherSet, int chunkCount) {
        public static final Codec<ExclusionZone> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryFileCodec.create(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, false).fieldOf("other_set").forGetter(ExclusionZone::otherSet), (App)Codec.intRange((int)1, (int)16).fieldOf("chunk_count").forGetter(ExclusionZone::chunkCount)).apply((Applicative)instance, ExclusionZone::new));

        boolean isPlacementForbidden(ChunkGeneratorStructureState chunkGeneratorStructureState, int n, int n2) {
            return chunkGeneratorStructureState.hasStructureChunkInRange(this.otherSet, n, n2, this.chunkCount);
        }
    }

    @FunctionalInterface
    public static interface FrequencyReducer {
        public boolean shouldGenerate(long var1, int var3, int var4, int var5, float var6);
    }
}

