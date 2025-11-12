/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public enum TrialSpawnerState implements StringRepresentable
{
    INACTIVE("inactive", 0, ParticleEmission.NONE, -1.0, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, ParticleEmission.SMALL_FLAMES, 200.0, true),
    ACTIVE("active", 8, ParticleEmission.FLAMES_AND_SMOKE, 1000.0, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, ParticleEmission.SMALL_FLAMES, -1.0, false),
    EJECTING_REWARD("ejecting_reward", 8, ParticleEmission.SMALL_FLAMES, -1.0, false),
    COOLDOWN("cooldown", 0, ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0f;
    private static final int TIME_BETWEEN_EACH_EJECTION;
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final ParticleEmission particleEmission;
    private final boolean isCapableOfSpawning;

    private TrialSpawnerState(String string2, int n2, ParticleEmission particleEmission, double d, boolean bl) {
        this.name = string2;
        this.lightLevel = n2;
        this.particleEmission = particleEmission;
        this.spinningMobSpeed = d;
        this.isCapableOfSpawning = bl;
    }

    TrialSpawnerState tickAndGetNext(BlockPos blockPos, TrialSpawner trialSpawner, ServerLevel serverLevel) {
        TrialSpawnerData trialSpawnerData = trialSpawner.getData();
        TrialSpawnerConfig trialSpawnerConfig = trialSpawner.getConfig();
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (trialSpawnerData.getOrCreateDisplayEntity(trialSpawner, serverLevel, WAITING_FOR_PLAYERS) == null) {
                    yield this;
                }
                yield WAITING_FOR_PLAYERS;
            }
            case 1 -> {
                if (!trialSpawner.canSpawnInLevel(serverLevel)) {
                    trialSpawnerData.reset();
                    yield this;
                }
                if (!trialSpawnerData.hasMobToSpawn(trialSpawner, serverLevel.random)) {
                    yield INACTIVE;
                }
                trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, trialSpawner);
                if (trialSpawnerData.detectedPlayers.isEmpty()) {
                    yield this;
                }
                yield ACTIVE;
            }
            case 2 -> {
                if (!trialSpawner.canSpawnInLevel(serverLevel)) {
                    trialSpawnerData.reset();
                    yield WAITING_FOR_PLAYERS;
                }
                if (!trialSpawnerData.hasMobToSpawn(trialSpawner, serverLevel.random)) {
                    yield INACTIVE;
                }
                int var6_6 = trialSpawnerData.countAdditionalPlayers(blockPos);
                trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, trialSpawner);
                if (trialSpawner.isOminous()) {
                    this.spawnOminousOminousItemSpawner(serverLevel, blockPos, trialSpawner);
                }
                if (trialSpawnerData.hasFinishedSpawningAllMobs(trialSpawnerConfig, var6_6)) {
                    if (trialSpawnerData.haveAllCurrentMobsDied()) {
                        trialSpawnerData.cooldownEndsAt = serverLevel.getGameTime() + (long)trialSpawner.getTargetCooldownLength();
                        trialSpawnerData.totalMobsSpawned = 0;
                        trialSpawnerData.nextMobSpawnsAt = 0L;
                        yield WAITING_FOR_REWARD_EJECTION;
                    }
                } else if (trialSpawnerData.isReadyToSpawnNextMob(serverLevel, trialSpawnerConfig, var6_6)) {
                    trialSpawner.spawnMob(serverLevel, blockPos).ifPresent(uUID -> {
                        trialSpawnerData.currentMobs.add((UUID)uUID);
                        ++trialSpawnerData.totalMobsSpawned;
                        trialSpawnerData.nextMobSpawnsAt = serverLevel.getGameTime() + (long)trialSpawnerConfig.ticksBetweenSpawn();
                        trialSpawnerConfig.spawnPotentialsDefinition().getRandom(serverLevel.getRandom()).ifPresent(wrapper -> {
                            trialSpawnerData.nextSpawnData = Optional.of((SpawnData)wrapper.data());
                            trialSpawner.markUpdated();
                        });
                    });
                }
                yield this;
            }
            case 3 -> {
                if (trialSpawnerData.isReadyToOpenShutter(serverLevel, 40.0f, trialSpawner.getTargetCooldownLength())) {
                    serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
                    yield EJECTING_REWARD;
                }
                yield this;
            }
            case 4 -> {
                if (!trialSpawnerData.isReadyToEjectItems(serverLevel, TIME_BETWEEN_EACH_EJECTION, trialSpawner.getTargetCooldownLength())) {
                    yield this;
                }
                if (trialSpawnerData.detectedPlayers.isEmpty()) {
                    serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
                    trialSpawnerData.ejectingLootTable = Optional.empty();
                    yield COOLDOWN;
                }
                if (trialSpawnerData.ejectingLootTable.isEmpty()) {
                    trialSpawnerData.ejectingLootTable = trialSpawnerConfig.lootTablesToEject().getRandomValue(serverLevel.getRandom());
                }
                trialSpawnerData.ejectingLootTable.ifPresent(resourceKey -> trialSpawner.ejectReward(serverLevel, blockPos, (ResourceKey<LootTable>)resourceKey));
                trialSpawnerData.detectedPlayers.remove(trialSpawnerData.detectedPlayers.iterator().next());
                yield this;
            }
            case 5 -> {
                trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, trialSpawner);
                if (!trialSpawnerData.detectedPlayers.isEmpty()) {
                    trialSpawnerData.totalMobsSpawned = 0;
                    trialSpawnerData.nextMobSpawnsAt = 0L;
                    yield ACTIVE;
                }
                if (trialSpawnerData.isCooldownFinished(serverLevel)) {
                    trialSpawner.removeOminous(serverLevel, blockPos);
                    trialSpawnerData.reset();
                    yield WAITING_FOR_PLAYERS;
                }
                yield this;
            }
        };
    }

    private void spawnOminousOminousItemSpawner(ServerLevel serverLevel, BlockPos blockPos, TrialSpawner trialSpawner) {
        TrialSpawnerConfig trialSpawnerConfig;
        TrialSpawnerData trialSpawnerData = trialSpawner.getData();
        ItemStack itemStack = trialSpawnerData.getDispensingItems(serverLevel, trialSpawnerConfig = trialSpawner.getConfig(), blockPos).getRandomValue(serverLevel.random).orElse(ItemStack.EMPTY);
        if (itemStack.isEmpty()) {
            return;
        }
        if (this.timeToSpawnItemSpawner(serverLevel, trialSpawnerData)) {
            TrialSpawnerState.calculatePositionToSpawnSpawner(serverLevel, blockPos, trialSpawner, trialSpawnerData).ifPresent(vec3 -> {
                OminousItemSpawner ominousItemSpawner = OminousItemSpawner.create(serverLevel, itemStack);
                ominousItemSpawner.moveTo((Vec3)vec3);
                serverLevel.addFreshEntity(ominousItemSpawner);
                float f = (serverLevel.getRandom().nextFloat() - serverLevel.getRandom().nextFloat()) * 0.2f + 1.0f;
                serverLevel.playSound(null, BlockPos.containing(vec3), SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0f, f);
                trialSpawnerData.cooldownEndsAt = serverLevel.getGameTime() + trialSpawner.getOminousConfig().ticksBetweenItemSpawners();
            });
        }
    }

    private static Optional<Vec3> calculatePositionToSpawnSpawner(ServerLevel serverLevel, BlockPos blockPos, TrialSpawner trialSpawner, TrialSpawnerData trialSpawnerData) {
        List<Player> list = trialSpawnerData.detectedPlayers.stream().map(serverLevel::getPlayerByUUID).filter(Objects::nonNull).filter(player -> !player.isCreative() && !player.isSpectator() && player.isAlive() && player.distanceToSqr(blockPos.getCenter()) <= (double)Mth.square(trialSpawner.getRequiredPlayerRange())).toList();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = TrialSpawnerState.selectEntityToSpawnItemAbove(list, trialSpawnerData.currentMobs, trialSpawner, blockPos, serverLevel);
        if (entity == null) {
            return Optional.empty();
        }
        return TrialSpawnerState.calculatePositionAbove(entity, serverLevel);
    }

    private static Optional<Vec3> calculatePositionAbove(Entity entity, ServerLevel serverLevel) {
        Vec3 vec3;
        Vec3 vec32 = entity.position();
        BlockHitResult blockHitResult = serverLevel.clip(new ClipContext(vec32, vec3 = vec32.relative(Direction.UP, entity.getBbHeight() + 2.0f + (float)serverLevel.random.nextInt(4)), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        Vec3 vec33 = blockHitResult.getBlockPos().getCenter().relative(Direction.DOWN, 1.0);
        BlockPos blockPos = BlockPos.containing(vec33);
        if (!serverLevel.getBlockState(blockPos).getCollisionShape(serverLevel, blockPos).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(vec33);
    }

    @Nullable
    private static Entity selectEntityToSpawnItemAbove(List<Player> list, Set<UUID> set, TrialSpawner trialSpawner, BlockPos blockPos, ServerLevel serverLevel) {
        List<Entity> list2;
        Stream<Entity> stream = set.stream().map(serverLevel::getEntity).filter(Objects::nonNull).filter(entity -> entity.isAlive() && entity.distanceToSqr(blockPos.getCenter()) <= (double)Mth.square(trialSpawner.getRequiredPlayerRange()));
        List<Entity> list3 = list2 = serverLevel.random.nextBoolean() ? stream.toList() : list;
        if (list2.isEmpty()) {
            return null;
        }
        if (list2.size() == 1) {
            return list2.getFirst();
        }
        return Util.getRandom(list2, serverLevel.random);
    }

    private boolean timeToSpawnItemSpawner(ServerLevel serverLevel, TrialSpawnerData trialSpawnerData) {
        return serverLevel.getGameTime() >= trialSpawnerData.cooldownEndsAt;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    public void emitParticles(Level level, BlockPos blockPos, boolean bl) {
        this.particleEmission.emit(level, level.getRandom(), blockPos, bl);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0f);
    }

    static interface ParticleEmission {
        public static final ParticleEmission NONE = (level, randomSource, blockPos, bl) -> {};
        public static final ParticleEmission SMALL_FLAMES = (level, randomSource, blockPos, bl) -> {
            if (randomSource.nextInt(2) == 0) {
                Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 0.9f);
                ParticleEmission.addParticle(bl ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, vec3, level);
            }
        };
        public static final ParticleEmission FLAMES_AND_SMOKE = (level, randomSource, blockPos, bl) -> {
            Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 1.0f);
            ParticleEmission.addParticle(ParticleTypes.SMOKE, vec3, level);
            ParticleEmission.addParticle(bl ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, vec3, level);
        };
        public static final ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (level, randomSource, blockPos, bl) -> {
            Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 0.9f);
            if (randomSource.nextInt(3) == 0) {
                ParticleEmission.addParticle(ParticleTypes.SMOKE, vec3, level);
            }
            if (level.getGameTime() % 20L == 0L) {
                Vec3 vec32 = blockPos.getCenter().add(0.0, 0.5, 0.0);
                int n = level.getRandom().nextInt(4) + 20;
                for (int i = 0; i < n; ++i) {
                    ParticleEmission.addParticle(ParticleTypes.SMOKE, vec32, level);
                }
            }
        };

        private static void addParticle(SimpleParticleType simpleParticleType, Vec3 vec3, Level level) {
            level.addParticle(simpleParticleType, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
        }

        public void emit(Level var1, RandomSource var2, BlockPos var3, boolean var4);
    }

    static class LightLevel {
        private static final int UNLIT = 0;
        private static final int HALF_LIT = 4;
        private static final int LIT = 8;

        private LightLevel() {
        }
    }

    static class SpinningMob {
        private static final double NONE = -1.0;
        private static final double SLOW = 200.0;
        private static final double FAST = 1000.0;

        private SpinningMob() {
        }
    }
}

