/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class TrialSpawner {
    public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
    public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02f;
    private final TrialSpawnerConfig normalConfig;
    private final TrialSpawnerConfig ominousConfig;
    private final TrialSpawnerData data;
    private final int requiredPlayerRange;
    private final int targetCooldownLength;
    private final StateAccessor stateAccessor;
    private PlayerDetector playerDetector;
    private final PlayerDetector.EntitySelector entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;

    public Codec<TrialSpawner> codec() {
        return RecordCodecBuilder.create(instance -> instance.group((App)TrialSpawnerConfig.CODEC.optionalFieldOf(NORMAL_CONFIG_TAG_NAME, (Object)TrialSpawnerConfig.DEFAULT).forGetter(TrialSpawner::getNormalConfig), (App)TrialSpawnerConfig.CODEC.optionalFieldOf(OMINOUS_CONFIG_TAG_NAME, (Object)TrialSpawnerConfig.DEFAULT).forGetter(TrialSpawner::getOminousConfigForSerialization), (App)TrialSpawnerData.MAP_CODEC.forGetter(TrialSpawner::getData), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", (Object)36000).forGetter(TrialSpawner::getTargetCooldownLength), (App)Codec.intRange((int)1, (int)128).optionalFieldOf("required_player_range", (Object)14).forGetter(TrialSpawner::getRequiredPlayerRange)).apply((Applicative)instance, (trialSpawnerConfig, trialSpawnerConfig2, trialSpawnerData, n, n2) -> new TrialSpawner((TrialSpawnerConfig)trialSpawnerConfig, (TrialSpawnerConfig)trialSpawnerConfig2, (TrialSpawnerData)trialSpawnerData, (int)n, (int)n2, this.stateAccessor, this.playerDetector, this.entitySelector)));
    }

    public TrialSpawner(StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this(TrialSpawnerConfig.DEFAULT, TrialSpawnerConfig.DEFAULT, new TrialSpawnerData(), 36000, 14, stateAccessor, playerDetector, entitySelector);
    }

    public TrialSpawner(TrialSpawnerConfig trialSpawnerConfig, TrialSpawnerConfig trialSpawnerConfig2, TrialSpawnerData trialSpawnerData, int n, int n2, StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this.normalConfig = trialSpawnerConfig;
        this.ominousConfig = trialSpawnerConfig2;
        this.data = trialSpawnerData;
        this.targetCooldownLength = n;
        this.requiredPlayerRange = n2;
        this.stateAccessor = stateAccessor;
        this.playerDetector = playerDetector;
        this.entitySelector = entitySelector;
    }

    public TrialSpawnerConfig getConfig() {
        return this.isOminous ? this.ominousConfig : this.normalConfig;
    }

    @VisibleForTesting
    public TrialSpawnerConfig getNormalConfig() {
        return this.normalConfig;
    }

    @VisibleForTesting
    public TrialSpawnerConfig getOminousConfig() {
        return this.ominousConfig;
    }

    private TrialSpawnerConfig getOminousConfigForSerialization() {
        return !this.ominousConfig.equals(this.normalConfig) ? this.ominousConfig : TrialSpawnerConfig.DEFAULT;
    }

    public void applyOminous(ServerLevel serverLevel, BlockPos blockPos) {
        serverLevel.setBlock(blockPos, (BlockState)serverLevel.getBlockState(blockPos).setValue(TrialSpawnerBlock.OMINOUS, true), 3);
        serverLevel.levelEvent(3020, blockPos, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, serverLevel);
    }

    public void removeOminous(ServerLevel serverLevel, BlockPos blockPos) {
        serverLevel.setBlock(blockPos, (BlockState)serverLevel.getBlockState(blockPos).setValue(TrialSpawnerBlock.OMINOUS, false), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public TrialSpawnerData getData() {
        return this.data;
    }

    public int getTargetCooldownLength() {
        return this.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    public TrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(Level level, TrialSpawnerState trialSpawnerState) {
        this.stateAccessor.setState(level, trialSpawnerState);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.EntitySelector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(Level level) {
        if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
    }

    public Optional<UUID> spawnMob(ServerLevel serverLevel, BlockPos blockPos) {
        Object object;
        Object object2;
        double d;
        RandomSource randomSource = serverLevel.getRandom();
        SpawnData spawnData = this.data.getOrCreateNextSpawnData(this, serverLevel.getRandom());
        CompoundTag compoundTag = spawnData.entityToSpawn();
        ListTag listTag = compoundTag.getList("Pos", 6);
        Optional<EntityType<?>> optional = EntityType.by(compoundTag);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        int n = listTag.size();
        double d2 = n >= 1 ? listTag.getDouble(0) : (double)blockPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
        double d3 = n >= 2 ? listTag.getDouble(1) : (double)(blockPos.getY() + randomSource.nextInt(3) - 1);
        double d4 = d = n >= 3 ? listTag.getDouble(2) : (double)blockPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
        if (!serverLevel.noCollision(optional.get().getSpawnAABB(d2, d3, d))) {
            return Optional.empty();
        }
        Vec3 vec3 = new Vec3(d2, d3, d);
        if (!TrialSpawner.inLineOfSight(serverLevel, blockPos.getCenter(), vec3)) {
            return Optional.empty();
        }
        BlockPos blockPos2 = BlockPos.containing(vec3);
        if (!SpawnPlacements.checkSpawnRules(optional.get(), serverLevel, MobSpawnType.TRIAL_SPAWNER, blockPos2, serverLevel.getRandom())) {
            return Optional.empty();
        }
        if (spawnData.getCustomSpawnRules().isPresent() && !((SpawnData.CustomSpawnRules)(object2 = spawnData.getCustomSpawnRules().get())).isValidPosition(blockPos2, serverLevel)) {
            return Optional.empty();
        }
        object2 = EntityType.loadEntityRecursive(compoundTag, serverLevel, entity -> {
            entity.moveTo(d2, d3, d, randomSource.nextFloat() * 360.0f, 0.0f);
            return entity;
        });
        if (object2 == null) {
            return Optional.empty();
        }
        if (object2 instanceof Mob) {
            boolean bl;
            object = (Mob)object2;
            if (!((Mob)object).checkSpawnObstruction(serverLevel)) {
                return Optional.empty();
            }
            boolean bl2 = bl = spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().contains("id", 8);
            if (bl) {
                ((Mob)object).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(((Entity)object).blockPosition()), MobSpawnType.TRIAL_SPAWNER, null);
            }
            ((Mob)object).setPersistenceRequired();
            spawnData.getEquipment().ifPresent(((Mob)object)::equip);
        }
        if (!serverLevel.tryAddFreshEntityWithPassengers((Entity)object2)) {
            return Optional.empty();
        }
        object = this.isOminous ? FlameParticle.OMINOUS : FlameParticle.NORMAL;
        serverLevel.levelEvent(3011, blockPos, ((FlameParticle)((Object)object)).encode());
        serverLevel.levelEvent(3012, blockPos2, ((FlameParticle)((Object)object)).encode());
        serverLevel.gameEvent((Entity)object2, GameEvent.ENTITY_PLACE, blockPos2);
        return Optional.of(((Entity)object2).getUUID());
    }

    public void ejectReward(ServerLevel serverLevel, BlockPos blockPos, ResourceKey<LootTable> resourceKey) {
        LootParams lootParams;
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY));
        if (!objectArrayList.isEmpty()) {
            for (ItemStack itemStack : objectArrayList) {
                DefaultDispenseItemBehavior.spawnItem(serverLevel, itemStack, 2, Direction.UP, Vec3.atBottomCenterOf(blockPos).relative(Direction.UP, 1.2));
            }
            serverLevel.levelEvent(3014, blockPos, 0);
        }
    }

    public void tickClient(Level level, BlockPos blockPos, boolean bl) {
        RandomSource randomSource;
        TrialSpawnerState trialSpawnerState = this.getState();
        trialSpawnerState.emitParticles(level, blockPos, bl);
        if (trialSpawnerState.hasSpinningMob()) {
            double d = Math.max(0L, this.data.nextMobSpawnsAt - level.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + trialSpawnerState.spinningMobSpeed() / (d + 200.0)) % 360.0;
        }
        if (trialSpawnerState.isCapableOfSpawning() && (randomSource = level.getRandom()).nextFloat() <= 0.02f) {
            SoundEvent soundEvent = bl ? SoundEvents.TRIAL_SPAWNER_AMBIENT_OMINOUS : SoundEvents.TRIAL_SPAWNER_AMBIENT;
            level.playLocalSound(blockPos, soundEvent, SoundSource.BLOCKS, randomSource.nextFloat() * 0.25f + 0.75f, randomSource.nextFloat() + 0.5f, false);
        }
    }

    public void tickServer(ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        TrialSpawnerState trialSpawnerState;
        this.isOminous = bl;
        TrialSpawnerState trialSpawnerState2 = this.getState();
        if (this.data.currentMobs.removeIf(uUID -> TrialSpawner.shouldMobBeUntracked(serverLevel, blockPos, uUID))) {
            this.data.nextMobSpawnsAt = serverLevel.getGameTime() + (long)this.getConfig().ticksBetweenSpawn();
        }
        if ((trialSpawnerState = trialSpawnerState2.tickAndGetNext(blockPos, this, serverLevel)) != trialSpawnerState2) {
            this.setState(serverLevel, trialSpawnerState);
        }
    }

    private static boolean shouldMobBeUntracked(ServerLevel serverLevel, BlockPos blockPos, UUID uUID) {
        Entity entity = serverLevel.getEntity(uUID);
        return entity == null || !entity.isAlive() || !entity.level().dimension().equals(serverLevel.dimension()) || entity.blockPosition().distSqr(blockPos) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level level, Vec3 vec3, Vec3 vec32) {
        BlockHitResult blockHitResult = level.clip(new ClipContext(vec32, vec3, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return blockHitResult.getBlockPos().equals(BlockPos.containing(vec3)) || blockHitResult.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level level, BlockPos blockPos, RandomSource randomSource, SimpleParticleType simpleParticleType) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double d2 = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double d3 = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.SMOKE, d, d2, d3, 0.0, 0.0, 0.0);
            level.addParticle(simpleParticleType, d, d2, d3, 0.0, 0.0, 0.0);
        }
    }

    public static void addBecomeOminousParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double d2 = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double d3 = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double d4 = randomSource.nextGaussian() * 0.02;
            double d5 = randomSource.nextGaussian() * 0.02;
            double d6 = randomSource.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.TRIAL_OMEN, d, d2, d3, d4, d5, d6);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d, d2, d3, d4, d5, d6);
        }
    }

    public static void addDetectPlayerParticles(Level level, BlockPos blockPos, RandomSource randomSource, int n, ParticleOptions particleOptions) {
        for (int i = 0; i < 30 + Math.min(n, 10) * 5; ++i) {
            double d = (double)(2.0f * randomSource.nextFloat() - 1.0f) * 0.65;
            double d2 = (double)(2.0f * randomSource.nextFloat() - 1.0f) * 0.65;
            double d3 = (double)blockPos.getX() + 0.5 + d;
            double d4 = (double)blockPos.getY() + 0.1 + (double)randomSource.nextFloat() * 0.8;
            double d5 = (double)blockPos.getZ() + 0.5 + d2;
            level.addParticle(particleOptions, d3, d4, d5, 0.0, 0.0, 0.0);
        }
    }

    public static void addEjectItemParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)blockPos.getX() + 0.4 + randomSource.nextDouble() * 0.2;
            double d2 = (double)blockPos.getY() + 0.4 + randomSource.nextDouble() * 0.2;
            double d3 = (double)blockPos.getZ() + 0.4 + randomSource.nextDouble() * 0.2;
            double d4 = randomSource.nextGaussian() * 0.02;
            double d5 = randomSource.nextGaussian() * 0.02;
            double d6 = randomSource.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.SMALL_FLAME, d, d2, d3, d4, d5, d6 * 0.25);
            level.addParticle(ParticleTypes.SMOKE, d, d2, d3, d4, d5, d6);
        }
    }

    @Deprecated(forRemoval=true)
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector playerDetector) {
        this.playerDetector = playerDetector;
    }

    @Deprecated(forRemoval=true)
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public static interface StateAccessor {
        public void setState(Level var1, TrialSpawnerState var2);

        public TrialSpawnerState getState();

        public void markUpdated();
    }

    public static enum FlameParticle {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particleType;

        private FlameParticle(SimpleParticleType simpleParticleType) {
            this.particleType = simpleParticleType;
        }

        public static FlameParticle decode(int n) {
            FlameParticle[] flameParticleArray = FlameParticle.values();
            if (n > flameParticleArray.length || n < 0) {
                return NORMAL;
            }
            return flameParticleArray[n];
        }

        public int encode() {
            return this.ordinal();
        }
    }
}

