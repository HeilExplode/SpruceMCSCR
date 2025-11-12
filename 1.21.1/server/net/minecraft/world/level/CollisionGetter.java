/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionGetter
extends BlockGetter {
    public WorldBorder getWorldBorder();

    @Nullable
    public BlockGetter getChunkForCollisions(int var1, int var2);

    default public boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        return true;
    }

    default public boolean isUnobstructed(BlockState blockState, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos, collisionContext);
        return voxelShape.isEmpty() || this.isUnobstructed(null, voxelShape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    default public boolean isUnobstructed(Entity entity) {
        return this.isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
    }

    default public boolean noCollision(AABB aABB) {
        return this.noCollision(null, aABB);
    }

    default public boolean noCollision(Entity entity) {
        return this.noCollision(entity, entity.getBoundingBox());
    }

    default public boolean noCollision(@Nullable Entity entity, AABB aABB) {
        for (VoxelShape voxelShape : this.getBlockCollisions(entity, aABB)) {
            if (voxelShape.isEmpty()) continue;
            return false;
        }
        if (!this.getEntityCollisions(entity, aABB).isEmpty()) {
            return false;
        }
        if (entity != null) {
            VoxelShape voxelShape = this.borderCollision(entity, aABB);
            return voxelShape == null || !Shapes.joinIsNotEmpty(voxelShape, Shapes.create(aABB), BooleanOp.AND);
        }
        return true;
    }

    default public boolean noBlockCollision(@Nullable Entity entity, AABB aABB) {
        for (VoxelShape voxelShape : this.getBlockCollisions(entity, aABB)) {
            if (voxelShape.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public List<VoxelShape> getEntityCollisions(@Nullable Entity var1, AABB var2);

    default public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB) {
        List<VoxelShape> list = this.getEntityCollisions(entity, aABB);
        Iterable iterable = this.getBlockCollisions(entity, aABB);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB) {
        return () -> new BlockCollisions<VoxelShape>(this, entity, aABB, false, (mutableBlockPos, voxelShape) -> voxelShape);
    }

    @Nullable
    private VoxelShape borderCollision(Entity entity, AABB aABB) {
        WorldBorder worldBorder = this.getWorldBorder();
        return worldBorder.isInsideCloseToBorder(entity, aABB) ? worldBorder.getCollisionShape() : null;
    }

    default public boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AABB aABB) {
        BlockCollisions<VoxelShape> blockCollisions = new BlockCollisions<VoxelShape>(this, entity, aABB, true, (mutableBlockPos, voxelShape) -> voxelShape);
        while (blockCollisions.hasNext()) {
            if (((VoxelShape)blockCollisions.next()).isEmpty()) continue;
            return true;
        }
        return false;
    }

    default public Optional<BlockPos> findSupportingBlock(Entity entity, AABB aABB) {
        BlockPos blockPos = null;
        double d = Double.MAX_VALUE;
        BlockCollisions<BlockPos> blockCollisions = new BlockCollisions<BlockPos>(this, entity, aABB, false, (mutableBlockPos, voxelShape) -> mutableBlockPos);
        while (blockCollisions.hasNext()) {
            BlockPos blockPos2 = (BlockPos)blockCollisions.next();
            double d2 = blockPos2.distToCenterSqr(entity.position());
            if (!(d2 < d) && (d2 != d || blockPos != null && blockPos.compareTo(blockPos2) >= 0)) continue;
            blockPos = blockPos2.immutable();
            d = d2;
        }
        return Optional.ofNullable(blockPos);
    }

    default public Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape voxelShape2, Vec3 vec3, double d, double d2, double d3) {
        if (voxelShape2.isEmpty()) {
            return Optional.empty();
        }
        AABB aABB2 = voxelShape2.bounds().inflate(d, d2, d3);
        VoxelShape voxelShape3 = StreamSupport.stream(this.getBlockCollisions(entity, aABB2).spliterator(), false).filter(voxelShape -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(voxelShape.bounds())).flatMap(voxelShape -> voxelShape.toAabbs().stream()).map(aABB -> aABB.inflate(d / 2.0, d2 / 2.0, d3 / 2.0)).map(Shapes::create).reduce(Shapes.empty(), Shapes::or);
        VoxelShape voxelShape4 = Shapes.join(voxelShape2, voxelShape3, BooleanOp.ONLY_FIRST);
        return voxelShape4.closestPointTo(vec3);
    }
}

