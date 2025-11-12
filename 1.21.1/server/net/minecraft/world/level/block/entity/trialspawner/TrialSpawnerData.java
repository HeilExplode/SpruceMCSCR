/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class TrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", (Object)Sets.newHashSet()).forGetter(trialSpawnerData -> trialSpawnerData.detectedPlayers), (App)UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", (Object)Sets.newHashSet()).forGetter(trialSpawnerData -> trialSpawnerData.currentMobs), (App)Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", (Object)0L).forGetter(trialSpawnerData -> trialSpawnerData.cooldownEndsAt), (App)Codec.LONG.lenientOptionalFieldOf(TAG_NEXT_MOB_SPAWNS_AT, (Object)0L).forGetter(trialSpawnerData -> trialSpawnerData.nextMobSpawnsAt), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", (Object)0).forGetter(trialSpawnerData -> trialSpawnerData.totalMobsSpawned), (App)SpawnData.CODEC.lenientOptionalFieldOf(TAG_SPAWN_DATA).forGetter(trialSpawnerData -> trialSpawnerData.nextSpawnData), (App)ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter(trialSpawnerData -> trialSpawnerData.ejectingLootTable)).apply((Applicative)instance, TrialSpawnerData::new));
    protected final Set<UUID> detectedPlayers = new HashSet<UUID>();
    protected final Set<UUID> currentMobs = new HashSet<UUID>();
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceKey<LootTable>> ejectingLootTable;
    @Nullable
    protected Entity displayEntity;
    @Nullable
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(Set<UUID> set, Set<UUID> set2, long l, long l2, int n, Optional<SpawnData> optional, Optional<ResourceKey<LootTable>> optional2) {
        this.detectedPlayers.addAll(set);
        this.currentMobs.addAll(set2);
        this.cooldownEndsAt = l;
        this.nextMobSpawnsAt = l2;
        this.totalMobsSpawned = n;
        this.nextSpawnData = optional;
        this.ejectingLootTable = optional2;
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
    }

    public boolean hasMobToSpawn(TrialSpawner trialSpawner, RandomSource randomSource) {
        boolean bl = this.getOrCreateNextSpawnData(trialSpawner, randomSource).getEntityToSpawn().contains("id", 8);
        return bl || !trialSpawner.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig trialSpawnerConfig, int n) {
        return this.totalMobsSpawned >= trialSpawnerConfig.calculateTargetTotalMobs(n);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, int n) {
        return serverLevel.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < trialSpawnerConfig.calculateTargetSimultaneousMobs(n);
    }

    public int countAdditionalPlayers(BlockPos blockPos) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Trial Spawner at " + String.valueOf(blockPos) + " has no detected players");
        }
        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel serverLevel, BlockPos blockPos, TrialSpawner trialSpawner) {
        List<UUID> list;
        boolean bl;
        boolean bl2;
        boolean bl3 = bl2 = (blockPos.asLong() + serverLevel.getGameTime()) % 20L != 0L;
        if (bl2) {
            return;
        }
        if (trialSpawner.getState().equals(TrialSpawnerState.COOLDOWN) && trialSpawner.isOminous()) {
            return;
        }
        List<UUID> list2 = trialSpawner.getPlayerDetector().detect(serverLevel, trialSpawner.getEntitySelector(), blockPos, trialSpawner.getRequiredPlayerRange(), true);
        if (trialSpawner.isOminous() || list2.isEmpty()) {
            bl = false;
        } else {
            Optional<Pair<Player, Holder<MobEffect>>> optional = TrialSpawnerData.findPlayerWithOminousEffect(serverLevel, list2);
            optional.ifPresent(pair -> {
                Player player = (Player)pair.getFirst();
                if (pair.getSecond() == MobEffects.BAD_OMEN) {
                    TrialSpawnerData.transformBadOmenIntoTrialOmen(player);
                }
                serverLevel.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
                trialSpawner.applyOminous(serverLevel, blockPos);
            });
            bl = optional.isPresent();
        }
        if (trialSpawner.getState().equals(TrialSpawnerState.COOLDOWN) && !bl) {
            return;
        }
        boolean bl4 = trialSpawner.getData().detectedPlayers.isEmpty();
        List<UUID> list3 = list = bl4 ? list2 : trialSpawner.getPlayerDetector().detect(serverLevel, trialSpawner.getEntitySelector(), blockPos, trialSpawner.getRequiredPlayerRange(), false);
        if (this.detectedPlayers.addAll(list)) {
            this.nextMobSpawnsAt = Math.max(serverLevel.getGameTime() + 40L, this.nextMobSpawnsAt);
            if (!bl) {
                int n = trialSpawner.isOminous() ? 3019 : 3013;
                serverLevel.levelEvent(n, blockPos, this.detectedPlayers.size());
            }
        }
    }

    private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel serverLevel, List<UUID> list) {
        Player player2 = null;
        for (UUID uUID : list) {
            Player player3 = serverLevel.getPlayerByUUID(uUID);
            if (player3 == null) continue;
            Holder<MobEffect> holder = MobEffects.TRIAL_OMEN;
            if (player3.hasEffect(holder)) {
                return Optional.of(Pair.of((Object)player3, holder));
            }
            if (!player3.hasEffect(MobEffects.BAD_OMEN)) continue;
            player2 = player3;
        }
        return Optional.ofNullable(player2).map(player -> Pair.of((Object)player, MobEffects.BAD_OMEN));
    }

    public void resetAfterBecomingOminous(TrialSpawner trialSpawner, ServerLevel serverLevel) {
        this.currentMobs.stream().map(serverLevel::getEntity).forEach(entity -> {
            if (entity == null) {
                return;
            }
            serverLevel.levelEvent(3012, entity.blockPosition(), TrialSpawner.FlameParticle.NORMAL.encode());
            if (entity instanceof Mob) {
                Mob mob = (Mob)entity;
                mob.dropPreservedEquipment();
            }
            entity.remove(Entity.RemovalReason.DISCARDED);
        });
        if (!trialSpawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }
        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = serverLevel.getGameTime() + (long)trialSpawner.getOminousConfig().ticksBetweenSpawn();
        trialSpawner.markUpdated();
        this.cooldownEndsAt = serverLevel.getGameTime() + trialSpawner.getOminousConfig().ticksBetweenItemSpawners();
    }

    private static void transformBadOmenIntoTrialOmen(Player player) {
        MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.BAD_OMEN);
        if (mobEffectInstance == null) {
            return;
        }
        int n = mobEffectInstance.getAmplifier() + 1;
        int n2 = 18000 * n;
        player.removeEffect(MobEffects.BAD_OMEN);
        player.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, n2, 0));
    }

    public boolean isReadyToOpenShutter(ServerLevel serverLevel, float f, int n) {
        long l = this.cooldownEndsAt - (long)n;
        return (float)serverLevel.getGameTime() >= (float)l + f;
    }

    public boolean isReadyToEjectItems(ServerLevel serverLevel, float f, int n) {
        long l = this.cooldownEndsAt - (long)n;
        return (float)(serverLevel.getGameTime() - l) % f == 0.0f;
    }

    public boolean isCooldownFinished(ServerLevel serverLevel) {
        return serverLevel.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(TrialSpawner trialSpawner, RandomSource randomSource, EntityType<?> entityType) {
        this.getOrCreateNextSpawnData(trialSpawner, randomSource).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    protected SpawnData getOrCreateNextSpawnData(TrialSpawner trialSpawner, RandomSource randomSource) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        }
        SimpleWeightedRandomList<SpawnData> simpleWeightedRandomList = trialSpawner.getConfig().spawnPotentialsDefinition();
        Optional<SpawnData> optional = simpleWeightedRandomList.isEmpty() ? this.nextSpawnData : simpleWeightedRandomList.getRandom(randomSource).map(WeightedEntry.Wrapper::data);
        this.nextSpawnData = Optional.of(optional.orElseGet(SpawnData::new));
        trialSpawner.markUpdated();
        return this.nextSpawnData.get();
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner trialSpawner, Level level, TrialSpawnerState trialSpawnerState) {
        CompoundTag compoundTag;
        if (!trialSpawnerState.hasSpinningMob()) {
            return null;
        }
        if (this.displayEntity == null && (compoundTag = this.getOrCreateNextSpawnData(trialSpawner, level.getRandom()).getEntityToSpawn()).contains("id", 8)) {
            this.displayEntity = EntityType.loadEntityRecursive(compoundTag, level, Function.identity());
        }
        return this.displayEntity;
    }

    public CompoundTag getUpdateTag(TrialSpawnerState trialSpawnerState) {
        CompoundTag compoundTag = new CompoundTag();
        if (trialSpawnerState == TrialSpawnerState.ACTIVE) {
            compoundTag.putLong(TAG_NEXT_MOB_SPAWNS_AT, this.nextMobSpawnsAt);
        }
        this.nextSpawnData.ifPresent(spawnData -> compoundTag.put(TAG_SPAWN_DATA, (Tag)SpawnData.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, spawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))));
        return compoundTag;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, BlockPos blockPos) {
        long l;
        LootParams lootParams;
        if (this.dispensing != null) {
            return this.dispensing;
        }
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(trialSpawnerConfig.itemsToDropWhenOminous());
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY), l = TrialSpawnerData.lowResolutionPosition(serverLevel, blockPos));
        if (objectArrayList.isEmpty()) {
            return SimpleWeightedRandomList.empty();
        }
        SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder<ItemStack>();
        for (ItemStack itemStack : objectArrayList) {
            builder.add(itemStack.copyWithCount(1), itemStack.getCount());
        }
        this.dispensing = builder.build();
        return this.dispensing;
    }

    private static long lowResolutionPosition(ServerLevel serverLevel, BlockPos blockPos) {
        BlockPos blockPos2 = new BlockPos(Mth.floor((float)blockPos.getX() / 30.0f), Mth.floor((float)blockPos.getY() / 20.0f), Mth.floor((float)blockPos.getZ() / 30.0f));
        return serverLevel.getSeed() + blockPos2.asLong();
    }
}

