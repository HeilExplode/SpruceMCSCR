/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalShape {
    private static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;
    private static final BlockBehaviour.StatePredicate FRAME = (blockState, blockGetter, blockPos) -> blockState.is(Blocks.OBSIDIAN);
    private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0f;
    private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0;
    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
        return PortalShape.findPortalShape(levelAccessor, blockPos, portalShape -> portalShape.isValid() && portalShape.numPortalBlocks == 0, axis);
    }

    public static Optional<PortalShape> findPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Predicate<PortalShape> predicate, Direction.Axis axis) {
        Optional<PortalShape> optional = Optional.of(new PortalShape(levelAccessor, blockPos, axis)).filter(predicate);
        if (optional.isPresent()) {
            return optional;
        }
        Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(new PortalShape(levelAccessor, blockPos, axis2)).filter(predicate);
    }

    public PortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
        this.level = levelAccessor;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(blockPos);
        if (this.bottomLeft == null) {
            this.bottomLeft = blockPos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos blockPos) {
        int n = Math.max(this.level.getMinBuildHeight(), blockPos.getY() - 21);
        while (blockPos.getY() > n && PortalShape.isEmpty(this.level.getBlockState(blockPos.below()))) {
            blockPos = blockPos.below();
        }
        Direction direction = this.rightDir.getOpposite();
        int n2 = this.getDistanceUntilEdgeAboveFrame(blockPos, direction) - 1;
        if (n2 < 0) {
            return null;
        }
        return blockPos.relative(direction, n2);
    }

    private int calculateWidth() {
        int n = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        if (n < 2 || n > 21) {
            return 0;
        }
        return n;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos blockPos, Direction direction) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= 21; ++i) {
            mutableBlockPos.set(blockPos).move(direction, i);
            BlockState blockState = this.level.getBlockState(mutableBlockPos);
            if (!PortalShape.isEmpty(blockState)) {
                if (!FRAME.test(blockState, this.level, mutableBlockPos)) break;
                return i;
            }
            BlockState blockState2 = this.level.getBlockState(mutableBlockPos.move(Direction.DOWN));
            if (!FRAME.test(blockState2, this.level, mutableBlockPos)) break;
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n = this.getDistanceUntilTop(mutableBlockPos);
        if (n < 3 || n > 21 || !this.hasTopFrame(mutableBlockPos, n)) {
            return 0;
        }
        return n;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos mutableBlockPos, int n) {
        for (int i = 0; i < this.width; ++i) {
            BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.set(this.bottomLeft).move(Direction.UP, n).move(this.rightDir, i);
            if (FRAME.test(this.level.getBlockState(mutableBlockPos2), this.level, mutableBlockPos2)) continue;
            return false;
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos mutableBlockPos) {
        for (int i = 0; i < 21; ++i) {
            mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
            if (!FRAME.test(this.level.getBlockState(mutableBlockPos), this.level, mutableBlockPos)) {
                return i;
            }
            mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
            if (!FRAME.test(this.level.getBlockState(mutableBlockPos), this.level, mutableBlockPos)) {
                return i;
            }
            for (int j = 0; j < this.width; ++j) {
                mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
                BlockState blockState = this.level.getBlockState(mutableBlockPos);
                if (!PortalShape.isEmpty(blockState)) {
                    return i;
                }
                if (!blockState.is(Blocks.NETHER_PORTAL)) continue;
                ++this.numPortalBlocks;
            }
        }
        return 21;
    }

    private static boolean isEmpty(BlockState blockState) {
        return blockState.isAir() || blockState.is(BlockTags.FIRE) || blockState.is(Blocks.NETHER_PORTAL);
    }

    public boolean isValid() {
        return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortalBlocks() {
        BlockState blockState = (BlockState)Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach(blockPos -> this.level.setBlock((BlockPos)blockPos, blockState, 18));
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public static Vec3 getRelativePosition(BlockUtil.FoundRectangle foundRectangle, Direction.Axis axis, Vec3 vec3, EntityDimensions entityDimensions) {
        Direction.Axis axis2;
        double d;
        double d2;
        double d3 = (double)foundRectangle.axis1Size - (double)entityDimensions.width();
        double d4 = (double)foundRectangle.axis2Size - (double)entityDimensions.height();
        BlockPos blockPos = foundRectangle.minCorner;
        if (d3 > 0.0) {
            d2 = (double)blockPos.get(axis) + (double)entityDimensions.width() / 2.0;
            d = Mth.clamp(Mth.inverseLerp(vec3.get(axis) - d2, 0.0, d3), 0.0, 1.0);
        } else {
            d = 0.5;
        }
        if (d4 > 0.0) {
            axis2 = Direction.Axis.Y;
            d2 = Mth.clamp(Mth.inverseLerp(vec3.get(axis2) - (double)blockPos.get(axis2), 0.0, d4), 0.0, 1.0);
        } else {
            d2 = 0.0;
        }
        axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double d5 = vec3.get(axis2) - ((double)blockPos.get(axis2) + 0.5);
        return new Vec3(d, d2, d5);
    }

    public static Vec3 findCollisionFreePosition(Vec3 vec32, ServerLevel serverLevel, Entity entity, EntityDimensions entityDimensions) {
        if (entityDimensions.width() > 4.0f || entityDimensions.height() > 4.0f) {
            return vec32;
        }
        double d = (double)entityDimensions.height() / 2.0;
        Vec3 vec33 = vec32.add(0.0, d, 0.0);
        VoxelShape voxelShape = Shapes.create(AABB.ofSize(vec33, entityDimensions.width(), 0.0, entityDimensions.width()).expandTowards(0.0, 1.0, 0.0).inflate(1.0E-6));
        Optional<Vec3> optional = serverLevel.findFreePosition(entity, voxelShape, vec33, entityDimensions.width(), entityDimensions.height(), entityDimensions.width());
        Optional<Vec3> optional2 = optional.map(vec3 -> vec3.subtract(0.0, d, 0.0));
        return optional2.orElse(vec32);
    }
}

