/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class Raid {
    private static final int SECTION_RADIUS_FOR_FINDING_NEW_VILLAGE_CENTER = 2;
    private static final int ATTEMPT_RAID_FARTHEST = 0;
    private static final int ATTEMPT_RAID_CLOSE = 1;
    private static final int ATTEMPT_RAID_INSIDE = 2;
    private static final int VILLAGE_SEARCH_RADIUS = 32;
    private static final int RAID_TIMEOUT_TICKS = 48000;
    private static final int NUM_SPAWN_ATTEMPTS = 3;
    private static final Component OMINOUS_BANNER_PATTERN_NAME = Component.translatable("block.minecraft.ominous_banner").withStyle(ChatFormatting.GOLD);
    private static final String RAIDERS_REMAINING = "event.minecraft.raid.raiders_remaining";
    public static final int VILLAGE_RADIUS_BUFFER = 16;
    private static final int POST_RAID_TICK_LIMIT = 40;
    private static final int DEFAULT_PRE_RAID_TICKS = 300;
    public static final int MAX_NO_ACTION_TIME = 2400;
    public static final int MAX_CELEBRATION_TICKS = 600;
    private static final int OUTSIDE_RAID_BOUNDS_TIMEOUT = 30;
    public static final int TICKS_PER_DAY = 24000;
    public static final int DEFAULT_MAX_RAID_OMEN_LEVEL = 5;
    private static final int LOW_MOB_THRESHOLD = 2;
    private static final Component RAID_NAME_COMPONENT = Component.translatable("event.minecraft.raid");
    private static final Component RAID_BAR_VICTORY_COMPONENT = Component.translatable("event.minecraft.raid.victory.full");
    private static final Component RAID_BAR_DEFEAT_COMPONENT = Component.translatable("event.minecraft.raid.defeat.full");
    private static final int HERO_OF_THE_VILLAGE_DURATION = 48000;
    public static final int VALID_RAID_RADIUS_SQR = 9216;
    public static final int RAID_REMOVAL_THRESHOLD_SQR = 12544;
    private final Map<Integer, Raider> groupToLeaderMap = Maps.newHashMap();
    private final Map<Integer, Set<Raider>> groupRaiderMap = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerLevel level;
    private boolean started;
    private final int id;
    private float totalHealth;
    private int raidOmenLevel;
    private boolean active;
    private int groupsSpawned;
    private final ServerBossEvent raidEvent = new ServerBossEvent(RAID_NAME_COMPONENT, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private int postRaidTicks;
    private int raidCooldownTicks;
    private final RandomSource random = RandomSource.create();
    private final int numGroups;
    private RaidStatus status;
    private int celebrationTicks;
    private Optional<BlockPos> waveSpawnPos = Optional.empty();

    public Raid(int n, ServerLevel serverLevel, BlockPos blockPos) {
        this.id = n;
        this.level = serverLevel;
        this.active = true;
        this.raidCooldownTicks = 300;
        this.raidEvent.setProgress(0.0f);
        this.center = blockPos;
        this.numGroups = this.getNumGroups(serverLevel.getDifficulty());
        this.status = RaidStatus.ONGOING;
    }

    public Raid(ServerLevel serverLevel, CompoundTag compoundTag) {
        this.level = serverLevel;
        this.id = compoundTag.getInt("Id");
        this.started = compoundTag.getBoolean("Started");
        this.active = compoundTag.getBoolean("Active");
        this.ticksActive = compoundTag.getLong("TicksActive");
        this.raidOmenLevel = compoundTag.getInt("BadOmenLevel");
        this.groupsSpawned = compoundTag.getInt("GroupsSpawned");
        this.raidCooldownTicks = compoundTag.getInt("PreRaidTicks");
        this.postRaidTicks = compoundTag.getInt("PostRaidTicks");
        this.totalHealth = compoundTag.getFloat("TotalHealth");
        this.center = new BlockPos(compoundTag.getInt("CX"), compoundTag.getInt("CY"), compoundTag.getInt("CZ"));
        this.numGroups = compoundTag.getInt("NumGroups");
        this.status = RaidStatus.getByName(compoundTag.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (compoundTag.contains("HeroesOfTheVillage", 9)) {
            ListTag listTag = compoundTag.getList("HeroesOfTheVillage", 11);
            for (Tag tag : listTag) {
                this.heroesOfTheVillage.add(NbtUtils.loadUUID(tag));
            }
        }
    }

    public boolean isOver() {
        return this.isVictory() || this.isLoss();
    }

    public boolean isBetweenWaves() {
        return this.hasFirstWaveSpawned() && this.getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
    }

    public boolean hasFirstWaveSpawned() {
        return this.groupsSpawned > 0;
    }

    public boolean isStopped() {
        return this.status == RaidStatus.STOPPED;
    }

    public boolean isVictory() {
        return this.status == RaidStatus.VICTORY;
    }

    public boolean isLoss() {
        return this.status == RaidStatus.LOSS;
    }

    public float getTotalHealth() {
        return this.totalHealth;
    }

    public Set<Raider> getAllRaiders() {
        HashSet hashSet = Sets.newHashSet();
        for (Set<Raider> set : this.groupRaiderMap.values()) {
            hashSet.addAll(set);
        }
        return hashSet;
    }

    public Level getLevel() {
        return this.level;
    }

    public boolean isStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.groupsSpawned;
    }

    private Predicate<ServerPlayer> validPlayer() {
        return serverPlayer -> {
            BlockPos blockPos = serverPlayer.blockPosition();
            return serverPlayer.isAlive() && this.level.getRaidAt(blockPos) == this;
        };
    }

    private void updatePlayers() {
        HashSet hashSet = Sets.newHashSet(this.raidEvent.getPlayers());
        List<ServerPlayer> list = this.level.getPlayers(this.validPlayer());
        for (ServerPlayer serverPlayer : list) {
            if (hashSet.contains(serverPlayer)) continue;
            this.raidEvent.addPlayer(serverPlayer);
        }
        for (ServerPlayer serverPlayer : hashSet) {
            if (list.contains(serverPlayer)) continue;
            this.raidEvent.removePlayer(serverPlayer);
        }
    }

    public int getMaxRaidOmenLevel() {
        return 5;
    }

    public int getRaidOmenLevel() {
        return this.raidOmenLevel;
    }

    public void setRaidOmenLevel(int n) {
        this.raidOmenLevel = n;
    }

    public boolean absorbRaidOmen(ServerPlayer serverPlayer) {
        MobEffectInstance mobEffectInstance = serverPlayer.getEffect(MobEffects.RAID_OMEN);
        if (mobEffectInstance == null) {
            return false;
        }
        this.raidOmenLevel += mobEffectInstance.getAmplifier() + 1;
        this.raidOmenLevel = Mth.clamp(this.raidOmenLevel, 0, this.getMaxRaidOmenLevel());
        if (!this.hasFirstWaveSpawned()) {
            serverPlayer.awardStat(Stats.RAID_TRIGGER);
            CriteriaTriggers.RAID_OMEN.trigger(serverPlayer);
        }
        return true;
    }

    public void stop() {
        this.active = false;
        this.raidEvent.removeAllPlayers();
        this.status = RaidStatus.STOPPED;
    }

    public void tick() {
        if (this.isStopped()) {
            return;
        }
        if (this.status == RaidStatus.ONGOING) {
            int n;
            boolean bl;
            boolean bl2 = this.active;
            this.active = this.level.hasChunkAt(this.center);
            if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                this.stop();
                return;
            }
            if (bl2 != this.active) {
                this.raidEvent.setVisible(this.active);
            }
            if (!this.active) {
                return;
            }
            if (!this.level.isVillage(this.center)) {
                this.moveRaidCenterToNearbyVillageSection();
            }
            if (!this.level.isVillage(this.center)) {
                if (this.groupsSpawned > 0) {
                    this.status = RaidStatus.LOSS;
                } else {
                    this.stop();
                }
            }
            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
                this.stop();
                return;
            }
            int n2 = this.getTotalRaidersAlive();
            if (n2 == 0 && this.hasMoreWaves()) {
                if (this.raidCooldownTicks > 0) {
                    bl = this.waveSpawnPos.isPresent();
                    int n3 = n = !bl && this.raidCooldownTicks % 5 == 0 ? 1 : 0;
                    if (bl && !this.level.isPositionEntityTicking(this.waveSpawnPos.get())) {
                        n = 1;
                    }
                    if (n != 0) {
                        int n4 = 0;
                        if (this.raidCooldownTicks < 100) {
                            n4 = 1;
                        } else if (this.raidCooldownTicks < 40) {
                            n4 = 2;
                        }
                        this.waveSpawnPos = this.getValidSpawnPos(n4);
                    }
                    if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                        this.updatePlayers();
                    }
                    --this.raidCooldownTicks;
                    this.raidEvent.setProgress(Mth.clamp((float)(300 - this.raidCooldownTicks) / 300.0f, 0.0f, 1.0f));
                } else if (this.raidCooldownTicks == 0 && this.groupsSpawned > 0) {
                    this.raidCooldownTicks = 300;
                    this.raidEvent.setName(RAID_NAME_COMPONENT);
                    return;
                }
            }
            if (this.ticksActive % 20L == 0L) {
                this.updatePlayers();
                this.updateRaiders();
                if (n2 > 0) {
                    if (n2 <= 2) {
                        this.raidEvent.setName(RAID_NAME_COMPONENT.copy().append(" - ").append(Component.translatable(RAIDERS_REMAINING, n2)));
                    } else {
                        this.raidEvent.setName(RAID_NAME_COMPONENT);
                    }
                } else {
                    this.raidEvent.setName(RAID_NAME_COMPONENT);
                }
            }
            bl = false;
            n = 0;
            while (this.shouldSpawnGroup()) {
                BlockPos blockPos;
                BlockPos blockPos2 = blockPos = this.waveSpawnPos.isPresent() ? this.waveSpawnPos.get() : this.findRandomSpawnPos(n, 20);
                if (blockPos != null) {
                    this.started = true;
                    this.spawnGroup(blockPos);
                    if (!bl) {
                        this.playSound(blockPos);
                        bl = true;
                    }
                } else {
                    ++n;
                }
                if (n <= 3) continue;
                this.stop();
                break;
            }
            if (this.isStarted() && !this.hasMoreWaves() && n2 == 0) {
                if (this.postRaidTicks < 40) {
                    ++this.postRaidTicks;
                } else {
                    this.status = RaidStatus.VICTORY;
                    for (UUID uUID : this.heroesOfTheVillage) {
                        Entity entity = this.level.getEntity(uUID);
                        if (!(entity instanceof LivingEntity)) continue;
                        LivingEntity livingEntity = (LivingEntity)entity;
                        if (entity.isSpectator()) continue;
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.raidOmenLevel - 1, false, false, true));
                        if (!(livingEntity instanceof ServerPlayer)) continue;
                        ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                        serverPlayer.awardStat(Stats.RAID_WIN);
                        CriteriaTriggers.RAID_WIN.trigger(serverPlayer);
                    }
                }
            }
            this.setDirty();
        } else if (this.isOver()) {
            ++this.celebrationTicks;
            if (this.celebrationTicks >= 600) {
                this.stop();
                return;
            }
            if (this.celebrationTicks % 20 == 0) {
                this.updatePlayers();
                this.raidEvent.setVisible(true);
                if (this.isVictory()) {
                    this.raidEvent.setProgress(0.0f);
                    this.raidEvent.setName(RAID_BAR_VICTORY_COMPONENT);
                } else {
                    this.raidEvent.setName(RAID_BAR_DEFEAT_COMPONENT);
                }
            }
        }
    }

    private void moveRaidCenterToNearbyVillageSection() {
        Stream<SectionPos> stream = SectionPos.cube(SectionPos.of(this.center), 2);
        stream.filter(this.level::isVillage).map(SectionPos::center).min(Comparator.comparingDouble(blockPos -> blockPos.distSqr(this.center))).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> getValidSpawnPos(int n) {
        for (int i = 0; i < 3; ++i) {
            BlockPos blockPos = this.findRandomSpawnPos(n, 1);
            if (blockPos == null) continue;
            return Optional.of(blockPos);
        }
        return Optional.empty();
    }

    private boolean hasMoreWaves() {
        if (this.hasBonusWave()) {
            return !this.hasSpawnedBonusWave();
        }
        return !this.isFinalWave();
    }

    private boolean isFinalWave() {
        return this.getGroupsSpawned() == this.numGroups;
    }

    private boolean hasBonusWave() {
        return this.raidOmenLevel > 1;
    }

    private boolean hasSpawnedBonusWave() {
        return this.getGroupsSpawned() > this.numGroups;
    }

    private boolean shouldSpawnBonusGroup() {
        return this.isFinalWave() && this.getTotalRaidersAlive() == 0 && this.hasBonusWave();
    }

    private void updateRaiders() {
        Iterator<Set<Raider>> iterator = this.groupRaiderMap.values().iterator();
        HashSet hashSet = Sets.newHashSet();
        while (iterator.hasNext()) {
            Set<Raider> set = iterator.next();
            Object object = set.iterator();
            while (object.hasNext()) {
                Raider raider = (Raider)object.next();
                BlockPos blockPos = raider.blockPosition();
                if (raider.isRemoved() || raider.level().dimension() != this.level.dimension() || this.center.distSqr(blockPos) >= 12544.0) {
                    hashSet.add(raider);
                    continue;
                }
                if (raider.tickCount <= 600) continue;
                if (this.level.getEntity(raider.getUUID()) == null) {
                    hashSet.add(raider);
                }
                if (!this.level.isVillage(blockPos) && raider.getNoActionTime() > 2400) {
                    raider.setTicksOutsideRaid(raider.getTicksOutsideRaid() + 1);
                }
                if (raider.getTicksOutsideRaid() < 30) continue;
                hashSet.add(raider);
            }
        }
        for (Object object : hashSet) {
            this.removeFromRaid((Raider)object, true);
        }
    }

    private void playSound(BlockPos blockPos) {
        float f = 13.0f;
        int n = 64;
        Collection<ServerPlayer> collection = this.raidEvent.getPlayers();
        long l = this.random.nextLong();
        for (ServerPlayer serverPlayer : this.level.players()) {
            Vec3 vec3 = serverPlayer.position();
            Vec3 vec32 = Vec3.atCenterOf(blockPos);
            double d = Math.sqrt((vec32.x - vec3.x) * (vec32.x - vec3.x) + (vec32.z - vec3.z) * (vec32.z - vec3.z));
            double d2 = vec3.x + 13.0 / d * (vec32.x - vec3.x);
            double d3 = vec3.z + 13.0 / d * (vec32.z - vec3.z);
            if (!(d <= 64.0) && !collection.contains(serverPlayer)) continue;
            serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.RAID_HORN, SoundSource.NEUTRAL, d2, serverPlayer.getY(), d3, 64.0f, 1.0f, l));
        }
    }

    private void spawnGroup(BlockPos blockPos) {
        boolean bl = false;
        int n = this.groupsSpawned + 1;
        this.totalHealth = 0.0f;
        DifficultyInstance difficultyInstance = this.level.getCurrentDifficultyAt(blockPos);
        boolean bl2 = this.shouldSpawnBonusGroup();
        for (RaiderType raiderType : RaiderType.VALUES) {
            Raider raider;
            int n2 = this.getDefaultNumSpawns(raiderType, n, bl2) + this.getPotentialBonusSpawns(raiderType, this.random, n, difficultyInstance, bl2);
            int n3 = 0;
            for (int i = 0; i < n2 && (raider = raiderType.entityType.create(this.level)) != null; ++i) {
                if (!bl && raider.canBeLeader()) {
                    raider.setPatrolLeader(true);
                    this.setLeader(n, raider);
                    bl = true;
                }
                this.joinRaid(n, raider, blockPos, false);
                if (raiderType.entityType != EntityType.RAVAGER) continue;
                Raider raider2 = null;
                if (n == this.getNumGroups(Difficulty.NORMAL)) {
                    raider2 = EntityType.PILLAGER.create(this.level);
                } else if (n >= this.getNumGroups(Difficulty.HARD)) {
                    raider2 = n3 == 0 ? (Raider)EntityType.EVOKER.create(this.level) : (Raider)EntityType.VINDICATOR.create(this.level);
                }
                ++n3;
                if (raider2 == null) continue;
                this.joinRaid(n, raider2, blockPos, false);
                raider2.moveTo(blockPos, 0.0f, 0.0f);
                raider2.startRiding(raider);
            }
        }
        this.waveSpawnPos = Optional.empty();
        ++this.groupsSpawned;
        this.updateBossbar();
        this.setDirty();
    }

    public void joinRaid(int n, Raider raider, @Nullable BlockPos blockPos, boolean bl) {
        boolean bl2 = this.addWaveMob(n, raider);
        if (bl2) {
            raider.setCurrentRaid(this);
            raider.setWave(n);
            raider.setCanJoinRaid(true);
            raider.setTicksOutsideRaid(0);
            if (!bl && blockPos != null) {
                raider.setPos((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
                raider.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(blockPos), MobSpawnType.EVENT, null);
                raider.applyRaidBuffs(this.level, n, false);
                raider.setOnGround(true);
                this.level.addFreshEntityWithPassengers(raider);
            }
        }
    }

    public void updateBossbar() {
        this.raidEvent.setProgress(Mth.clamp(this.getHealthOfLivingRaiders() / this.totalHealth, 0.0f, 1.0f));
    }

    public float getHealthOfLivingRaiders() {
        float f = 0.0f;
        for (Set<Raider> set : this.groupRaiderMap.values()) {
            for (Raider raider : set) {
                f += raider.getHealth();
            }
        }
        return f;
    }

    private boolean shouldSpawnGroup() {
        return this.raidCooldownTicks == 0 && (this.groupsSpawned < this.numGroups || this.shouldSpawnBonusGroup()) && this.getTotalRaidersAlive() == 0;
    }

    public int getTotalRaidersAlive() {
        return this.groupRaiderMap.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromRaid(Raider raider, boolean bl) {
        boolean bl2;
        Set<Raider> set = this.groupRaiderMap.get(raider.getWave());
        if (set != null && (bl2 = set.remove(raider))) {
            if (bl) {
                this.totalHealth -= raider.getHealth();
            }
            raider.setCurrentRaid(null);
            this.updateBossbar();
            this.setDirty();
        }
    }

    private void setDirty() {
        this.level.getRaids().setDirty();
    }

    public static ItemStack getLeaderBannerInstance(HolderGetter<BannerPattern> holderGetter) {
        ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
        BannerPatternLayers bannerPatternLayers = new BannerPatternLayers.Builder().addIfRegistered(holderGetter, BannerPatterns.RHOMBUS_MIDDLE, DyeColor.CYAN).addIfRegistered(holderGetter, BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).addIfRegistered(holderGetter, BannerPatterns.STRIPE_CENTER, DyeColor.GRAY).addIfRegistered(holderGetter, BannerPatterns.BORDER, DyeColor.LIGHT_GRAY).addIfRegistered(holderGetter, BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK).addIfRegistered(holderGetter, BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).addIfRegistered(holderGetter, BannerPatterns.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY).addIfRegistered(holderGetter, BannerPatterns.BORDER, DyeColor.BLACK).build();
        itemStack.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers);
        itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        itemStack.set(DataComponents.ITEM_NAME, OMINOUS_BANNER_PATTERN_NAME);
        return itemStack;
    }

    @Nullable
    public Raider getLeader(int n) {
        return this.groupToLeaderMap.get(n);
    }

    @Nullable
    private BlockPos findRandomSpawnPos(int n, int n2) {
        int n3 = n == 0 ? 2 : 2 - n;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        SpawnPlacementType spawnPlacementType = SpawnPlacements.getPlacementType(EntityType.RAVAGER);
        for (int i = 0; i < n2; ++i) {
            float f = this.level.random.nextFloat() * ((float)Math.PI * 2);
            int n4 = this.center.getX() + Mth.floor(Mth.cos(f) * 32.0f * (float)n3) + this.level.random.nextInt(5);
            int n5 = this.center.getZ() + Mth.floor(Mth.sin(f) * 32.0f * (float)n3) + this.level.random.nextInt(5);
            int n6 = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, n4, n5);
            mutableBlockPos.set(n4, n6, n5);
            if (this.level.isVillage(mutableBlockPos) && n < 2) continue;
            int n7 = 10;
            if (!this.level.hasChunksAt(mutableBlockPos.getX() - 10, mutableBlockPos.getZ() - 10, mutableBlockPos.getX() + 10, mutableBlockPos.getZ() + 10) || !this.level.isPositionEntityTicking(mutableBlockPos) || !spawnPlacementType.isSpawnPositionOk(this.level, mutableBlockPos, EntityType.RAVAGER) && (!this.level.getBlockState((BlockPos)mutableBlockPos.below()).is(Blocks.SNOW) || !this.level.getBlockState(mutableBlockPos).isAir())) continue;
            return mutableBlockPos;
        }
        return null;
    }

    private boolean addWaveMob(int n, Raider raider) {
        return this.addWaveMob(n, raider, true);
    }

    public boolean addWaveMob(int n2, Raider raider, boolean bl) {
        this.groupRaiderMap.computeIfAbsent(n2, n -> Sets.newHashSet());
        Set<Raider> set = this.groupRaiderMap.get(n2);
        Raider raider2 = null;
        for (Raider raider3 : set) {
            if (!raider3.getUUID().equals(raider.getUUID())) continue;
            raider2 = raider3;
            break;
        }
        if (raider2 != null) {
            set.remove(raider2);
            set.add(raider);
        }
        set.add(raider);
        if (bl) {
            this.totalHealth += raider.getHealth();
        }
        this.updateBossbar();
        this.setDirty();
        return true;
    }

    public void setLeader(int n, Raider raider) {
        this.groupToLeaderMap.put(n, raider);
        raider.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance(raider.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
        raider.setDropChance(EquipmentSlot.HEAD, 2.0f);
    }

    public void removeLeader(int n) {
        this.groupToLeaderMap.remove(n);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos blockPos) {
        this.center = blockPos;
    }

    public int getId() {
        return this.id;
    }

    private int getDefaultNumSpawns(RaiderType raiderType, int n, boolean bl) {
        return bl ? raiderType.spawnsPerWaveBeforeBonus[this.numGroups] : raiderType.spawnsPerWaveBeforeBonus[n];
    }

    private int getPotentialBonusSpawns(RaiderType raiderType, RandomSource randomSource, int n, DifficultyInstance difficultyInstance, boolean bl) {
        int n2;
        Difficulty difficulty = difficultyInstance.getDifficulty();
        boolean bl2 = difficulty == Difficulty.EASY;
        boolean bl3 = difficulty == Difficulty.NORMAL;
        switch (raiderType.ordinal()) {
            case 3: {
                if (!bl2 && n > 2 && n != 4) {
                    n2 = 1;
                    break;
                }
                return 0;
            }
            case 0: 
            case 2: {
                if (bl2) {
                    n2 = randomSource.nextInt(2);
                    break;
                }
                if (bl3) {
                    n2 = 1;
                    break;
                }
                n2 = 2;
                break;
            }
            case 4: {
                n2 = !bl2 && bl ? 1 : 0;
                break;
            }
            default: {
                return 0;
            }
        }
        return n2 > 0 ? randomSource.nextInt(n2 + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("Id", this.id);
        compoundTag.putBoolean("Started", this.started);
        compoundTag.putBoolean("Active", this.active);
        compoundTag.putLong("TicksActive", this.ticksActive);
        compoundTag.putInt("BadOmenLevel", this.raidOmenLevel);
        compoundTag.putInt("GroupsSpawned", this.groupsSpawned);
        compoundTag.putInt("PreRaidTicks", this.raidCooldownTicks);
        compoundTag.putInt("PostRaidTicks", this.postRaidTicks);
        compoundTag.putFloat("TotalHealth", this.totalHealth);
        compoundTag.putInt("NumGroups", this.numGroups);
        compoundTag.putString("Status", this.status.getName());
        compoundTag.putInt("CX", this.center.getX());
        compoundTag.putInt("CY", this.center.getY());
        compoundTag.putInt("CZ", this.center.getZ());
        ListTag listTag = new ListTag();
        for (UUID uUID : this.heroesOfTheVillage) {
            listTag.add(NbtUtils.createUUID(uUID));
        }
        compoundTag.put("HeroesOfTheVillage", listTag);
        return compoundTag;
    }

    public int getNumGroups(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: {
                return 3;
            }
            case NORMAL: {
                return 5;
            }
            case HARD: {
                return 7;
            }
        }
        return 0;
    }

    public float getEnchantOdds() {
        int n = this.getRaidOmenLevel();
        if (n == 2) {
            return 0.1f;
        }
        if (n == 3) {
            return 0.25f;
        }
        if (n == 4) {
            return 0.5f;
        }
        if (n == 5) {
            return 0.75f;
        }
        return 0.0f;
    }

    public void addHeroOfTheVillage(Entity entity) {
        this.heroesOfTheVillage.add(entity.getUUID());
    }

    static enum RaidStatus {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final RaidStatus[] VALUES;

        static RaidStatus getByName(String string) {
            for (RaidStatus raidStatus : VALUES) {
                if (!string.equalsIgnoreCase(raidStatus.name())) continue;
                return raidStatus;
            }
            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        static {
            VALUES = RaidStatus.values();
        }
    }

    static enum RaiderType {
        VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
        EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
        PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
        WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
        RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        static final RaiderType[] VALUES;
        final EntityType<? extends Raider> entityType;
        final int[] spawnsPerWaveBeforeBonus;

        private RaiderType(EntityType<? extends Raider> entityType, int[] nArray) {
            this.entityType = entityType;
            this.spawnsPerWaveBeforeBonus = nArray;
        }

        static {
            VALUES = RaiderType.values();
        }
    }
}

