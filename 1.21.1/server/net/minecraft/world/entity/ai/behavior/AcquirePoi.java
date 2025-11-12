/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;

public class AcquirePoi {
    public static final int SCAN_RANGE = 48;

    public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memoryModuleType, boolean bl, Optional<Byte> optional) {
        return AcquirePoi.create(predicate, memoryModuleType, memoryModuleType, bl, optional);
    }

    public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memoryModuleType, MemoryModuleType<GlobalPos> memoryModuleType2, boolean bl, Optional<Byte> optional) {
        int n = 5;
        int n2 = 20;
        MutableLong mutableLong = new MutableLong(0L);
        Long2ObjectOpenHashMap long2ObjectOpenHashMap = new Long2ObjectOpenHashMap();
        OneShot<PathfinderMob> oneShot = BehaviorBuilder.create(arg_0 -> AcquirePoi.lambda$create$8(memoryModuleType2, bl, mutableLong, (Long2ObjectMap)long2ObjectOpenHashMap, predicate, optional, arg_0));
        if (memoryModuleType2 == memoryModuleType) {
            return oneShot;
        }
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(memoryModuleType)).apply((Applicative)instance, memoryAccessor -> oneShot));
    }

    @Nullable
    public static Path findPathToPois(Mob mob, Set<Pair<Holder<PoiType>, BlockPos>> set) {
        if (set.isEmpty()) {
            return null;
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        int n = 1;
        for (Pair<Holder<PoiType>, BlockPos> pair : set) {
            n = Math.max(n, ((PoiType)((Holder)pair.getFirst()).value()).validRange());
            hashSet.add((BlockPos)pair.getSecond());
        }
        return mob.getNavigation().createPath(hashSet, n);
    }

    private static /* synthetic */ App lambda$create$8(MemoryModuleType memoryModuleType, boolean bl, MutableLong mutableLong, Long2ObjectMap long2ObjectMap, Predicate predicate, Optional optional, BehaviorBuilder.Instance instance) {
        return instance.group(instance.absent(memoryModuleType)).apply((Applicative)instance, memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
            if (bl && pathfinderMob.isBaby()) {
                return false;
            }
            if (mutableLong.getValue() == 0L) {
                mutableLong.setValue(serverLevel.getGameTime() + (long)serverLevel.random.nextInt(20));
                return false;
            }
            if (serverLevel.getGameTime() < mutableLong.getValue()) {
                return false;
            }
            mutableLong.setValue(l + 20L + (long)serverLevel.getRandom().nextInt(20));
            PoiManager poiManager = serverLevel.getPoiManager();
            long2ObjectMap.long2ObjectEntrySet().removeIf(entry -> !((JitteredLinearRetry)entry.getValue()).isStillValid(l));
            Predicate<BlockPos> predicate2 = blockPos -> {
                JitteredLinearRetry jitteredLinearRetry = (JitteredLinearRetry)long2ObjectMap.get(blockPos.asLong());
                if (jitteredLinearRetry == null) {
                    return true;
                }
                if (!jitteredLinearRetry.shouldRetry(l)) {
                    return false;
                }
                jitteredLinearRetry.markAttempt(l);
                return true;
            };
            Set<Pair<Holder<PoiType>, BlockPos>> set = poiManager.findAllClosestFirstWithType(predicate, predicate2, pathfinderMob.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE).limit(5L).collect(Collectors.toSet());
            Path path = AcquirePoi.findPathToPois(pathfinderMob, set);
            if (path != null && path.canReach()) {
                BlockPos blockPos2 = path.getTarget();
                poiManager.getType(blockPos2).ifPresent(holder2 -> {
                    poiManager.take(predicate, (holder, blockPos2) -> blockPos2.equals(blockPos2), blockPos2, 1);
                    memoryAccessor.set(GlobalPos.of(serverLevel.dimension(), blockPos2));
                    optional.ifPresent(by -> serverLevel.broadcastEntityEvent(pathfinderMob, (byte)by));
                    long2ObjectMap.clear();
                    DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos2);
                });
            } else {
                for (Pair<Holder<PoiType>, BlockPos> pair : set) {
                    long2ObjectMap.computeIfAbsent(((BlockPos)pair.getSecond()).asLong(), l2 -> new JitteredLinearRetry(serverLevel.random, l));
                }
            }
            return true;
        });
    }

    static class JitteredLinearRetry {
        private static final int MIN_INTERVAL_INCREASE = 40;
        private static final int MAX_INTERVAL_INCREASE = 80;
        private static final int MAX_RETRY_PATHFINDING_INTERVAL = 400;
        private final RandomSource random;
        private long previousAttemptTimestamp;
        private long nextScheduledAttemptTimestamp;
        private int currentDelay;

        JitteredLinearRetry(RandomSource randomSource, long l) {
            this.random = randomSource;
            this.markAttempt(l);
        }

        public void markAttempt(long l) {
            this.previousAttemptTimestamp = l;
            int n = this.currentDelay + this.random.nextInt(40) + 40;
            this.currentDelay = Math.min(n, 400);
            this.nextScheduledAttemptTimestamp = l + (long)this.currentDelay;
        }

        public boolean isStillValid(long l) {
            return l - this.previousAttemptTimestamp < 400L;
        }

        public boolean shouldRetry(long l) {
            return l >= this.nextScheduledAttemptTimestamp;
        }

        public String toString() {
            return "RetryMarker{, previousAttemptAt=" + this.previousAttemptTimestamp + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptTimestamp + ", currentDelay=" + this.currentDelay + "}";
        }
    }
}

