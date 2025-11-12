/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathTypeCache;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class PathfindingContext {
    private final CollisionGetter level;
    @Nullable
    private final PathTypeCache cache;
    private final BlockPos mobPosition;
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public PathfindingContext(CollisionGetter collisionGetter, Mob mob) {
        this.level = collisionGetter;
        Level level = mob.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.cache = serverLevel.getPathTypeCache();
        } else {
            this.cache = null;
        }
        this.mobPosition = mob.blockPosition();
    }

    public PathType getPathTypeFromState(int n, int n2, int n3) {
        BlockPos.MutableBlockPos mutableBlockPos = this.mutablePos.set(n, n2, n3);
        if (this.cache == null) {
            return WalkNodeEvaluator.getPathTypeFromState(this.level, mutableBlockPos);
        }
        return this.cache.getOrCompute(this.level, mutableBlockPos);
    }

    public BlockState getBlockState(BlockPos blockPos) {
        return this.level.getBlockState(blockPos);
    }

    public CollisionGetter level() {
        return this.level;
    }

    public BlockPos mobPosition() {
        return this.mobPosition;
    }
}

