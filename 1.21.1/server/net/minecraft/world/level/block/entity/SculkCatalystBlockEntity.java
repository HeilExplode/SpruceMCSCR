/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.Optionull;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity
extends BlockEntity
implements GameEventListener.Provider<CatalystListener> {
    private final CatalystListener catalystListener;

    public SculkCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SCULK_CATALYST, blockPos, blockState);
        this.catalystListener = new CatalystListener(blockState, new BlockPositionSource(blockPos));
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SculkCatalystBlockEntity sculkCatalystBlockEntity) {
        sculkCatalystBlockEntity.catalystListener.getSculkSpreader().updateCursors(level, blockPos, level.getRandom(), true);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.catalystListener.sculkSpreader.load(compoundTag);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.catalystListener.sculkSpreader.save(compoundTag);
        super.saveAdditional(compoundTag, provider);
    }

    @Override
    public CatalystListener getListener() {
        return this.catalystListener;
    }

    @Override
    public /* synthetic */ GameEventListener getListener() {
        return this.getListener();
    }

    public static class CatalystListener
    implements GameEventListener {
        public static final int PULSE_TICKS = 8;
        final SculkSpreader sculkSpreader;
        private final BlockState blockState;
        private final PositionSource positionSource;

        public CatalystListener(BlockState blockState, PositionSource positionSource) {
            this.blockState = blockState;
            this.positionSource = positionSource;
            this.sculkSpreader = SculkSpreader.createLevelSpreader();
        }

        @Override
        public PositionSource getListenerSource() {
            return this.positionSource;
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public GameEventListener.DeliveryMode getDeliveryMode() {
            return GameEventListener.DeliveryMode.BY_DISTANCE;
        }

        @Override
        public boolean handleGameEvent(ServerLevel serverLevel, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec32) {
            Object object;
            if (holder.is(GameEvent.ENTITY_DIE) && (object = context.sourceEntity()) instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)object;
                if (!livingEntity.wasExperienceConsumed()) {
                    object = livingEntity.getLastDamageSource();
                    int n = livingEntity.getExperienceReward(serverLevel, Optionull.map(object, DamageSource::getEntity));
                    if (livingEntity.shouldDropExperience() && n > 0) {
                        this.sculkSpreader.addCursors(BlockPos.containing(vec32.relative(Direction.UP, 0.5)), n);
                        this.tryAwardItSpreadsAdvancement(serverLevel, livingEntity);
                    }
                    livingEntity.skipDropExperience();
                    this.positionSource.getPosition(serverLevel).ifPresent(vec3 -> this.bloom(serverLevel, BlockPos.containing(vec3), this.blockState, serverLevel.getRandom()));
                }
                return true;
            }
            return false;
        }

        @VisibleForTesting
        public SculkSpreader getSculkSpreader() {
            return this.sculkSpreader;
        }

        private void bloom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(SculkCatalystBlock.PULSE, true), 3);
            serverLevel.scheduleTick(blockPos, blockState.getBlock(), 8);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.15, (double)blockPos.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0);
            serverLevel.playSound(null, blockPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0f, 0.6f + randomSource.nextFloat() * 0.4f);
        }

        private void tryAwardItSpreadsAdvancement(Level level, LivingEntity livingEntity) {
            LivingEntity livingEntity2 = livingEntity.getLastHurtByMob();
            if (livingEntity2 instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity2;
                DamageSource damageSource = livingEntity.getLastDamageSource() == null ? level.damageSources().playerAttack(serverPlayer) : livingEntity.getLastDamageSource();
                CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverPlayer, livingEntity, damageSource);
            }
        }
    }
}

