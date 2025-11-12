/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P4
 *  com.mojang.datafixers.Products$P5
 *  com.mojang.datafixers.Products$P9
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ConcentricRingsStructurePlacement
extends StructurePlacement {
    public static final MapCodec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> ConcentricRingsStructurePlacement.codec((RecordCodecBuilder.Instance<ConcentricRingsStructurePlacement>)instance).apply((Applicative)instance, ConcentricRingsStructurePlacement::new));
    private final int distance;
    private final int spread;
    private final int count;
    private final HolderSet<Biome> preferredBiomes;

    private static Products.P9<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, HolderSet<Biome>> codec(RecordCodecBuilder.Instance<ConcentricRingsStructurePlacement> instance) {
        Products.P5<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> p5 = ConcentricRingsStructurePlacement.placementCodec(instance);
        Products.P4 p4 = instance.group((App)Codec.intRange((int)0, (int)1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance), (App)Codec.intRange((int)0, (int)1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread), (App)Codec.intRange((int)1, (int)4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count), (App)RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("preferred_biomes").forGetter(ConcentricRingsStructurePlacement::preferredBiomes));
        return new Products.P9(p5.t1(), p5.t2(), p5.t3(), p5.t4(), p5.t5(), p4.t1(), p4.t2(), p4.t3(), p4.t4());
    }

    public ConcentricRingsStructurePlacement(Vec3i vec3i, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float f, int n, Optional<StructurePlacement.ExclusionZone> optional, int n2, int n3, int n4, HolderSet<Biome> holderSet) {
        super(vec3i, frequencyReductionMethod, f, n, optional);
        this.distance = n2;
        this.spread = n3;
        this.count = n4;
        this.preferredBiomes = holderSet;
    }

    public ConcentricRingsStructurePlacement(int n, int n2, int n3, HolderSet<Biome> holderSet) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, 0, Optional.empty(), n, n2, n3, holderSet);
    }

    public int distance() {
        return this.distance;
    }

    public int spread() {
        return this.spread;
    }

    public int count() {
        return this.count;
    }

    public HolderSet<Biome> preferredBiomes() {
        return this.preferredBiomes;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int n, int n2) {
        List<ChunkPos> list = chunkGeneratorStructureState.getRingPositionsFor(this);
        if (list == null) {
            return false;
        }
        return list.contains(new ChunkPos(n, n2));
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.CONCENTRIC_RINGS;
    }
}

