/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class AmphibiousNodeEvaluator
extends WalkNodeEvaluator {
    private final boolean prefersShallowSwimming;
    private float oldWalkableCost;
    private float oldWaterBorderCost;

    public AmphibiousNodeEvaluator(boolean bl) {
        this.prefersShallowSwimming = bl;
    }

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        mob.setPathfindingMalus(PathType.WATER, 0.0f);
        this.oldWalkableCost = mob.getPathfindingMalus(PathType.WALKABLE);
        mob.setPathfindingMalus(PathType.WALKABLE, 6.0f);
        this.oldWaterBorderCost = mob.getPathfindingMalus(PathType.WATER_BORDER);
        mob.setPathfindingMalus(PathType.WATER_BORDER, 4.0f);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(PathType.WALKABLE, this.oldWalkableCost);
        this.mob.setPathfindingMalus(PathType.WATER_BORDER, this.oldWaterBorderCost);
        super.done();
    }

    @Override
    public Node getStart() {
        if (!this.mob.isInWater()) {
            return super.getStart();
        }
        return this.getStartNode(new BlockPos(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ)));
    }

    @Override
    public Target getTarget(double d, double d2, double d3) {
        return this.getTargetNodeAt(d, d2 + 0.5, d3);
    }

    @Override
    public int getNeighbors(Node[] nodeArray, Node node) {
        int n = super.getNeighbors(nodeArray, node);
        PathType pathType = this.getCachedPathType(node.x, node.y + 1, node.z);
        PathType pathType2 = this.getCachedPathType(node.x, node.y, node.z);
        int n2 = this.mob.getPathfindingMalus(pathType) >= 0.0f && pathType2 != PathType.STICKY_HONEY ? Mth.floor(Math.max(1.0f, this.mob.maxUpStep())) : 0;
        double d = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));
        Node node2 = this.findAcceptedNode(node.x, node.y + 1, node.z, Math.max(0, n2 - 1), d, Direction.UP, pathType2);
        Node node3 = this.findAcceptedNode(node.x, node.y - 1, node.z, n2, d, Direction.DOWN, pathType2);
        if (this.isVerticalNeighborValid(node2, node)) {
            nodeArray[n++] = node2;
        }
        if (this.isVerticalNeighborValid(node3, node) && pathType2 != PathType.TRAPDOOR) {
            nodeArray[n++] = node3;
        }
        for (int i = 0; i < n; ++i) {
            Node node4 = nodeArray[i];
            if (node4.type != PathType.WATER || !this.prefersShallowSwimming || node4.y >= this.mob.level().getSeaLevel() - 10) continue;
            node4.costMalus += 1.0f;
        }
        return n;
    }

    private boolean isVerticalNeighborValid(@Nullable Node node, Node node2) {
        return this.isNeighborValid(node, node2) && node.type == PathType.WATER;
    }

    @Override
    protected boolean isAmphibious() {
        return true;
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingContext, int n, int n2, int n3) {
        PathType pathType = pathfindingContext.getPathTypeFromState(n, n2, n3);
        if (pathType == PathType.WATER) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                mutableBlockPos.set(n, n2, n3).move(direction);
                PathType pathType2 = pathfindingContext.getPathTypeFromState(mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ());
                if (pathType2 != PathType.BLOCKED) continue;
                return PathType.WATER_BORDER;
            }
            return PathType.WATER;
        }
        return super.getPathType(pathfindingContext, n, n2, n3);
    }
}

