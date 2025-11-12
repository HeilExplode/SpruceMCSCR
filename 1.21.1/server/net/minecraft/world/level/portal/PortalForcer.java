/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalForcer {
    public static final int TICKET_RADIUS = 3;
    private static final int NETHER_PORTAL_RADIUS = 16;
    private static final int OVERWORLD_PORTAL_RADIUS = 128;
    private static final int FRAME_HEIGHT = 5;
    private static final int FRAME_WIDTH = 4;
    private static final int FRAME_BOX = 3;
    private static final int FRAME_HEIGHT_START = -1;
    private static final int FRAME_HEIGHT_END = 4;
    private static final int FRAME_WIDTH_START = -1;
    private static final int FRAME_WIDTH_END = 3;
    private static final int FRAME_BOX_START = -1;
    private static final int FRAME_BOX_END = 2;
    private static final int NOTHING_FOUND = -1;
    private final ServerLevel level;

    public PortalForcer(ServerLevel serverLevel) {
        this.level = serverLevel;
    }

    public Optional<BlockPos> findClosestPortalPosition(BlockPos blockPos3, boolean bl, WorldBorder worldBorder) {
        PoiManager poiManager = this.level.getPoiManager();
        int n = bl ? 16 : 128;
        poiManager.ensureLoadedAndValid(this.level, blockPos3, n);
        return poiManager.getInSquare(holder -> holder.is(PoiTypes.NETHER_PORTAL), blockPos3, n, PoiManager.Occupancy.ANY).map(PoiRecord::getPos).filter(worldBorder::isWithinBounds).filter(blockPos -> this.level.getBlockState((BlockPos)blockPos).hasProperty(BlockStateProperties.HORIZONTAL_AXIS)).min(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos3)).thenComparingInt(Vec3i::getY));
    }

    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos blockPos, Direction.Axis axis) {
        int n;
        int n2;
        int n3;
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d = -1.0;
        BlockPos blockPos2 = null;
        double d2 = -1.0;
        BlockPos blockPos3 = null;
        WorldBorder worldBorder = this.level.getWorldBorder();
        int n4 = Math.min(this.level.getMaxBuildHeight(), this.level.getMinBuildHeight() + this.level.getLogicalHeight()) - 1;
        boolean bl = true;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (BlockPos.MutableBlockPos mutableBlockPos2 : BlockPos.spiralAround(blockPos, 16, Direction.EAST, Direction.SOUTH)) {
            int n5 = Math.min(n4, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos2.getX(), mutableBlockPos2.getZ()));
            if (!worldBorder.isWithinBounds(mutableBlockPos2) || !worldBorder.isWithinBounds(mutableBlockPos2.move(direction, 1))) continue;
            mutableBlockPos2.move(direction.getOpposite(), 1);
            for (n3 = n5; n3 >= this.level.getMinBuildHeight(); --n3) {
                mutableBlockPos2.setY(n3);
                if (!this.canPortalReplaceBlock(mutableBlockPos2)) continue;
                n2 = n3;
                while (n3 > this.level.getMinBuildHeight() && this.canPortalReplaceBlock(mutableBlockPos2.move(Direction.DOWN))) {
                    --n3;
                }
                if (n3 + 4 > n4 || (n = n2 - n3) > 0 && n < 3) continue;
                mutableBlockPos2.setY(n3);
                if (!this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 0)) continue;
                double d3 = blockPos.distSqr(mutableBlockPos2);
                if (this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, -1) && this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 1) && (d == -1.0 || d > d3)) {
                    d = d3;
                    blockPos2 = mutableBlockPos2.immutable();
                }
                if (d != -1.0 || d2 != -1.0 && !(d2 > d3)) continue;
                d2 = d3;
                blockPos3 = mutableBlockPos2.immutable();
            }
        }
        if (d == -1.0 && d2 != -1.0) {
            blockPos2 = blockPos3;
            d = d2;
        }
        if (d == -1.0) {
            int n6 = n4 - 9;
            int n7 = Math.max(this.level.getMinBuildHeight() - -1, 70);
            if (n6 < n7) {
                return Optional.empty();
            }
            blockPos2 = new BlockPos(blockPos.getX() - direction.getStepX() * 1, Mth.clamp(blockPos.getY(), n7, n6), blockPos.getZ() - direction.getStepZ() * 1).immutable();
            blockPos2 = worldBorder.clampToBounds(blockPos2);
            Direction direction2 = direction.getClockWise();
            for (n3 = -1; n3 < 2; ++n3) {
                for (n2 = 0; n2 < 2; ++n2) {
                    for (n = -1; n < 3; ++n) {
                        BlockState blockState = n < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        mutableBlockPos.setWithOffset(blockPos2, n2 * direction.getStepX() + n3 * direction2.getStepX(), n, n2 * direction.getStepZ() + n3 * direction2.getStepZ());
                        this.level.setBlockAndUpdate(mutableBlockPos, blockState);
                    }
                }
            }
        }
        for (int i = -1; i < 3; ++i) {
            for (int j = -1; j < 4; ++j) {
                if (i != -1 && i != 2 && j != -1 && j != 3) continue;
                mutableBlockPos.setWithOffset(blockPos2, i * direction.getStepX(), j, i * direction.getStepZ());
                this.level.setBlock(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
        BlockState blockState = (BlockState)Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 3; ++j) {
                mutableBlockPos.setWithOffset(blockPos2, i * direction.getStepX(), j, i * direction.getStepZ());
                this.level.setBlock(mutableBlockPos, blockState, 18);
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(blockPos2.immutable(), 2, 3));
    }

    private boolean canPortalReplaceBlock(BlockPos.MutableBlockPos mutableBlockPos) {
        BlockState blockState = this.level.getBlockState(mutableBlockPos);
        return blockState.canBeReplaced() && blockState.getFluidState().isEmpty();
    }

    private boolean canHostFrame(BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int n) {
        Direction direction2 = direction.getClockWise();
        for (int i = -1; i < 3; ++i) {
            for (int j = -1; j < 4; ++j) {
                mutableBlockPos.setWithOffset(blockPos, direction.getStepX() * i + direction2.getStepX() * n, j, direction.getStepZ() * i + direction2.getStepZ() * n);
                if (j < 0 && !this.level.getBlockState(mutableBlockPos).isSolid()) {
                    return false;
                }
                if (j < 0 || this.canPortalReplaceBlock(mutableBlockPos)) continue;
                return false;
            }
        }
        return true;
    }
}

