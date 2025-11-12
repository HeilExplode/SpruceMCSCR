/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;

public class FlyNodeEvaluator
extends WalkNodeEvaluator {
    private final Long2ObjectMap<PathType> pathTypeByPosCache = new Long2ObjectOpenHashMap();
    private static final float SMALL_MOB_SIZE = 1.0f;
    private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.1f;
    private static final int MAX_START_NODE_CANDIDATES = 10;

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.pathTypeByPosCache.clear();
        mob.onPathfindingStart();
    }

    @Override
    public void done() {
        this.mob.onPathfindingDone();
        this.pathTypeByPosCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        BlockPos blockPos;
        int n;
        if (this.canFloat() && this.mob.isInWater()) {
            n = this.mob.getBlockY();
            blockPos = new BlockPos.MutableBlockPos(this.mob.getX(), (double)n, this.mob.getZ());
            Object object = this.currentContext.getBlockState(blockPos);
            while (((BlockBehaviour.BlockStateBase)object).is(Blocks.WATER)) {
                ((BlockPos.MutableBlockPos)blockPos).set(this.mob.getX(), ++n, this.mob.getZ());
                object = this.currentContext.getBlockState(blockPos);
            }
        } else {
            n = Mth.floor(this.mob.getY() + 0.5);
        }
        if (!this.canStartAt(blockPos = BlockPos.containing(this.mob.getX(), n, this.mob.getZ()))) {
            for (BlockPos blockPos2 : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
                if (!this.canStartAt(blockPos2)) continue;
                return super.getStartNode(blockPos2);
            }
        }
        return super.getStartNode(blockPos);
    }

    @Override
    protected boolean canStartAt(BlockPos blockPos) {
        PathType pathType = this.getCachedPathType(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return this.mob.getPathfindingMalus(pathType) >= 0.0f;
    }

    @Override
    public Target getTarget(double d, double d2, double d3) {
        return this.getTargetNodeAt(d, d2, d3);
    }

    @Override
    public int getNeighbors(Node[] nodeArray, Node node) {
        Node node2;
        Node node3;
        Node node4;
        Node node5;
        Node node6;
        Node node7;
        Node node8;
        Node node9;
        Node node10;
        Node node11;
        Node node12;
        Node node13;
        Node node14;
        Node node15;
        Node node16;
        Node node17;
        Node node18;
        Node node19;
        Node node20;
        Node node21;
        Node node22;
        Node node23;
        Node node24;
        Node node25;
        Node node26;
        int n = 0;
        Node node27 = this.findAcceptedNode(node.x, node.y, node.z + 1);
        if (this.isOpen(node27)) {
            nodeArray[n++] = node27;
        }
        if (this.isOpen(node26 = this.findAcceptedNode(node.x - 1, node.y, node.z))) {
            nodeArray[n++] = node26;
        }
        if (this.isOpen(node25 = this.findAcceptedNode(node.x + 1, node.y, node.z))) {
            nodeArray[n++] = node25;
        }
        if (this.isOpen(node24 = this.findAcceptedNode(node.x, node.y, node.z - 1))) {
            nodeArray[n++] = node24;
        }
        if (this.isOpen(node23 = this.findAcceptedNode(node.x, node.y + 1, node.z))) {
            nodeArray[n++] = node23;
        }
        if (this.isOpen(node22 = this.findAcceptedNode(node.x, node.y - 1, node.z))) {
            nodeArray[n++] = node22;
        }
        if (this.isOpen(node21 = this.findAcceptedNode(node.x, node.y + 1, node.z + 1)) && this.hasMalus(node27) && this.hasMalus(node23)) {
            nodeArray[n++] = node21;
        }
        if (this.isOpen(node20 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z)) && this.hasMalus(node26) && this.hasMalus(node23)) {
            nodeArray[n++] = node20;
        }
        if (this.isOpen(node19 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z)) && this.hasMalus(node25) && this.hasMalus(node23)) {
            nodeArray[n++] = node19;
        }
        if (this.isOpen(node18 = this.findAcceptedNode(node.x, node.y + 1, node.z - 1)) && this.hasMalus(node24) && this.hasMalus(node23)) {
            nodeArray[n++] = node18;
        }
        if (this.isOpen(node17 = this.findAcceptedNode(node.x, node.y - 1, node.z + 1)) && this.hasMalus(node27) && this.hasMalus(node22)) {
            nodeArray[n++] = node17;
        }
        if (this.isOpen(node16 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z)) && this.hasMalus(node26) && this.hasMalus(node22)) {
            nodeArray[n++] = node16;
        }
        if (this.isOpen(node15 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z)) && this.hasMalus(node25) && this.hasMalus(node22)) {
            nodeArray[n++] = node15;
        }
        if (this.isOpen(node14 = this.findAcceptedNode(node.x, node.y - 1, node.z - 1)) && this.hasMalus(node24) && this.hasMalus(node22)) {
            nodeArray[n++] = node14;
        }
        if (this.isOpen(node13 = this.findAcceptedNode(node.x + 1, node.y, node.z - 1)) && this.hasMalus(node24) && this.hasMalus(node25)) {
            nodeArray[n++] = node13;
        }
        if (this.isOpen(node12 = this.findAcceptedNode(node.x + 1, node.y, node.z + 1)) && this.hasMalus(node27) && this.hasMalus(node25)) {
            nodeArray[n++] = node12;
        }
        if (this.isOpen(node11 = this.findAcceptedNode(node.x - 1, node.y, node.z - 1)) && this.hasMalus(node24) && this.hasMalus(node26)) {
            nodeArray[n++] = node11;
        }
        if (this.isOpen(node10 = this.findAcceptedNode(node.x - 1, node.y, node.z + 1)) && this.hasMalus(node27) && this.hasMalus(node26)) {
            nodeArray[n++] = node10;
        }
        if (this.isOpen(node9 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z - 1)) && this.hasMalus(node13) && this.hasMalus(node24) && this.hasMalus(node25) && this.hasMalus(node23) && this.hasMalus(node18) && this.hasMalus(node19)) {
            nodeArray[n++] = node9;
        }
        if (this.isOpen(node8 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z + 1)) && this.hasMalus(node12) && this.hasMalus(node27) && this.hasMalus(node25) && this.hasMalus(node23) && this.hasMalus(node21) && this.hasMalus(node19)) {
            nodeArray[n++] = node8;
        }
        if (this.isOpen(node7 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z - 1)) && this.hasMalus(node11) && this.hasMalus(node24) && this.hasMalus(node26) && this.hasMalus(node23) && this.hasMalus(node18) && this.hasMalus(node20)) {
            nodeArray[n++] = node7;
        }
        if (this.isOpen(node6 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z + 1)) && this.hasMalus(node10) && this.hasMalus(node27) && this.hasMalus(node26) && this.hasMalus(node23) && this.hasMalus(node21) && this.hasMalus(node20)) {
            nodeArray[n++] = node6;
        }
        if (this.isOpen(node5 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z - 1)) && this.hasMalus(node13) && this.hasMalus(node24) && this.hasMalus(node25) && this.hasMalus(node22) && this.hasMalus(node14) && this.hasMalus(node15)) {
            nodeArray[n++] = node5;
        }
        if (this.isOpen(node4 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z + 1)) && this.hasMalus(node12) && this.hasMalus(node27) && this.hasMalus(node25) && this.hasMalus(node22) && this.hasMalus(node17) && this.hasMalus(node15)) {
            nodeArray[n++] = node4;
        }
        if (this.isOpen(node3 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z - 1)) && this.hasMalus(node11) && this.hasMalus(node24) && this.hasMalus(node26) && this.hasMalus(node22) && this.hasMalus(node14) && this.hasMalus(node16)) {
            nodeArray[n++] = node3;
        }
        if (this.isOpen(node2 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z + 1)) && this.hasMalus(node10) && this.hasMalus(node27) && this.hasMalus(node26) && this.hasMalus(node22) && this.hasMalus(node17) && this.hasMalus(node16)) {
            nodeArray[n++] = node2;
        }
        return n;
    }

    private boolean hasMalus(@Nullable Node node) {
        return node != null && node.costMalus >= 0.0f;
    }

    private boolean isOpen(@Nullable Node node) {
        return node != null && !node.closed;
    }

    @Nullable
    protected Node findAcceptedNode(int n, int n2, int n3) {
        Node node = null;
        PathType pathType = this.getCachedPathType(n, n2, n3);
        float f = this.mob.getPathfindingMalus(pathType);
        if (f >= 0.0f) {
            node = this.getNode(n, n2, n3);
            node.type = pathType;
            node.costMalus = Math.max(node.costMalus, f);
            if (pathType == PathType.WALKABLE) {
                node.costMalus += 1.0f;
            }
        }
        return node;
    }

    @Override
    protected PathType getCachedPathType(int n, int n2, int n3) {
        return (PathType)((Object)this.pathTypeByPosCache.computeIfAbsent(BlockPos.asLong(n, n2, n3), l -> this.getPathTypeOfMob(this.currentContext, n, n2, n3, this.mob)));
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingContext, int n, int n2, int n3) {
        PathType pathType = pathfindingContext.getPathTypeFromState(n, n2, n3);
        if (pathType == PathType.OPEN && n2 >= pathfindingContext.level().getMinBuildHeight() + 1) {
            BlockPos blockPos = new BlockPos(n, n2 - 1, n3);
            PathType pathType2 = pathfindingContext.getPathTypeFromState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (pathType2 == PathType.DAMAGE_FIRE || pathType2 == PathType.LAVA) {
                pathType = PathType.DAMAGE_FIRE;
            } else if (pathType2 == PathType.DAMAGE_OTHER) {
                pathType = PathType.DAMAGE_OTHER;
            } else if (pathType2 == PathType.COCOA) {
                pathType = PathType.COCOA;
            } else if (pathType2 == PathType.FENCE) {
                if (!blockPos.equals(pathfindingContext.mobPosition())) {
                    pathType = PathType.FENCE;
                }
            } else {
                PathType pathType3 = pathType = pathType2 == PathType.WALKABLE || pathType2 == PathType.OPEN || pathType2 == PathType.WATER ? PathType.OPEN : PathType.WALKABLE;
            }
        }
        if (pathType == PathType.WALKABLE || pathType == PathType.OPEN) {
            pathType = FlyNodeEvaluator.checkNeighbourBlocks(pathfindingContext, n, n2, n3, pathType);
        }
        return pathType;
    }

    private Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions(Mob mob) {
        boolean bl;
        AABB aABB = mob.getBoundingBox();
        boolean bl2 = bl = aABB.getSize() < 1.0;
        if (!bl) {
            return List.of(BlockPos.containing(aABB.minX, mob.getBlockY(), aABB.minZ), BlockPos.containing(aABB.minX, mob.getBlockY(), aABB.maxZ), BlockPos.containing(aABB.maxX, mob.getBlockY(), aABB.minZ), BlockPos.containing(aABB.maxX, mob.getBlockY(), aABB.maxZ));
        }
        double d = Math.max(0.0, (double)1.1f - aABB.getZsize());
        double d2 = Math.max(0.0, (double)1.1f - aABB.getXsize());
        double d3 = Math.max(0.0, (double)1.1f - aABB.getYsize());
        AABB aABB2 = aABB.inflate(d2, d3, d);
        return BlockPos.randomBetweenClosed(mob.getRandom(), 10, Mth.floor(aABB2.minX), Mth.floor(aABB2.minY), Mth.floor(aABB2.minZ), Mth.floor(aABB2.maxX), Mth.floor(aABB2.maxY), Mth.floor(aABB2.maxZ));
    }
}

