/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealableBlock {
    public boolean isValidBonemealTarget(LevelReader var1, BlockPos var2, BlockState var3);

    public boolean isBonemealSuccess(Level var1, RandomSource var2, BlockPos var3, BlockState var4);

    public void performBonemeal(ServerLevel var1, RandomSource var2, BlockPos var3, BlockState var4);

    default public BlockPos getParticlePos(BlockPos blockPos) {
        return switch (this.getType().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> blockPos.above();
            case 1 -> blockPos;
        };
    }

    default public Type getType() {
        return Type.GROWER;
    }

    public static enum Type {
        NEIGHBOR_SPREADER,
        GROWER;

    }
}

