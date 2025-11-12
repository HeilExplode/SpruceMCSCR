/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSelector;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface VibrationSystem {
    public static final List<ResourceKey<GameEvent>> RESONANCE_EVENTS = List.of(GameEvent.RESONATE_1.key(), GameEvent.RESONATE_2.key(), GameEvent.RESONATE_3.key(), GameEvent.RESONATE_4.key(), GameEvent.RESONATE_5.key(), GameEvent.RESONATE_6.key(), GameEvent.RESONATE_7.key(), GameEvent.RESONATE_8.key(), GameEvent.RESONATE_9.key(), GameEvent.RESONATE_10.key(), GameEvent.RESONATE_11.key(), GameEvent.RESONATE_12.key(), GameEvent.RESONATE_13.key(), GameEvent.RESONATE_14.key(), GameEvent.RESONATE_15.key());
    public static final int DEFAULT_VIBRATION_FREQUENCY = 0;
    public static final ToIntFunction<ResourceKey<GameEvent>> VIBRATION_FREQUENCY_FOR_EVENT = (ToIntFunction)Util.make(new Reference2IntOpenHashMap(), reference2IntOpenHashMap -> {
        reference2IntOpenHashMap.defaultReturnValue(0);
        reference2IntOpenHashMap.put(GameEvent.STEP.key(), 1);
        reference2IntOpenHashMap.put(GameEvent.SWIM.key(), 1);
        reference2IntOpenHashMap.put(GameEvent.FLAP.key(), 1);
        reference2IntOpenHashMap.put(GameEvent.PROJECTILE_LAND.key(), 2);
        reference2IntOpenHashMap.put(GameEvent.HIT_GROUND.key(), 2);
        reference2IntOpenHashMap.put(GameEvent.SPLASH.key(), 2);
        reference2IntOpenHashMap.put(GameEvent.ITEM_INTERACT_FINISH.key(), 3);
        reference2IntOpenHashMap.put(GameEvent.PROJECTILE_SHOOT.key(), 3);
        reference2IntOpenHashMap.put(GameEvent.INSTRUMENT_PLAY.key(), 3);
        reference2IntOpenHashMap.put(GameEvent.ENTITY_ACTION.key(), 4);
        reference2IntOpenHashMap.put(GameEvent.ELYTRA_GLIDE.key(), 4);
        reference2IntOpenHashMap.put(GameEvent.UNEQUIP.key(), 4);
        reference2IntOpenHashMap.put(GameEvent.ENTITY_DISMOUNT.key(), 5);
        reference2IntOpenHashMap.put(GameEvent.EQUIP.key(), 5);
        reference2IntOpenHashMap.put(GameEvent.ENTITY_INTERACT.key(), 6);
        reference2IntOpenHashMap.put(GameEvent.SHEAR.key(), 6);
        reference2IntOpenHashMap.put(GameEvent.ENTITY_MOUNT.key(), 6);
        reference2IntOpenHashMap.put(GameEvent.ENTITY_DAMAGE.key(), 7);
        reference2IntOpenHashMap.put(GameEvent.DRINK.key(), 8);
        reference2IntOpenHashMap.put(GameEvent.EAT.key(), 8);
        reference2IntOpenHashMap.put(GameEvent.CONTAINER_CLOSE.key(), 9);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_CLOSE.key(), 9);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_DEACTIVATE.key(), 9);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_DETACH.key(), 9);
        reference2IntOpenHashMap.put(GameEvent.CONTAINER_OPEN.key(), 10);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_OPEN.key(), 10);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_ACTIVATE.key(), 10);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_ATTACH.key(), 10);
        reference2IntOpenHashMap.put(GameEvent.PRIME_FUSE.key(), 10);
        reference2IntOpenHashMap.put(GameEvent.NOTE_BLOCK_PLAY.key(), 10);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_CHANGE.key(), 11);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_DESTROY.key(), 12);
        reference2IntOpenHashMap.put(GameEvent.FLUID_PICKUP.key(), 12);
        reference2IntOpenHashMap.put(GameEvent.BLOCK_PLACE.key(), 13);
        reference2IntOpenHashMap.put(GameEvent.FLUID_PLACE.key(), 13);
        reference2IntOpenHashMap.put(GameEvent.ENTITY_PLACE.key(), 14);
        reference2IntOpenHashMap.put(GameEvent.LIGHTNING_STRIKE.key(), 14);
        reference2IntOpenHashMap.put(GameEvent.TELEPORT.key(), 14);
        reference2IntOpenHashMap.put(GameEvent.ENTITY_DIE.key(), 15);
        reference2IntOpenHashMap.put(GameEvent.EXPLODE.key(), 15);
        for (int i = 1; i <= 15; ++i) {
            reference2IntOpenHashMap.put(VibrationSystem.getResonanceEventByFrequency(i), i);
        }
    });

    public Data getVibrationData();

    public User getVibrationUser();

    public static int getGameEventFrequency(Holder<GameEvent> holder) {
        return holder.unwrapKey().map(VibrationSystem::getGameEventFrequency).orElse(0);
    }

    public static int getGameEventFrequency(ResourceKey<GameEvent> resourceKey) {
        return VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt(resourceKey);
    }

    public static ResourceKey<GameEvent> getResonanceEventByFrequency(int n) {
        return RESONANCE_EVENTS.get(n - 1);
    }

    public static int getRedstoneStrengthForDistance(float f, int n) {
        double d = 15.0 / (double)n;
        return Math.max(1, 15 - Mth.floor(d * (double)f));
    }

    public static interface User {
        public int getListenerRadius();

        public PositionSource getPositionSource();

        public boolean canReceiveVibration(ServerLevel var1, BlockPos var2, Holder<GameEvent> var3, GameEvent.Context var4);

        public void onReceiveVibration(ServerLevel var1, BlockPos var2, Holder<GameEvent> var3, @Nullable Entity var4, @Nullable Entity var5, float var6);

        default public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
        }

        default public boolean canTriggerAvoidVibration() {
            return false;
        }

        default public boolean requiresAdjacentChunksToBeTicking() {
            return false;
        }

        default public int calculateTravelTimeInTicks(float f) {
            return Mth.floor(f);
        }

        default public boolean isValidVibration(Holder<GameEvent> holder, GameEvent.Context context) {
            if (!holder.is(this.getListenableEvents())) {
                return false;
            }
            Entity entity = context.sourceEntity();
            if (entity != null) {
                if (entity.isSpectator()) {
                    return false;
                }
                if (entity.isSteppingCarefully() && holder.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                    if (this.canTriggerAvoidVibration() && entity instanceof ServerPlayer) {
                        ServerPlayer serverPlayer = (ServerPlayer)entity;
                        CriteriaTriggers.AVOID_VIBRATION.trigger(serverPlayer);
                    }
                    return false;
                }
                if (entity.dampensVibrations()) {
                    return false;
                }
            }
            if (context.affectedState() != null) {
                return !context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
            }
            return true;
        }

        default public void onDataChanged() {
        }
    }

    public static interface Ticker {
        public static void tick(Level level, Data data, User user) {
            if (!(level instanceof ServerLevel)) {
                return;
            }
            ServerLevel serverLevel = (ServerLevel)level;
            if (data.currentVibration == null) {
                Ticker.trySelectAndScheduleVibration(serverLevel, data, user);
            }
            if (data.currentVibration == null) {
                return;
            }
            boolean bl = data.getTravelTimeInTicks() > 0;
            Ticker.tryReloadVibrationParticle(serverLevel, data, user);
            data.decrementTravelTime();
            if (data.getTravelTimeInTicks() <= 0) {
                bl = Ticker.receiveVibration(serverLevel, data, user, data.currentVibration);
            }
            if (bl) {
                user.onDataChanged();
            }
        }

        private static void trySelectAndScheduleVibration(ServerLevel serverLevel, Data data, User user) {
            data.getSelectionStrategy().chosenCandidate(serverLevel.getGameTime()).ifPresent(vibrationInfo -> {
                data.setCurrentVibration((VibrationInfo)vibrationInfo);
                Vec3 vec3 = vibrationInfo.pos();
                data.setTravelTimeInTicks(user.calculateTravelTimeInTicks(vibrationInfo.distance()));
                serverLevel.sendParticles(new VibrationParticleOption(user.getPositionSource(), data.getTravelTimeInTicks()), vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
                user.onDataChanged();
                data.getSelectionStrategy().startOver();
            });
        }

        private static void tryReloadVibrationParticle(ServerLevel serverLevel, Data data, User user) {
            double d;
            double d2;
            int n;
            double d3;
            double d4;
            boolean bl;
            if (!data.shouldReloadVibrationParticle()) {
                return;
            }
            if (data.currentVibration == null) {
                data.setReloadVibrationParticle(false);
                return;
            }
            Vec3 vec3 = data.currentVibration.pos();
            PositionSource positionSource = user.getPositionSource();
            Vec3 vec32 = positionSource.getPosition(serverLevel).orElse(vec3);
            int n2 = data.getTravelTimeInTicks();
            boolean bl2 = bl = serverLevel.sendParticles(new VibrationParticleOption(positionSource, n2), d4 = Mth.lerp(d3 = 1.0 - (double)n2 / (double)(n = user.calculateTravelTimeInTicks(data.currentVibration.distance())), vec3.x, vec32.x), d2 = Mth.lerp(d3, vec3.y, vec32.y), d = Mth.lerp(d3, vec3.z, vec32.z), 1, 0.0, 0.0, 0.0, 0.0) > 0;
            if (bl) {
                data.setReloadVibrationParticle(false);
            }
        }

        private static boolean receiveVibration(ServerLevel serverLevel, Data data, User user, VibrationInfo vibrationInfo) {
            BlockPos blockPos = BlockPos.containing(vibrationInfo.pos());
            BlockPos blockPos2 = user.getPositionSource().getPosition(serverLevel).map(BlockPos::containing).orElse(blockPos);
            if (user.requiresAdjacentChunksToBeTicking() && !Ticker.areAdjacentChunksTicking(serverLevel, blockPos2)) {
                return false;
            }
            user.onReceiveVibration(serverLevel, blockPos, vibrationInfo.gameEvent(), vibrationInfo.getEntity(serverLevel).orElse(null), vibrationInfo.getProjectileOwner(serverLevel).orElse(null), Listener.distanceBetweenInBlocks(blockPos, blockPos2));
            data.setCurrentVibration(null);
            return true;
        }

        private static boolean areAdjacentChunksTicking(Level level, BlockPos blockPos) {
            ChunkPos chunkPos = new ChunkPos(blockPos);
            for (int i = chunkPos.x - 1; i <= chunkPos.x + 1; ++i) {
                for (int j = chunkPos.z - 1; j <= chunkPos.z + 1; ++j) {
                    if (level.shouldTickBlocksAt(ChunkPos.asLong(i, j)) && level.getChunkSource().getChunkNow(i, j) != null) continue;
                    return false;
                }
            }
            return true;
        }
    }

    public static class Listener
    implements GameEventListener {
        private final VibrationSystem system;

        public Listener(VibrationSystem vibrationSystem) {
            this.system = vibrationSystem;
        }

        @Override
        public PositionSource getListenerSource() {
            return this.system.getVibrationUser().getPositionSource();
        }

        @Override
        public int getListenerRadius() {
            return this.system.getVibrationUser().getListenerRadius();
        }

        @Override
        public boolean handleGameEvent(ServerLevel serverLevel, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec3) {
            Data data = this.system.getVibrationData();
            User user = this.system.getVibrationUser();
            if (data.getCurrentVibration() != null) {
                return false;
            }
            if (!user.isValidVibration(holder, context)) {
                return false;
            }
            Optional<Vec3> optional = user.getPositionSource().getPosition(serverLevel);
            if (optional.isEmpty()) {
                return false;
            }
            Vec3 vec32 = optional.get();
            if (!user.canReceiveVibration(serverLevel, BlockPos.containing(vec3), holder, context)) {
                return false;
            }
            if (Listener.isOccluded(serverLevel, vec3, vec32)) {
                return false;
            }
            this.scheduleVibration(serverLevel, data, holder, context, vec3, vec32);
            return true;
        }

        public void forceScheduleVibration(ServerLevel serverLevel, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec3) {
            this.system.getVibrationUser().getPositionSource().getPosition(serverLevel).ifPresent(vec32 -> this.scheduleVibration(serverLevel, this.system.getVibrationData(), holder, context, vec3, (Vec3)vec32));
        }

        private void scheduleVibration(ServerLevel serverLevel, Data data, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec3, Vec3 vec32) {
            data.selectionStrategy.addCandidate(new VibrationInfo(holder, (float)vec3.distanceTo(vec32), vec3, context.sourceEntity()), serverLevel.getGameTime());
        }

        public static float distanceBetweenInBlocks(BlockPos blockPos, BlockPos blockPos2) {
            return (float)Math.sqrt(blockPos.distSqr(blockPos2));
        }

        private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec32) {
            Vec3 vec33 = new Vec3((double)Mth.floor(vec3.x) + 0.5, (double)Mth.floor(vec3.y) + 0.5, (double)Mth.floor(vec3.z) + 0.5);
            Vec3 vec34 = new Vec3((double)Mth.floor(vec32.x) + 0.5, (double)Mth.floor(vec32.y) + 0.5, (double)Mth.floor(vec32.z) + 0.5);
            for (Direction direction : Direction.values()) {
                Vec3 vec35 = vec33.relative(direction, 1.0E-5f);
                if (level.isBlockInLine(new ClipBlockStateContext(vec35, vec34, blockState -> blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType() == HitResult.Type.BLOCK) continue;
                return false;
            }
            return true;
        }
    }

    public static final class Data {
        public static Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)VibrationInfo.CODEC.lenientOptionalFieldOf("event").forGetter(data -> Optional.ofNullable(data.currentVibration)), (App)VibrationSelector.CODEC.fieldOf("selector").forGetter(Data::getSelectionStrategy), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse((Object)0).forGetter(Data::getTravelTimeInTicks)).apply((Applicative)instance, (optional, vibrationSelector, n) -> new Data(optional.orElse(null), (VibrationSelector)vibrationSelector, (int)n, true)));
        public static final String NBT_TAG_KEY = "listener";
        @Nullable
        VibrationInfo currentVibration;
        private int travelTimeInTicks;
        final VibrationSelector selectionStrategy;
        private boolean reloadVibrationParticle;

        private Data(@Nullable VibrationInfo vibrationInfo, VibrationSelector vibrationSelector, int n, boolean bl) {
            this.currentVibration = vibrationInfo;
            this.travelTimeInTicks = n;
            this.selectionStrategy = vibrationSelector;
            this.reloadVibrationParticle = bl;
        }

        public Data() {
            this(null, new VibrationSelector(), 0, false);
        }

        public VibrationSelector getSelectionStrategy() {
            return this.selectionStrategy;
        }

        @Nullable
        public VibrationInfo getCurrentVibration() {
            return this.currentVibration;
        }

        public void setCurrentVibration(@Nullable VibrationInfo vibrationInfo) {
            this.currentVibration = vibrationInfo;
        }

        public int getTravelTimeInTicks() {
            return this.travelTimeInTicks;
        }

        public void setTravelTimeInTicks(int n) {
            this.travelTimeInTicks = n;
        }

        public void decrementTravelTime() {
            this.travelTimeInTicks = Math.max(0, this.travelTimeInTicks - 1);
        }

        public boolean shouldReloadVibrationParticle() {
            return this.reloadVibrationParticle;
        }

        public void setReloadVibrationParticle(boolean bl) {
            this.reloadVibrationParticle = bl;
        }
    }
}

