/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator
extends NodeEvaluator {
    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
    private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125;
    private final Long2ObjectMap<PathType> pathTypesByPosCacheByMob = new Long2ObjectOpenHashMap();
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap();
    private final Node[] reusableNeighbors = new Node[Direction.Plane.HORIZONTAL.length()];

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        mob.onPathfindingStart();
    }

    @Override
    public void done() {
        this.mob.onPathfindingDone();
        this.pathTypesByPosCacheByMob.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        Object object;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n = this.mob.getBlockY();
        BlockState blockState = this.currentContext.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)n, this.mob.getZ()));
        if (this.mob.canStandOnFluid(blockState.getFluidState())) {
            while (this.mob.canStandOnFluid(blockState.getFluidState())) {
                blockState = this.currentContext.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++n), this.mob.getZ()));
            }
            --n;
        } else if (this.canFloat() && this.mob.isInWater()) {
            while (blockState.is(Blocks.WATER) || blockState.getFluidState() == Fluids.WATER.getSource(false)) {
                blockState = this.currentContext.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++n), this.mob.getZ()));
            }
            --n;
        } else if (this.mob.onGround()) {
            n = Mth.floor(this.mob.getY() + 0.5);
        } else {
            mutableBlockPos.set(this.mob.getX(), this.mob.getY() + 1.0, this.mob.getZ());
            while (mutableBlockPos.getY() > this.currentContext.level().getMinBuildHeight()) {
                n = mutableBlockPos.getY();
                mutableBlockPos.setY(mutableBlockPos.getY() - 1);
                object = this.currentContext.getBlockState(mutableBlockPos);
                if (((BlockBehaviour.BlockStateBase)object).isAir() || ((BlockBehaviour.BlockStateBase)object).isPathfindable(PathComputationType.LAND)) continue;
                break;
            }
        }
        object = this.mob.blockPosition();
        if (!this.canStartAt(mutableBlockPos.set(((Vec3i)object).getX(), n, ((Vec3i)object).getZ()))) {
            AABB aABB = this.mob.getBoundingBox();
            if (this.canStartAt(mutableBlockPos.set(aABB.minX, (double)n, aABB.minZ)) || this.canStartAt(mutableBlockPos.set(aABB.minX, (double)n, aABB.maxZ)) || this.canStartAt(mutableBlockPos.set(aABB.maxX, (double)n, aABB.minZ)) || this.canStartAt(mutableBlockPos.set(aABB.maxX, (double)n, aABB.maxZ))) {
                return this.getStartNode(mutableBlockPos);
            }
        }
        return this.getStartNode(new BlockPos(((Vec3i)object).getX(), n, ((Vec3i)object).getZ()));
    }

    protected Node getStartNode(BlockPos blockPos) {
        Node node = this.getNode(blockPos);
        node.type = this.getCachedPathType(node.x, node.y, node.z);
        node.costMalus = this.mob.getPathfindingMalus(node.type);
        return node;
    }

    protected boolean canStartAt(BlockPos blockPos) {
        PathType pathType = this.getCachedPathType(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return pathType != PathType.OPEN && this.mob.getPathfindingMalus(pathType) >= 0.0f;
    }

    @Override
    public Target getTarget(double d, double d2, double d3) {
        return this.getTargetNodeAt(d, d2, d3);
    }

    @Override
    public int getNeighbors(Node[] nodeArray, Node node) {
        Object object;
        int n = 0;
        int n2 = 0;
        PathType pathType = this.getCachedPathType(node.x, node.y + 1, node.z);
        PathType pathType2 = this.getCachedPathType(node.x, node.y, node.z);
        if (this.mob.getPathfindingMalus(pathType) >= 0.0f && pathType2 != PathType.STICKY_HONEY) {
            n2 = Mth.floor(Math.max(1.0f, this.mob.maxUpStep()));
        }
        double d = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.reusableNeighbors[direction.get2DDataValue()] = object = this.findAcceptedNode(node.x + direction.getStepX(), node.y, node.z + direction.getStepZ(), n2, d, direction, pathType2);
            if (!this.isNeighborValid((Node)object, node)) continue;
            nodeArray[n++] = object;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Node node2;
            object = direction.getClockWise();
            if (!this.isDiagonalValid(node, this.reusableNeighbors[direction.get2DDataValue()], this.reusableNeighbors[((Direction)object).get2DDataValue()]) || !this.isDiagonalValid(node2 = this.findAcceptedNode(node.x + direction.getStepX() + ((Direction)object).getStepX(), node.y, node.z + direction.getStepZ() + ((Direction)object).getStepZ(), n2, d, direction, pathType2))) continue;
            nodeArray[n++] = node2;
        }
        return n;
    }

    protected boolean isNeighborValid(@Nullable Node node, Node node2) {
        return node != null && !node.closed && (node.costMalus >= 0.0f || node2.costMalus < 0.0f);
    }

    protected boolean isDiagonalValid(Node node, @Nullable Node node2, @Nullable Node node3) {
        if (node3 == null || node2 == null || node3.y > node.y || node2.y > node.y) {
            return false;
        }
        if (node2.type == PathType.WALKABLE_DOOR || node3.type == PathType.WALKABLE_DOOR) {
            return false;
        }
        boolean bl = node3.type == PathType.FENCE && node2.type == PathType.FENCE && (double)this.mob.getBbWidth() < 0.5;
        return (node3.y < node.y || node3.costMalus >= 0.0f || bl) && (node2.y < node.y || node2.costMalus >= 0.0f || bl);
    }

    protected boolean isDiagonalValid(@Nullable Node node) {
        if (node == null || node.closed) {
            return false;
        }
        if (node.type == PathType.WALKABLE_DOOR) {
            return false;
        }
        return node.costMalus >= 0.0f;
    }

    private static boolean doesBlockHavePartialCollision(PathType pathType) {
        return pathType == PathType.FENCE || pathType == PathType.DOOR_WOOD_CLOSED || pathType == PathType.DOOR_IRON_CLOSED;
    }

    private boolean canReachWithoutCollision(Node node) {
        AABB aABB = this.mob.getBoundingBox();
        Vec3 vec3 = new Vec3((double)node.x - this.mob.getX() + aABB.getXsize() / 2.0, (double)node.y - this.mob.getY() + aABB.getYsize() / 2.0, (double)node.z - this.mob.getZ() + aABB.getZsize() / 2.0);
        int n = Mth.ceil(vec3.length() / aABB.getSize());
        vec3 = vec3.scale(1.0f / (float)n);
        for (int i = 1; i <= n; ++i) {
            if (!this.hasCollisions(aABB = aABB.move(vec3))) continue;
            return false;
        }
        return true;
    }

    protected double getFloorLevel(BlockPos blockPos) {
        CollisionGetter collisionGetter = this.currentContext.level();
        if ((this.canFloat() || this.isAmphibious()) && collisionGetter.getFluidState(blockPos).is(FluidTags.WATER)) {
            return (double)blockPos.getY() + 0.5;
        }
        return WalkNodeEvaluator.getFloorLevel(collisionGetter, blockPos);
    }

    public static double getFloorLevel(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        VoxelShape voxelShape = blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2);
        return (double)blockPos2.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.max(Direction.Axis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected Node findAcceptedNode(int n, int n2, int n3, int n4, double d, Direction direction, PathType pathType) {
        Node node = null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        double d2 = this.getFloorLevel(mutableBlockPos.set(n, n2, n3));
        if (d2 - d > this.getMobJumpHeight()) {
            return null;
        }
        PathType pathType2 = this.getCachedPathType(n, n2, n3);
        float f = this.mob.getPathfindingMalus(pathType2);
        if (f >= 0.0f) {
            node = this.getNodeAndUpdateCostToMax(n, n2, n3, pathType2, f);
        }
        if (WalkNodeEvaluator.doesBlockHavePartialCollision(pathType) && node != null && node.costMalus >= 0.0f && !this.canReachWithoutCollision(node)) {
            node = null;
        }
        if (pathType2 == PathType.WALKABLE || this.isAmphibious() && pathType2 == PathType.WATER) {
            return node;
        }
        if ((node == null || node.costMalus < 0.0f) && n4 > 0 && (pathType2 != PathType.FENCE || this.canWalkOverFences()) && pathType2 != PathType.UNPASSABLE_RAIL && pathType2 != PathType.TRAPDOOR && pathType2 != PathType.POWDER_SNOW) {
            node = this.tryJumpOn(n, n2, n3, n4, d, direction, pathType, mutableBlockPos);
        } else if (!this.isAmphibious() && pathType2 == PathType.WATER && !this.canFloat()) {
            node = this.tryFindFirstNonWaterBelow(n, n2, n3, node);
        } else if (pathType2 == PathType.OPEN) {
            node = this.tryFindFirstGroundNodeBelow(n, n2, n3);
        } else if (WalkNodeEvaluator.doesBlockHavePartialCollision(pathType2) && node == null) {
            node = this.getClosedNode(n, n2, n3, pathType2);
        }
        return node;
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, (double)this.mob.maxUpStep());
    }

    private Node getNodeAndUpdateCostToMax(int n, int n2, int n3, PathType pathType, float f) {
        Node node = this.getNode(n, n2, n3);
        node.type = pathType;
        node.costMalus = Math.max(node.costMalus, f);
        return node;
    }

    private Node getBlockedNode(int n, int n2, int n3) {
        Node node = this.getNode(n, n2, n3);
        node.type = PathType.BLOCKED;
        node.costMalus = -1.0f;
        return node;
    }

    private Node getClosedNode(int n, int n2, int n3, PathType pathType) {
        Node node = this.getNode(n, n2, n3);
        node.closed = true;
        node.type = pathType;
        node.costMalus = pathType.getMalus();
        return node;
    }

    @Nullable
    private Node tryJumpOn(int n, int n2, int n3, int n4, double d, Direction direction, PathType pathType, BlockPos.MutableBlockPos mutableBlockPos) {
        Node node = this.findAcceptedNode(n, n2 + 1, n3, n4 - 1, d, direction, pathType);
        if (node == null) {
            return null;
        }
        if (this.mob.getBbWidth() >= 1.0f) {
            return node;
        }
        if (node.type != PathType.OPEN && node.type != PathType.WALKABLE) {
            return node;
        }
        double d2 = (double)(n - direction.getStepX()) + 0.5;
        double d3 = (double)(n3 - direction.getStepZ()) + 0.5;
        double d4 = (double)this.mob.getBbWidth() / 2.0;
        AABB aABB = new AABB(d2 - d4, this.getFloorLevel(mutableBlockPos.set(d2, (double)(n2 + 1), d3)) + 0.001, d3 - d4, d2 + d4, (double)this.mob.getBbHeight() + this.getFloorLevel(mutableBlockPos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002, d3 + d4);
        return this.hasCollisions(aABB) ? null : node;
    }

    @Nullable
    private Node tryFindFirstNonWaterBelow(int n, int n2, int n3, @Nullable Node node) {
        --n2;
        while (n2 > this.mob.level().getMinBuildHeight()) {
            PathType pathType = this.getCachedPathType(n, n2, n3);
            if (pathType != PathType.WATER) {
                return node;
            }
            node = this.getNodeAndUpdateCostToMax(n, n2, n3, pathType, this.mob.getPathfindingMalus(pathType));
            --n2;
        }
        return node;
    }

    private Node tryFindFirstGroundNodeBelow(int n, int n2, int n3) {
        for (int i = n2 - 1; i >= this.mob.level().getMinBuildHeight(); --i) {
            if (n2 - i > this.mob.getMaxFallDistance()) {
                return this.getBlockedNode(n, i, n3);
            }
            PathType pathType = this.getCachedPathType(n, i, n3);
            float f = this.mob.getPathfindingMalus(pathType);
            if (pathType == PathType.OPEN) continue;
            if (f >= 0.0f) {
                return this.getNodeAndUpdateCostToMax(n, i, n3, pathType, f);
            }
            return this.getBlockedNode(n, i, n3);
        }
        return this.getBlockedNode(n, n2, n3);
    }

    private boolean hasCollisions(AABB aABB) {
        return this.collisionCache.computeIfAbsent((Object)aABB, object -> !this.currentContext.level().noCollision(this.mob, aABB));
    }

    protected PathType getCachedPathType(int n, int n2, int n3) {
        return (PathType)((Object)this.pathTypesByPosCacheByMob.computeIfAbsent(BlockPos.asLong(n, n2, n3), l -> this.getPathTypeOfMob(this.currentContext, n, n2, n3, this.mob)));
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext pathfindingContext, int n, int n2, int n3, Mob mob) {
        Set<PathType> set = this.getPathTypeWithinMobBB(pathfindingContext, n, n2, n3);
        if (set.contains((Object)PathType.FENCE)) {
            return PathType.FENCE;
        }
        if (set.contains((Object)PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        }
        PathType pathType = PathType.BLOCKED;
        for (PathType pathType2 : set) {
            if (mob.getPathfindingMalus(pathType2) < 0.0f) {
                return pathType2;
            }
            if (!(mob.getPathfindingMalus(pathType2) >= mob.getPathfindingMalus(pathType))) continue;
            pathType = pathType2;
        }
        if (this.entityWidth <= 1 && pathType != PathType.OPEN && mob.getPathfindingMalus(pathType) == 0.0f && this.getPathType(pathfindingContext, n, n2, n3) == PathType.OPEN) {
            return PathType.OPEN;
        }
        return pathType;
    }

    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext pathfindingContext, int n, int n2, int n3) {
        EnumSet<PathType> enumSet = EnumSet.noneOf(PathType.class);
        for (int i = 0; i < this.entityWidth; ++i) {
            for (int j = 0; j < this.entityHeight; ++j) {
                for (int k = 0; k < this.entityDepth; ++k) {
                    int n4 = i + n;
                    int n5 = j + n2;
                    int n6 = k + n3;
                    PathType pathType = this.getPathType(pathfindingContext, n4, n5, n6);
                    BlockPos blockPos = this.mob.blockPosition();
                    boolean bl = this.canPassDoors();
                    if (pathType == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && bl) {
                        pathType = PathType.WALKABLE_DOOR;
                    }
                    if (pathType == PathType.DOOR_OPEN && !bl) {
                        pathType = PathType.BLOCKED;
                    }
                    if (pathType == PathType.RAIL && this.getPathType(pathfindingContext, blockPos.getX(), blockPos.getY(), blockPos.getZ()) != PathType.RAIL && this.getPathType(pathfindingContext, blockPos.getX(), blockPos.getY() - 1, blockPos.getZ()) != PathType.RAIL) {
                        pathType = PathType.UNPASSABLE_RAIL;
                    }
                    enumSet.add(pathType);
                }
            }
        }
        return enumSet;
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingContext, int n, int n2, int n3) {
        return WalkNodeEvaluator.getPathTypeStatic(pathfindingContext, new BlockPos.MutableBlockPos(n, n2, n3));
    }

    public static PathType getPathTypeStatic(Mob mob, BlockPos blockPos) {
        return WalkNodeEvaluator.getPathTypeStatic(new PathfindingContext(mob.level(), mob), blockPos.mutable());
    }

    public static PathType getPathTypeStatic(PathfindingContext pathfindingContext, BlockPos.MutableBlockPos mutableBlockPos) {
        int n;
        int n2;
        int n3 = mutableBlockPos.getX();
        PathType pathType = pathfindingContext.getPathTypeFromState(n3, n2 = mutableBlockPos.getY(), n = mutableBlockPos.getZ());
        if (pathType != PathType.OPEN || n2 < pathfindingContext.level().getMinBuildHeight() + 1) {
            return pathType;
        }
        return switch (pathfindingContext.getPathTypeFromState(n3, n2 - 1, n)) {
            case PathType.OPEN, PathType.WATER, PathType.LAVA, PathType.WALKABLE -> PathType.OPEN;
            case PathType.DAMAGE_FIRE -> PathType.DAMAGE_FIRE;
            case PathType.DAMAGE_OTHER -> PathType.DAMAGE_OTHER;
            case PathType.STICKY_HONEY -> PathType.STICKY_HONEY;
            case PathType.POWDER_SNOW -> PathType.DANGER_POWDER_SNOW;
            case PathType.DAMAGE_CAUTIOUS -> PathType.DAMAGE_CAUTIOUS;
            case PathType.TRAPDOOR -> PathType.DANGER_TRAPDOOR;
            default -> WalkNodeEvaluator.checkNeighbourBlocks(pathfindingContext, n3, n2, n, PathType.WALKABLE);
        };
    }

    public static PathType checkNeighbourBlocks(PathfindingContext pathfindingContext, int n, int n2, int n3, PathType pathType) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (i == 0 && k == 0) continue;
                    PathType pathType2 = pathfindingContext.getPathTypeFromState(n + i, n2 + j, n3 + k);
                    if (pathType2 == PathType.DAMAGE_OTHER) {
                        return PathType.DANGER_OTHER;
                    }
                    if (pathType2 == PathType.DAMAGE_FIRE || pathType2 == PathType.LAVA) {
                        return PathType.DANGER_FIRE;
                    }
                    if (pathType2 == PathType.WATER) {
                        return PathType.WATER_BORDER;
                    }
                    if (pathType2 != PathType.DAMAGE_CAUTIOUS) continue;
                    return PathType.DAMAGE_CAUTIOUS;
                }
            }
        }
        return pathType;
    }

    protected static PathType getPathTypeFromState(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (blockState.isAir()) {
            return PathType.OPEN;
        }
        if (blockState.is(BlockTags.TRAPDOORS) || blockState.is(Blocks.LILY_PAD) || blockState.is(Blocks.BIG_DRIPLEAF)) {
            return PathType.TRAPDOOR;
        }
        if (blockState.is(Blocks.POWDER_SNOW)) {
            return PathType.POWDER_SNOW;
        }
        if (blockState.is(Blocks.CACTUS) || blockState.is(Blocks.SWEET_BERRY_BUSH)) {
            return PathType.DAMAGE_OTHER;
        }
        if (blockState.is(Blocks.HONEY_BLOCK)) {
            return PathType.STICKY_HONEY;
        }
        if (blockState.is(Blocks.COCOA)) {
            return PathType.COCOA;
        }
        if (blockState.is(Blocks.WITHER_ROSE) || blockState.is(Blocks.POINTED_DRIPSTONE)) {
            return PathType.DAMAGE_CAUTIOUS;
        }
        FluidState fluidState = blockState.getFluidState();
        if (fluidState.is(FluidTags.LAVA)) {
            return PathType.LAVA;
        }
        if (WalkNodeEvaluator.isBurningBlock(blockState)) {
            return PathType.DAMAGE_FIRE;
        }
        if (block instanceof DoorBlock) {
            DoorBlock doorBlock = (DoorBlock)block;
            if (blockState.getValue(DoorBlock.OPEN).booleanValue()) {
                return PathType.DOOR_OPEN;
            }
            return doorBlock.type().canOpenByHand() ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED;
        }
        if (block instanceof BaseRailBlock) {
            return PathType.RAIL;
        }
        if (block instanceof LeavesBlock) {
            return PathType.LEAVES;
        }
        if (blockState.is(BlockTags.FENCES) || blockState.is(BlockTags.WALLS) || block instanceof FenceGateBlock && !blockState.getValue(FenceGateBlock.OPEN).booleanValue()) {
            return PathType.FENCE;
        }
        if (!blockState.isPathfindable(PathComputationType.LAND)) {
            return PathType.BLOCKED;
        }
        if (fluidState.is(FluidTags.WATER)) {
            return PathType.WATER;
        }
        return PathType.OPEN;
    }
}

