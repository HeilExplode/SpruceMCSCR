/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public abstract class BaseSpawner {
    public static final String SPAWN_DATA_TAG = "SpawnData";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EVENT_SPAWN = 1;
    private int spawnDelay = 20;
    private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
    @Nullable
    private SpawnData nextSpawnData;
    private double spin;
    private double oSpin;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity displayEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public void setEntityId(EntityType<?> entityType, @Nullable Level level, RandomSource randomSource, BlockPos blockPos) {
        this.getOrCreateNextSpawnData(level, randomSource, blockPos).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    private boolean isNearPlayer(Level level, BlockPos blockPos) {
        return level.hasNearbyAlivePlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(Level level, BlockPos blockPos) {
        if (!this.isNearPlayer(level, blockPos)) {
            this.oSpin = this.spin;
        } else if (this.displayEntity != null) {
            RandomSource randomSource = level.getRandom();
            double d = (double)blockPos.getX() + randomSource.nextDouble();
            double d2 = (double)blockPos.getY() + randomSource.nextDouble();
            double d3 = (double)blockPos.getZ() + randomSource.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, d, d2, d3, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, d, d2, d3, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.oSpin = this.spin;
            this.spin = (this.spin + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerLevel serverLevel, BlockPos blockPos) {
        if (!this.isNearPlayer(serverLevel, blockPos)) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.delay(serverLevel, blockPos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }
        boolean bl = false;
        RandomSource randomSource = serverLevel.getRandom();
        SpawnData spawnData = this.getOrCreateNextSpawnData(serverLevel, randomSource, blockPos);
        for (int i = 0; i < this.spawnCount; ++i) {
            Object object;
            double d;
            CompoundTag compoundTag = spawnData.getEntityToSpawn();
            Optional<EntityType<?>> optional = EntityType.by(compoundTag);
            if (optional.isEmpty()) {
                this.delay(serverLevel, blockPos);
                return;
            }
            ListTag listTag = compoundTag.getList("Pos", 6);
            int n = listTag.size();
            double d2 = n >= 1 ? listTag.getDouble(0) : (double)blockPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;
            double d3 = n >= 2 ? listTag.getDouble(1) : (double)(blockPos.getY() + randomSource.nextInt(3) - 1);
            double d4 = d = n >= 3 ? listTag.getDouble(2) : (double)blockPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;
            if (!serverLevel.noCollision(optional.get().getSpawnAABB(d2, d3, d))) continue;
            BlockPos blockPos2 = BlockPos.containing(d2, d3, d);
            if (!spawnData.getCustomSpawnRules().isPresent() ? !SpawnPlacements.checkSpawnRules(optional.get(), serverLevel, MobSpawnType.SPAWNER, blockPos2, serverLevel.getRandom()) : !optional.get().getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL || !((SpawnData.CustomSpawnRules)(object = spawnData.getCustomSpawnRules().get())).isValidPosition(blockPos2, serverLevel)) continue;
            object = EntityType.loadEntityRecursive(compoundTag, serverLevel, entity -> {
                entity.moveTo(d2, d3, d, entity.getYRot(), entity.getXRot());
                return entity;
            });
            if (object == null) {
                this.delay(serverLevel, blockPos);
                return;
            }
            int n2 = serverLevel.getEntities(EntityTypeTest.forExactClass(object.getClass()), new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).inflate(this.spawnRange), EntitySelector.NO_SPECTATORS).size();
            if (n2 >= this.maxNearbyEntities) {
                this.delay(serverLevel, blockPos);
                return;
            }
            ((Entity)object).moveTo(((Entity)object).getX(), ((Entity)object).getY(), ((Entity)object).getZ(), randomSource.nextFloat() * 360.0f, 0.0f);
            if (object instanceof Mob) {
                boolean bl2;
                Mob mob = (Mob)object;
                if (spawnData.getCustomSpawnRules().isEmpty() && !mob.checkSpawnRules(serverLevel, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(serverLevel)) continue;
                boolean bl3 = bl2 = spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().contains("id", 8);
                if (bl2) {
                    ((Mob)object).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(((Entity)object).blockPosition()), MobSpawnType.SPAWNER, null);
                }
                spawnData.getEquipment().ifPresent(mob::equip);
            }
            if (!serverLevel.tryAddFreshEntityWithPassengers((Entity)object)) {
                this.delay(serverLevel, blockPos);
                return;
            }
            serverLevel.levelEvent(2004, blockPos, 0);
            serverLevel.gameEvent((Entity)object, GameEvent.ENTITY_PLACE, blockPos2);
            if (object instanceof Mob) {
                ((Mob)object).spawnAnim();
            }
            bl = true;
        }
        if (bl) {
            this.delay(serverLevel, blockPos);
        }
    }

    private void delay(Level level, BlockPos blockPos) {
        RandomSource randomSource = level.random;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + randomSource.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getRandom(randomSource).ifPresent(wrapper -> this.setNextSpawnData(level, blockPos, (SpawnData)wrapper.data()));
        this.broadcastEvent(level, blockPos, 1);
    }

    public void load(@Nullable Level level, BlockPos blockPos, CompoundTag compoundTag) {
        boolean bl;
        this.spawnDelay = compoundTag.getShort("Delay");
        boolean bl2 = compoundTag.contains(SPAWN_DATA_TAG, 10);
        if (bl2) {
            SpawnData spawnData = SpawnData.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getCompound(SPAWN_DATA_TAG)).resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string)).orElseGet(SpawnData::new);
            this.setNextSpawnData(level, blockPos, spawnData);
        }
        if (bl = compoundTag.contains("SpawnPotentials", 9)) {
            ListTag listTag = compoundTag.getList("SpawnPotentials", 10);
            this.spawnPotentials = SpawnData.LIST_CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)listTag).resultOrPartial(string -> LOGGER.warn("Invalid SpawnPotentials list: {}", string)).orElseGet(SimpleWeightedRandomList::empty);
        } else {
            this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData());
        }
        if (compoundTag.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = compoundTag.getShort("MinSpawnDelay");
            this.maxSpawnDelay = compoundTag.getShort("MaxSpawnDelay");
            this.spawnCount = compoundTag.getShort("SpawnCount");
        }
        if (compoundTag.contains("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = compoundTag.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = compoundTag.getShort("RequiredPlayerRange");
        }
        if (compoundTag.contains("SpawnRange", 99)) {
            this.spawnRange = compoundTag.getShort("SpawnRange");
        }
        this.displayEntity = null;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putShort("Delay", (short)this.spawnDelay);
        compoundTag.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        compoundTag.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        compoundTag.putShort("SpawnCount", (short)this.spawnCount);
        compoundTag.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        compoundTag.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        compoundTag.putShort("SpawnRange", (short)this.spawnRange);
        if (this.nextSpawnData != null) {
            compoundTag.put(SPAWN_DATA_TAG, (Tag)SpawnData.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.nextSpawnData).getOrThrow(string -> new IllegalStateException("Invalid SpawnData: " + string)));
        }
        compoundTag.put("SpawnPotentials", (Tag)SpawnData.LIST_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, this.spawnPotentials).getOrThrow());
        return compoundTag;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level level, BlockPos blockPos) {
        if (this.displayEntity == null) {
            CompoundTag compoundTag = this.getOrCreateNextSpawnData(level, level.getRandom(), blockPos).getEntityToSpawn();
            if (!compoundTag.contains("id", 8)) {
                return null;
            }
            this.displayEntity = EntityType.loadEntityRecursive(compoundTag, level, Function.identity());
            if (compoundTag.size() != 1 || this.displayEntity instanceof Mob) {
                // empty if block
            }
        }
        return this.displayEntity;
    }

    public boolean onEventTriggered(Level level, int n) {
        if (n == 1) {
            if (level.isClientSide) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    protected void setNextSpawnData(@Nullable Level level, BlockPos blockPos, SpawnData spawnData) {
        this.nextSpawnData = spawnData;
    }

    private SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource randomSource, BlockPos blockPos) {
        if (this.nextSpawnData != null) {
            return this.nextSpawnData;
        }
        this.setNextSpawnData(level, blockPos, this.spawnPotentials.getRandom(randomSource).map(WeightedEntry.Wrapper::data).orElseGet(SpawnData::new));
        return this.nextSpawnData;
    }

    public abstract void broadcastEvent(Level var1, BlockPos var2, int var3);

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }
}

