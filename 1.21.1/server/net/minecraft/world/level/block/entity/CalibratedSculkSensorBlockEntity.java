/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class CalibratedSculkSensorBlockEntity
extends SculkSensorBlockEntity {
    public CalibratedSculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CALIBRATED_SCULK_SENSOR, blockPos, blockState);
    }

    @Override
    public VibrationSystem.User createVibrationUser() {
        return new VibrationUser(this.getBlockPos());
    }

    protected class VibrationUser
    extends SculkSensorBlockEntity.VibrationUser {
        public VibrationUser(BlockPos blockPos) {
            super(CalibratedSculkSensorBlockEntity.this, blockPos);
        }

        @Override
        public int getListenerRadius() {
            return 16;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable GameEvent.Context context) {
            int n = this.getBackSignal(serverLevel, this.blockPos, CalibratedSculkSensorBlockEntity.this.getBlockState());
            if (n != 0 && VibrationSystem.getGameEventFrequency(holder) != n) {
                return false;
            }
            return super.canReceiveVibration(serverLevel, blockPos, holder, context);
        }

        private int getBackSignal(Level level, BlockPos blockPos, BlockState blockState) {
            Direction direction = blockState.getValue(CalibratedSculkSensorBlock.FACING).getOpposite();
            return level.getSignal(blockPos.relative(direction), direction);
        }
    }
}

