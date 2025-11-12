/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;

public class SwimNodeEvaluator
extends NodeEvaluator {
    private final boolean allowBreaching;
    private final Long2ObjectMap<PathType> pathTypesByPosCache = new Long2ObjectOpenHashMap();

    public SwimNodeEvaluator(boolean bl) {
        this.allowBreaching = bl;
    }

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.pathTypesByPosCache.clear();
    }

    @Override
    public void done() {
        super.done();
        this.pathTypesByPosCache.clear();
    }

    @Override
    public Node getStart() {
        return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ));
    }

    @Override
    public Target getTarget(double d, double d2, double d3) {
        return this.getTargetNodeAt(d, d2, d3);
    }

    @Override
    public int getNeighbors(Node[] nodeArray, Node node) {
        int n = 0;
        EnumMap enumMap = Maps.newEnumMap(Direction.class);
        for (Direction object : Direction.values()) {
            Node node2 = this.findAcceptedNode(node.x + object.getStepX(), node.y + object.getStepY(), node.z + object.getStepZ());
            enumMap.put(object, node2);
            if (!this.isNodeValid(node2)) continue;
            nodeArray[n++] = node2;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Node node3;
            Direction direction2 = direction.getClockWise();
            if (!SwimNodeEvaluator.hasMalus((Node)enumMap.get(direction)) || !SwimNodeEvaluator.hasMalus((Node)enumMap.get(direction2)) || !this.isNodeValid(node3 = this.findAcceptedNode(node.x + direction.getStepX() + direction2.getStepX(), node.y, node.z + direction.getStepZ() + direction2.getStepZ()))) continue;
            nodeArray[n++] = node3;
        }
        return n;
    }

    protected boolean isNodeValid(@Nullable Node node) {
        return node != null && !node.closed;
    }

    private static boolean hasMalus(@Nullable Node node) {
        return node != null && node.costMalus >= 0.0f;
    }

    @Nullable
    protected Node findAcceptedNode(int n, int n2, int n3) {
        float f;
        Node node = null;
        PathType pathType = this.getCachedBlockType(n, n2, n3);
        if ((this.allowBreaching && pathType == PathType.BREACH || pathType == PathType.WATER) && (f = this.mob.getPathfindingMalus(pathType)) >= 0.0f) {
            node = this.getNode(n, n2, n3);
            node.type = pathType;
            node.costMalus = Math.max(node.costMalus, f);
            if (this.currentContext.level().getFluidState(new BlockPos(n, n2, n3)).isEmpty()) {
                node.costMalus += 8.0f;
            }
        }
        return node;
    }

    protected PathType getCachedBlockType(int n, int n2, int n3) {
        return (PathType)((Object)this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(n, n2, n3), l -> this.getPathType(this.currentContext, n, n2, n3)));
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingContext, int n, int n2, int n3) {
        return this.getPathTypeOfMob(pathfindingContext, n, n2, n3, this.mob);
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext pathfindingContext, int n, int n2, int n3, Mob mob) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n + this.entityWidth; ++i) {
            for (int j = n2; j < n2 + this.entityHeight; ++j) {
                for (int k = n3; k < n3 + this.entityDepth; ++k) {
                    BlockState blockState = pathfindingContext.getBlockState(mutableBlockPos.set(i, j, k));
                    FluidState fluidState = blockState.getFluidState();
                    if (fluidState.isEmpty() && blockState.isPathfindable(PathComputationType.WATER) && blockState.isAir()) {
                        return PathType.BREACH;
                    }
                    if (fluidState.is(FluidTags.WATER)) continue;
                    return PathType.BLOCKED;
                }
            }
        }
        BlockState blockState = pathfindingContext.getBlockState(mutableBlockPos);
        if (blockState.isPathfindable(PathComputationType.WATER)) {
            return PathType.WATER;
        }
        return PathType.BLOCKED;
    }
}

