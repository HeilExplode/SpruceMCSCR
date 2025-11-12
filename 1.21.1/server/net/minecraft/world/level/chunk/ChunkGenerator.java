/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.world.level.chunk;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class ChunkGenerator {
    public static final Codec<ChunkGenerator> CODEC = BuiltInRegistries.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    protected final BiomeSource biomeSource;
    private final Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;
    private final Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter;

    public ChunkGenerator(BiomeSource biomeSource) {
        this(biomeSource, holder -> ((Biome)holder.value()).getGenerationSettings());
    }

    public ChunkGenerator(BiomeSource biomeSource, Function<Holder<Biome>, BiomeGenerationSettings> function) {
        this.biomeSource = biomeSource;
        this.generationSettingsGetter = function;
        this.featuresPerStep = Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(List.copyOf(biomeSource.possibleBiomes()), holder -> ((BiomeGenerationSettings)function.apply((Holder<Biome>)holder)).features(), true));
    }

    public void validate() {
        this.featuresPerStep.get();
    }

    protected abstract MapCodec<? extends ChunkGenerator> codec();

    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> holderLookup, RandomState randomState, long l) {
        return ChunkGeneratorStructureState.createForNormal(randomState, l, this.biomeSource, holderLookup);
    }

    public Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return BuiltInRegistries.CHUNK_GENERATOR.getResourceKey(this.codec());
    }

    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            chunkAccess.fillBiomesFromNoise(this.biomeSource, randomState.sampler());
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    public abstract void applyCarvers(WorldGenRegion var1, long var2, RandomState var4, BiomeManager var5, StructureManager var6, ChunkAccess var7, GenerationStep.Carving var8);

    @Nullable
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel serverLevel, HolderSet<Structure> holderSet, BlockPos blockPos, int n, boolean bl) {
        ChunkGeneratorStructureState chunkGeneratorStructureState = serverLevel.getChunkSource().getGeneratorState();
        Object2ObjectArrayMap object2ObjectArrayMap = new Object2ObjectArrayMap();
        for (Holder holder : holderSet) {
            for (StructurePlacement object2 : chunkGeneratorStructureState.getPlacementsForStructure(holder)) {
                object2ObjectArrayMap.computeIfAbsent(object2, structurePlacement -> new ObjectArraySet()).add(holder);
            }
        }
        if (object2ObjectArrayMap.isEmpty()) {
            return null;
        }
        Iterator<Object> iterator = null;
        double d = Double.MAX_VALUE;
        StructureManager structureManager = serverLevel.structureManager();
        ArrayList arrayList = new ArrayList(object2ObjectArrayMap.size());
        for (Map.Entry n3 : object2ObjectArrayMap.entrySet()) {
            StructurePlacement i = (StructurePlacement)n3.getKey();
            if (i instanceof ConcentricRingsStructurePlacement) {
                BlockPos blockPos2;
                double d2;
                ConcentricRingsStructurePlacement bl2 = (ConcentricRingsStructurePlacement)i;
                Iterator iterator2 = this.getNearestGeneratedStructure((Set)n3.getValue(), serverLevel, structureManager, blockPos, bl, bl2);
                if (iterator2 == null || !((d2 = blockPos.distSqr(blockPos2 = (BlockPos)iterator2.getFirst())) < d)) continue;
                d = d2;
                iterator = iterator2;
                continue;
            }
            if (!(i instanceof RandomSpreadStructurePlacement)) continue;
            arrayList.add(n3);
        }
        if (!arrayList.isEmpty()) {
            int n2 = SectionPos.blockToSectionCoord(blockPos.getX());
            int n3 = SectionPos.blockToSectionCoord(blockPos.getZ());
            for (int i = 0; i <= n; ++i) {
                boolean bl2 = false;
                for (Map.Entry entry : arrayList) {
                    RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)entry.getKey();
                    Pair<BlockPos, Holder<Structure>> pair = ChunkGenerator.getNearestGeneratedStructure((Set)entry.getValue(), serverLevel, structureManager, n2, n3, i, bl, chunkGeneratorStructureState.getLevelSeed(), randomSpreadStructurePlacement);
                    if (pair == null) continue;
                    bl2 = true;
                    double d3 = blockPos.distSqr((Vec3i)pair.getFirst());
                    if (!(d3 < d)) continue;
                    d = d3;
                    iterator = pair;
                }
                if (!bl2) continue;
                return iterator;
            }
        }
        return iterator;
    }

    @Nullable
    private Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> set, ServerLevel serverLevel, StructureManager structureManager, BlockPos blockPos, boolean bl, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        List<ChunkPos> list = serverLevel.getChunkSource().getGeneratorState().getRingPositionsFor(concentricRingsStructurePlacement);
        if (list == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        }
        Pair<BlockPos, Holder<Structure>> pair = null;
        double d = Double.MAX_VALUE;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (ChunkPos chunkPos : list) {
            Pair<BlockPos, Holder<Structure>> pair2;
            mutableBlockPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
            double d2 = mutableBlockPos.distSqr(blockPos);
            boolean bl2 = pair == null || d2 < d;
            if (!bl2 || (pair2 = ChunkGenerator.getStructureGeneratingAt(set, serverLevel, structureManager, bl, concentricRingsStructurePlacement, chunkPos)) == null) continue;
            pair = pair2;
            d = d2;
        }
        return pair;
    }

    @Nullable
    private static Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> set, LevelReader levelReader, StructureManager structureManager, int n, int n2, int n3, boolean bl, long l, RandomSpreadStructurePlacement randomSpreadStructurePlacement) {
        int n4 = randomSpreadStructurePlacement.spacing();
        for (int i = -n3; i <= n3; ++i) {
            boolean bl2 = i == -n3 || i == n3;
            for (int j = -n3; j <= n3; ++j) {
                int n5;
                int n6;
                ChunkPos chunkPos;
                Pair<BlockPos, Holder<Structure>> pair;
                boolean bl3;
                boolean bl4 = bl3 = j == -n3 || j == n3;
                if (!bl2 && !bl3 || (pair = ChunkGenerator.getStructureGeneratingAt(set, levelReader, structureManager, bl, randomSpreadStructurePlacement, chunkPos = randomSpreadStructurePlacement.getPotentialStructureChunk(l, n6 = n + n4 * i, n5 = n2 + n4 * j))) == null) continue;
                return pair;
            }
        }
        return null;
    }

    @Nullable
    private static Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(Set<Holder<Structure>> set, LevelReader levelReader, StructureManager structureManager, boolean bl, StructurePlacement structurePlacement, ChunkPos chunkPos) {
        for (Holder<Structure> holder : set) {
            StructureCheckResult structureCheckResult = structureManager.checkStructurePresence(chunkPos, holder.value(), structurePlacement, bl);
            if (structureCheckResult == StructureCheckResult.START_NOT_PRESENT) continue;
            if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
                return Pair.of((Object)structurePlacement.getLocatePos(chunkPos), holder);
            }
            ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart structureStart = structureManager.getStartForStructure(SectionPos.bottomOf(chunkAccess), holder.value(), chunkAccess);
            if (structureStart == null || !structureStart.isValid() || bl && !ChunkGenerator.tryAddReference(structureManager, structureStart)) continue;
            return Pair.of((Object)structurePlacement.getLocatePos(structureStart.getChunkPos()), holder);
        }
        return null;
    }

    private static boolean tryAddReference(StructureManager structureManager, StructureStart structureStart) {
        if (structureStart.canBeReferenced()) {
            structureManager.addReference(structureStart);
            return true;
        }
        return false;
    }

    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager) {
        ChunkPos chunkPos = chunkAccess.getPos();
        if (SharedConstants.debugVoidTerrain(chunkPos)) {
            return;
        }
        SectionPos sectionPos = SectionPos.of(chunkPos, worldGenLevel.getMinSection());
        BlockPos blockPos = sectionPos.origin();
        Registry<Structure> registry = worldGenLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Map<Integer, List<Structure>> map = registry.stream().collect(Collectors.groupingBy(structure -> structure.step().ordinal()));
        List<FeatureSorter.StepFeatureData> list = this.featuresPerStep.get();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long l = worldgenRandom.setDecorationSeed(worldGenLevel.getSeed(), blockPos.getX(), blockPos.getZ());
        ObjectArraySet objectArraySet = new ObjectArraySet();
        ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(arg_0 -> ChunkGenerator.lambda$applyBiomeDecoration$6(worldGenLevel, (Set)objectArraySet, arg_0));
        objectArraySet.retainAll(this.biomeSource.possibleBiomes());
        int n = list.size();
        try {
            Registry<PlacedFeature> registry2 = worldGenLevel.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            int n2 = Math.max(GenerationStep.Decoration.values().length, n);
            for (int i = 0; i < n2; ++i) {
                List<HolderSet<PlacedFeature>> list2;
                Object object2;
                IntArraySet intArraySet;
                int n3 = 0;
                if (structureManager.shouldGenerateStructures()) {
                    intArraySet = map.getOrDefault(i, Collections.emptyList());
                    for (Object object2 : intArraySet) {
                        worldgenRandom.setFeatureSeed(l, n3, i);
                        list2 = () -> ChunkGenerator.lambda$applyBiomeDecoration$7(registry, (Structure)object2);
                        try {
                            worldGenLevel.setCurrentlyGenerating((Supplier<String>)((Object)list2));
                            structureManager.startsForStructure(sectionPos, (Structure)object2).forEach(structureStart -> structureStart.placeInChunk(worldGenLevel, structureManager, this, worldgenRandom, ChunkGenerator.getWritableArea(chunkAccess), chunkPos));
                        }
                        catch (Exception exception) {
                            CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                            crashReport.addCategory("Feature").setDetail("Description", ((Supplier)((Object)list2))::get);
                            throw new ReportedException(crashReport);
                        }
                        ++n3;
                    }
                }
                if (i >= n) continue;
                intArraySet = new IntArraySet();
                for (Object object2 : objectArraySet) {
                    list2 = this.generationSettingsGetter.apply((Holder<Biome>)object2).features();
                    if (i >= list2.size()) continue;
                    HolderSet holderSet = (HolderSet)list2.get(i);
                    FeatureSorter.StepFeatureData stepFeatureData = list.get(i);
                    holderSet.stream().map(Holder::value).forEach(arg_0 -> ChunkGenerator.lambda$applyBiomeDecoration$9((IntSet)intArraySet, stepFeatureData, arg_0));
                }
                int n4 = intArraySet.size();
                object2 = intArraySet.toIntArray();
                Arrays.sort((int[])object2);
                list2 = list.get(i);
                for (int j = 0; j < n4; ++j) {
                    Object object3 = object2[j];
                    PlacedFeature placedFeature = ((FeatureSorter.StepFeatureData)((Object)list2)).features().get((int)object3);
                    Supplier<String> supplier = () -> registry2.getResourceKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
                    worldgenRandom.setFeatureSeed(l, (int)object3, i);
                    try {
                        worldGenLevel.setCurrentlyGenerating(supplier);
                        placedFeature.placeWithBiomeCheck(worldGenLevel, this, worldgenRandom, blockPos);
                        continue;
                    }
                    catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                        crashReport.addCategory("Feature").setDetail("Description", supplier::get);
                        throw new ReportedException(crashReport);
                    }
                }
            }
            worldGenLevel.setCurrentlyGenerating(null);
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Biome decoration");
            crashReport.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Decoration Seed", l);
            throw new ReportedException(crashReport);
        }
    }

    private static BoundingBox getWritableArea(ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int n = chunkPos.getMinBlockX();
        int n2 = chunkPos.getMinBlockZ();
        LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
        int n3 = levelHeightAccessor.getMinBuildHeight() + 1;
        int n4 = levelHeightAccessor.getMaxBuildHeight() - 1;
        return new BoundingBox(n, n3, n2, n + 15, n4, n2 + 15);
    }

    public abstract void buildSurface(WorldGenRegion var1, StructureManager var2, RandomState var3, ChunkAccess var4);

    public abstract void spawnOriginalMobs(WorldGenRegion var1);

    public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.biomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> holder, StructureManager structureManager, MobCategory mobCategory, BlockPos blockPos) {
        Map<Structure, LongSet> map = structureManager.getAllStructuresAt(blockPos);
        for (Map.Entry<Structure, LongSet> entry : map.entrySet()) {
            Structure structure = entry.getKey();
            StructureSpawnOverride structureSpawnOverride = structure.spawnOverrides().get(mobCategory);
            if (structureSpawnOverride == null) continue;
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = structureSpawnOverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE ? structureStart -> structureManager.structureHasPieceAt(blockPos, (StructureStart)structureStart) : structureStart -> structureStart.getBoundingBox().isInside(blockPos);
            structureManager.fillStartsForStructure(structure, entry.getValue(), structureStart -> {
                if (mutableBoolean.isFalse() && predicate.test((StructureStart)structureStart)) {
                    mutableBoolean.setTrue();
                }
            });
            if (!mutableBoolean.isTrue()) continue;
            return structureSpawnOverride.spawns();
        }
        return holder.value().getMobSettings().getMobs(mobCategory);
    }

    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState chunkGeneratorStructureState, StructureManager structureManager, ChunkAccess chunkAccess, StructureTemplateManager structureTemplateManager) {
        ChunkPos chunkPos = chunkAccess.getPos();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        RandomState randomState = chunkGeneratorStructureState.randomState();
        chunkGeneratorStructureState.possibleStructureSets().forEach(holder -> {
            StructurePlacement structurePlacement = ((StructureSet)holder.value()).placement();
            List<StructureSet.StructureSelectionEntry> list = ((StructureSet)holder.value()).structures();
            for (StructureSet.StructureSelectionEntry object2 : list) {
                StructureStart n = structureManager.getStartForStructure(sectionPos, object2.structure().value(), chunkAccess);
                if (n == null || !n.isValid()) continue;
                return;
            }
            if (!structurePlacement.isStructureChunk(chunkGeneratorStructureState, chunkPos.x, chunkPos.z)) {
                return;
            }
            if (list.size() == 1) {
                this.tryGenerateStructure(list.get(0), structureManager, registryAccess, randomState, structureTemplateManager, chunkGeneratorStructureState.getLevelSeed(), chunkAccess, chunkPos, sectionPos);
                return;
            }
            ArrayList arrayList = new ArrayList(list.size());
            arrayList.addAll(list);
            WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
            worldgenRandom.setLargeFeatureSeed(chunkGeneratorStructureState.getLevelSeed(), chunkPos.x, chunkPos.z);
            int n = 0;
            Iterator n2 = arrayList.iterator();
            while (n2.hasNext()) {
                StructureSet.StructureSelectionEntry n3 = (StructureSet.StructureSelectionEntry)n2.next();
                n += n3.weight();
            }
            while (!arrayList.isEmpty()) {
                StructureSet.StructureSelectionEntry structureSelectionEntry;
                int n3 = worldgenRandom.nextInt(n);
                int n4 = 0;
                Object object = arrayList.iterator();
                while (object.hasNext() && (n3 -= (structureSelectionEntry = (StructureSet.StructureSelectionEntry)object.next()).weight()) >= 0) {
                    ++n4;
                }
                object = (StructureSet.StructureSelectionEntry)arrayList.get(n4);
                if (this.tryGenerateStructure((StructureSet.StructureSelectionEntry)object, structureManager, registryAccess, randomState, structureTemplateManager, chunkGeneratorStructureState.getLevelSeed(), chunkAccess, chunkPos, sectionPos)) {
                    return;
                }
                arrayList.remove(n4);
                n -= ((StructureSet.StructureSelectionEntry)object).weight();
            }
        });
    }

    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos) {
        Structure structure = structureSelectionEntry.structure().value();
        int n = ChunkGenerator.fetchReferences(structureManager, chunkAccess, sectionPos, structure);
        HolderSet<Biome> holderSet = structure.biomes();
        Predicate<Holder<Biome>> predicate = holderSet::contains;
        StructureStart structureStart = structure.generate(registryAccess, this, this.biomeSource, randomState, structureTemplateManager, l, chunkPos, n, chunkAccess, predicate);
        if (structureStart.isValid()) {
            structureManager.setStartForStructure(sectionPos, structure, structureStart, chunkAccess);
            return true;
        }
        return false;
    }

    private static int fetchReferences(StructureManager structureManager, ChunkAccess chunkAccess, SectionPos sectionPos, Structure structure) {
        StructureStart structureStart = structureManager.getStartForStructure(sectionPos, structure, chunkAccess);
        return structureStart != null ? structureStart.getReferences() : 0;
    }

    public void createReferences(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkAccess chunkAccess) {
        int n = 8;
        ChunkPos chunkPos = chunkAccess.getPos();
        int n2 = chunkPos.x;
        int n3 = chunkPos.z;
        int n4 = chunkPos.getMinBlockX();
        int n5 = chunkPos.getMinBlockZ();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        for (int i = n2 - 8; i <= n2 + 8; ++i) {
            for (int j = n3 - 8; j <= n3 + 8; ++j) {
                long l = ChunkPos.asLong(i, j);
                for (StructureStart structureStart : worldGenLevel.getChunk(i, j).getAllStarts().values()) {
                    try {
                        if (!structureStart.isValid() || !structureStart.getBoundingBox().intersects(n4, n5, n4 + 15, n5 + 15)) continue;
                        structureManager.addReferenceForStructure(sectionPos, structureStart.getStructure(), l, chunkAccess);
                        DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
                    }
                    catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Generating structure reference");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
                        Optional<Registry<Structure>> optional = worldGenLevel.registryAccess().registry(Registries.STRUCTURE);
                        crashReportCategory.setDetail("Id", () -> optional.map(registry -> registry.getKey(structureStart.getStructure()).toString()).orElse("UNKNOWN"));
                        crashReportCategory.setDetail("Name", () -> BuiltInRegistries.STRUCTURE_TYPE.getKey(structureStart.getStructure().type()).toString());
                        crashReportCategory.setDetail("Class", () -> structureStart.getStructure().getClass().getCanonicalName());
                        throw new ReportedException(crashReport);
                    }
                }
            }
        }
    }

    public abstract CompletableFuture<ChunkAccess> fillFromNoise(Blender var1, RandomState var2, StructureManager var3, ChunkAccess var4);

    public abstract int getSeaLevel();

    public abstract int getMinY();

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4, RandomState var5);

    public abstract NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3, RandomState var4);

    public int getFirstFreeHeight(int n, int n2, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.getBaseHeight(n, n2, types, levelHeightAccessor, randomState);
    }

    public int getFirstOccupiedHeight(int n, int n2, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.getBaseHeight(n, n2, types, levelHeightAccessor, randomState) - 1;
    }

    public abstract void addDebugScreenInfo(List<String> var1, RandomState var2, BlockPos var3);

    @Deprecated
    public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> holder) {
        return this.generationSettingsGetter.apply(holder);
    }

    private static /* synthetic */ void lambda$applyBiomeDecoration$9(IntSet intSet, FeatureSorter.StepFeatureData stepFeatureData, PlacedFeature placedFeature) {
        intSet.add(stepFeatureData.indexMapping().applyAsInt(placedFeature));
    }

    private static /* synthetic */ String lambda$applyBiomeDecoration$7(Registry registry, Structure structure) {
        return registry.getResourceKey(structure).map(Object::toString).orElseGet(structure::toString);
    }

    private static /* synthetic */ void lambda$applyBiomeDecoration$6(WorldGenLevel worldGenLevel, Set set, ChunkPos chunkPos) {
        ChunkAccess chunkAccess = worldGenLevel.getChunk(chunkPos.x, chunkPos.z);
        for (LevelChunkSection levelChunkSection : chunkAccess.getSections()) {
            levelChunkSection.getBiomes().getAll(set::add);
        }
    }
}

