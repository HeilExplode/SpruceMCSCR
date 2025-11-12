/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.base.Ticker
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.slf4j.Logger;

public class ChunkGeneratorStructureState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RandomState randomState;
    private final BiomeSource biomeSource;
    private final long levelSeed;
    private final long concentricRingsSeed;
    private final Map<Structure, List<StructurePlacement>> placementsForStructure = new Object2ObjectOpenHashMap();
    private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions = new Object2ObjectArrayMap();
    private boolean hasGeneratedPositions;
    private final List<Holder<StructureSet>> possibleStructureSets;

    public static ChunkGeneratorStructureState createForFlat(RandomState randomState, long l, BiomeSource biomeSource, Stream<Holder<StructureSet>> stream) {
        List<Holder<StructureSet>> list = stream.filter(holder -> ChunkGeneratorStructureState.hasBiomesForStructureSet((StructureSet)holder.value(), biomeSource)).toList();
        return new ChunkGeneratorStructureState(randomState, biomeSource, l, 0L, list);
    }

    public static ChunkGeneratorStructureState createForNormal(RandomState randomState, long l, BiomeSource biomeSource, HolderLookup<StructureSet> holderLookup) {
        List<Holder<StructureSet>> list = holderLookup.listElements().filter(reference -> ChunkGeneratorStructureState.hasBiomesForStructureSet((StructureSet)reference.value(), biomeSource)).collect(Collectors.toUnmodifiableList());
        return new ChunkGeneratorStructureState(randomState, biomeSource, l, l, list);
    }

    private static boolean hasBiomesForStructureSet(StructureSet structureSet, BiomeSource biomeSource) {
        Stream stream = structureSet.structures().stream().flatMap(structureSelectionEntry -> {
            Structure structure = structureSelectionEntry.structure().value();
            return structure.biomes().stream();
        });
        return stream.anyMatch(biomeSource.possibleBiomes()::contains);
    }

    private ChunkGeneratorStructureState(RandomState randomState, BiomeSource biomeSource, long l, long l2, List<Holder<StructureSet>> list) {
        this.randomState = randomState;
        this.levelSeed = l;
        this.biomeSource = biomeSource;
        this.concentricRingsSeed = l2;
        this.possibleStructureSets = list;
    }

    public List<Holder<StructureSet>> possibleStructureSets() {
        return this.possibleStructureSets;
    }

    private void generatePositions() {
        Set<Holder<Biome>> set = this.biomeSource.possibleBiomes();
        this.possibleStructureSets().forEach(holder -> {
            StructurePlacement structurePlacement;
            StructureSet structureSet = (StructureSet)holder.value();
            boolean bl = false;
            for (StructureSet.StructureSelectionEntry object2 : structureSet.structures()) {
                Structure structure2 = object2.structure().value();
                if (!structure2.biomes().stream().anyMatch(set::contains)) continue;
                this.placementsForStructure.computeIfAbsent(structure2, structure -> new ArrayList()).add(structureSet.placement());
                bl = true;
            }
            if (bl && (structurePlacement = structureSet.placement()) instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
                this.ringPositions.put(concentricRingsStructurePlacement, this.generateRingPositions((Holder<StructureSet>)holder, concentricRingsStructurePlacement));
            }
        });
    }

    private CompletableFuture<List<ChunkPos>> generateRingPositions(Holder<StructureSet> holder, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        if (concentricRingsStructurePlacement.count() == 0) {
            return CompletableFuture.completedFuture(List.of());
        }
        Stopwatch stopwatch = Stopwatch.createStarted((Ticker)Util.TICKER);
        int n = concentricRingsStructurePlacement.distance();
        int n2 = concentricRingsStructurePlacement.count();
        ArrayList<CompletableFuture<ChunkPos>> arrayList = new ArrayList<CompletableFuture<ChunkPos>>(n2);
        int n3 = concentricRingsStructurePlacement.spread();
        HolderSet<Biome> holderSet = concentricRingsStructurePlacement.preferredBiomes();
        RandomSource randomSource = RandomSource.create();
        randomSource.setSeed(this.concentricRingsSeed);
        double d = randomSource.nextDouble() * Math.PI * 2.0;
        int n4 = 0;
        int n5 = 0;
        for (int i = 0; i < n2; ++i) {
            double d2 = (double)(4 * n + n * n5 * 6) + (randomSource.nextDouble() - 0.5) * ((double)n * 2.5);
            int n6 = (int)Math.round(Math.cos(d) * d2);
            int n7 = (int)Math.round(Math.sin(d) * d2);
            RandomSource randomSource2 = randomSource.fork();
            arrayList.add(CompletableFuture.supplyAsync(() -> {
                Pair<BlockPos, Holder<Biome>> pair = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(n6, 8), 0, SectionPos.sectionToBlockCoord(n7, 8), 112, holderSet::contains, randomSource2, this.randomState.sampler());
                if (pair != null) {
                    BlockPos blockPos = (BlockPos)pair.getFirst();
                    return new ChunkPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
                }
                return new ChunkPos(n6, n7);
            }, Util.backgroundExecutor()));
            d += Math.PI * 2 / (double)n3;
            if (++n4 != n3) continue;
            n4 = 0;
            n3 += 2 * n3 / (++n5 + 1);
            n3 = Math.min(n3, n2 - i);
            d += randomSource.nextDouble() * Math.PI * 2.0;
        }
        return Util.sequence(arrayList).thenApply(list -> {
            double d = (double)stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            LOGGER.debug("Calculation for {} took {}s", (Object)holder, (Object)d);
            return list;
        });
    }

    public void ensureStructuresGenerated() {
        if (!this.hasGeneratedPositions) {
            this.generatePositions();
            this.hasGeneratedPositions = true;
        }
    }

    @Nullable
    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        this.ensureStructuresGenerated();
        CompletableFuture<List<ChunkPos>> completableFuture = this.ringPositions.get(concentricRingsStructurePlacement);
        return completableFuture != null ? completableFuture.join() : null;
    }

    public List<StructurePlacement> getPlacementsForStructure(Holder<Structure> holder) {
        this.ensureStructuresGenerated();
        return this.placementsForStructure.getOrDefault(holder.value(), List.of());
    }

    public RandomState randomState() {
        return this.randomState;
    }

    public boolean hasStructureChunkInRange(Holder<StructureSet> holder, int n, int n2, int n3) {
        StructurePlacement structurePlacement = holder.value().placement();
        for (int i = n - n3; i <= n + n3; ++i) {
            for (int j = n2 - n3; j <= n2 + n3; ++j) {
                if (!structurePlacement.isStructureChunk(this, i, j)) continue;
                return true;
            }
        }
        return false;
    }

    public long getLevelSeed() {
        return this.levelSeed;
    }
}

