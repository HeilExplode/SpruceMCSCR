/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity
extends BlockEntity
implements GameEventListener.Provider<VibrationSystem.Listener>,
VibrationSystem {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WARNING_SOUND_RADIUS = 10;
    private static final int WARDEN_SPAWN_ATTEMPTS = 20;
    private static final int WARDEN_SPAWN_RANGE_XZ = 5;
    private static final int WARDEN_SPAWN_RANGE_Y = 6;
    private static final int DARKNESS_RADIUS = 40;
    private static final int SHRIEKING_TICKS = 90;
    private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(1, (Object)SoundEvents.WARDEN_NEARBY_CLOSE);
        int2ObjectOpenHashMap.put(2, (Object)SoundEvents.WARDEN_NEARBY_CLOSER);
        int2ObjectOpenHashMap.put(3, (Object)SoundEvents.WARDEN_NEARBY_CLOSEST);
        int2ObjectOpenHashMap.put(4, (Object)SoundEvents.WARDEN_LISTENING_ANGRY);
    });
    private int warningLevel;
    private final VibrationSystem.User vibrationUser = new VibrationUser();
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

    public SculkShriekerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SCULK_SHRIEKER, blockPos, blockState);
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        if (compoundTag.contains("warning_level", 99)) {
            this.warningLevel = compoundTag.getInt("warning_level");
        }
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        if (compoundTag.contains("listener", 10)) {
            VibrationSystem.Data.CODEC.parse(registryOps, (Object)compoundTag.getCompound("listener")).resultOrPartial(string -> LOGGER.error("Failed to parse vibration listener for Sculk Shrieker: '{}'", string)).ifPresent(data -> {
                this.vibrationData = data;
            });
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.putInt("warning_level", this.warningLevel);
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        VibrationSystem.Data.CODEC.encodeStart(registryOps, (Object)this.vibrationData).resultOrPartial(string -> LOGGER.error("Failed to encode vibration listener for Sculk Shrieker: '{}'", string)).ifPresent(tag -> compoundTag.put("listener", (Tag)tag));
    }

    @Nullable
    public static ServerPlayer tryGetPlayer(@Nullable Entity entity) {
        Entity entity2;
        Entity entity3;
        LivingEntity livingEntity;
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            return serverPlayer;
        }
        if (entity != null && (livingEntity = entity.getControllingPassenger()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            return serverPlayer;
        }
        if (entity instanceof Projectile && (entity3 = ((Projectile)(entity2 = (Projectile)entity)).getOwner()) instanceof ServerPlayer) {
            livingEntity = (ServerPlayer)entity3;
            return livingEntity;
        }
        if (entity instanceof ItemEntity && (entity3 = ((ItemEntity)(entity2 = (ItemEntity)entity)).getOwner()) instanceof ServerPlayer) {
            livingEntity = (ServerPlayer)entity3;
            return livingEntity;
        }
        return null;
    }

    public void tryShriek(ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer) {
        if (serverPlayer == null) {
            return;
        }
        BlockState blockState = this.getBlockState();
        if (blockState.getValue(SculkShriekerBlock.SHRIEKING).booleanValue()) {
            return;
        }
        this.warningLevel = 0;
        if (this.canRespond(serverLevel) && !this.tryToWarn(serverLevel, serverPlayer)) {
            return;
        }
        this.shriek(serverLevel, serverPlayer);
    }

    private boolean tryToWarn(ServerLevel serverLevel, ServerPlayer serverPlayer) {
        OptionalInt optionalInt = WardenSpawnTracker.tryWarn(serverLevel, this.getBlockPos(), serverPlayer);
        optionalInt.ifPresent(n -> {
            this.warningLevel = n;
        });
        return optionalInt.isPresent();
    }

    private void shriek(ServerLevel serverLevel, @Nullable Entity entity) {
        BlockPos blockPos = this.getBlockPos();
        BlockState blockState = this.getBlockState();
        serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(SculkShriekerBlock.SHRIEKING, true), 2);
        serverLevel.scheduleTick(blockPos, blockState.getBlock(), 90);
        serverLevel.levelEvent(3007, blockPos, 0);
        serverLevel.gameEvent(GameEvent.SHRIEK, blockPos, GameEvent.Context.of(entity));
    }

    private boolean canRespond(ServerLevel serverLevel) {
        return this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON) != false && serverLevel.getDifficulty() != Difficulty.PEACEFUL && serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING);
    }

    public void tryRespond(ServerLevel serverLevel) {
        if (this.canRespond(serverLevel) && this.warningLevel > 0) {
            if (!this.trySummonWarden(serverLevel)) {
                this.playWardenReplySound(serverLevel);
            }
            Warden.applyDarknessAround(serverLevel, Vec3.atCenterOf(this.getBlockPos()), null, 40);
        }
    }

    private void playWardenReplySound(Level level) {
        SoundEvent soundEvent = (SoundEvent)SOUND_BY_LEVEL.get(this.warningLevel);
        if (soundEvent != null) {
            BlockPos blockPos = this.getBlockPos();
            int n = blockPos.getX() + Mth.randomBetweenInclusive(level.random, -10, 10);
            int n2 = blockPos.getY() + Mth.randomBetweenInclusive(level.random, -10, 10);
            int n3 = blockPos.getZ() + Mth.randomBetweenInclusive(level.random, -10, 10);
            level.playSound(null, (double)n, (double)n2, (double)n3, soundEvent, SoundSource.HOSTILE, 5.0f, 1.0f);
        }
    }

    private boolean trySummonWarden(ServerLevel serverLevel) {
        if (this.warningLevel < 4) {
            return false;
        }
        return SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, serverLevel, this.getBlockPos(), 20, 5, 6, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER).isPresent();
    }

    @Override
    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    @Override
    public /* synthetic */ GameEventListener getListener() {
        return this.getListener();
    }

    class VibrationUser
    implements VibrationSystem.User {
        private static final int LISTENER_RADIUS = 8;
        private final PositionSource positionSource;

        public VibrationUser() {
            this.positionSource = new BlockPositionSource(SculkShriekerBlockEntity.this.worldPosition);
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.SHRIEKER_CAN_LISTEN;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, GameEvent.Context context) {
            return SculkShriekerBlockEntity.this.getBlockState().getValue(SculkShriekerBlock.SHRIEKING) == false && SculkShriekerBlockEntity.tryGetPlayer(context.sourceEntity()) != null;
        }

        @Override
        public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity2, float f) {
            SculkShriekerBlockEntity.this.tryShriek(serverLevel, SculkShriekerBlockEntity.tryGetPlayer(entity2 != null ? entity2 : entity));
        }

        @Override
        public void onDataChanged() {
            SculkShriekerBlockEntity.this.setChanged();
        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}

