/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class RandomSpreadStructurePlacement
extends StructurePlacement {
    public static final MapCodec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> RandomSpreadStructurePlacement.placementCodec(instance).and(instance.group((App)Codec.intRange((int)0, (int)4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing), (App)Codec.intRange((int)0, (int)4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation), (App)RandomSpreadType.CODEC.optionalFieldOf("spread_type", (Object)RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType))).apply((Applicative)instance, RandomSpreadStructurePlacement::new)).validate(RandomSpreadStructurePlacement::validate);
    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;

    private static DataResult<RandomSpreadStructurePlacement> validate(RandomSpreadStructurePlacement randomSpreadStructurePlacement) {
        if (randomSpreadStructurePlacement.spacing <= randomSpreadStructurePlacement.separation) {
            return DataResult.error(() -> "Spacing has to be larger than separation");
        }
        return DataResult.success((Object)randomSpreadStructurePlacement);
    }

    public RandomSpreadStructurePlacement(Vec3i vec3i, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float f, int n, Optional<StructurePlacement.ExclusionZone> optional, int n2, int n3, RandomSpreadType randomSpreadType) {
        super(vec3i, frequencyReductionMethod, f, n, optional);
        this.spacing = n2;
        this.separation = n3;
        this.spreadType = randomSpreadType;
    }

    public RandomSpreadStructurePlacement(int n, int n2, RandomSpreadType randomSpreadType, int n3) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, n3, Optional.empty(), n, n2, randomSpreadType);
    }

    public int spacing() {
        return this.spacing;
    }

    public int separation() {
        return this.separation;
    }

    public RandomSpreadType spreadType() {
        return this.spreadType;
    }

    public ChunkPos getPotentialStructureChunk(long l, int n, int n2) {
        int n3 = Math.floorDiv(n, this.spacing);
        int n4 = Math.floorDiv(n2, this.spacing);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(l, n3, n4, this.salt());
        int n5 = this.spacing - this.separation;
        int n6 = this.spreadType.evaluate(worldgenRandom, n5);
        int n7 = this.spreadType.evaluate(worldgenRandom, n5);
        return new ChunkPos(n3 * this.spacing + n6, n4 * this.spacing + n7);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int n, int n2) {
        ChunkPos chunkPos = this.getPotentialStructureChunk(chunkGeneratorStructureState.getLevelSeed(), n, n2);
        return chunkPos.x == n && chunkPos.z == n2;
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.RANDOM_SPREAD;
    }
}

