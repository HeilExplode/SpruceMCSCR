/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.PotentialCalculator;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class NaturalSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SPAWN_DISTANCE = 24;
    public static final int SPAWN_DISTANCE_CHUNK = 8;
    public static final int SPAWN_DISTANCE_BLOCK = 128;
    static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
    private static final MobCategory[] SPAWNING_CATEGORIES = (MobCategory[])Stream.of(MobCategory.values()).filter(mobCategory -> mobCategory != MobCategory.MISC).toArray(MobCategory[]::new);

    private NaturalSpawner() {
    }

    public static SpawnState createState(int n, Iterable<Entity> iterable, ChunkGetter chunkGetter, LocalMobCapCalculator localMobCapCalculator) {
        PotentialCalculator potentialCalculator = new PotentialCalculator();
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (Entity entity : iterable) {
            Object object;
            if (entity instanceof Mob && (((Mob)(object = (Mob)entity)).isPersistenceRequired() || ((Mob)object).requiresCustomPersistence()) || (object = entity.getType().getCategory()) == MobCategory.MISC) continue;
            BlockPos blockPos = entity.blockPosition();
            chunkGetter.query(ChunkPos.asLong(blockPos), arg_0 -> NaturalSpawner.lambda$createState$2(blockPos, entity, potentialCalculator, localMobCapCalculator, (MobCategory)object, object2IntOpenHashMap, arg_0));
        }
        return new SpawnState(n, (Object2IntOpenHashMap<MobCategory>)object2IntOpenHashMap, potentialCalculator, localMobCapCalculator);
    }

    static Biome getRoughBiome(BlockPos blockPos, ChunkAccess chunkAccess) {
        return chunkAccess.getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ())).value();
    }

    public static void spawnForChunk(ServerLevel serverLevel, LevelChunk levelChunk, SpawnState spawnState, boolean bl, boolean bl2, boolean bl3) {
        serverLevel.getProfiler().push("spawner");
        for (MobCategory mobCategory : SPAWNING_CATEGORIES) {
            if (!bl && mobCategory.isFriendly() || !bl2 && !mobCategory.isFriendly() || !bl3 && mobCategory.isPersistent() || !spawnState.canSpawnForCategory(mobCategory, levelChunk.getPos())) continue;
            NaturalSpawner.spawnCategoryForChunk(mobCategory, serverLevel, levelChunk, spawnState::canSpawn, spawnState::afterSpawn);
        }
        serverLevel.getProfiler().pop();
    }

    public static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, SpawnPredicate spawnPredicate, AfterSpawnCallback afterSpawnCallback) {
        BlockPos blockPos = NaturalSpawner.getRandomPosWithin(serverLevel, levelChunk);
        if (blockPos.getY() < serverLevel.getMinBuildHeight() + 1) {
            return;
        }
        NaturalSpawner.spawnCategoryForPosition(mobCategory, serverLevel, levelChunk, blockPos, spawnPredicate, afterSpawnCallback);
    }

    @VisibleForDebug
    public static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, BlockPos blockPos2) {
        NaturalSpawner.spawnCategoryForPosition(mobCategory, serverLevel, serverLevel.getChunk(blockPos2), blockPos2, (entityType, blockPos, chunkAccess) -> true, (mob, chunkAccess) -> {});
    }

    public static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, SpawnPredicate spawnPredicate, AfterSpawnCallback afterSpawnCallback) {
        StructureManager structureManager = serverLevel.structureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        int n = blockPos.getY();
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (blockState.isRedstoneConductor(chunkAccess, blockPos)) {
            return;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n2 = 0;
        block0: for (int i = 0; i < 3; ++i) {
            int n3 = blockPos.getX();
            int n4 = blockPos.getZ();
            int n5 = 6;
            MobSpawnSettings.SpawnerData spawnerData = null;
            SpawnGroupData spawnGroupData = null;
            int n6 = Mth.ceil(serverLevel.random.nextFloat() * 4.0f);
            int n7 = 0;
            for (int j = 0; j < n6; ++j) {
                Object object;
                double d;
                mutableBlockPos.set(n3 += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6), n, n4 += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6));
                double d2 = (double)n3 + 0.5;
                double d3 = (double)n4 + 0.5;
                Player player = serverLevel.getNearestPlayer(d2, (double)n, d3, -1.0, false);
                if (player == null || !NaturalSpawner.isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, d = player.distanceToSqr(d2, n, d3))) continue;
                if (spawnerData == null) {
                    object = NaturalSpawner.getRandomSpawnMobAt(serverLevel, structureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos);
                    if (((Optional)object).isEmpty()) continue block0;
                    spawnerData = ((Optional)object).get();
                    n6 = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                }
                if (!NaturalSpawner.isValidSpawnPostitionForType(serverLevel, mobCategory, structureManager, chunkGenerator, spawnerData, mutableBlockPos, d) || !spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) continue;
                object = NaturalSpawner.getMobForSpawn(serverLevel, spawnerData.type);
                if (object == null) {
                    return;
                }
                ((Entity)object).moveTo(d2, n, d3, serverLevel.random.nextFloat() * 360.0f, 0.0f);
                if (!NaturalSpawner.isValidPositionForMob(serverLevel, (Mob)object, d)) continue;
                spawnGroupData = ((Mob)object).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(((Entity)object).blockPosition()), MobSpawnType.NATURAL, spawnGroupData);
                ++n7;
                serverLevel.addFreshEntityWithPassengers((Entity)object);
                afterSpawnCallback.run((Mob)object, chunkAccess);
                if (++n2 >= ((Mob)object).getMaxSpawnClusterSize()) {
                    return;
                }
                if (((Mob)object).isMaxGroupSizeReached(n7)) continue block0;
            }
        }
    }

    private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos, double d) {
        if (d <= 576.0) {
            return false;
        }
        if (serverLevel.getSharedSpawnPos().closerToCenterThan(new Vec3((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5), 24.0)) {
            return false;
        }
        return Objects.equals(new ChunkPos(mutableBlockPos), chunkAccess.getPos()) || serverLevel.isNaturalSpawningAllowed(mutableBlockPos);
    }

    private static boolean isValidSpawnPostitionForType(ServerLevel serverLevel, MobCategory mobCategory, StructureManager structureManager, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnerData, BlockPos.MutableBlockPos mutableBlockPos, double d) {
        EntityType<?> entityType = spawnerData.type;
        if (entityType.getCategory() == MobCategory.MISC) {
            return false;
        }
        if (!entityType.canSpawnFarFromPlayer() && d > (double)(entityType.getCategory().getDespawnDistance() * entityType.getCategory().getDespawnDistance())) {
            return false;
        }
        if (!entityType.canSummon() || !NaturalSpawner.canSpawnMobAt(serverLevel, structureManager, chunkGenerator, mobCategory, spawnerData, mutableBlockPos)) {
            return false;
        }
        if (!SpawnPlacements.isSpawnPositionOk(entityType, serverLevel, mutableBlockPos)) {
            return false;
        }
        if (!SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.NATURAL, mutableBlockPos, serverLevel.random)) {
            return false;
        }
        return serverLevel.noCollision(entityType.getSpawnAABB((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5));
    }

    @Nullable
    private static Mob getMobForSpawn(ServerLevel serverLevel, EntityType<?> entityType) {
        try {
            Object obj = entityType.create(serverLevel);
            if (obj instanceof Mob) {
                Mob mob = (Mob)obj;
                return mob;
            }
            LOGGER.warn("Can't spawn entity of type: {}", (Object)BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to create mob", (Throwable)exception);
        }
        return null;
    }

    private static boolean isValidPositionForMob(ServerLevel serverLevel, Mob mob, double d) {
        if (d > (double)(mob.getType().getCategory().getDespawnDistance() * mob.getType().getCategory().getDespawnDistance()) && mob.removeWhenFarAway(d)) {
            return false;
        }
        return mob.checkSpawnRules(serverLevel, MobSpawnType.NATURAL) && mob.checkSpawnObstruction(serverLevel);
    }

    private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, RandomSource randomSource, BlockPos blockPos) {
        Holder<Biome> holder = serverLevel.getBiome(blockPos);
        if (mobCategory == MobCategory.WATER_AMBIENT && holder.is(BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS) && randomSource.nextFloat() < 0.98f) {
            return Optional.empty();
        }
        return NaturalSpawner.mobsAt(serverLevel, structureManager, chunkGenerator, mobCategory, blockPos, holder).getRandom(randomSource);
    }

    private static boolean canSpawnMobAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, MobSpawnSettings.SpawnerData spawnerData, BlockPos blockPos) {
        return NaturalSpawner.mobsAt(serverLevel, structureManager, chunkGenerator, mobCategory, blockPos, null).unwrap().contains(spawnerData);
    }

    private static WeightedRandomList<MobSpawnSettings.SpawnerData> mobsAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, BlockPos blockPos, @Nullable Holder<Biome> holder) {
        if (NaturalSpawner.isInNetherFortressBounds(blockPos, serverLevel, mobCategory, structureManager)) {
            return NetherFortressStructure.FORTRESS_ENEMIES;
        }
        return chunkGenerator.getMobsAt(holder != null ? holder : serverLevel.getBiome(blockPos), structureManager, mobCategory, blockPos);
    }

    public static boolean isInNetherFortressBounds(BlockPos blockPos, ServerLevel serverLevel, MobCategory mobCategory, StructureManager structureManager) {
        if (mobCategory != MobCategory.MONSTER || !serverLevel.getBlockState(blockPos.below()).is(Blocks.NETHER_BRICKS)) {
            return false;
        }
        Structure structure = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.FORTRESS);
        if (structure == null) {
            return false;
        }
        return structureManager.getStructureAt(blockPos, structure).isValid();
    }

    private static BlockPos getRandomPosWithin(Level level, LevelChunk levelChunk) {
        ChunkPos chunkPos = levelChunk.getPos();
        int n = chunkPos.getMinBlockX() + level.random.nextInt(16);
        int n2 = chunkPos.getMinBlockZ() + level.random.nextInt(16);
        int n3 = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, n, n2) + 1;
        int n4 = Mth.randomBetweenInclusive(level.random, level.getMinBuildHeight(), n3);
        return new BlockPos(n, n4, n2);
    }

    public static boolean isValidEmptySpawnBlock(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, EntityType<?> entityType) {
        if (blockState.isCollisionShapeFullBlock(blockGetter, blockPos)) {
            return false;
        }
        if (blockState.isSignalSource()) {
            return false;
        }
        if (!fluidState.isEmpty()) {
            return false;
        }
        if (blockState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
            return false;
        }
        return !entityType.isBlockDangerous(blockState);
    }

    public static void spawnMobsForChunkGeneration(ServerLevelAccessor serverLevelAccessor, Holder<Biome> holder, ChunkPos chunkPos, RandomSource randomSource) {
        MobSpawnSettings mobSpawnSettings = holder.value().getMobSettings();
        WeightedRandomList<MobSpawnSettings.SpawnerData> weightedRandomList = mobSpawnSettings.getMobs(MobCategory.CREATURE);
        if (weightedRandomList.isEmpty()) {
            return;
        }
        int n = chunkPos.getMinBlockX();
        int n2 = chunkPos.getMinBlockZ();
        while (randomSource.nextFloat() < mobSpawnSettings.getCreatureProbability()) {
            Optional<MobSpawnSettings.SpawnerData> optional = weightedRandomList.getRandom(randomSource);
            if (optional.isEmpty()) continue;
            MobSpawnSettings.SpawnerData spawnerData = optional.get();
            int n3 = spawnerData.minCount + randomSource.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
            SpawnGroupData spawnGroupData = null;
            int n4 = n + randomSource.nextInt(16);
            int n5 = n2 + randomSource.nextInt(16);
            int n6 = n4;
            int n7 = n5;
            for (int i = 0; i < n3; ++i) {
                boolean bl = false;
                for (int j = 0; !bl && j < 4; ++j) {
                    BlockPos blockPos = NaturalSpawner.getTopNonCollidingPos(serverLevelAccessor, spawnerData.type, n4, n5);
                    if (spawnerData.type.canSummon() && SpawnPlacements.isSpawnPositionOk(spawnerData.type, serverLevelAccessor, blockPos)) {
                        Mob mob;
                        Object obj;
                        float f = spawnerData.type.getWidth();
                        double d = Mth.clamp((double)n4, (double)n + (double)f, (double)n + 16.0 - (double)f);
                        double d2 = Mth.clamp((double)n5, (double)n2 + (double)f, (double)n2 + 16.0 - (double)f);
                        if (!serverLevelAccessor.noCollision(spawnerData.type.getSpawnAABB(d, blockPos.getY(), d2)) || !SpawnPlacements.checkSpawnRules(spawnerData.type, serverLevelAccessor, MobSpawnType.CHUNK_GENERATION, BlockPos.containing(d, blockPos.getY(), d2), serverLevelAccessor.getRandom())) continue;
                        try {
                            obj = spawnerData.type.create(serverLevelAccessor.getLevel());
                        }
                        catch (Exception exception) {
                            LOGGER.warn("Failed to create mob", (Throwable)exception);
                            continue;
                        }
                        if (obj == null) continue;
                        ((Entity)obj).moveTo(d, blockPos.getY(), d2, randomSource.nextFloat() * 360.0f, 0.0f);
                        if (obj instanceof Mob && (mob = (Mob)obj).checkSpawnRules(serverLevelAccessor, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(serverLevelAccessor)) {
                            spawnGroupData = mob.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawnGroupData);
                            serverLevelAccessor.addFreshEntityWithPassengers(mob);
                            bl = true;
                        }
                    }
                    n4 += randomSource.nextInt(5) - randomSource.nextInt(5);
                    n5 += randomSource.nextInt(5) - randomSource.nextInt(5);
                    while (n4 < n || n4 >= n + 16 || n5 < n2 || n5 >= n2 + 16) {
                        n4 = n6 + randomSource.nextInt(5) - randomSource.nextInt(5);
                        n5 = n7 + randomSource.nextInt(5) - randomSource.nextInt(5);
                    }
                }
            }
        }
    }

    private static BlockPos getTopNonCollidingPos(LevelReader levelReader, EntityType<?> entityType, int n, int n2) {
        int n3 = levelReader.getHeight(SpawnPlacements.getHeightmapType(entityType), n, n2);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n, n3, n2);
        if (levelReader.dimensionType().hasCeiling()) {
            do {
                mutableBlockPos.move(Direction.DOWN);
            } while (!levelReader.getBlockState(mutableBlockPos).isAir());
            do {
                mutableBlockPos.move(Direction.DOWN);
            } while (levelReader.getBlockState(mutableBlockPos).isAir() && mutableBlockPos.getY() > levelReader.getMinBuildHeight());
        }
        return SpawnPlacements.getPlacementType(entityType).adjustSpawnPosition(levelReader, mutableBlockPos.immutable());
    }

    private static /* synthetic */ void lambda$createState$2(BlockPos blockPos, Entity entity, PotentialCalculator potentialCalculator, LocalMobCapCalculator localMobCapCalculator, MobCategory mobCategory, Object2IntOpenHashMap object2IntOpenHashMap, LevelChunk levelChunk) {
        MobSpawnSettings.MobSpawnCost mobSpawnCost = NaturalSpawner.getRoughBiome(blockPos, levelChunk).getMobSettings().getMobSpawnCost(entity.getType());
        if (mobSpawnCost != null) {
            potentialCalculator.addCharge(entity.blockPosition(), mobSpawnCost.charge());
        }
        if (entity instanceof Mob) {
            localMobCapCalculator.addMob(levelChunk.getPos(), mobCategory);
        }
        object2IntOpenHashMap.addTo((Object)mobCategory, 1);
    }

    @FunctionalInterface
    public static interface ChunkGetter {
        public void query(long var1, Consumer<LevelChunk> var3);
    }

    public static class SpawnState {
        private final int spawnableChunkCount;
        private final Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
        private final PotentialCalculator spawnPotential;
        private final Object2IntMap<MobCategory> unmodifiableMobCategoryCounts;
        private final LocalMobCapCalculator localMobCapCalculator;
        @Nullable
        private BlockPos lastCheckedPos;
        @Nullable
        private EntityType<?> lastCheckedType;
        private double lastCharge;

        SpawnState(int n, Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap, PotentialCalculator potentialCalculator, LocalMobCapCalculator localMobCapCalculator) {
            this.spawnableChunkCount = n;
            this.mobCategoryCounts = object2IntOpenHashMap;
            this.spawnPotential = potentialCalculator;
            this.localMobCapCalculator = localMobCapCalculator;
            this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(object2IntOpenHashMap);
        }

        private boolean canSpawn(EntityType<?> entityType, BlockPos blockPos, ChunkAccess chunkAccess) {
            double d;
            this.lastCheckedPos = blockPos;
            this.lastCheckedType = entityType;
            MobSpawnSettings.MobSpawnCost mobSpawnCost = NaturalSpawner.getRoughBiome(blockPos, chunkAccess).getMobSettings().getMobSpawnCost(entityType);
            if (mobSpawnCost == null) {
                this.lastCharge = 0.0;
                return true;
            }
            this.lastCharge = d = mobSpawnCost.charge();
            double d2 = this.spawnPotential.getPotentialEnergyChange(blockPos, d);
            return d2 <= mobSpawnCost.energyBudget();
        }

        private void afterSpawn(Mob mob, ChunkAccess chunkAccess) {
            Object object;
            EntityType<?> entityType = mob.getType();
            BlockPos blockPos = mob.blockPosition();
            double d = blockPos.equals(this.lastCheckedPos) && entityType == this.lastCheckedType ? this.lastCharge : ((object = NaturalSpawner.getRoughBiome(blockPos, chunkAccess).getMobSettings().getMobSpawnCost(entityType)) != null ? ((MobSpawnSettings.MobSpawnCost)object).charge() : 0.0);
            this.spawnPotential.addCharge(blockPos, d);
            object = entityType.getCategory();
            this.mobCategoryCounts.addTo(object, 1);
            this.localMobCapCalculator.addMob(new ChunkPos(blockPos), (MobCategory)object);
        }

        public int getSpawnableChunkCount() {
            return this.spawnableChunkCount;
        }

        public Object2IntMap<MobCategory> getMobCategoryCounts() {
            return this.unmodifiableMobCategoryCounts;
        }

        boolean canSpawnForCategory(MobCategory mobCategory, ChunkPos chunkPos) {
            int n = mobCategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / MAGIC_NUMBER;
            if (this.mobCategoryCounts.getInt((Object)mobCategory) >= n) {
                return false;
            }
            return this.localMobCapCalculator.canSpawn(mobCategory, chunkPos);
        }
    }

    @FunctionalInterface
    public static interface SpawnPredicate {
        public boolean test(EntityType<?> var1, BlockPos var2, ChunkAccess var3);
    }

    @FunctionalInterface
    public static interface AfterSpawnCallback {
        public void run(Mob var1, ChunkAccess var2);
    }
}

