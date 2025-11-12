/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
    public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> entityType, MobSpawnType mobSpawnType, ServerLevel serverLevel, BlockPos blockPos, int n, int n2, int n3, Strategy strategy) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 0; i < n; ++i) {
            Mob mob;
            int n4 = Mth.randomBetweenInclusive(serverLevel.random, -n2, n2);
            int n5 = Mth.randomBetweenInclusive(serverLevel.random, -n2, n2);
            mutableBlockPos.setWithOffset(blockPos, n4, n3, n5);
            if (!serverLevel.getWorldBorder().isWithinBounds(mutableBlockPos) || !SpawnUtil.moveToPossibleSpawnPosition(serverLevel, n3, mutableBlockPos, strategy) || (mob = (Mob)entityType.create(serverLevel, null, mutableBlockPos, mobSpawnType, false, false)) == null) continue;
            if (mob.checkSpawnRules(serverLevel, mobSpawnType) && mob.checkSpawnObstruction(serverLevel)) {
                serverLevel.addFreshEntityWithPassengers(mob);
                return Optional.of(mob);
            }
            mob.discard();
        }
        return Optional.empty();
    }

    private static boolean moveToPossibleSpawnPosition(ServerLevel serverLevel, int n, BlockPos.MutableBlockPos mutableBlockPos, Strategy strategy) {
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos().set(mutableBlockPos);
        BlockState blockState = serverLevel.getBlockState(mutableBlockPos2);
        for (int i = n; i >= -n; --i) {
            mutableBlockPos.move(Direction.DOWN);
            mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, Direction.UP);
            BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
            if (strategy.canSpawnOn(serverLevel, mutableBlockPos, blockState2, mutableBlockPos2, blockState)) {
                mutableBlockPos.move(Direction.UP);
                return true;
            }
            blockState = blockState2;
        }
        return false;
    }

    public static interface Strategy {
        @Deprecated
        public static final Strategy LEGACY_IRON_GOLEM = (serverLevel, blockPos, blockState, blockPos2, blockState2) -> {
            if (blockState.is(Blocks.COBWEB) || blockState.is(Blocks.CACTUS) || blockState.is(Blocks.GLASS_PANE) || blockState.getBlock() instanceof StainedGlassPaneBlock || blockState.getBlock() instanceof StainedGlassBlock || blockState.getBlock() instanceof LeavesBlock || blockState.is(Blocks.CONDUIT) || blockState.is(Blocks.ICE) || blockState.is(Blocks.TNT) || blockState.is(Blocks.GLOWSTONE) || blockState.is(Blocks.BEACON) || blockState.is(Blocks.SEA_LANTERN) || blockState.is(Blocks.FROSTED_ICE) || blockState.is(Blocks.TINTED_GLASS) || blockState.is(Blocks.GLASS)) {
                return false;
            }
            return !(!blockState2.isAir() && !blockState2.liquid() || !blockState.isSolid() && !blockState.is(Blocks.POWDER_SNOW));
        };
        public static final Strategy ON_TOP_OF_COLLIDER = (serverLevel, blockPos, blockState, blockPos2, blockState2) -> blockState2.getCollisionShape(serverLevel, blockPos2).isEmpty() && Block.isFaceFull(blockState.getCollisionShape(serverLevel, blockPos), Direction.UP);

        public boolean canSpawnOn(ServerLevel var1, BlockPos var2, BlockState var3, BlockPos var4, BlockState var5);
    }
}

